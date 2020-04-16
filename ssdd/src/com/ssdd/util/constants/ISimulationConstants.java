package com.ssdd.util.constants;

/** 
 * Stores the constant values for the simulation of the critical section.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public interface ISimulationConstants {

	public static final String SIMULATION_LOG_SERVER = "192.168.1.101";	
	public static final String SIMULATION_LOG_FILE = "/home/vagrant/ssdd/allsimulation.log";
	
	/** 
	 * Number of iterations of calculus and critical section wait in simulation.
	 */
	public static final int SIMULATION_NUM_ITERATIONS = 100;
	/** 
	 * Minimum time (in milliseconds) of calclus simulation in application.
	 */
	public static final long SIMULATION_MIN_CALULUS_TIME = 300;
	/** 
	 * Maximum time (in milliseconds) of calclus simulation in application.
	 */
	public static final long SIMULATION_MAX_CALULUS_TIME = 500;
	/** 
	 * Minimum time (in milliseconds) of critical section wait in application.
	 */
	public static final long SIMULATION_MIN_CRITICAL_SECTION_TIME = 100;
	/** 
	 * Maximum time (in milliseconds) of critical section wait in application.
	 */
	public static final long SIMULATION_MAX_CRITICAL_SECTION_TIME = 300;

}
