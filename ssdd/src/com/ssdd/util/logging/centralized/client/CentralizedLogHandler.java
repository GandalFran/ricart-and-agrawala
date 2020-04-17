package com.ssdd.util.logging.centralized.client;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.ssdd.util.constants.ILoggingConstants;
import com.ssdd.util.logging.centralized.service.CentralizedLogService;
import com.ssdd.util.logging.centralized.service.CentralizedLogServiceProxy;

/** 
 * {@link java.util.logging.Handler} to log the information to the centralized server
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public class CentralizedLogHandler extends Handler{

	/**
	 * Proxy to access the centralzied log service configured in {@link com.ssdd.util.constants.ILoggingConstants#CENTRALIZED_LOG_IP}.
	 * */
	private CentralizedLogService service;
	
	public CentralizedLogHandler(){
		this.service = CentralizedLogService.buildProxy(ILoggingConstants.CENTRALIZED_LOG_IP);
	}
	
	/**
	 * Checks if service is available.
	 * 
	 * @see com.ssdd.util.logging.centralized.service.CentralizedLogService#isAvailable()
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return boolean indicating if the server is suitable for logging.
	 * */
	public boolean isServerAvailable() {
		try {
			String isAvailableResponse = this.service.isAvailable();
			return CentralizedLogServiceProxy.parseIsAvailableResponse(isAvailableResponse);
		}catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}
	
	/**
	 * Sends to the centralized log service the information to write to centralized log.
	 * 
	 * @see com.ssdd.util.logging.centralized.service.CentralizedLogService#log(String)
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param arg0 record to persist in the centralized log service
	 * */
	@Override
	public void publish(LogRecord arg0) {
		String line = this.getFormatter().format(arg0);
		this.service.log(line);
	}

}
