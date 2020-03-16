package com.ssdd.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ssdd.ntp.bean.Pair;
import com.ssdd.ntp.service.NTPService;
import com.ssdd.ntp.service.NTPServiceProxy;
import com.ssdd.util.Utils;

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
