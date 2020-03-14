package com.ssdd.cs.service;

import java.util.logging.Logger;

import com.google.gson.Gson;
import com.ssdd.util.logging.SSDDLogFactory;

/** 
 * Critical section proxy, to access a Critical Section service in a easier way
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class CriticalSectionServiceProxy extends CriticalSectionService{

    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionServiceProxy.class);
    
	public static String [] parseSuscribedResponse(String response) {
		return new Gson().fromJson(response, String[].class);
	}
	
}
