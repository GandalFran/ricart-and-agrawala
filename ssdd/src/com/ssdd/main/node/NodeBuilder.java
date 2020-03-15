package com.ssdd.main.node;

import com.ssdd.cs.client.CriticalSectionClient;
import com.ssdd.cs.service.CriticalSectionService;

/**
 * Builder to create {@link com.ssdd.main.node.Node}
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 * */
public class NodeBuilder {

	private String nodeId;
	private int numNodes;
	private int nodeIdRagneStart;
	private int nodeIdRagneEnd;

	private String logFile;
	
	private String [] servers;
	private String asignedBroker;

	/**
	 * builds a {@link com.ssdd.main.node.Node} with the setted parameters 
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return Node build with the provied data in other methods
	 * */
	public Node build() {
		CriticalSectionClient cs = this.buildCsClient();
		cs.suscribe();
		return new Node(nodeId, logFile, cs);
	}
	
	
	/**
	 * builds a {@link com.ssdd.cs.service.CriticalSectionService} client.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return {@link com.ssdd.cs.client.CriticalSectionClient}  to be used as critical section interface
	 * */
	private CriticalSectionClient buildCsClient() {
		CriticalSectionService [] services = new CriticalSectionService [this.servers.length];
		
		for(int i=0; i<services.length; i++) {
			services[i] = CriticalSectionService.buildProxy(servers[i]);
		}
		
		String [] nodes = this.buildNodeIds(1, this.numNodes);
		
		return new CriticalSectionClient(nodeId, CriticalSectionService.buildProxy(this.asignedBroker), nodes, services);
	}
	
	/**
	 * builds an array with a nodeId range.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param rangeStart start of range
	 * @param rangeEnd end of range
	 * 
	 * @return String[] with the id of nodes in given range
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
	
	public NodeBuilder asignedBroker(String asignedBroker) {
		this.setAsignedBroker(asignedBroker);
		return this;
	}
	
	public NodeBuilder logFile(String file) {
		this.setLogFile(file);
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

	public String getAsignedBroker() {
		return asignedBroker;
	}

	public void setAsignedBroker(String asignedBroker) {
		this.asignedBroker = asignedBroker;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
	
}

