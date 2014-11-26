package edu.boun.cloudsim;
/*
 * Title:        Home cloudlet simulation
 * Description:  Example application used for simulating a home cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */



import java.util.Calendar;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

public class mainApp {
	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.disable();
		for(int i=1; i<=SimSettings.NUMBER_OF_ITERATION; i++){
			for(int j=1; j<=SimSettings.NUMBER_OF_MOBILE_DEVICE; j++){
				for(int k=1; k<=SimSettings.NUMBER_OF_VM; k++){
					for (SimSettings.VM_TYPE vmType : SimSettings.VM_TYPE.values()) {
						for(int l=0; l<SimSettings.POISSON_MEAN_VALUES.length; l++){
							for (SimSettings.VM_SELECTION_TYPE selectionMethod : SimSettings.VM_SELECTION_TYPE.values()) {
								//if(selectionMethod != SimSettings.CLOUD_SELECTION_TYPE.BALANCED)
								//	continue;
								SimLogger.printLine("Starting home cloudlet example...");
								SimLogger.printLine("Cloudlet Selection type: " + selectionMethod);
								SimLogger.printLine("Current iteration: " + i);
								SimLogger.printLine("Vm  type: " + vmType);
								SimLogger.printLine("Poisson mean: " + SimSettings.POISSON_MEAN_VALUES[l]);
								SimLogger.printLine("Number of virtual machine: " + k);
								SimLogger.printLine("Number of mobile devices: " + j);
								//SimSettings.VM_SELECTION = selectionMethod;
								SimLogger.getInstance().simStarted("SIMRESULT_ITE"+i+"_"+vmType+"_MEAN"+(int)SimSettings.POISSON_MEAN_VALUES[l]+"_"+j+"DEVICE");
								
								try {
									// First step: Initialize the CloudSim package. It should be called
									// before creating any entities.
									int num_user = 2;   // number of grid users
									Calendar calendar = Calendar.getInstance();
									boolean trace_flag = false;  // mean trace events
							
									// Initialize the CloudSim library
									CloudSim.init(num_user, calendar, trace_flag);
							
									@SuppressWarnings("unused")
									SimManager manager = new SimManager(k,j,vmType,SimSettings.POISSON_MEAN_VALUES[l]);
							
									SimLogger.getInstance().simStopped();
									SimLogger.printLine("Home cloudlet example finished!");
									SimLogger.printLine("-----------------------------------------");
								}
								catch (Exception e)
								{
									e.printStackTrace();
									SimLogger.printLine("The simulation has been terminated due to an unexpected error");
								}
							}//End of CLOUD_SELECTION_TYPE loop
						}//End of VM_TYPE loop
					}//End of POISSON_MEAN_VALUES loop
				}//End of NUMBER_OF_VM loop
			}//End of NUMBER_OF_MOBILE_DEVICE loop
		}//End of NUMBER_OF_ITERATION loop
	}
}
