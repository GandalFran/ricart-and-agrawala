package com.ssdd.simulation.centralizedLog;

import java.util.logging.Logger;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

import com.ssdd.simulation.SimulationLog;
import com.ssdd.util.constants.ISimulationConstants;
import com.ssdd.util.logging.SSDDLogFactory;

public class SimulationLogCentralized extends SimulationLog{

    private final static Logger LOGGER = SSDDLogFactory.logger(SimulationLogCentralized.class);

	private WebTarget service;
	
	public SimulationLogCentralized(String processId, String logFile) {
		super(processId, logFile);
		String service = SimulationLogCentralized.buildServiceUri(ISimulationConstants.LOG_SERVER);
		this.service = ClientBuilder.newClient().target(UriBuilder.fromUri(service).build());
	}
	
	public static String buildServiceUri(String host){
		return String.format("http://%s:8080/ssdd/log", host);
	}
	
	public void logIn() {
		this.service.path("in").queryParam("p", super.getProcessId()).request().get();
	}
	
	public void logOut() {
		this.service.path("out").queryParam("p", super.getProcessId()).request().get();
	}
}
