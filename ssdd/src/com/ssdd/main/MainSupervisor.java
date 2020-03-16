package com.ssdd.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.ntp.bean.Pair;
import com.ssdd.ntp.client.NTPClient;
import com.ssdd.ntp.service.NTPService;
import com.ssdd.ntp.service.NTPServiceProxy;
import com.ssdd.simulation.SimulationLogAdjuster;
import com.ssdd.util.Utils;
import com.ssdd.util.logging.SSDDLogFactory;

public class MainSupervisor {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(MainSupervisor.class);
    
	public static void main(String [] args) {
		// take service type argument
		String service = args[0];

		switch(service) {
			case "restartCs":
				// take arguments
				int numNodes = Integer.parseInt(args[1]);
				String [] csServers = Arrays.copyOfRange(args, 2, args.length);
				// restart critical section
				MainSupervisor.restartCs(numNodes, csServers);
			break;
			case "ntp":
				// take arguments
				String file = args[1];
				String [] ntpServers = Arrays.copyOfRange(args, 2, args.length);
				// sample ntp and store to file
				MainSupervisor.sampleNtp(file, ntpServers);
				break;
			case "correctLog":
				// take arguments
				String ntpFile = args[1];
				String [] restOfArgs = Arrays.copyOfRange(args, 2, args.length);
				// associate logfile with ip
				Map<String, String> logFiles = new HashMap<>();
				for(int i =0; i<restOfArgs.length; i+=2) {
					logFiles.put(restOfArgs[i], restOfArgs[i+1]);
				}
				// correct logs
				MainSupervisor.correctLogs(ntpFile, logFiles);
				break;
			default:
				System.out.println("ERROR: selected service (" + service + ") not found.");
		}
		
	}
	
	private static void restartCs(int numNodes, String [] servers) {
		for(String server : servers)
			CriticalSectionService.buildProxy(server).restart(numNodes);
	}
	
	private static void sampleNtp(String file, String [] servers) {
		// build client
		NTPClient ntp = MainSupervisor.buildNtpClient(servers);
		
		// sample ntp
		Map<NTPService, Pair []> samples = ntp.sample();
		
		// if the file exists, load stored pairs, and join with the sampled
		File f = new File(file);
		if(f.exists()) {
			// read old file content
			Map<NTPService, Pair []> oldSample = MainSupervisor.loadNtpSamples(file);
			// join old and new samples
			samples = MainSupervisor.joinMaps(samples, oldSample);
		}
		
		// serialize results to store into file
		String jsonPairs = new Gson().toJson(samples);
		
		// store into file
		try {
			FileWriter writer = new FileWriter(file, true);
			writer.write(jsonPairs);
			writer.close();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("sampleNtp: ERROR: %s", e.getMessage()), e);
		}
	}
	
	private static void correctLogs(String ntpFile, Map<String, String> logFiles) {

		Map<String, Pair> logsToCorrect = new HashMap<>();
		
		// load ntp samples
		Map<NTPService, Pair []> samples = MainSupervisor.loadNtpSamples(ntpFile);
		
		// calculate best pairs for each
		NTPClient ntp = new NTPClient();
		for(NTPService s : samples.keySet()) {
			String serverIp = ((NTPServiceProxy) s).getServerIp();
			// get the log file with the server ip
			String logFile = logFiles.get(serverIp);
			// add to logsToCorrect
			logsToCorrect.put(logFile, ntp.selectBestPair(samples.get(s)));
		}	
		
		// adjust logs
		SimulationLogAdjuster adjuster = new SimulationLogAdjuster();
		for(String log : logsToCorrect.keySet()) {
			Pair p = logsToCorrect.get(log);
			long offset = (long) Math.ceil(p.getOffset());
			adjuster.adjustTime(log, offset);
		}
	}

	public static NTPClient buildNtpClient(String [] servers) {
		NTPService [] services = new NTPService [servers.length];
		for(int i=0; i<services.length; i++) 
			services[i] = NTPService.buildProxy(servers[i]);
		return new NTPClient(services);
	}
	
	private static Map<NTPService, Pair[]> loadNtpSamples(String file) {
		Map<NTPService, Pair []> samples = null;
		try {
			// read file
			List<String> lines = Files.readAllLines(new File(file).toPath());
			String oldSamplesJson = Utils.listToString(lines);
			// deserialize file content
			samples = new Gson().fromJson(oldSamplesJson, new TypeToken<Map<NTPServiceProxy, Pair []>>(){}.getType());
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("loadNtpSamples: ERROR: %s", e.getMessage()), e);
		}
		
		return samples;
	}
	
	private static Map<NTPService, Pair []> joinMaps(Map<NTPService, Pair []> m1, Map<NTPService, Pair []> m2){
		Map<NTPService, Pair []> result = new HashMap<>();
		
		for(NTPService service : m1.keySet()) {
			// join pairs into list
			List<Pair> pairList = new ArrayList<>();
			pairList.addAll(Arrays.asList(m1.get(service)));
			pairList.addAll(Arrays.asList(m2.get(service)));

			// generate array from list
			Pair [] pairArray = new Pair[pairList.size()];
			pairList.toArray(pairArray);
			
			// put in map
			result.put(service, pairArray);
		}
		return result;
	}
}
