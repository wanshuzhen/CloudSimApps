package edu.boun.google_glass;
/*
 * Title:        Mobile cloudlet simulation
 * Description:  Example application used for simulating a google glass cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */


import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

public class SimManager extends SimEntity {
	private static final int CREATE_TASK = 0;
	private static final int GET_LOAD_LOG = 1;
	private static final int STOP_SIMULATION = 2;
	
	private PhysicalServerManager physicalServerManager;
	private MobileDeviceManager mobileDeviceManager;
	private VmManager vmManager;
	
	private SimSettings.VM_TYPE vmType;
	private PoissonDistr poissonRNG;
	private int numOfBroker;
	
	public SimManager(int numOfVm, int _numOfBroker, SimSettings.CLIENT_TYPE clientType, double poissonMean) {
		super("SimManager");
		numOfBroker=_numOfBroker;
		poissonRNG = new PoissonDistr(poissonMean);
		
		try {
			if(clientType == SimSettings.CLIENT_TYPE.CLOUD_WIFI_CLIENT)
				vmType = SimSettings.VM_TYPE.CLOUD_VM;
			else if(clientType == SimSettings.CLIENT_TYPE.CLOUDLET_WIFI_CLIENT)
				vmType = SimSettings.VM_TYPE.CLOUDLET_VM;
			
			//report simulation parameters to NetworkLinkModel
			NetworkLinkModel.getInstance().adjustSimSettings(clientType,poissonMean,(double)350000, _numOfBroker);
			
			//Create Physical Servers & Start Servers
			physicalServerManager = new PhysicalServerManager();
			physicalServerManager.startServers();
			
			//Create Client Manager
			mobileDeviceManager = new MobileDeviceManager("MobileDeviceManager");
			
			//Create VM manager & Generate VMs
			vmManager = new VmManager();
			vmManager.createVmList(vmType, numOfVm, mobileDeviceManager.getId());
			
			//maps CloudSim entities to BRITE entities
            //NetworkTopology.addLink(CloudServers.getInstance().getServer().getId(), mobileDeviceManager.getId(),donwlinkBw,donwlinkLatency);
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
		mobileDeviceManager.submitVmList(vmManager.getVmList(vmType));
		double brokerStartTime=30;
		for(int i=0; i<numOfBroker; i++)
		{
			brokerStartTime += poissonRNG.sample();
			schedule(getId(), brokerStartTime, CREATE_TASK);
		}
		schedule(getId(), SimSettings.INTERVAL_TO_GET_VM_LOAD_LOG, GET_LOAD_LOG);
		schedule(getId(), SimSettings.SIMULATION_TIME, STOP_SIMULATION);
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

					double randomNumber = poissonRNG.sample();
					schedule(getId(), randomNumber, CREATE_TASK);
//				}
				break;
			case GET_LOAD_LOG:
				double load = vmManager.getAvgCurrentNumberOfTask(vmType);
				//double load1 = vmManager.getAvgCurrentRequestedMips();
				SimLogger.getInstance().fixServerLoadLog(CloudSim.clock());
				schedule(getId(), SimSettings.INTERVAL_TO_GET_VM_LOAD_LOG, GET_LOAD_LOG);
				break;
			case STOP_SIMULATION:
				CloudSim.terminateSimulation();
				SimLogger.getInstance().simStopped();
				break;
			default:
				Log.printLine(getName() + ": unknown event type");
				break;
			}
		}
	}

	@Override
	public void shutdownEntity() {
		physicalServerManager.terminateServers();
		mobileDeviceManager.simulationEnded();
	}
}
