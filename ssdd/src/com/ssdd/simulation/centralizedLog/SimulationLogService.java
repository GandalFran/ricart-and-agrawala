package com.ssdd.simulation.centralizedLog;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.ssdd.util.constants.ISimulationConstants;
import com.ssdd.util.logging.SSDDLogFactory;

@Singleton
@Path("/simulation")
public class SimulationLogService {

    private final static Logger LOGGER = SSDDLogFactory.logger(SimulationLogService.class);

	private static final String IN_FORMAT = "P%s E %d\n";
	private static final String OUT_FORMAT = "P%s S %d\n";
	
	private FileWriter file;
	
	public SimulationLogService() {
		try {
			this.file = new FileWriter(ISimulationConstants.SIMULATION_LOG_FILE, true);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("constructor: error %s", e.getMessage()), e);
		}
	}
	
	@GET
	@Path("/status")
	@Produces(MediaType.TEXT_PLAIN)
	public String status() {
		return "{ \"service\": \"simulation log\", \"status\": \"ok\"}";
	}
	
	@GET
	@Path("/in")
	public void in(@QueryParam(value="p") String p){
		try {
			String logEntry = String.format(SimulationLogService.IN_FORMAT, p, System.currentTimeMillis());
			LOGGER.log(Level.INFO,String.format(" new log entry %s", logEntry.substring(0, logEntry.length()-2)));
			this.file.write(logEntry);
			this.file.flush();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("logIn: error %s", e.getMessage()), e);
		}
	}
	
	@GET
	@Path("/out")
	public void out(@QueryParam(value="p") String p){
		try {
			String logEntry = String.format(SimulationLogService.OUT_FORMAT, p, System.currentTimeMillis());
			LOGGER.log(Level.INFO,String.format(" new log entry %s", logEntry.substring(0, logEntry.length()-2)));
			this.file.write(logEntry);
			this.file.flush();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("logOut: error %s", e.getMessage()), e);
		}
	}
}
