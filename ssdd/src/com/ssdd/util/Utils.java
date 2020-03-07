package com.ssdd.util;

import java.util.Random;

public class Utils {
 
	 public static long randomBetweenInterval(Random r, long min, long max) {
		 return (min + (r.nextLong() % (max - min)));
	 }
}
