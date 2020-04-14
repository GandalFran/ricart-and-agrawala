package com.ssdd.cs.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.util.logging.SSDDLogFactory;

public class CriticalSectionQueue {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionQueue.class);
 
    
	private boolean queueingAllowed;
	private List<Semaphore> waitingProcesses;
	
	public CriticalSectionQueue(){
		this.queueingAllowed = false;
		this.waitingProcesses = new ArrayList<>();
	}
	
	public synchronized void activate(){
		this.queueingAllowed = true;
	}
	
	public synchronized void deactivateAndRelease(){
		this.waitingProcesses.forEach(s -> s.release());
		this.waitingProcesses.clear();
		this.queueingAllowed = false;
	}
	
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
