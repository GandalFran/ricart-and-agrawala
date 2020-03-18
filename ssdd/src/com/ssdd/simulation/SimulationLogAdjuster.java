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
	
	public void adjustTime(String file, double offset) {
		List<String> log = null;
		
		// determine offset
		long finalOffset = 0;
		int intPart = (int) offset;
		double decimalPart = offset - intPart;
		if(decimalPart >= 0.5) {
			finalOffset = (long) Math.ceil(offset);
		}else {
			finalOffset = (long) (offset);
		}
		
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
		
		for(String line : log) {
			
			String [] lineArray = line.split(" ");
			
			String pid = lineArray[0];
			String operation = lineArray[1];
			long time = Long.parseLong(lineArray[2]);
			
			// increment time
			time += finalOffset;
			
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
