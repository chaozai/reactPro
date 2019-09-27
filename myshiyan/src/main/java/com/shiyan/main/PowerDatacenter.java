/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package com.shiyan.main;

import java.util.List;
import java.util.Map;

import com.shiyan.core.CloudSim;
import com.shiyan.core.CloudSimTags;
import com.shiyan.core.SimEvent;
import com.shiyan.core.predicates.PredicateType;
import com.shiyan.models.PowerHost;
import com.shiyan.models.Vm;

public class PowerDatacenter extends Datacenter {

	/** The datacenter consumed power. */
	private double power;
	
	private double datacenterWrongProbability;
	
	/** Indicates if migrations are disabled or not. 指示是否禁用迁移*/
	private boolean disableMigrations;

	/** The last time submitted cloudlets were processed. */
	private double cloudletSubmitted;

	/** The VM migration count. */
	private int migrationCount;

	/**
	 * Instantiates a new PowerDatacenter.
	 * 
	 * @param name the datacenter name
	 * @param characteristics the datacenter characteristics
	 * @param schedulingInterval the scheduling interval
	 * @param vmAllocationPolicy the vm provisioner
	 * @param storageList the storage list
	 * @throws Exception the exception
	 */
	public PowerDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		setPower(0.0);
		setDatacenterWrongProbability(0.0);
		setDisableMigrations(false);
		setCloudletSubmitted(-1);
		setMigrationCount(0);
	}

	@Override
	protected void updateCloudletProcessing() {
		
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			return;
		}
		double currentTime = CloudSim.clock();

		// if some time passed since last processing
		if (currentTime > getLastProcessTime()) {
			System.out.print(currentTime + " ");

			double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

			//isDisableMigrations()中，不允许迁移，则返回true，允许则返回false
			//若允许迁移则开始执行以下步骤
			if (!isDisableMigrations()) {
				
				List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(
						getVmList());

				if (migrationMap != null) {
					for (Map<String, Object> migrate : migrationMap) {
						Vm vm = (Vm) migrate.get("vm");
						PowerHost targetHost = (PowerHost) migrate.get("host");
						PowerHost oldHost = (PowerHost) vm.getHost();

						if (oldHost == null) {
							Log.formatLine(
									"%.2f: Migration of VM #%d to Host #%d is started",
									currentTime,
									vm.getId(),
									targetHost.getId());
						} else {
							Log.formatLine(
									"%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
									currentTime,
									vm.getId(),
									oldHost.getId(),
									targetHost.getId());
						}

						targetHost.addMigratingInVm(vm);
						incrementMigrationCount();

						/** VM migration delay = RAM / bandwidth **/
						// we use BW / 2 to model BW available for migration purposes, the other
						// half of BW is for VM communication
						// around 16 seconds for 1024 MB using 1 Gbit/s network
						send(
								getId(),
								vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
								CloudSimTags.VM_MIGRATE,
								migrate);
					}
				}
			}

			// schedules an event to the next time 把活动安排到下次
			if (minTime != Double.MAX_VALUE) {
				CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
				send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			}
			setLastProcessTime(currentTime);
		}
	}

	/**
	 * Update cloudet processing without scheduling future events.
	 * 更新cloudet处理，而不安排将来的事件
	 * @return the double
         * @see #updateCloudetProcessingWithoutSchedulingFutureEventsForce() 
         * @todo There is an inconsistence in the return value of this
         * method with return value of similar methods
         * such as {@link #updateCloudetProcessingWithoutSchedulingFutureEventsForce()},
         * that returns {@link Double#MAX_VALUE} by default.
         * The current method returns 0 by default.
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEvents() {
		if (CloudSim.clock() > getLastProcessTime()) {
			return updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
		return 0;
	}

	/**
	 * Update cloudet processing without scheduling future events.
	  *  在不安排将来的事件的情况下，更新cloudet处理
	 * @return expected time of completion of the next cloudlet in all VMs of all hosts or
	 *         {@link Double#MAX_VALUE} if there is no future events expected in this host
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;

		Log.printLine("\n\n--------------------------------------------------------------\n\n");
		//在300.10开始的时间段内使用新的资源
		Log.formatLine("New resource usage for the time frame starting at %.2f:", currentTime);
		
		for (PowerHost host : this.<PowerHost> getHostList()) {
			Log.printLine();

			double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing通知VMs更新处理，返回任务执行用时最少的执行时间
			
			if (time < minTime) {
				minTime = time;
			}

			Log.formatLine(
					"%.2f: [Host #%d] utilization is %.2f%%,当前错误率为： %.2f%%",",当前发生迁移的数量为：",
					currentTime,
					host.getId(),
					host.getUtilizationOfCpu() * 100, host.getHostWrongProbability(),
					host.getVmScheduler().getVmsMigrationSize());
		}

		if (timeDiff > 0) {
			Log.formatLine(
					"\nEnergy consumption for the last time frame from %.2f to %.2f:",
					getLastProcessTime(),
					currentTime);

			for (PowerHost host : this.<PowerHost> getHostList()) {
				
				double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
				double previousHostWrongProbability = host.getPreviousHostWrongProbability();
				int previousvmmigrationsize = host.getPreviousVmMigrationSize();
				double utilizationOfCpu = host.getUtilizationOfCpu();
				double hostWrongProbability = host.getHostWrongProbability();
				double hostMigrationSize =host.getVmMigrationSize();
				
				double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
						previousUtilizationOfCpu,
						utilizationOfCpu,
						timeDiff);
				timeFrameDatacenterEnergy += timeFrameHostEnergy;

				Log.printLine();
				Log.formatLine(
						"%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
						"错误率是 %.2f%%, now is %.2f%%", "迁移数量 %.2f%%, now is %.2f%%", "错误率增幅：%.2f%%","迁移增幅：%.2f%%",
						currentTime,
						host.getId(),
						getLastProcessTime(),
						previousUtilizationOfCpu * 100,
						utilizationOfCpu * 100,
						previousHostWrongProbability * 100,
						hostWrongProbability* 100,
						previousvmmigrationsize,
						hostMigrationSize,
						hostWrongProbability* 100-previousHostWrongProbability * 100,
						(hostMigrationSize-previousvmmigrationsize)/previousvmmigrationsize*100
						);
				Log.formatLine(
						"%.2f: [Host #%d] energy is %.2f W*sec",
						currentTime,
						host.getId(),
						timeFrameHostEnergy);
				
			}

			Log.formatLine(
					"\n%.2f: Data center's energy is %.2f W*sec\n",
					currentTime,
					timeFrameDatacenterEnergy);
		}

		setPower(getPower() + timeFrameDatacenterEnergy);

		checkCloudletCompletion();

		/** Remove completed VMs **/
		for (PowerHost host : this.<PowerHost> getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
				Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
			}
		}

		Log.printLine();

		setLastProcessTime(currentTime);
		return minTime;
	}

	@Override
	protected void processVmMigrate(SimEvent ev, boolean ack) {
		updateCloudetProcessingWithoutSchedulingFutureEvents();
		super.processVmMigrate(ev, ack);
		SimEvent event = CloudSim.findFirstDeferred(getId(), new PredicateType(CloudSimTags.VM_MIGRATE));
		if (event == null || event.eventTime() > CloudSim.clock()) {
			updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
	}

	@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		super.processCloudletSubmit(ev, ack);
		setCloudletSubmitted(CloudSim.clock());
	}

	/**
	 * Gets the power.
	 * 
	 * @return the power
	 */
	public double getPower() {
		return power;
	}

	/**
	 * Sets the power.
	 * 
	 * @param power the new power
	 */
	protected void setPower(double power) {
		this.power = power;
	}

	/**
	 * Checks if PowerDatacenter is in migration.
	 * 
	 * @return true, if PowerDatacenter is in migration; false otherwise
	 */
	protected boolean isInMigration() {
		boolean result = false;
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if migrations are disabled.
	 * 
	 * @return true, if  migrations are disable; false otherwise
	 */
	public boolean isDisableMigrations() {
		return disableMigrations;
	}

	/**
	 * Disable or enable migrations.
	 * 禁用或启用迁移。
	 * @param disableMigrations true to disable migrations; false to enable
	 */
	public void setDisableMigrations(boolean disableMigrations) {
		this.disableMigrations = disableMigrations;
	}

	/**
	 * Checks if is cloudlet submited.
	 * 检查是否提交了cloudlet。
	 * @return true, if is cloudlet submited
	 */
	protected double getCloudletSubmitted() {
		return cloudletSubmitted;
	}

	/**
	 * Sets the cloudlet submitted.
	 * 
	 * @param cloudletSubmitted the new cloudlet submited
	 */
	protected void setCloudletSubmitted(double cloudletSubmitted) {
		this.cloudletSubmitted = cloudletSubmitted;
	}

	/**
	 * Gets the migration count.
	 * 获取迁移计数
	 * @return the migration count
	 */
	public int getMigrationCount() {
		return migrationCount;
	}

	/**
	 * Sets the migration count.
	 * 
	 * @param migrationCount the new migration count
	 */
	protected void setMigrationCount(int migrationCount) {
		this.migrationCount = migrationCount;
	}

	/**
	 * Increment migration count. 增量迁移数
	 */
	protected void incrementMigrationCount() {
		setMigrationCount(getMigrationCount() + 1);
	}

	public double getDatacenterWrongProbability() {
		return datacenterWrongProbability;
	}

	public void setDatacenterWrongProbability(double datacenterWrongProbability) {
		this.datacenterWrongProbability = datacenterWrongProbability;
	}

}
