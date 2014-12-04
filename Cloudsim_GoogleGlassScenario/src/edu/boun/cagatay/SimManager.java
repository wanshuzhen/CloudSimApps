package edu.boun.cagatay;

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
	private static final int CREATE_BROKER = 0;
	private static final int GET_SERVER_LOAD_LOG = 1;
	private int numOfBroker;
	private int brokerCounter;
	private double LastBrokerCreationTime;
	
	public SimManager(int _numOfBroker) {
		super("SimManager");
		numOfBroker = _numOfBroker;
		brokerCounter = 0;
		LastBrokerCreationTime = 0;
		
		//Generate all servers
		CloudServers.getInstance().startServers();
		
		//Starts the simulation
		CloudSim.startSimulation();
	}

	@Override
	public void startEntity() {
		Log.printLine(super.getName()+" is starting...");
		int brokerStartTime=0;
		for(int i=0; i<numOfBroker; i++)
		{
			brokerStartTime += 5;
			schedule(getId(), brokerStartTime, CREATE_BROKER);
		}
		schedule(getId(), SimSettings.INTERVAL_TO_GET_SERVER_LOAD_LOG, GET_SERVER_LOAD_LOG);
	}

	@Override
	public void processEvent(SimEvent ev) {
		synchronized(this){
			switch (ev.getTag()) {
			case CREATE_BROKER:
				if(LastBrokerCreationTime == CloudSim.clock()){
					Log.printLine("Cannot create brokers at the same time! Wait one seconds");
					schedule(getId(), (double)1, CREATE_BROKER);
				}
				else {
					LastBrokerCreationTime = CloudSim.clock();
					try {
						brokerCounter++;
						SimSettings.TASK_CPU_REQ randomCpuReq = SimSettings.getRandomTaskCpuReq();
						SimSettings.TASK_BW_REQ randomBwReq = SimSettings.getRandomTaskBwReq();
						
						@SuppressWarnings("unused")
						MobileBroker broker = new MobileBroker("MobileBroker",brokerCounter,randomCpuReq,randomBwReq);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		
					//Check if simulation is ended
					if (CloudSim.clock() >= SimSettings.SIMULATION_TIME)
						CloudSim.stopSimulation();
					else {
						int randomNumber = SimSettings.getRandomNumber(15, 45);
						CloudSim.resumeSimulation();
						schedule(getId(), randomNumber, CREATE_BROKER);
					}
				}
				break;
			case GET_SERVER_LOAD_LOG:
				double load1 = CloudServers.getInstance().getAvgUtilization(CloudServers.getInstance().getCloudServer(SimSettings.SERVER_TYPE.LOW_SERVER));
				double load2 = CloudServers.getInstance().getAvgUtilization(CloudServers.getInstance().getCloudServer(SimSettings.SERVER_TYPE.MEDIUM_SERVER));
				double load3 = CloudServers.getInstance().getAvgUtilization(CloudServers.getInstance().getCloudServer(SimSettings.SERVER_TYPE.HIGH_SERVER));
				SimLogger.getInstance().addServerLoadLog(CloudSim.clock(), load1, load2, load3);
				
				//reques to get new log if simulation is not ended
				if (CloudSim.clock() < SimSettings.SIMULATION_TIME)
					schedule(getId(), SimSettings.INTERVAL_TO_GET_SERVER_LOAD_LOG, GET_SERVER_LOAD_LOG);
				
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
