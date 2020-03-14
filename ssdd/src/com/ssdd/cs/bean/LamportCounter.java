package com.ssdd.cs.bean;

import com.google.gson.Gson;

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
	 * Increments the lamport counter in one unit.
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
	 * the lamport counter in one unit.
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


	/** 
	 * Updates the lamport counter with other counter value and then increments 
	 * the current lamport counter in one unit.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param o other lamport time counter
	 */
	public void update(LamportCounter o) {
		this.counter = ((this.counter > o.counter) ? this.counter : o.counter) + 1;
	}
	
	/** 
	 * Checks if the other counter has a minor numer of reigstered events.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param o other lamport time counter
	 * 
	 * @return true if the other counter has registered a minor number of events
	 */
	public boolean isBefore(LamportCounter o) {
		return (o.counter < this.counter);
	}
	
	/** 
	 * Serializes the counter to JSON.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return a String with the JSON serialized counter.
	 */
    public String toJson(){
        return new Gson().toJson(this);
    }

    /** 
	 * Deserializes the counter from JSON.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param data String with a JSON serialized LapmortCounter
	 * 
	 * @return a LamportCounter instanced with data contained in the JSON.
	 */
    public static LamportCounter fromJson(String data){
        return new Gson().fromJson(data, LamportCounter.class);
    }
	
	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}

}
