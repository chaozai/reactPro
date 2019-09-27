/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package com.shiyan.main;

import java.util.ArrayList;
import java.util.List;

import com.shiyan.models.Cloudlet;

/**
 * CloudletScheduler is an abstract class that represents the policy of scheduling performed by a
 * virtual machine to run its {@link Cloudlet Cloudlets}. CloudletScheduler是一个抽象类，它表示由a执行的调度策�?
 * So, classes extending this must execute Cloudlets. Also, the interface forcloudlet管理也在这个类中实现�?
 * cloudlet management is also implemented in this class.每个VM都必须有自己的CloudletScheduler实例�?
 * Each VM has to have its own instance of a CloudletScheduler.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class CloudletScheduler {

	/** The previous time. 当前时间*/
	private double previousTime;

	/** The list of current mips share available for the VM using the scheduler. 使用调度器的VM可用的当前mips共享列表*/
	private List<Double> currentMipsShare;
	
	private double currentWrongProbability;
	
	/** The list of cloudlet waiting to be executed on the VM. 等待在VM上执行的cloudlet列表*/
	protected List<? extends ResCloudlet> cloudletWaitingList;

	/** The list of cloudlets being executed on the VM. 在VM上执行的cloudlets列表*/
	protected List<? extends ResCloudlet> cloudletExecList;

	/** The list of paused cloudlets. 暂停的cloudlets列表*/
	protected List<? extends ResCloudlet> cloudletPausedList;

	/** The list of finished cloudlets. 完成了的cloudlets列表*/
	protected List<? extends ResCloudlet> cloudletFinishedList;

	/** The list of failed cloudlets. 失败的cloudlets列表*/
	protected List<? extends ResCloudlet> cloudletFailedList;

	/**
	 * Creates a new CloudletScheduler object. 创建�?个新的CloudletScheduler对象
         * A CloudletScheduler must be created before starting the actual simulation.
	 * 在开始实际模拟之前，必须创建�?个CloudletScheduler
	 * @pre $none
	 * @post $none
	 */
	public CloudletScheduler() {
		setPreviousTime(0.0);
		cloudletWaitingList = new ArrayList<ResCloudlet>();
		cloudletExecList = new ArrayList<ResCloudlet>();
		cloudletPausedList = new ArrayList<ResCloudlet>();
		cloudletFinishedList = new ArrayList<ResCloudlet>();
		cloudletFailedList = new ArrayList<ResCloudlet>();
	}

	/**
	 * Updates the processing of cloudlets running under management of this scheduler.
	 * 更新在此调度器管理下运行的cloudlets的处
	 * @param currentTime current simulation time
	 * @param mipsShare list with MIPS share of each Pe available to the scheduler
	 * @return the predicted completion time of the earliest finishing cloudlet, 
         * or 0 if there is no next events
	 * @pre currentTime >= 0
	 * @post $none
	 */
	public abstract double updateVmProcessing(double currentTime, List<Double> mipsShare);

	/**
	 * Receives an cloudlet to be executed in the VM managed by this scheduler.
	 * 接收一个将要在此调度程序管理的VM中执行的cloudlet?
	 * @param gl the submited cloudlet (@todo it's a strange param name)
	 * @param fileTransferTime time required to move the required files from the SAN to the VM
	 * @return expected finish time of this cloudlet, or 0 if it is in a waiting queue
	 * @pre gl != null
	 * @post $none
	 */
	public abstract double cloudletSubmit(Cloudlet gl, double fileTransferTime);
	
	public abstract double cloudletExpectedExecTime(Cloudlet gl, double fileTransferTime);
	/**
	 * Receives an cloudlet to be executed in the VM managed by this scheduler.
	 * 
	 * @param gl the submited cloudlet
	 * @return expected finish time of this cloudlet, or 0 if it is in a waiting queue
	 * @pre gl != null
	 * @post $none
	 */
	public abstract double cloudletSubmit(Cloudlet gl);

	/**
	 * Cancels execution of a cloudlet. 取消执行cloudlet
	 * 
	 * @param clId ID of the cloudlet being canceled
	 * @return the canceled cloudlet, $null if not found
	 * @pre $none
	 * @post $none
	 */
	public abstract Cloudlet cloudletCancel(int clId);

	/**
	 * Pauses execution of a cloudlet. 暂停执行cloudlet
	 * 
	 * @param clId ID of the cloudlet being paused
	 * @return $true if cloudlet paused, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean cloudletPause(int clId);

	/**
	 * Resumes execution of a paused cloudlet.重新执行暂停的cloudlet�?
	 * 
	 * @param clId ID of the cloudlet being resumed
	 * @return expected finish time of the cloudlet, 0.0 if queued
	 * @pre $none
	 * @post $none
	 */
	public abstract double cloudletResume(int clId);

	/**
	 * Processes a finished cloudlet. 处理�?个完成的cloudlet�?
	 * 
	 * @param rcl finished cloudlet
	 * @pre rgl != $null
	 * @post $none
	 */
	public abstract void cloudletFinish(ResCloudlet rcl);
	public abstract void cloudletFailed(ResCloudlet rcl);
	/**
	 * Gets the status of a cloudlet.获得云任务的状�??
	 * 
	 * @param clId ID of the cloudlet
	 * @return status of the cloudlet, -1 if cloudlet not found
	 * @pre $none
	 * @post $none
         * 
         * @todo cloudlet status should be an enum
	 */
	public abstract int getCloudletStatus(int clId);

	/**
	 * Informs if there is any cloudlet that finished to execute in the VM managed by this scheduler.
	 * 通知此调度程序管理的VM中是否有任何完成执行的cloudlet�?
	 * @return $true if there is at least one finished cloudlet; $false otherwise
	 * @pre $none
	 * @post $none
         * @todo the method name would be isThereFinishedCloudlets to be clearer
	 */
	public abstract boolean isFinishedCloudlets();

	/**
	 * Returns the next cloudlet in the finished list.
	 * 返回完成列表中的下一个cloudlet�?
	 * @return a finished cloudlet or $null if the respective list is empty
	 * @pre $none
	 * @post $none
	 */
	public abstract Cloudlet getNextFinishedCloudlet();

	/**
	 * Returns the number of cloudlets running in the virtual machine.
	 * 返回虚拟机中运行的cloudlets的数量�??
	 * @return number of cloudlets running
	 * @pre $none
	 * @post $none
	 */
	public abstract int runningCloudlets();

	/**
	 * Returns one cloudlet to migrate to another vm.
	 * 返回�?个cloudlet以迁移到另一个vm
	 * @return one running cloudlet
	 * @pre $none
	 * @post $none
	 */
	public abstract Cloudlet migrateCloudlet();

	/**
	 * Gets total CPU utilization percentage of all cloudlets, according to CPU UtilizationModel of 
     * each one.
	 * 根据每一个云任务CPU利用率模型得到所有cloudlets的CPU利用率百分比
	 * @param time the time to get the current CPU utilization
	 * @return total utilization
	 */
	public abstract double getTotalUtilizationOfCpu(double time);
    
	public abstract double getTotalUtilizationOfRam(double time);
	/**
	 * Gets the current requested mips.
	 * 获取当前请求的mips
	 * @return the current mips
	 */
	public abstract List<Double> getCurrentRequestedMips();

	/**
	 * Gets the total current available mips for the Cloudlet.
	 * 获取Cloudlet的当前可用mips总量
	 * @param rcl the rcl
	 * @param mipsShare the mips share
	 * @return the total current mips
         * @todo In fact, this method is returning different data depending 
         * of the subclass. It is expected that the way the method use to compute
         * the resulting value can be different in every subclass,
         * but is not supposed that each subclass returns a complete different 
         * result for the same method of the superclass.
         * In some class such as {@link NetworkCloudletSpaceSharedScheduler},
         * the method returns the average MIPS for the available PEs,
         * in other classes such as {@link CloudletSchedulerDynamicWorkload} it returns
         * the MIPS' sum of all PEs.
	 */
	public abstract double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare);

	/**
	 * Gets the total current requested mips for a given cloudlet.
	 * 获取给定cloudlet的当前请求mips总数�?
	 * @param rcl the rcl
	 * @param time the time
	 * @return the total current requested mips for the given cloudlet
	 */
	public abstract double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, double time);

	/**
	 * Gets the total current allocated mips for cloudlet.
	 * 获取为cloudlet分配的�?�当前mips�?
	 * @param rcl the rcl
	 * @param time the time
	 * @return the total current allocated mips for cloudlet
	 */
	public abstract double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time);

	/**
	 * Gets the current requested ram.
	 * 
	 * @return the current requested ram
	 */
	public abstract double getCurrentRequestedUtilizationOfRam();

	/**
	 * Gets the current requested bw.
	 * 
	 * @return the current requested bw
	 */
	public abstract double getCurrentRequestedUtilizationOfBw();

	/**
	 * Gets the previous time.获取之前的时间�??
	 * 
	 * @return the previous time
	 */
	public double getPreviousTime() {
		return previousTime;
	}

	/**
	 * Sets the previous time.
	 * 
	 * @param previousTime the new previous time
	 */
	protected void setPreviousTime(double previousTime) {
		this.previousTime = previousTime;
	}

	/**
	 * Sets the current mips share.
	 * 
	 * @param currentMipsShare the new current mips share
	 */
	protected void setCurrentMipsShare(List<Double> currentMipsShare) {
		this.currentMipsShare = currentMipsShare;
	}

	/**
	 * Gets the current mips share.
	 * 
	 * @return the current mips share
	 */
	public List<Double> getCurrentMipsShare() {
		return currentMipsShare;
	}

	/**
	 * Gets the cloudlet waiting list.
	 *     获取cloudlet等待列表
	 * @param <T> the generic type
	 * @return the cloudlet waiting list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResCloudlet> List<T> getCloudletWaitingList() {
		return (List<T>) cloudletWaitingList;
	}

	/**
	 * Cloudlet waiting list.云任务等待列�?
	 * 
	 * @param <T> the generic type
	 * @param cloudletWaitingList the cloudlet waiting list
	 */
	protected <T extends ResCloudlet> void setCloudletWaitingList(List<T> cloudletWaitingList) {
		this.cloudletWaitingList = cloudletWaitingList;
	}

	/**
	 * Gets the cloudlet exec list.
	 * 获取cloudlet执行列表�?
	 * @param <T> the generic type
	 * @return the cloudlet exec list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResCloudlet> List<T> getCloudletExecList() {
		return (List<T>) cloudletExecList;
	}

	/**
	 * Sets the cloudlet exec list.
	 * 设置cloudlet执行列表
	 * @param <T> the generic type
	 * @param cloudletExecList the new cloudlet exec list
	 */
	protected <T extends ResCloudlet> void setCloudletExecList(List<T> cloudletExecList) {
		this.cloudletExecList = cloudletExecList;
	}

	/**
	 * Gets the cloudlet paused list.
	 * 获取cloudlet暂停列表�?
	 * @param <T> the generic type
	 * @return the cloudlet paused list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResCloudlet> List<T> getCloudletPausedList() {
		return (List<T>) cloudletPausedList;
	}

	/**
	 * Sets the cloudlet paused list.
	 * 设置cloudlet暂停列表�?
	 * @param <T> the generic type
	 * @param cloudletPausedList the new cloudlet paused list
	 */
	protected <T extends ResCloudlet> void setCloudletPausedList(List<T> cloudletPausedList) {
		this.cloudletPausedList = cloudletPausedList;
	}

	/**
	 * Gets the cloudlet finished list.
	 * 获取完成的cloudlet列表�?
	 * @param <T> the generic type
	 * @return the cloudlet finished list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResCloudlet> List<T> getCloudletFinishedList() {
		return (List<T>) cloudletFinishedList;
	}

	/**
	 * Sets the cloudlet finished list.
	 * 设置cloudlet完成列表
	 * @param <T> the generic type
	 * @param cloudletFinishedList the new cloudlet finished list
	 */
	protected <T extends ResCloudlet> void setCloudletFinishedList(List<T> cloudletFinishedList) {
		this.cloudletFinishedList = cloudletFinishedList;
	}

	/**
	 * Gets the cloudlet failed list.
	 * 获得cloudlet失败列表
	 * @param <T> the generic type
	 * @return the cloudlet failed list.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResCloudlet> List<T>  getCloudletFailedList() {
		return (List<T>) cloudletFailedList;
	}

	/**
	 * Sets the cloudlet failed list.
	 * 设置cloudlet失败列表
	 * @param <T> the generic type
	 * @param cloudletFailedList the new cloudlet failed list.
	 */
	protected <T extends ResCloudlet> void setCloudletFailedList(List<T> cloudletFailedList) {
		this.cloudletFailedList = cloudletFailedList;
	}
    /**
          * 得到当前的虚拟机错误率
     * @return
     */
	public double getCurrentWrongProbability() {
		return currentWrongProbability;
	}

	public void setCurrentWrongProbability(double currentWrongProbability) {
		this.currentWrongProbability = currentWrongProbability;
	}

}
