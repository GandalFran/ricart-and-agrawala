package com.ssdd.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ssdd.ntp.bean.Pair;
import com.ssdd.util.Utils;
import com.ssdd.util.logging.SSDDLogFactory;

public class MainLogVerification {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(MainLogVerification.class);
	
    
	public static void main(String [] args) {
		String pairsFile = args[0];
		String logFile = args[1];
		
		Map<String, Pair> pairs = loadPairs(pairsFile);
		
		Comprobador.main(new String [] {
				logFile,
				new Double(pairs.get("1").getDelay()).toString(),
				new Double(pairs.get("2").getDelay()).toString()
		});
	}
	
	private static Map<String, Pair> loadPairs(String file) {
		Map<String, Pair> pairs = null;
		try {
			// read file
			List<String> lines = Files.readAllLines(new File(file).toPath());
			String pairsJson = Utils.listToString(lines);
			// deserialize file content
			pairs = new Gson().fromJson(pairsJson, new TypeToken<Map<String, Pair>>(){}.getType());
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("loadNtpSamples: ERROR: %s", e.getMessage()), e);
		}
		return pairs;
	}
}
