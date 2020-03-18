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

public class MainLogVerification {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(MainLogVerification.class);
	
    
	public static void main(String [] args) {
		String pairsFile = args[0];
		String logFile = args[1];
		
		System.err.println(args[0]);
		System.err.println(args[1]);
		
		Map<String, Pair> idsAndPairs = MainLogVerification.loadPairs(pairsFile);
		
		Comprobador.main(new String [] {
				logFile,
				new Double(idsAndPairs.get("1").getDelay()).toString()
				// , new Double(idsAndPairs.get("2").getDelay()).toString()
		});
	}
	
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
