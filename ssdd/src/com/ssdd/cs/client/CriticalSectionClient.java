package com.ssdd.cs.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.util.logging.SSDDLogFactory;

public class CriticalSectionClient {

	private static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionClient.class);

	private String ID;
	private CriticalSectionRouter router;
	
	
	public CriticalSectionClient(String ID, CriticalSectionService selectedBroker, String [] nodes, CriticalSectionService [] services) {
		super();
		this.ID = ID;
		this.router = new CriticalSectionRouter(nodes, services);
		this.suscribe();
	}

	public void suscribe() {
		CriticalSectionService myService = this.router.route(this.ID);
		myService.subscribe(this.ID);
		LOGGER.log(Level.INFO, "Suscribed to " + myService.toString());
	}
	
	public void acquire() {
		
	}
	
	public void release() {
		
	}
	
}
