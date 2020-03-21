package com.ssdd.simulation;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.client.CriticalSectionClient;
import com.ssdd.util.Utils;
import com.ssdd.util.constants.ISimulationConstants;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

/** 
 * Behaviour of a process in critical section test simulation.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class SimulationProcess extends Thread{

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(SimulationProcess.class);
    
    /** 
     * the process's id
    */
	private String processId;    
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
	
	public SimulationProcess(String processId, String logFile, CriticalSectionClient cs) {
		super();
		this.cs = cs;
		this.processId = processId;
		this.generator = new Random();
		this.csLog = new SimulationLog(processId, logFile);
		// set thread name
		this.setName(String.format("P%s", processId));
	}
	
	/** 
	 * Behaviour to be executed as a independient Thread.
	 * For {@link com.ssdd.util.constants.ISimulationConstants#SIMULATION_NUM_ITERATIONS} times sleep during a random 
	 * interval of time simulating a calulation. Then try acces to critical section.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void run() {
		// wait until the other processs are ready
		this.cs.ready();
		// iterate N times simulating calculus and entering in the critical section
		for(int i=0; i< ISimulationConstants.SIMULATION_NUM_ITERATIONS; i++) {
			LOGGER.log(Level.INFO, String.format("iter %d", i));
			this.simulateSleep(ISimulationConstants.SIMULATION_MIN_CALULUS_TIME, ISimulationConstants.SIMULATION_MAX_CALULUS_TIME);
			this.enterCriticalSection();
		}
		// wait untill all processs has finished
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
		this.simulateSleep(ISimulationConstants.SIMULATION_MIN_CRITICAL_SECTION_TIME, ISimulationConstants.SIMULATION_MAX_CRITICAL_SECTION_TIME);

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
			LOGGER.log(Level.INFO, String.format("simulateSleep: error %s", processId, e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_THREAD_ERROR);
		}
	}
	
}
