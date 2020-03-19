package com.ssdd.util;

import java.util.List;
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
	 * @return positive random number containted in (min, max) interval. 
	 * 
	 * */
	 public static long randomBetweenInterval(Random r, long min, long max) {
		 long random = r.nextLong();
		 random *= (random >= 0) ? 1 : -1;
		 long number = (min + (random % (max - min + 1)));
		 return number;
	 }
	 
	 
	 public static String listToString(List<String> lines) {
		 StringBuilder sb = new StringBuilder();
		 for(String line : lines) {
			 sb.append(line);
		 }
		 return sb.toString();
	 }
}
