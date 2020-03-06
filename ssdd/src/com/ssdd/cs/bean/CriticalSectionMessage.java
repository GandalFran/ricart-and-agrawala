package com.ssdd.cs.bean;

import com.google.gson.Gson;

public class CriticalSectionMessage{
	
	private String processId;
	private LamportCounter time;
	private CriticalSectionMessageType messageType;
	
	public CriticalSectionMessage(String processId, long time, CriticalSectionMessageType messageType) {
		super();
		this.processId = processId;
		this.messageType = messageType;
		this.time = new LamportCounter(time);
	}
	
	public CriticalSectionMessage(String processId, LamportCounter time, CriticalSectionMessageType messageType) {
		super();
		this.time = time;
		this.processId = processId;
		this.messageType = messageType;
	}

	public boolean hasPriority(String otherId, LamportCounter otherTime) {
		return ( 
				(this.time.getCounter() > otherTime.getCounter()) 
				|| (
						(this.time.getCounter() == otherTime.getCounter()) 
						&& (this.processId.compareTo(otherId) < 0)
				   )
			   );
	}
	
    public String toJson(){
        return new Gson().toJson(this);
    }

    public static CriticalSectionMessage fromJson(String data){
        return new Gson().fromJson(data, CriticalSectionMessage.class);
    }
	
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	public LamportCounter getTime() {
		return time;
	}
	public void setTime(LamportCounter time) {
		this.time = time;
	}
}
