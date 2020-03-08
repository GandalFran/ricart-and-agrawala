package com.ssdd.ntp.client;

import java.util.HashMap;
import java.util.Map;

import com.ssdd.ntp.bean.Pair;
import com.ssdd.ntp.service.NTPService;
import com.ssdd.ntp.service.NTPServiceProxy;
import com.ssdd.util.IConstants;


public class NTPClient {
	
	private NTPService[] services;
		
	public NTPClient(NTPService[] services) {
		super();
		this.services = services;
	}

	/**
	 * for each given {@link com.ssdd.ntp.service.NTPService} does the following {@link com.ssdd.util.IConstants.NTP_NUM_ITERATIONS} times:
	 * Samples time once, invokes the com.ssdd.ntp.service.NTPServiceProxy#pedirTiempo() method, parses it and samples the time again. Then
	 * calculates the delay and offset. After, this process has been completed {@link com.ssdd.util.IConstants.NTP_NUM_ITERATIONS} times, it
	 * takes the best pair (delay, offset).
	 * 
	 * @see com.ssdd.ntp.service.NTPService#pedirTiempo()
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return map with the asociation between a service and the best pair (offset, delay) for this service.
	 * */
	public Map<NTPService, Pair> estimate() {
		long time0, time1, time2, time3;
		Map<NTPService, Pair> bestPairs = new HashMap<>();
				
		// for each server
		for(NTPService server : this.services) {
			// for each iteration
			Pair [] pairs = new Pair [IConstants.NTP_NUM_ITERATIONS];
			for(int currIteration=0; currIteration<IConstants.NTP_NUM_ITERATIONS; currIteration++) {
				// get times
				time0 = System.currentTimeMillis();
				long [] response = NTPServiceProxy.parsePedirTiempoResponse(server.pedirTiempo());
				time1 = response[0]; 
				time2 = response[1]; 
				time3 = System.currentTimeMillis();
				pairs[currIteration] = new Pair(this.calculateDelay(time0, time1, time2, time3), this.calculateOffset(time0, time1, time2, time3));
			}
			bestPairs.put(server, this.selectBestPair(pairs));
		}
		
		return bestPairs;
	} 
	
	/**
	 * Calculates the offset of the time sampling in client and service.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param time0 first time sampled in client
	 * @param time1 first time sampled in server
	 * @param time2 first time sampled in server
	 * @param time3 second time sampled in client
	 * 
	 * @return the calculated offset with the NTP algorithm.
	 * */
	private double calculateOffset(long time0, long time1, long time2, long time3) {
		return ((double)(time1-time0+time2-time3))/2;
	}
	
	/**
	 * Calculates the delay of the time sampling in client and service.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param time0 first time sampled in client
	 * @param time1 first time sampled in server
	 * @param time2 first time sampled in server
	 * @param time3 second time sampled in client
	 * 
	 * @return the calculated delay with the NTP algorithm.
	 * */
	private double calculateDelay(long time0, long time1, long time2, long time3) {
		return ((double)(time1-time0+time3-time2));
	}
	
	/**
	 * Given a list of Pairs (delay, offset), selects the best pair. The pair selected as best
	 * is theone with smallest delay.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param time0 first time sampled in client
	 * @param time1 first time sampled in server
	 * @param time2 first time sampled in server
	 * @param time3 second time sampled in client
	 * 
	 * @return the Pair selected as Best.
	 * */
	protected Pair selectBestPair(Pair [] pairs) {
		Pair bestPair = new Pair(Long.MAX_VALUE, 0);
		
		for(Pair p: pairs) {
			if(p.getDelay() < bestPair.getDelay()) {
				bestPair = p;
			}
		}
		return bestPair;
	}

	public NTPService[] getServers() {
		return services;
	}

	public void setServers(NTPService[] servers) {
		this.services = servers;
	}
	
}
