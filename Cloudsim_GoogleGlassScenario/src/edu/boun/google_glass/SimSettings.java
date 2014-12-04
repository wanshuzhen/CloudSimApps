package edu.boun.google_glass;
/*
 * Title:        Mobile cloudlet simulation
 * Description:  Example application used for simulating a google glass cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */

import java.util.Random;

import org.cloudbus.cloudsim.Log;

public class SimSettings {
	//public static enum TASK_CPU_REQ {HIGH_UTULIZATION, MEDIUM_UTULIZATION, LOW_UTULIZATION }
	//public static enum TASK_BW_REQ { HIGH_COMMUNICATON, MEDIUM_COMMUNICATON, LOW_COMMUNICATON }
	//public static enum CLOUDLET_TYPE { LOW_SERVER, MEDIUM_SERVER, HIGH_SERVER }
	public static enum TASK_TYPE { LOW_TASK, MEDIUM_TASK, HIGH_TASK }
	public static enum VM_TYPE { CLOUD_VM, CLOUDLET_VM }
	public static enum CLIENT_TYPE { /*CLOUD_3G_CLIENT,*/ CLOUD_WIFI_CLIENT, CLOUDLET_WIFI_CLIENT }
	public static enum VM_SELECTION_TYPE { ROUND_ROBIN }
	
	public static final String OUTPUT_FOLDER = "C:/Users/çagatay/Desktop/sim_results"; 
    public static final double SIMULATION_TIME = 60 * 60 * 12;
	public static final double[] POISSON_MEAN_VALUES = { 20 };
    public static final double INTERVAL_TO_GET_VM_LOAD_LOG = 20;
    public static final int MAX_NUMBER_OF_TASKS_ON_VM = 100;
    public static final int NUMBER_OF_ITERATION = 5;
    //public static final int[] MOBILE_DEVICES = {5,10,15,20,25,30,35,40,45,50};
    public static final int[] MOBILE_DEVICES = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50};
    public static final int NUMBER_OF_VM = 1;
    public static final Random RNG = new Random(System.currentTimeMillis());
    
    public static int getRandomNumber(int start, int end) {
    	//return pd.sample();
		long range = (long)end - (long)start + 1;
		long fraction = (long)(range * SimSettings.RNG.nextDouble());
		return (int)(fraction + start); 
    }
    
    public static TASK_TYPE getRandomTask() {
		TASK_TYPE randomTask;
		switch (getRandomNumber(0, 2)) {
		case 0:
			randomTask = TASK_TYPE.LOW_TASK;
			break;
		case 1:
			randomTask = TASK_TYPE.MEDIUM_TASK;
			break;
		case 2:
			randomTask = TASK_TYPE.HIGH_TASK;
			break;
		default:
			randomTask = TASK_TYPE.MEDIUM_TASK;
			Log.printLine("Impossible occured: unknown cpu requirement!");
			break;
		}
		
		return randomTask;
    }
}
