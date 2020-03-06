package services.criticalsection;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

public class CriticalSectionProxy extends CriticalSection{

	private String serviceUri;
	private WebTarget service;
	
	public CriticalSectionProxy(String serviceUri) {
		this.serviceUri = serviceUri;
		this.service = ClientBuilder.newClient().target(UriBuilder.fromUri(serviceUri).build());
	}
	
}
