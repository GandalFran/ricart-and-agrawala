package com.ssdd.cs.verify;

import com.ssdd.cs.verify.bean.CriticalSectionEventType;

public class CriticalSectionEvent implements Comparable<CriticalSectionEvent>{

	private long time;
	private String nodeId;
	private CriticalSectionEventType type;
	
	public CriticalSectionEvent(long time, String nodeId, CriticalSectionEventType type) {
		super();
		this.time = time;
		this.type = type;
		this.nodeId = nodeId;
	}

	public static CriticalSectionEvent buildFromString(String logLine) {
		String [] splitted = logLine.split(" ");
		String nodeId = splitted[0].substring(1);
		CriticalSectionEventType type = CriticalSectionEventType.typeFromString(splitted[1]);
		long time = Long.parseLong(splitted[2]);
		return new CriticalSectionEvent(time, nodeId, type);
	}

	@Override
	public int compareTo(CriticalSectionEvent o) {
		return Long.compare(this.time, o.time);
	}
	
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public CriticalSectionEventType getType() {
		return type;
	}

	public void setType(CriticalSectionEventType type) {
		this.type = type;
	}

}
