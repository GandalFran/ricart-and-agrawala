package com.ssdd.cs.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.cs.service.CriticalSectionServiceProxy;

/** 
 * Provides access to a process by it's associated service.
 * If the process is not found, the class will request all available services
 * for the subscribed processes, to know which service is the one associated with
 * the requested process.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 */
public class CriticalSectionRouter {
	
	/**
	 * List of available services to request the subscribed processes
	 * */
	private List<CriticalSectionService> services;
	/**
	 * Cached association between process and service
	 * */
	private Map<String, CriticalSectionService> router;

	public CriticalSectionRouter() {
		this.router = new HashMap<>();
		this.services = new ArrayList<>();
	}
	
	public CriticalSectionRouter(String [] processes, CriticalSectionService [] services) {
		this.router = new HashMap<>();
		this.services = Arrays.asList(services);
		// store the processes as keys on the map
		for(String process : processes)
			router.put(process, null);
	}

	/** 
	 * given a process, provides its associated service.
	 * If the process is not found, it will request all available services
	 * for the subscribed processes, to know which service is the one associated with
	 * the requested process.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param processId the requested process's id
	 * 
	 * @return the process's associated {@link com.ssdd.cs.service.CriticalSectionService}, to which it is subscribed
	*/
	public CriticalSectionService route(String processId) {
		// if the requested process is not registered in router, update it
		if(this.router.get(processId) == null) {
			this.updateAll();
		}
		return this.router.get(processId);
	}
	
	/** 
	 * updates the local association information with a new association between a process and a service.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param processId a process's id
	 * @param service the process's associated service
	*/
	public void update(String processId, CriticalSectionService service) {
		this.router.put(processId, service);
	}
	
	/** 
	 * requests all services for all subscribed processes, and updates the local association information.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void updateAll() {
		this.services.forEach(service -> 
			{
				String response = service.suscribed();
				String [] processes = CriticalSectionServiceProxy.parseSuscribedResponse(response);
				for(String process : processes) {
					this.update(process, service);
				}
			}
		);
	}

	public List<String> getProcesses(){
		return new ArrayList<>(this.router.keySet());
	}
	
	public List<CriticalSectionService> getServices() {
		return services;
	}

	public void setServices(List<CriticalSectionService> services) {
		this.services = services;
	}

	public Map<String, CriticalSectionService> getRouter() {
		return router;
	}

	public void setRouter(Map<String, CriticalSectionService> router) {
		this.router = router;
	}
	
}
