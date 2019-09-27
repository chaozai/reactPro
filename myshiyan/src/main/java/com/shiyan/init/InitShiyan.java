package com.shiyan.init;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.shiyan.main.CloudletSchedulerSpaceShared;
import com.shiyan.main.Datacenter;
import com.shiyan.main.DatacenterCharacteristics;
import com.shiyan.main.Pe;
import com.shiyan.main.PowerDatacenterBroker;
import com.shiyan.main.Storage;
import com.shiyan.main.VmAllocationPolicySimple;
import com.shiyan.main.VmSchedulerTimeShared;
import com.shiyan.models.Cloudlet;
import com.shiyan.models.Host;
import com.shiyan.models.UtilizationModel;
import com.shiyan.models.UtilizationModelFull;
import com.shiyan.models.Vm;
import com.shiyan.provisioners.BwProvisionerSimple;
import com.shiyan.provisioners.PeProvisionerSimple;
import com.shiyan.provisioners.RamProvisionerSimple;

public class InitShiyan {

	public static List<Cloudlet> createCloudletList(int brokeid,int cloudletsnumber) {
		List<Cloudlet> cloudletlist = new ArrayList<Cloudlet>();
		
		UtilizationModel utilizationModelfull = new UtilizationModelFull();
        
		for (int i = 0; i < cloudletsnumber; i++) {
			Cloudlet cloudlet = null;
				cloudlet = new Cloudlet(
						i,
						Constants.CLOUDLET_LENGTH[i%Constants.CLOUDLET_TYPE],
						Constants.CLOUDLET_DEADLINETIME[i%(Constants.CLOUDLET_TYPE/2)],
						Constants.CLOUDLET_PES,
						utilizationModelfull,
						utilizationModelfull,
						utilizationModelfull,
						Constants.USER_MONEY[i%Constants.CLOUDLET_TYPE]);
			cloudlet.setUserId(brokeid);
			cloudlet.setVmId(-1);
			cloudletlist.add(cloudlet);
		}

		return cloudletlist;
	}
	
	public static List<Vm> createVmList(int vmsnumber,int brokerid,String vmmname) {
		List<Vm> vmlist = new ArrayList<Vm>();
		for (int i = 0; i < vmsnumber; i++) {
			Vm vm = null;
			vm = new Vm(
					i, 
					brokerid, 
					Constants.VM_MIPS[i%Constants.VM_TYPES], 
					Constants.VM_PES[i%Constants.VM_TYPES],
					Constants.VM_RAM[i%Constants.VM_TYPES], 
					Constants.VM_BW[i%Constants.VM_TYPES], 
					Constants.VM_SIZE[i%Constants.VM_TYPES], 
					Constants.VM_COST[i%Constants.VM_TYPES],
					vmmname, 
					new CloudletSchedulerSpaceShared());
			
			vmlist.add(vm);
		}
		return vmlist;
	}
	
	public static List<Host> createHostList(int hostnumber) {
		List<Host> hostList = new ArrayList<Host>();
		for (int i = 0; i < hostnumber; i++) {
			int hostType = i % Constants.HOST_TYPES;

			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < Constants.HOST_PES[hostType]; j++) {
				peList.add(new Pe(j, new PeProvisionerSimple(Constants.HOST_MIPS[hostType])));
			}
           
			hostList.add(
	    			new Host(
	    				i,
	    				new RamProvisionerSimple(Constants.HOST_RAM[i%Constants.HOST_TYPES]),
	    				new BwProvisionerSimple(Constants.HOST_BW),
	    				Constants.HOST_STORAGE,
	    				peList,
	    				new VmSchedulerTimeShared(peList)
	    			)
	    		); // This is our first machine
		}
		
		return hostList;
	}
	public static PowerDatacenterBroker createBroker(String name){

		PowerDatacenterBroker broker = null;
		try {
			broker = new PowerDatacenterBroker(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	
    public static Datacenter createDatacenter(String name, List<Host> hostList){
		
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.1;	// the cost of using storage in this resource
		double costPerBw = 0.1;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
		Datacenter datacenter = null;
				try {
			
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
}
