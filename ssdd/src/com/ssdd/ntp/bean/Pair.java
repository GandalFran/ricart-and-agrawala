package com.ssdd.ntp.bean;

import java.io.Serializable;

/** 
 * Represents a pair of (delay,offset) for NTP bussines logic.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class Pair implements Comparable<Pair>, Serializable{
	
	/**
	 * Delay calculated with the server and client samples in NTP algorithm.
	 * */
	private double delay;	
	/**
	 * Offset calculated with the server and client samples in NTP algorithm.
	 * */
	private double offset;
	
	public Pair(double delay, double offset) {
		super();
		this.delay = delay;
		this.offset = offset;
	}
	
	public MarzulloInterval[] toMarzulloInterval() {
		return new MarzulloInterval [] {
				// new MarzulloInterval(this.offset,-1),
				// new MarzulloInterval(this.delay,+1)
				new MarzulloInterval(this.offset - (this.delay/2),-1),
				new MarzulloInterval(this.offset + (this.delay/2),+1)
		};
	}

	@Override
	public int compareTo(Pair p) {
		return Double.compare(this.offset, p.offset);
	}
	
	@Override
	public String toString() {
		return String.format("(o:%f, d:%f)",this.offset, this.delay);
	}
	
	public double getOffset() {
		return offset;
	}
	public void setOffset(double offset) {
		this.offset = offset;
	}
	public double getDelay() {
		return delay;
	}
	public void setDelay(double delay) {
		this.delay = delay;
	}
}
