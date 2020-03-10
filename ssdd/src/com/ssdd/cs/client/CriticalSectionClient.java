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
		this.router.update(ID, selectedBroker);
		this.suscribe();
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
		myService.subscribe(this.ID);
		LOGGER.log(Level.INFO, String.format("Suscribed to %s", myService.toString()));
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
		// Hola amigo, usa loggers y String,format para dejar constancia de cada accion porfa
		// 	luego si es necesario ya quitamos cosas.
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
		// Hola amigo, usa loggers y String,format para dejar constancia de cada accion porfa
		// 	luego si es necesario ya quitamos cosas.
	}
	
}
