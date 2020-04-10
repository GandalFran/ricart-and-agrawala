package com.ssdd.util.logging.centralized;

import java.io.FileWriter;
import java.io.IOException;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.ssdd.util.constants.IConstants;

@Singleton
@Path("/log")
public class CentralizedLogService {
	
	private FileWriter file;
	
	public CentralizedLogService() {
		try {
			this.file = new FileWriter(IConstants.CENTRALIZED_LOG_FILE, true);
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
