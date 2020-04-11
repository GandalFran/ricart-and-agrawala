package com.ssdd.cs.client.senders;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.client.CriticalSectionRouter;
import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.cs.service.ProcessNotFoundException;
import com.ssdd.util.concurrent.ConcurrentSender;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

/**
 * sends in a concurrent way messages to all processes to request the acces to critical section.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 */
public class CriticalSectionRequestConcurrentSender extends ConcurrentSender{

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionRequestConcurrentSender.class);

	public CriticalSectionRequestConcurrentSender() {
		super();
	}
	
	/**
	 * Builds as many runnables as given processes to perform all message send tasks in a concurrent way.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param sender the sender's process id
	 * @param receivers the receivers' process id
	 * @param router to route messages between current sender and receivers
	 * @param messageTimeStamp the timestamp associated with the message
	 * 
	 * @return the list of runnables to perform the send task to each process
	 */
	public List<Runnable> buildCommunicationTasks(String sender, List<String> receivers, CriticalSectionRouter router, long messageTimeStamp){
		List<Runnable> tasks = new ArrayList<>();
		receivers.forEach(receiver -> {
			CriticalSectionService service = router.route(receiver);
			tasks.add(new Runnable() {		
					private String sender;
					private String receiver;
					private long messageTimeStamp;
					private CriticalSectionService service;
					
					public Runnable init(String sender, String receiver, long messageTimeStamp, CriticalSectionService service) {
				        this.sender = sender;
				        this.receiver = receiver;
				        this.messageTimeStamp = messageTimeStamp;
					    this.service = service;
					    return this;
					}
					
					public void run() {
						try {
							service.request(this.receiver, this.sender, this.messageTimeStamp);
							LOGGER.log(Level.INFO, String.format("recived response from P%s", this.receiver));
						} catch (ProcessNotFoundException e) {
							LOGGER.log(Level.WARNING, String.format("run: ProcessNotFoundException: error %s", e.getMessage()), e);
							System.exit(IConstants.EXIT_CODE_SIMULATION_ERROR);
						}
					}
				}.init(sender, receiver, messageTimeStamp, service)
			);
		});
		return tasks;
	}
}
