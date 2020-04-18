package com.ssdd.ntp.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.ntp.bean.Pair;
import com.ssdd.ntp.service.NTPService;
import com.ssdd.util.concurrent.ConcurrentExecutor;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;


/**
 * samples in a concurrent way all NTP servers
 * 
 * @see <a href="https://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread">https://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread</a>
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 */
public class NTPConcurrentSender extends ConcurrentExecutor{

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(NTPConcurrentSender.class);
 
	
	public NTPConcurrentSender() {
		super();
	}
	
	/**
	 * Builds as many runnables as services to perform all message send tasks in a concurrent way.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param client the NTP client to call the {@link com.ssdd.ntp.client.NTPClient#sample(NTPService)} method.
	 * @param services the list of services to ask for samples
	 * @param samples the map to store the results (association between service and an array of NTP Pairs)
	 * 
	 * @return list of NTP sampling tasks, to be run in a threadpool
	 * 
	 */
	public List<Runnable> buildTasks(NTPClient client, NTPService[] services, Map<NTPService, Pair []> samples){
		
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
					Pair[] pairs = null;
					try {
						pairs = this.client.sample(this.service);
					} catch (NTPMaxFailsReachedException e) {
						LOGGER.log(Level.WARNING, "unable to sample service: " + this.service.toString());
						System.exit(IConstants.EXIT_CODE_HTTP_REQUEST_ERROR);
					}
					samples.put(this.service, pairs);
				 }
				  
			}.init(client, service, samples);
			
			tasks.add(task);
		}
		
		return tasks;
	}
}


