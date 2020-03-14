package com.ssdd.cs.bean;

/** 
 * Represents a lamport clock.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 */
public class LamportCounter {
	
	/** 
	 * The internal counter for the clock
	 */
	private long counter;
	
	public LamportCounter() {
		super();
		this.counter = 0;
	}

	public LamportCounter(long counter) {
		super();
		this.counter = counter;
	}

	/** 
	 * Increments the lamport counter in one unit. Uses LC1 formule.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 */
	public void update() {
		this.counter++;
	}

	/** 
	 * Updates the lamport counter with other counter value and then increments 
	 * the lamport counter in one unit. Uses LC2 formule.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param otherCounter the value of other lamport time counter
	 */
	public void update(long otherCounter) {
		this.counter = ((this.counter > otherCounter) ? this.counter : otherCounter) + 1;
	}
	
	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}

}
