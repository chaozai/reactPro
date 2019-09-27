/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package com.shiyan.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.shiyan.core.CloudSim;
import com.shiyan.lists.PeList;
import com.shiyan.main.Log;
import com.shiyan.main.Pe;
import com.shiyan.main.VmScheduler;
import com.shiyan.provisioners.BwProvisioner;
import com.shiyan.provisioners.RamProvisioner;

/**
 * A host supporting WrongProbability and performance degradation.
 * 支持动态错误率和性能退化的主机
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class HostDynamicWrongProbability extends Host {

	/** The utilization mips. */
	private double utilizationMips;
	
	private double hostWrong;
	
	private Integer vmMigrationSize;
	
	private double previousHostWrong;
	
	private Integer previousVmMigrationSize;
	
	private double hostWrongProbability;

	/** The previous utilization mips. */
	private double previousHostWrongProbability;

	/** The previous utilization mips. */
	private double previousUtilizationMips;
	
	/** The host utilization state history. */
	private final List<HostStateHistoryEntry> stateHistory = new LinkedList<HostStateHistoryEntry>();

	/**
	 * Instantiates a new host.
	 * 
	 * @param id the id
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage capacity
	 * @param peList the host's PEs list
	 * @param vmScheduler the VM scheduler
	 */
	public HostDynamicWrongProbability(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		
		setVmMigrationSize(0);
		setPreviousVmMigrationSize(0);
		setHostWrong(0);
		setUtilizationMips(0);
		setHostWrongProbability(0);
		setPreviousHostWrongProbability(0);
		setPreviousUtilizationMips(0);
		setPreviousHostWrong(0);
	}

	@Override
	public double updateVmsProcessing(double currentTime) {
		
		setPreviousVmMigrationSize(getVmMigrationSize());
		
		double smallerTime = super.updateVmsProcessing(currentTime);
		
		setPreviousHostWrongProbability(getHostWrongProbability());
		setPreviousUtilizationMips(getUtilizationMips());
		setPreviousHostWrong(getPreviousHostWrong());
		
		setUtilizationMips(0);
		setHostWrong(0);
		
		double hostTotalRequestedMips = 0;
		
		for (Vm vm : getVmList()) {
			getVmScheduler().deallocatePesForVm(vm);
		}

		for (Vm vm : getVmList()) {
			getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips());
		}

		for (Vm vm : getVmList()) {
			double totalRequestedMips = vm.getCurrentRequestedTotalMips();
			double totalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);
			double currentwrongProbability = vm.getCloudletScheduler().getCurrentWrongProbability();
			
			if (!Log.isDisabled()) {
				Log.formatLine(
						"%.2f: [Host #" + getId() + "] Total allocated MIPS for VM #" + vm.getId()
								+ " (Host #" + vm.getHost().getId()
								+ ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)"+" 错误率为：%.2f%%",
						CloudSim.clock(),
						totalAllocatedMips,
						totalRequestedMips,
						vm.getMips(),
						totalRequestedMips / vm.getMips() * 100, currentwrongProbability);

				List<Pe> pes = getVmScheduler().getPesAllocatedForVM(vm);
				StringBuilder pesString = new StringBuilder();
				for (Pe pe : pes) {
					pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getPeProvisioner()
							.getTotalAllocatedMipsForVm(vm)));
				}
				Log.formatLine(
						"%.2f: [Host #" + getId() + "] MIPS for VM #" + vm.getId() + " by PEs ("
								+ getNumberOfPes() + " * " + getVmScheduler().getPeCapacity() + ")."
								+ pesString,
						CloudSim.clock());
			}

			if (getVmsMigratingIn().contains(vm)) {
				Log.formatLine("%.2f: [Host #" + getId() + "] VM #" + vm.getId()
						+ " is being migrated to Host #" + getId(), CloudSim.clock());
			} else {
				if (totalAllocatedMips + 0.1 < totalRequestedMips) {
					Log.formatLine("%.2f: [Host #" + getId() + "] Under allocated MIPS for VM #" + vm.getId()
							+ ": %.2f", CloudSim.clock(), totalRequestedMips - totalAllocatedMips);
				}

				vm.addStateHistoryEntry(
						currentTime,
						totalAllocatedMips,
						totalRequestedMips,
						currentwrongProbability,
						(vm.isInMigration() && !getVmsMigratingIn().contains(vm)));

				if (vm.isInMigration()) {
					Log.formatLine(
							"%.2f: [Host #" + getId() + "] VM #" + vm.getId() + " is in migration",
							CloudSim.clock());
					totalAllocatedMips /= 0.9; // performance degradation due to migration - 10%由于迁移造成的性能下降
				}
			}
			setUtilizationMips(getUtilizationMips() + totalAllocatedMips);
			setHostWrong(getHostWrong() + currentwrongProbability*100);
			
			hostTotalRequestedMips += totalRequestedMips;

		}
        int currentVmMigrationSize = getVmScheduler().getVmsMigrationSize();
		setHostWrongProbability(getHostWrong()/getVmList().size()*100);
		setVmMigrationSize(currentVmMigrationSize);
		
		addStateHistoryEntry(
				currentTime,
				getUtilizationMips(),
				hostTotalRequestedMips,
				getHostWrong(),
				getVmMigrationSize(),
				(getUtilizationMips() > 0));

		return smallerTime;
	}

	/**
	 * Gets the list of completed vms.
	 * 
	 * @return the completed vms
	 */
	public List<Vm> getCompletedVms() {
		List<Vm> vmsToRemove = new ArrayList<Vm>();
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				continue;
			}
			if (vm.getCurrentRequestedTotalMips() == 0) {
				vmsToRemove.add(vm);
			}
		}
		return vmsToRemove;
	}

	/**
	 * Gets the max utilization percentage among by all PEs.
	 * 
	 * @return the maximum utilization percentage
	 */
	public double getMaxUtilization() {
		return PeList.getMaxUtilization(getPeList());
	}

	/**
	 * Gets the max utilization percentage among by all PEs allocated to a VM.
	 * 
	 * @param vm the vm
	 * @return the max utilization percentage of the VM
	 */
	public double getMaxUtilizationAmongVmsPes(Vm vm) {
		return PeList.getMaxUtilizationAmongVmsPes(getPeList(), vm);
	}

	/**
	 * Gets the utilization of memory (in absolute values).
	 * 
	 * @return the utilization of memory
	 */
	public double getUtilizationOfRam() {
		return getRamProvisioner().getUsedRam();
	}

	/**
	 * Gets the utilization of bw (in absolute values).
	 * 
	 * @return the utilization of bw
	 */
	public double getUtilizationOfBw() {
		return getBwProvisioner().getUsedBw();
	}

	/**
	 * Get current utilization of CPU in percentage.
	 * 得到此主机当前的cpu利用率百分比
	 * @return current utilization of CPU in percents
	 */
	public double getUtilizationOfCpu() {
		double utilization = getUtilizationMips() / getTotalMips();
		if (utilization > 1 && utilization < 1.01) {
			utilization = 1;
		}
		return utilization;
	}
	/**
	 * Get current utilization of CPU in percentage.
	 * 得到此主机当前的cpu利用率百分比
	 * @return current utilization of CPU in percents
	 */
	
	/**
	 * Gets the previous utilization of CPU in percentage.
	 * 得到之前的cpu利用率百分比
	 * @return the previous utilization of cpu in percents
	 */
	public double getPreviousUtilizationOfCpu() {
		double utilization = getPreviousUtilizationMips() / getTotalMips();
		if (utilization > 1 && utilization < 1.01) {
			utilization = 1;
		}
		return utilization;
	}

	/**
	 * Get current utilization of CPU in MIPS.
	 * 
	 * @return current utilization of CPU in MIPS
         * @todo This method only calls the  {@link #getUtilizationMips()}.
         * getUtilizationMips may be deprecated and its code copied here.
	 */
	public double getUtilizationOfCpuMips() {
		return getUtilizationMips();
	}

	/**
	 * Gets the utilization of CPU in MIPS.
	 * 获取MIPS中的CPU利用率
	 * @return current utilization of CPU in MIPS
	 */
	public double getUtilizationMips() {
		return utilizationMips;
	}

	/**
	 * Sets the utilization mips.
	 * 
	 * @param utilizationMips the new utilization mips
	 */
	
	protected void setUtilizationMips(double utilizationMips) {
		this.utilizationMips = utilizationMips;
	}

	/**
	 * Gets the host state history.
	 * 
	 * @return the state history
	 */
	public List<HostStateHistoryEntry> getStateHistory() {
		return stateHistory;
	}

	/**
	 * Adds a host state history entry.
	 * 
	 * @param time the time
	 * @param allocatedMips the allocated mips
	 * @param requestedMips the requested mips
	 * @param isActive the is active
	 */
	public
			void
			addStateHistoryEntry(double time, double allocatedMips, double requestedMips, double wrongProbability, int vmMigrationsize, boolean isActive) {

		HostStateHistoryEntry newState = new HostStateHistoryEntry(
				time,
				allocatedMips,
				requestedMips,
				wrongProbability,
				vmMigrationsize,
				isActive);
		if (!getStateHistory().isEmpty()) {
			HostStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
			if (previousState.getTime() == time) {
				getStateHistory().set(getStateHistory().size() - 1, newState);
				return;
			}
		}
		getStateHistory().add(newState);
	}

	public void setTotalWrongProbability(double hostwrongProbability) {
		this.hostWrongProbability = hostwrongProbability;
	}

	public double getPreviouswrongProbability() {
		return previousHostWrongProbability;
	}

	public void setPreviouswrongProbability(double previoushostwrongProbability) {
		this.previousHostWrongProbability = previoushostwrongProbability;
	}

	public double getPreviousUtilizationMips() {
		return previousUtilizationMips;
	}

	public void setPreviousUtilizationMips(double previousUtilizationMips) {
		this.previousUtilizationMips = previousUtilizationMips;
	}

	public double getHostWrongProbability() {
		return hostWrongProbability;
	}

	public void setHostWrongProbability(double hostWrongProbability) {
		this.hostWrongProbability = hostWrongProbability;
	}

	public double getPreviousHostWrongProbability() {
		return previousHostWrongProbability;
	}

	public void setPreviousHostWrongProbability(double previousHostWrongProbability) {
		this.previousHostWrongProbability = previousHostWrongProbability;
	}

	public double getHostWrong() {
		return hostWrong;
	}

	public void setHostWrong(double hostWrong) {
		this.hostWrong = hostWrong;
	}

	public double getPreviousHostWrong() {
		return previousHostWrong;
	}

	public void setPreviousHostWrong(double previousHostWrong) {
		this.previousHostWrong = previousHostWrong;
	}

	public Integer getVmMigrationSize() {
		return vmMigrationSize;
	}

	public void setVmMigrationSize(Integer vmMigrationSize) {
		this.vmMigrationSize = vmMigrationSize;
	}

	public Integer getPreviousVmMigrationSize() {
		return previousVmMigrationSize;
	}

	public void setPreviousVmMigrationSize(Integer previousVmMigrationSize) {
		this.previousVmMigrationSize = previousVmMigrationSize;
	}


}
