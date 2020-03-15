package com.ssdd.cs.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ssdd.cs.service.CriticalSectionService;
import com.ssdd.cs.service.NodeNotFoundException;
import com.ssdd.util.logging.SSDDLogFactory;

public class CriticalSectionRequestSenderPool extends SenderPool{

	/**
	 * Class logger generated with {@link com.ssdd.util.logging.SSDDLogFactory#logger(Class)}
	 * */
    private final static Logger LOGGER = SSDDLogFactory.logger(SenderPool.class);
    
    /** 
     * send one critical section access request from sender to receiver.
     * @see com.ssdd.cs.service.CriticalSectionService#request(String, String, long)
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
	@Override
	public void send(String sender, String receiver, CriticalSectionRouter router, long messageTimeStamp, Object other) {
		CriticalSectionService service = router.route(receiver);
		try {
			service.request(receiver, sender, messageTimeStamp);
		} catch (NodeNotFoundException e) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] send:NodeNotFoundException: error %s", sender, e.getMessage()), e);
		}
	}

}
