package com.ssdd.util.logging.centralized;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.ssdd.util.constants.IConstants;
import com.ssdd.util.logging.SSDDLogFactory;

public class CentralizedLogHandler extends Handler{

	private CentralizedLogService service;
	
	public CentralizedLogHandler(){
		this.service = CentralizedLogService.buildProxy(IConstants.CENTRALIZED_LOG_IP);
	}
	
	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void publish(LogRecord arg0) {
		String line = SSDDLogFactory.buildFormatter(arg0.getLoggerName()).format(arg0);
		this.service.log(line);
	}

}
