package com.ssdd.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;
/**
 * Adjusts the time in logs with the offset between computers calculated with NTP algorithm.
 * 
 * @see <a href="https://stackoverflow.com/questions/8430022/what-is-the-java-equivalent-of-sscanf-for-parsing-values-from-a-string-using-a-k">https://stackoverflow.com/questions/8430022/what-is-the-java-equivalent-of-sscanf-for-parsing-values-from-a-string-using-a-k</a> 
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 * */
public class SimulationLogAdjuster{
	// https://stackoverflow.com/questions/8430022/what-is-the-java-equivalent-of-sscanf-for-parsing-values-from-a-string-using-a-k
		
	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(SimulationLogAdjuster.class);
	
    /**
     * Does the time adjustemnt.
     * 
     * @see <a href="https://stackoverflow.com/questions/8430022/what-is-the-java-equivalent-of-sscanf-for-parsing-values-from-a-string-using-a-k">https://stackoverflow.com/questions/8430022/what-is-the-java-equivalent-of-sscanf-for-parsing-values-from-a-string-using-a-k</a> 
     * 
     * @version 1.0
     * @author Héctor Sánchez San Blas
     * @author Francisco Pinto Santos
     * 
     * @param file path to the log which time wil ve adjusted
     * @param offset to adjust the log
     * */
	public void adjustTime(String file, double offset) {
		List<String> log = null;
		
		File f = new File(file);
		
		// read log
		try {
			log = Files.readAllLines(f.toPath());
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("adjustTime: ERROR %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_IO_ERROR);
		}
		
		// delete the old file
		if(!f.delete()) {
			LOGGER.log(Level.WARNING, String.format("adjustTime: ERROR unable to delete the older log File"));
			System.exit(IConstants.EXIT_CODE_IO_ERROR);
		}
		
		// open the write stream
		FileWriter writer = null;
		try {
			writer = new FileWriter(file, true);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("adjustTime: ERROR %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_IO_ERROR);
		}
		
		// correct times and write it to file
		Scanner s = null;
		for(String line : log) {
			
			// get groups of a log line
			s = new Scanner(line);
			s.useDelimiter(" ");

			String pid = s.next();
			String operation = s.next();
			long time = Long.parseLong(s.next());
			
			// increment time
			double finalTime = time + offset;
			
			// write new line
			try {
				writer.write(String.format("%s %s %f\n", pid, operation, finalTime));
				writer.flush();
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, String.format("adjustTime: ERROR %s", e.getMessage()), e);
				System.exit(IConstants.EXIT_CODE_IO_ERROR);
			}
		}
		
		try {
			writer.close();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("adjustTime: ERROR %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_IO_ERROR);
		}

	}
}
