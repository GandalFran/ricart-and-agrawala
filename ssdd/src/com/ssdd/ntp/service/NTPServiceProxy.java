package com.ssdd.ntp.service;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.google.gson.Gson;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

/** 
 * NTP proxy, to access a NTP service in a easier way
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class NTPServiceProxy extends NTPService implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1422303209511297072L;

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(NTPServiceProxy.class);
	
    /**
     * server's ip
     * */
    private String serverIp;
	/**
	 * NTP service URI.
	 * */
	private String serviceUri;
	/**
	 * api client to make requests to NTP service..
	 * */
	private WebTarget service;
	
	public NTPServiceProxy(String serverIp, String serviceUri) {
		this.serverIp = serverIp;
		this.serviceUri = serviceUri;
		this.service = ClientBuilder.newClient().target(UriBuilder.fromUri(serviceUri).build());
	}
	
	/**
	 * See {@link com.ssdd.ntp.service.NTPService#time()}
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return array with to long, corresponding to time1 and time2 in "time1_time2" response.
	 * */
	@Override
	public String time() {
		LOGGER.log(Level.INFO, "/ntp/time");
		try {
			return this.service.path("time").request(MediaType.TEXT_PLAIN).get(String.class);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("time: ERROR %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
			return null;
		}
	}
	
	/**
	 * method to parse the /ntp/time response from String to long [].
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param response of the /ntp/time response
	 * 
	 * @return array with to long, corresponding to time1 and time2 in "time1_time2" response.
	 * */
	public static long[] parseTimeResponse(String response) {
		if(response== null || response.isEmpty()) {
			return null;
		}else {
			long [] times = new Gson().fromJson(response, long[].class);
			return (times.length == 0) ? null : times;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		else {
			NTPServiceProxy n = (NTPServiceProxy) obj;
			return this.serverIp.equals(n.serverIp) && this.serviceUri.equals(n.serviceUri);
		}
	}

	@Override
	public String toString() {
		return this.serverIp;
	}

	public String getServiceUri() {
		return serviceUri;
	}

	public void setServiceUri(String serviceUri) {
		this.serviceUri = serviceUri;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	
}
