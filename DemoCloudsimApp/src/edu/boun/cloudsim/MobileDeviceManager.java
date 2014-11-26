package edu.boun.cloudsim;
/*
 * Title:        Home cloudlet simulation
 * Description:  Example application used for simulating a home cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

public class MobileDeviceManager extends DatacenterBroker {
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
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId() + " received");
		SimLogger.getInstance().updateLog(cloudlet.getCloudletId(), CloudSim.clock(), 0, 0);
		//getCloudletReceivedList().add(cloudlet);
		//cloudletsSubmitted--;
		//if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
		//	
		//}
	}

	/**
	 * Submit first cloudlet from the list.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitTask(SimSettings.TASK_TYPE taskType) {
		Cloudlet cloudlet = createTask(taskType);
		SimLogger.getInstance().addLog(cloudlet.getCloudletId(), getDatacenterIdsList().get(0), CloudSim.clock());
		
		//Select VM one by one
		Vm vm = getVmsCreatedList().get((selectedVmId + 1) % getVmsCreatedList().size());
		getCloudletList().add(cloudlet);
		bindCloudletToVm(cloudlet.getCloudletId(),vm.getId());
		
		sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
		//--cloudletsSubmitted++;
		//getCloudletSubmittedList().add(cloudlet);
		//getCloudletList().remove(0);
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
	
	public void simulationEnded(){
		clearDatacenters();
		finishExecution();
	}
	
	private Cloudlet createTask(SimSettings.TASK_TYPE taskType){
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		//cloudlet parameters
		long length = 25000;
		long inputOutputFileSize = 500;
		int pesNumber = 1;
		
		if(taskType == SimSettings.TASK_TYPE.MEDIUM_TASK){
			length = 50000;
			inputOutputFileSize = 1000;
		}
		else if(taskType == SimSettings.TASK_TYPE.HIGH_TASK){
			length = 75000;
			inputOutputFileSize = 1500;
		}

		UtilizationModel utilizationModel = new UtilizationModelFull();

		taskIdCounter++;
		Cloudlet task = new Cloudlet(taskIdCounter, length, pesNumber, inputOutputFileSize, inputOutputFileSize, utilizationModel, utilizationModel, utilizationModel);
		// setting the owner of these Cloudlets
		task.setUserId(this.getId());
		return task;
	}
}
