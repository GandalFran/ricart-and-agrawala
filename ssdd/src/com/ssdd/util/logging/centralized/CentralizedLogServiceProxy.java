package com.ssdd.util.logging.centralized;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.uri.UriComponent;

public class CentralizedLogServiceProxy extends CentralizedLogService{

	/**
	 * api client to make requests to NTP service..
	 * */
	private WebTarget service;
	
	public CentralizedLogServiceProxy(String serviceUri) {
		this.service = ClientBuilder.newClient().target(UriBuilder.fromUri(serviceUri).build());
	}
	
	@Override
	public void log(String line){
		try {
			String encodedLine = UriComponent.encode(line, UriComponent.Type.FRAGMENT);
			this.service.path("write").queryParam("line", encodedLine).request().get();
		} catch (Exception e) {}
	}
	
	@Override
	public String isAvailable() {
		try {
			return this.service.path("isAvailable").request(MediaType.TEXT_PLAIN).get(String.class);
		} catch (Exception e) {
			return new Boolean(false).toString();
		}
		
	}
	
	public static boolean parseIsAvailableResponse(String response) {
		return Boolean.parseBoolean(response);
	}
}
