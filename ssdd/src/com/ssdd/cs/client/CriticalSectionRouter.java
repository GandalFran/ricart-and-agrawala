package com.ssdd.cs.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.cs.service.CriticalSectionServiceProxy;

/** 
 * Provides access to a node by it's associated broker.
 * If the node is not found, the class will request all available brokers
 * for the suscribed nodes, to know which service is the one associated with
 * the requested node.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class CriticalSectionRouter {
	
	/**
	 * List of availabe services to request the suscribed nodes
	 * */
	private List<CriticalSectionService> brokers;
	/**
	 * Cached asociation between node and broker
	 * */
	private Map<String, CriticalSectionService> router;

	public CriticalSectionRouter() {
		this.router = new HashMap<>();
		this.brokers = new ArrayList<>();
	}
	
	public CriticalSectionRouter(String [] nodes, CriticalSectionService [] services) {
		this.router = new HashMap<>();
		this.brokers = Arrays.asList(services);
		// store the nodes as keys on the map
		for(String node : nodes)
			router.put(node, null);
	}
	

	/** 
	 * Given a node, provides its associated service.
	 * If the node is not found, it will request all available brokers
	 * for the suscribed nodes, to know which service is the one associated with
	 * the requested node.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId the requested node's id
	 * 
	 * @return the node's associated {@link com.ssdd.cs.service.CriticalSectionService}, to which it is subscribed
	*/
	public CriticalSectionService route(String nodeId) {
		// if the requested node is not registered in router, update it
		if(this.router.get(nodeId) == null) {
			this.updateAll();
		}
		return this.router.get(nodeId);
	}
	
	/** 
	 * Updates the local association information with a new association between a node and a service.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param nodeId a node's id
	 * @param service the node's associated broker
	*/
	public void update(String nodeId, CriticalSectionService service) {
		this.router.put(nodeId, service);
	}
	
	/** 
	 * Requests all brokers for all suscribed nodes, and updates the local association information.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void updateAll() {
		for(CriticalSectionService broker : this.brokers) {
			String response = broker.suscribed();
			String [] nodes = CriticalSectionServiceProxy.parseSuscribedResponse(response);
			for(String node : nodes) {
				this.update(node, broker);
			}
		}
	}

	public List<String> getNodes(){
		return new ArrayList<>(this.router.keySet());
	}
	
	public List<CriticalSectionService> getBrokers() {
		return brokers;
	}

	public void setBrokers(List<CriticalSectionService> brokers) {
		this.brokers = brokers;
	}

	public Map<String, CriticalSectionService> getRouter() {
		return router;
	}

	public void setRouter(Map<String, CriticalSectionService> router) {
		this.router = router;
	}
	
}
