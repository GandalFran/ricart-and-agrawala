package com.ssdd.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.ntp.bean.Pair;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

/**
 * Main class to verify that consistency is fullfilmed in critical section during all simulation
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 * */
public class MainLogVerification {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(MainLogVerification.class);
	
    
	public static void main(String [] args) {
		// get pairs file and main logFile
		String pairsFile = args[0];
		String logFile = args[1];
		
		LOGGER.log(Level.INFO, "Arguments");
		LOGGER.log(Level.INFO, String.format("\t log file: %s", logFile));
		LOGGER.log(Level.INFO, String.format("\t pairs file: %s", pairsFile));
		
		// args to comprobation
		String [] comprobationArgs = null;
		
		switch(args.length-2) {
			case -2: 
			case -1:
				System.err.println("ERROR: error number of arguments");
				System.exit(IConstants.EXIT_CODE_ARGS_ERROR);
				break;
			case 0:
				// instance the comrpobationArgs array and set logFile
				comprobationArgs =  new String [] { logFile };
				break;
			default:
				// load pairs from ntp file
				Map<String, Pair> logsAndPairs = MainLogVerification.loadPairs(pairsFile);
				
				// instance the comrpobationArgs array
				comprobationArgs =  new String [args.length - 1];
				
				// set logFile
				comprobationArgs[0] = logFile;
				
				// load delay
				LOGGER.log(Level.INFO, "\t offsets and delays");
				for(int i = 0; i<(args.length-2); i++) {
					String log = args[i+2];
					Pair assignedPair = logsAndPairs.get(log);
					comprobationArgs[i+1] = new Double(assignedPair.getDelay()).toString();
					LOGGER.log(Level.INFO, String.format("\t\t LOG %s pair %s", log, assignedPair.toString()));
				}
		}

		// execute comprobation
		Comprobador.main(comprobationArgs);
	}
	
	/**
	 * Load a map with the association of logfile and associated NTP Pair (delay, offset).
	 * 
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param file where the map is stored in the java native serialization format
	 * 
	 * @return the described map
	 * */
	private static Map<String, Pair> loadPairs(String file) {
		Map<String, Pair> idsAndPairs = null;
		try {
	         FileInputStream fis = new FileInputStream(file);
	         ObjectInputStream ois = new ObjectInputStream(fis);
	         idsAndPairs = (Map<String, Pair>) ois.readObject();
	         ois.close();
	         fis.close();
	    } catch (IOException | ClassNotFoundException e) {
			LOGGER.log(Level.WARNING, String.format("loadPairs: ERROR: %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_IO_ERROR);
	    }

		return idsAndPairs;
	}
}
