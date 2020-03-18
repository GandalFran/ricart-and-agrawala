package com.ssdd.ntp.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ssdd.ntp.bean.MarzulloInterval;
import com.ssdd.ntp.bean.Pair;
import com.ssdd.ntp.service.NTPService;
import com.ssdd.ntp.service.NTPServiceProxy;
import com.ssdd.util.constants.IConstants;


/** 
 * NTP client to wrapp the access to implement the client part
 * of the NTP algorithm.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class NTPClient {
	
	/**
	 * list of services to request the time samples.
	 * */
	private NTPService [] services;
		
	public NTPClient() {
		super();
		this.services = null;
	}
	
	public NTPClient(NTPService [] services) {
		super();
		this.services = services;
	}

	/**
	 * for each service, samples time in host and server and calculates the delay and offset for {@link com.ssdd.util.constants.IConstants#NTP_NUM_ITERATIONS} times
	 * 
	 * @see com.ssdd.ntp.service.NTPService#time()
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return map with an association between {@link com.ssdd.ntp.service.NTPService} and an array with calculated {@link com.ssdd.ntp.bean.Pair} from servers
	 * */
	public Map<NTPService, Pair []> sample() {
		long time0, time1, time2, time3;
		Map<NTPService, Pair []> samples = new HashMap<>();
		
		for(NTPService service : services) {
			Pair [] pairs = new Pair [IConstants.NTP_NUM_ITERATIONS];
			for(int currIteration=0; currIteration<IConstants.NTP_NUM_ITERATIONS; currIteration++) {
				// get times
				time0 = System.currentTimeMillis();
				long [] response = NTPServiceProxy.parseTimeResponse(service.time());
				time3 = System.currentTimeMillis();
				time1 = response[0]; 
				time2 = response[1]; 
				pairs[currIteration] = new Pair(this.calculateDelay(time0, time1, time2, time3), this.calculateOffset(time0, time1, time2, time3));
			}
			samples.put(service, pairs);
		}
		
		return samples;
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
		return (((double)(time1-time0+time2-time3))/2);
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
		return ((double)((time1-time0)+(time3-time2)));
	}
	
	/**
	 * Given a list of Pairs (delay, offset), selects the best pair. Currently, the pair selected as best
	 * is the one with smallest delay.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param allPairs list from which pair will be selected
	 * 
	 * @return the Pair selected as Best.
	 * */
	public Pair selectBestPair(Pair [] allPairs) {
		Pair bestPair = new Pair(Long.MAX_VALUE, 0);
		
		for(Pair p: allPairs) {
			if(p.getDelay() <= bestPair.getDelay()) {
				bestPair = p;
			}
		}
		return bestPair;
	}
	/*
	public Pair selectBestPair(Pair [] pairs) {
		MarzulloInterval [] table = this.generateMarzulloTable(pairs);
		
		double best=0, cnt=0, bestStart=0, bestEnd=0;
		for(int i = 0; i< table.length; i++) {
			 MarzulloInterval interval  = table[i];
			cnt -= interval.getIntervalEnd();
			if(cnt > best) {
				best = cnt;
				bestStart = interval.getIntervalStart();
				bestEnd = table[i+1].getIntervalStart();
			}
		}
		
		Pair pair = this.calculatePair(bestStart, bestEnd);
		return pair;
	}
	
	private MarzulloInterval[] generateMarzulloTable(Pair [] pairs) {
		List <MarzulloInterval> table = new ArrayList<>();
		for(Pair p: pairs) {
			table.addAll(Arrays.asList(MarzulloInterval.buildMarzulloInterval(p)));
		}
		Collections.sort(table);
		return Arrays.copyOf(table.toArray(), table.size(), MarzulloInterval[].class);
	}
	
	private Pair calculatePair(double bestStart, double bestEnd) {
		double delay = bestEnd - bestStart;
		double offset = (bestStart + bestEnd)/2;
		return new Pair(delay, offset);
	}*/
	
}
