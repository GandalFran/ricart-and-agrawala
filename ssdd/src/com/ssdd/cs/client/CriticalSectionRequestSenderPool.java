package com.ssdd.cs.client;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.cs.service.CriticalSectionServiceProxy;
import com.ssdd.cs.service.NodeNotFoundException;
import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

import jersey.repackaged.com.google.common.util.concurrent.ThreadFactoryBuilder;

public class CriticalSectionRequestSenderPool{

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionRequestSenderPool.class);

    /**
     * ThreadPool to send the messages
     * */
    private ExecutorService pool;
    
    /** 
     * creates a threadpool with receivers.size() threads, to send receivers.size() messages in a concurrent way.
     * Then stops the threadpool to forbid the send of more messages in current poool.
     * To send messages uses the {@link com.ssdd.cs.client.CriticalSectionRequestSenderPool#send(String, String, CriticalSectionRouter, long)} method
     * 
     * @version 1.0
     * @author H�ctor S�nchez San Blas
     * @author Francisco Pinto Santos
     * 
     * @param sender the sender's id in the system
     * @param receivers list of the receivers's id in the system
     * @param router to access the service where the receiver is allowed
     * @param messageTimeStamp the timestamp to store in the message, concretelly the sender's lamport time
    */
	public void multicastSend(String sender, List<String> receivers, CriticalSectionRouter router, long messageTimeStamp) {

		// create new thread pool
		ThreadFactory nameThreadFactory = new ThreadFactoryBuilder().setNameFormat(Thread.currentThread().getName() + " pool %d").build();
		this.pool = Executors.newFixedThreadPool(receivers.size(), nameThreadFactory);
		
		// send receivers.size() messages	
		receivers.forEach(receiver -> 
			this.pool.submit( () -> {
				this.send(sender, receiver, router, messageTimeStamp);
			})
		);

		// shutdown the pool when all tasks has finished
		this.pool.shutdown();	
	}
	
	/** 
     * waits for the response of all messages
     * 
     * @version 1.0
     * @author H�ctor S�nchez San Blas
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
     * send one critical section access request from sender to receiver.
     * @see com.ssdd.cs.service.CriticalSectionService#request(String, String, long)
     * 
     * @version 1.0
     * @author H�ctor S�nchez San Blas
     * @author Francisco Pinto Santos
     * 
     * @param sender the sender's id in the system
     * @param receiver the receivers's id in the system
     * @param router to access the service where the receiver is allowed
     * @param messageTimeStamp the timestamp to store in the message, concretelly the sender's lamport time
    */
	public void send(String sender, String receiver, CriticalSectionRouter router, long messageTimeStamp) {
		CriticalSectionService service = router.route(receiver);
		
		try {
			service.request(receiver, sender, messageTimeStamp);
		} catch (NodeNotFoundException e) {
			LOGGER.log(Level.WARNING, String.format("send: NodeNotFoundException: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_SIMULATION_ERROR);
		}
		
		/*CriticalSectionServiceProxy service = (CriticalSectionServiceProxy) router.route(receiver);
		Semaphore s = new Semaphore(0);
		try {
			service.request(receiver, sender, messageTimeStamp, 
				new InvocationCallback<Response>(){
					@Override
					public void completed(Response response){
						s.release();
					}
					@Override
					public void failed(Throwable throwable){
						System.out.println("Invocation failed.");
						throwable.printStackTrace();
					}
				}
			);
		} catch (NodeNotFoundException e) {
			LOGGER.log(Level.WARNING, String.format("send: NodeNotFoundException: error %s", e.getMessage()), e);
			System.exit(IConstants.EXIT_CODE_SIMULATION_ERROR);
		}
		
		try {
			s.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}
