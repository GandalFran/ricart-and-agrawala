package com.ssdd.util.logging.centralized;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
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
		String encodedLine = UriComponent.encode(line, UriComponent.Type.FRAGMENT);
		this.service.path("write").queryParam("line", encodedLine).request().get();
	}
}
