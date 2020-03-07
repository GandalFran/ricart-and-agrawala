package com.ssdd.main;

import java.util.Arrays;

import com.ssdd.cs.client.CriticalSectionClient;
import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.ntp.client.NTPClient;
import com.ssdd.ntp.server.NTPService;
import com.ssdd.util.IConstants;

public class main {

	public static void main(String [] args) {
		int numberOfNodes = Integer.parseInt(args[0]);
		int nodeIdRangeStart = Integer.parseInt(args[1]);
		int nodeIdRangeEnd = Integer.parseInt(args[2]);
		String [] servers = Arrays.copyOfRange(args, 2, args.length);
		
		// build nodes
		String [] nodes = main.buildNodeIds(nodeIdRangeStart, nodeIdRangeEnd);
		
		// build and start nodes
		for(String node : nodes) {
			ClientBehaviour n = main.buildBehaviour(node, nodes, servers);
			n.start();
		}
	}
	
	private static String [] buildNodeIds(int nodeIdRangeStart, int nodeIdRangeEnd) {
		String [] nodes = new String [nodeIdRangeEnd-nodeIdRangeStart];
		for(int i=nodeIdRangeStart; i<=nodeIdRangeEnd; i++)
			nodes[i-nodeIdRangeStart] = String.format("Node%d", i);
		return nodes;
	}
	
	private static ClientBehaviour buildBehaviour(String nodeId, String [] nodes, String [] servers) {
		NTPClient ntp = main.buildNtpClient(servers);
		CriticalSectionClient cs = main.buildCsClient(nodes, servers);
		return new ClientBehaviour(nodeId, ntp, cs);
	}
	
	private static NTPClient buildNtpClient(String [] servers) {
		NTPService [] services = new NTPService [servers.length];
		
		for(int i=0; i<services.length; i++)
			services[i] = NTPService.buildProxy(servers[i]);
		
		return new NTPClient(services);
	}
	
	private static CriticalSectionClient buildCsClient(String [] nodes, String [] servers) {
		CriticalSectionService [] services = new CriticalSectionService [servers.length];
		
		for(int i=0; i<services.length; i++)
			services[i] = CriticalSectionService.buildProxy(servers[i]);
		
		return new CriticalSectionClient(nodes, services);
	}
}
