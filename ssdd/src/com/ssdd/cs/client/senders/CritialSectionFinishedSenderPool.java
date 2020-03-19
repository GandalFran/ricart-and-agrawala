package com.ssdd.cs.client.senders;

import com.ssdd.cs.service.CriticalSectionService;


/**
 * sends in a concurrent way messages to all servers to indicate that client has finished
 * */
public class CritialSectionFinishedSenderPool extends CritialSectionReadySenderPool{
	 /** 
     * send one critical section finished message to broker.
     * @see com.ssdd.cs.service.CriticalSectionService#ready()
     * 
     * @version 1.0
     * @author Héctor Sánchez San Blas
     * @author Francisco Pinto Santos
     * 
     * @param service the service which will be notified that this node has finished
    */
	public void send(CriticalSectionService service) {
		service.finished();
	}
}
