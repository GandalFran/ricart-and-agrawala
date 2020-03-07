package com.ssdd.cs.service;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

public class CriticalSectionServiceProxy extends CriticalSectionService{

	private String serviceUri;
	private WebTarget service;
	
	public CriticalSectionServiceProxy(String serviceUri) {
		this.serviceUri = serviceUri;
		this.service = ClientBuilder.newClient().target(UriBuilder.fromUri(serviceUri).build());
	}
	
	public static String [] parseSuscribedResponse(String response) {
		String [] splittedresponse = response.split("_");
		return splittedresponse;
	}
	
}
