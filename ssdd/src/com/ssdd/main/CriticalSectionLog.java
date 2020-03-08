package com.ssdd.main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

public class CriticalSectionLog {
	// src: https://www.codejava.net/java-se/file-io/how-to-read-and-write-text-file-in-java

    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionLog.class);
    
	private static final String IN_FORMAT = "\nP%s E %d";
	private static final String OUT_FORMAT = "\nP%s S %d";

	private String nodeId;
	private FileWriter file;
	
	public CriticalSectionLog(String nodeId) {
		this.nodeId = nodeId;
		try {
			this.file = new FileWriter(IConstants.SIMULATION_LOG_FILE, true);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] constructor: error %s", this.nodeId, e.getMessage()), e);
		}
	}
	
	public void logIn() {
		try {
			this.file.write(String.format(CriticalSectionLog.IN_FORMAT, this.nodeId, System.currentTimeMillis()));
			this.file.flush();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] logIn: error %s", this.nodeId, e.getMessage()), e);
		}
	}
	
	public void logOut() {
		try {
			this.file.write(String.format(CriticalSectionLog.OUT_FORMAT, this.nodeId, System.currentTimeMillis()));
			this.file.flush();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] logOut: error %s", this.nodeId, e.getMessage()), e);
		}
	}
	
}
