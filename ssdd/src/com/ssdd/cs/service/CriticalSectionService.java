package com.ssdd.cs.service;


import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.ssdd.cs.bean.CritialSectionProcessState;
import com.ssdd.cs.bean.CriticalSectionState;
import com.ssdd.cs.bean.LamportCounter;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

/**
 * Service to regulate the access to critical section.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos 
 * */
@Singleton
@Path("/cs")
public class CriticalSectionService{

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionService.class);
 
	/**
     * barrier to wait all processes to be suscribed
     * */
	private CyclicBarrier startBarrier;

	/**
     * barrier to wait all processes to finish
     * */
	private CyclicBarrier finishBarrier;
    /**
     * associates the state of a subscribed process to it's id, and stores the state relative to
     * the process's critical section state
     * */
	private Map<String, CritialSectionProcessState> processes;
	
	public CriticalSectionService() {
		this.processes = new ConcurrentHashMap<String, CritialSectionProcessState>();
	}

	/**
	 * factory method, to build a proxy to access an instance of this service in remote.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param host the IP adress and PORT of server in which the service is allocated.
	 * 
	 * @return CriticalSectionService to serve as proxy for the /cs service, served in the given host
	 * */
	public static CriticalSectionService buildProxy(String host) {
		String serviceUri = CriticalSectionService.buildServiceUri(host);
		return new CriticalSectionServiceProxy(serviceUri);
	}

	/**
	 * factory method, to build a URI for a CriticalSectionService from the host IP and port.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param host the IP adress and PORT of server in which the service is allocated.
	 * 
	 * @return String containing the URI to the service, served in the given host
	 * */
	public static String buildServiceUri(String host) {
		return String.format(IConstants.BASE_URI + "/cs", host);
	}
	
	/**
	 * shows service status.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return string indicating the status of the service is up.
	 * */
	@GET
	@Path("/status")
	@Produces(MediaType.TEXT_PLAIN)
	public String status() {
		this.setThreadName();
		return "{ \"service\": \"cs\", \"status\": \"ok\"}";
	}
	
	/**
	 * restarts the service cleaning all process's state structures and
	 * receives the number of processes that will try to access the critical section.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param numProcesses the number of processes that will try to access to critial section
	 * */
	@GET
	@Path("/restart")
	public void restart(@QueryParam(value="numProcesses") int numProcesses){
		this.setThreadName();
		LOGGER.log(Level.INFO, String.format("/cs/restart"));
		this.processes.clear();
		this.startBarrier = new CyclicBarrier(numProcesses);
		this.finishBarrier = new CyclicBarrier(numProcesses);
	}

	/**
	 * waits untill all processes are ready
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * */
	@GET
	@Path("/ready")
	public void ready(){
		this.setThreadName();
		LOGGER.log(Level.INFO, String.format("/cs/ready"));
		try {
			this.startBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			LOGGER.log(Level.WARNING, String.format("/cs/ready ERROR: %s", e.getMessage()), e);
		}
	}
	
	/**
	 * waits untill all processes has finished
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * */
	@GET
	@Path("/finished")
	public void finished(){
		this.setThreadName();
		LOGGER.log(Level.INFO, String.format("/cs/finished"));
		try {
			this.finishBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			LOGGER.log(Level.WARNING, String.format("/cs/finished ERROR: %s", e.getMessage()), e);
		}
	}
	
	/**
	 * registers a process, and create the neccesary structures to work with it.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param processId the id of the process who wants to suscribe to current service.
	 * */
	@GET
	@Path("/suscribe")
	public void suscribe(@QueryParam(value="process") String processId){
		this.setThreadName(processId);
		LOGGER.log(Level.INFO, String.format("[process: %s] /cs/suscribe", processId));
		CritialSectionProcessState process = new CritialSectionProcessState(processId, new LamportCounter(), CriticalSectionState.FREE);
		this.processes.put(processId, process);
	}

	/**
	 * return a JSON serialized list of suscribed processes.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return String containing the list of suscribed processes separated with the '_character'.
	  */
	@GET
	@Path("/suscribed")
	@Produces(MediaType.APPLICATION_JSON)
	public String suscribed(){
		this.setThreadName();
		LOGGER.log(Level.INFO, String.format("/cs/suscribed"));

		// get processes
		String[] suscribedProcesses = new String[this.processes.keySet().size()];
		this.processes.keySet().toArray(suscribedProcesses);
		
		// serialize list of processes to JSON
		String response = new Gson().toJson(suscribedProcesses);
		return response;
	}
	
	/**
	 * used by processes to notify to its associated service that the critical section is requested
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param processId the id of the sender. Must be suscribed to current service.
	 * 
	 * @return the message timestamp for the client
	 * 
	 * @throws ProcessNotFoundException when then processId doesn't corresponds to any process suscribed to current service
	 * */
	@GET
	@Path("/set/requested")
	@Produces(MediaType.TEXT_PLAIN)
	public long setRequested(@QueryParam(value="process") String processId) throws ProcessNotFoundException {
		this.setThreadName(processId);
		LOGGER.log(Level.INFO, String.format("[process: %s] /cs/set/requested", processId));
		
		// get process
		CritialSectionProcessState process = this.getProcessState(processId);
		// lock operations
		process.lock();
		// get process' lamport time and update process's critical section state
		long messageTimeStamp = process.saveLastTimeStamp();
		process.setState(CriticalSectionState.REQUESTED);
		// enable queueing
		process.getQueue().activate();
		// unlock operations
		process.unlock();
		// return the timestamp to process, for sending the requests
		return messageTimeStamp;
	}
	
	/**
	 * processes the requests to the critical section access, send by other processes
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param processId the id of the process that will be asked to access the critical section. Must be suscribed to requested service.
	 * @param sender the id of the process trying to accces the critical section.
	 * @param messageTimeStamp the message's timestamp (process's lamport time counter value)
	 * 
	 * @throws ProcessNotFoundException when then processId doesn't corresponds to any process suscribed to current service
	 * */
	@GET
	@Path("/request")
	public void request(@QueryParam(value="process") String processId, @QueryParam(value="sender") String sender, @QueryParam(value="messageTimeStamp") long messageTimeStamp) throws ProcessNotFoundException {
		this.setThreadName(processId);

		// get process
		CritialSectionProcessState process = this.getProcessState(processId);
		// lock operations
		process.lock();
		// update local lamport time
		process.getCounter().update(messageTimeStamp);
		// check if the enter of process is permited or not
		boolean permitEnter = process.permitEnter(sender, messageTimeStamp);
		LOGGER.log(Level.INFO, String.format("[process: %s] /cs/request process %s %s", processId, sender,( permitEnter ? "ALLOWED" : "QUEUED" )));
		// unlock operations
		process.unlock();
		
		if(!permitEnter){
			// wait until the enter in CS is permited
			process.getQueue().waitInQueue();
			LOGGER.log(Level.INFO, String.format("[process: %s] /cs/request process %s DEQUEUED (ALLOWED)", processId, sender));
		}
	}

	/**
	 * used by processes to notify to its associated service that the critical section is acquired
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param processId the id of the sender. Must be suscribed to current service.
	 * 
	 * @throws ProcessNotFoundException when then processId doesn't corresponds to any process suscribed to current service
	 * */
	@GET
	@Path("/set/acquired")
	public void setAcquired(@QueryParam(value="process") String processId) throws ProcessNotFoundException {
		this.setThreadName(processId);
		LOGGER.log(Level.INFO, String.format("[process: %s] /cs/set/acquired", processId));
		// get process
		CritialSectionProcessState process = this.getProcessState(processId);
		// lock operations
		process.lock();
		// get process' lamport time and update process's critical section state
		process.getCounter().update();
		process.setState(CriticalSectionState.ACQUIRED);
		// unlock operations
		process.unlock();
	}

	/**
	 * used by suscribed processes to to release the critical section
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param processId the id of the process trying to accces the critical section. Must be suscribed to requested service
	 * 
	 * @throws ProcessNotFoundException when then processId doesn't corresponds to any process suscribed to current service
	 * */
	@GET
	@Path("/release")
	public void release(@QueryParam(value="process") String processId) throws ProcessNotFoundException {
		this.setThreadName(processId);
		LOGGER.log(Level.INFO, String.format("[process: %s] /cs/release", processId));
		
		// get process
		CritialSectionProcessState process = this.getProcessState(processId);
		// lock operations
		process.lock();
		// set new state
		process.setState(CriticalSectionState.FREE);
		// release queued processes
		process.getQueue().deactivateAndRelease();
		// unlock operations
		process.unlock();
	}
	
	/**
	 * given a processId checks if is suscribed to current service instance and returns the
	 * status of the process in the critical section
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param processId id of the process we want to check if you have subscribed to this service.
	 * 
	 * @return the status of the process in the system
	 * 
	 * @throws ProcessNotFoundException when then processId doesn't corresponds to any process suscribed to current service
	 * */
	private CritialSectionProcessState getProcessState(String processId) throws ProcessNotFoundException{
		// check if given processId corresponds to a suscribed process
		if(! this.processes.keySet().contains(processId)) {
			LOGGER.log(Level.WARNING, String.format("ERROR the given process is not subscribed %s", processId));
			throw new ProcessNotFoundException(processId);
		}
		return this.processes.get(processId);
	}
	
	private void setThreadName(){
		Thread.currentThread().setName(String.format("CS"));
	}
	
	private void setThreadName(String p){
		Thread.currentThread().setName(String.format("CS.%s",p));
	}
}
