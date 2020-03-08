package com.ssdd.cs.service;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.ssdd.cs.bean.CriticalSectionMessage;
import com.ssdd.cs.bean.CriticalSectionMessageType;
import com.ssdd.cs.bean.CriticalSectionState;
import com.ssdd.cs.bean.LamportCounter;
import com.ssdd.util.logging.SSDDLogFactory;

public class CriticalSectionServiceProxy extends CriticalSectionService{

    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionServiceProxy.class);
	
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
	
	
	/**
	 * @see com.ssdd.cs.service.CriticalSectionService#subscribe(String)
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * */
	public void subscribe(String nodeId){
		LOGGER.log(Level.INFO, String.format("[node: %s] subscribe", nodeId));
		try {
			this.service.path("suscribe").queryParam("node", nodeId).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] subscribe: error %s", nodeId, e.getMessage()), e);
		}
	}

	/**
	 * @see com.ssdd.cs.service.CriticalSectionService#suscribed()
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * */
	public String suscribed(){
		LOGGER.log(Level.INFO, String.format("subscribed"));
		try {
			return this.service.path("subscribed").request(MediaType.TEXT_PLAIN).get(String.class);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("suscribed: error %s", e.getMessage()), e);
			return null;
		}
	}
	
	/**
	 * @see com.ssdd.cs.service.CriticalSectionService#requestAccess(String, String)
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * */
	public String requestAccess(String nodeId, String request){
		LOGGER.log(Level.INFO, String.format("[node: %s] requestAccess", nodeId));
		try {
			return this.service.path("request").queryParam("node", nodeId).queryParam("request", request).request(MediaType.TEXT_PLAIN).get(String.class);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] requestAccess: error %s", nodeId, e.getMessage()), e);
			return null;
		}
	}
	
	/**
	 * @see com.ssdd.cs.service.CriticalSectionService#treatDelayedresponses(String)
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * */
	public void treatDelayedresponses(String nodeId){
		LOGGER.log(Level.INFO, String.format("treatDelayedresponses"));
		try {
			this.service.path("response").queryParam("node", nodeId).request(MediaType.TEXT_PLAIN).post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] treatDelayedresponses: error %s", nodeId, e.getMessage()), e);
		}
	}
	
	/**
	 * @see com.ssdd.cs.service.CriticalSectionService#release(String)
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * */
	public void release(String nodeId){
		LOGGER.log(Level.INFO, String.format("[node: %s] release", nodeId));
		try {
			this.service.path("release").queryParam("node", nodeId).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] release: error %s", nodeId, e.getMessage()), e);
		}
	}
	
}
