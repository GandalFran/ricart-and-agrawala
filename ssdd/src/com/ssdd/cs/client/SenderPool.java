package com.ssdd.cs.client;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.bean.LamportCounter;
import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.cs.service.NodeNotFoundException;
import com.ssdd.util.logging.SSDDLogFactory;

public class SenderPool {

    private final static Logger LOGGER = SSDDLogFactory.logger(SenderPool.class);
    
	public static void send(String sender, LamportCounter c, List<String> receivers, CriticalSectionRouter router) {
		
		ExecutorService pool = Executors.newFixedThreadPool(receivers.size());
				
		for(String receiver : receivers) {
			pool.submit(() -> {
				CriticalSectionService service = router.route(receiver);
				try {
					service.request(receiver, sender, c.getCounter());
				} catch (NodeNotFoundException e) {
					LOGGER.log(Level.WARNING, String.format("[node: %s] send:NodeNotFoundException: error %s", sender, e.getMessage()), e);
				}
			});
		}
				
		pool.shutdown();
		
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] send:InterruptedException: error %s", sender, e.getMessage()), e);
		}	
	}
	
}
