package com.shiyan.init;

/**
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 *
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 *
 * @author Anton Beloglazov
 * @since Jan 6, 2012
 */
public class Constants {
   
	public final static int CLOUDLET_TYPE      = 10;
	//任务的最高接受价格
	public final static double[] USER_MONEY    = {0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.1};//元
	public final static int[] CLOUDLET_LENGTH  = {10000,15000,20000,25000,30000,35000,40000,45000,50000,55000};//单位是百万 
	public final static int CLOUDLET_PES       = 1;
	public final static int[] CLOUDLET_DEADLINETIME = {1*60*60,2*60*60,3*60*60,4*60*60,5*60*60};//s
	//public final static int[] CLOUDLET_DEADLINETIME = {10,9,1,2,8,7,3,4,6,5};//s
	//public final static double[] VMFITWEIGHT   = {0.6,0.4};
//	public final static double K = 1.0;
//	public final static double WF_THRESHOLD    = 5.0;
//	public final static double WP_THRESHOLD    = 0.5;
//	public final static double WP_STANDAED     = 100.0;
	/*
	 * VM instance types:
	 *   High-Memory Extra Large Instance: 3.25 EC2 Compute Units, 8.55 GB // too much MIPS
	 *   High-CPU Medium Instance: 2.5 EC2 Compute Units, 0.85 GB
	 *   Extra Large Instance: 2 EC2 Compute Units, 3.75 GB
	 *   Small Instance: 1 EC2 Compute Unit, 1.7 GB
	 *   Micro Instance: 0.5 EC2 Compute Unit, 0.633 GB
	 *   We decrease the memory size two times to enable oversubscription
	  *  我们使用阿里云的虚拟服务器的一些参数作为我们的测试虚拟机
	  *  这里规定一个机器周期等于四个时钟周期，每条指令等于三个机器周期，mips只与主频有关。
	 */
	public final static int VM_TYPES	= 5;//虚拟机类型对应我们选择的实例类型
	public final static double[] VM_MIPS= {42, 83, 125, 167, 208};//计算能力，最高按照cpu主频2.5GHz来计算mips
	public final static int[] VM_PES	= {1, 1, 2, 1, 2};//核数，每个cpu的核数
	public final static int[] VM_RAM	= {1*1024, 2*1024, 4*1024,4*1024,8*1024};//Mbit
	public final static long[] VM_BW	= {512, 512, 512, 512, 512}; // 100 Mbit/s
	public final static int[] VM_SIZE	= {2500,1000,3000,4500,3500}; // 2.5 GB
	public final static double[] VM_COST= {0.16/60/60,0.29/60/60,0.71/60/60,0.54/60/60,1.08/60/60};//RMB元

	/*
	 * Host types:
	 *   HP ProLiant ML110 G4 (1 x [Xeon 3040 1860 MHz, 2 cores], 4GB)
	 *   HP ProLiant ML110 G5 (1 x [Xeon 3075 2660 MHz, 2 cores], 4GB)
	 *   We increase the memory size to enable over-subscription (x4)
	 *   2.5 GHz主频的Intel Xeon E5-2682 v4（Broadwell）
	 */
	public final static int HOST_TYPES	 = 5;
	public final static int[] HOST_MIPS	 = {600,700,800,900,1000};
	public final static int[] HOST_PES	 = {11,12,13,14,15};
	public final static int[] HOST_RAM	 = {60000,70000,80000,90000,100000};
	public final static long HOST_BW     = 10240; // 10G/s=10240M/s
	public final static int HOST_STORAGE = 100000; // 100G

}
