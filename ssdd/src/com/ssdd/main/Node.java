package com.ssdd.main;

import java.util.Map;
import java.util.Random;

import com.ssdd.cs.client.CriticalSectionClient;
import com.ssdd.ntp.bean.Pair;
import com.ssdd.ntp.client.NTPClient;
import com.ssdd.ntp.service.NTPService;
import com.ssdd.util.Utils;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Node extends Thread{

    private final static Logger LOGGER = SSDDLogFactory.logger(Node.class);
    
	private String nodeId;
	private Random generator;
	
	private NTPClient ntp;
	private CriticalSectionClient cs;
    private CriticalSectionLog csLog;
	
	public Node(String nodeId, NTPClient ntp, CriticalSectionClient cs) {
		super();
		this.cs = cs;
		this.ntp = ntp;
		this.nodeId = nodeId;
		this.generator = new Random();
		this.csLog = new CriticalSectionLog(nodeId);
	}
	
	public void run() {
		// calculate the ntp delay and offset at the begginng
		LOGGER.log(Level.INFO, String.format("[node: %s] ntp initial", nodeId));
		Map<NTPService, Pair> ntpInitialResult = ntp.estimate();
		LOGGER.log(Level.INFO, String.format("[node: %s] ntp initial result: %s", nodeId, ntpInitialResult.toString()));
		
		// iterate N times simulating calculus and entering in the critical section
		for(int i=0; i< IConstants.SIMULATION_NUM_ITERATIONS; i++) {
			LOGGER.log(Level.INFO, String.format("[node: %s] iter %d", nodeId, i));
			LOGGER.log(Level.INFO, String.format("[node: %s] iter %d simulating calculus", nodeId, i));
			this.simulateSleep(IConstants.SIMULATION_MIN_CALULUS_TIME, IConstants.SIMULATION_MAX_CALULUS_TIME);
			LOGGER.log(Level.INFO, String.format("[node: %s] iter %d entering critical section", nodeId, i));
			this.enterCriticalSection();
		}

		// calculate the ntp delay and offset at the end
		LOGGER.log(Level.INFO, String.format("[node: %s] ntp final", nodeId));
		Map<NTPService, Pair> ntpFinalResult = ntp.estimate();
		LOGGER.log(Level.INFO, String.format("[node: %s] ntp final result: %s", nodeId, ntpFinalResult.toString()));
		

		LOGGER.log(Level.INFO, String.format("[node: %s] finished", nodeId));
	}
	

	private void enterCriticalSection() {	
		// acquire critical section
		cs.acquire();
		
		// log to file when entered in critical section
		csLog.logIn();
		
		LOGGER.log(Level.INFO, String.format("[node: %s] simulating calculus in critical section", nodeId));
		this.simulateSleep(IConstants.SIMULATION_MIN_CRITICAL_SECTION_TIME, IConstants.SIMULATION_MAX_CRITICAL_SECTION_TIME);

		// log to file when exited from critical section
		csLog.logOut();
		
		// release critical section
		cs.release();
		
	}
	
	private void simulateSleep(long min, long max) {
		// calculate the sleep interval
		long sleepIntervalMs = Utils.randomBetweenInterval(this.generator, min, max);
		
		// sleep to simulate calculus
		LOGGER.log(Level.INFO, String.format("[node: %s] simulating sleep of %d ms", nodeId, sleepIntervalMs));
		try {
			Thread.sleep(sleepIntervalMs);
		} catch (InterruptedException e) {
			LOGGER.log(Level.INFO, String.format("[node: %s] simulateSleep: error %s", nodeId, e.getMessage()), e);
		}
	}
	
}
