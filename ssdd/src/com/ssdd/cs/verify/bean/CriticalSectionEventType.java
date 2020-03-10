package com.ssdd.cs.verify.bean;

/**
 * Represents a event type (in or out) in the critical section on log.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 * */
public enum CriticalSectionEventType {
	IN {
        public String toString() {
            return "E";
        }
    },
	OUT {
        public String toString() {
            return "S";
        }
    };
	
	/**
	 * To deserialize the part of CriticalSectionEventType on the log entries.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param type string with serialized CriticalSectionEventType
	 * 
	 * @return CriticalSectionEventType deserialized
	 * */
	public static CriticalSectionEventType typeFromString(String type) {
		return type.equals("E") ? IN : OUT;
	}
	
}
