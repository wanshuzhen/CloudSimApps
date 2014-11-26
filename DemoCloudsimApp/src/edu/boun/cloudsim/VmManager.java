package edu.boun.cloudsim;


import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

public class VmManager {
	private List<Vm> vmList;
	private int vmIdCounter;

	private static VmManager instance = null;

	private VmManager() {
		vmIdCounter = 0;
	}
	
	public static VmManager getInstance() {
		if(instance == null) {
			instance = new VmManager();
		}
		return instance;
	}

	public List<Vm> getVmList(){
		return vmList;
	}
	
	public double getAvgCurrentNumberOfTask(){
		int result=0;
		for (Vm vm : vmList)
			result += vm.getCloudletScheduler().runningCloudlets();

		return (double)result/(double)vmList.size();
	}
	
	public double getAvgCurrentRequestedMips(){
		double result=0;
		for (Vm vm : vmList) {
			result += vm.getTotalUtilizationOfCpu(CloudSim.clock());
		}
		result = result/(double)vmList.size();
		return result;
	}
	
	public void createVmList(SimSettings.VM_TYPE vmType, int numOfVm, int brokerId) {
		//VM Parameters
		long size = 5000; //image size (MB)
		int mips = 1000;
		int ram = 1024; //vm memory (MB)
		long bw = 500;
		int pesNumber = 1; //number of CPUs
		String vmm = "Xen"; //VMM name
		
		if(vmType == SimSettings.VM_TYPE.MEDIUM_VM){
			size = 7500;
			mips = 2000;
			ram = 2048;
			bw = 1000;
		}
		else if(vmType == SimSettings.VM_TYPE.HIGH_VM){
			size = 10000;
			mips = 3000;
			ram = 4096;
			bw = 2000;
		}
		
		vmList = new LinkedList<Vm>();
		
		for(int i=0; i<numOfVm; i++){
			vmIdCounter++;
			vmList.add(new Vm(vmIdCounter, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared()));
		}
	}
}
