package com.ssdd.util;

import java.util.Random;

public class Utils {
 
	/**
	 * generates a random number between a given interval.
	 * 
	 * @version 1.0
	 * @author Héctor Sánchez San Blas
	 * @author Francisco Pinto Santos
	 * 
	 * @param r random number generator
	 * @param min interval start
	 * @param max interval end
	 * 
	 * @return random number containted in (min, max) interval. 
	 * 
	 * */
	 public static long randomBetweenInterval(Random r, long min, long max) {
		 return (min + (r.nextLong() % (max - min + 1)));
	 }
}
