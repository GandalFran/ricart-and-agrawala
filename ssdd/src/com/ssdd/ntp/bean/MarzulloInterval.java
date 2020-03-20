package com.ssdd.ntp.bean;


public class MarzulloInterval implements Comparable<MarzulloInterval>{

	private int type;	
	private double offset;
	
	public MarzulloInterval(double offset, int type) {
		super();
		this.type = type;
		this.offset = offset;
	}
	
	public static Pair toPair(double start, double end) {
		double delay = end - start;
		double offset = (start + end)/2;
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
	public int compareTo(MarzulloInterval o) {
		// TODO: cambiar forma de comparar
		if(o.offset > this.offset) {
			return -1;
		}else if(o.offset < this.offset){
			return 1;
		}else {
			return this.type;
		}
	}
	
}
