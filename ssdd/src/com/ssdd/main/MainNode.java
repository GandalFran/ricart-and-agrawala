package com.ssdd.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.main.node.Node;
import com.ssdd.main.node.NodeBuilder;
import com.ssdd.ntp.bean.Pair;
import com.ssdd.ntp.client.NTPClient;
import com.ssdd.ntp.service.NTPService;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

public class MainNode {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(MainNode.class);
    
	public static void main(String [] args) {
		// take arguments
		boolean isSupervisor = Boolean.parseBoolean(args[0]);
		int numberOfNodes = Integer.parseInt(args[1]);
		int assignedNodeIdRangeStart = Integer.parseInt(args[2]);
		int assignedNodeIdRangeEnd = Integer.parseInt(args[3]);
		int assignedBrokerPosition = Integer.parseInt(args[4]);
		String [] servers = Arrays.copyOfRange(args, 5, args.length);
		
		// calculate assigned broker from parameters
		String assignedBroker = servers[assignedBrokerPosition];
		String logFile = String.format("%d_%s", assignedBrokerPosition, IConstants.SIMULATION_LOG_FILE);
		
		// print information about params
		LOGGER.log(Level.INFO, "Params:");
		LOGGER.log(Level.INFO, "\t is supervisor: " + isSupervisor);
		LOGGER.log(Level.INFO, "\t number of nodes: " + numberOfNodes);
		LOGGER.log(Level.INFO, "\t assigned range start: " + assignedNodeIdRangeStart);
		LOGGER.log(Level.INFO, "\t assigned range start: " + assignedNodeIdRangeEnd);
		LOGGER.log(Level.INFO, "\t asigned broker: " + assignedBroker);
		LOGGER.log(Level.INFO, "\t servers: " + Arrays.toString(servers));
		LOGGER.log(Level.INFO, "\t log file: " + logFile);

		// instance builder
		NodeBuilder builder = new NodeBuilder();

		// configure builder
		builder.servers(servers)
			.logFile(logFile)
			.numNodes(numberOfNodes)
			.asignedBroker(assignedBroker)
			.assignedIdRange(assignedNodeIdRangeStart, assignedNodeIdRangeEnd);
		
		// if is supervisor restart critical section in servers 
		if(isSupervisor) {
			for(String server : servers)
				CriticalSectionService.buildProxy(server).restart();
		}

		// if is supervisor instance ntp client
		NTPClient ntp = null;
		if(isSupervisor) {
			ntp = builder.buildNtpClient();
		}
		
		// if is supervisor calculate first ntp round
		Map<NTPService, Pair []> ntpInitial = null;
		if(isSupervisor) {
			ntpInitial = ntp.sample();
		}

		// build node arrays
		String [] assignedNodes = builder.buildNodeIds(assignedNodeIdRangeStart, assignedNodeIdRangeEnd);
		
		// build nodes
		List<Node> nodes = new ArrayList<>();
		for(String node : assignedNodes) {
			nodes.add( builder.nodeId(node).build());
		}
		
		// start nodes
		nodes.forEach(n -> n.start());	
		
		// wait for nodoes to finish
		nodes.forEach(n -> {
			try {
				n.join();
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, String.format("ERROR when waiting for nodes to finish: %s", e.getMessage()), e);
			}
		});	
		
		// if is supervisor calculate first ntp round
		Map<NTPService, Pair []> ntpFinal = null;
		if(isSupervisor) {
			ntpFinal = ntp.sample();
		}
		
		// if is supervisor generate NTP results
		if(isSupervisor) {
			Map<NTPService, List<Pair>> all = MainNode.joinMaps(ntpInitial, ntpFinal);
			Map<NTPService, Pair> results = new HashMap<>();
			
			// calculate results
			for(NTPService service : all.keySet()) {
				results.put(service, ntp.selectBestPair(all.get(service)));
			}
			
			// print results
			LOGGER.log(Level.INFO, String.format("NTP result:"));
			for(NTPService service: results.keySet())
				LOGGER.log(Level.INFO, String.format("%s | %s", service.toString(), results.get(service).toString() ));
		}
		
		// TODO: remove when finished
		LOGGER.log(Level.INFO, String.format("TODO: BORRAR cuando acabado : resultados prueba"));
		Comprobador.main(new String [] { logFile });
		
	}
	
	private static Map<NTPService, List<Pair>> joinMaps(Map<NTPService, Pair []> m1, Map<NTPService, Pair []> m2){
		Map<NTPService, List<Pair>> result = new HashMap<>();
		for(NTPService service : m1.keySet()) {
			List<Pair> p = new ArrayList<>();
			p.addAll(Arrays.asList(m1.get(service)));
			p.addAll(Arrays.asList(m2.get(service)));
			result.put(service, p);
		}
		return result;
	}
}
