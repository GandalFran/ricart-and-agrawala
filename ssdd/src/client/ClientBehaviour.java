package client;

import java.util.Random;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import util.IConstants;

public class ClientBehaviour {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientBehaviour.class);
    

	private Random random;
	
	public ClientBehaviour() {
		this.random = new Random();
	}
	
	
	public void start() {
		
		long sleepIntervalMs = this.generateSleepInterval();
		
		LOGGER.debug("sleep " + sleepIntervalMs + " ms");
		try {
			Thread.sleep(sleepIntervalMs);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
		LOGGER.debug("sleep ended, accessing critical section");
	}
	
	
	private long generateSleepInterval() {
		long randomNumber = this.random.nextLong();
		long sleepIntervalMs = IConstants.MIN_CALCULUS_TIME_MS + ( randomNumber % (IConstants.MAX_CALCULUS_TIME_MS-IConstants.MIN_CALCULUS_TIME_MS));
		return sleepIntervalMs;
	}
}
