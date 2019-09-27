/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package com.shiyan.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.shiyan.core.CloudSim;
import com.shiyan.core.CloudSimTags;
import com.shiyan.core.SimEntity;
import com.shiyan.core.SimEvent;
import com.shiyan.lists.CloudletList;
import com.shiyan.lists.VmList;
import com.shiyan.models.Cloudlet;
import com.shiyan.models.Vm;

/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM management, as vm
 * creation, submission of cloudlets to VMs and destruction of VMs.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBroker extends SimEntity {

	/** The list of VMs submitted to be managed by the broker. */
	protected List<? extends Vm> vmList;
	
	protected List<? extends Cloudlet> cancledCloudletList;
	

	/** The list of VMs created by the broker. */
	protected List<? extends Vm> vmsCreatedList;

	/** The list of cloudlet submitted to the broker. 
         * @see #submitCloudletList(java.util.List) 
         */
	protected List<? extends Cloudlet> cloudletList;

	/** The list of submitted cloudlets. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The list of received cloudlet. */
	protected List<? extends Cloudlet> cloudletReceivedList;
	
	/** The number of submitted cloudlets. */
	protected int cloudletsSubmitted;

	/** The number of requests to create VM. */
	protected int vmsRequested;

	/** The number of acknowledges (ACKs) sent in response to
         * VM creation requests. */
	protected int vmsAcks;

	/** The number of destroyed VMs. */
	protected int vmsDestroyed;

	/** The id's list of available datacenters. */
	protected List<Integer> datacenterIdsList;

	/** The list of datacenters where was requested to place VMs. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The vms to datacenters map, where each key is a VM id
         * and each value is the datacenter id whwere the VM is placed. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics map where each key
	 * 数据中心特征映射，其中每个键是一个数据中心id，每个值是它的特征
         * is a datacenter id and each value is its characteristics.. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by {@link SimEntity} class)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	public DatacenterBroker(String name) throws Exception {
		super(name);

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<Cloudlet>());
		setCancledCloudletList(new ArrayList<Cloudlet>());
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());
		
		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
	}

	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
         * 
         * @todo The name of the method is confused with the {@link #submitCloudlets()},
         * that in fact submit cloudlets to VMs. The term "submit" is being used
         * ambiguously. The method {@link #submitCloudlets()} would be named "sendCloudletsToVMs"
         * 
         * The method {@link #submitVmList(java.util.List)} may have
         * be checked too.
	 */
	public void submitCloudletList(List<? extends Cloudlet> list) {
		double submitTime = CloudSim.clock();
		for(Cloudlet cloudlet: list) {
			cloudlet.setStartSubmit(submitTime);
			getCloudletList().add(cloudlet);
		}
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 指定给定的cloudlet必须在特定的虚拟机中运行。
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
	}
	//...................................................................所有算法从此开始。
	
	/**
	 * 这里是一个比较的算法——短任务优先算法
	 * 
	 * @throws Exception 
	 */
//	public void bindCloudletsToVmsBySJF() {
//		
//		List<Cloudlet> cloudletlist = getCloudletList();
//		int cloudletlength = cloudletlist.size();
//		int vmsize = getVmList().size();
//		int i=0;
//		for(Cloudlet cloudlet: getCloudletList()){
//			cloudlet.setVmId(++i%vmsize);
//		}
//		Cloudlet mincloudlet = null;
//		int minindex = -1;
//		
//        for(int i=0;i<length;i++) {
//        	double mincloudletlength = Double.MAX_VALUE;
//		    for(int j=i;j<length;j++){
//			   double newcloudletlength = cloudletlist.get(j).getCloudletLength();
//			   if(newcloudletlength < mincloudletlength) {
//				  mincloudletlength = newcloudletlength;
//				  minindex = j;
//		       }
//		    }
//		    if(minindex!=-1) {
//		        mincloudlet = cloudletlist.get(minindex);
//		        cloudletlist.set(minindex, cloudletlist.get(i));
//		        cloudletlist.set(i, mincloudlet);
//		    	minindex=-1;
//		    }
//        }
//  }
	/**
	 * 这里是一个比较的算法——长任务优先算法
	 * 
	 * @throws Exception 
	 */
//	public void bindCloudletsToVmsByLJF() {
//		
//		List<Cloudlet> cloudletlist = getCloudletList();
//		int length = cloudletlist.size();
//		Cloudlet maxcloudlet = null;
//		int maxindex = -1;
//        for(int i=0;i<length;i++) {
//        	double maxcloudletlength = Double.MIN_VALUE;
//		    for(int j=i;j<length;j++){
//			   double newcloudletlength = cloudletlist.get(j).getCloudletLength();
//			   if(newcloudletlength > maxcloudletlength) {
//				  maxcloudletlength = newcloudletlength;
//				  maxindex = j;
//		       }
//		    }
//		    if(maxindex!=-1) {
//		        maxcloudlet = cloudletlist.get(maxindex);
//		        cloudletlist.set(maxindex, cloudletlist.get(i));
//		        cloudletlist.set(i, maxcloudlet);
//		    	maxindex=-1;
//		    }
//        }
//	}
	/**
	  * 这里是一个加入了QoS需求的算法——基于用户QoS需求的先来先服务算法I-FCFS(RR)
	  * 这种算法在为云任务分配虚拟机时，考虑了用户的各项Qos需求
	  * 同时结合了FCFS-RR算法
	 * 
	 * @throws Exception 
	 */
	public void bindCloudletsToVmsByIFCFSRR() {
		
		double currentTime = CloudSim.clock();
		Map<Integer,Double> lastVmProcessTime =new HashMap<Integer,Double>();
		List<Cloudlet> newcloudletslist = new ArrayList<Cloudlet>();
		int vmsize = getVmList().size();
		int vmid = 0;
		
		for(Vm vm: getVmList()) {
			if (!lastVmProcessTime.containsKey(vm.getId()))
				lastVmProcessTime.put(vm.getId(), currentTime);
		}
		
      for(Cloudlet cloudlet: getCloudletList()) {
      	int iterator = vmsize;
      	if(cloudlet.getVmId()==-1) {
      		int expectedvmid = vmid%vmsize;
      		Vm vm = getVmList().get(expectedvmid);
      		
      		if(checkQoS(cloudlet, vm, lastVmProcessTime)) {
      			cloudlet.setVmId(vm.getId());
      			lastVmProcessTime.put(vm.getId(), calculateFinishLine(cloudlet,vm,lastVmProcessTime,"+"));
      			newcloudletslist.add(cloudlet);
      			vmid++;
      		}
      		else {
      			for(int i=(expectedvmid+1)%vmsize;i<=vmsize;i=(i+1)%vmsize) {
      				if(iterator<=1)
      					break;
      				Vm newvm = getVmList().get(i);
      				if(checkQoS(cloudlet, newvm, lastVmProcessTime)) {
              			cloudlet.setVmId(newvm.getId());
              			lastVmProcessTime.put(newvm.getId(), calculateFinishLine(cloudlet,newvm,lastVmProcessTime,"+"));
              			newcloudletslist.add(cloudlet);
              			vmid++;
              			break;
              		}
      				else 
      					iterator--;
      			}	
      		}
      	}
      }
      getCloudletList().clear();
      getCloudletList().addAll(newcloudletslist);
	}
	/**
	 * 这里是一个比较的算法——Min-Min算法
	 * 
	 * 算法的时间复杂度为O(m*n) 算法的空间复杂度为O(m)
	 * @throws Exception 
	 */
	public void bindCloudletsToVmsMinMin(){
		double currentTime =  CloudSim.clock();
		int cloudletNum = getCloudletList().size();
		List<Cloudlet> newCloudlets = new LinkedList<Cloudlet>();
		Map<Integer,Double> lastVmProcessTime =new HashMap<Integer,Double>();
		
		for(Vm vm: getVmList()) {
			if (!lastVmProcessTime.containsKey(vm.getId()))
				lastVmProcessTime.put(vm.getId(), currentTime);
		}
		while(cloudletNum >0) {
			//寻找具有最小最早任务完成时间的任务和对应的虚拟机
			double mincloudletfiniedline = Double.MAX_VALUE;
			double minallcloudletfiniedline = Double.MAX_VALUE;
			
			Cloudlet allbestCloudlet = null;
			Vm allbestVm = null;
			
			for(Cloudlet cloudlet: getCloudletList()) {
				if (cloudlet.getVmId() == -1) {
					Cloudlet bestCloudlet = null;
					Vm bestVm = null;
					
					for(Vm vm: getVmList()) {
						double newfinishline = calculateFinishLine(cloudlet, vm, lastVmProcessTime,"+");
		                if(newfinishline < mincloudletfiniedline) {
		                	mincloudletfiniedline = newfinishline;
		                	bestCloudlet =cloudlet;
							bestVm = vm;
		                }
					}
					if(minallcloudletfiniedline > mincloudletfiniedline) {
						minallcloudletfiniedline = mincloudletfiniedline;
						allbestCloudlet = bestCloudlet;
						allbestVm = bestVm;
					}
				}
			}
			if(allbestCloudlet!=null&&allbestVm!=null) {
				//已经为某个云任务找到了一个适应度最好的虚拟机,将该任务绑定到此虚拟机上
				//Log.printConcatLine("云任务#", bestCloudlet.getCloudletId() ,"匹配到了一个最好的虚拟机#" + bestVm.getId());
				bindCloudletToVm(allbestCloudlet.getCloudletId(), allbestVm.getId());
				lastVmProcessTime.put(allbestVm.getId(), calculateFinishLine(allbestCloudlet,allbestVm,lastVmProcessTime,"+"));
				newCloudlets.add(allbestCloudlet);
			}
			//为下一个任务找到适应度最高的虚拟机
			cloudletNum--;
		}
		if(newCloudlets!=null) {
			
			getCloudletList().clear();
			getCloudletList().addAll(newCloudlets);
		}
	}
	/**
	 * 这里是一个比较的算法——基于用户QoS的Min-Min算法I-MinMin
	 * 算法的时间复杂度为O(m*(n-x)=mn-mx) 算法的空间复杂度为O(m)
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	public void bindCloudletsToVmsIMinMin() throws Exception {
		
		double currentTime =  CloudSim.clock();
		int cloudletNum = getCloudletList().size();
		
		List<Cloudlet> newCloudlets = new LinkedList<Cloudlet>();
		Map<Integer,Double> lastVmProcessTime =new HashMap<Integer,Double>();
		//List<Map<Integer,Integer>> lastCloudletToVm = new ArrayList<Map<Integer,Integer>>();
		
		for(Vm vm: getVmList()) {
			if (!lastVmProcessTime.containsKey(vm.getId()))
				lastVmProcessTime.put(vm.getId(), currentTime);
		}
		
		while(cloudletNum >0) {
			boolean flag = false;
			double mincloudletfiniedline;
			double minallcloudletfiniedline = Double.MAX_VALUE;
			
			Cloudlet allbestCloudlet = null;
			Vm allbestVm = null;
			Cloudlet lastcloudlet = null;
			
			for(Cloudlet cloudlet: getCloudletList()) {
				if (cloudlet.getVmId() == -1&&cloudlet.getStatus()!=6) {
					
					mincloudletfiniedline = Double.MAX_VALUE;
					Cloudlet bestCloudlet = null;
					Vm bestVm = null;
					flag = false;
					
					for(Vm vm: getVmList()) 
					{
						if (checkQoS(cloudlet, vm, lastVmProcessTime)) {
							double newfinishline = calculateFinishLine(cloudlet, vm, lastVmProcessTime,"+");
							if(newfinishline < mincloudletfiniedline) {
			                	mincloudletfiniedline = newfinishline;
			                	bestCloudlet =cloudlet;
								bestVm = vm;
							}
							flag=true;
						}
					}
					if(!flag){
						Log.printConcatLine("云任务：", cloudlet.getCloudletId() ,"由于任何虚拟机满足不了其完成期限的需求，故被取消 ");
					    cloudlet.setCloudletStatus(Cloudlet.CANCELED);
					    flag = false;
					    
					    continue;
					}
					if(minallcloudletfiniedline > mincloudletfiniedline) {
						minallcloudletfiniedline = mincloudletfiniedline;
						allbestCloudlet = bestCloudlet;
						allbestVm = bestVm;
					}
				}
			}
			if(allbestCloudlet!=null&&allbestVm!=null) {
				//已经为某个云任务找到了一个最好的虚拟机,将该任务绑定到此虚拟机上
				//Log.printConcatLine("云任务#", bestCloudlet.getCloudletId() ,"匹配到了一个最好的虚拟机#" + bestVm.getId());
				bindCloudletToVm(allbestCloudlet.getCloudletId(), allbestVm.getId());
				lastVmProcessTime.put(allbestVm.getId(), calculateFinishLine(allbestCloudlet,allbestVm,lastVmProcessTime,"+"));
				newCloudlets.add(allbestCloudlet);
			}
			else 
				break;
			//为下一个任务找到适应度最高的虚拟机
			cloudletNum--;
		}
		if(newCloudlets!=null) {
			getCloudletList().clear();
			getCloudletList().addAll(newCloudlets);
		}
	}
	/**
	 * 这里是一个比较的算法——Max-Min算法
	 * 算法的时间复杂度为O(m*n) 算法的空间复杂度为O(m)
	 * @throws Exception 
	 */
	public void bindCloudletsToVmsMaxMin(){
		
		double currentTime =  CloudSim.clock();
		int cloudletNum = getCloudletList().size();
		List<Cloudlet> newCloudlets = new LinkedList<Cloudlet>();
		Map<Integer,Double> lastVmProcessTime =new HashMap<Integer,Double>();
		
		for(Vm vm: getVmList()) {
			if (!lastVmProcessTime.containsKey(vm.getId()))
				lastVmProcessTime.put(vm.getId(), currentTime);
		}
		while(cloudletNum >0) {
			//寻找具有最小最早任务完成时间的任务和对应的虚拟机
			double mincloudletfiniedline = Double.MAX_VALUE;
			double maxallcloudletfiniedline = Double.MIN_VALUE;
			
			Cloudlet allbestCloudlet = null;
			Vm allbestVm = null;
			
			for(Cloudlet cloudlet: getCloudletList()) {
				if (cloudlet.getVmId() == -1) {
					Cloudlet bestCloudlet = null;
					Vm bestVm = null;
					
					for(Vm vm: getVmList()) {
						double newfinishline = calculateFinishLine(cloudlet, vm, lastVmProcessTime,"+");
		                if(newfinishline < mincloudletfiniedline) {
		                	mincloudletfiniedline = newfinishline;
		                	bestCloudlet =cloudlet;
							bestVm = vm;
		                }
					}
					if(maxallcloudletfiniedline < mincloudletfiniedline) {
						maxallcloudletfiniedline = mincloudletfiniedline;
						allbestCloudlet = bestCloudlet;
						allbestVm = bestVm;
					}
				}
			}
			if(allbestCloudlet!=null&&allbestVm!=null) {
				//已经为某个云任务找到了一个适应度最好的虚拟机,将该任务绑定到此虚拟机上
				//Log.printConcatLine("云任务#", bestCloudlet.getCloudletId() ,"匹配到了一个最好的虚拟机#" + bestVm.getId());
				bindCloudletToVm(allbestCloudlet.getCloudletId(), allbestVm.getId());
				lastVmProcessTime.put(allbestVm.getId(), calculateFinishLine(allbestCloudlet,allbestVm,lastVmProcessTime,"+"));
				newCloudlets.add(allbestCloudlet);
			}
			//为下一个任务找到适应度最高的虚拟机
			cloudletNum--;
		}
		if(newCloudlets!=null) {
			getCloudletList().clear();
			getCloudletList().addAll(newCloudlets);
		}
	}
	/**
	 * 这里是一个比较的算法——基于用户QoS的Max-Min算法I-MaxMin
	 * 算法的时间复杂度为O(m*(n-x)=mn-mx) 算法的空间复杂度为O(m)
	 * @throws Exception 
	 */
	@SuppressWarnings("deprecation")
	public void bindCloudletsToVmsIMaxMin() throws Exception {
		
		double currentTime =  CloudSim.clock();
		int cloudletNum = getCloudletList().size();
		
		List<Cloudlet> newCloudlets = new LinkedList<Cloudlet>();
		Map<Integer,Double> lastVmProcessTime =new HashMap<Integer,Double>();
		//List<Map<Integer,Integer>> lastCloudletToVm = new ArrayList<Map<Integer,Integer>>();
		
		for(Vm vm: getVmList()) {
			if (!lastVmProcessTime.containsKey(vm.getId()))
				lastVmProcessTime.put(vm.getId(), currentTime);
		}
		
		while(cloudletNum >0) {
			boolean flag = false;
			double mincloudletfiniedline;
			double maxallcloudletfiniedline = Double.MIN_VALUE;
			
			Cloudlet allbestCloudlet = null;
			Vm allbestVm = null;
			
			for(Cloudlet cloudlet: getCloudletList()) {
				if (cloudlet.getVmId() == -1 && cloudlet.getCloudletStatus()!=6) {	
					
					mincloudletfiniedline = Double.MAX_VALUE;
					Cloudlet bestCloudlet = null;
					Vm bestVm = null;
					flag = false;
					
					for(Vm vm: getVmList()) {
						if (checkQoS(cloudlet, vm, lastVmProcessTime)) {
							double newfinishline = calculateFinishLine(cloudlet, vm, lastVmProcessTime,"+");
							if(newfinishline < mincloudletfiniedline) {
			                	mincloudletfiniedline = newfinishline;
			                	bestCloudlet =cloudlet;
								bestVm = vm;
							}
							flag=true;
						}
					}
					if(!flag){
						Log.printConcatLine("云任务：", cloudlet.getCloudletId() ,"由于任何虚拟机满足不了其完成期限的需求，故被取消 ");
					    cloudlet.setCloudletStatus(Cloudlet.CANCELED);
					    flag=false;
					    
					    continue;
					}
					if(maxallcloudletfiniedline < mincloudletfiniedline) {
						maxallcloudletfiniedline = mincloudletfiniedline;
						allbestCloudlet = bestCloudlet;
						allbestVm = bestVm;
					}
				}
			}
			if(allbestCloudlet!=null&&allbestVm!=null) {
				//已经为某个云任务找到了一个最好的虚拟机,将该任务绑定到此虚拟机上
				//Log.printConcatLine("云任务#", bestCloudlet.getCloudletId() ,"匹配到了一个最好的虚拟机#" + bestVm.getId());
				bindCloudletToVm(allbestCloudlet.getCloudletId(), allbestVm.getId());
				lastVmProcessTime.put(allbestVm.getId(), calculateFinishLine(allbestCloudlet,allbestVm,lastVmProcessTime,"+"));
				newCloudlets.add(allbestCloudlet);
			}
			
			cloudletNum--;
		}
		if(newCloudlets!=null) {
			getCloudletList().clear();
			getCloudletList().addAll(newCloudlets);
		}
	}
	
	/**
	 * 这里是一个比较的算法——基于用户QoS需求降级的Min-Min算法QoS-MinMin
	 * 算法的时间复杂度为O(m*(n-x)=mn-mx) 算法的空间复杂度为O(m)
	 * @throws Exception 
	 */
	public void bindCloudletsToVmsQoSMinMin() throws Exception {
		
		double currentTime =  CloudSim.clock();
		int cloudletsize = 0;
		List<Cloudlet> calculateCloudlets = new ArrayList<Cloudlet>();
		List<Cloudlet> storageCloudlets = new ArrayList<Cloudlet>();
		double totalDeadTime=0,calculateCloudletsize=0,averageDeadTime=0,storageloudletsize = 0;
		
		List<Cloudlet> newCloudlets = new LinkedList<Cloudlet>();
		Map<Integer,Double> lastVmProcessTime =new HashMap<Integer,Double>();
		
		for(Vm vm: getVmList()) {
			if (!lastVmProcessTime.containsKey(vm.getId()))
				lastVmProcessTime.put(vm.getId(), currentTime);
		}
		for(Cloudlet cloudlet: getCloudletList()) {
			totalDeadTime += cloudlet.getDeadlineTime();		
			cloudletsize++;
		}
		averageDeadTime = totalDeadTime/cloudletsize;
		
		for(Cloudlet cloudlet: getCloudletList()) {
			if(cloudlet.getDeadlineTime()<averageDeadTime) {
				calculateCloudlets.add(cloudlet);
				calculateCloudletsize++;
			}
			else {
				storageCloudlets.add(cloudlet);	
				storageloudletsize++;
			}
		}
		
		Map<Integer,Integer> newcloudlettovm = new HashMap<Integer, Integer>();
		while(calculateCloudletsize>0) {
			//为云任务找到最好的虚拟机
			newcloudlettovm = findABestVmforcloudlet(calculateCloudlets, lastVmProcessTime, 1);//为云任务寻找一个合适的虚拟机，这里补偿参数选择3
			if(newcloudlettovm!=null) {
				for(Integer cloudletid: newcloudlettovm.keySet()) {
					bindCloudletToVm(cloudletid, newcloudlettovm.get(cloudletid));
					lastVmProcessTime.put(newcloudlettovm.get(cloudletid), calculateFinishLine(getCloudletList().get(cloudletid),getVmList().get(newcloudlettovm.get(cloudletid)),lastVmProcessTime,"+"));
					newCloudlets.add(getCloudletList().get(cloudletid));	
				}
			}
			calculateCloudletsize--;
		}
		
		while(storageloudletsize>0) {
			
			newcloudlettovm = findABestVmforcloudlet(storageCloudlets, lastVmProcessTime,2);
			if(newcloudlettovm!=null) {
				for(Integer cloudletid: newcloudlettovm.keySet()) {
					bindCloudletToVm(cloudletid, newcloudlettovm.get(cloudletid));
					lastVmProcessTime.put(newcloudlettovm.get(cloudletid), calculateFinishLine(getCloudletList().get(cloudletid),getVmList().get(newcloudlettovm.get(cloudletid)),lastVmProcessTime,"+"));
					newCloudlets.add(getCloudletList().get(cloudletid));	
				}
			}
			storageloudletsize--;
		}
		if(newCloudlets!=null) {
			getCloudletList().clear();
			getCloudletList().addAll(newCloudlets);
		}
	}
	
	public Map<Integer,Integer> findABestVmforcloudlet(List<Cloudlet> cloudletlist, Map<Integer,Double> lastVmProcessTime,int type) {
		
		boolean flag = false;
		double mincloudletfiniedline = Double.MAX_VALUE;
		double minallcloudletfiniedline = Double.MAX_VALUE;
		Map<Integer,Integer> cloudlettovm = new HashMap<Integer, Integer>();
		List<Cloudlet> lastdelaycloudlet = new ArrayList<Cloudlet>();
		
		Cloudlet allbestCloudlet = null;
		Vm allbestVm = null;
		//采用Min-Min的方法找到一个最合适的任务和对应的虚拟机
		for(Cloudlet cloudlet: cloudletlist) {
			if (cloudlet.getVmId() == -1) {	
//				if(lastdelaycloudlet!=null&&lastdelaycloudlet.contains(cloudlet))
//					continue;
				Cloudlet bestCloudlet = null;
				Vm bestVm = null; 
				flag = false;
				
				for(Vm vm: getVmList()) {
//					if (checkQoS(cloudlet, vm, lastVmProcessTime)) {
						double newfinishline = calculateFinishLine(cloudlet, vm, lastVmProcessTime,"+");
						if(newfinishline < mincloudletfiniedline) {
		                	mincloudletfiniedline = newfinishline;
		                	bestCloudlet =cloudlet;
							bestVm = vm;
						}
						//flag=true;
					//}
						
				}
				if(minallcloudletfiniedline > mincloudletfiniedline) {
					minallcloudletfiniedline = mincloudletfiniedline;
					allbestCloudlet = bestCloudlet;
					allbestVm = bestVm;
				}
			}
		}
		if(allbestCloudlet!=null&&allbestVm!=null) {
		//如果这个最好的云任务和虚拟机不能够满足Qos的需求，则开始启用延迟补偿和收益敏感的策略
			if (!checkQoS(allbestCloudlet, allbestVm, lastVmProcessTime)) {
				allbestVm = findBestVmBySLD(allbestCloudlet, lastVmProcessTime, type);//通过延迟补偿和收益敏感的策略为这个违背QoS的云任务寻找一个最好的虚拟机
				//如果能够找到一个合适的虚拟机，则将其加入到最佳映射列表
				if (allbestVm != null) {
					cloudlettovm.put(allbestCloudlet.getCloudletId(), allbestVm.getId());
				}
				//如果不能够找到一个合适的虚拟机，该任务会被取消
				else {
					Log.printConcatLine("云任务：", allbestCloudlet.getCloudletId() ,"由于任何虚拟机满足不了其完成期限的需求，故被取消 ");
//				    lastdelaycloudlet.add(allbestCloudlet);
					allbestCloudlet.setUserId(-2);
					return null;
				}
					
			}
			//如果能够满足QoS的需求，则将其加入到最佳映射列表s
			else {
				cloudlettovm.put(allbestCloudlet.getCloudletId(), allbestVm.getId());
			}
		}
		return cloudlettovm;
	}
	
	//计算新的云任务完成时间
	public double calculateFinishLine(Cloudlet cloudlet, Vm vm, Map<Integer, Double> lastVmProcessTime,String type) {
		double newdeadline = 0.0; 
		
		if(type=="-"||type.equals("-"))
			newdeadline=lastVmProcessTime.get(vm.getId())-calculateloudletExpectedExecTime(cloudlet, vm, 0.0);
		else
			newdeadline=lastVmProcessTime.get(vm.getId())+calculateloudletExpectedExecTime(cloudlet, vm, 0.0);
		
		return newdeadline;
	}
	
	//计算任务在此虚拟机上执行是否满足用户QoS需求，满足则返回真，否则返回假；
	public boolean checkQoS(Cloudlet cloudlet, Vm vm, Map<Integer, Double> lastVmProcessTime) {
		
//		System.out.println((lastVmProcessTime.get(vm.getId())+ cloudlet.getCloudletLength()/vm.getMips()) <= (cloudlet.getStartSubmit()+cloudlet.getDeadlineTime()));
//		System.out.println(cloudlet.getCloudletLength()/vm.getMips()*vm.getCostPerVm() <= cloudlet.getUserMoney());
		
		return (lastVmProcessTime.get(vm.getId())+ cloudlet.getCloudletLength()/vm.getMips() <= cloudlet.getStartSubmit()+cloudlet.getDeadlineTime())
				&&(cloudlet.getCloudletLength()/vm.getMips()*vm.getCostPerVm() <= cloudlet.getUserMoney());
	}
	//检查是违背了哪一个用户QoS需求，若是违背了截止时间需求返回0；若违背内核需求返回1；若违背了成本需求返回2;若都满足返回3
	public int checkWhichQoS(Cloudlet cloudlet, Vm vm, Map<Integer, Double> lastVmProcessTime) {
		
		if(cloudlet.getCloudletLength()/vm.getMips()*vm.getCostPerVm() > cloudlet.getUserMoney())
			return 0;
		if(lastVmProcessTime.get(vm.getId())+ cloudlet.getCloudletLength()/vm.getMips() > cloudlet.getStartSubmit()+cloudlet.getDeadlineTime())
			return 1;
		
		return 2;
	}
	
   //计算云任务的预期执行时间
	public double calculateloudletExpectedExecTime(Cloudlet cloudlet, Vm vm, double fileTransferTime) {
		
		// use the current capacity to estimate the extra amount of 使用当前的容量估计额外的数量文件传输时间
		// time to file transferring. It must be added to the cloudlet length 文件传输的时间，它必须添加加到云的长度上
		double extraSize = vm.getMips() * fileTransferTime;
		long length = cloudlet.getCloudletLength();
		length += extraSize;
		cloudlet.setCloudletLength(length);
		return cloudlet.getCloudletLength() / vm.getMips();
	}
	
	/**
	 * 
	 * 通过延迟补偿和收益敏感的方法来为任务找到一个合适的虚拟机
	 */
	public Vm findBestVmBySLD(Cloudlet cloudlet, Map<Integer,Double> lastVmProcessTime,int type) {
		
		List<Integer> lastvmid = new ArrayList<Integer>();
		boolean flag = false;
		int vmsize = getVmList().size();
		
		while(vmsize>0) {
			double mincloudletfiniedline = Double.MAX_VALUE;
			//double maxCloudletProfit = Double.MIN_VALUE;
			Vm bestVm = null;
			
			//找到一个最小完成时间的虚拟机
			
			for(Vm vm: getVmList()) {
				
				if(lastvmid!=null&&lastvmid.contains(vm.getId()))
					continue;
				double newfinishline = calculateFinishLine(cloudlet, vm, lastVmProcessTime,"+");
				if(newfinishline < mincloudletfiniedline) {
					mincloudletfiniedline = newfinishline;
					bestVm = vm;
				}
			}
			if(bestVm!=null) {
				int result = checkWhichQoS(cloudlet, bestVm, lastVmProcessTime);//检查这个云任务和对应的虚拟机属于违背QoS的哪一种情况
				//属于违背收入的情况
				if (result==0) {
					vmsize--;
					lastvmid.add(bestVm.getId());
					continue;
				} 
				//属于违背截止时间的情况
				if (result==1) { 
					if(SLD(cloudlet, bestVm, lastVmProcessTime, type)) {
						return bestVm;
					}
					else {
						vmsize--;
						lastvmid.add(bestVm.getId());
						continue;
					}
				}
				return bestVm;
			}
			else 
				return null;
		}	
		return null;
	}
	public boolean SLD(Cloudlet cloudlet, Vm bestVm, Map<Integer,Double> lastVmProcessTime, int type) {
		//云任务在虚拟机中执行会支付一定的费用，这些费用被当成云服务商的收入，如果任务能够在满足QoS的情况下执行成功，
		//云服务商会获取一定的收入（收入=20%的成本+80%的盈利），成本包括一些机器的维护和电力的消耗等，
		//云服务商想要获得收入，就必须满足用户任务的QoS需求，否则用户将不会为云任务支付花费；
		//不管有没有满足云任务的QoS需求，云任务只要在虚拟机中执行都要产生成本（我们这里按照收入的20%计算），
		//故，在计算盈利时，需要从所有的收入中减去这些成本
		
		//本算法通过延迟补偿和收入敏感的方法来提高云任务的完成度同时不减少盈利
		//任务的最终完成时间
		double cloudletFinalTime = calculateFinishLine(cloudlet, bestVm, lastVmProcessTime,"+");
		//任务延迟时间=任务完成时间-任务截止时间
		double cloudletDealyTime =  cloudletFinalTime - cloudlet.getDeadlineTime();
		//
		//double cloudletPrimaryProfit = cloudlet.getUserMoney()-cloudlet.getCloudletLength()/vm.getMips()*vm.getCostPerVm();
		//
		//double compensationProbability = cloudlet.getUserMoney()/(cloudletDealyTime-cloudlet.getStartSubmit())*cloudletDealyTime;
		//服务商正常收入
		double VmNormalIncome = bestVm.getCostPerVm()*calculateloudletExpectedExecTime(cloudlet,bestVm,0.0);
		//补偿成本=虚拟机单位运行成本*虚拟机预计运行时间/（任务类型*（任务截止时间-任务提交时间））*任务延迟时间
		double compensationCost = VmNormalIncome/(type*(cloudlet.getDeadlineTime()-cloudlet.getStartSubmit()))*cloudletDealyTime;
		//任务最终支付价格=任务所支付的价格-补偿成本
		//double cloudletFinalMoney = cloudlet.getUserMoney() - compensationCost;
		//服务商最终收入
		//double VmFinalIncome = cloudlet.getUserMoney() - compensationCost;
		//服务商最终收入=服务商正常收入-补偿成本
		double cloudletFinalCost = VmNormalIncome - compensationCost;
		//服务商最终盈利=服务商正常收入-服务商成本-补偿成本
		double cloudletFinalProfit = VmNormalIncome - 0.2*VmNormalIncome - compensationCost;
		
		if(cloudletFinalProfit >= 0 && cloudletFinalCost <= cloudlet.getUserMoney()) {
			return true;
		}
			
		else 
			return false;
	}
	
	
	/**
	 *  这里是一个比较的算法——Sufferage算法，
	 *  算法原理为：
	 *  计算每个任务的调度损失度,即次小完成时间与最小完成时间之差值,
	 *  在发生资源竞争时,则比较每个任务的调度损失度,
	 *  最终选择调度损失度最大的任务进行指派,若调度损失度相等,
	 *  则默认最先分配给资源的任务最终享有该资源的使用权
	 * 
	  *此算法的时间复杂度为O(m*n) 算法的空间复杂度为O(m)
	 * @throws Exception 
	 */
	public void bindCloudletsToVmsSufferage(){
		double currentTime =  CloudSim.clock();
		//算法最终形成的云任务列表，存储在这里
		List<Cloudlet> finalCloudlets = new LinkedList<Cloudlet>();
		List<Cloudlet> newCloudlets = new LinkedList<Cloudlet>();
		//虚拟机的就绪时间
		Map<Integer,Double> lastVmProcessTime =new HashMap<Integer,Double>();
		//云任务的调度损失，每次只存储最新的云任务调度损失
		Map<Integer,Double> cloudletSufferage =new HashMap<Integer,Double>();
		//上次放置在虚拟机中的云任务，每次只存储最新的云任务
		Map<Integer,Integer> vmlastcloudlet =new HashMap<Integer,Integer>();
		//初始化所有虚拟机的就绪时间为当前系统时间
		for(Vm vm: getVmList()) {
			if (!lastVmProcessTime.containsKey(vm.getId()))
				lastVmProcessTime.put(vm.getId(), currentTime);
		}
		//初始化所有云任务的调度损失为0
		for(Cloudlet cloudlet: getCloudletList()) {
			if (!cloudletSufferage.containsKey(cloudlet.getCloudletId()))
				lastVmProcessTime.put(cloudlet.getCloudletId(), 0.0);
		}
		//设置所有虚拟机为未分配状态
		for(Vm vm: getVmList()) {
			if (!vmlastcloudlet.containsKey(vm.getId()))
				vmlastcloudlet.put(vm.getId(), -1);
		}
		//循环，直到新的任务序列达到提交的任务大小
		while(finalCloudlets.size() < getCloudletList().size()) {
			//遍历每一个云任务
			for(Cloudlet cloudlet: getCloudletList()) {
				if (cloudlet.getVmId() == -1) {
					//寻找具有最早和次早任务完成时间的虚拟机
					double mincloudletfiniedline = Double.MAX_VALUE;
					double secmincloudletfiniedline = Double.MAX_VALUE;
					Cloudlet bestCloudlet = null;
					Vm bestVm = null;
					//找到使得此任务完成时间最短的虚拟机
					for(Vm vm: getVmList()) {
						double newfinishline = calculateFinishLine(cloudlet, vm, lastVmProcessTime,"+");
		                if(newfinishline < mincloudletfiniedline) {
		                	secmincloudletfiniedline = mincloudletfiniedline;
		                	mincloudletfiniedline = newfinishline;
		                	bestCloudlet = cloudlet;
							bestVm = vm;
		                }
					}
					//计算新的调度损失
					double newsufferage = secmincloudletfiniedline - mincloudletfiniedline;
					//若此虚拟机中还没有分配任务，则将此任务分配给它，更新虚拟机的分配状态
					if(vmlastcloudlet.get(bestVm.getId()) == -1) {
						bindCloudletToVm(bestCloudlet.getCloudletId(), bestVm.getId());
						
						//每次虚拟机中只存储上一个云任务
						vmlastcloudlet.put(bestVm.getId(), bestCloudlet.getCloudletId());
						//存储这个任务的调度损失，以便于后来的比较
						cloudletSufferage.put(bestCloudlet.getCloudletId(), newsufferage);
						//暂时将此任务加入到新的任务列表中
						newCloudlets.add(bestCloudlet);
					}
					//若此虚拟机中已经有分配的任务，比较此任务与上一个在此虚拟机中执行的任务的调度损失大小
					else {
						//获取此虚拟机中上一个云任务的调度损失
						double lastcloudletSufferage = cloudletSufferage.get(vmlastcloudlet.get(bestVm.getId()));
						//若这个新的任务的调度损失较大，则将此虚拟机中上一个任务暂时移除掉，重新放到任务调度队列，并将新的任务放置到该虚拟机
						if(newsufferage > lastcloudletSufferage) {
							//更新此任务新的调度损失
							cloudletSufferage.put(bestCloudlet.getCloudletId(), newsufferage);
							//设置此任务的虚拟机
							bindCloudletToVm(bestCloudlet.getCloudletId(), bestVm.getId());
							//设置上一个任务为未分配状态
							bindCloudletToVm(vmlastcloudlet.get(bestVm.getId()), -1);
							//更新虚拟机的就绪时间
							//从任务列表中移除上一个任务
							newCloudlets.remove(getCloudletList().get(vmlastcloudlet.get(bestVm.getId())));
							//更新虚拟机中上一个任务
							vmlastcloudlet.put(bestVm.getId(), bestCloudlet.getCloudletId());
							//暂时将此任务加入到新的任务列表中
							newCloudlets.add(bestCloudlet);
						}
					}
				}
			}
			//经过上述循环，至少有一个任务被分配到了虚拟机，且每个虚拟机最多分配一个云任务（调度损失最大的那个云任务）
			//更新虚拟机的就绪时间，每次更新最新产生的云任务
			if(newCloudlets!=null)
			{
				for(Cloudlet cloudlet: newCloudlets) {
					if (cloudlet.getVmId() != -1)
						lastVmProcessTime.put(cloudlet.getVmId(), calculateFinishLine(cloudlet,getVmList().get(cloudlet.getVmId()),lastVmProcessTime,"+"));
				}	
			}
			//初始化虚拟机的任务分配状态，将所有已经分配任务的虚拟机设置为未分配状态
			for(Integer vmid: vmlastcloudlet.keySet()) {
				vmlastcloudlet.put(vmid, -1);	
			}
			finalCloudlets.addAll(newCloudlets);
			//清除新产生的任务列表
			newCloudlets.clear();
		}
		//将新的云任务序列更新到云任务列表中
		if(finalCloudlets!=null) {
			getCloudletList().clear();
			getCloudletList().addAll(finalCloudlets);
		}
	}
	/**
	 *  这里是一个比较的算法——I-Sufferage算法，
	 *  太过复杂，且性能也不好，舍弃
	  *此算法的时间复杂度为O(m*n) 算法的空间复杂度为O(m)
	 * @throws Exception 
	 */
	@SuppressWarnings("deprecation")
	public void bindCloudletsToVmsISufferage() throws Exception{
		double currentTime =  CloudSim.clock();
		int cloudletsize = getCloudletList().size();
		//算法每次形成的云任务列表，存储在这里
		List<Cloudlet> newCloudlets = new LinkedList<Cloudlet>();
		//算法最终形成的云任务列表，存储在这里
		List<Cloudlet> finalCloudlets = new LinkedList<Cloudlet>();
		//虚拟机的就绪时间
		Map<Integer,Double> lastVmProcessTime =new HashMap<Integer,Double>();
		//云任务的调度损失，每次只存储最新的云任务调度损失
		Map<Integer,Double> cloudletSufferage =new HashMap<Integer,Double>();
		//上次放置在虚拟机中的云任务，每次只存储最新的云任务
		Map<Integer,Integer> vmlastcloudlet =new HashMap<Integer,Integer>();
		
		boolean flag=false;
		//初始化所有虚拟机的就绪时间为当前系统时间
		for(Vm vm: getVmList()) {
			if (!lastVmProcessTime.containsKey(vm.getId()))
				lastVmProcessTime.put(vm.getId(), currentTime);
		}
		//初始化所有云任务的调度损失为0
		for(Cloudlet cloudlet: getCloudletList()) {
			if (!cloudletSufferage.containsKey(cloudlet.getCloudletId()))
				lastVmProcessTime.put(cloudlet.getCloudletId(), 0.0);
		}
		//设置所有虚拟机为未分配状态
		for(Vm vm: getVmList()) {
			if (!vmlastcloudlet.containsKey(vm.getId()))
				vmlastcloudlet.put(vm.getId(), -1);
		}
		
		while(finalCloudlets.size() < cloudletsize) {
			flag = false;
			
			for(Cloudlet cloudlet: getCloudletList()) {
				
				if (cloudlet.getVmId() == -1&&cloudlet.getCloudletStatus()!=6) {
					//寻找具有最早和次早任务完成时间的虚拟机
					double mincloudletfiniedline = Double.MAX_VALUE;
					double secmincloudletfiniedline = Double.MAX_VALUE;
					Cloudlet bestCloudlet = null;
					Vm bestVm = null;
					flag=false;
					
					//找到使得此任务完成时间最短的虚拟机
					for(Vm vm: getVmList()) {
						if(checkQoS(cloudlet, vm, lastVmProcessTime)) {
							double newfinishline = calculateFinishLine(cloudlet, vm, lastVmProcessTime,"+");
							if(newfinishline < mincloudletfiniedline) {
								secmincloudletfiniedline = mincloudletfiniedline;
								mincloudletfiniedline = newfinishline;
								bestCloudlet = cloudlet;
								bestVm = vm;
							}
							flag=true;
						}
					}
					if(!flag){
						Log.printConcatLine("云任务：", cloudlet.getCloudletId() ,"由于任何虚拟机满足不了其完成期限的需求，故被取消 ");
					    cloudlet.setCloudletStatus(Cloudlet.CANCELED);
					    cloudletsize--;
						flag = false;
						
					    continue;
					}
					//计算新的调度损失
					double newsufferage = secmincloudletfiniedline - mincloudletfiniedline;
					//若此虚拟机中还没有分配任务，则将此任务分配给它，更新虚拟机的分配状态
					if(vmlastcloudlet.get(bestVm.getId()) == -1) {
						//暂时绑定云任务到此虚拟机中	
						bindCloudletToVm(bestCloudlet.getCloudletId(), bestVm.getId());
						//暂时更新虚拟机的的就绪时间
						
						vmlastcloudlet.put(bestVm.getId(), bestCloudlet.getCloudletId());
						cloudletSufferage.put(bestCloudlet.getCloudletId(), newsufferage);
						newCloudlets.add(bestCloudlet);
					}
					//若此虚拟机中已经有分配的任务，比较此任务与上一个在此虚拟机中执行的任务的调度损失大小
					else {
						//获取此虚拟机中上一个云任务的调度损失
						double lastcloudletSufferage = cloudletSufferage.get(vmlastcloudlet.get(bestVm.getId()));
						//若这个新的任务的调度损失较大，则将此虚拟机中上一个任务暂时移除掉，重新放到任务调度队列，并将新的任务放置到该虚拟机
						if(newsufferage > lastcloudletSufferage) {
							cloudletSufferage.put(bestCloudlet.getCloudletId(), newsufferage);
							//设置此任务的虚拟机
							bindCloudletToVm(bestCloudlet.getCloudletId(), bestVm.getId());
							//设置上一个任务为未分配状态
							bindCloudletToVm(vmlastcloudlet.get(bestVm.getId()), -1);
							
							newCloudlets.remove(getCloudletList().get(vmlastcloudlet.get(bestVm.getId())));
							vmlastcloudlet.put(bestVm.getId(), bestCloudlet.getCloudletId());
							newCloudlets.add(bestCloudlet);
						}
					}
				}
			}
			//经过上述循环，至少有一个任务被分配到了虚拟机，且每个虚拟机每次最多分配一个云任务
			//更新虚拟机的就绪时间
			if(newCloudlets!=null) {
				for(Cloudlet cloudlet: newCloudlets) {
					if (cloudlet.getVmId() != -1)
						lastVmProcessTime.put(cloudlet.getVmId(), calculateFinishLine(cloudlet,getVmList().get(cloudlet.getVmId()),lastVmProcessTime,"+"));
				}
			}
			//初始化虚拟机的任务分配状态，将所有已经分配任务的虚拟机设置为未分配状态
			for(Integer vmid: vmlastcloudlet.keySet()) {
				vmlastcloudlet.put(vmid, -1);	
			}
			finalCloudlets.addAll(newCloudlets);
			newCloudlets.clear();
		}
		//将新的云任务序列更新到云任务列表中
		if(finalCloudlets!=null) {
			getCloudletList().clear();
			getCloudletList().addAll(finalCloudlets);
		}
	}
	/**
	 * Submit cloudlets to the created VMs.
	  *  提交云任务到虚拟机中的请求
	  *  任务调度默认使用RR轮询和FCFS相结合的调度算法 FCFS-RR
	 * @pre $none
	 * @post $none
     * @see #submitCloudletList(java.util.List) 
	 */
	protected void submitCloudlets() {
		int vmIndex = 0;
		List<Cloudlet> successfullySubmitted = new ArrayList<Cloudlet>();
		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {//没有绑定虚拟机
				vm = getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm 已经绑定了虚拟机
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					if(!Log.isDisabled()) {				    
					    Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
							cloudlet.getCloudletId(), ": bount VM not available");
					}
					continue;
				}
			}

			if (!Log.isDisabled()) {
			    Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ",
					cloudlet.getCloudletId(), " to VM #", vm.getId());
			}
			
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
			successfullySubmitted.add(cloudlet);
		} 

		// remove submitted cloudlets from waiting list
		getCloudletList().removeAll(successfullySubmitted);
	}
	//...................................................................所有算法到此结束。
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Resource characteristics request  云资源特性请求（注册cis等）
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer 资源特点的回复
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer创建虚拟机的请求
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;
			// A finished cloudlet returned 表示Cloudlet返回给发送方
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes定义simulation结束
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Process the return of a request for the characteristics of a Datacenter.
	     * 处理对数据中心特征的请求的返回
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			createVmsInDatacenter(getDatacenterIdsList().get(0));//开始在数据中心中创建虚拟机
		}
	}

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Cloud Resource List received with ",
				getDatacenterIdsList().size(), " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			//向另一个实体发送带有表示事件类型的标记的事件/消息。
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 *    创建虚拟机请求的返回
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];
    
		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #", vmId,
					" has been created in Datacenter #", datacenterId, ", Host #",
					VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Creation of VM #", vmId,
					" failed in Datacenter #", datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created所有请求的虚拟机已经被创建
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	/**
	 * Process a cloudlet return event.
	 * 云任务返回
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Cloudlet ", cloudlet.getCloudletId(),
				" received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

		}
	}

	/**
	 * Process non-default received events that aren't processed by
         * the {@link #processEvent(com.shiyan.core.SimEvent)} method.
         * This method should be overridden by subclasses in other to process
         * new defined events.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
         * @todo to ensure the method will be overridden, it should be defined 
         * as abstract in a super class from where new brokers have to be extended.
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printConcatLine(getName(), ".processOtherEvent(): ", "Error - an event is null.");
			return;
		}

		Log.printConcatLine(getName(), ".processOtherEvent(): Error - event unknown by this DatacenterBroker.");
	}

	/**
	 * Create the submitted virtual machines in a datacenter.
	 *   提交在数据中心创建虚拟机的请求
	 * @param datacenterId Id of the chosen Datacenter
	 * @pre $none
	 * @post $none
         * @see #submitVmList(java.util.List) 
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one 向数据中心发送创建虚拟机的请求
		int requestedVms = 0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		for (Vm vm : getVmList()) {//试图创建所有的虚拟机，并将创建请求发送到数据中心
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) { 
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
						+ " in " + datacenterName);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Destroy all virtual machines running in datacenters.
	 * 销毁所有在数据中心中的虚拟机的请求
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			Log.printConcatLine(CloudSim.clock(), ": " + getName(), ": Destroying VM #", vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}
		getVmsCreatedList().clear();
	}
	
	/**
	 * Send an internal event communicating the end of the simulation.
	 * 发送一个在模拟结束时的内部事件通信
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}
    
	@Override
	public void shutdownEntity() {
		Log.printConcatLine(getName(), " is shutting down...");
	}

	@Override
	public void startEntity() {
		Log.printConcatLine(getName(), " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}
	
	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
	 */
	protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment the number of acknowledges (ACKs) sent in response
         * to requests of VM creation.针对VM创建的请求增加响应中发送的确认(ack)的数量
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the vms to datacenters map.
	 * 获取vms到数据中心映射。
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 获取数据中心请求的id列表
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}
	public List<? extends Cloudlet> getCancledCloudletList() {
		return cancledCloudletList;
	}

	public void setCancledCloudletList(List<? extends Cloudlet> cancledCloudletList) {
		this.cancledCloudletList = cancledCloudletList;
	}
	/**
	 * Sets the datacenter requested ids list.
	 * 设置数据中心请求id列表
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}

}
