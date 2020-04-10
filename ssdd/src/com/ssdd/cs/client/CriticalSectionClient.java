package com.ssdd.cs.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.client.senders.CritialSectionFinishedConcurrentSender;
import com.ssdd.cs.client.senders.CritialSectionReadyConcurrentSender;
import com.ssdd.cs.client.senders.CriticalSectionRequestConcurrentSender;
import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.cs.service.ProcessNotFoundException;
import com.ssdd.util.constants.IConstants;
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
	 * client's process id
	 * */
	private String ID;
	/**
	 * the other processes's trying to access critical section, ids
	 * */
	private List<String> processes;
	/**
	 * router to access other processes
	 * */
	private CriticalSectionRouter router;
	
	public CriticalSectionClient(String ID, CriticalSectionService selectedService, String [] processes, CriticalSectionService [] services) {
		this.ID = ID;
		this.router = new CriticalSectionRouter(processes, services);
		this.router.update(ID, selectedService);
		this.processes = this.buildProcessArray(processes);
	}
	
	/** 
	 * builds an array with all processes' ids except the client's process id
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param allProcesses array containing all processes
	 * 
	 * @return a list with all processes' ids except the client's process id
	*/
	private List<String> buildProcessArray(String [] allProcesses) {
		List<String> processes =  new ArrayList<>();
		for(String process : allProcesses) {
			if(!process.equals(this.ID)) {
				processes.add(process);
			}
		}
		return processes;
	}

	/** 
	 * suscribes this client to a service
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void suscribe() {
		CriticalSectionService myService = this.router.route(this.ID);
		LOGGER.log(Level.INFO, String.format("suscribing to %s", myService.toString()));
		myService.suscribe(this.ID);
		LOGGER.log(Level.INFO, String.format("suscribed to %s", myService.toString()));
	}
	
	/** 
	 * indicates the services that the current process is ready and waits until all processes are ready
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void ready() {
		LOGGER.log(Level.INFO, "ready start");
		CritialSectionReadyConcurrentSender multicastSender = new CritialSectionReadyConcurrentSender();
		List<Runnable>tasks = multicastSender.buildCommunicationTasks(this.router.getServices());
		multicastSender.multicastSend(tasks);
		multicastSender.await();
		LOGGER.log(Level.INFO, "ready end");
	}
	
	/** 
	 * indicates the services that the current process has finished and waits untill all processes has finished
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void finished() {
		LOGGER.log(Level.INFO, " finished start");
		CritialSectionFinishedConcurrentSender multicastSender = new CritialSectionFinishedConcurrentSender();
		List<Runnable>tasks = multicastSender.buildCommunicationTasks(this.router.getServices());
		multicastSender.multicastSend(tasks);
		multicastSender.await();
		LOGGER.log(Level.INFO, "finished end");
	}
	
	/** 
	 * acquires the critical section with the Ricart and Argawala's algorithm.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void acquire() {
		LOGGER.log(Level.INFO, "acquire");
		try {
			// get my associated service to send messages
			CriticalSectionService myservice = this.router.route(this.ID);

			// set requested in associated service adn retrieve the message timestamp
			long messageTimeStamp = myservice.setRequested(this.ID);
			
			// send requests
			LOGGER.log(Level.INFO, String.format("requesting access with %d", messageTimeStamp));
			CriticalSectionRequestConcurrentSender multicastSender = new CriticalSectionRequestConcurrentSender();
			List<Runnable>tasks = multicastSender.buildCommunicationTasks(this.ID, this.processes, router, messageTimeStamp);
			multicastSender.multicastSend(tasks);
		
			// wait for responses
			multicastSender.await();
			
			// notify the associated service that critical section is acquired (because all responses has arrived)
			myservice.setAcquired(this.ID);
			
		} catch (ProcessNotFoundException e) {
			LOGGER.log(Level.WARNING, String.format("acquire: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_SIMULATION_ERROR);
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
		LOGGER.log(Level.INFO, "release");
		try {
			// get my associated service to send messages
			CriticalSectionService myservice = this.router.route(this.ID);
			// release the critical section
			myservice.release(this.ID);
		} catch (ProcessNotFoundException e) {
			LOGGER.log(Level.WARNING, String.format("release: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_SIMULATION_ERROR);
		}
	}
	
}
