package com.ssdd.cs.service;


import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.ssdd.cs.bean.CriticalSectionMessage;
import com.ssdd.cs.bean.CriticalSectionState;
import com.ssdd.cs.bean.LamportCounter;
import com.ssdd.util.logging.SSDDLogFactory;

@Singleton
@Path("/cs")
public class CriticalSectionService{

    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionService.class);
	
	private Map<String, LamportCounter> lamportTime;
	private Map<String, CriticalSectionState> state;
	private Map<String, Object> releaseNotifier;
	private Map<String,Semaphore> locks;
	
	public CriticalSectionService() {
		this.lamportTime = new ConcurrentHashMap<String, LamportCounter>();
		this.state = new ConcurrentHashMap<String, CriticalSectionState>();
		this.releaseNotifier = new ConcurrentHashMap<String, Object>();
		this.locks = new ConcurrentHashMap<String, Semaphore>();
	}

	/**
	 * factory method, to build a proxy to access an instance of this service in remote.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param host the IP adress and PORT of server in which the service is allocated.
	 * 
	 * @return CriticalSectionService to serve as proxy for the /cs service, served in the given host
	 * */
	public static CriticalSectionService buildProxy(String host) {
		String serviceUri = CriticalSectionService.buildServiceUri(host);
		return new CriticalSectionServiceProxy(serviceUri);
	}

	/**
	 * factory method, to build a URI for a CriticalSectionService from the host IP and port.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param host the IP adress and PORT of server in which the service is allocated.
	 * 
	 * @return String containing the URI to the service, served in the given host
	 * */
	public static String buildServiceUri(String host) {
		return String.format("http://%s/ssdd/cs", host);
	}
	
	/**
	 * shows service status.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return string indicating the status of the service is up.
	 * */
	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	public String status() {
		return "{ \"service\": \"cs\", \"status\": \"ok\"}";
	}
	
	/**
	 * registers a node, and create the neccesary structures to work with it.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node who wants to suscribe to current service.
	 * */
	@POST
	@Path("/suscribe")
	public void suscribe(@QueryParam(value="node") String nodeId){
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/suscribe", nodeId));
		this.lamportTime.put(nodeId, new LamportCounter());
		this.state.put(nodeId, CriticalSectionState.FREE);
		this.releaseNotifier.put(nodeId, new Object());		
		this.locks.put(nodeId, new Semaphore(1));
	}

	/**
	 * return a JSON serialized list of suscribed nodes.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return String containing the list of suscribed nodes separated with the '_character'.
	  */
	@GET
	@Path("/suscribed")
	@Produces(MediaType.APPLICATION_JSON)
	public String suscribed(){
		LOGGER.log(Level.INFO, String.format("/cs/suscribed"));

		// get nodes
		String[] suscribedNodes = new String[this.state.keySet().size()];
		this.state.keySet().toArray(suscribedNodes);
		
		// serialize list of nodes to JSON
		String response = new Gson().toJson(suscribedNodes);
		return response;
	}

	
	/**
	 * set new state for the critical section variable on a concrete node.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be a suscribed node.
	 * @param newstate the new state given to the critical section by the node
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to current service.
	 * */
	@POST
	@Path("/set/state")
	public void setCsState(@QueryParam(value="node") String nodeId, @QueryParam(value="state") String newState) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/set/state %s", nodeId, newState));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/set/state: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		//set new state
		CriticalSectionState state = CriticalSectionState.valueOf(newState);
		this.state.put(nodeId, state);
	}
	
	/**
	 * get lamport counter for a node.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be a suscribed node.
	 * 
	 * 
	 * @return a JSON serialized lamport counter, correspondind to the requested node
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to current service.
	 * */
	@GET
	@Path("/get/lamport")
	@Produces(MediaType.TEXT_PLAIN)
	public String getLamport(@QueryParam(value="node") String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/get/lamport", nodeId));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/set/state: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}

		// get lamport counter and serialize to json
		LamportCounter counter = this.lamportTime.get(nodeId);
		String jsonCounter = counter.toJson();
		
		return jsonCounter;
	}

	
	
	/**
	 * processes the requests to the critical section access send by other nodes.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be a suscribed node.
	 * @param request a JSON serialized {@link com.ssdd.cs.bean.CriticalSectionMessage} containing the request.
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to current service.
	 * */
	@POST
	@Path("/request")
	public void request(@QueryParam(value="node") String nodeId, @QueryParam(value="sender") String sender, @QueryParam(value="time") long time) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request sender %s ", nodeId, sender));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/request: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node state
		LamportCounter lamportTime = this.lamportTime.get(nodeId);
		CriticalSectionState state = this.state.get(nodeId);
		
		// update local lamport time
		lamportTime.update(time);
		
		if(state == CriticalSectionState.ACQUIRED
				|| (state == CriticalSectionState.REQUESTED && this.compareCounters(nodeId, sender, lamportTime.getCounter(),time))) {
			LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request node %s QUEUED", nodeId, sender));
			// make wait for process to release the critical section
			LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request node %s ALLOWED", nodeId, sender));
			try {
				Object notifier = this.releaseNotifier.get(nodeId);
				synchronized(notifier) {
					notifier.wait();
				}
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/request: ERROR when waiting", nodeId));
			}
		}else {
			LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request node %s ALLOWED", nodeId, sender));
		}
		
	}
	
	private boolean compareCounters(String n1, String n2, long c1, long c2) {
		return (c1 == c2) ? (n1.compareTo(n2) < 0) : (c1 < c2);
	}

	/**
	 * processes the requests to the critical section access send by other nodes.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be a suscribed node.
	 * @param request a JSON serialized {@link com.ssdd.cs.bean.CriticalSectionMessage} containing the request.
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to current service.
	 * */
	@POST
	@Path("/release")
	public void release(@QueryParam(value="node") String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/release", nodeId));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/release: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// notify all that critical section has been released
		Object notifier = this.releaseNotifier.get(nodeId);
		synchronized(notifier){
			notifier.notifyAll();
		}
	}
	
	
	/**
	 * given a nodeId checks if is suscribed to current service instance.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId id of the node we want to check if you have subscribed to this service.
	 * 
	 * @return boolean indicating if node is suscribed to this broker
	 * */
	private boolean isSuscribed(String nodeId) {
		return this.state.keySet().contains(nodeId);
	}
	
	@GET
	@Path("/update/lamport")
	public void updateLamport(@QueryParam(value="node") String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/update/lamport", nodeId));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/set/state: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}

		// get lamport counter and update
		LamportCounter counter = this.lamportTime.get(nodeId);
		counter.update();
	}	
	
	@POST
	@Path("/lock")
	public void lock(@QueryParam(value="node") String nodeId){
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/lock", nodeId));
		try {
			this.locks.get(nodeId).acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@POST
	@Path("/unlock")
	public void unlock(@QueryParam(value="node") String nodeId){
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/unlock", nodeId));
		this.locks.get(nodeId).release();
	}
	
	@POST
	@Path("/restart")
	public void restart(){
		LOGGER.log(Level.INFO, String.format("/cs/restart"));
		this.lamportTime = new ConcurrentHashMap<String, LamportCounter>();
		this.state = new ConcurrentHashMap<String, CriticalSectionState>();
		this.releaseNotifier = new ConcurrentHashMap<String, Object>();
		this.locks = new ConcurrentHashMap<String, Semaphore>();
	}

}
