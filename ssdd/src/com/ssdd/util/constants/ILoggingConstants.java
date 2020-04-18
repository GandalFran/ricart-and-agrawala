package com.ssdd.util.constants;

/** 
 * Stores the constant values related to logging.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class ILoggingConstants {
	/** 
	 * If set to true debugging information is dumped to log
	 */
	public static final boolean DEBUG = true;
	/** 
	 * If se to true, the log is centralized and messages are send to {@link #CENTRALIZED_LOG_IP} to be written to {@link #CENTRALIZED_LOG_FILE}
	 */
	public static final boolean CENTRALIZED_LOG = true;
	/** 
	 * If log is centralized, the host where the log messages will be send
	 */
	public static final String CENTRALIZED_LOG_IP = "vm1";
	/** 
	 * If log is centralized, the file, where the selected host in {@link #CENTRALIZED_LOG_IP} will write the log messages
	 */
	public static final String CENTRALIZED_LOG_FILE = "/home/vagrant/ssdd/ssdd.log";
}
