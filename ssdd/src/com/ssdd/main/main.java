package com.ssdd.main;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.util.logging.SSDDLogFactory;

public class main {

    private final static Logger LOGGER = SSDDLogFactory.logger(main.class);
    
	public static void main(String [] args) {
		// take arguments
		int numberOfNodes = Integer.parseInt(args[0]);
		int assignedNodeIdRangeStart = Integer.parseInt(args[1]);
		int assignedNodeIdRangeEnd = Integer.parseInt(args[2]);
		String selectedService = args[3];
		String [] servers = Arrays.copyOfRange(args, 4, args.length);
		
		// print information about params
		LOGGER.log(Level.INFO, "Params:");
		LOGGER.log(Level.INFO, "\t number of nodes: " + numberOfNodes);
		LOGGER.log(Level.INFO, "\t assigned range start: " + assignedNodeIdRangeStart);
		LOGGER.log(Level.INFO, "\t assigned range start: " + assignedNodeIdRangeEnd);
		LOGGER.log(Level.INFO, "\t selected service: " + selectedService);
		LOGGER.log(Level.INFO, "\t services: " + Arrays.toString(servers));
		
		
		// instance node builder
		NodeBuilder builder = new NodeBuilder();
		
		// build node arrays
		String [] assignedNodes = builder.buildNodeIds(assignedNodeIdRangeStart, assignedNodeIdRangeEnd);
		
		// configure builder
		builder.servers(servers)
				.numNodes(numberOfNodes)
				.selectedServer(selectedService)
				.assignedIdRange(assignedNodeIdRangeStart, assignedNodeIdRangeEnd);
		
		// build and start nodes
		for(String node : assignedNodes) {
			Node n = builder.nodeId(node).build();
			n.start();
		}
		
	}
}
