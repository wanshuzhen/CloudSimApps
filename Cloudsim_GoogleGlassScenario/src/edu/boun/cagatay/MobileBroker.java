/*
 * Title:        Home cloudlet simulation
 * Description:  Example application used for simulating a home cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */

package edu.boun.cagatay;

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
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

public class MobileBroker extends DatacenterBroker {
	private SimSettings.TASK_CPU_REQ cpuRequirement;
	private SimSettings.TASK_BW_REQ bwRequirement;
	private int taskId;
	
	public MobileBroker(String name, int _taskId,
			SimSettings.TASK_CPU_REQ _cpuRequirement,
			SimSettings.TASK_BW_REQ _bwRequirement) throws Exception {
		super(name+"_"+_taskId);

		taskId=_taskId;
		cpuRequirement=_cpuRequirement;
		bwRequirement=_bwRequirement;
		submitCloudletList(createTasks(5));
	}
	
	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId + " failed in Datacenter #" + datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName() + ": none of the required VMs could be created. Aborting");
					SimLogger.getInstance().addLog(taskId, getDatacenterIdsList().get(0), CloudSim.clock());
					finishExecution();
				}
			}
		}
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
		getCloudletReceivedList().add(cloudlet);
		//Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId() + " received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			double bwCost=0, cpuCost=0;
			for (Cloudlet task : getCloudletReceivedList()) {
				cpuCost += task.getCostPerSec() * task.getActualCPUTime();
				bwCost += task.getProcessingCost();
			}

			SimLogger.getInstance().updateLog(taskId, CloudSim.clock(), bwCost, cpuCost);
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			submitCloudlet();
		}
	}

	/**
	 * Submit first cloudlet from the list.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlet() {
		Cloudlet cloudlet = getCloudletList().get(0);

		Vm vm;
		// if user didn't bind this cloudlet and it has not been executed yet
		if (cloudlet.getVmId() == -1) {
			vm = getVmsCreatedList().get(0); //Chose always the first VM
		} else { // submit to the specific vm
			vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
			if (vm == null) { // vm was not created
				if(!Log.isDisabled()) {
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet " +
							cloudlet.getCloudletId() + ": bount VM not available");
				}
				return;
			}
		}

		cloudlet.setVmId(vm.getId());
		sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
		cloudletsSubmitted++;
		getCloudletSubmittedList().add(cloudlet);
		getCloudletList().remove(0);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		SimLogger.getInstance().addLog(taskId, getDatacenterIdsList().get(0), CloudSim.clock());
		submitCloudlet();
	}
	
	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		//CagatayS: Select proper datacenter anc virtual machine according to server selection settings
		SimSettings.SERVER_TYPE serverTypeToSelect = CloudServers.getInstance().getCloudServer();
		Vm vmToRequest = CloudServers.getInstance().getVM(serverTypeToSelect, getId());
		Datacenter serverToSelect = CloudServers.getInstance().getCloudServer(serverTypeToSelect);

		//CagatayS: Creates a container to store VMs. (we will use only one vm)
		LinkedList<Vm> vmList = new LinkedList<Vm>();
		vmList.add(vmToRequest);
		//CagatayS: pass vm list to the broker
		submitVmList(vmList);
		
		//CagatayS: Creates a container to store server list. (we will use only one server)
		LinkedList<Integer> serverList = new LinkedList<Integer>();
		serverList.add(serverToSelect.getId());
		//CagatayS: pass server list to the broker
		setDatacenterIdsList(serverList);
		
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with " + getDatacenterIdsList().size() + " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}
	
	private List<Cloudlet> createTasks(int cloudlets){
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		//cloudlet parameters
		long length = 25000;
		long inputOutputFileSize = 500;
		int pesNumber = 1;
		
		if(cpuRequirement == SimSettings.TASK_CPU_REQ.MEDIUM_UTULIZATION)
			length = 50000;
		else if(cpuRequirement == SimSettings.TASK_CPU_REQ.HIGH_UTULIZATION)
			length = 100000;
		
		if(bwRequirement == SimSettings.TASK_BW_REQ.MEDIUM_COMMUNICATON)
			inputOutputFileSize = 1000;
		else if(bwRequirement == SimSettings.TASK_BW_REQ.HIGH_COMMUNICATON)
			inputOutputFileSize = 2000;

		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
			cloudlet[i] = new Cloudlet(i, length, pesNumber, inputOutputFileSize, inputOutputFileSize, utilizationModel, utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(this.getId());
			list.add(cloudlet[i]);
		}

		return list;
	}
}
