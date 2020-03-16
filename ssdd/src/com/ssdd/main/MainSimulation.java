package com.ssdd.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.simulation.Node;
import com.ssdd.simulation.NodeBuilder;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

public class MainSimulation {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(MainSimulation.class);
    
	public static void main(String [] args) {
		// take arguments
		String simulationLogFile = args[0];
		int numberOfNodes = Integer.parseInt(args[1]);
		int assignedNodeIdRangeStart = Integer.parseInt(args[2]);
		int assignedNodeIdRangeEnd = Integer.parseInt(args[3]);
		int assignedBrokerPosition = Integer.parseInt(args[4]);
		String [] servers = Arrays.copyOfRange(args, 5, args.length);
		
		// calculate assigned broker from parameters
		String assignedBroker = servers[assignedBrokerPosition];
		
		// print information about params
		LOGGER.log(Level.INFO, "Params:");
		LOGGER.log(Level.INFO, "\t log file: " + simulationLogFile);
		LOGGER.log(Level.INFO, "\t number of nodes: " + numberOfNodes);
		LOGGER.log(Level.INFO, "\t assigned range start: " + assignedNodeIdRangeStart);
		LOGGER.log(Level.INFO, "\t assigned range start: " + assignedNodeIdRangeEnd);
		LOGGER.log(Level.INFO, "\t asigned broker: " + assignedBroker);
		LOGGER.log(Level.INFO, "\t servers: " + Arrays.toString(servers));

		// instance builder
		NodeBuilder builder = new NodeBuilder();

		// configure builder
		builder.servers(servers)
			.logFile(simulationLogFile)
			.numNodes(numberOfNodes)
			.asignedBroker(assignedBroker)
			.assignedIdRange(assignedNodeIdRangeStart, assignedNodeIdRangeEnd);

		// build node arrays
		String [] assignedNodes = builder.buildNodeIds(assignedNodeIdRangeStart, assignedNodeIdRangeEnd);
		
		// build nodes
		List<Node> nodes = new ArrayList<>();
		for(String node : assignedNodes) {
			nodes.add(builder.nodeId(node).build());
		}
		
		// start nodes
		nodes.forEach(n -> n.start());	
		
		// wait for nodoes to finish
		nodes.forEach(n -> {
			try {
				n.join();
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, String.format("ERROR when waiting for nodes to finish: %s", e.getMessage()), e);
				System.exit(IConstants.EXIT_CODE_THREAD_ERROR);
			}
		});	
	}
}
