package com.ssdd.cs.bean;

public enum CriticalSectionMessageType {
	REQUEST,					// used to send request
	RESPONSE_DENY,				// used to response that acces is denied by this node
	RESPONSE_ALLOW,				// used to response that acces is allowed by this node  
	RESPONSE_DELAYED			// used to response that the response is queued and delayed
}
