package com.ssdd.util.logging;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SSDDLogFactory {
	// src: https://www.logicbig.com/tutorials/core-java-tutorial/logging/customizing-default-format.html
	
	/**
	 * buils a logger for a given class.
	 * @see https://www.logicbig.com/tutorials/core-java-tutorial/logging/customizing-default-format.html
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param c class which the name for the logger will be taken from
	 * 
	 * @return a java.util.logging.Logger object with the setted Formatter and Handler.
	 * */
	public static Logger logger(Class<?> c) {
		// generate log name
		String className = c.getClass().getPackage().getName() + "." + c.getClass().getName();

		// generate handler and formatter
		Handler handler = SSDDLogFactory.buildHandler();
	    handler.setFormatter(SSDDLogFactory.buildFormatter());
	    
	    // create and configure log
		Logger log = Logger.getLogger(className);
	    log.setUseParentHandlers(false);
		log.addHandler(handler);
	    
		return log;
	}
	
	/**
	 * buils a Handler for the logger.
	 * @see https://www.logicbig.com/tutorials/core-java-tutorial/logging/customizing-default-format.html
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return a java.util.logging.Handler for the configured medium (currently console).
	 * */
	private static Handler buildHandler() {
		return new ConsoleHandler(); 
	}
	
	/**
	 * buils a Formatter for the logger.
	 * @see https://www.logicbig.com/tutorials/core-java-tutorial/logging/customizing-default-format.html
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return a java.util.logging.SimpleFormatter object with the selected format.
	 * */
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
