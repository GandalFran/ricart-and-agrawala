package com.ssdd.ntp.server;

import java.util.Random;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.ssdd.util.IConstants;
import com.ssdd.util.Utils;


@Singleton
@Path("/ntp")
public class NTPService {

	private Random generator;

	public NTPService() {
		this.generator = new Random();
	}
	
	public static NTPService buildProxy(String serviceUri) {
		return new NTPServiceProxy(serviceUri);
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/status")
	public String hola() {
		return "{ \"service\": \"ntp\", \"status\": \"ok\"}";
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/pedirTiempo")
	public String pedirTiempo() {
		long time1 = System.currentTimeMillis(); 
		try {
			Thread.sleep(Utils.randomBetweenInterval(this.generator, IConstants.NTP_MIN_SLEEP_MS, IConstants.NTP_MAX_SLEEP_MS));
		} catch (InterruptedException e) {
			System.err.println("["+ Thread.currentThread().getId()+"] An error occurred in " + e.toString());
		}
		long time2 = System.currentTimeMillis();
		
		return String.format("%d_%d", time1, time2);
	}
	
}
