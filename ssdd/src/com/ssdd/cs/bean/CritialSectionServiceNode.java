package com.ssdd.cs.bean;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.util.logging.SSDDLogFactory;

public class CritialSectionServiceNode {

    private final static Logger LOGGER = SSDDLogFactory.logger(CritialSectionServiceNode.class);
    
    private String id;
	private LamportCounter counter;
	private CriticalSectionState state;
	private Object releaseNotifier;
	private Semaphore lock;
    private long lastTimeStamp;
	
	public CritialSectionServiceNode(String id, LamportCounter counter, CriticalSectionState state) {
		super();
		this.id = id;
		this.counter = counter;
		this.state = state;
		this.lastTimeStamp = 0;
		this.releaseNotifier = new Object();
		this.lock = new Semaphore(1);
	}

	public long saveLastTimeStamp() {
		this.lastTimeStamp = this.counter.getCounter();
		return lastTimeStamp;
	}
	
	public void lock() {
		try {
			this.lock.acquire();
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] lock: ERROR when waiting", this.id));
		}
	}
	
	public void unlock() {
		this.lock.release();
	}
	
	public void waitToReleaseCriticalSection() {
		
		synchronized(this.releaseNotifier) {
			try {
				this.releaseNotifier.wait();
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, String.format("[node: %s] waitToReleaseCriticalSection: ERROR when waiting %s", this.id, e.toString()), e);
			}
		}
	}
	
	public void releaseCriticalSection() {
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
