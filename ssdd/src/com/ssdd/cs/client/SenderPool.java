package com.ssdd.cs.client;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.util.logging.SSDDLogFactory;

/** 
 * ThreadPool to send multicast messages
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public abstract class SenderPool {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(SenderPool.class);
    
    /**
     * ThreadPool to send the messages
     * */
    private ExecutorService pool;
    
    /** 
     * creates a threadpool with receivers.size() threads, to send receivers.size() messages in a concurrent way.
     * Then stops the threadpool to forbid the send of more messages in current poool.
     * To send messages uses the {@link SenderPool#send(String, String, CriticalSectionRouter, long, Object)} method, that must be overriden in child classes
     * 
     * @version 1.0
     * @author Héctor Sánchez San Blas
     * @author Francisco Pinto Santos
     * 
     * @param sender the sender's id in the system
     * @param receivers list of the receivers's id in the system
     * @param router to access the service where the receiver is allowed
     * @param messageTimeStamp the timestamp to store in the message, concretelly the sender's lamport time
     * @param other for future use (if neccesary)
    */
	public void multicastSend(String sender, List<String> receivers, CriticalSectionRouter router, long messageTimeStamp, Object other) {

		// create new thread pool
		this.pool = Executors.newFixedThreadPool(receivers.size());
		
		// send receivers.size() messages	
		receivers.forEach(receiver -> 
			this.pool.submit( () -> {
				this.send(sender, receiver, router, messageTimeStamp, other);
			})
		);

		// shutdown the pool when all tasks has finished
		this.pool.shutdown();	
	}
	
	/** 
     * waits for the response of all messages
     * 
     * @version 1.0
     * @author Héctor Sánchez San Blas
     * @author Francisco Pinto Santos
     * */
	public void await() {
		try {
			this.pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] send:InterruptedException: error %s", e.getMessage()), e);
		}
	}
	
	/** 
     * send one message from sender to receiver with the given timestamp. The router is used to obtain the service
     * where the receiver is allocated, and the other is for programmer use, to store give the method information
     * to send.
     * 
     * @version 1.0
     * @author Héctor Sánchez San Blas
     * @author Francisco Pinto Santos
     * 
     * @param sender the sender's id in the system
     * @param receiver the receivers's id in the system
     * @param router to access the service where the receiver is allowed
     * @param messageTimeStamp the timestamp to store in the message, concretelly the sender's lamport time
     * @param other for future use (if neccesary)
    */
	protected abstract void send(String sender, String receiver, CriticalSectionRouter router, long messageTimeStamp, Object other);
	
}
