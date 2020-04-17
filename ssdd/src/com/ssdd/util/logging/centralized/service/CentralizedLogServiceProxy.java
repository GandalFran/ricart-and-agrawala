package com.ssdd.util.logging.centralized.service;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.uri.UriComponent;

/** 
 * Centralized log proxy, to access a Centralized log service in a easier way
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class CentralizedLogServiceProxy extends CentralizedLogService{

	/**
	 * api client to make requests to NTP service..
	 * */
	private WebTarget service;
	
	public CentralizedLogServiceProxy(String serviceUri) {
		this.service = ClientBuilder.newClient().target(UriBuilder.fromUri(serviceUri).build());
	}

	/**
	 * See {@link com.ssdd.util.logging.centralized.service.CentralizedLogService#isAvailable()}
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return string indicating if the server is suitable for logging.
	 * */
	@Override
	public String isAvailable() {
		try {
			String result = this.service.path("available").request(MediaType.TEXT_PLAIN).get(String.class);
			return result;
		} catch (Exception e){
			return new Boolean(false).toString();
		}
		
	}
	
	/**
	 * Parses the /log/available service response from String to boolean.
	 * @see com.ssdd.util.logging.centralized.service.CentralizedLogService#isAvailable()
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param response thre response of the service's /log/available response
	 * 
	 * @return boolean with the response.
	 * */
	public static boolean parseIsAvailableResponse(String response) {
		return Boolean.parseBoolean(response);
	}
	
	/**
	 * See {@link com.ssdd.util.logging.centralized.service.CentralizedLogService#log(String)}
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param line the line to write in the service's log file
	 * */
	@Override
	public void log(String line){
		try {
			String encodedLine = UriComponent.encode(line, UriComponent.Type.FRAGMENT);
			this.service.path("write").queryParam("line", encodedLine).request().get();
		} catch (Exception e) {}
	}
}
