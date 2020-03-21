package com.ssdd.simulation;

import com.ssdd.cs.client.CriticalSectionClient;
import com.ssdd.cs.service.CriticalSectionService;

/**
 * Builder to create {@link com.ssdd.simulation.SimulationProcess}
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 * */
public class SimulationProcessBuilder {

	private String processId;
	private int numProcesses;
	private int processIdRagneStart;
	private int processIdRagneEnd;

	private String logFile;
	
	private String [] servers;
	private String asignedService;

	/**
	 * builds a {@link com.ssdd.simulation.SimulationProcess} with the setted parameters 
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return SimulationProcess build with the provied data in other methods
	 * */
	public SimulationProcess build() {
		CriticalSectionClient cs = this.buildCsClient();
		cs.suscribe();
		return new SimulationProcess(processId, logFile, cs);
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
		for(int i=0; i<services.length; i++) 
			services[i] = CriticalSectionService.buildProxy(servers[i]);
		String [] processes = this.buildProcessIds(1, this.numProcesses);
		return new CriticalSectionClient(processId, CriticalSectionService.buildProxy(this.asignedService), processes, services);
	}
	
	/**
	 * builds an array with a processId range.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param rangeStart start of range
	 * @param rangeEnd end of range
	 * 
	 * @return String[] with the id of processes in given range
	 * */
	public String [] buildProcessIds(int rangeStart, int rangeEnd) {
		String [] processes = new String [(rangeEnd-rangeStart)+1];
		for(int i=rangeStart; i<=rangeEnd; i++)
			processes[i-rangeStart] = String.format("%d", i);
		return processes;
	}
	
	public SimulationProcessBuilder processId(String processId) {
		this.setProcessId(processId);
		return this;
	}
	
	public SimulationProcessBuilder numProcesses(int numProcesses) {
		this.setNumProcesses(numProcesses);
		return this;
	}
	
	public SimulationProcessBuilder assignedIdRange(int min, int max) {
		this.setProcessIdRagneStart(min);
		this.setProcessIdRagneStart(max);
		return this;
	}
	
	public SimulationProcessBuilder servers(String [] servers) {
		this.setServers(servers);
		return this;
	}
	
	public SimulationProcessBuilder asignedService(String asignedService) {
		this.setAsignedService(asignedService);
		return this;
	}
	
	public SimulationProcessBuilder logFile(String file) {
		this.setLogFile(file);
		return this;
	}
	
	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public int getNumProcesses() {
		return numProcesses;
	}

	public void setNumProcesses(int numProcesses) {
		this.numProcesses = numProcesses;
	}

	public int getProcessIdRagneStart() {
		return processIdRagneStart;
	}

	public void setProcessIdRagneStart(int processIdRagneStart) {
		this.processIdRagneStart = processIdRagneStart;
	}

	public int getProcessIdRagneEnd() {
		return processIdRagneEnd;
	}

	public void setProcessIdRagneEnd(int processIdRagneEnd) {
		this.processIdRagneEnd = processIdRagneEnd;
	}

	public String[] getServers() {
		return servers;
	}

	public void setServers(String[] servers) {
		this.servers = servers;
	}

	public String getAsignedService() {
		return asignedService;
	}

	public void setAsignedService(String asignedService) {
		this.asignedService = asignedService;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
	
}

