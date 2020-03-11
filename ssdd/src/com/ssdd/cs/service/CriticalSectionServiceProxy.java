package com.ssdd.cs.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.google.gson.Gson;
import com.ssdd.cs.bean.LamportCounter;
import com.ssdd.util.logging.SSDDLogFactory;

/** 
 * Critical section proxy, to access a Critical Section service in a easier way
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class CriticalSectionServiceProxy extends CriticalSectionService{

    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionServiceProxy.class);
    
    /**
	 * Critical section service URI.
	 * */
	private String serviceUri;
	/**
	 * api client to make requests to Critical section service..
	 * */
	private WebTarget service;
	
	public CriticalSectionServiceProxy(String serviceUri) {
		this.serviceUri = serviceUri;
		this.service = ClientBuilder.newClient().target(UriBuilder.fromUri(serviceUri).build());
	}
	
	/**
	 * method to parse the /cs/suscribed response from JSON serialized String [] to String [].
	 * 
	 * @see com.ssdd.cs.service.CriticalSectionService#suscribed()
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param response of the /cs/suscribed response
	 * 
	 * @return String [] with deserialized response
	 * */
	public static String [] parseSuscribedResponse(String response) {
		return new Gson().fromJson(response, String[].class);
	}
	
	
	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#suscribe(String)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node who wants to suscribe to current service.
	 * */
	@Override
	public void suscribe(String nodeId){
		LOGGER.log(Level.INFO, String.format("[node: %s] suscribe", nodeId));
		try {
			this.service.path("suscribe").queryParam("node", nodeId).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] suscribe: error %s", nodeId, e.getMessage()), e);
		}
	}
	
	@Override
	public void lock(String nodeId){
		LOGGER.log(Level.INFO, String.format("[node: %s] lock", nodeId));
		try {
			this.service.path("lock").queryParam("node", nodeId).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] lock: error %s", nodeId, e.getMessage()), e);
		}
	}
	
	@Override
	public void unlock(String nodeId){
		LOGGER.log(Level.INFO, String.format("[node: %s] unlock", nodeId));
		try {
			this.service.path("unlock").queryParam("node", nodeId).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] unlock: error %s", nodeId, e.getMessage()), e);
		}
	}

	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#suscribed()}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return String containing the list of suscribed nodes separated with the '_character'.
	 * */
	@Override
	public String suscribed(){
		LOGGER.log(Level.INFO, String.format("suscribed"));
		try {
			return this.service.path("suscribed").request(MediaType.APPLICATION_JSON).get(String.class);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("suscribed: error %s", e.getMessage()), e);
			return null;
		}
	}

	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#setCsState(String, String)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be a suscribed node.
	 * @param newstate the new state given to the critical section by the node
	 * */
	@Override
	public void setCsState(String nodeId, String newState){
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/set/state %s", nodeId, newState));
		try {
			this.service.path("set").path("state").queryParam("node", nodeId).queryParam("state", newState.toString()).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/set/state: error %s", nodeId, e.getMessage()), e);
		}
	}
	
	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#getLamport(String)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be a suscribed node.
	 * 
	 * @return a JSON serialized lamport counter, correspondind to the requested node
	 * 
	 * */
	@Override
	public String getLamport(String nodeId){
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/get/lamport", nodeId));
		try {
			return this.service.path("get").path("lamport").queryParam("node", nodeId).request(MediaType.APPLICATION_JSON).get(String.class);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/get/lamport: error %s", nodeId, e.getMessage()), e);
			return null;
		}
	}
	
	
	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#request(String, String)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be a suscribed node.
	 * @param request a JSON serialized {@link com.ssdd.cs.bean.CriticalSectionMessage} containing the request.
	 * */
	@Override
	public void request(String nodeId, String sender, long time){
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request", nodeId));
		try {
			this.service.path("request").queryParam("node", nodeId).queryParam("sender", sender).queryParam("time", time).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/request: error %s", nodeId, e.getMessage()), e);
		}
	}
	

	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#release(String)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be a suscribed node.
	 * @param request a JSON serialized {@link com.ssdd.cs.bean.CriticalSectionMessage} containing the request.
	 * */
	@Override
	public void release(String nodeId){
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/release", nodeId));
		try {
			this.service.path("release").queryParam("node", nodeId).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/release: error %s", nodeId, e.getMessage()), e);
		}
	}
	
}
