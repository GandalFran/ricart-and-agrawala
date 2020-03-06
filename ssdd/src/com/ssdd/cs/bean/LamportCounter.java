package com.ssdd.cs.bean;

import com.google.gson.Gson;

public class LamportCounter {
	
	private long counter;
	
	public LamportCounter() {
		super();
		this.counter = 1;
	}

	public LamportCounter(long counter) {
		super();
		this.counter = counter;
	}

	public void update() {
		this.counter++;
	}
	
	public void update(long otherCounter) {
		this.counter = ((this.counter > otherCounter) ? this.counter : otherCounter) + 1;
	}
	
	public void update(LamportCounter o) {
		this.counter = ((this.counter > o.counter) ? this.counter : o.counter) + 1;
	}
	
	public boolean isBefore(LamportCounter o) {
		return (o.counter < this.counter);
	}
	
    public String toJson(){
        return new Gson().toJson(this);
    }

    public static CriticalSectionMessage fromJson(String data){
        return new Gson().fromJson(data, CriticalSectionMessage.class);
    }
	
	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}

}
