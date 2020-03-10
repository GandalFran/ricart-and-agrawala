package com.ssdd.main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

/** 
 * Writes in the required format the simulation log file with the node's ins and outs.
 * @see <a href="https://www.codejava.net/java-se/file-io/how-to-read-and-write-text-file-in-java">https://www.codejava.net/java-se/file-io/how-to-read-and-write-text-file-in-java</a>
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class CriticalSectionLog {
	// src: https://www.codejava.net/java-se/file-io/how-to-read-and-write-text-file-in-java

    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionLog.class);
    
    /**
     * Format to log the ins to critical section
     * */
	private static final String IN_FORMAT = "\nP%s E %d";
    /**
     * Format to log the outs to critical section
     * */
	private static final String OUT_FORMAT = "\nP%s S %d";


    /**
     * node's id
     * */
	private String nodeId;
	/**
     * Data stream to write to file
     * */
	private FileWriter file;
	
	public CriticalSectionLog(String nodeId) {
		this.nodeId = nodeId;
		try {
			this.file = new FileWriter(IConstants.SIMULATION_LOG_FILE, true);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] constructor: error %s", this.nodeId, e.getMessage()), e);
		}
	}
	
	/** 
	 * logs an enter into the critical section on the log file.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void logIn() {
		try {
			this.file.write(String.format(CriticalSectionLog.IN_FORMAT, this.nodeId, System.currentTimeMillis()));
			this.file.flush();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] logIn: error %s", this.nodeId, e.getMessage()), e);
		}
	}
	
	/** 
	 * logs an exit into the critical section on the log file.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	*/
	public void logOut() {
		try {
			this.file.write(String.format(CriticalSectionLog.OUT_FORMAT, this.nodeId, System.currentTimeMillis()));
			this.file.flush();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] logOut: error %s", this.nodeId, e.getMessage()), e);
		}
	}
	
}
