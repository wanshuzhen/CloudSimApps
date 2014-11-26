package edu.boun.cloudsim;
/*
 * Title:        Home cloudlet simulation
 * Description:  Example application used for simulating a home cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */



import java.util.Random;

import org.cloudbus.cloudsim.Log;

public class SimSettings {
	//public static enum TASK_CPU_REQ {HIGH_UTULIZATION, MEDIUM_UTULIZATION, LOW_UTULIZATION }
	//public static enum TASK_BW_REQ { HIGH_COMMUNICATON, MEDIUM_COMMUNICATON, LOW_COMMUNICATON }
	public static enum CLOUDLET_TYPE { LOW_SERVER, MEDIUM_SERVER, HIGH_SERVER }
	public static enum TASK_TYPE { LOW_TASK, MEDIUM_TASK, HIGH_TASK }
	public static enum VM_TYPE { LOW_VM, MEDIUM_VM, HIGH_VM }
	public static enum VM_SELECTION_TYPE { ROUND_ROBIN }
	
	public static final String OUTPUT_FOLDER = "D:\\Users\\AR430805\\Desktop\\sim_result"; 
    public static final double SIMULATION_TIME = 60 * 60 * 24;
	public static final double[] POISSON_MEAN_VALUES = { 90, 120, 150 };
    public static final int INTERVAL_TO_GET_VM_LOAD_LOG = 30;
    public static final int NUMBER_OF_ITERATION = 1;
    public static final int NUMBER_OF_MOBILE_DEVICE = 10;
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
