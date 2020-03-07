package com.ssdd.cs.service;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


// src: https://stackoverflow.com/questions/23858488/how-i-return-http-404-json-xml-response-in-jax-rs-jersey-on-tomcat

@Provider
public class NodeNotSuscribedInServiceException extends Exception implements ExceptionMapper<NodeNotSuscribedInServiceException>{

	private String nodeId;
	
	public NodeNotSuscribedInServiceException(String nodeId) {
		this.nodeId = nodeId;
	}
	
    @Context
    private HttpHeaders headers;

    public Response toResponse(NodeNotSuscribedInServiceException ex){
        return Response.status(404).entity("The node (" + this.nodeId + ") is not suscribed to current service").type(MediaType.TEXT_PLAIN).build();
    }
}
