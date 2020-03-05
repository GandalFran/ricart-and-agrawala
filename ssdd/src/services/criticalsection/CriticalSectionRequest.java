package services.criticalsection;

import com.google.gson.Gson;

public class CriticalSectionRequest{
	
	private String processId;
	private long lamportTime;
	
	public CriticalSectionRequest(String processId, long lamportTime) {
		super();
		this.processId = processId;
		this.lamportTime = lamportTime;
	}

	public boolean hasPriority(String otherId, long otherLamport) {
		return ( (this.lamportTime > otherLamport) || 
				 ((this.lamportTime == otherLamport) && (this.processId.compareTo(otherId) < 0))
				);
	}
	
    public String toJson(){
        return new Gson().toJson(this);
    }

    public static CriticalSectionRequest fromJson(String data){
        return new Gson().fromJson(data, CriticalSectionRequest.class);
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
