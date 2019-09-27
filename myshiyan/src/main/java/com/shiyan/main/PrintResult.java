package com.shiyan.main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.shiyan.models.Cloudlet;
import com.shiyan.models.Vm;

public class PrintResult {
	
	public static String wayName = null;
	
	@SuppressWarnings("deprecation")
	public static void printCloudletList(int cloudlets,List<Cloudlet> list,List<Vm> vmlist) {
		
		int size = list.size();
		Cloudlet cloudlet;
		double violateQosNum = 0.0;
		double waitTime = 0.0;
		double averageTime = 0.0;
		double variance = 0.0;
		double std = 0.0;
		double allVmprofit = 0.0;
		double allvmtcost = 0.0;
		double totalDeadTime =0,averageDeadTime=0;
		
		List<Double> vmExecTime = new ArrayList<Double>();
		
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + indent + "Exec Time" + 
				indent + indent+"Submit Time"+indent+"Start Time" + indent + "Finish Time"+ indent +indent+"Dead Line"
				+ indent +"Requested PE"+ indent +"Available PE"+ indent +"Vm Cost"+ indent+"Cloudlet Accepted Cost");

		DecimalFormat dft = new DecimalFormat("###.###");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() !=-1){
				Log.print(cloudlet.getCloudletStatusString());

				Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +indent + indent +indent +dft.format(cloudlet.getStartSubmit())
						+indent + indent +indent + dft.format(cloudlet.getExecStartTime())+ indent + indent + indent + dft.format(cloudlet.getFinishTime())
						+indent + indent + indent+dft.format(cloudlet.getDeadlineTime()+cloudlet.getStartSubmit())+indent+indent+indent+dft.format(cloudlet.getNumberOfPes())
						+indent + indent +indent+indent+dft.format(vmlist.get(cloudlet.getVmId()).getNumberOfPes())+indent +indent+indent+dft.
						format(cloudlet.getActualCPUTime()*vmlist.get(cloudlet.getVmId()).getCostPerVm())
						+indent +indent+indent+indent+dft.format(cloudlet.getUserMoney()));
			}
			waitTime+=cloudlet.getExecStartTime()-cloudlet.getStartSubmit();
			totalDeadTime += cloudlet.getDeadlineTime();	
		}
		averageDeadTime = totalDeadTime/size;
		//尽管我们通过预分配的方法来尽量满足用户的Qos,但是数据中心是复杂的，任务最终的执行时间有可能会发生变化，
		//因此违约率和收益率可能会有变化，这里我们需要将其记录下来
		Log.print("违反SLA的任务为：\n");	
		
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + indent + "Exec Time" + 
				indent +"Submit Time"+indent+"Start Time" + indent + indent+"Finish Time"+ indent +indent+"Dead Line"
				+ indent +"Requested PE"+ indent +"Available PE"+ indent +"Vm Cost"+ indent+"Cloudlet Accepted Cost");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Vm vm1 = vmlist.get(cloudlet.getVmId());
			
			if ((cloudlet.getFinishTime()>cloudlet.getDeadlineTime()+cloudlet.getStartSubmit())
			||cloudlet.getUserMoney()<(cloudlet.getActualCPUTime()*vm1.getCostPerVm()))
			{
				violateQosNum++;
				Log.print(indent + cloudlet.getCloudletId() + indent + indent);
				
				Log.print(cloudlet.getCloudletStatusString());
				Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +indent + indent +indent +dft.format(cloudlet.getStartSubmit())
						+indent + indent +indent + dft.format(cloudlet.getExecStartTime())+ indent + indent + indent + dft.format(cloudlet.getFinishTime())
						+indent + indent + indent+dft.format(cloudlet.getDeadlineTime()+cloudlet.getStartSubmit())+indent+indent+indent+dft.format(cloudlet.getNumberOfPes())
						+indent + indent +indent+indent+dft.format(vmlist.get(cloudlet.getVmId()).getNumberOfPes())+indent +indent+indent+dft.
						format(cloudlet.getActualCPUTime()*vmlist.get(cloudlet.getVmId()).getCostPerVm())
						+indent +indent+indent+indent+dft.format(cloudlet.getUserMoney()));
				//从这里开始我们计算服务商的收益
				if(checkFinalQoS(cloudlet,vm1)==0) {
					allVmprofit-=cloudlet.getActualCPUTime()*vm1.getCostPerVm()*0.2;
				}
				else if(checkFinalQoS(cloudlet,vm1)==1) {
					if(wayName==null||wayName.equals("")) {
						allVmprofit-=cloudlet.getActualCPUTime()*vm1.getCostPerVm()*0.2;
					}
					else if(wayName.equals("QoSMinMin")) {
						if(cloudlet.getDeadlineTime()<averageDeadTime) 
							allVmprofit += calculateCloudletFinalPay(cloudlet,vmlist.get(cloudlet.getVmId()),1);
						else
							allVmprofit += calculateCloudletFinalPay(cloudlet,vmlist.get(cloudlet.getVmId()),2);
					}
				}
			}
			else {
				allVmprofit+= cloudlet.getActualCPUTime()*vmlist.get(cloudlet.getVmId()).getCostPerVm()*0.8;
			}
		}
		//算出每一个虚拟机中执行的所有任务的花费，作为服务商的最终收益
		for (Vm vm: vmlist) {
			double maxFinishedTime=Double.MIN_VALUE;
			for (Cloudlet cloudlet1: list) {
				if(cloudlet1.getVmId()==vm.getId()) {
					if(cloudlet1.getFinishTime()>maxFinishedTime) {
						maxFinishedTime = cloudlet1.getFinishTime();
					}
					//allvmtcost+=cloudlet1.getActualCPUTime()*vm.getCostPerVm();
					//System.out.println("任务"+cloudlet1.getCloudletId()+"的支付费用为："+cloudlet1.getActualCPUTime()*vm.getCostPerVm());
				}
			}
			if(maxFinishedTime!=Double.MAX_VALUE) {
				//System.out.println("最终完成时间Vm"+vm.getId()+": "+maxFinishedTime/60/60+"时");
				vmExecTime.add(maxFinishedTime);
			}
		}
		if(vmExecTime!=null) {
			Double allVmExecTime = 0.0;
			Double nvariance = 0.0;
			
			for (Double vmexectime: vmExecTime) {
				allVmExecTime+=vmexectime;
			}
			//System.out.println("总时间"+allVmExecTime/60/60+"时");
			
			averageTime = allVmExecTime/vmExecTime.size();
			//System.out.println("平均完成时间"+averageTime/60/60+"时");
			
			for (Double vmexectime: vmExecTime) {
				nvariance += (vmexectime-averageTime)*(vmexectime-averageTime);
				//System.out.println("单偏差"+(vmexectime-averageTime));
			}
			
			variance = nvariance/vmExecTime.size();
			std = Math.sqrt(variance);
		}
        //1个用户有多个任务，各个任务间相互独立，且有各自的最高可接受花费，如果某个任务在一个虚拟机上的执行费用超过这个任务的最高可接受花费，
		//则这个任务将不会在这个虚拟机上运行，这项用户要求被添加到QoS需求中，以保证云服务商的服务水平
		Log.formatLine("\n用户提交的云任务为：%d个，完成的总任务为：%d个，任务完成度为：%.2f%%",cloudlets,size,((double)size/cloudlets*100));	
		
		Log.formatLine("违背要求的任务为：%.0f个，违约率为： %.2f%%",violateQosNum,violateQosNum/size*100);	
		Log.format("任务最终完成时间(makespan)为：%.2f时，任务平均完成时间为：%.2f秒\n",
				(double)((list.get(size-1).getFinishTime())/60/60),(double)((list.get(size-1).getFinishTime())/size));	
		Log.formatLine("任务平均等待时间为：%.2f时",
				(double)(waitTime/size)/60/60);
		Log.formatLine("服务商收益为：%.2f元",allVmprofit);
	}
    public static int checkFinalQoS(Cloudlet cloudlet, Vm vm) {
    	if(cloudlet.getActualCPUTime()*vm.getCostPerVm() > cloudlet.getUserMoney())
			return 0;
		if(cloudlet.getFinishTime()>cloudlet.getDeadlineTime()+cloudlet.getStartSubmit())
			return 1;
		return 2;
	}
	//这里用来计算当我们降低QoS需求时，服务商最终所收到的盈利
	public static double calculateCloudletFinalPay(Cloudlet cloudlet,Vm vm, int type) {
		//获得云任务的延迟时间
		double cloudletDealyTime = cloudlet.getFinishTime() - cloudlet.getDeadlineTime();
		//任务原收益
		//double cloudletPrimaryProfit = cloudlet.getUserMoney()-cloudlet.getCloudletLength()/vm.getMips()*vm.getCostPerVm();
		//补偿率=任务延迟时间/任务截止时间
		//double compensationProbability = cloudletDealyTime/(cloudletDealyTime-cloudlet.getStartSubmit());
		
		//服务商正常收入
		double VmNormalIncome = vm.getCostPerVm()*cloudlet.getActualCPUTime();
		
		//补偿成本=虚拟机单位运行成本*虚拟机预计运行时间/（任务类型*（任务截止时间-任务提交时间））*任务延迟时间
		double compensationCost = VmNormalIncome/(type*(cloudlet.getDeadlineTime()-cloudlet.getStartSubmit()))*cloudletDealyTime;
		//double compensationCost = cloudlet.getUserMoney()/(type*(cloudlet.getDeadlineTime()-cloudlet.getStartSubmit()))*cloudletDealyTime;
		//云任务最终花费=服务商正常收入-补偿成本
		double cloudletFinalCost = VmNormalIncome - compensationCost;
		
		//服务商最终盈利=服务商正常收入*80%-补偿成本
		//虚拟机执行成本=服务商正常收入*20%
		double cloudletFinalProfit = VmNormalIncome - VmNormalIncome*0.2 - compensationCost;
		if(cloudletFinalCost > cloudlet.getUserMoney())
			cloudletFinalProfit = -VmNormalIncome*0.2;//如果云任务最终花费大于用户的最高承受价格，用户将不会为任务支付任何费用，并且还会减去虚拟机的执行成本
		return cloudletFinalProfit;
		
		
	}
}
