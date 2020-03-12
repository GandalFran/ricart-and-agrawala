package com.ssdd.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.cs.verify.Comprobador;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

public class main {

    private final static Logger LOGGER = SSDDLogFactory.logger(main.class);
    
	public static void main(String [] args) {
		// take arguments
		int numberOfNodes = Integer.parseInt(args[0]);
		int assignedNodeIdRangeStart = Integer.parseInt(args[1]);
		int assignedNodeIdRangeEnd = Integer.parseInt(args[2]);
		String assignedBroker = args[3];
		String ntpService = args[4];
		String [] servers = Arrays.copyOfRange(args, 5, args.length);
		
		// print information about params
		LOGGER.log(Level.INFO, "Params:");
		LOGGER.log(Level.INFO, "\t number of nodes: " + numberOfNodes);
		LOGGER.log(Level.INFO, "\t assigned range start: " + assignedNodeIdRangeStart);
		LOGGER.log(Level.INFO, "\t assigned range start: " + assignedNodeIdRangeEnd);
		LOGGER.log(Level.INFO, "\t ntp service: " + ntpService);
		LOGGER.log(Level.INFO, "\t asigned broker: " + assignedBroker);
		LOGGER.log(Level.INFO, "\t services: " + Arrays.toString(servers));
		
		// restart servers
		for(String server : servers)
			CriticalSectionService.buildProxy(server).restart();
		
		// instance node builder
		NodeBuilder builder = new NodeBuilder();
		
		// build node arrays
		String [] assignedNodes = builder.buildNodeIds(assignedNodeIdRangeStart, assignedNodeIdRangeEnd);
		
		// configure builder
		builder.servers(servers)
				.numNodes(numberOfNodes)
				.ntpServer(ntpService)
				.asignedBroker(assignedBroker)
				.assignedIdRange(assignedNodeIdRangeStart, assignedNodeIdRangeEnd);
			
		// delete old log file
		 new File(IConstants.SIMULATION_LOG_FILE).delete(); 
		
		// build and start nodes
		List<Node> nodes = new ArrayList<>();
		for(String node : assignedNodes) {
			nodes.add( builder.nodeId(node).build());
		}
		nodes.forEach(n -> n.start());	
		
		// wait for nodoes to finish
		nodes.forEach(n -> {
			try {
				n.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});	
		
		// pass test
		Comprobador.main(new String [] {IConstants.SIMULATION_LOG_FILE});
	}
}
