package com.ssdd.cs.bean;

import com.google.gson.Gson;

/** 
 * types of messages used to synchronize between critical sections.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class CriticalSectionMessage{
	
	/**
	 * the sender's node id
	 * */
	private String senderId;
	/**
	 * the sender's lamport counter to update the reciver counter
	 * */
	private LamportCounter time;
	/**
	 * the message type
	 * */
	private CriticalSectionMessageType messageType;
	
	public CriticalSectionMessage(String processId, long time, CriticalSectionMessageType messageType) {
		super();
		this.senderId = processId;
		this.messageType = messageType;
		this.time = new LamportCounter(time);
	}
	
	public CriticalSectionMessage(String senderId, LamportCounter time, CriticalSectionMessageType messageType) {
		super();
		this.time = time;
		this.senderId = senderId;
		this.messageType = messageType;
	}

	/**
	 * Compares two messages to know if other node has priority over this message.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param otherId other node's id
	 * @param otherTime other node's lamport time counter
	 * 
	 * @return true if the other node has priority over current message
	*/
	public boolean hasPriority(String otherId, LamportCounter otherTime) {
		return ( 
				(this.time.getCounter() > otherTime.getCounter()) 
				|| (
						(this.time.getCounter() == otherTime.getCounter()) 
						&& (this.senderId.compareTo(otherId) < 0)
				   )
			   );
	}

	/** 
	 * Serializes the message to JSON.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return a String with the JSON serialized message.
	 */
    public String toJson(){
        return new Gson().toJson(this);
    }

    /** 
	 * Deserializes the message from JSON.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param data String with a serialized CriticalSectionMessage
	 * 
	 * @return a CriticalSectionMessage instanced with data contained in the JSON.
	 */
    public static CriticalSectionMessage fromJson(String data){
        return new Gson().fromJson(data, CriticalSectionMessage.class);
    }
	
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public LamportCounter getTime() {
		return time;
	}
	public void setTime(LamportCounter time) {
		this.time = time;
	}
}
