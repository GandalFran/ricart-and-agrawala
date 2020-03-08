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
	
	/**
	 * method to parse the /cs/suscribed response from String to String [].
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param response of the /cs/suscribed response
	 * 
	 * @return String [], with splitted response by the selected character "_"
	 * */
	public static String [] parseSuscribedResponse(String response) {
		String [] splittedresponse = response.split("_");
		return splittedresponse;
	}
	
}
