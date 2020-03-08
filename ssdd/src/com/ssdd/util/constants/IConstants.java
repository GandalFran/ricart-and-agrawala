package com.ssdd.util.constants;

public interface IConstants {

	// ntp service constants
	public static final int NTP_NUM_ITERATIONS = 10;
	public static final long NTP_MIN_SLEEP_MS = 100;
	public static final long NTP_MAX_SLEEP_MS = 500;
	
	// client constants	
	public static final String SIMULATION_LOG_FILE = "simulation.txt";
	public static final int SIMULATION_NUM_ITERATIONS = 100;
	public static final long SIMULATION_MIN_CALULUS_TIME = 300;
	public static final long SIMULATION_MAX_CALULUS_TIME = 500;
	public static final long SIMULATION_MIN_CRITICAL_SECTION_TIME = 100;
	public static final long SIMULATION_MAX_CRITICAL_SECTION_TIME = 300;	
}
