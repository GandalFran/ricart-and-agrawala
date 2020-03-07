package com.ssdd.util.logging;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SSDDLogFactory {
	// src: https://www.logicbig.com/tutorials/core-java-tutorial/logging/customizing-default-format.html

	public static Logger logger(Class<?> c) {
		// generate log name
		String className = c.getClass().getPackage().getName() + "." + c.getClass().getName();

		// generate handler and formatter
		ConsoleHandler handler = new ConsoleHandler();
	    handler.setFormatter(SSDDLogFactory.buildFormatter());
	    
	    // create and configure log
		Logger log = Logger.getLogger(className);
	    log.setUseParentHandlers(false);
		log.addHandler(handler);
	    
		return log;
	}
	
	private static SimpleFormatter buildFormatter() {
		return new SimpleFormatter() {
	          private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

	          @Override
	          public synchronized String format(LogRecord lr) {
	              return String.format(format,
	                      new Date(lr.getMillis()),
	                      lr.getLevel().getLocalizedName(),
	                      lr.getMessage()
	              );
	          }
	      };
		
	}
	
}
