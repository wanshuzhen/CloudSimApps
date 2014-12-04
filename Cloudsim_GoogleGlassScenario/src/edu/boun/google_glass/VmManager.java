package edu.boun.google_glass;
/*
 * Title:        Mobile cloudlet simulation
 * Description:  Example application used for simulating a google glass cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */

import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

public class VmManager {
	private List<Vm> cloudletVmList;
	private List<Vm> cloudVmList;
	private int vmIdCounter;

	public VmManager() {
		vmIdCounter = 0;
	}
	
	public List<Vm> getVmList(SimSettings.VM_TYPE vmType){
		if(vmType == SimSettings.VM_TYPE.CLOUD_VM)
			return cloudVmList;
		else
			return cloudletVmList;
	}
	
	public double getAvgCurrentNumberOfTask(SimSettings.VM_TYPE vmType){
		int result=0;
		List<Vm> vmList = getVmList(vmType);
		for (Vm vm : vmList)
			result += vm.getCloudletScheduler().runningCloudlets();

		return (double)result/(double)vmList.size();
	}
	
	public double getAvgCurrentRequestedMips(SimSettings.VM_TYPE vmType){
		int result=0;
		List<Vm> vmList = getVmList(vmType);
		for (Vm vm : vmList) {
			result += vm.getTotalUtilizationOfCpu(CloudSim.clock());
		}
		return (double)result/(double)vmList.size();
	}
	
	public void createVmList(SimSettings.VM_TYPE vmType, int numOfVm, int brokerId) {
		//VM Parameters
		long size = 0; //image size (MB)
		int mips = 0; //million instructions per second
		int ram = 0; //vm memory (MB)
		long bw = 0; //bandwidth (byte per seconds)
		int pesNumber = 0; //number of CPUs
		String vmm = ""; //VMM name
		
		if(vmType == SimSettings.VM_TYPE.CLOUDLET_VM){
			cloudletVmList = new LinkedList<Vm>();
			size = 10000;
			mips = 5000;
			ram = 16000;
			bw = 100000;
			pesNumber = 2;
			vmm = "Cloudlet";
		}
		else if(vmType == SimSettings.VM_TYPE.CLOUD_VM){
			cloudVmList = new LinkedList<Vm>();
			size = 100000;
			mips = 10000;
			ram = 64000;
			bw = 100;
			pesNumber = 32;
			vmm = "Cloud";
		}
		
		for(int i=0; i<numOfVm; i++){
			vmIdCounter++;
			if(vmType == SimSettings.VM_TYPE.CLOUDLET_VM)
				cloudletVmList.add(new Vm(vmIdCounter, brokerId, mips, pesNumber, ram, bw, size, vmm, new TaskSchedular()));
			else
				cloudVmList.add(new Vm(vmIdCounter, brokerId, mips, pesNumber, ram, bw, size, vmm, new TaskSchedular()));
		}
	}
}
