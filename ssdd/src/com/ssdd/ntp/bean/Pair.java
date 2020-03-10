package com.ssdd.ntp.bean;

/** 
 * Represents a pair of (delay,offset) for NTP bussines logic.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class Pair implements Comparable<Pair>{
	
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
		this.offset = offset;
		this.delay = delay;
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
