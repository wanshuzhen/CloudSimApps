/*
 * Title:        Home cloudlet simulation
 * Description:  Example application used for simulating a home cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */

package edu.boun.cagatay;

import java.util.Random;

import org.cloudbus.cloudsim.Log;

public class SimSettings {
	public static enum TASK_CPU_REQ {HIGH_UTULIZATION, MEDIUM_UTULIZATION, LOW_UTULIZATION }
	public static enum TASK_BW_REQ { HIGH_COMMUNICATON, MEDIUM_COMMUNICATON, LOW_COMMUNICATON }
	public static enum SERVER_TYPE { LOW_SERVER, MEDIUM_SERVER, HIGH_SERVER }
	//public static enum CPU_TYPE { HIGH_CPU, MEDIUM_CPU, LOW_CPU }
	//public static enum BANDWITH_TYPE { HIGH_BW, MEDIUM_BW, LOW_BW }
	public static enum CLOUD_SELECTION_TYPE { RANDOM, BALANCED, CHEAPEST, FASTEST }
	
    public static final double SIMULATION_TIME = 60 * 60 * 12;
    public static final int INTERVAL_TO_GET_SERVER_LOAD_LOG = 20;
    public static final int NUMBER_OF_ITERATION = 4;
    public static final int NUMBER_OF_MOBILE_DEVICE = 10;
    public static final int NUMBER_OF_CLOUTLET = 2;
    public static final Random RNG = new Random(System.currentTimeMillis());
    
    public static CLOUD_SELECTION_TYPE CLOUD_SELECTION;
    
    //public static final PoissonDistribution pd = new PoissonDistribution(50);
    
    public static int getRandomNumber(int start, int end) {
    	//return pd.sample();
    	
		long range = (long)end - (long)start + 1;
		long fraction = (long)(range * SimSettings.RNG.nextDouble());
		return (int)(fraction + start); 
    }
    
    public static TASK_CPU_REQ getRandomTaskCpuReq() {
		TASK_CPU_REQ randomCpuReq;
		switch (getRandomNumber(0, 2)) {
		case 0:
			randomCpuReq = TASK_CPU_REQ.LOW_UTULIZATION;
			break;
		case 1:
			randomCpuReq = TASK_CPU_REQ.MEDIUM_UTULIZATION;
			break;
		case 2:
			randomCpuReq = TASK_CPU_REQ.HIGH_UTULIZATION;
			break;
		default:
			randomCpuReq = TASK_CPU_REQ.MEDIUM_UTULIZATION;
			Log.printLine("Impossible occured: unknown cpu requirement!");
			break;
		}
		
		return randomCpuReq;
    }
    
    public static TASK_BW_REQ getRandomTaskBwReq() {
		TASK_BW_REQ randomBwReq;
		switch (getRandomNumber(0, 2)) {
		case 0:
			randomBwReq = TASK_BW_REQ.LOW_COMMUNICATON;
			break;
		case 1:
			randomBwReq = TASK_BW_REQ.MEDIUM_COMMUNICATON;
			break;
		case 2:
			randomBwReq = TASK_BW_REQ.HIGH_COMMUNICATON;
			break;
		default:
			randomBwReq = TASK_BW_REQ.MEDIUM_COMMUNICATON;
			Log.printLine("Impossible occured: unknown bw requirement!");
			break;
		}
		return randomBwReq;
	}
}
