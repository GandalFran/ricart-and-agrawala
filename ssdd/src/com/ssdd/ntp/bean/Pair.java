package com.ssdd.ntp.bean;

import java.io.Serializable;

/** 
 * Represents a pair of (delay,offset) for NTP bussines logic.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class Pair implements Serializable{
	
	/**
	 * to serialize
	 */
	private static final long serialVersionUID = 3000013736926164125L;
	
	/**
	 * delay calculated with the server and client samples in NTP algorithm.
	 * */
	private double delay;	
	/**
	 * offset calculated with the server and client samples in NTP algorithm.
	 * */
	private double offset;
	
	public Pair(double delay, double offset) {
		super();
		this.delay = delay;
		this.offset = offset;
	}
	
	public Pair(long t0, long t1, long t2, long t3) {
		super();
		this.delay = this.calculateDelay(t0, t1, t2, t3);
		this.offset = this.calculateOffset(t0, t1, t2, t3);
	}
	
	/**
	 * transform the pair into a pair of marzullo tuples (ofset - delay/2, -1) 
	 * and (ofset - delay/2, 1). 
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return a pair of Marzullo tuples representing the strat and end of the interval represented by the Pair
	 */
	public MarzulloTuple[] toMarzulloTuple() {
		return new MarzulloTuple [] {
				new MarzulloTuple(this.offset - (this.delay/2.0), -1),
				new MarzulloTuple(this.offset + (this.delay/2.0), +1)
		};
	}
	
	/**
	 * given four time stamples (two in the client side and two in the server side),
	 * calculates the offset between two hosts.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param time0 first time sampled in client
	 * @param time1 first time sampled in server
	 * @param time2 first time sampled in server
	 * @param time3 second time sampled in client
	 * 
	 * @return the calculated offset for the NTP algorithm.
	 * */
	private double calculateOffset(long time0, long time1, long time2, long time3) {
		return (((double)(time1-time0+time2-time3))/2.0);
	}
	
	/**
	 * given four time stamples (two in the client side and two in the server side),
	 * calculates the delay between two hosts.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param time0 first time sampled in client
	 * @param time1 first time sampled in server
	 * @param time2 first time sampled in server
	 * @param time3 second time sampled in client
	 * 
	 * @return the calculated delay for the NTP algorithm.
	 * */
	private double calculateDelay(long time0, long time1, long time2, long time3) {
		return ((double)(time1-time0+time3-time2));
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
