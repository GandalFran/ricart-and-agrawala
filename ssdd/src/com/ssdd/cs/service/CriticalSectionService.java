package com.ssdd.cs.service;


import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
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

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionService.class);
 
	/**
     * barrier to wait all nodes to be suscribed
     * */
	private CyclicBarrier startBarrier;

	/**
     * barrier to wait all nodes to finish
     * */
	private CyclicBarrier finishBarrier;
    /**
     * associates the state of a subscribed node to it's id, and stores the state relative to
     * the node's critical section state
     * */
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
		return String.format("http://%s:8080/ssdd/cs", host);
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
	
	/**
	 * restarts the service cleaning all node's state structures and
	 * receives the number of nodes that will try to access the critical section.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param numNodes the number of nodes that will try to access to critial section
	 * */
	@GET
	@Path("/restart")
	public void restart(@QueryParam(value="numNodes") int numNodes){
		LOGGER.log(Level.INFO, String.format("/cs/restart"));
		this.nodes.clear();
		this.startBarrier = new CyclicBarrier(numNodes);
		this.finishBarrier = new CyclicBarrier(numNodes);
	}

	/**
	 * waits untill all nodes are ready
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * */
	@GET
	@Path("/ready")
	public void ready(){
		LOGGER.log(Level.INFO, String.format("/cs/ready"));
		try {
			this.startBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			LOGGER.log(Level.WARNING, String.format("/cs/ready ERROR: %s", e.getMessage()), e);
		}
	}
	
	/**
	 * waits untill all nodes has finished
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * */
	@GET
	@Path("/finished")
	public void finished(){
		LOGGER.log(Level.INFO, String.format("/cs/finished"));
		try {
			this.finishBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			LOGGER.log(Level.WARNING, String.format("/cs/finished ERROR: %s", e.getMessage()), e);
		}
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
	@GET
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
	 * set new state for the critical section on a concrete node.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be a suscribed node.
	 * @param newState the new state given to the critical section by the node. Must be a String serialized {@link com.ssdd.cs.bean.CriticalSectionState}
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to current service.
	 * */
	@GET
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
	 * get a node's current lamport counter value, to use it as a timestamp on a message
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be a suscribed node
	 * 
	 * @return requested node's current lamport counter value
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to current service
	 * */
	@GET
	@Path("/get/messagetimestamp")
	@Produces(MediaType.TEXT_PLAIN)
	public long getMessageTimeStamp(@QueryParam(value="node") String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/get/messagetimestamp", nodeId));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/set/messagetimestamp: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = this.nodes.get(nodeId);
		
		// save and obtain counter value
		long timeStamp = node.saveLastTimeStamp();
		
		return timeStamp;
	}

	/**
	 * updates a node's lamport counter value in one unit with the LC1 formule
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be a suscribed node
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to current service
	 * */
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
	 * processes the requests to the critical section access, send by other nodes
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node that will be asked to access the critical section. Must be suscribed to requested service.
	 * @param sender the id of the node trying to accces the critical section.
	 * @param messageTimeStamp the message's timestamp (node's lamport time counter value)
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to current service
	 * */
	@GET
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
			node.queueAccessRequest();
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
	 * used by suscribed nodes to to release the critical section
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to accces the critical section. Must be suscribed to requested service
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to current service
	 * */
	@GET
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
		node.dequeueAcessRequest();

		// unlock operations
		node.unlock();
	}
	
	/**
	 * locks the operations over a suscribed node
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to lock. Must be suscribed to requested service
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to current service
	 * */
	@GET
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
	
	/**
	 * unlocks the operations over a suscribed node
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node trying to lock. Must be suscribed to requested service
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to current service
	 * */
	@GET
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
}
