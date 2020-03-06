package com.ssdd.util;

import java.util.Properties;

public interface IConstants {

	// properties file
	final static String PROPERTIES_FILE = "constants.properties";
	final static Properties PROPERTIES = Utils.loadPropertiesFile(PROPERTIES_FILE);
	
	// client constants
	public final static int MIN_CALCULUS_TIME_MS = Integer.parseInt((String) PROPERTIES.get("minCalculusTime"));
	public final static int MAX_CALCULUS_TIME_MS = Integer.parseInt((String) PROPERTIES.get("maxCalculusTime"));
	
	public final static int MIN_CS_TIME_MS = Integer.parseInt((String) PROPERTIES.get("minCriticSectionTime"));
	public final static int MAX_CS_TIME_MS = Integer.parseInt((String) PROPERTIES.get("maxCriticSectionTime"));

	// ntp service constants
	public final static long NTP_MIN_SLEEP_MS = Integer.parseInt((String) PROPERTIES.get("minNtpSleepMs"));
	public final static long NTP_MAX_SLEEP_MS = Integer.parseInt((String) PROPERTIES.get("maxNtpSleepMs"));
	
	
}
