package com.ssdd.util.logging;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.ssdd.util.constants.ILoggingConstants;
import com.ssdd.util.logging.centralized.client.CentralizedLogHandler;

/** 
 * Class with static methods to build and configure {@link java.util.logging.Logger}
 * @see <a href="https://www.logicbig.com/tutorials/core-java-tutorial/logging/customizing-default-format.html">https://www.logicbig.com/tutorials/core-java-tutorial/logging/customizing-default-format.html</a> 
 * 
 * @version 1.0
 * @author H�ctor S�nchez San Blas
 * @author Francisco Pinto Santos
*/
public class SSDDLogFactory {
	// src: https://www.logicbig.com/tutorials/core-java-tutorial/logging/customizing-default-format.html
	
	/**
	 * buils a {@link java.util.logging.Logger} for a given class.
	 * 
	 * @version 1.0
	 * @author H�ctor S�nchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param c class which the name for the logger will be taken from
	 * 
	 * @return a {@link java.util.logging.Logger} object with the setted Formatter and Handler.
	 * */
	public static Logger logger(Class<?> c) {
		// generate log name	
		String [] classInfo = c.getCanonicalName().split("\\.");
		String className = classInfo[classInfo.length-1];

		// generate handler and formatter
		Handler handler = SSDDLogFactory.buildHandler();
	    handler.setFormatter(SSDDLogFactory.buildFormatter(className));
	    
	    // create and configure log
		Logger log = Logger.getLogger(className);
		log.addHandler(handler);
	    log.setUseParentHandlers(false);
	    
		return log;
	}
	
	
	/**
	 * buils a Handler for {@link java.util.logging.Logger}.
	 * If the {@link com.ssdd.util.constants.ILoggingConstants#DEBUG} is set to false,
	 * only the warning and error information is logged to console.
	 * If the {@link com.ssdd.util.constants.ILoggingConstants#CENTRALIZED_LOG} is set to true,
	 * a {@link com.ssdd.util.logging.centralized.client.CentralizedLogHandler} is instanced.
	 * 
	 * @version 1.0
	 * @author H�ctor S�nchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @return a {@link java.util.logging.Handler} for the configured medium (currently console).
	 * */
	public static Handler buildHandler() {
		Handler handler = null;
		
		if(ILoggingConstants.CENTRALIZED_LOG) {
			CentralizedLogHandler centralizedHandler = new CentralizedLogHandler();
			if(centralizedHandler.isServerAvailable()) {
				handler = centralizedHandler;
			}else {
				System.err.println("\n\n\n[ERROR] Logging server not available, using local logging\\n\\n\\n");
				handler = new ConsoleHandler();
			}
		}else {
			handler = new ConsoleHandler();
		}
		
		if(! ILoggingConstants.DEBUG) {
			handler.setLevel(Level.WARNING);
		}
		
		return handler; 
	}
	
	/**
	 * buils a Formatter for the {@link java.util.logging.Logger}
	 * 
	 * @version 1.0
	 * @author H�ctor S�nchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param className string containig the name of the class where the logger is instanced
	 * 
	 * @return a {@link java.util.logging.SimpleFormatter} object with the selected format.
	 * */
	public static SimpleFormatter buildFormatter(final String className) {
		return new SimpleFormatter() {
	          private String format = "[%1$tF %1$tT] [%2$-7s] [%3$-4s] [" + className + "]  %4$s %n";
	          
	          @Override
	          public synchronized String format(LogRecord lr) {
	              return String.format(format,
	                      new Date(lr.getMillis()),
	                      lr.getLevel().getLocalizedName(),
	                      Thread.currentThread().getName(),
	                      lr.getMessage()
	              );
	          }
	      };
	}
	
}
