package com.ssdd.ntp.client;

import java.util.List;

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
	private NTPService service;
		
	public NTPClient(NTPService service) {
		super();
		this.service = service;
	}

	/**
	 * samples time in host and serer and calculates the delay and offset for {@link com.ssdd.util.constants.IConstants#NTP_NUM_ITERATIONS} times
	 * 
	 * @see com.ssdd.ntp.service.NTPService#time()
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return array with calculated {@link com.ssdd.ntp.bean.Pair}
	 * */
	public Pair [] sample() {
		long time0, time1, time2, time3;
		Pair [] pairs = new Pair [IConstants.NTP_NUM_ITERATIONS];
		
		for(int currIteration=0; currIteration<IConstants.NTP_NUM_ITERATIONS; currIteration++) {
			// get times
			time0 = System.currentTimeMillis();
			long [] response = NTPServiceProxy.parseTimeResponse(this.service.time());
			time1 = response[0]; 
			time2 = response[1]; 
			time3 = System.currentTimeMillis();
			pairs[currIteration] = new Pair(this.calculateDelay(time0, time1, time2, time3), this.calculateOffset(time0, time1, time2, time3));
		}
		
		return pairs;
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
	public Pair selectBestPair(List<Pair> allPairs) {
		Pair bestPair = new Pair(Long.MAX_VALUE, 0);
		
		for(Pair p: allPairs) {
			if(p.getDelay() < bestPair.getDelay()) {
				bestPair = p;
			}
		}
		return bestPair;
	}

	public NTPService getService() {
		return service;
	}

	public void setService(NTPService service) {
		this.service = service;
	}
}
