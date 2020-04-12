package com.ssdd.ntp.bean;

/** 
 * represents a tuple for the Marzullo's algorithm.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 */
public class MarzulloTuple implements Comparable<MarzulloTuple>{
	
	/**
     * type of interval edge: if is -1 is start and if is -1 is end.
     * */
	private int type;	    
	/**
     * the value of the edge of the interval
     * */
	private double offset;
	
	public MarzulloTuple(double offset, int type) {
		super();
		this.type = type;
		this.offset = offset;
	}
	
	/**
	 * given the start and the oend of an interval, it transforms it into a 
	 * (delay, offset) pair for the NTP algorithm.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param start the start of the interval
	 * @param end the end of the interval
	 * 
	 * @return the interval, expressed in a pair (delay, offset), resulting from NTP algorithm 
	 */
	public static Pair toPair(double start, double end) {
		double delay = end - start;
		double offset = (start + end) / 2.0;
		return new Pair(delay, offset);
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
	}

	@Override
	public int compareTo(MarzulloTuple o) {
		//return (o.offset > this.offset) ? (-1) : ( (o.offset < this.offset) ? (1) : this.type);
		return ((this.offset == o.offset) ? (o.type - this.type) : ((this.offset < o.offset) ? (-1) : (1)) );
	}
	
}
