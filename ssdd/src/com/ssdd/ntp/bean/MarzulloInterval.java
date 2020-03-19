package com.ssdd.ntp.bean;


public class MarzulloInterval implements Comparable<MarzulloInterval>{

	private double intervalEnd;
	private double intervalStart;	
	
	public MarzulloInterval(double intervalStart, double intervalEnd) {
		super();
		this.intervalEnd = intervalEnd;
		this.intervalStart = intervalStart;
	}
	
	public static Pair toPair(double start, double end) {
		double delay = end - start;
		double offset = (start + end)/2;
		return new Pair(delay, offset);
	}
	
	public double getIntervalEnd() {
		return intervalEnd;
	}

	public void setIntervalEnd(double intervalEnd) {
		this.intervalEnd = intervalEnd;
	}

	public double getIntervalStart() {
		return intervalStart;
	}

	public void setIntervalStart(double intervalStart) {
		this.intervalStart = intervalStart;
	}

	@Override
	public int compareTo(MarzulloInterval o) {
		//int foo = Double.compare(this.intervalStart, o.intervalStart);	
		//return (foo != 0) ? foo : Double.compare(this.intervalEnd, o.intervalEnd);
		return Double.compare(this.intervalStart, o.intervalStart);
	}
	
}
