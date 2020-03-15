package com.ssdd.util.constants;

/** 
 * Stores constant values for the application.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public interface IConstants {

	// ntp service constants
	/** 
	 * Number of iterations of sampling time in the client and server on NTP algorithm.
	 */
	public static final int NTP_NUM_ITERATIONS = 10;
	/** 
	 * Minimum time (in milliseconds) between time samples in NTP service.
	 */
	public static final long NTP_MIN_SLEEP_MS = 100;
	/** 
	 * Maximum time (in milliseconds) between time samples in NTP service.
	 */
	public static final long NTP_MAX_SLEEP_MS = 500;
	
	// simulation constants	
	/** 
	 * Path to the application critical secion in and out registration log.
	 */
	public static final String SIMULATION_LOG_FILE_SUFIX = "simulation.log";
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
