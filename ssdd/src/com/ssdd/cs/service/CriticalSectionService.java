package com.ssdd.cs.service;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import com.ssdd.cs.bean.CritialSectionServiceNode;
import com.ssdd.cs.bean.CriticalSectionState;
import com.ssdd.cs.bean.LamportCounter;
import com.ssdd.util.logging.SSDDLogFactory;

@Singleton
@Path("/cs")
public class CriticalSectionService{

    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionService.class);

	private Map<String, CritialSectionServiceNode> nodes;
	
	public CriticalSectionService() {
		this.nodes = new ConcurrentHashMap<String, CritialSectionServiceNode>();
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
	@Produces(MediaType.TEXT_PLAIN)
	public String status() {
		return "{ \"service\": \"cs\", \"status\": \"ok\"}";
	}
	
	@POST
	@Path("/restart")
	public void restart(){
		LOGGER.log(Level.INFO, String.format("/cs/restart"));
		this.nodes.clear();
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
		CritialSectionServiceNode node = new CritialSectionServiceNode(nodeId, new LamportCounter(), CriticalSectionState.FREE);
		this.nodes.put(nodeId, node);
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
		String[] suscribedNodes = new String[this.nodes.keySet().size()];
		this.nodes.keySet().toArray(suscribedNodes);
		
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
		
		// get node
		CritialSectionServiceNode node = this.nodes.get(nodeId);
		
		//set new state
		node.setState(CriticalSectionState.valueOf(newState));
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
	@Path("/get/messagetimestamp")
	@Produces(MediaType.TEXT_PLAIN)
	public long getMessageTimeStamp(@QueryParam(value="node") String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/get/messagetimestamp", nodeId));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/set/state: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = this.nodes.get(nodeId);
		
		// save and obtain counter value
		long timeStamp = node.saveLastTimeStamp();
		
		return timeStamp;
	}


	@GET
	@Path("/update/counter")
	public void updateCounter(@QueryParam(value="node") String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/update/counter", nodeId));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/update/counter: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = this.nodes.get(nodeId);

		// update counter
		node.getCounter().update();
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
	public void request(@QueryParam(value="node") String nodeId, @QueryParam(value="sender") String sender, @QueryParam(value="messageTimeStamp") long messageTimeStamp) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request sender %s ", nodeId, sender));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/request: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = this.nodes.get(nodeId);
		
		// lock operations
		node.lock();
		
		// update local lamport time
		node.getCounter().update(messageTimeStamp);
		
		if(node.getState() == CriticalSectionState.ACQUIRED
				|| (node.getState() == CriticalSectionState.REQUESTED && this.compareCounters(nodeId, sender, node.getLastTimeStamp(), messageTimeStamp))) {
			LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request node %s QUEUED", nodeId, sender));
			// unlock operations
			node.unlock();
			// wait until the enter in CS is permited
			node.waitToReleaseCriticalSection();
			LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request node %s DEQUEUED (ALLOWED)", nodeId, sender));
		}else {
			// unlock operations
			node.unlock();
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
		
		// get node
		CritialSectionServiceNode node = this.nodes.get(nodeId);

		// lock operations
		node.lock();

		// set new state
		node.setState(CriticalSectionState.FREE);
		
		// notify all that critical section has been released
		node.releaseCriticalSection();

		// unlock operations
		node.unlock();
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
		return this.nodes.keySet().contains(nodeId);
	}
	
	@POST
	@Path("/lock")
	public void lock(@QueryParam(value="node") String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/lock", nodeId));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/release: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = this.nodes.get(nodeId);
		
		// lock
		node.lock();
	}
	
	@POST
	@Path("/unlock")
	public void unlock(@QueryParam(value="node") String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/unlock", nodeId));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/release: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = this.nodes.get(nodeId);
		
		// unlock
		node.unlock();
	}

}
