package com.ssdd.main;

import com.ssdd.cs.client.CriticalSectionClient;
import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.ntp.client.NTPClient;
import com.ssdd.ntp.service.NTPService;

public class NodeBuilder {

	private String nodeId;
	private int numNodes;
	private int nodeIdRagneStart;
	private int nodeIdRagneEnd;

	private String [] servers;
	private String selectedServer;

	/**
	 * builds a Node 
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param servers array with the ip and port of all servers in system
	 * 
	 * @return NTPClient to be used as critical ntp interface
	 * */
	public Node build() {
		NTPClient ntp = this.buildNtpClient();
		CriticalSectionClient cs = this.buildCsClient();
		return new Node(nodeId, ntp, cs);
	}
	
	/**
	 * builds a ntp service client.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return NTPClient to be used as critical ntp interface
	 * */
	private NTPClient buildNtpClient() {
		NTPService [] services = new NTPService [this.servers.length];
		for(int i=0; i<services.length; i++)
			services[i] = NTPService.buildProxy(this.servers[i]);
		return new NTPClient(services);
	}
	
	/**
	 * builds a criticalSection client.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return CriticalSectionClient to be used as critical section interface
	 * */
	private CriticalSectionClient buildCsClient() {
		CriticalSectionService [] services = new CriticalSectionService [this.servers.length];
		
		for(int i=0; i<services.length; i++)
			services[i] = CriticalSectionService.buildProxy(servers[i]);
		
		String [] nodes = this.buildNodeIds(1, this.numNodes);
		
		return new CriticalSectionClient(nodeId, CriticalSectionService.buildProxy(this.selectedServer), nodes, services);
	}
	
	/**
	 * builds an array with a nodeId range.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return CriticalSectionClient to be used as critical section interface
	 * */
	public String [] buildNodeIds(int rangeStart, int rangeEnd) {
		String [] nodes = new String [(rangeEnd-rangeStart)+1];
		for(int i=rangeStart; i<=rangeEnd; i++)
			nodes[i-rangeStart] = String.format("%d", i);
		return nodes;
	}

	
	public NodeBuilder nodeId(String nodeId) {
		this.setNodeId(nodeId);
		return this;
	}
	
	public NodeBuilder numNodes(int numNodes) {
		this.setNumNodes(numNodes);
		return this;
	}
	
	public NodeBuilder assignedIdRange(int min, int max) {
		this.setNodeIdRagneStart(min);
		this.setNodeIdRagneStart(max);
		return this;
	}
	
	public NodeBuilder servers(String [] servers) {
		this.setServers(servers);
		return this;
	}
	
	public NodeBuilder selectedServer(String selectedServer) {
		this.setSelectedServer(selectedServer);
		return this;
	}
	
	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public int getNumNodes() {
		return numNodes;
	}

	public void setNumNodes(int numNodes) {
		this.numNodes = numNodes;
	}

	public int getNodeIdRagneStart() {
		return nodeIdRagneStart;
	}

	public void setNodeIdRagneStart(int nodeIdRagneStart) {
		this.nodeIdRagneStart = nodeIdRagneStart;
	}

	public int getNodeIdRagneEnd() {
		return nodeIdRagneEnd;
	}

	public void setNodeIdRagneEnd(int nodeIdRagneEnd) {
		this.nodeIdRagneEnd = nodeIdRagneEnd;
	}

	public String[] getServers() {
		return servers;
	}

	public void setServers(String[] servers) {
		this.servers = servers;
	}

	public String getSelectedServer() {
		return selectedServer;
	}

	public void setSelectedServer(String selectedServer) {
		this.selectedServer = selectedServer;
	}
	
	
	
}
