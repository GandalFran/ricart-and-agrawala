package com.ssdd.cs.client.senders;

import java.util.ArrayList;
import java.util.List;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.util.concurrent.ConcurrentSender;


/**
 * sends in a concurrent way messages to all servers to indicate that client has finished
 * 
 * @see <a href="https://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread">https://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread</a>
 * 
 * @version 1.0
 * @author H�ctor S�nchez San Blas
 * @author Francisco Pinto Santos
 */
public class CritialSectionFinishedConcurrentSender extends ConcurrentSender{

	public CritialSectionFinishedConcurrentSender() {
		super();
	}
	
	/**
	 * Builds as many runnables as services to perform all message send tasks in a concurrent way.
	 * 
	 * @version 1.0
	 * @author H�ctor S�nchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param services list to services to send the message 
	 * 
	 * @return the list of runnables to perform the send task to each service
	 */
	public List<Runnable> buildCommunicationTasks(List<CriticalSectionService> services){
		
		List<Runnable> tasks = new ArrayList<Runnable>();
		
		for(CriticalSectionService service : services) {
			Runnable task = new Runnable() {		
				private CriticalSectionService service;
				
				public Runnable init(CriticalSectionService service) {
				        this.service = service;
				        return this;
				}
				
				public void run() {
					 this.service.finished();
				}
			}.init(service);
			
			tasks.add(task);
		}	
		
		return tasks;
	}
}


