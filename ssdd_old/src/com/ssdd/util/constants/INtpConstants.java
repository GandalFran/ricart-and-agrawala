package com.ssdd.util.constants;

/** 
 * Stores the constant values for the NTP algorithm.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public interface INtpConstants {
	/** 
	 * Number of iterations of sampling time in the client and server on NTP algorithm.
	 */
	public static final int NTP_NUM_ITERATIONS = 10;
	/** 
	 * Minimum time (in milliseconds) between time samples in NTP service.
	 */
	public static final long NTP_MIN_SLEEP_MS = 1000;
	/** 
	 * Maximum time (in milliseconds) between time samples in NTP service.
	 */
	public static final long NTP_MAX_SLEEP_MS = 2000;
}
