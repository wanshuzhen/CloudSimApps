/*
 * Title:        Home cloudlet simulation
 * Description:  Example application used for simulating a home cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */

package edu.boun.cagatay;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class LogItem
{
	private boolean isCompleted;
	private int dataCenterId;
	private double taskStartTime;
	private double taskEndTime;
	private double bwCost;
	private double cpuCost;
	LogItem (int _dataCenterId, double _taskStartTime)
	{
		dataCenterId = _dataCenterId;
		taskStartTime = _taskStartTime;
		taskEndTime = 0;
	}
	public void setTaskEndTime(double _taskEndTime) {
		taskEndTime = _taskEndTime;
		isCompleted = true;
	}
	public void setCost(double _bwCost, double _cpuCos) {
		bwCost = _bwCost;
		cpuCost = _cpuCos;
	}
	public double getCost() {
		return bwCost + cpuCost;
	}
	public double getServiceTime() {
		return taskEndTime - taskStartTime;
	}
	public boolean isCompleted(){
		return isCompleted;
	}
	public String toString (int taskId)
	{
		String result = taskId + ";" + dataCenterId + ";" + taskStartTime;
		if(isCompleted)
			result = taskId + ";" + dataCenterId + ";" + taskStartTime + ";" + taskEndTime + ";" + bwCost + ";" + cpuCost;
		return result;
	}
}

public class SimLogger {
	private String filePrefix;
	private Map<Integer, LogItem> taskMap;
	private BufferedWriter serverLoadFileBufferedWriter;

	private static SimLogger singleton = new SimLogger( );

	/* A private Constructor prevents any other 
	 * class from instantiating.
	 */
	private SimLogger(){
		
	}

	/* Static 'instance' method */
	public static SimLogger getInstance( ) {
		return singleton;
	}
	
	public void simStarted(String fileName) {
		filePrefix = fileName;
		taskMap = new HashMap<Integer, LogItem>();
		
		try {
			File serverLoadFile = new File(filePrefix+"_SERVER_LOAD.log");
			FileWriter serverLoadFileWriter = new FileWriter(serverLoadFile, true);
			serverLoadFileBufferedWriter = new BufferedWriter(serverLoadFileWriter);
			serverLoadFileBufferedWriter.write("time;load1;load2;load3");
			serverLoadFileBufferedWriter.newLine();
		} catch (IOException e) {
			printLine("file io error for server load log file!");
			e.printStackTrace();
		}
	}

	public void simStopped() {
		int uncompletedTask = 0;
		int completedTask = 0;
		double totalCost = 0;
		double totalServiceTime = 0;
		try {
			//first of all close server load log file!
			serverLoadFileBufferedWriter.close();
			
			File file1 = new File(filePrefix+"_SUCCESS.log");
			FileWriter fileWriter1 = new FileWriter(file1, true);
			BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);
			File file2 = new File(filePrefix+"_FAIL.log");
			FileWriter fileWriter2 = new FileWriter(file2, true);
			BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2);
			bufferedWriter1.write("taskId;dataCenterId;taskStartTime;taskEndTime;bwCost;cpuCost");
			bufferedWriter2.write("taskId;dataCenterId;taskStartTime");
			bufferedWriter1.newLine();
			bufferedWriter2.newLine();
			for (Map.Entry<Integer, LogItem> entry : taskMap.entrySet()) {
				Integer key = entry.getKey();
				LogItem value = entry.getValue();
			    
				if(value.isCompleted()){
					completedTask++;
					totalCost += value.getCost();
					totalServiceTime += value.getServiceTime();
					bufferedWriter1.write(value.toString(key));
					bufferedWriter1.newLine();
				}
				else{
					uncompletedTask++;
					bufferedWriter2.write(value.toString(key));
					bufferedWriter2.newLine();
				}
			}
			bufferedWriter1.close();
			bufferedWriter2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		taskMap.clear();
		printLine("Logs are saved to file.");
		printLine("# of task: " + (completedTask+uncompletedTask));
		printLine("# of uncompleted tasks: " + uncompletedTask);
		printLine("average service time: " + totalServiceTime/completedTask + " seconds");
		printLine("average cost: " + totalCost/completedTask + "$");
	}

	public void updateLog(int taskId, double taskEndTime, double bwCost, double cpuCost) {
		taskMap.get(taskId).setTaskEndTime(taskEndTime);
		taskMap.get(taskId).setCost(bwCost, cpuCost);
	}
	
	public void addLog(int taskId, int dataCenterId, double taskStartTime) {
		taskMap.put(taskId, new LogItem(dataCenterId, taskStartTime));
	}

	public void addServerLoadLog(double time, double load1, double load2, double load3) {
		//printLine(time+";"+load1+";"+load2+";"+load3);
		try {
			serverLoadFileBufferedWriter.write(time+";"+load1+";"+load2+";"+load3);
			serverLoadFileBufferedWriter.newLine();
		} catch (IOException e) {
			printLine("file io error for server load log file!");
			e.printStackTrace();
		}
	}
	
	public static void printLine(String msg){
		System.out.println(msg);
	}
}
