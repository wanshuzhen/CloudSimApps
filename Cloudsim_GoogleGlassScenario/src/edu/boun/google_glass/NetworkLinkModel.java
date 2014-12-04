package edu.boun.google_glass;
/*
 * Title:        Mobile cloudlet simulation
 * Description:  Example application used for simulating a google glass cloudlet scenario
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014, Bogazici University, Istanbul, Turkey
 */

public class NetworkLinkModel {
	private SimSettings.CLIENT_TYPE clientType;
	private double PoissonMean; //seconds
	private double avgTaskSize; //bytes
	private int numOfClients;
	private double delay;
	private static NetworkLinkModel instance = null;

	private NetworkLinkModel() {
	}
	
	public static NetworkLinkModel getInstance() {
		if(instance == null) {
			instance = new NetworkLinkModel();
		}
		return instance;
	}
	
	public void adjustSimSettings(SimSettings.CLIENT_TYPE _clientType, double _PoissonMean, double _avgTaskSize, int _numOfClients){
		clientType=_clientType;
		PoissonMean=_PoissonMean;
		avgTaskSize=_avgTaskSize;
		numOfClients=_numOfClients;
		
		//CAlculate delay
		double Mbps = 0, Bps=0, mu=0, lamda=0;
		
		if(clientType == SimSettings.CLIENT_TYPE.CLOUD_WIFI_CLIENT){
			Mbps = 8; //Mbps
		}
		else if(clientType == SimSettings.CLIENT_TYPE.CLOUDLET_WIFI_CLIENT){
			Mbps = 40; //Mbps
		}
		
		Bps = Mbps * (double)1000000 / (double)8; //convert from Mbps to byte per seconds
		mu = Bps / avgTaskSize; //task per seconds
	    lamda = ((double)1/(double)PoissonMean); //task per seconds
	    
		delay = (double)1 / (mu-lamda*numOfClients);
	}
	
	public double getDelay() {
		return delay;
	}
}
