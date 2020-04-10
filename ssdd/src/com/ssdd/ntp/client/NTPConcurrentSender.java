package com.ssdd.ntp.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ssdd.ntp.bean.Pair;
import com.ssdd.ntp.service.NTPService;
import com.ssdd.util.concurrent.ConcurrentSender;
import com.ssdd.util.constants.INtpConstants;


/**
 * samples in a concurrent way all NTP servers
 * 
 * @see <a href="https://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread">https://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread</a>
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 */
public class NTPConcurrentSender extends ConcurrentSender{

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
					Pair [] pairs = this.client.sampleTime(this.service, INtpConstants.NTP_NUM_ITERATIONS);
					samples.put(this.service, pairs);
				 }
				  
			}.init(client, service, samples);
			
			tasks.add(task);
		}
		
		return tasks;
	}
}


