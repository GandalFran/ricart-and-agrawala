package com.ssdd.cs.client.senders;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

import jersey.repackaged.com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * sends in a concurrent way messages to all servers to indicate that client is ready
 * */
public class CritialSectionReadySenderPool {

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(CritialSectionReadySenderPool.class);

    /**
     * ThreadPool to send the messages
     * */
    private ExecutorService pool;
    
    /** 
     * creates a threadpool with services.size() threads, to send services.size() messages in a concurrent way.
     * Then stops the threadpool to forbid the send of more messages in current poool.
     * To send messages uses the {@link #send(CriticalSectionService)} method
     * 
     * @version 1.0
     * @author Héctor Sánchez San Blas
     * @author Francisco Pinto Santos
     * 
     * @param services the list of sercvices to notify that this node is ready
    */
	public void multicastSend(List<CriticalSectionService> services) {

		// create new thread pool
		ThreadFactory nameThreadFactory = new ThreadFactoryBuilder().setNameFormat(Thread.currentThread().getName() + " pool %d").build();
		this.pool = Executors.newFixedThreadPool(services.size(), nameThreadFactory);
		
		// send services.size() messages	
		services.forEach(service -> 
			this.pool.submit( () -> {
				this.send(service);
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
			LOGGER.log(Level.WARNING, String.format("send: InterruptedException: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_THREAD_ERROR);
		}
	}
	
    /** 
     * send one critical section ready message to broker.
     * @see com.ssdd.cs.service.CriticalSectionService#ready()
     * 
     * @version 1.0
     * @author Héctor Sánchez San Blas
     * @author Francisco Pinto Santos
     * 
     * @param service the service which will be notified that this node is ready
    */
	public void send(CriticalSectionService service) {
		service.ready();
	}
}
