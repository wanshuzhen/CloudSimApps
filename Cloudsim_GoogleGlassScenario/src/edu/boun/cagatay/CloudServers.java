/*
 * Title:        Home cloudlet simulation
 * Description:  Example application used for simulating a home cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */

package edu.boun.cagatay;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class CloudServers {
	private Datacenter lowendCloudServer;
	private Datacenter highendCloudServer;
	private Datacenter mediumCloudServer;
	private Vm lowendVm; //used for testing cloud server availability
	private Vm highendVm; //used for testing cloud server availability
	private Vm mediumVm; //used for testing cloud server availability
	private int datacenterIdCounter;
	private int vmIdCounter;

	private static CloudServers instance = null;

	private CloudServers() {
		
	}
	public static CloudServers getInstance() {
		if(instance == null) {
			instance = new CloudServers();
		}
		return instance;
	}
	public void startServers(){
		datacenterIdCounter = 0;
		vmIdCounter = 0;
		lowendCloudServer = createDatacenter("LowendCloudServer", SimSettings.SERVER_TYPE.LOW_SERVER);
		mediumCloudServer = createDatacenter("MediumCloudServer", SimSettings.SERVER_TYPE.MEDIUM_SERVER);
		highendCloudServer = createDatacenter("HighendCloudServer", SimSettings.SERVER_TYPE.HIGH_SERVER);
		lowendVm = getVM(SimSettings.SERVER_TYPE.LOW_SERVER, 1/*dummy broker id*/);
		mediumVm = getVM(SimSettings.SERVER_TYPE.MEDIUM_SERVER, 1/*dummy broker id*/);
		highendVm = getVM(SimSettings.SERVER_TYPE.HIGH_SERVER, 1/*dummy broker id*/);
	}
	public void terminateServers(){
		lowendCloudServer.shutdownEntity();
		mediumCloudServer.shutdownEntity();
		highendCloudServer.shutdownEntity();
	}
	public Datacenter getCloudServer(SimSettings.SERVER_TYPE serverType){
		Datacenter result;
		if(serverType == SimSettings.SERVER_TYPE.LOW_SERVER)
			result = lowendCloudServer;
		else if(serverType == SimSettings.SERVER_TYPE.MEDIUM_SERVER)
			result = mediumCloudServer;
		else
			result = highendCloudServer;
		
		return result;
	}
	
	public SimSettings.SERVER_TYPE getCloudServer(){
		SimSettings.SERVER_TYPE result;
		if(SimSettings.CLOUD_SELECTION == SimSettings.CLOUD_SELECTION_TYPE.RANDOM)
			result = getRandomCloudServer();
		else if (SimSettings.CLOUD_SELECTION == SimSettings.CLOUD_SELECTION_TYPE.BALANCED){
			double minUtilization;
			double lowServerUtilization = getAvgUtilization(lowendCloudServer);
			double mediumServerUtilization = getAvgUtilization(mediumCloudServer);
			double highServerUtilization = getAvgUtilization(highendCloudServer);
			if(lowServerUtilization < mediumServerUtilization){
				result = SimSettings.SERVER_TYPE.LOW_SERVER;
				minUtilization = lowServerUtilization;
			}
			else{
				result = SimSettings.SERVER_TYPE.MEDIUM_SERVER;
				minUtilization = mediumServerUtilization;
			}
			if(minUtilization > highServerUtilization)
				result = SimSettings.SERVER_TYPE.HIGH_SERVER;
		}
		else if (SimSettings.CLOUD_SELECTION == SimSettings.CLOUD_SELECTION_TYPE.CHEAPEST){
			if(isSuitable(lowendCloudServer,lowendVm))
				result = SimSettings.SERVER_TYPE.LOW_SERVER;
			else if(isSuitable(mediumCloudServer,mediumVm))
				result = SimSettings.SERVER_TYPE.MEDIUM_SERVER;
			else if(isSuitable(highendCloudServer,highendVm))
				result = SimSettings.SERVER_TYPE.HIGH_SERVER;
			else
				result = getRandomCloudServer();
		}
		else if (SimSettings.CLOUD_SELECTION == SimSettings.CLOUD_SELECTION_TYPE.FASTEST){
			if(isSuitable(highendCloudServer,highendVm))
				result = SimSettings.SERVER_TYPE.HIGH_SERVER;
			else if(isSuitable(mediumCloudServer,mediumVm))
				result = SimSettings.SERVER_TYPE.MEDIUM_SERVER;
			else if(isSuitable(lowendCloudServer,lowendVm))
				result = SimSettings.SERVER_TYPE.LOW_SERVER;
			else
				result = getRandomCloudServer();
		}
		else{
			result = getRandomCloudServer();
			Log.printLine("Impossible occured: unknown cloud server selection mechanism!");
		}
		return result;
	}
	
	public SimSettings.SERVER_TYPE getRandomCloudServer(){
		SimSettings.SERVER_TYPE randomserver = SimSettings.SERVER_TYPE.MEDIUM_SERVER;
		switch (SimSettings.getRandomNumber(0, 2)) {
		case 0:
			randomserver = SimSettings.SERVER_TYPE.LOW_SERVER;
			break;
		case 1:
			randomserver = SimSettings.SERVER_TYPE.MEDIUM_SERVER;
			break;
		case 2:
			randomserver = SimSettings.SERVER_TYPE.HIGH_SERVER;
			break;
		default:
			randomserver = SimSettings.SERVER_TYPE.MEDIUM_SERVER;
			SimLogger.printLine("Impossible occured: unknown cloud server!");
			break;
		}

		return randomserver;
	}
	
	public double getAvgUtilization(Datacenter dc){
		double result = 0;
		List<? extends Host> list = dc.getHostList();
		// for each host...
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			//SimLogger.printLine("HOST " + i + " - MIPS usage -> " + (host.getTotalMips()-host.getAvailableMips()) + "/" + host.getTotalMips());
			//SimLogger.printLine("HOST " + i + " - RAM usage -> " + (host.getRam()-host.getRamProvisioner().getAvailableRam()) + "/" + host.getRam());
			result += ((double)host.getTotalMips()-(double)host.getAvailableMips()) / (double)host.getTotalMips();
			result += ((double)host.getRam()-(double)host.getRamProvisioner().getAvailableRam()) / (double)host.getRam();
			//bw is always available, no need to check
			//result += (host.getBw()-host.getBwProvisioner().getAvailableBw()) / host.getBw();
		}
		return result/(list.size()*2);
	}
	
	private boolean isSuitable(Datacenter dc, Vm vm){
		boolean result = false;
		List<? extends Host> list = dc.getHostList();
		// for each host...
		for (int i = 0; i < list.size(); i++) {
			result = list.get(i).isSuitableForVm(vm);
			if(result)
				break;
		}
		return result;
	}

	private Datacenter createDatacenter(String name, SimSettings.SERVER_TYPE serverType){
		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		//    Machines
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
		//    create a list to store these PEs before creating
		//    a Machine.
		List<Pe> peList1 = new ArrayList<Pe>();

		int mips = 1000;
		if(serverType == SimSettings.SERVER_TYPE.MEDIUM_SERVER)
			mips = 2000;
		else if(serverType == SimSettings.SERVER_TYPE.HIGH_SERVER)
			mips = 3000;
		
		// 3. Create PEs and add these into the list.
		peList1.add(new Pe(0, new PeProvisionerSimple(mips))); //default dual-core machine
		peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
		if(serverType == SimSettings.SERVER_TYPE.MEDIUM_SERVER){ //quad-core machine
			peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
			peList1.add(new Pe(3, new PeProvisionerSimple(mips)));
		}
		else if(serverType == SimSettings.SERVER_TYPE.HIGH_SERVER){ //8-core machine
			peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
			peList1.add(new Pe(3, new PeProvisionerSimple(mips)));
			peList1.add(new Pe(4, new PeProvisionerSimple(mips)));
			peList1.add(new Pe(5, new PeProvisionerSimple(mips)));
			peList1.add(new Pe(6, new PeProvisionerSimple(mips)));
			peList1.add(new Pe(7, new PeProvisionerSimple(mips)));
		}
		//Another list
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(mips))); //default dual-core machine
		peList2.add(new Pe(1, new PeProvisionerSimple(mips)));
		if(serverType == SimSettings.SERVER_TYPE.MEDIUM_SERVER){ //quad-core machine
			peList2.add(new Pe(2, new PeProvisionerSimple(mips)));
			peList2.add(new Pe(3, new PeProvisionerSimple(mips)));
		}
		else if(serverType == SimSettings.SERVER_TYPE.HIGH_SERVER){ //8-core machine
			peList2.add(new Pe(2, new PeProvisionerSimple(mips)));
			peList2.add(new Pe(3, new PeProvisionerSimple(mips)));
			peList2.add(new Pe(4, new PeProvisionerSimple(mips)));
			peList2.add(new Pe(5, new PeProvisionerSimple(mips)));
			peList2.add(new Pe(6, new PeProvisionerSimple(mips)));
			peList2.add(new Pe(7, new PeProvisionerSimple(mips)));
		}

		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		datacenterIdCounter++;
		long storage = 500000; //host storage
		int ram = 8192; //host memory (MB)
		int bw = 10000; //default bandwidth for low bandwidth
		
		if(serverType == SimSettings.SERVER_TYPE.MEDIUM_SERVER){
			storage = 750000;
			ram = 16384;
			bw = 50000;
		}
		else if(serverType == SimSettings.SERVER_TYPE.HIGH_SERVER){
			storage = 1000000;
			ram = 32768;
			bw = 100000;
		}

		hostList.add(
				new Host(
						datacenterIdCounter,
						new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw),
						storage,
						peList1,
						new VmSchedulerTimeShared(peList1)
						)
				); // This is our first machine

		datacenterIdCounter++;

		hostList.add(
				new Host(
						datacenterIdCounter,
						new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw),
						storage,
						peList2,
						new VmSchedulerTimeShared(peList2)
						)
				); // Second machine

		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 0.001;              // the cost of using processing in this resource
		double costPerMem = 0.001;		// the cost of using memory in this resource
		double costPerStorage = 0.001;	// the cost of using storage in this resource
		double costPerBw = 0.001;			// the cost of using bw in this resource

		if(serverType == SimSettings.SERVER_TYPE.MEDIUM_SERVER){ 
			cost = 0.005;
			costPerMem = 0.0025;
			costPerStorage = 0.0015;
			costPerBw = 0.01;
		}
		else if(serverType == SimSettings.SERVER_TYPE.HIGH_SERVER){
			cost = 0.03;
			costPerMem = 0.005;
			costPerStorage = 0.002;
			costPerBw = 0.025;
		}

		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
	
	public Vm getVM(SimSettings.SERVER_TYPE typeOfServerToRun, int brokerId) {
		//VM Parameters
		long size = 5000; //image size (MB)
		int mips = 125;
		int ram = 512; //vm memory (MB)
		long bw = 200;
		int pesNumber = 1; //number of CPUs
		String vmm = "Xen"; //VMM name
		
		if(typeOfServerToRun == SimSettings.SERVER_TYPE.MEDIUM_SERVER){
			size = 7500;
			mips = 500;
			ram = 1024;
			bw = 2000;
		}
		else if(typeOfServerToRun == SimSettings.SERVER_TYPE.HIGH_SERVER){
			size = 10000;
			mips = 1500;
			ram = 2048;
			bw = 4000;
		}
		
		vmIdCounter++;
		return new Vm(vmIdCounter, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
	}
}
