package com.ssdd.cs.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
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
	 * @param numProcesses the number of processes that will try to access to critial section
	 * */
	@Override
	public void restart(int numProcesses) {
		LOGGER.log(Level.INFO, String.format("/cs/restart numProcesses:%d", numProcesses));
		try {
			this.service.path("restart").queryParam("numProcesses", numProcesses).request().get();
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
	 * @param processId the id of the process who wants to suscribe to requested service
	 * */
	@Override
	public void suscribe(String processId){
		LOGGER.log(Level.INFO, "/cs/suscribe");
		try {
			this.service.path("suscribe").queryParam("process", processId).request().get();
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
	 * @return String containing a JSON serialized array with the list of processes suscribed to requested service
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
	 * See {@link com.ssdd.cs.service.CriticalSectionService#setRequested(String)}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param processId the id of the sender. Must be suscribed to current service.
	 * 
	 * @throws ProcessNotFoundException when then processId doesn't corresponds to any process suscribed to current service
	 * */
	public long setRequested(String processId) throws ProcessNotFoundException {
		LOGGER.log(Level.INFO, "/set/requested");
		try {
			String repsonse = this.service.path("set").path("requested").queryParam("process", processId).request(MediaType.TEXT_PLAIN).get(String.class);
			return Long.parseLong(repsonse);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/set/requested: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
			return 0;
		}
	}
	
	/**
	 * used by processes to notify to its associated service that the critical section is acquired
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param processId the id of the sender. Must be suscribed to current service.
	 * 
	 * @throws ProcessNotFoundException when then processId doesn't corresponds to any process suscribed to current service
	 * */
	public void setAcquired(String processId) throws ProcessNotFoundException {
		LOGGER.log(Level.INFO, "/set/acquired");
		try {
			this.service.path("set").path("acquired").queryParam("process", processId).request().get();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/set/acquired: error %s", e.getMessage()), e);
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
	 * @param processId the id of the process that will be asked to access the critical section. Must be suscribed to requested service.
	 * @param sender the id of the process trying to accces the critical section.
	 * @param messageTimeStamp the message's timestamp (process's lamport time counter value)
	 * 
	 * @throws ProcessNotFoundException when then processId doesn't corresponds to any process suscribed to requested service
	 * */
	@Override
	public void request(String processId, String sender, long messageTimeStamp) throws ProcessNotFoundException{
		LOGGER.log(Level.INFO, "/cs/request");
		try {
			this.service.path("request").queryParam("process", processId).queryParam("sender", sender).queryParam("messageTimeStamp", messageTimeStamp).request().get();
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
	 * @param processId the id of the process trying to accces the critical section. Must be suscribed to requested service.
	 * 
	 * @throws ProcessNotFoundException when then processId doesn't corresponds to any process suscribed to requested service. 
	 * */
	@Override
	public void release(String processId) throws ProcessNotFoundException{
		LOGGER.log(Level.INFO, "/cs/release");
		try {
			this.service.path("release").queryParam("process", processId).request().get();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("/cs/release: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
		}
	}

	@Override
	public String toString() {
		return this.serviceUri;
	}
}
