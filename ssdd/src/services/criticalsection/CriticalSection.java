package services.criticalsection;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class CriticalSection {
	
	
	private Semaphore accessSemaphore = new Semaphore(1);
	private CriticalSectionState state;
	private Queue<CriticalSectionRequest> accessRequests;
	
	
	public CriticalSection() {
		this.state = CriticalSectionState.FREE;
		this.accessRequests = new LinkedList<>();
	}

	/**
	 * Reset the critical section state and drop queued access requests.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * */
	public void reset(){
		try {
			this.accessSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.state = CriticalSectionState.FREE;
		this.accessRequests.clear();
		this.accessSemaphore.release();
	}
	
	/**
	 * Used by local process to access the critical secion.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * */
	public String acquire() {
		
	}

	
	/**
	 * Used by local process to access the critical secion.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * */
	public String requestForAccess() {
		
	}

	
	/**
	 * Used by local process to release access to critical section.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas and Francisco Pinto Santos
	 * */
	public String release() {
		
	}

}
