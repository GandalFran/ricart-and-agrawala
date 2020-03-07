package com.ssdd.ntp.server;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.ssdd.util.IConstants;
import com.ssdd.util.Utils;
import com.ssdd.util.logging.SSDDLogFactory;


@Singleton
@Path("/ntp")
public class NTPService{

    private final static Logger LOGGER = SSDDLogFactory.logger(NTPService.class);
	
	private Random generator;

	public NTPService() {
		this.generator = new Random();
	}

	/**
	 * Factory method, to build a proxy to access an instance of this service in remote.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * 
	 * @param host the IP adress and PORT of server in which the service is allocated.
	 * */
	public static NTPService buildProxy(String host) {
		String serviceUri = NTPService.buildServiceUri(host);
		return new NTPServiceProxy(serviceUri);
	}
	
	/**
	 * Factory method, to build a URI for a NTPService from the host IP and port.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * 
	 * @param host the IP adress and PORT of server in which the service is allocated.
	 * */
	public static String buildServiceUri(String host) {
		return String.format("http://%s/ssdd/ntp", host);
	}

	/**
	 * shows service status.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
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
	 * @see #System.currentTimeMillis
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * 
	 * @return the two samples formatted into "time1_time2" if succeed. If it fails returns "0_0".
	 * */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/pedirTiempo")
	public String pedirTiempo() {
		LOGGER.log(Level.INFO, "/ntp/pedirTiempo");
		
		// sample time
		long time1 = System.currentTimeMillis(); 
		
		// sleep during a random time
		try {
			long interval = Utils.randomBetweenInterval(this.generator, IConstants.NTP_MIN_SLEEP_MS, IConstants.NTP_MAX_SLEEP_MS);
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
