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
			System.err.println("ERROR: error number of arguments. use -h option for information");
			System.exit(IConstants.EXIT_CODE_ARGS_ERROR);
		}
		
		String command = args[0];
		args = Arrays.copyOfRange(args, 1, args.length);
		
		switch(command) {
			case "-h": 
			case "help":
				System.err.println("usage: <command>");
				System.err.println("\t supervisor: for ntp sampling and log correction. use supervisor -h for help.");
				System.err.println("\t simulation: for critical section simulation. use simulation -h for help.");
				System.err.println("\t verification: for log verification. use verification -h for help.");
				break;
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
				System.out.println("ERROR: selected command (" + command + ") not found. use -h option for information.");
				System.exit(IConstants.EXIT_CODE_ARGS_ERROR);
		}
	}
}
