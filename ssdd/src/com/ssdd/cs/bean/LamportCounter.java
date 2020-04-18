package com.ssdd.cs.bean;

/** 
 * Represents a Lamport clock.
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
	 * Increments the Lamport counter in one unit. Uses LC1 formulae.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 */
	public void update() {
		this.counter++;
	}

	/** 
	 * Updates the Lamport counter with other counter value and then increments 
	 * the Lamport counter in one unit. Uses LC2 formulae.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param otherCounter the value of other Lamport time counter
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
