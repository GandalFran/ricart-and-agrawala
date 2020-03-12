package com.ssdd.cs.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.bean.CriticalSectionState;
import com.ssdd.cs.bean.LamportCounter;
import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.cs.service.NodeNotFoundException;
import com.ssdd.util.logging.SSDDLogFactory;

public class CriticalSectionClient {
	
	private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionClient.class);
    
	private String ID;
	private List<String> nodes;
	private CriticalSectionRouter router;
	
	public CriticalSectionClient(String ID, CriticalSectionService selectedBroker, String [] nodes, CriticalSectionService [] services) {
		this.ID = ID;
		this.router = new CriticalSectionRouter(nodes, services);
		this.router.update(ID, selectedBroker);
		this.nodes = this.buildNodeArray(nodes);
	}
	
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
	 * Suscribes this client to a broker
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
	 * PONER DESCRIPCION
	 * 
	 * TODO:
	 * 	- establecer estado a buscada
	 *  - for each node:
	 *  	- hacer request a /request
	 *  	- si respuesta es delayed
	 *  		delayed_responses.add(response)
	 *  - hacer request a /waitToCompleteResponses
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
			
			// get my lamport counter
			String lamportStr = myservice.getLamport(this.ID);
			LamportCounter c = LamportCounter.fromJson(lamportStr);
			
			// send requests and unlock
			SenderPool.send(this.ID, c, this.nodes, router);

			// update lamport counter
			myservice.lock(this.ID);
			myservice.setCsState(this.ID, CriticalSectionState.ACQUIRED.toString());
			myservice.updateLamport(this.ID);
			myservice.unlock(this.ID);
			
		} catch (NodeNotFoundException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] acquire: error %s", this.ID, e.getMessage()), e);
		}
	}
	
	/** 
	 * PONER DESCRIPCION
	 * 
	 * TODO:
	 * 	- establecer estado a libre
	 *  - llamar a /getQueuedRequests
	 *  - for each request
	 *  	- responder a /delayedRequest
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
