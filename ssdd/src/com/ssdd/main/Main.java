package com.ssdd.main;

import java.util.Arrays;

public class Main {
	
	public static void main(String [] args) {
		String selectedApplication = args[0];
		args = Arrays.copyOfRange(args, 1, args.length);
		
		switch(selectedApplication) {
			case "supervisor":
				MainSupervisor.main(args);
				break;
			case "simulation":
				MainSimulation.main(args);
				break;
			case "verification":
				Comprobador.main(args);
				break;
			default:
				System.out.println("ERROR: selected application (" + selectedApplication + ") not found.");
				System.out.println("Availabe applications");
				System.out.println("\t - supervisor: for ntp sampling and log correction");
				System.out.println("\t - simulation: for critical section simulation");
				System.out.println("\t - verification: for log verification");
		}
		
	}

}
