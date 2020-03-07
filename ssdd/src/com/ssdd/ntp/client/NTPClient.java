package com.ssdd.ntp.client;

import java.util.HashMap;
import java.util.Map;

import com.ssdd.ntp.bean.Pair;
import com.ssdd.ntp.server.NTPService;
import com.ssdd.ntp.server.NTPServiceProxy;
import com.ssdd.util.IConstants;


public class NTPClient {
	
	private NTPService[] services;
	
	
	public NTPClient(NTPService[] services) {
		super();
		this.services = services;
	}

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
	
	private double calculateOffset(long time0, long time1, long time2, long time3) {
		return ((double)(time1-time0+time2-time3))/2;
	}
	
	private double calculateDelay(long time0, long time1, long time2, long time3) {
		return ((double)(time1-time0+time3-time2));
	}
	
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
