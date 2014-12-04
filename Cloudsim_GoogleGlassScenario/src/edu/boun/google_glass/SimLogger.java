package edu.boun.google_glass;
/*
 * Title:        Mobile cloudlet simulation
 * Description:  Example application used for simulating a google glass cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


class VmLoadLogItem
{
	private double time;
	private double vmLoad;
	
	VmLoadLogItem(double _time, double _vmLoad)
	{
		time = _time;
		vmLoad = _vmLoad;
	}
	public double getLoad()
	{
		return vmLoad;
	}
	public String toString()
	{
		return time + ";" + vmLoad;
	}
}
class LogItem
{
	private SimLogger.TASK_STATUS status;
	private int dataCenterId;
	private double taskStartTime;
	private double taskEndTime;
	private double networkDelay;
	private double bwCost;
	private double cpuCost;
	LogItem (int _dataCenterId, double _taskStartTime, double taskUploadTime)
	{
		dataCenterId = _dataCenterId;
		taskStartTime = _taskStartTime;
		networkDelay = taskUploadTime;
		status = SimLogger.TASK_STATUS.UPLOADING;
		taskEndTime = 0;
	}
	public void taskUploaded() {
		status = SimLogger.TASK_STATUS.PROCESSING;
	}
	public void taskProcessed(double taskDownloadTime) {
		networkDelay += taskDownloadTime;
		status = SimLogger.TASK_STATUS.DOWNLOADING;
	}
	public void taskDownloaded(double _taskEndTime) {
		taskEndTime = _taskEndTime;
		status = SimLogger.TASK_STATUS.COMLETED;
	}
	public void taskCancelled(double _taskCancelledTime) {
		taskEndTime = _taskCancelledTime;
		status = SimLogger.TASK_STATUS.CANCELLED;
	}
	public void setCost(double _bwCost, double _cpuCos) {
		bwCost = _bwCost;
		cpuCost = _cpuCos;
	}
	public double getCost() {
		return bwCost + cpuCost;
	}
	public double getNetworkDelay() {
		return networkDelay;
	}
	public double getServiceTime() {
		return taskEndTime - taskStartTime;
	}
	public SimLogger.TASK_STATUS getStatus(){
		return status;
	}
	public String toString (int taskId)
	{
		String result = taskId + ";" + dataCenterId + ";" + taskStartTime + ";" + taskEndTime + ";" + networkDelay;
		return result;
	}
}

public class SimLogger {
	public static enum TASK_STATUS { UPLOADING, PROCESSING, DOWNLOADING, COMLETED, CANCELLED }
	
	private static boolean fileLogEnabled;
	private static boolean printLogEnabled;
	private String filePrefix;
	private Map<Integer, LogItem> taskMap;
	private LinkedList<VmLoadLogItem> vmLoadList;
	private LinkedList<Double> vmPreLoadList;

	private static SimLogger singleton = new SimLogger( );

	/* A private Constructor prevents any other 
	 * class from instantiating.
	 */
	private SimLogger(){
		fileLogEnabled = true;
		printLogEnabled = true;
	}

	/* Static 'instance' method */
	public static SimLogger getInstance( ) {
		return singleton;
	}
	
	public static void disableFileLog() {
		fileLogEnabled = false;
	}
	
	public static void disablePrintLog() {
		printLogEnabled = false;
	}

	public static void printLine(String msg){
		if(printLogEnabled)
			System.out.println(msg);
	}
	
	public void simStarted(String fileName) {
		filePrefix = SimSettings.OUTPUT_FOLDER + "\\" + fileName;
		taskMap = new HashMap<Integer, LogItem>();
		vmLoadList = new LinkedList<VmLoadLogItem>();
		vmPreLoadList = new LinkedList<Double>();
	}

	public void simStopped() {
		int uncompletedTask = 0;
		int completedTask = 0;
		int cancelledTask = 0;
		double totalCost = 0;
		double totalServiceTime = 0;
		double totalNetworkDelay = 0;
		double totalVmLoad = 0;
		try {
			if(fileLogEnabled){
				File file1 = new File(filePrefix+"_SUCCESS.log");
				FileWriter fileWriter1 = new FileWriter(file1, true);
				BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);
				
				File file2 = new File(filePrefix+"_FAIL.log");
				FileWriter fileWriter2 = new FileWriter(file2, true);
				BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2);
				
				File file3 = new File(filePrefix+"_VM_LOAD.log");
				FileWriter fileWriter3 = new FileWriter(file3, true);
				BufferedWriter bufferedWriter3 = new BufferedWriter(fileWriter3);
				
				bufferedWriter1.write("taskId;dataCenterId;taskStartTime;taskEndTime;bwCost;cpuCost");
				bufferedWriter2.write("taskId;dataCenterId;taskStartTime");
				bufferedWriter3.write("time;load");
				bufferedWriter1.newLine();
				bufferedWriter2.newLine();
				bufferedWriter3.newLine();
				
				//write task information to file
				for (Map.Entry<Integer, LogItem> entry : taskMap.entrySet()) {
					Integer key = entry.getKey();
					LogItem value = entry.getValue();
				    
					if(value.getStatus() == SimLogger.TASK_STATUS.COMLETED){
						completedTask++;
						totalCost += value.getCost();
						totalServiceTime += value.getServiceTime();
						totalNetworkDelay += value.getNetworkDelay();
						bufferedWriter1.write(value.toString(key));
						bufferedWriter1.newLine();
					}
					else if(value.getStatus() == SimLogger.TASK_STATUS.CANCELLED){
						cancelledTask++;
						bufferedWriter2.write(value.toString(key));
						bufferedWriter2.newLine();
					}
					else {
						uncompletedTask++;
					}
				}
				
				//write server load to file
				for(VmLoadLogItem entry : vmLoadList){
					totalVmLoad += entry.getLoad();
					bufferedWriter3.write(entry.toString());
					bufferedWriter3.newLine();
				}
				
				bufferedWriter1.close();
				bufferedWriter2.close();
				bufferedWriter3.close();
			}
			else{
				//write task information to file
				for (Map.Entry<Integer, LogItem> entry : taskMap.entrySet()) {
					//Integer key = entry.getKey();
					LogItem value = entry.getValue();
				    
					if(value.getStatus() == SimLogger.TASK_STATUS.COMLETED){
						completedTask++;
						totalCost += value.getCost();
						totalServiceTime += value.getServiceTime();
						totalNetworkDelay += value.getNetworkDelay();
					}
					else if(value.getStatus() == SimLogger.TASK_STATUS.CANCELLED){
						cancelledTask++;
					}
					else {
						uncompletedTask++;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		taskMap.clear();
		printLine("Logs are saved to file.");
		printLine("# of task: " + (completedTask+uncompletedTask));
		printLine("# of cancelled tasks: " + cancelledTask);
		printLine("# of uncompleted tasks: " + uncompletedTask);
		printLine("average service time: " + totalServiceTime/completedTask + " seconds");
		printLine("average netwrok delay: " + totalNetworkDelay/completedTask + " seconds");
		printLine("average server load: " + totalVmLoad/vmLoadList.size() + " seconds");
		printLine("average cost: " + totalCost/completedTask + "$");
	}

	public void addTaskLog(int taskId, int dataCenterId, double taskStartTime, double taskUploadTime) {
		//printLine(taskId+"->"+taskStartTime);
		taskMap.put(taskId, new LogItem(dataCenterId, taskStartTime, taskUploadTime));
	}
	
	public void updateTaskLogAsUploaded(int taskId) {
		taskMap.get(taskId).taskUploaded();
	}
	
	public void updateTaskLogAsProcessed(int taskId, double taskDownloadTime) {
		taskMap.get(taskId).taskProcessed(taskDownloadTime);
	}

	public void updateTaskLogAsDownloaded(int taskId, double taskEndTime) {
		taskMap.get(taskId).taskDownloaded(taskEndTime);
	}

	public void updateTaskLogAsCancelled(int taskId, double taskCancelledTime) {
		taskMap.get(taskId).taskCancelled(taskCancelledTime);
	}
	
	public void addServerLoadLog(double load) {
		//printLine(time+";"+load);
		vmPreLoadList.add(load);
	}
	public void fixServerLoadLog(double time) {
		double maxLoad = 0;
		for(Double load : vmPreLoadList){
			if(load>maxLoad)
				maxLoad = load;
		}
		vmPreLoadList.clear();
		vmLoadList.add(new VmLoadLogItem(time, maxLoad));
	}
}
