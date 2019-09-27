package com.shiyan.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shiyan.models.Host;
import com.shiyan.models.PowerHost;
import com.shiyan.models.Vm;
import com.shiyan.util.ExecutionTimeMeasurer;

public class PowerVmAllocationPolicyWrongProbability extends VmAllocationPolicyAbstract {

	/** The vm selection policy. */
	private PowerVmSelectionPolicy vmSelectionPolicy;

	/** A list of maps between a VM and the host where it is place.
         * @todo This list of map is implemented in the worst way.
         * It should be used just a Map<Vm, Host> to find out 
         * what PM is hosting a given VM.
                  *  映射列表存储虚拟机和主机的对应关系
         */
	private final List<Map<String, Object>> savedAllocation = new ArrayList<Map<String, Object>>();

	/** A map of CPU utilization history (in percentage) for each host,
         where each key is a host id and each value is the CPU utilization percentage history.*/
	private final Map<Integer, List<Double>> utilizationHistory = new HashMap<Integer, List<Double>>();

	/** 
         * The metric history. 
         * @todo the map stores different data. Sometimes it stores the upper threshold,有时它存储上阈值
         * other it stores utilization threshold or predicted utilization, that其他信息存储利用率阈值或预测利用率
         * is very confusing.
         */
	private final Map<Integer, List<Double>> metricHistory = new HashMap<Integer, List<Double>>();

	/** The time when entries in each history list was added. 在每个历史记录列表中添加条目的时间
         * All history lists are updated at the same time.所有历史列表将同时更新
         */
	private final Map<Integer, List<Double>> timeHistory = new HashMap<Integer, List<Double>>();

	/** The history of time spent in VM selection The history of time spent in VM selection
         * every time the optimization of VM allocation method is called. 每次调用VM分配方法的优化
         * @see #optimizeAllocation(java.util.List) 
         */
	private final List<Double> executionTimeHistoryVmSelection = new LinkedList<Double>();

	/** The history of time spent in host selection 在主机选择上花费的时间的历史
         * every time the optimization of VM allocation method is called. 每次调用VM分配方法的优化。
         * @see #optimizeAllocation(java.util.List) 
         */
	private final List<Double> executionTimeHistoryHostSelection = new LinkedList<Double>();

	/** The history of time spent in VM reallocation 用于VM重新分配的时间历史
         * every time the optimization of VM allocation method is called. 每次调用VM分配方法的优化。
         * @see #optimizeAllocation(java.util.List) 
         */
	private final List<Double> executionTimeHistoryVmReallocation = new LinkedList<Double>();

	/** The history of total time spent in every call of the 每次调用最佳VM分配方法的总时间花费历史
         * optimization of VM allocation method. 
         * @see #optimizeAllocation(java.util.List) 
         */
	private final List<Double> executionTimeHistoryTotal = new LinkedList<Double>();

	/**
	 * Instantiates a new PowerVmAllocationPolicyMigrationAbstract.
	 * 
	 * @param hostList the host list
	 * @param vmSelectionPolicy the vm selection policy
	 */
	public PowerVmAllocationPolicyWrongProbability(
			List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy) {
		super(hostList);
		setVmSelectionPolicy(vmSelectionPolicy);
	}

	/**
	 * Optimize allocation of the VMs according to current each host's total wrong probability.
	  *  根据当前各个主机的总错误率为虚拟机选择最佳的分配
	 * @param vmList the vm list
	 * 
	 * @return the array list< hash map< string, object>>
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		
		Map<String, Integer> hostmigrationsize = new HashMap<String, Integer>();
		List<Map<String, Object>> migrationMap = new LinkedList<Map<String,Object>>();
		
		for(PowerHost host: this.<PowerHost> getHostList()) {	
			hostmigrationsize.put(String.valueOf(host.getId()), host.getVmScheduler().getVmsMigrationSize());
		}
		
		ExecutionTimeMeasurer.start("optimizeAllocationTotal");
		
		ExecutionTimeMeasurer.start("optimizeAllocationVmSelection");
		List<? extends Vm> vmsToMigrate = getVmsToMigrateFromHosts(getHostList());//得到所有错误率超标的虚拟机
		
		for(Vm vm: vmsToMigrate) {
			int minmigrationsize = Integer.MAX_VALUE;
			String minmigrationsizehost = null;
			
			for(String key : hostmigrationsize.keySet()){
				if(key.equals(String.valueOf(vm.getHost().getId())))
					continue;
				if(minmigrationsize >= hostmigrationsize.get(key)){
					minmigrationsize=hostmigrationsize.get(key);
					minmigrationsizehost=key;
				}
			}
			if(minmigrationsizehost!=null) {
				Map<String, Object> migrate = new HashMap<String, Object>();
				hostmigrationsize.put(minmigrationsizehost, hostmigrationsize.get(minmigrationsizehost)+1);
				migrate.put("vm", vm);
				migrate.put("host", getHostList().get(Integer.parseInt(minmigrationsizehost)));
				migrationMap.add(migrate);
			}
		}

		getExecutionTimeHistoryHostSelection().add(
				ExecutionTimeMeasurer.end("optimizeAllocationHostSelection"));

		saveAllocation();

		ExecutionTimeMeasurer.start("optimizeAllocationVmSelection");
		
		getExecutionTimeHistoryVmSelection().add(ExecutionTimeMeasurer.end("optimizeAllocationVmSelection"));

		Log.printLine("Reallocation of VMs from the over-utilized hosts:");
		ExecutionTimeMeasurer.start("optimizeAllocationVmReallocation");
		
		getExecutionTimeHistoryVmReallocation().add(
				ExecutionTimeMeasurer.end("optimizeAllocationVmReallocation"));
		Log.printLine();

		restoreAllocation();

		getExecutionTimeHistoryTotal().add(ExecutionTimeMeasurer.end("optimizeAllocationTotal"));

		return migrationMap;
	}

	/**
	 * Finds a PM that has enough resources to host a given VM 为给定的虚拟机找到一个有足够资源的主机
         * The selected host will be that one with most efficient 选择的主机将会是放置该虚拟机后最节能的主机
         * power usage for the given VM.
	 * 
	 * @param vm the VM
	 * @param excludedHosts the excluded hosts
	 * @return the host found to host the VM
	 */
	public Host findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
	
