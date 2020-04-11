package com.ssdd.util.concurrent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import jersey.repackaged.com.google.common.util.concurrent.ThreadFactoryBuilder;

import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;


public class ConcurrentSender {
	
	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(ConcurrentSender.class);

    /**
     * ThreadPool to perform concurrent communication tasks.
     * */
    private ExecutorService pool;
	
    /** 
     * creates a threadpool with communicationTasks.size() threads, to perform all the communication tasks in a concurrent way.
     * Then stops the threadpool to forbid the send of more messages in current poool.
     * 
     * @version 1.0
     * @author Héctor Sánchez San Blas
     * @author Francisco Pinto Santos
     * 
     * @param communicationTasks tasks to perform in a concurrent way
    */
	public void multicastSend(List<Runnable> communicationTasks) {

		// create new thread pool
		ThreadFactory nameThreadFactory = new ThreadFactoryBuilder().setNameFormat(Thread.currentThread().getName() + ".%d").build();
		this.pool = Executors.newFixedThreadPool(communicationTasks.size(), nameThreadFactory);
		
		// send services.size() messages	
		for(Runnable task: communicationTasks)
			this.pool.submit(task);

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
			LOGGER.log(Level.WARNING, String.format("await: InterruptedException: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_THREAD_ERROR);
		}
	}

}
