package com.ssdd.util.logging.centralized;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.ssdd.util.constants.ILoggingConstants;

public class CentralizedLogHandler extends Handler{

	private CentralizedLogService service;
	
	public CentralizedLogHandler(){
		this.service = CentralizedLogService.buildProxy(ILoggingConstants.CENTRALIZED_LOG_IP);
	}
	
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

	@Override
	public void publish(LogRecord arg0) {
		String line = this.getFormatter().format(arg0);
		this.service.log(line);
	}

}
