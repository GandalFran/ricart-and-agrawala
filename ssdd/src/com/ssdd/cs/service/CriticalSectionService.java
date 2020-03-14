package com.ssdd.cs.service;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.ssdd.cs.bean.CritialSectionServiceNode;
import com.ssdd.cs.bean.CriticalSectionState;
import com.ssdd.cs.bean.LamportCounter;
import com.ssdd.util.logging.SSDDLogFactory;

@Singleton
@Path("/cs")
public class CriticalSectionService{

    private final static Logger LOGGER = SSDDLogFactory.logger(CriticalSectionService.class);
	
	private static Map<String, CritialSectionServiceNode> nodes;
	
	
	public static CriticalSectionService buildProxy(String host) {
		return new CriticalSectionService();
	}
	
	public static void restart(){
		LOGGER.log(Level.INFO, String.format("/cs/restart"));
		CriticalSectionService.nodes = new ConcurrentHashMap<String, CritialSectionServiceNode>();
	}

	public static void suscribe(@QueryParam(value="node") String nodeId){
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/suscribe", nodeId));
		CritialSectionServiceNode node = new CritialSectionServiceNode(nodeId, new LamportCounter(), CriticalSectionState.FREE);
		CriticalSectionService.nodes.put(nodeId, node);
	}
	
	public static String suscribed(){
		LOGGER.log(Level.INFO, String.format("/cs/suscribed"));

		// get nodes
		String[] suscribedNodes = new String[CriticalSectionService.nodes.keySet().size()];
		CriticalSectionService.nodes.keySet().toArray(suscribedNodes);
		
		// serialize list of nodes to JSON
		String response = new Gson().toJson(suscribedNodes);
		return response;
	}

	
	public static void setCsState(String nodeId, String newState) throws NodeNotFoundException {
		// LOGGER.log(Level.INFO, String.format("[node: %s] /cs/set/state %s ", nodeId, newState));
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/set/state %s", nodeId, newState));
		
		// check if given nodeId corresponds to a suscribed process
		if(! CriticalSectionService.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/set/state: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = CriticalSectionService.nodes.get(nodeId);
		
		//set new state
		node.setState(CriticalSectionState.valueOf(newState));
	}
	
	public  static String getLamport(String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/get/lamport %d", nodeId, CriticalSectionService.nodes.get(nodeId).getCounter().getCounter()));
		// check if given nodeId corresponds to a suscribed process
		if(! CriticalSectionService.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/set/state: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = CriticalSectionService.nodes.get(nodeId);
		
		// get lamport counter and serialize to json
		LamportCounter counter = node.getCounter();
		String jsonCounter = counter.toJson();
		
		node.setCounterValueWhenRequest();
		
		return jsonCounter;
	}
	
	
	public static void updateLamport(String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/update/lamport %d + 1", nodeId, CriticalSectionService.nodes.get(nodeId).getCounter().getCounter()));
		
		// check if given nodeId corresponds to a suscribed process
		if(! CriticalSectionService.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/set/state: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = CriticalSectionService.nodes.get(nodeId);

		// update counter
		node.getCounter().update();
	}	

	public static void request(String nodeId, String sender, long time) throws NodeNotFoundException {
		//LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request sender %s ", nodeId, sender));
		
		// check if given nodeId corresponds to a suscribed process
		if(! CriticalSectionService.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/request: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = CriticalSectionService.nodes.get(nodeId);
		
		// lock operations
		node.lock();
	
		// get node state and counter
		CriticalSectionState state = node.getState();
		LamportCounter counter = node.getCounter();
		
		// update local lamport time
		counter.update(time);
		
		if(state == CriticalSectionState.ACQUIRED
				|| (state == CriticalSectionState.REQUESTED && CriticalSectionService.compareCounters(nodeId, sender, node.getCounterValueWhenRequest(), time))) {
			LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request state: %s  (node,counter) -> (%s, %d) vs (%s, %d) = %s", nodeId, node.getState().toString(), nodeId, node.getCounterValueWhenRequest(), sender, time, "QUEUED"));
			//LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request node %s QUEUED", nodeId, sender));
			// make wait for process to release the critical section
			// unlock operations
			node.unlock();
			// wait until the enter in CS is permited
			node.waitToReleaseCriticalSection();
			LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request node %s DEQUEUED", nodeId, sender));
		}else {
			// unlock operations
			node.unlock();
			LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request state: %s  (node,counter) -> (%s, %d) vs (%s, %d) = %s", nodeId, node.getState().toString(), nodeId, counter.getCounter(), sender, time, "ALLOWED"));
			//LOGGER.log(Level.INFO, String.format("[node: %s] /cs/request node %s ALLOWED", nodeId, sender));
		}
		
	}
	
	private static boolean compareCounters(String pi, String pj, long ci, long tj) {
		return ( (ci < tj) || ( (ci == tj) && (pi.compareTo(pj) < 0) ) );
	}

	public static void release(String nodeId) throws NodeNotFoundException {
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/release", nodeId));
		
		// check if given nodeId corresponds to a suscribed process
		if(! CriticalSectionService.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/release: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = CriticalSectionService.nodes.get(nodeId);

		// lock operations
		node.lock();

		// set new state
		node.setState(CriticalSectionState.FREE);
		
		// notify all that critical section has been released
		node.releaseCriticalSection();

		// unlock operations
		node.unlock();
	}
	
	private static boolean isSuscribed(String nodeId) {
		return CriticalSectionService.nodes.keySet().contains(nodeId);
	}
	
	public static void lock( String nodeId) throws NodeNotFoundException {
		// check if given nodeId corresponds to a suscribed process
		if(! CriticalSectionService.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/release: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = CriticalSectionService.nodes.get(nodeId);
		
		// lock
		node.lock();
		
		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/lock", nodeId));
	}
	
	public static void unlock(String nodeId) throws NodeNotFoundException {
		// check if given nodeId corresponds to a suscribed process
		if(! CriticalSectionService.isSuscribed(nodeId)) {
			LOGGER.log(Level.WARNING, String.format("[node: %s] /cs/release: ERROR the given node is not subscribed", nodeId));
			throw new NodeNotFoundException(nodeId);
		}
		
		// get node
		CritialSectionServiceNode node = CriticalSectionService.nodes.get(nodeId);

		LOGGER.log(Level.INFO, String.format("[node: %s] /cs/unlock", nodeId));
		
		// unlock
		node.unlock();
	}

}
