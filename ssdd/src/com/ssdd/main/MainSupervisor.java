package com.ssdd.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.ntp.bean.Pair;
import com.ssdd.ntp.client.NTPClient;
import com.ssdd.ntp.service.NTPService;
import com.ssdd.ntp.service.NTPServiceProxy;
import com.ssdd.simulation.SimulationLogAdjuster;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

public class MainSupervisor {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(MainSupervisor.class);
    
	public static void main(String [] args)  throws Exception{
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
				// associate id with logfile and server
				Map<String, String> logAndServer = new HashMap<>();
				for(int i =0; i<restOfArgs.length; i+=2) {
					logAndServer.put(restOfArgs[i], restOfArgs[i+1]);
				}
				// correct logs
				MainSupervisor.correctLogs(ntpFile, logAndServer);
				break;
			default:
				System.out.println("ERROR: selected service (" + service + ") not found.");
		}
		
	}
	
	private static void restartCs(int numNodes, String [] servers) {
		for(String server : servers)
			CriticalSectionService.buildProxy(server).restart(numNodes);
	}
	
	private static void sampleNtp(String file, String [] servers)  throws Exception{
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
			// delete old sample file before write new samples
			f.delete();
		}
		
		// store into file
		MainSupervisor.storeNtpSamples(file, samples);
		
	}
	
	private static void correctLogs(String ntpFile, Map<String, String> logAndServer) {
		// load ntp samples
		Map<NTPService, Pair []> samples = MainSupervisor.loadNtpSamples(ntpFile);
	
		LOGGER.log(Level.INFO, "Arguments");
		LOGGER.log(Level.INFO, String.format("\t ntp file %s", ntpFile));
		LOGGER.log(Level.INFO, String.format("\t logs, hosts and pairs:", ntpFile));
		
		// calculate best pairs for each
		NTPClient ntp = new NTPClient();
		Map<String, Pair> logsAndPairs = new HashMap<>();
		for(String log : logAndServer.keySet()) {
			// get server
			String server = logAndServer.get(log);
			// get list of pairs
			for(NTPService service : samples.keySet()) {
				String serviceIp = ((NTPServiceProxy) service).getServerIp();
				if(server.equals(serviceIp)) {
					// calculate best pair and add it to logsAndPairs
					Pair pair = ntp.selectBestPair(samples.get(service));
					logsAndPairs.put(log, pair);
					LOGGER.log(Level.INFO, String.format("\t log: %s host: %s pair: %s", log, serviceIp, pair.toString()));
				}
			}			
		}
		
		// adjust logs
		SimulationLogAdjuster adjuster = new SimulationLogAdjuster();
		for(String log : logsAndPairs.keySet()) {
			Pair p = logsAndPairs.get(log);
			adjuster.adjustTime(log, p.getOffset());
		}
		
		
		// store association between log and pair into ntp file
		new File(ntpFile).delete();
		MainSupervisor.storePairs(ntpFile, logsAndPairs);
	}

	public static NTPClient buildNtpClient(String [] servers) {
		NTPService [] services = new NTPService [servers.length];
		for(int i=0; i<services.length; i++) 
			services[i] = NTPService.buildProxy(servers[i]);
		return new NTPClient(services);
	}
	
	private static Map<NTPService, Pair[]> loadNtpSamples(String file) {
		Map<String, Pair[]> loadMap = null;
		try {
	         FileInputStream fis = new FileInputStream(file);
	         ObjectInputStream ois = new ObjectInputStream(fis);
	         loadMap = (Map<String, Pair[]>) ois.readObject();
	         ois.close();
	         fis.close();
	    } catch (IOException | ClassNotFoundException e) {
			LOGGER.log(Level.WARNING, String.format("loadNtpSamples: ERROR: %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_IO_ERROR);
	    }

		Map<NTPService, Pair []> samples = new HashMap<>();
		for(String host : loadMap.keySet()) {
			samples.put(NTPService.buildProxy(host), loadMap.get(host));
		}
		
		return samples;
	}
	
	private static void storeNtpSamples(String file, Map<NTPService, Pair[]> samples) throws Exception{
		Map<String, Pair[]> storeMap = new HashMap<>();
		for(NTPService service : samples.keySet()) {
			storeMap.put(((NTPServiceProxy)service).getServerIp(), samples.get(service));
		}
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fos);
	        out.writeObject(storeMap);
	        out.close();
	        fos.close();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("storeNtpSamples: ERROR: %s", e.getMessage()), e);
			throw e;
			// System.exit(IConstants.EXIT_CODE_IO_ERROR);
		}
	}
	
	private static void storePairs(String file, Map<String, Pair> result) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fos);
	        out.writeObject(result);
	        out.close();
	        fos.close();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, String.format("storePairs: ERROR: %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_IO_ERROR);
		}
	}
	
	private static Map<NTPService, Pair []> joinMaps(Map<NTPService, Pair []> m1, Map<NTPService, Pair []> m2){
		Map<NTPService, Pair []> result = new HashMap<>();
		
		for(NTPService service : m1.keySet()) {
			// join pairs into list
			List<Pair> pairList = new ArrayList<>();
			pairList.addAll(Arrays.asList(m1.get(service)));
			
			// search the service equal to m1 in the m2 key list
			for(NTPService serviceM2 : m2.keySet()) {
				if(serviceM2.equals(service))
					pairList.addAll(Arrays.asList(m2.get(serviceM2)));
			}
			
			// generate array from list
			Pair [] pairArray = new Pair[pairList.size()];
			pairList.toArray(pairArray);
			
			// put in map
			result.put(service, pairArray);
		}
		return result;
	}
}
