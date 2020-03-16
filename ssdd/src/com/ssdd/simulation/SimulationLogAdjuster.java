package com.ssdd.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

public class SimulationLogAdjuster{
	// https://stackoverflow.com/questions/8430022/what-is-the-java-equivalent-of-sscanf-for-parsing-values-from-a-string-using-a-k
		
	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(SimulationLogAdjuster.class);
	
	private final static String LOG_PATTERN = "\"P[0-9]+ [E|S] [0-9]+";
	
	public void adjustTime(String file, long offset) {
		List<String> log = null;
		
		// read log
		try {
			log = Files.readAllLines(new File(file).toPath());
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("adjustTime: ERROR %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_IO_ERROR);
		}
		
		// correct times while write the file
		
		// create stream to write file
		FileWriter writer = null;
		try {
			writer = new FileWriter(file, true);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("adjustTime: ERROR %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_IO_ERROR);
		}
		
		// pattern to read the formatted log
		Pattern p = Pattern.compile(LOG_PATTERN);
		
		for(String line : log) {
			// extract elements from string
			Matcher m = p.matcher(line);
			
			String pid = m.group(0);
			String operation = m.group(1);
			long time = Long.parseLong(m.group(2));
			
			// increment time
			time += offset;
			
			// write new line
			try {
				writer.write(String.format("%s %s %d\n", pid, operation, time));
				writer.flush();
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, String.format("adjustTime: ERROR %s", e.getMessage()), e);
				System.exit(IConstants.EXIT_CODE_IO_ERROR);
			}
		}

	}
}
