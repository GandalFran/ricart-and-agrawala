package com.ssdd.util;

import java.util.Random;

/**
 * collection of uitilites
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
 * */
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
	 * @return positive random number containted in (min, max) interval. 
	 * 
	 * */
	 public static long randomBetweenInterval(Random r, long min, long max) {
		 long random = r.nextInt((int)(max - min + 1));
		 random *= (random >= 0) ? 1 : -1;
		 long number = min + random;
		 return number;
	 }
}
