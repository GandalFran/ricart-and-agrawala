package com.ssdd.util.logging.centralized;

import java.io.FileWriter;
import java.io.IOException;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.ssdd.util.constants.IConstants;
import com.ssdd.util.constants.ILoggingConstants;

@Singleton
@Path("/log")
public class CentralizedLogService {
	
	private FileWriter file;
	
	public CentralizedLogService() {
		try {
			this.file = new FileWriter(ILoggingConstants.CENTRALIZED_LOG_FILE, true);
		} catch (IOException e) {
			System.exit(IConstants.EXIT_CODE_IO_ERROR);
		}
	}
	
	public static CentralizedLogService buildProxy(String host) {
		String serviceUri = CentralizedLogService.buildServiceUri(host);
		return new CentralizedLogServiceProxy(serviceUri);
	}
	
	public static String buildServiceUri(String host){
		return String.format("http://%s:8080/ssdd/log", host);
	}
	
	/**
	 * shows service status.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return string indicating the status of the service is up.
	 * */
	@GET
	@Path("/status")
	@Produces(MediaType.TEXT_PLAIN)
	public String status() {
		return "{ \"service\": \"centralized log\", \"status\": \"ok\"}";
	}
	@GET
	@Path("/write")
	public void log(@QueryParam(value="line") String line){
		try {
			this.file.write(line);
			this.file.flush();
		} catch (IOException e) {
			System.err.println("ERROR: log request failed");
			System.exit(IConstants.EXIT_CODE_IO_ERROR);
		}
	}
}
