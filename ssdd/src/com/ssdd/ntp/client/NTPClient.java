package com.ssdd.ntp.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.ntp.bean.MarzulloTuple;
import com.ssdd.ntp.bean.Pair;
import com.ssdd.ntp.service.NTPService;
import com.ssdd.ntp.service.NTPServiceProxy;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.constants.INtpConstants;
import com.ssdd.util.logging.SSDDLogFactory;


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
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(NTPClient.class);
 
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
	 * for each service, samples time in host and server and calculates the delay and offset for {@link com.ssdd.util.constants.INtpConstants#NTP_NUM_ITERATIONS} times
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
		Map<NTPService, Pair []> samples = new HashMap<>();
		
		List<Runnable> tasks = new ArrayList<>();
		for(NTPService service : services){
			Runnable task = new Runnable(){
				private NTPClient client;
				private NTPService service;
				private Map<NTPService, Pair []> samples;
				  
				private Runnable init(NTPClient client, NTPService service,  Map<NTPService, Pair []> samples){
					  this.client = client;
					  this.service = service;
					  this.samples = samples;
					  return this;
				}
				  
				public void run(){
					Pair [] pairs = this.client.sampleTime(this.service, INtpConstants.NTP_NUM_ITERATIONS);
					samples.put(this.service, pairs);
				 }
				  
			}.init(this, service, samples);
			tasks.add(task);
		}

		// execute with threadpool
		ExecutorService pool = Executors.newFixedThreadPool(tasks.size());	
		for(Runnable task: tasks)
			pool.submit(task);
		pool.shutdown();
		
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, String.format("await: InterruptedException: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_THREAD_ERROR);
		}
		
		return samples;
	} 
	
	private Pair [] sampleTime(NTPService service, int numIterations){
		long time0, time1, time2, time3;
		Pair [] pairs = new Pair [INtpConstants.NTP_NUM_ITERATIONS];
		for(int currIteration=0; currIteration<numIterations; currIteration++) {
			// get times
			time0 = System.currentTimeMillis();
			long [] response = NTPServiceProxy.parseTimeResponse(service.time());
			time3 = System.currentTimeMillis();
			time1 = response[0]; 
			time2 = response[1]; 
			pairs[currIteration] = new Pair(time0, time1, time2, time3);
			LOGGER.log(Level.INFO, String.format("sampled pair %s", pairs[currIteration].toString()));
		}
		return pairs;
	 }
	
	
	/**
	 * Given a list of Pairs (delay, offset), selects the best pair. Currently, the pair selected as best
	 * is the one selected with the Marzullo's algorithm.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param pairs list from which pair will be selected
	 * 
	 * @return the Pair selected as Best.
	 * */
	public Pair selectBestPair(Pair [] pairs) {
		List <MarzulloTuple> table = this.populateMarzulloTable(pairs);

		Collections.sort(table);
		
		int cnt=0, best=0;
		double bestStart=0.0, bestEnd=0.0;
		for(int i = 0; i< table.size(); i++) {
			cnt = cnt - table.get(i).getType();
			if(cnt > best) {
				best = cnt;
				bestStart = table.get(i).getOffset();
				bestEnd = table.get(i+1).getOffset();
			}
		}
		
		Pair pair = MarzulloTuple.toPair(bestStart, bestEnd);
		return pair;
	}
	
	/**
	 * facility to populate the marzullo's algorithm tuples table 
	 * using pairs of (delay, offset) resulting from the NTP time sampling.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param pairs list of pairs to populate the table.
	 * 
	 * @return the table
	 * */
	private List <MarzulloTuple> populateMarzulloTable(Pair [] pairs) {
		List <MarzulloTuple> table = new ArrayList<>();
		for(Pair p: pairs) {
			table.addAll(Arrays.asList(p.toMarzulloTuple()));
		}
		return table;
	}
	
}
