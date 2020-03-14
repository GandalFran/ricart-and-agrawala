package com.ssdd.cs.bean;

/** 
 * possible states for the critical section.
 * 
 * @version 1.0
 * @author Héctor Sánchez San Blas
 * @author Francisco Pinto Santos
*/
public enum CriticalSectionState {
	/**
	 * the node is not trying to acces the critical section
	 * */
	FREE, 
	/**
	 * the node is trying to acces the critical section
	 * */
	REQUESTED, 
	/**
	 * the node has the property of the critical section
	 * */
	ACQUIRED
}
