package com.ssdd.ntp.service;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/** 
 * NTP proxy, to access a NTP service in a easier way
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class NTPServiceProxy extends NTPService{

	/**
	 * NTP service URI.
	 * */
	private String serviceUri;
	/**
	 * api client to make requests to NTP service..
	 * */
	private WebTarget service;
	
	public NTPServiceProxy(String serviceUri) {
		this.serviceUri = serviceUri;
		this.service = ClientBuilder.newClient().target(UriBuilder.fromUri(serviceUri).build());
	}
	
	/**
	 * See {@link com.ssdd.ntp.service.NTPService#time()}
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return array with to long, corresponding to time1 and time2 in "time1_time2" response.
	 * */
	@Override
	public String time() {
		try {
			return this.service.path("time").request(MediaType.TEXT_PLAIN).get(String.class);
		} catch (Exception ex) {
			System.err.println("["+ Thread.currentThread().getId()+"] An error occurred in " + ex.toString());
			return null;
		}
	}
	
	/**
	 * method to parse the /ntp/time response from String to long [].
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param response of the /ntp/time response
	 * 
	 * @return array with to long, corresponding to time1 and time2 in "time1_time2" response.
	 * */
	public static long[] parseTimeResponse(String response) {
		String [] splittedresponse = response.split("_");
		return new long [] {Long.parseLong(splittedresponse[0]), Long.parseLong(splittedresponse[1]) };
	}
	
	@Override
	public String toString() {
		return this.serviceUri;
	}

	public String getServiceUri() {
		return serviceUri;
	}

	public void setServiceUri(String serviceUri) {
		this.serviceUri = serviceUri;
	}
	
}
