package com.ssdd.cs.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.google.gson.Gson;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

/** 
 * Critical section proxy, to access a Critical Section service in a easier way
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class CriticalSectionServiceProxy extends CriticalSectionService{

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
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
	 * See {@link com.ssdd.cs.service.CriticalSectionService#restart(int)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param numNodes the number of nodes that will try to access to critial section
	 * */
	@Override
	public void restart(int numNodes) {
		LOGGER.log(Level.INFO, String.format("/cs/restart numNodes:%d", numNodes));
		try {
			this.service.path("restart").queryParam("numNodes", numNodes).request().get();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/restart: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
		}
	}
	
	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#ready()}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * */
	@Override
	public void ready() {
		LOGGER.log(Level.INFO, "/cs/ready");
		try {
			this.service.path("ready").request().get();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/ready: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
		}
	}
	
	/**
	 * See {@link com.ssdd.cs.service.CriticalSectionService#finished()}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * */
	@Override
	public void finished() {
		LOGGER.log(Level.INFO, "/cs/finished");
		try {
			this.service.path("finished").request().get();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/finished: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
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
		LOGGER.log(Level.INFO, "/cs/suscribe");
		try {
			this.service.path("suscribe").queryParam("node", nodeId).request().get();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/suscribe: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
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
		LOGGER.log(Level.INFO, "/cs/suscribed");
		try {
			return this.service.path("suscribed").request(MediaType.APPLICATION_JSON).get(String.class);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/suscribed: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
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
		LOGGER.log(Level.INFO, String.format("/cs/set/state %s", newState));
		try {
			this.service.path("set").path("state").queryParam("node", nodeId).queryParam("state", newState.toString()).request().get();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/set/state: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
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
		LOGGER.log(Level.INFO, "/cs/get/messagetimestamp");
		try {
			String data = this.service.path("get").path("messagetimestamp").queryParam("node", nodeId).request(MediaType.TEXT_PLAIN).get(String.class);
			return Long.parseLong(data);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/messagetimestamp: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
			return 0;
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
		LOGGER.log(Level.INFO, "/cs/update/counter");
		try {
			this.service.path("update").path("counter").queryParam("node", nodeId).request().get();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/update/counter: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
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
		LOGGER.log(Level.INFO, "/cs/request");
		try {
			this.service.path("request").queryParam("node", nodeId).queryParam("sender", sender).queryParam("messageTimeStamp", messageTimeStamp).request().get();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/request: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
		}
	}
	
	public void request(String nodeId, String sender, long messageTimeStamp,  InvocationCallback<Response> callback) throws NodeNotFoundException{
		LOGGER.log(Level.INFO, "/cs/request");
		try {
			this.service.path("request").queryParam("node", nodeId).queryParam("sender", sender).queryParam("messageTimeStamp", messageTimeStamp).request().async().get(callback);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/request: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
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
		LOGGER.log(Level.INFO, "/cs/release");
		try {
			this.service.path("release").queryParam("node", nodeId).request().get();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/release: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
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
		LOGGER.log(Level.INFO, "/cs/lock");
		try {
			this.service.path("lock").queryParam("node", nodeId).request().get();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/lock: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
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
		LOGGER.log(Level.INFO, "/cs/unlock");
		try {
			this.service.path("unlock").queryParam("node", nodeId).request().get();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/unlock: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
		}
	}

	@Override
	public String toString() {
		return this.serviceUri;
	}
}
