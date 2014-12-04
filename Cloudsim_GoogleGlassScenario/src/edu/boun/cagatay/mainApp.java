/*
 * Title:        Home cloudlet simulation
 * Description:  Example application used for simulating a home cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */

package edu.boun.cagatay;

import java.util.Calendar;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

public class mainApp {
	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		//Log.disable();
		for(int i=1; i<=SimSettings.NUMBER_OF_ITERATION; i++){
			for(int j=1; j<=SimSettings.NUMBER_OF_MOBILE_DEVICE; j++){
				for (SimSettings.CLOUD_SELECTION_TYPE selectionMethod : SimSettings.CLOUD_SELECTION_TYPE.values()) {
					//if(selectionMethod != SimSettings.CLOUD_SELECTION_TYPE.BALANCED)
					//	continue;
					SimLogger.printLine("Starting home cloudlet example...");
					SimLogger.printLine("Current iteration: " + i);
					SimLogger.printLine("Cloud Selection type: " + selectionMethod);
					SimLogger.printLine("Number of mobile devices: " + j);
					SimSettings.CLOUD_SELECTION = selectionMethod;
					SimLogger.getInstance().simStarted("SIMRESULT_ITE"+i+"_"+selectionMethod+"_"+j+"BROKER");
					
					try {
						// First step: Initialize the CloudSim package. It should be called
						// before creating any entities.
						int num_user = 2;   // number of grid users
						Calendar calendar = Calendar.getInstance();
						boolean trace_flag = false;  // mean trace events
				
						// Initialize the CloudSim library
						CloudSim.init(num_user, calendar, trace_flag);
				
						@SuppressWarnings("unused")
						SimManager manager = new SimManager(j);
				
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
			}//End of NUMBER_OF_MOBILE_DEVICE loop
		}//End of NUMBER_OF_ITERATION loop
	}
}