		Host allocatedHost = null;

		for (Host host : this.<Host> getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
			if (host.isSuitableForVm(vm)) {//该主机首先应该是有足够的资源

				allocatedHost = host;
			}
		}
		return allocatedHost;
	}

	/**
	 * Checks if a host will be over utilized after placing of a candidate VM.
	 * 
	 * @param host the host to verify
	 * @param vm the candidate vm 
	 * @return true, if the host will be over utilized after VM placement; false otherwise
	 */

	/**
	 * Extracts the host list from a migration map.
	 * 从迁移映射中提取主机列表
	 * @param migrationMap the migration map
	 * @return the list
	 */
	protected List<PowerHost> extractHostListFromMigrationMap(List<Map<String, Object>> migrationMap) {
		List<PowerHost> hosts = new LinkedList<PowerHost>();
		for (Map<String, Object> map : migrationMap) {
			hosts.add((PowerHost) map.get("host"));
		}
		return hosts;
	}

	/**
	 * Gets the VMs to migrate from hosts.
	 * 
	 * @param overUtilizedHosts the over utilized hosts
	 * @return the VMs to migrate from hosts
	 */
	protected List<? extends Vm> getVmsToMigrateFromHosts(List<PowerHost> Hosts) {
		List<Vm> vmsToMigrate = new LinkedList<Vm>();
		for (PowerHost host : Hosts) {
			while (true) {
				List<Vm> vmlist = getVmSelectionPolicy().getMigratableVms(host);//从主机当中选择所有错误率超标的虚拟机去迁移
				if (vmlist == null) {
					break;
				}
				vmsToMigrate.addAll(vmlist);
				host.vmSpecialDestroy(vmlist);
			}
		}
		return vmsToMigrate;
	}

	/**
	 * Gets the VMs to migrate from under utilized host.
	 * 获取低利用率的主机上的虚拟机
	 * @param host the host
	 * @return the vms to migrate from under utilized host
	 */
	protected List<? extends Vm> getVmsToMigrateFromUnderUtilizedHost(PowerHost host) {
		List<Vm> vmsToMigrate = new LinkedList<Vm>();
		for (Vm vm : host.getVmList()) {
			if (!vm.isInMigration()) {
				vmsToMigrate.add(vm);
			}
		}
		return vmsToMigrate;
	}
	
	/**
	 * Gets the switched off hosts.
	 * 获取关闭的主机
	 * @return the switched off hosts
	 */
	protected List<PowerHost> getSwitchedOffHosts() {
		List<PowerHost> switchedOffHosts = new LinkedList<PowerHost>();
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (host.getUtilizationOfCpu() == 0) {
				switchedOffHosts.add(host);
			}
		}
		return switchedOffHosts;
	}

	/**
	 * Updates the list of maps between a VM and the host where it is place.更新虚拟机和主机之间的映射列表。过滤掉正在迁移中的虚拟机
         * @see #savedAllocation
	 */
	protected void saveAllocation() {
		getSavedAllocation().clear();
		for (Host host : getHostList()) {
			for (Vm vm : host.getVmList()) {//便历此主机上的虚拟机
				if (host.getVmsMigratingIn().contains(vm)) {//判断此主机上的虚拟机迁移列表中是否有正在迁移的虚拟机，如果有，则跳过此虚拟机，若果没有，则将其和它所对应的主机加入到savedAllocation中，遍历下一个虚拟机
					continue;
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("host", host);
				map.put("vm", vm);
				getSavedAllocation().add(map);
			}
		}
	}

	/**
	 * Restore VM allocation from the allocation history.
         * @see #savedAllocation从分配历史中恢复VM分配。
	 */
	protected void restoreAllocation() {
		for (Host host : getHostList()) {
			host.vmDestroyAll();
			host.reallocateMigratingInVms();
		}
		for (Map<String, Object> map : getSavedAllocation()) {
			Vm vm = (Vm) map.get("vm");
			PowerHost host = (PowerHost) map.get("host");
			if (!host.vmCreate(vm)) {
				Log.printConcatLine("Couldn't restore VM #", vm.getId(), " on host #", host.getId());
				System.exit(0);
			}
			getVmTable().put(vm.getUid(), host);
		}
	}

	/**
	 * Gets the power consumption of a host after placement of a candidate VM.
         * The VM is not in fact placed at the host.
	 * 获取放置候选VM后主机的功耗。VM实际上并没有放在主机上。
	 * @param host the host
	 * @param vm the candidate vm
	 * 
	 * @return the power after allocation
	 */
	protected double getPowerAfterAllocation(PowerHost host, Vm vm) {
		double power = 0;
		try {
			power = host.getPowerModel().getPower(getMaxUtilizationAfterAllocation(host, vm));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	/**
	 * Gets the max power consumption of a host after placement of a candidate VM.
         * The VM is not in fact placed at the host.
         * We assume that load is balanced between PEs. The only
	 * restriction is: VM's max MIPS < PE's MIPS
	 * 
	 * @param host the host
	 * @param vm the vm
	 * 
	 * @return the power after allocation
	 */
	protected double getMaxUtilizationAfterAllocation(PowerHost host, Vm vm) {
		double requestedTotalMips = vm.getCurrentRequestedTotalMips();
		double hostUtilizationMips = getUtilizationOfCpuMips(host);
		double hostPotentialUtilizationMips = hostUtilizationMips + requestedTotalMips;
		double pePotentialUtilization = hostPotentialUtilizationMips / host.getTotalMips();
		return pePotentialUtilization;
	}
	
	/**
	 * Gets the utilization of the CPU in MIPS for the current potentially allocated VMs.
	 *
	 * @param host the host
	 *
	 * @return the utilization of the CPU in MIPS
	 */
	protected double getUtilizationOfCpuMips(PowerHost host) {
		double hostUtilizationMips = 0;
		for (Vm vm2 : host.getVmList()) {
			if (host.getVmsMigratingIn().contains(vm2)) {
				// calculate additional potential CPU usage of a migrating in VM
				hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2) * 0.9 / 0.1;
			}
			hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2);
		}
		return hostUtilizationMips;
	}

	/**
	 * Gets the saved allocation.
	 * 
	 * @return the saved allocation
	 */
	protected List<Map<String, Object>> getSavedAllocation() {
		return savedAllocation;
	}

	/**
	 * Sets the vm selection policy.
	 * 
	 * @param vmSelectionPolicy the new vm selection policy
	 */
	protected void setVmSelectionPolicy(PowerVmSelectionPolicy vmSelectionPolicy) {
		this.vmSelectionPolicy = vmSelectionPolicy;
	}

	/**
	 * Gets the vm selection policy.
	 * 
	 * @return the vm selection policy
	 */
	protected PowerVmSelectionPolicy getVmSelectionPolicy() {
		return vmSelectionPolicy;
	}

	/**
	 * Gets the utilization history.
	 * 
	 * @return the utilization history
	 */
	public Map<Integer, List<Double>> getUtilizationHistory() {
		return utilizationHistory;
	}

	/**
	 * Gets the metric history.
	 * 
	 * @return the metric history
	 */
	public Map<Integer, List<Double>> getMetricHistory() {
		return metricHistory;
	}

	/**
	 * Gets the time history.
	 * 
	 * @return the time history
	 */
	public Map<Integer, List<Double>> getTimeHistory() {
		return timeHistory;
	}

	/**
	 * Gets the execution time history vm selection.
	 * 
	 * @return the execution time history vm selection
	 */
	public List<Double> getExecutionTimeHistoryVmSelection() {
		return executionTimeHistoryVmSelection;
	}

	/**
	 * Gets the execution time history host selection.
	 * 
	 * @return the execution time history host selection
	 */
	public List<Double> getExecutionTimeHistoryHostSelection() {
		return executionTimeHistoryHostSelection;
	}

	/**
	 * Gets the execution time history vm reallocation.
	 * 
	 * @return the execution time history vm reallocation
	 */
	public List<Double> getExecutionTimeHistoryVmReallocation() {
		return executionTimeHistoryVmReallocation;
	}

	/**
	 * Gets the execution time history total.
	 * 
	 * @return the execution time history total
	 */
	public List<Double> getExecutionTimeHistoryTotal() {
		return executionTimeHistoryTotal;
	}

}
