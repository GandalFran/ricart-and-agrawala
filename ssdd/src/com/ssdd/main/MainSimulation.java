package com.ssdd.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.simulation.SimulationProcess;
import com.ssdd.simulation.SimulationProcessBuilder;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

/**
 * main class to manage the simulation of the critical section enter
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 * */
public class MainSimulation {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(MainSimulation.class);
    
	public static void main(String [] args) {
		// args length check
		if(args.length < 5) {
			if(args[0].contentEquals("-h") || args[0].contentEquals("help")) {
				System.err.println("usage: <logFile> <numProcess> <assignedProcessRangeStart> <assignedProcessRangeEnd> <assignedServerPosition> <server1> [<server2> ... <serverN>]");
				System.err.println("NOTE: assignedServerPosition is the server position in the server array given in next parameters, from 0 to N-1.");
				return;
			}
			System.err.println("ERROR: error number of arguments. use -h for help.");
			System.exit(IConstants.EXIT_CODE_ARGS_ERROR);
		}
		
		// take arguments
		String simulationLogFile = args[0];
		int numberOfProcesses = Integer.parseInt(args[1]);
		int assignedProcessIdRangeStart = Integer.parseInt(args[2]);
		int assignedProcessIdRangeEnd = Integer.parseInt(args[3]);
		int assignedServerPosition = Integer.parseInt(args[4]);
		String [] servers = Arrays.copyOfRange(args, 5, args.length);
		
		// assignedservice length check
		if(assignedServerPosition < 0 || assignedServerPosition >= servers.length) {
			System.err.println("ERROR: error in assignedServer");
			System.err.println(String.format("Selected server %d, but number of servers is %d", assignedServerPosition, servers.length));
			System.err.println("NOTE: assignedServerPosition is the server position in the server array given in next parameters, from 0 to N-1.");
			System.exit(IConstants.EXIT_CODE_ARGS_ERROR);
		}
		
		// calculate assigned service from parameters
		String assignedService = servers[assignedServerPosition];
		
		// print information about params
		LOGGER.log(Level.INFO, "Params:"
			+ "\n\t log file: " + simulationLogFile
			+ "\n\t number of processes: " + numberOfProcesses
			+ "\n\t assigned range start: " + assignedProcessIdRangeStart
			+ "\n\t assigned range start: " + assignedProcessIdRangeEnd
			+ "\n\t asigned server: " + assignedService
			+ "\n\t servers: " + Arrays.toString(servers));

		// instance builder
		SimulationProcessBuilder builder = new SimulationProcessBuilder();

		// configure builder
		builder.servers(servers)
			.logFile(simulationLogFile)
			.numProcesses(numberOfProcesses)
			.asignedService(assignedService)
			.assignedIdRange(assignedProcessIdRangeStart, assignedProcessIdRangeEnd);

		// build process arrays
		String [] assignedProcesses = builder.buildProcessIds(assignedProcessIdRangeStart, assignedProcessIdRangeEnd);
		
		// build processes
		List<SimulationProcess> processes = new ArrayList<>();
		for(String process : assignedProcesses) {
			processes.add(builder.processId(process).build());
		}
		
		// start processes
		for(SimulationProcess p : processes)
			p.start();
		
		// wait for nodes to finish

		for(SimulationProcess p : processes){
			try {
				p.join();
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, String.format("ERROR when waiting for processes to finish: %s", e.getMessage()), e);
				System.exit(IConstants.EXIT_CODE_THREAD_ERROR);
			}
		}
	}
}
