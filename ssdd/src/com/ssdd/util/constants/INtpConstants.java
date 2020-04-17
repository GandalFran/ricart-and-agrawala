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
	public static final int NUM_SAMPLES = 10;
	/** 
	 * Minimum time (in milliseconds) between time samples in NTP service.
	 */
	public static final long MIN_SLEEP_MS = 1000;
	/** 
	 * Maximum time (in milliseconds) between time samples in NTP service.
	 */
	public static final long MAX_SLEEP_MS = 2000;
	/**
	 * If set to true in the NTP service it waits a random time interval between samples, else the two time samples
	 * are taken consecutively.
	 * */
	public static final boolean SLEEP_BETWEEN_SAMPLES = true;
	/**
	 * Maximum failed attempts for the time sample request process  (/ntp/time) in the NTP service.
	 * */
	public static final int MAX_FAILED_ATTEMPTS = 10;
}
