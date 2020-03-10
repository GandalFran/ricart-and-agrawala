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
import com.ssdd.cs.bean.CriticalSectionMessageType;
import com.ssdd.cs.bean.CriticalSectionState;
import com.ssdd.cs.bean.LamportCounter;
import com.ssdd.util.logging.SSDDLogFactory;

@Singleton
@Path("/cs")
public class CriticalSectionService{

    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionService.class);
	
	private Semaphore accessSemaphore; // TODO: solo provisional, es por si da problemas de concurrencia, para regular el acceso a los elementos de la clase
	private Map<String, LamportCounter> lamportTime;
	private Map<String, CriticalSectionState> state;
	private Map<String, Queue<CriticalSectionMessage>> accessRequests;
	private Map<String, Semaphore> waitToCompleteResponsesSemaphore;
	
	public CriticalSectionService() {
		this.accessSemaphore = new Semaphore(1);
		this.lamportTime = new ConcurrentHashMap<String, LamportCounter>();
		this.state = new ConcurrentHashMap<String, CriticalSectionState>();
		this.accessRequests = new ConcurrentHashMap<String, Queue<CriticalSectionMessage>>();
		this.waitToCompleteResponsesSemaphore = new ConcurrentHashMap<String, Semaphore>();
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
	@Path("/subscribe")
	public void subscribe(@QueryParam(value="node") String nodeId){
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/subscribe", nodeId));
		this.lamportTime.put(nodeId, new LamportCounter());
		this.state.put(nodeId, CriticalSectionState.FREE);
		this.accessRequests.put(nodeId, new ConcurrentLinkedQueue<>());
		this.waitToCompleteResponsesSemaphore.put(nodeId, new Semaphore(0));		
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
	@Path("/subscribed")
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
	@Path("/setState")
	public void setCsState(@QueryParam(value="node") String nodeId, @QueryParam(value="state") String newState) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/setState %s", nodeId, newState));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/release: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		//set new state
		CriticalSectionState state = CriticalSectionState.valueOf(newState);
		this.state.put(nodeId, state);
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
	 * 
	 * @return CriticalSectionMessage with the response
	 * */
	@GET
	@Path("/request")
	@Produces(MediaType.APPLICATION_JSON)
	public String requestAccess(@QueryParam(value="node") String nodeId, @QueryParam(value="request") String requestStr) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/requestAccess", nodeId));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/release: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node response
		CriticalSectionMessage response;
		CriticalSectionMessage request = CriticalSectionMessage.fromJson(requestStr);

		// update process time
		this.lamportTime.get(nodeId).update(request.getTime());
		
		// get node state
		LamportCounter lamportTime = this.lamportTime.get(nodeId);
		CriticalSectionState state = this.state.get(nodeId);
		
		if(state == CriticalSectionState.ACQUIRED
				|| (state == CriticalSectionState.REQUESTED && request.hasPriority(nodeId, request.getTime()))) {
			LOGGER.log(Level.INFO, String.format("[node: %s] /cs/requestAccess queued request from %s", nodeId, request.getSenderId()));
			// queue response
			this.accessRequests.get(nodeId).add(request);
			// build response
			response = new CriticalSectionMessage(nodeId, lamportTime, CriticalSectionMessageType.RESPONSE_DELAYED);
		}else {
			LOGGER.log(Level.INFO, String.format("[node: %s] /cs/requestAccess delayed request from %s", nodeId, request.getSenderId()));
			// build alternative response
			response = new CriticalSectionMessage(nodeId, lamportTime, CriticalSectionMessageType.RESPONSE_ALLOW);
		}
		
		return response.toJson();
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
	@Path("/waitToCompleteResponses")
	public void waitToCompleteResponses(@QueryParam(value="node") String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/waitToCompleteResponses", nodeId));
		
		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/release: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		this.waitToCompleteResponsesSemaphore.get(nodeId).acquire();
	}
	
	
	
	/**
	 * processes the delayed responses to the critical seciton access send by other nodes. Only can be called by a suscribed node.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node for which the delayed answer is directed. Must be a suscribed node.
	 * 
	 * @throws NodeNotFoundException when then nodeId doesn't corresponds to any node suscribed to current service.
	 * */
	@POST
	@Path("/response")
	public void treatDelayedresponses(@QueryParam(value="node") String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/release", nodeId));

		// check if given nodeId corresponds to a suscribed process
		if(! this.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/release: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		//TODO: esto es un problema porque necesitamos tratar las respuestas retardadas
	}
	
	/**
	 * releases critical section. Only can be called by a suscribed node.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node which is releasing the critical section. Must be a suscribed node.
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
		
		// update process time
		LamportCounter myTime = this.lamportTime.get(nodeId);
		myTime.update();
		
		// set state to released
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/release: changing cs state to FREE", nodeId));
		this.state.put(nodeId, CriticalSectionState.FREE);
		
		// answer to responses
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/release: replying request", nodeId));
		for(CriticalSectionMessage r : this.accessRequests.get(nodeId)) {
			//TODO: responder a nodo
			CriticalSectionMessage response = new CriticalSectionMessage(nodeId, myTime, CriticalSectionMessageType.RESPONSE_ALLOW);
			// TODO: responder con proxy (de este servicio con direccion a otro servicio) a servidor que lo envio
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


}
