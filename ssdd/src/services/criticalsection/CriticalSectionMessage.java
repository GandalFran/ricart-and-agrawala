package services.criticalsection;

import com.google.gson.Gson;

public class CriticalSectionMessage{
	
	private String processId;
	private long lamportTime;
	private CriticalSectionMessageType messageType;
	
	public CriticalSectionMessage(String processId, long lamportTime, CriticalSectionMessageType messageType) {
		super();
		this.processId = processId;
		this.lamportTime = lamportTime;
		this.messageType = messageType;
	}

	public boolean hasPriority(String otherId, long otherLamport) {
		return ( (this.lamportTime > otherLamport) || 
				 ((this.lamportTime == otherLamport) && (this.processId.compareTo(otherId) < 0))
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
	public long getLamportTime() {
		return lamportTime;
	}
	public void setLamportTime(long lamportTime) {
		this.lamportTime = lamportTime;
	}
}
