package com.ssdd.cs.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.util.logging.SSDDLogFactory;

/** 
 * manages the critical section queue in a syncrhonized way.
 * 
 * @version 1.0
 * @author H�ctor S�nchez San Blas
 * @author Francisco Pinto Santos
 */
public class CriticalSectionQueue {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionQueue.class);
 
	/**
     * flag that indicates that the queuing is allowed
     * */
	private boolean queueingAllowed;
	/**
     * queue of semaphores where the threads are waiting
     * */
	private List<Semaphore> waitingProcesses;
	
	public CriticalSectionQueue(){
		this.queueingAllowed = false;
		this.waitingProcesses = new ArrayList<>();
	}
	
	/** 
	 * enables the queuing, allowing processes to wait in the critical section in the {@link #waitInQueue()} method.
	 * 
	 * @version 1.0
	 * @author H�ctor S�nchez San Blas
	 * @author Francisco Pinto Santos
	 */
	public synchronized void activate(){
		this.queueingAllowed = true;
	}

	/** 
	 * disables the queuing, forbidding processes to wait in the critical section in the {@link #waitInQueue()} method.
	 * Also releases all processes waiting in {@link #waitInQueue()}, making a release over every  {@link java.util.concurrent.Semaphore},
	 * and cleans the {@link #waitingProcesses} list.
	 * 
	 * @version 1.0
	 * @author H�ctor S�nchez San Blas
	 * @author Francisco Pinto Santos
	 */
	public synchronized void deactivateAndRelease(){
		this.waitingProcesses.forEach(s -> s.release());
		this.waitingProcesses.clear();
		this.queueingAllowed = false;
	}	
	
	/** 
	 * synchronizedly checks if the queueing is allowed. In that case instances a  {@link java.util.concurrent.Semaphore} with 0
	 * permits and pushes it into the {@link #waitingProcesses} list. Then goes out the syncrhonyzed code fragment and waits for the semaphore.
	 * In the case that is not allowed, the semaphore is not instanced, and in the not synchronized part of the method, is not waited for.
	 * 
	 * @version 1.0
	 * @author H�ctor S�nchez San Blas
	 * @author Francisco Pinto Santos
	 */
	
	public void waitInQueue(){
		Semaphore s = null;
		synchronized(this){
			if(queueingAllowed){
				s = new Semaphore(0);
				this.waitingProcesses.add(s);
			}
		}
		if(null != s){
			try {
				s.acquire();
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, String.format("error waiting in queue"), e);
			}
		}
	}
}
