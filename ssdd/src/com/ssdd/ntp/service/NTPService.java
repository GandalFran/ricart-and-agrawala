package com.ssdd.ntp.service;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.ssdd.util.Utils;
import com.ssdd.util.constants.INtpConstants;
import com.ssdd.util.logging.SSDDLogFactory;

/** 
 * NTP service to deploy in Apache Tomcat 7.0.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
@Singleton
@Path("/ntp")
public class NTPService{

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(NTPService.class);
	
    /** 
     * Random number generator (for sleep time).
     */
	private Random generator;

	public NTPService() {
		this.generator = new Random();
	}

	/**
	 * factory method, to build a proxy to access an instance of this service in remote.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param host the IP adress and PORT of server in which the service is allocated.
	 * 
	 * @return NTPServiceProxy to serve as proxy for the /ntp service, served in the given host
	 * */
	public static NTPService buildProxy(String host) {
		String serviceUri = NTPService.buildServiceUri(host);
		return new NTPServiceProxy(host, serviceUri);
	}
	
	/**
	 * factory method, to build a URI for a NTPService from the host IP and port.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param host the IP adress and PORT of server in which the service is allocated.
	 * 
	 * @return String containing the URI to the service, served in the given host
	 * */
	public static String buildServiceUri(String host) {
		return String.format("http://%s:8080/ssdd/ntp", host);
	}
	
	/**
	 * shows service status.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return string indicating the status of the service is up.
	 * */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/status")
	public String status() {
		return "{ \"service\": \"ntp\", \"status\": \"ok\"}";
	}
	

	/**
	 * samples the time twice separated by a random interval and returns it in a time1_time2 format.
	 * @see System#currentTimeMillis()
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return the two samples formatted into "time1_time2" if succeed. If it fails returns "0_0".
	 * */
	@GET
	@Path("/time")
	@Produces(MediaType.TEXT_PLAIN)
	public String time() {
		LOGGER.log(Level.INFO, "/ntp/time");
		
		// sample time
		long time1 = System.currentTimeMillis(); 
		
		// sleep during a random time
		try {
			long interval = Utils.randomBetweenInterval(this.generator, INtpConstants.NTP_MIN_SLEEP_MS, INtpConstants.NTP_MAX_SLEEP_MS);
			Thread.sleep(interval);
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, String.format("/ntp/pedirTiempo: ERROR InterruptedException: %s", e.getMessage()));
			return "0_0";
		}
		
		// sample for second time
		long time2 = System.currentTimeMillis();
		
		// build the response
		String response = String.format("%d_%d", time1, time2);
		
		return response;
	}
		
	
}
