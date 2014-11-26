package edu.boun.cloudsim;
/*
 * Title:        Home cloudlet simulation
 * Description:  Example application used for simulating a home cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */



import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class CloudServers {
	private Datacenter cloudletServer;
	private int datacenterIdCounter;

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
		createCloudletServer("CloudletServer");
	}
	
	public void terminateServers(){
		cloudletServer.shutdownEntity();
	}
	
	public double getAvgUtilization(){
		double result = 0;
		List<? extends Host> list = cloudletServer.getHostList();
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

	private void createCloudletServer(String name){
		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		//    Machines
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
		//    create a list to store these PEs before creating
		//    a Machine.
		List<Pe> peList1 = new ArrayList<Pe>();

		int mips = 3000;
		
		// 3. Create PEs and add these into the list.
		peList1.add(new Pe(0, new PeProvisionerSimple(mips))); //quad-core machine
		peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(3, new PeProvisionerSimple(mips)));

		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		datacenterIdCounter++;
		long storage = 500000; //host storage
		int ram = 8192; //host memory (MB)
		int bw = 100000; //default bandwidth for low bandwidth
		
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

		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		try {
			cloudletServer = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
