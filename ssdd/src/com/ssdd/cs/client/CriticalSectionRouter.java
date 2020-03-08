package com.ssdd.cs.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.cs.service.CriticalSectionServiceProxy;

public class CriticalSectionRouter {
	
	private List<CriticalSectionService> brokers;
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
	
	public CriticalSectionService route(String nodeId) {
		// if the requested node is not registered in router, update it
		if(this.router.get(nodeId) == null) {
			this.updateAll();
		}
		return this.router.get(nodeId);
	}
	
	public void update(String nodeId, CriticalSectionService service) {
		this.router.put(nodeId, service);
	}
	
	public void updateAll() {
		for(CriticalSectionService broker : this.brokers) {
			String response = broker.suscribed();
			String [] nodes = CriticalSectionServiceProxy.parseSuscribedResponse(response);
			for(String node :nodes) {
				this.update(node, broker);
			}
		}
	}
	
}
