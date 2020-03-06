package com.ssdd.cs.service;


import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import com.ssdd.cs.bean.CriticalSectionMessage;
import com.ssdd.cs.bean.CriticalSectionMessageType;
import com.ssdd.cs.bean.CriticalSectionState;
import com.ssdd.cs.bean.LamportCounter;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;



public class CriticalSection {

    private final static Logger LOGGER = LoggerFactory.getLogger(CriticalSection.class);
	
	private Semaphore accessSemaphore;
	private Map<String, LamportCounter> lamportTime;
	private Map<String, CriticalSectionState> state;
	private Map<String, Queue<CriticalSectionMessage>> accessRequests;
	
	public CriticalSection() {
		LOGGER.debug("Creating critical section");
		this.accessSemaphore = new Semaphore(1);
		this.lamportTime = new ConcurrentHashMap<String, LamportCounter>();
		this.state = new ConcurrentHashMap<String, CriticalSectionState>();
		this.accessRequests = new ConcurrentHashMap<String, Queue<CriticalSectionMessage>>();
	}
	
	/**
	 * Factory method, to build a proxy to access an instance of this service in remote.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node who send the request.
	 * */
	public static CriticalSection buildProxy(String serviceUri) {
		return new CriticalSectionProxy(serviceUri);
	}

	/**
	 * Register a node, and create the neccesary structures to work with it.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node who send the request.
	 * */
	public void subscribe(String nodeId){
		LOGGER.debug("[" + nodeId + "] /cs/subscribe");
		this.lamportTime.put(nodeId, new LamportCounter());
		this.state.put(nodeId, CriticalSectionState.FREE);
		this.accessRequests.put(nodeId, new ConcurrentLinkedQueue<>());
	}
	
	/**
	 * Used by local process to request the critical secion access.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node who send the request.
	 * */
	public void acquire(String nodeId) {
		LOGGER.debug("[" + nodeId + "] /cs/acquire");
		
		// update process time
		this.lamportTime.get(nodeId).update();
		
		
	}

	
	/**
	 * Used by not subscribed nodes to request the critical secion access.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * 
	 * @param nodeId the id of the node who send the request.
	 * */
	public String requestAccess(String nodeId, String request) {
		LOGGER.debug("[" + nodeId + "] /cs/requestAccess");
		
		// get node response
		CriticalSectionMessage response;
		CriticalSectionMessage r = CriticalSectionMessage.fromJson(request);

		// update process time
		this.lamportTime.get(nodeId).update(r.getTime());
		
		// get node state
		LamportCounter lamportTime = this.lamportTime.get(nodeId);
		CriticalSectionState state = this.state.get(nodeId);
		
		if(state == CriticalSectionState.ACQUIRED 
				|| (state == CriticalSectionState.REQUESTED && r.hasPriority(nodeId, r.getTime()))) {
			this.accessRequests.get(nodeId).add(r);
			// build response
			response = new CriticalSectionMessage(nodeId, lamportTime, CriticalSectionMessageType.RESPONSE_DELAYED);
		}else {
			//TODO: no se si hay que responder esto
			response = new CriticalSectionMessage(nodeId, lamportTime, CriticalSectionMessageType.RESPONSE_ALLOW);
		}
		
		return response.toJson();
	}
	
	/**
	 * Used by a subscribed node to receive delayed responses
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * */
	public String treatDelayedresponses() {
		//TODO: esto es un problema porque necesitamos tratar las respuestas retardadas
	}
	
	/**
	 * Used by a subscribed node to release access to critical section.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * */
	public void release(String nodeId) {
		LOGGER.debug("[" + nodeId + "] /cs/release");
		
		// update process time
		this.lamportTime.get(nodeId).update();
		
		// set state to released
		LOGGER.debug("["+nodeId+"] /cs/release: changing state");
		this.state.put(nodeId, CriticalSectionState.FREE);
		
		// answer to responses
		LOGGER.debug("["+nodeId+"] /cs/release: replying requests");
		for(CriticalSectionMessage r : this.accessRequests.get(nodeId)) {
			//TODO: responder a nodo
			response = new CriticalSectionMessage(nodeId, lamportTime, CriticalSectionMessageType.RESPONSE_ALLOW);
			// TODO: responder con proxy a servidor que lo envio
		}
	}
	

}
