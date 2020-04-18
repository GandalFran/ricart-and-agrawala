package com.ssdd.util.concurrent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

import jersey.repackaged.com.google.common.util.concurrent.ThreadFactoryBuilder;


public class ConcurrentExecutor {
	
	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(ConcurrentExecutor.class);

    /**
     * ThreadPool to perform concurrent communication tasks.
     * */
    private ExecutorService pool;
	
    /** 
     * creates a threadpool with tasks.size() threads, to perform all the tasks in a concurrent way.
     * Then stops the threadpool to forbid the creation of new tasks in current pool.
     * 
     * @version 1.0
     * @author Héctor Sánchez San Blas
     * @author Francisco Pinto Santos
     * 
     * @param tasks tasks to perform in a concurrent way
    */
	public void multicastSend(List<Runnable> tasks) {

		// create new thread pool
		ThreadFactory nameThreadFactory = new ThreadFactoryBuilder().setNameFormat(Thread.currentThread().getName() + ".%d").build();
		this.pool = Executors.newFixedThreadPool(tasks.size(), nameThreadFactory);
		
		// send communicationTasks.size() messages	
		tasks.forEach(task -> this.pool.submit(task));
			
		// shutdown the pool when all tasks has finished
		this.pool.shutdown();	
	}
	
	/** 
     * waits for all tasks to finish
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
