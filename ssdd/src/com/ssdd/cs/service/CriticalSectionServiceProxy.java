package com.ssdd.cs.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.google.gson.Gson;
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
	 * method to parse the /cs/suscribed response from JSON serialized string array, to a String [].
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
	 * See {@link com.ssdd.cs.service.CriticalSectionService#restart()}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * */
	@Override
	public void restart() {
		LOGGER.log(Level.INFO, String.format("/cs/restart"));
		try {
			this.service.path("restart").request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/restart: error %s", e.getMessage()), e);
		}
	}
	
	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#suscribe(String)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node who wants to suscribe to requested service
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

	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#suscribed()}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return String containing a JSON serialized array with the list of nodes suscribed to requested service
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
	 * @param nodeId the id of the node trying to accces the critical section. Must be suscribed to requested service.
	 * @param newState the new state given to the critical section by the node. Must be a String serialized {@link com.ssdd.cs.bean.CriticalSectionState}
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to requested service.
	 * */
	@Override
	public void setCsState(String nodeId, String newState) throws NodeNotFoundException{
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/set/state %s", nodeId, newState));
		try {
			this.service.path("set").path("state").queryParam("node", nodeId).queryParam("state", newState.toString()).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/set/state: error %s", nodeId, e.getMessage()), e);
			throw new NodeNotFoundException(nodeId);
		}
	}
	
	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#getMessageTimeStamp(String)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be suscribed to requested service.
	 * 
	 * @return the current lamport counter value, corresponding to the requested node.
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to requested service.
	 * */
	@Override
	public long getMessageTimeStamp(String nodeId) throws NodeNotFoundException{
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/get/messagetimestamp", nodeId));
		try {
			String data = this.service.path("get").path("messagetimestamp").queryParam("node", nodeId).request(MediaType.TEXT_PLAIN).get(String.class);
			return Long.parseLong(data);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/messagetimestamp: error %s", nodeId, e.getMessage()), e);
			throw new NodeNotFoundException(nodeId);
		}
	}

	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#updateCounter(String)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be suscribed to requested service.
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to requested service.
	 * */
	@Override
	public void updateCounter(String nodeId) throws NodeNotFoundException{
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/update/counter", nodeId));
		try {
			this.service.path("update").path("counter").queryParam("node", nodeId).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/update/counter: error %s", nodeId, e.getMessage()), e);
			throw new NodeNotFoundException(nodeId);
		}
	}
	
	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#request(String, String, long)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node that will be asked to access the critical section. Must be suscribed to requested service.
	 * @param sender the id of the node trying to accces the critical section.
	 * @param messageTimeStamp the message's timestamp (node's lamport time counter value)
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to requested service
	 * */
	@Override
	public void request(String nodeId, String sender, long messageTimeStamp) throws NodeNotFoundException{
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request", nodeId));
		try {
			this.service.path("request").queryParam("node", nodeId).queryParam("sender", sender).queryParam("messageTimeStamp", messageTimeStamp).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/request: error %s", nodeId, e.getMessage()), e);
			throw new NodeNotFoundException(nodeId);
		}
	}
	

	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#release(String)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be suscribed to requested service.
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to requested service. 
	 * */
	@Override
	public void release(String nodeId) throws NodeNotFoundException{
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/release", nodeId));
		try {
			this.service.path("release").queryParam("node", nodeId).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/release: error %s", nodeId, e.getMessage()), e);
			throw new NodeNotFoundException(nodeId);
		}
	}
	
	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#lock(String)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to lock. Must be suscribed to requested service.
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to requested service. 
	 * */
	@Override
	public void lock(String nodeId) throws NodeNotFoundException{
		LOGGER.log(Level.INFO, String.format("[node: %s] lock", nodeId));
		try {
			this.service.path("lock").queryParam("node", nodeId).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] lock: error %s", nodeId, e.getMessage()), e);
			throw new NodeNotFoundException(nodeId);
		}
	}
	
	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#unlock(String)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to unlock. Must be suscribed to requested service.
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to requested service. 
	 * */
	@Override
	public void unlock(String nodeId) throws NodeNotFoundException{
		LOGGER.log(Level.INFO, String.format("[node: %s] unlock", nodeId));
		try {
			this.service.path("unlock").queryParam("node", nodeId).request().post(null);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] unlock: error %s", nodeId, e.getMessage()), e);
			throw new NodeNotFoundException(nodeId);
		}
	}

	@Override
	public String toString() {
		return this.serviceUri;
	}
}
