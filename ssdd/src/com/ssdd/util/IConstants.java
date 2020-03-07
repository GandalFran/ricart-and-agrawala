package com.ssdd.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public interface IConstants {

	// properties file
	final static String PROPERTIES_FILE = "constants.properties";
	final static Properties PROPERTIES = loadPropertiesFile(PROPERTIES_FILE);

	static Properties loadPropertiesFile(String file) {
		// src: https://mkyong.com/java/java-properties-file-examples/
		Properties p = new Properties();
		try (InputStream is = new FileInputStream(file)) {
			p.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}
	
	// ntp service constants
	public final static int NTP_NUM_ITERATIONS = Integer.parseInt((String) PROPERTIES.get("NTP_NUM_ITERATIONS"));
	public final static long NTP_MIN_SLEEP_MS = Long.parseLong((String) PROPERTIES.get("NTP_MIN_SLEEP_MS"));
	public final static long NTP_MAX_SLEEP_MS = Long.parseLong((String) PROPERTIES.get("NTP_MAX_SLEEP_MS"));;
	
	// client constants	
	static final int SIMULATION_NUM_ITERATIONS = Integer.parseInt((String) PROPERTIES.get("SIMULATION_NUM_ITERATIONS"));
	static final long SIMULATION_MIN_CALULUS_TIME = Long.parseLong((String) PROPERTIES.get("SIMULATION_MIN_CALULUS_TIME"));
	static final long SIMULATION_MAX_CALULUS_TIME = Long.parseLong((String) PROPERTIES.get("SIMULATION_MAX_CALULUS_TIME"));
	static final long SIMULATION_MIN_CRITICAL_SECTION_TIME = Long.parseLong((String) PROPERTIES.get("SIMULATION_MIN_CRITICAL_SECTION_TIME"));
	static final long SIMULATION_MAX_CRITICAL_SECTION_TIME = Long.parseLong((String) PROPERTIES.get("SIMULATION_MAX_CRITICAL_SECTION_TIME"));
		
}
