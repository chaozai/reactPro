/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */
package com.shiyan.demo;

import java.util.Calendar;
import java.util.List;

import com.shiyan.core.CloudSim;
import com.shiyan.init.InitShiyan;
import com.shiyan.main.Datacenter;
import com.shiyan.main.Log;
import com.shiyan.main.PowerDatacenterBroker;
import com.shiyan.main.PrintResult;
import com.shiyan.models.Cloudlet;
import com.shiyan.models.Host;
import com.shiyan.models.Vm;

/**
  * 我们通过引入QoS作为用户对服务的一项标准，用户在选择虚拟机（可以理解为任务选择合适的虚拟机去执行）时，会依照这项QoS需求选择虚拟机，
  * 当某个虚拟机不满足QoS需求时，任务会按照时间负载均衡的思想去选择其他的虚拟机执行，如果所有的虚拟机
  * 都不能满足任务的QoS需求，则此任务将被舍弃 ，当任务量不断增多时，这种方法必然会引起任务完成度不断减小，服务商的收益不能最大化
  * 因此我们选择降低用户的QoS标准来完成那些违背QoS的任务，最终我们将这种方法与FcFs,MinMin以及MaxMin相比较，来验证我们这种方法的可行性与价值
 */
public class MyshiyanMain {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	/** The vmList. */
	private static List<Vm> vmList;
	private static List<Host> hostList;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log.printLine("Starting MyshiyanMain...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			
			// before creating any entities. 第一步，在创建任何实体之前，初始化CloudSim包文件
			int num_user = 1;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library初始化ClousSim库文件
			CloudSim.init(num_user, calendar, trace_flag);
			
			// Second step: Create Datacenters 第二步，创建数据中心（数据中心包含一些基本参数，开销以及一些虚拟机和主机的相关放置策略等）
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
			
			//Third step: Create Broker第三步，创建代理（向数据中心提交各种请求以及接受返回的结果）
			PowerDatacenterBroker broker = InitShiyan.createBroker("Broker_0");
			int brokerId = broker.getId();
			hostList = InitShiyan.createHostList(5);//创建5个主机
			Datacenter datacenter0 = InitShiyan.createDatacenter("Datacenter_1",hostList);
			
			//Fourth step: Create VMs and Cloudlets and send them to broker，第四步，创建虚拟机列表和云任务列表
			vmList = InitShiyan.createVmList(20, brokerId, "Xen"); //creating 20 vms
			cloudletList = InitShiyan.createCloudletList(brokerId,300); // creating 300 cloudlets
			//datacenter0.setDisableMigrations(false);//设置数据中心是支持虚拟机迁移的
			broker.submitVmList(vmList);
             
			//下面三个算法请勿测试，与论文无关
			//broker.bindCloudletsToVmsByFitness();//基于适应度的任务分配算法
			//broker.bindCloudletsToVmsBySJF();//基于短任务优先的方法
		    //broker.bindCloudletsToVmsByLJF();//基于长任务优先的方法
			
			//下面是论文的所有算法调用
			broker.submitCloudletList(cloudletList);//如果不使用自己的算法，提交后，程序会自动选用FCFS-RR算法，
			
			//broker.bindCloudletsToVmsByIFCFSRR();//嵌入QoS需求的I-FCFS(RR)算法
			
			//broker.bindCloudletsToVmsMinMin();//Min-Min算法
		
		    //broker.bindCloudletsToVmsIMinMin();//嵌入Qos需求的I-MinMin算法
		
		    //broker.bindCloudletsToVmsMaxMin();//Max-Min算法
			
			//broker.bindCloudletsToVmsIMaxMin();//嵌入Qos的Max-Min算法
			
			//broker.bindCloudletsToVmsSufferage();//Sufferage算法
			
	        //broker.bindCloudletsToVmsISufferage();//嵌入Qos需求的Sufferage算法，太复杂，舍弃
			
			//基于QoS需求降级的QoS-MinMin算法
		    //broker.bindCloudletsToVmsQoSMinMin();
			//PrintResult.wayName="QoSMinMin";
		
			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			PrintResult.printCloudletList(cloudletList.size(),newList,vmList);

			Log.printLine("MyshiyanMain finished!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}
}