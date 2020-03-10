package com.ssdd.cs.service;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


/** 
 * Exception to represent that the requested node is not suscribed to curent broker.
 * Is only a security check to avoid unhandled errors like NoSuchKeyException on Maps, ...
 * @see <a href="https://stackoverflow.com/questions/23858488/how-i-return-http-404-json-xml-response-in-jax-rs-jersey-on-tomcat">https://stackoverflow.com/questions/23858488/how-i-return-http-404-json-xml-response-in-jax-rs-jersey-on-tomcat</a>
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
@Provider
public class NodeNotFoundException extends Exception implements ExceptionMapper<NodeNotFoundException>{

	/**
	 * The requested node's id.
	 * */
	private String nodeId;
	
	public NodeNotFoundException() {
		super("The requested node is not suscribed to this service");
	}
	
	public NodeNotFoundException(String nodeId) {
		this.nodeId = nodeId;
	}
    
    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(NodeNotFoundException ex){
        return Response.status(404).entity("The requested node (" + this.nodeId + ") is not suscribed to this service").type(MediaType.TEXT_PLAIN).build();
    }
}
