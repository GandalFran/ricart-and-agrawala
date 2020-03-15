package com.ssdd.cs.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.bean.CriticalSectionState;
import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.cs.service.NodeNotFoundException;
import com.ssdd.util.logging.SSDDLogFactory;

/**
 * Client for the {@link com.ssdd.cs.service.CriticalSectionService} service
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 * */
public class CriticalSectionClient {
	
	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
	private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionClient.class);
    
	/**
	 * client's node id
	 * */
	private String ID;
	/**
	 * the other node's trying to access critical section, ids
	 * */
	private List<String> nodes;
	/**
	 * router to access other nodes
	 * */
	private CriticalSectionRouter router;
	/**
	 * threadpool to send a multicast access request
	 * */
	private SenderPool multicastSender;
	
	public CriticalSectionClient(String ID, CriticalSectionService selectedBroker, String [] nodes, CriticalSectionService [] services) {
		this.ID = ID;
		this.router = new CriticalSectionRouter(nodes, services);
		this.router.update(ID, selectedBroker);
		this.nodes = this.buildNodeArray(nodes);
		this.multicastSender = new CriticalSectionRequestSenderPool();
	}
	
	/** 
	 * builds an array with all nodes' ids except the client's node id
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param allNodes array containing all nodes
	 * 
	 * @return a list with all nodes' ids except the client's node id
	*/
	private List<String> buildNodeArray(String [] allNodes) {
		List<String> nodes =  new ArrayList<>();
		for(String node : allNodes) {
			if(!node.equals(this.ID)) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	/** 
	 * suscribes this client to a broker
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void suscribe() {
		CriticalSectionService myService = this.router.route(this.ID);
		LOGGER.log(Level.INFO, String.format("[node %s] suscribing to %s", this.ID, myService.toString()));
		myService.suscribe(this.ID);
		LOGGER.log(Level.INFO, String.format("[node %s] suscribed to %s", this.ID, myService.toString()));
	}
	
	/** 
	 * acquires the critical section with the Ricart and Argawala algorithm.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void acquire() {
		LOGGER.log(Level.INFO, String.format("[node %s] acquire", this.ID));
		try {

			CriticalSectionService myservice = this.router.route(this.ID);

			// lock
			myservice.lock(this.ID);
			
			// set cs state
			myservice.setCsState(this.ID, CriticalSectionState.REQUESTED.toString());
			
			// get message timestamp
			long messageTimeStamp = myservice.getMessageTimeStamp(this.ID);
			
			// send requests and unlock
			this.multicastSender.multicastSend(this.ID, this.nodes, router, messageTimeStamp, null);
			myservice.unlock(this.ID);
			
			// wait to requests
			this.multicastSender.await();
			
			myservice.lock(this.ID);
			
			// update lamport counter and critical section state
			myservice.setCsState(this.ID, CriticalSectionState.ACQUIRED.toString());
			myservice.updateCounter(this.ID);
			myservice.unlock(this.ID);
			
		} catch (NodeNotFoundException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] acquire: error %s", this.ID, e.getMessage()), e);
		}
	}
	
	/** 
	 *  releases the critical section with the Ricart and Argawala algorithm.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void release() {
		LOGGER.log(Level.INFO, String.format("[node %s] release", this.ID));
		try {
			CriticalSectionService myservice = this.router.route(this.ID);
			myservice.release(this.ID);
		} catch (NodeNotFoundException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] release: error %s", this.ID, e.getMessage()), e);
		}
	}
	
}
