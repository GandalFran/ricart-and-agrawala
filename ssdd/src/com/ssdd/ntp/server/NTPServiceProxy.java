package com.ssdd.ntp.server;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class NTPServiceProxy extends NTPService{

	private String serviceUri;
	private WebTarget service;
	
	public NTPServiceProxy(String serviceUri) {
		this.serviceUri = serviceUri;
		this.service = ClientBuilder.newClient().target(UriBuilder.fromUri(serviceUri).build());
	}
	
	@Override
	public String pedirTiempo() {
		try {
			return this.service.path("pedirTiempo").request(MediaType.TEXT_PLAIN).get(String.class);
		} catch (Exception ex) {
			System.err.println("["+ Thread.currentThread().getId()+"] An error occurred in " + ex.toString());
			return null;
		}
	}
	
	public static long[] parsePedirTiempoResponse(String response) {
		String [] splittedresponse = response.split("_");
		return new long [] {Long.parseLong(splittedresponse[0]), Long.parseLong(splittedresponse[1]) };
	}

	public String getServiceUri() {
		return serviceUri;
	}

	public void setServiceUri(String serviceUri) {
		this.serviceUri = serviceUri;
	}
	
	
}
