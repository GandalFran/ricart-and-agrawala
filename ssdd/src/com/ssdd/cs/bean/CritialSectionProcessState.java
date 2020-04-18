package com.ssdd.cs.bean;

import java.util.concurrent.Semaphore;

/** 
 * stores all the relative information to a process's in the critical section context in the server side.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 */
public class CritialSectionProcessState {

	/**
     * the process's id
     * */
    private String id;
    /**
     * process's time counter based in the Lamport's counter (guided by events)
     * */
	private LamportCounter counter;
    /**
     * process's timestamp (Lamport counter value) for the last message
     * */
    private long lastTimeStamp;
    /**
     * critical section's state in the process's context
     * */
	private CriticalSectionState state;
    /**
     * to lock operations on the process
     * */
	private Semaphore lock;
    /**
     * to manage process trying to enter critical section queue
     * */
	private CriticalSectionQueue queue;
	
	public CritialSectionProcessState(String id, LamportCounter counter, CriticalSectionState state) {
		super();
		this.id = id;
		this.counter = counter;
		this.state = state;
		this.lastTimeStamp = 0;
		
		this.lock = new Semaphore(1);
		this.queue = new CriticalSectionQueue();
	}

	/** 
	 * when a request is received, decides if the process has access to critical section with the 
	 * criteria set on the Ricart and Argawala's algorithm.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param senderId the process's id of the request's sender
	 * @param senderTimeStamp the Lamport counter value of the request's sender at the moment of the build of request
	 * 
	 * @return true if the request is accepted and false if the request should be queued
	 */
	public boolean permitEnter(String senderId, long senderTimeStamp) {
		switch(this.state) {
			case FREE:
				return true;
			case ACQUIRED:
				return false;
			case REQUESTED:
				if(this.lastTimeStamp == senderTimeStamp) {
					return (senderId.compareTo(this.id) < 0);
				}else {
					return (senderTimeStamp < this.lastTimeStamp);
				}
			default:
				// NOTE: this won't never be reached by the program, but is neccesary to shut up the eclipse warnings
				return false;
		}
	}
	
	/** 
	 * stores the process's current counter value, to be used as last message timestamp
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return the process's current counter value, to be used as message timestamp
	 */
	public long saveLastTimeStamp() {
		this.lastTimeStamp = this.counter.getCounter();
		return this.lastTimeStamp;
	}
	
	/** 
	 * locks operations relative to process in the critical section
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 */
	public void lock() {
		try {
			this.lock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * unlocks operations relative to process in the critical section
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 */
	public void unlock() {
		this.lock.release();
	}
	
	public LamportCounter getCounter() {
		return counter;
	}
	public void setCounter(LamportCounter counter) {
		this.counter = counter;
	}
	public CriticalSectionState getState() {
		return state;
	}
	public void setState(CriticalSectionState state) {
		this.state = state;
	}

	public long getLastTimeStamp() {
		return lastTimeStamp;
	}

	public CriticalSectionQueue getQueue() {
		return queue;
	}

	public void setQueue(CriticalSectionQueue queue) {
		this.queue = queue;
	}	
}
