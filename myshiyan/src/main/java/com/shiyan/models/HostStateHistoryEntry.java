/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2011, The University of Melbourne, Australia
 */

package com.shiyan.models;

/**
 * Stores historic data about a host.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.1.2
 */
public class HostStateHistoryEntry {

	/** The time. */
	private double time;

	/** The allocated mips. */
	private double allocatedMips;

	/** The requested mips. */
	private double requestedMips;
    
	/** The requested mips. */
	private double wrongProbability;
	
	private int vmMigrationsize;
	/** Indicates if the host was active in the indicated time. 
         * @see #time
         */
	private boolean isActive;

	/**
	 * Instantiates a new host state history entry.
	 * 
	 * @param time the time
	 * @param allocatedMips the allocated mips
	 * @param requestedMips the requested mips
	 * @param isActive the is active
	 */
	public HostStateHistoryEntry(double time, double allocatedMips, double requestedMips, double wrongProbability, int vmMigrationsize, boolean isActive) {
		setTime(time);
		setAllocatedMips(allocatedMips);
		setRequestedMips(requestedMips);
		setWrongProbability(wrongProbability);
		setVmMigrationsize(vmMigrationsize);
		setActive(isActive);
	}

	/**
	 * Sets the time.
	 * 
	 * @param time the new time
	 */
	protected void setTime(double time) {
		this.time = time;
	}

	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Sets the allocated mips.
	 * 
	 * @param allocatedMips the new allocated mips
	 */
	protected void setAllocatedMips(double allocatedMips) {
		this.allocatedMips = allocatedMips;
	}

	/**
	 * Gets the allocated mips.
	 * 
	 * @return the allocated mips
	 */
	public double getAllocatedMips() {
		return allocatedMips;
	}

	/**
	 * Sets the requested mips.
	 * 
	 * @param requestedMips the new requested mips
	 */
	protected void setRequestedMips(double requestedMips) {
		this.requestedMips = requestedMips;
	}

	/**
	 * Gets the requested mips.
	 * 
	 * @return the requested mips
	 */
	public double getRequestedMips() {
		return requestedMips;
	}

	/**
	 * Sets the active.
	 * 
	 * @param isActive the new active
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * Checks if is active.
	 * 
	 * @return true, if is active
	 */
	public boolean isActive() {
		return isActive;
	}

	public double getWrongProbability() {
		return wrongProbability;
	}

	public void setWrongProbability(double wrongProbability) {
		this.wrongProbability = wrongProbability;
	}

	public int getVmMigrationsize() {
		return vmMigrationsize;
	}

	public void setVmMigrationsize(int vmMigrationsize) {
		this.vmMigrationsize = vmMigrationsize;
	}

}
