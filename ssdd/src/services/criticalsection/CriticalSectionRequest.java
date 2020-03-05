package services.criticalsection;

public class CriticalSectionRequest{
	
	private String processId;
	private long lamportTime;
	
	public CriticalSectionRequest(String processId, long lamportTime) {
		super();
		this.processId = processId;
		this.lamportTime = lamportTime;
	}

	public boolean hasPriority(CriticalSectionRequest o) {
		return ( (this.lamportTime > o.lamportTime) || 
				 ((this.lamportTime == o.lamportTime) && (this.processId.compareTo(o.processId) < 0))
				);
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
