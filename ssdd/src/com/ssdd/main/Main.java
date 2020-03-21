package com.ssdd.main;

import java.util.Arrays;

import com.ssdd.util.constants.IConstants;

/**
 * main class
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 * */
public class Main {
	
	public static void main(String [] args){
		// args length check
		if(args.length < 1) {
			System.err.println("ERROR: error number of arguments");
			System.err.println("usage: <command>");
			System.err.println("\t supervisor: for ntp sampling and log correction");
			System.err.println("\t simulation: for critical section simulation");
			System.err.println("\t verification: for log verification");
			System.exit(IConstants.EXIT_CODE_ARGS_ERROR);
		}
		
		String command = args[0];
		args = Arrays.copyOfRange(args, 1, args.length);
		
		switch(command) {
			case "supervisor":
				MainSupervisor.main(args);
				break;
			case "simulation":
				MainSimulation.main(args);
				break;
			case "verification":
				MainLogVerification.main(args);
				break;
			default:
				System.out.println("ERROR: selected command (" + command + ") not found.");
				System.out.println("usage: availabe commands");
				System.err.println("\t supervisor: for ntp sampling and log correction");
				System.err.println("\t simulation: for critical section simulation");
				System.err.println("\t verification: for log verification");
				System.exit(IConstants.EXIT_CODE_ARGS_ERROR);
		}
	}
}
