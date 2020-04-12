package com.ssdd.ntp.client;

import com.ssdd.ntp.service.NTPService;
import com.ssdd.util.constants.INtpConstants;

public class NTPMaxFailsReachedException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public NTPMaxFailsReachedException(NTPService service) {
		super("NTP service " + service.toString() + " not reachable, max failed attemps (" + INtpConstants.MAX_FAILED_ATTEMPTS + ") reached");
	}

}
