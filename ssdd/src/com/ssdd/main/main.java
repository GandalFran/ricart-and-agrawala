package com.ssdd.main;

import java.util.Arrays;

import com.ssdd.cs.client.CriticalSectionClient;
import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.ntp.client.NTPClient;
import com.ssdd.ntp.service.NTPService;
import com.ssdd.util.IConstants;

public class main {

	public static void main(String [] args) {
		// take arguments
		int numberOfNodes = Integer.parseInt(args[0]);
		int assignedNodeIdRangeStart = Integer.parseInt(args[1]);
		int assignedNodeIdRangeEnd = Integer.parseInt(args[2]);
		String selectedService = args[3];
		String [] servers = Arrays.copyOfRange(args, 4, args.length);
		
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
