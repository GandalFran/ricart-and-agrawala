package services.criticalsection;


import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;



public class CriticalSection {

    private final static Logger LOGGER = LoggerFactory.getLogger(CriticalSection.class);
	
	private Semaphore accessSemaphore;
	private Map<String, Long> lamportTime;
	private Map<String, CriticalSectionState> state;
	private Map<String, Queue<CriticalSectionMessage>> accessRequests;
	
	public CriticalSection() {
		LOGGER.debug("Creating critical section");
		this.accessSemaphore = new Semaphore(1);
		this.lamportTime = new ConcurrentHashMap<String, Long>();
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
		this.lamportTime.put(nodeId, new Long(0));
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

		// get node state
		long lamportTime = this.lamportTime.get(nodeId);
		CriticalSectionState state = this.state.get(nodeId);
		
		if(state == CriticalSectionState.ACQUIRED 
				|| (state == CriticalSectionState.REQUESTED && r.hasPriority(nodeId, r.getLamportTime()))) {
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
		
		LOGGER.debug("["+nodeId+"] /cs/release: changing state");
		this.state.put(nodeId, CriticalSectionState.FREE);
		LOGGER.debug("["+nodeId+"] /cs/release: replying requests");
		for(CriticalSectionMessage r : this.accessRequests.get(nodeId)) {
			//TODO: responder a nodo
			response = new CriticalSectionMessage(nodeId, lamportTime, CriticalSectionMessageType.RESPONSE_ALLOW);
		}
	}
	
	private void updateLamportTime(nodeId) {
		
	}
	
	private void updateLamportTime(String nodeId, long messageLamportTime) {
		
	}

}
