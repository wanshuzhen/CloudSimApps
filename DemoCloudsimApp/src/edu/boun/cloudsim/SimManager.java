package edu.boun.cloudsim;


import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
/*
 * Title:        Home cloudlet simulation
 * Description:  Example application used for simulating a home cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */

import org.cloudbus.cloudsim.core.SimEvent;

public class SimManager extends SimEntity {
	private static final int CREATE_TASK = 0;
	private static final int GET_LOAD_LOG = 1;
	private MobileDeviceManager mobileDeviceManager;
	private VmManager vmManager;
	private int numOfBroker;
	private PoissonDistr poissonRNG;
	
	public SimManager(int numOfVm, int _numOfBroker, SimSettings.VM_TYPE vmType, double poissonMean) {
		super("SimManager");
		numOfBroker=_numOfBroker;
		poissonRNG = new PoissonDistr(poissonMean);
		
		try {
			//Generate all servers
			CloudServers.getInstance().startServers();
			mobileDeviceManager = new MobileDeviceManager("MobileDeviceManager");
			VmManager.getInstance().createVmList(vmType, numOfVm, mobileDeviceManager.getId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Starts the simulation
		CloudSim.startSimulation();
	}

	@Override
	public void startEntity() {
		Log.printLine(super.getName()+" is starting...");
		mobileDeviceManager.submitVmList(VmManager.getInstance().getVmList());
		double brokerStartTime=30;
		for(int i=0; i<numOfBroker; i++)
		{
			brokerStartTime += poissonRNG.sample();
			schedule(getId(), brokerStartTime, CREATE_TASK);
		}
		schedule(getId(), SimSettings.INTERVAL_TO_GET_VM_LOAD_LOG, GET_LOAD_LOG);
	}

	@Override
	public void processEvent(SimEvent ev) {
		synchronized(this){
			switch (ev.getTag()) {
			case CREATE_TASK:
//				if(LastBrokerCreationTime == CloudSim.clock()){
//					Log.printLine("Cannot create brokers at the same time! Wait one seconds");
//					schedule(getId(), (double)1, CREATE_BROKER);
//				}
//				else {
//					LastBrokerCreationTime = CloudSim.clock();
					try {
						mobileDeviceManager.submitTask(SimSettings.getRandomTask());						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		
					//Check if simulation is ended
					if (CloudSim.clock() >= SimSettings.SIMULATION_TIME)
						CloudSim.stopSimulation();
					else {
						double randomNumber = poissonRNG.sample();
						CloudSim.resumeSimulation();
						schedule(getId(), randomNumber, CREATE_TASK);
					}
//				}
				break;
			case GET_LOAD_LOG:
				double load = vmManager.getInstance().getAvgCurrentNumberOfTask();
				//double load1 = vmManager.getInstance().getAvgCurrentRequestedMips();
				
				SimLogger.getInstance().addServerLoadLog(CloudSim.clock(), load);
				
				//reques to get new log if simulation is not ended
				if (CloudSim.clock() < SimSettings.SIMULATION_TIME)
					schedule(getId(), SimSettings.INTERVAL_TO_GET_VM_LOAD_LOG, GET_LOAD_LOG);
				
				break;
			default:
				Log.printLine(getName() + ": unknown event type");
				break;
			}
		}
	}

	@Override
	public void shutdownEntity() {
		CloudServers.getInstance().terminateServers();
	}
}
