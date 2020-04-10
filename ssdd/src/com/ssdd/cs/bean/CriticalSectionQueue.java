package com.ssdd.cs.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class CriticalSectionQueue {

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
		for(Semaphore s : this.waitingProcesses)
			s.release();
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
				e.printStackTrace();
			}
		}
	}
}
