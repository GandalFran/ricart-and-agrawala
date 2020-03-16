package com.ssdd.cs.client;

import com.ssdd.cs.service.CriticalSectionService;

public class CritialSectionFinishedSenderPool extends CritialSectionReadySenderPool{
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
		service.finished();
	}
}
