package com.ssdd.cs.bean;

/** 
 * types of messages used to synchronize between critical sections.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public enum CriticalSectionMessageType {
	/** used to send request */
	REQUEST,
	/** used to response that acces is denied by this node */
	RESPONSE_DENY,
	/** used to response that acces is allowed by this node */
	RESPONSE_ALLOW,
	/** used to response that the response is queued and delayed */
	RESPONSE_DELAYED			
}
