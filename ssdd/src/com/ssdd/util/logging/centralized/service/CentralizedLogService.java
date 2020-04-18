package com.ssdd.util.logging.centralized.service;

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

/** 
 * Centralized logging service to deploy in Apache Tomcat 7.0.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
@Singleton
@Path("/log")
public class CentralizedLogService {
	
    /** 
     * File to write the logging content.
     */
	private FileWriter file;
    /** 
     * flag to indicate if the write is permitted in current service.
     */
	private boolean isFileWritable;
	
	public CentralizedLogService() {
		try {
			this.isFileWritable = true;
			this.file = new FileWriter(ILoggingConstants.CENTRALIZED_LOG_FILE, true);
		} catch (IOException e) {
			this.isFileWritable = false;
		}
	}
	
	/**
	 * factory method, to build a proxy to access an instance of this service in remote.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param host the IP adress and PORT of server in which the service is allocated.
	 * 
	 * @return CentralizedLogServerProxy to serve as proxy for the /log service, served in the given host
	 * */
	public static CentralizedLogService buildProxy(String host) {
		String serviceUri = CentralizedLogService.buildServiceUri(host);
		return new CentralizedLogServiceProxy(serviceUri);
	}
	
	/**
	 * factory method, to build a URI for a CentralizedLogService from the host IP and PORT.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param host the IP adress and PORT of server in which the service is allocated.
	 * 
	 * @return String containing the URI to the service, served in the given host
	 * */
	public static String buildServiceUri(String host){
		return String.format(IConstants.BASE_URI_FORMAT + "/log", host);
	}
	
	/**
	 * shows service status.
	 * 
	 * @version 1.0
	 * @author Hï¿½ctor Sï¿½nchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return string indicating the status of the service is up.
	 * */
	@GET
	@Path("/status")
	@Produces(MediaType.TEXT_PLAIN)
	public String status() {
		return "{ \"service\": \"log\", \"status\": \"ok\", \"available\": \""+ this.isFileWritable +"\"}";
	}
	
	/**
	 * Indicates if the file is writable and therefore if current service is suitable for logging
	 * 
	 * @version 1.0
	 * @author Hï¿½ctor Sï¿½nchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return string indicating if the server is suitable for logging.
	 * */
	@GET
	@Path("/available")
	@Produces(MediaType.TEXT_PLAIN)
	public String isAvailable(){
		return new Boolean(this.isFileWritable).toString();
	}
	
	/**
	 * writes the received line in the file, and then flushes it to ensure that line is persisted in file.
	 * 
	 * @version 1.0
	 * @author Hï¿½ctor Sï¿½nchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param line the line to write in the service's log file
	 * */
	@GET
	@Path("/write")
	public void log(@QueryParam(value="line") String line){
		try {
			this.file.write(line);
			this.file.flush();
		} catch (Exception e) {
			System.err.println("ERROR: log request failed");
		}
	}
}
