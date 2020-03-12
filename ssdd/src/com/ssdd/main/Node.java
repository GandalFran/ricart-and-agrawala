package com.ssdd.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

/** 
 * Behaviour of a process in critical section test simulation.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class Node extends Thread{

    private final static Logger LOGGER = SSDDLogFactory.logger(Node.class);
    
    /** 
     * the node's id
    */
	private String nodeId;    
	/** 
     * random number generator
    */
	private Random generator;
    
	/** 
     * client to access the system's private NTP service.
    */
	private NTPClient ntp;	
	/** 
     * client to access the system's private critical section service.
    */
	private CriticalSectionClient cs;
	/** 
     * critical section log, to register the events and its respectives timestamp.
    */
    private CriticalSectionLog csLog;
	
	public Node(String nodeId, NTPClient ntp, CriticalSectionClient cs) {
		super();
		this.cs = cs;
		this.ntp = ntp;
		this.nodeId = nodeId;
		this.generator = new Random();
		this.csLog = new CriticalSectionLog(nodeId);
	}
	
	/** 
	 * Behaviour to be executed as a independient Thread.
	 * Run steps:
	 * 	+ runs NTP algorithm to obtain {@link com.ssdd.util.constants.IConstants#NTP_NUM_ITERATIONS} {@link com.ssdd.ntp.bean.Pair} 
	 *  + iterates {@link com.ssdd.util.constants.IConstants#SIMULATION_NUM_ITERATIONS} times doing the following:
	 *  	- sleep during a random interval of time simulating a calulation
	 *  	- try acces to critical section
	 * 	+ runs NTP algorithm to obtain {@link com.ssdd.util.constants.IConstants#NTP_NUM_ITERATIONS} {@link com.ssdd.ntp.bean.Pair}
	 *  + calculates the delay and offset with the 2*{@link com.ssdd.util.constants.IConstants#NTP_NUM_ITERATIONS} obtained {@link com.ssdd.ntp.bean.Pair}
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void run() {
		// calculate the ntp delay and offset at the begginng
		LOGGER.log(Level.INFO, String.format("[node: %s] ntp initial", nodeId));
		Pair [] ntpInitialResult = ntp.sample();
		
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
		Pair [] ntpFinalResult = ntp.sample();

		// join all obtained pairs and calculate the best pair
		List <Pair> allPairs = new ArrayList<>(Arrays.asList(ntpInitialResult));
		allPairs.addAll(Arrays.asList(ntpFinalResult));
		Pair pair = this.ntp.selectBestPair(allPairs);
		LOGGER.log(Level.INFO, String.format("[node: %s] ntp result: %s", nodeId, pair.toString()));
		
		LOGGER.log(Level.INFO, String.format("[node: %s] finished", nodeId));
	}
	
	/** 
	 * Enteres in critical section, logs it with a {@link com.ssdd.main.CriticalSectionLog}, sleeps during a random interval
	 * of time, and then logs the out of critical section again. At last, releases the critical section.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
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
	
	/** 
	 * Simulates a sleep between an interval of min and max.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param min minimun time to sleep
	 * @param max maximum time to sleep
	*/
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
