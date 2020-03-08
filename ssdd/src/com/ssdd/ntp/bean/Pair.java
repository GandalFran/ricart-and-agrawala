package com.ssdd.ntp.bean;

public class Pair implements Comparable<Pair>{
	
	private double delay;
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
