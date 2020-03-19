package com.ssdd.cs.bean;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.util.logging.SSDDLogFactory;

/** 
 * stores all the relative information to a node's in the critical section context in the server side.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 */
public class CritialSectionServiceNode {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(CritialSectionServiceNode.class);
    

    /**
     * the node's id
     * */
    private String id;
    /**
     * node's time counter based in the Lamport's counter (gided by events)
     * */
	private LamportCounter counter;
    /**
     * node's timestamp (lamport counter value) for the last message
     * */
    private long lastTimeStamp;
    /**
     * critical section's state in the node's context
     * */
	private CriticalSectionState state;
    /**
     * object to notify that critical section has been released in the queued nodes by the algorithm of Ricart and Argawala
     * */
	private Object releaseNotifier;
    /**
     * to lock operations on the node
     * */
	private Semaphore lock;
	
	public CritialSectionServiceNode(String id, LamportCounter counter, CriticalSectionState state) {
		super();
		this.id = id;
		this.counter = counter;
		this.state = state;
		this.lastTimeStamp = 0;
		this.releaseNotifier = new Object();
		this.lock = new Semaphore(1);
	}

	/** 
	 * when a request is received, decides if the node has access to critical section with the 
	 * criteria set on the Ricart and Argawala's algorithm.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param senderId the node's id of the request's sender
	 * @param senderTimeStamp the lamport counter value of the request's sender at the moment of the build of request
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
					return (this.id.compareTo(senderId) > 0);
				}else {
					return (this.lastTimeStamp > senderTimeStamp);
				}
			default:
				// NOTE: this won't never be reached by the program, but is neccesary to shut up the eclipse warnings
				return false;
		}
	}
	
	/** 
	 * stores the node's current counter value, to be used as last message timestamp
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return the node's current counter value, to be used as message timestamp
	 */
	public long saveLastTimeStamp() {
		this.lastTimeStamp = this.counter.getCounter();
		return lastTimeStamp;
	}
	
	/** 
	 * locks relative operations on the node in the critical section
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 */
	public void lock() {
		try {
			this.lock.acquire();
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, "lock: ERROR when waiting");
		}
	}
	
	/** 
	 * unlocks relative operations on the node in the critical section
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 */
	public void unlock() {
		this.lock.release();
	}
	
	/** 
	 * blocks the thread until the node releases the critical section
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 */
	public void queueAccessRequest() {
		
		synchronized(this.releaseNotifier) {
			try {
				this.releaseNotifier.wait();
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, String.format("waitToReleaseCriticalSection: ERROR when waiting %s", e.toString()), e);
			}
		}
	}
	
	/** 
	 * allows the blocked nodes in the {@link #queueAccessRequest()} to continue when the node releases the critical section
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 */
	public void dequeueAcessRequest() {
		synchronized(this.releaseNotifier) {
			this.releaseNotifier.notifyAll();
		}
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
	public Object getReleaseNotifier() {
		return releaseNotifier;
	}
	public void setReleaseNotifier(Object releaseNotifier) {
		this.releaseNotifier = releaseNotifier;
	}

	public long getLastTimeStamp() {
		return lastTimeStamp;
	}

	
}
