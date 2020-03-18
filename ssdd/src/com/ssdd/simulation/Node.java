package com.ssdd.simulation;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.client.CriticalSectionClient;
import com.ssdd.util.Utils;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

/** 
 * Behaviour of a process in critical section test simulation.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class Node extends Thread{

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
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
     * client to access the system's private critical section service.
    */
	private CriticalSectionClient cs;
	/** 
     * critical section log, to register the events and its respectives timestamp.
    */
    private SimulationLog csLog;
	
	public Node(String nodeId, String logFile, CriticalSectionClient cs) {
		super();
		this.cs = cs;
		this.nodeId = nodeId;
		this.generator = new Random();
		this.csLog = new SimulationLog(nodeId, logFile);
		// set thread name
		this.setName("node " + nodeId);
	}
	
	/** 
	 * Behaviour to be executed as a independient Thread.
	 * For {@link com.ssdd.util.constants.IConstants#SIMULATION_NUM_ITERATIONS} times sleep during a random 
	 * interval of time simulating a calulation. Then try acces to critical section.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void run() {
		// wait until the other nodes are ready
		this.cs.ready();
		// iterate N times simulating calculus and entering in the critical section
		for(int i=0; i< IConstants.SIMULATION_NUM_ITERATIONS; i++) {
			LOGGER.log(Level.INFO, String.format("iter %d", i));
			this.simulateSleep(IConstants.SIMULATION_MIN_CALULUS_TIME, IConstants.SIMULATION_MAX_CALULUS_TIME);
			this.enterCriticalSection();
		}
		// wait untill all nodes has finished
		this.cs.finished();
	}
	
	/** 
	 * Enteres in critical section, logs it with a {@link com.ssdd.simulation.SimulationLog}, sleeps during a random interval
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
		
		LOGGER.log(Level.INFO, "simulating calculus in critical section");
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
		LOGGER.log(Level.INFO, String.format("simulating sleep of %d ms", sleepIntervalMs));
		try {
			Thread.sleep(sleepIntervalMs);
		} catch (InterruptedException e) {
			LOGGER.log(Level.INFO, String.format("simulateSleep: error %s", nodeId, e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_THREAD_ERROR);
		}
	}
	
}
