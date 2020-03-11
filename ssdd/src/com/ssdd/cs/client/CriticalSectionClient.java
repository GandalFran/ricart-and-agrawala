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
		LOGGER.log(Level.INFO, String.format("[node %s] acquiring", this.ID));
		try {
			
			this.router.route(this.ID).lock(this.ID);
			
			// get my lamport counter
			String lamportStr = this.router.route(this.ID).getLamport(this.ID);
			LamportCounter c = LamportCounter.fromJson(lamportStr);
					
			// set cs state
			this.router.route(this.ID).setCsState(this.ID, CriticalSectionState.REQUESTED.toString());
			
			this.router.route(this.ID).unlock(this.ID);
			
			// send requests
			for(String node : this.nodes) {
				CriticalSectionService service = this.router.route(node);
				service.request(node, this.ID, c.getCounter());
			}
			this.router.route(this.ID).setCsState(this.ID, CriticalSectionState.ACQUIRED.toString());
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		LOGGER.log(Level.INFO, String.format("[node %s] releasing", this.ID));
		try {
			this.router.route(this.ID).lock(this.ID);
			this.router.route(this.ID).setCsState(this.ID, CriticalSectionState.FREE.toString());
			this.router.route(this.ID).release(this.ID);
			this.router.route(this.ID).unlock(this.ID);
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
