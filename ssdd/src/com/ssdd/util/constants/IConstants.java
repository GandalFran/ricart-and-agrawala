package com.ssdd.util.constants;

/** 
 * Stores the general constant values for the application.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public interface IConstants {
	/** 
	 * Log information
	 */
	public static final boolean DEBUG = true;
	/** 
	 * If log is centralized
	 */
	public static final boolean CENTRALIZED_LOG = true;
	/** 
	 * If log is centralized, centralized log host
	 */
	public static final String CENTRALIZED_LOG_IP = "192.168.230.129";
	/** 
	 * If log is centralized, centralized log host
	 */
	public static final String CENTRALIZED_LOG_FILE = "/root/ssdd.log";
	
	
	/**
	 * Error code given when an error related to HTTP request occurs.
	 * */
	public static final int EXIT_CODE_HTTP_REQUEST_ERROR = 100;
	/**
	 * Error code given when an error related to Thread behaviour (block, sleep, wait, ...) occurs.
	 * */
	public static final int EXIT_CODE_THREAD_ERROR = 200;
	/**
	 * Error code given when an error related to the simulation behaviour (linke a ProcessNotFoundException) occurs.
	 * */
	public static final int EXIT_CODE_SIMULATION_ERROR = 300;
	/**
	 * Error code given when an error related to I/O occurs.
	 * */
	public static final int EXIT_CODE_IO_ERROR =400;
	/**
	 * Error code given when an error related to number of arguments in any main class occurs.
	 * */
	public static final int EXIT_CODE_ARGS_ERROR = 500;
}
