package edu.boun.google_glass;
/*
 * Title:        Mobile cloudlet simulation
 * Description:  Example application used for simulating a google glass cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

public class MobileDeviceManager extends DatacenterBroker {
	private static final int READY_TO_SUBMIT_TASK = 0;
	private static final int READY_TO_RECEIVE_TASK = 1;
	private static int taskIdCounter;
	private int selectedVmId;
	
	public MobileDeviceManager(String name) throws Exception {
		super(name);
		selectedVmId = 0;
	}
	
	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();

		//SimLogger.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId() + " received");
		double Networkdelay = NetworkLinkModel.getInstance().getDelay();
		schedule(getId(), Networkdelay, READY_TO_RECEIVE_TASK, cloudlet);
		
		//getCloudletReceivedList().add(cloudlet);
		//cloudletsSubmitted--;
		//if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
		//	
		//}
		
		SimLogger.getInstance().updateTaskLogAsProcessed(cloudlet.getCloudletId(), Networkdelay);
	}

	/**
	 * Submit first cloudlet from the list.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitTask(SimSettings.TASK_TYPE taskType) {
		Cloudlet cloudlet = createTask(taskType);

		//Select VM one by one
		Vm vm = getVmsCreatedList().get((selectedVmId + 1) % getVmsCreatedList().size());
		getCloudletList().add(cloudlet);
		bindCloudletToVm(cloudlet.getCloudletId(),vm.getId());
		
		double Networkdelay = NetworkLinkModel.getInstance().getDelay();
		schedule(getId(), Networkdelay, READY_TO_SUBMIT_TASK, cloudlet);
		//--cloudletsSubmitted++;
		//getCloudletSubmittedList().add(cloudlet);
		//getCloudletList().remove(0);
		
		SimLogger.getInstance().addTaskLog(cloudlet.getCloudletId(), getDatacenterIdsList().get(0), CloudSim.clock(),Networkdelay);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		//do nothing!
	}
	
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			SimLogger.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		switch (ev.getTag()) {
		// Resource characteristics request
			case READY_TO_SUBMIT_TASK:
			{
				Cloudlet cloudlet = (Cloudlet) ev.getData();
				
				//check if related VM reaches max number of task
				int NumOfRunningCloudletOnVm = 0;
				for (Vm vm : getVmsCreatedList()) {
					if (vm.getId() == cloudlet.getVmId()) {
						NumOfRunningCloudletOnVm = vm.getCloudletScheduler().runningCloudlets();
						break;
					}
				}
				
				//Do not allow submitting new tasks if VM is full
				if(NumOfRunningCloudletOnVm > SimSettings.MAX_NUMBER_OF_TASKS_ON_VM){
					SimLogger.getInstance().updateTaskLogAsCancelled(cloudlet.getCloudletId(), CloudSim.clock());
				}
				else {
					SimLogger.getInstance().updateTaskLogAsUploaded(cloudlet.getCloudletId());
					sendNow(getVmsToDatacentersMap().get(cloudlet.getVmId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
				}
				break;
			}
			case READY_TO_RECEIVE_TASK:
			{
				Cloudlet cloudlet = (Cloudlet) ev.getData();
				SimLogger.getInstance().updateTaskLogAsDownloaded(cloudlet.getCloudletId(), CloudSim.clock());
				break;
			}
			default:
				SimLogger.printLine(getName() + ".processOtherEvent(): " + "Error - event unknown by this DatacenterBroker.");
				break;
		}
	}
	
	public void simulationEnded(){
		clearDatacenters();
		finishExecution();
	}
	
	private Cloudlet createTask(SimSettings.TASK_TYPE taskType){
		//cloudlet parameters
		long length = 2200;
		long inputOutputFileSize = 350000; //350 KB
		int pesNumber = 1;
		
		if(taskType == SimSettings.TASK_TYPE.MEDIUM_TASK){
			length = 4400;
		}
		else if(taskType == SimSettings.TASK_TYPE.HIGH_TASK){
			length = 6600;
		}

		UtilizationModel utilizationModel = new UtilizationModelFull();

		taskIdCounter++;
		Cloudlet task = new Cloudlet(taskIdCounter, length, pesNumber, inputOutputFileSize, inputOutputFileSize, utilizationModel, utilizationModel, utilizationModel);
		// setting the owner of these Cloudlets
		task.setUserId(this.getId());
		return task;
	}
}
