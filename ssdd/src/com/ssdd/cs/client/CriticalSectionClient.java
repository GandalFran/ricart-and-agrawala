package com.ssdd.cs.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.cs.service.CriticalSectionServiceProxy;
import com.ssdd.util.logging.SSDDLogFactory;

public class CriticalSectionClient {

	private static Logger logger = SSDDLogFactory.logger(CriticalSectionClient.class);

	private List<String> nodes;
	private List<CriticalSectionService> brokers;
	private Map<String, CriticalSectionService> router;
	
	
	public CriticalSectionClient(Map<String, CriticalSectionService> router) {
		super();
		this.router = router;
		this.nodes = new ArrayList<String>(router.values());
		this.brokers = new ArrayList<CriticalSectionService>(router.keySet());
	}
	
	public CriticalSectionClient(String [] nodes, CriticalSectionService [] services) {
		super();
		this.nodes = Arrays.asList(nodes);
		this.brokers = Arrays.asList(services);
		this.router = this.buildRouter(nodes);
	}
	
	public static Map<String, CriticalSectionService> buildRouter(String [] nodes) {
		Map<String, CriticalSectionService> router = new HashMap<>();
		for(String node : nodes)
			router.put(node, null);
		return router;
	}
	
	public void acquire() {
		
	}
	
	public void release() {
		
	}
	
	private CriticalSectionService route(String nodeId) {
		// if the requested node is not registered in router, update it
		if(this.router.get(nodeId) == null) {
			for(CriticalSectionService broker : brokers) {
				String response = broker.suscribed();
				String [] nodes = CriticalSectionServiceProxy.parseSuscribedResponse(response);
				for(String node :nodes) {
					this.router.put(node, broker);
				}
			}	
		}
		
		return this.router.get(nodeId);
	}
	
}
