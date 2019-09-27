/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package com.shiyan.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.shiyan.core.CloudSim;
import com.shiyan.core.CloudSimTags;
import com.shiyan.core.SimEntity;
import com.shiyan.core.SimEvent;
import com.shiyan.models.Cloudlet;
import com.shiyan.models.Host;
import com.shiyan.models.Vm;

/**
 * Datacenter class is a CloudResource whose hostList are virtualized. It deals with processing of
 * VM queries (i.e., handling of VMs) instead of processing Cloudlet-related queries. 
 * 
 * So, even though an AllocPolicy will be instantiated (in the init() method of the superclass, 
 * it will not be used, as processing of cloudlets are handled by the CloudletScheduler and 
 * processing of VirtualMachines are handled by the VmAllocationPolicy.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 * 
 * @todo In fact, there isn't the method init() in the super class, as stated in
 * the documentation here. An AllocPolicy isn't being instantiated there.
 * The last phrase of the class documentation appears to be out-of-date or wrong.
 */
public class Datacenter extends SimEntity {
   int count1 = 0;
	/** The characteristics. */
	private DatacenterCharacteristics characteristics;

	/** The regional Cloud Information Service (CIS) name. 区域云信息服�?(CIS)名称
         * @see com.shiyan.core.CloudInformationService
         */
	private String regionalCisName;

	/** The vm provisioner. */
	private VmAllocationPolicy vmAllocationPolicy;

	/** The last time some cloudlet was processed in the datacenter. 上次在数据中心中处理了一些cloudlet*/
	private double lastProcessTime;

	/** The storage list.存储列表 */
	private List<Storage> storageList;

	/** The vm list. */
	private List<? extends Vm> vmList;

	/** The scheduling delay to process each datacenter received event. */
	private double schedulingInterval;

	/**
	 * Allocates a new Datacenter object.
	 * 
	 * @param name the name to be associated with this entity (as required by the super class)
	 * @param characteristics the characteristics of the datacenter to be created
	 * @param storageList a List of storage elements, for data simulation 用于数据模拟的存储元素列�?
	 * @param vmAllocationPolicy the policy to be used to allocate VMs into hosts
         * @param schedulingInterval the scheduling delay to process each datacenter received event
	 * @throws Exception when one of the following scenarios occur:
	 *  <ul>
	 *    <li>creating this entity before initializing CloudSim package
	 *    <li>this entity name is <tt>null</tt> or empty
	 *    <li>this entity has <tt>zero</tt> number of PEs (Processing Elements). <br/>
	 *    No PEs mean the Cloudlets can't be processed. A CloudResource must contain 
	 *    one or more Machines. A Machine must contain one or more PEs.
	 *  </ul>
         * 
	 * @pre name != null
	 * @pre resource != null
	 * @post $none
	 */
	public Datacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name);

		setCharacteristics(characteristics);
		setVmAllocationPolicy(vmAllocationPolicy);
		setLastProcessTime(0.0);
		setStorageList(storageList);
		setVmList(new ArrayList<Vm>());
		setSchedulingInterval(schedulingInterval);

		for (Host host : getCharacteristics().getHostList()) {
			host.setDatacenter(this);//一开始就将所有的虚拟机的数据中心设置为本数据中心
		}

		// If this resource doesn't have any PEs then no useful at all 如果这个资源没有任何PEs，那么它就没有任何用处
		if (getCharacteristics().getNumberOfPes() == 0) {
                    throw new Exception(super.getName()
                        + " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
		}

		// stores id of this class
		getCharacteristics().setId(super.getId());
	}

	/**
	 * Overrides this method when making a new and different type of resource. <br>
	 * <b>NOTE:</b> You do not need to override {@link #body()} method, if you use this method.
	 * 
	 * @pre $none
	 * @post $none
         * 
         * @todo This method doesn't appear to be used
	 */
	protected void registerOtherEntity() {
		// empty. This should be override by a child class
	}

	@Override//重中之重
	public void processEvent(SimEvent ev) {
		int srcId = -1;

		switch (ev.getTag()) {
		// Resource characteristics inquiry
			case CloudSimTags.RESOURCE_CHARACTERISTICS://表示云资源特征信�?
				srcId = ((Integer) ev.getData()).intValue();
				sendNow(srcId, ev.getTag(), getCharacteristics());
				break;

			// Resource dynamic info inquiry
			case CloudSimTags.RESOURCE_DYNAMICS://表示云资源分配策�?
				srcId = ((Integer) ev.getData()).intValue();
				sendNow(srcId, ev.getTag(), 0);
				break;

			case CloudSimTags.RESOURCE_NUM_PE://表示获取资源的处理元素�?�数(PEs)的请�?
				srcId = ((Integer) ev.getData()).intValue();
				int numPE = getCharacteristics().getNumberOfPes();
				sendNow(srcId, ev.getTag(), numPE);
				break;

			case CloudSimTags.RESOURCE_NUM_FREE_PE://表示获取资源的自由处理元素�?�数(PEs)的请�?
				srcId = ((Integer) ev.getData()).intValue();
				int freePesNumber = getCharacteristics().getNumberOfFreePes();
				sendNow(srcId, ev.getTag(), freePesNumber);
				break;

			// New Cloudlet arrives
			case CloudSimTags.CLOUDLET_SUBMIT://表示云任务的提交
				processCloudletSubmit(ev, false);
				break;

			// New Cloudlet arrives, but the sender asks for an ack 新的Cloudlet到达，但是发送方要求�?个ack
			case CloudSimTags.CLOUDLET_SUBMIT_ACK://表示提交带有确认信息的Cloudlet�?
				processCloudletSubmit(ev, true);
				break;

			// Cancels a previously submitted Cloudlet 取消之前提交的Cloudlet
			case CloudSimTags.CLOUDLET_CANCEL://取消在CloudResource实体中提交的Cloudlet�?
				processCloudlet(ev, CloudSimTags.CLOUDLET_CANCEL);
				break;

			// Pauses a previously submitted Cloudlet 暂停之前提交的Cloudlet
			case CloudSimTags.CLOUDLET_PAUSE://暂停在CloudResource实体中提交的Cloudlet�?
				processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE);
				break;

			// Pauses a previously submitted Cloudlet, but the sender
			// asks for an acknowledgement 暂停之前提交的Cloudlet，但发�?�方要求确认
			case CloudSimTags.CLOUDLET_PAUSE_ACK:
				processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE_ACK);
				break;

			// Resumes a previously submitted Cloudlet 恢复之前提交的Cloudlet
			case CloudSimTags.CLOUDLET_RESUME://恢复在CloudResource实体中提交的Cloudlet
				processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME);
				break;

			// Resumes a previously submitted Cloudlet, but the sender
			// asks for an acknowledgement恢复之前提交的Cloudlet,但发送方要求确认
			case CloudSimTags.CLOUDLET_RESUME_ACK://恢复在CloudResource实体中提交的Cloudlet，并提供确认
				processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME_ACK);
				break;

			// Moves a previously submitted Cloudlet to a different resource 将先前提交的Cloudlet移动到另�?个资�?
			case CloudSimTags.CLOUDLET_MOVE://将一个Cloudlet移动到另�?个CloudResource实体�?
				processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE);
				break;

			// Moves a previously submitted Cloudlet to a different resource 将先前提交的Cloudlet移动到另�?个资�?
			case CloudSimTags.CLOUDLET_MOVE_ACK://将一个Cloudlet移动到另�?个CloudResource实体，并提供�?个确�?
				processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE_ACK);
				break;

			// Checks the status of a Cloudlet �?查云任务的状�?
			case CloudSimTags.CLOUDLET_STATUS://表示Cloudlet的状态�??
				processCloudletStatus(ev);
				break;

			// Ping packet Ping�?
			case CloudSimTags.INFOPKT_SUBMIT://实体使用此标记发送ping请求
				processPingRequest(ev);
				break;

			case CloudSimTags.VM_CREATE://表示在数据中心中创建新VM的请求，其中包含数据中心发�?�的确认信息�?
				processVmCreate(ev, false);
				break;

			case CloudSimTags.VM_CREATE_ACK://表示在数据中心中创建新VM的请求，其中包含数据中心发�?�的确认信息
				processVmCreate(ev, true);
				break;

			case CloudSimTags.VM_DESTROY://表示在数据中心中�?毁新VM的请�?
				processVmDestroy(ev, false);
				break;

			case CloudSimTags.VM_DESTROY_ACK://表示�?毁数据中心中的新VM的请求，其中包含由数据发送器发�?�的确认信息
				processVmDestroy(ev, true);
				break;

			case CloudSimTags.VM_MIGRATE://表示在数据中心中迁移新VM的请求�??
				processVmMigrate(ev, false);//指示事件的发送方是否希望在事件完成处理时接收到确认消�?
				break;

			case CloudSimTags.VM_MIGRATE_ACK://表示在数据中心中迁移新VM的请求，其中包含由数据中心发送的确认信息�?
				processVmMigrate(ev, true);
				break;

			case CloudSimTags.VM_DATA_ADD://表示将文件从用户发�?�到数据中心的事�?
				processDataAdd(ev, false);
				break;

			case CloudSimTags.VM_DATA_ADD_ACK://表示�?个事件，用于将文件从用户发�?�到数据中心，其中包含由数据中心发�?�的确认信息
				processDataAdd(ev, true);
				break;

			case CloudSimTags.VM_DATA_DEL://表示从数据中心删除文件的事件�?
				processDataDelete(ev, false);
				break;

			case CloudSimTags.VM_DATA_DEL_ACK://表示从Datacener发�?�确认信息的数据中心中移除文件的事件�?
				processDataDelete(ev, true);
				break;

			case CloudSimTags.VM_DATACENTER_EVENT://表示在数据中心里生成的内部事件�??
				updateCloudletProcessing();//更新在此数据中心中运行的每个cloudlet的处理�?�这是必要的，因为主机和虚拟机是�?单的对象�?
				//而不是实体�?�因此，它们不接收事件，并且必须从外部调用内部的cloudlets来更新它们�??
				checkCloudletCompletion();//验证此数据中心中的一些cloudlet是否已经完成，如果是，将其发送给datacenterbroker
				break;

			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Process a file deletion request.处理文件删除请求�?
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
	 */
	protected void processDataDelete(SimEvent ev, boolean ack) {
		if (ev == null) {
			return;
		}

		Object[] data = (Object[]) ev.getData();
		if (data == null) {
			return; 
		}

		String filename = (String) data[0];
		int req_source = ((Integer) data[1]).intValue();
		int tag = -1;

		// check if this file can be deleted (do not delete is right now)�?查这个文件是否可以删�?(现在不删�?)
		int msg = deleteFileFromStorage(filename);
		if (msg == DataCloudTags.FILE_DELETE_SUCCESSFUL) {
			tag = DataCloudTags.CTLG_DELETE_MASTER;
		} else { // if an error occured, notify user
			tag = DataCloudTags.FILE_DELETE_MASTER_RESULT;
		}

		if (ack) {
			// send back to sender返回到发送方
			Object pack[] = new Object[2];
			pack[0] = filename;
			pack[1] = Integer.valueOf(msg);

			sendNow(req_source, tag, pack);
		}
	}

	/**
	 * Process a file inclusion request.处理文件包含请求
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
	 */
	protected void processDataAdd(SimEvent ev, boolean ack) {
		if (ev == null) {
			return;
		}

		Object[] pack = (Object[]) ev.getData();
		if (pack == null) {
			return;
		}

		File file = (File) pack[0]; // get the file
		file.setMasterCopy(true); // set the file into a master copy
		int sentFrom = ((Integer) pack[1]).intValue(); // get sender ID

		/******
		 * // DEBUG Log.printLine(super.get_name() + ".addMasterFile(): " + file.getName() +
		 * " from " + CloudSim.getEntityName(sentFrom));
		 *******/

		Object[] data = new Object[3];
		data[0] = file.getName();

		int msg = addFile(file); // add the file

		if (ack) {
			data[1] = Integer.valueOf(-1); // no sender id
			data[2] = Integer.valueOf(msg); // the result of adding a master file
			sendNow(sentFrom, DataCloudTags.FILE_ADD_MASTER_RESULT, data);
		}
	}

	/**
	 * Processes a ping request.处理ping请求
	 * 
	 * @param ev information about the event just happened
         * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processPingRequest(SimEvent ev) {
		InfoPacket pkt = (InfoPacket) ev.getData();
		pkt.setTag(CloudSimTags.INFOPKT_RETURN);
		pkt.setDestId(pkt.getSrcId());

		// sends back to the sender
		sendNow(pkt.getSrcId(), CloudSimTags.INFOPKT_RETURN, pkt);//此标记用于将ping请求返回给发送方
	}

	/**
	 * Process the event for an User/Broker who wants to know the status of a Cloudlet. This
	 * Datacenter will then send the status back to the User/Broker.
	 * 
	 * @param ev information about the event just happened
         * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processCloudletStatus(SimEvent ev) {//希望了解Cloudlet状态的用户/代理处理事件,然后Datacenter将把状�?�发送回用户/代理�?
		int cloudletId = 0;
		int userId = 0;
		int vmId = 0;
		int status = -1;

		try {
			// if a sender using cloudletXXX() methods
			int data[] = (int[]) ev.getData();
			cloudletId = data[0];
			userId = data[1];
			vmId = data[2];

			status = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId).getCloudletScheduler()
					.getCloudletStatus(cloudletId);
		}

		// if a sender using normal send() methods
		catch (ClassCastException c) {
			try {
				Cloudlet cl = (Cloudlet) ev.getData();
				cloudletId = cl.getCloudletId();
				userId = cl.getUserId();

				status = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
						.getCloudletScheduler().getCloudletStatus(cloudletId);
			} catch (Exception e) {
				Log.printConcatLine(getName(), ": Error in processing CloudSimTags.CLOUDLET_STATUS");
				Log.printLine(e.getMessage());
				return;
			}
		} catch (Exception e) {
			Log.printConcatLine(getName(), ": Error in processing CloudSimTags.CLOUDLET_STATUS");
			Log.printLine(e.getMessage());
			return;
		}

		int[] array = new int[3];//三个�?
		array[0] = getId();
		array[1] = cloudletId;
		array[2] = status;

		int tag = CloudSimTags.CLOUDLET_STATUS;
		sendNow(userId, tag, array);
	}

	/**
	 * Process non-default received events that aren't processed by 处理未被处理的非默认接收事件
         * the {@link #processEvent(com.shiyan.core.SimEvent)} method.
         * This method should be overridden by subclasses in other to process
         * new defined events.
	 * 
	 * @param ev information about the event just happened
         * 
	 * @pre $none
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printConcatLine(getName(), ".processOtherEvent(): Error - an event is null.");
		}
	}

	/**
	 * Process the event for an User/Broker who wants to create a VM in this Datacenter. This
	 * Datacenter will then send the status back to the User/Broker.为希望在此数据中心中创建VM的用代理处理事件�?
	 * 这然后，Datacenter将把状�?�发送回用户/代理�?
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
         * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev, boolean ack) {//处理来自数据中心代理发送过来的创建虚拟机的请求
		Vm vm = (Vm) ev.getData();
		
		boolean result = getVmAllocationPolicy().allocateHostForVm(vm);//这里可以定义自己的策略，在创建虚拟机的时候为其选择合适的主机

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			send(vm.getUserId(), CloudSim.getMinTimeBetweenEvents(), //创建数组列表事件存储虚拟机的分配信息,并向数据中心发送虚拟机创建的返回信息
					CloudSimTags.VM_CREATE_ACK, data);
		}

		if (result) {
			getVmList().add(vm);

			if (vm.isBeingInstantiated()) {
				vm.setBeingInstantiated(false);
			}

			vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
					.getAllocatedMipsForVm(vm));//更新在此虚拟机上运行的所有的云任务
		}
	}

	/**
	 * Process the event for an User/Broker who wants to destroy a VM previously created in this为希望销毁先前在此创建的VM的代理处理事�?
	 * Datacenter. This Datacenter may send, upon request, the status back to the
	 * User/Broker.
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
         * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmDestroy(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();
		getVmAllocationPolicy().deallocateHostForVm(vm);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();
			data[2] = CloudSimTags.TRUE;

			sendNow(vm.getUserId(), CloudSimTags.VM_DESTROY_ACK, data);
		}

		getVmList().remove(vm);
	}

	/**
	 * Process the event for an User/Broker who wants to migrate a VM. This Datacenter will
	 * then send the status back to the User/Broker.
	 * 为希望迁移VM的用�?/代理处理事件
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
         * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmMigrate(SimEvent ev, boolean ack) {
		Object tmp = ev.getData();
		if (!(tmp instanceof Map<?, ?>)) {
			throw new ClassCastException("The data object must be Map<String, Object>");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> migrate = (HashMap<String, Object>) tmp;

		Vm vm = (Vm) migrate.get("vm");
		Host host = (Host) migrate.get("host");

		getVmAllocationPolicy().deallocateHostForVm(vm);
		host.removeMigratingInVm(vm);
		
		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, host);
		
		if (!result) {
			Log.printLine("[Datacenter.processVmMigrate] VM allocation to the destination host failed");
			System.exit(0);
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(ev.getSource(), CloudSimTags.VM_CREATE_ACK, data);
		}

		Log.formatLine(
				"%.2f: Migration of VM #%d to Host #%d is completed",
				CloudSim.clock(),
				vm.getId(),
				host.getId());
		vm.setInMigration(false);
	}

	/**
	 * Processes a Cloudlet based on the event type. 根据事件类型处理Cloudlet
	 * 
	 * @param ev information about the event just happened
	 * @param type event type
         * 
	 * @pre ev != null
	 * @pre type > 0
	 * @post $none
	 */
	protected void processCloudlet(SimEvent ev, int type) {
		int cloudletId = 0;
		int userId = 0;
		int vmId = 0;

		try { // if the sender using cloudletXXX() methods
			int data[] = (int[]) ev.getData();
			cloudletId = data[0];
			userId = data[1];
			vmId = data[2];
		}

		// if the sender using normal send() methods 如果发�?�方使用普�?�的send()方法
		catch (ClassCastException c) {
			try {
				Cloudlet cl = (Cloudlet) ev.getData();
				cloudletId = cl.getCloudletId();
				userId = cl.getUserId();
				vmId = cl.getVmId();
			} catch (Exception e) {
				Log.printConcatLine(super.getName(), ": Error in processing Cloudlet");
				Log.printLine(e.getMessage());
				return;
			}
		} catch (Exception e) {
			Log.printConcatLine(super.getName(), ": Error in processing a Cloudlet.");
			Log.printLine(e.getMessage());
			return;
		}

		// begins executing .... �?始执�?...
		switch (type) {
			case CloudSimTags.CLOUDLET_CANCEL:
				processCloudletCancel(cloudletId, userId, vmId);
				break;

			case CloudSimTags.CLOUDLET_PAUSE:
				processCloudletPause(cloudletId, userId, vmId, false);
				break;

			case CloudSimTags.CLOUDLET_PAUSE_ACK:
				processCloudletPause(cloudletId, userId, vmId, true);
				break;

			case CloudSimTags.CLOUDLET_RESUME:
				processCloudletResume(cloudletId, userId, vmId, false);
				break;

			case CloudSimTags.CLOUDLET_RESUME_ACK:
				processCloudletResume(cloudletId, userId, vmId, true);
				break;
			default:
				break;
		}

	}

	/**
	 * Process the event for an User/Broker who wants to move a Cloudlet.
	 * 为希望移动Cloudlet的用�?/代理处理事件
	 * @param receivedData information about the migration
	 * @param type event type
	 * @pre receivedData != null
	 * @pre type > 0
	 * @post $none
	 */
	protected void processCloudletMove(int[] receivedData, int type) {
		updateCloudletProcessing();

		int[] array = receivedData;
		int cloudletId = array[0];
		int userId = array[1];
		int vmId = array[2];
		int vmDestId = array[3];
		int destId = array[4];

		// get the cloudlet
		Cloudlet cl = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
				.getCloudletScheduler().cloudletCancel(cloudletId);

		boolean failed = false;
		if (cl == null) {// cloudlet doesn't exist
			failed = true;
		} else {
			// has the cloudlet already finished?
			if (cl.getCloudletStatusString() == "Success") {// if yes, send it back to user
				int[] data = new int[3];
				data[0] = getId();
				data[1] = cloudletId;
				data[2] = 0;
				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
			}

			// prepare cloudlet for migration
			cl.setVmId(vmDestId);

			// the cloudlet will migrate from one vm to another does the destination VM exist?
			if (destId == getId()) {
				Vm vm = getVmAllocationPolicy().getHost(vmDestId, userId).getVm(vmDestId,userId);
				if (vm == null) {
					failed = true;
				} else {
					// time to transfer the files
					double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());
					vm.getCloudletScheduler().cloudletSubmit(cl, fileTransferTime);
				}
			} else {// the cloudlet will migrate from one resource to another
				int tag = ((type == CloudSimTags.CLOUDLET_MOVE_ACK) ? CloudSimTags.CLOUDLET_SUBMIT_ACK
						: CloudSimTags.CLOUDLET_SUBMIT);
				sendNow(destId, tag, cl);
			}
		}

		if (type == CloudSimTags.CLOUDLET_MOVE_ACK) {// send ACK if requested
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (failed) {
				data[2] = 0;
			} else {
				data[2] = 1;
			}
			sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet submission.处理Cloudlet提交�?
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
         * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		updateCloudletProcessing();

		try {
			// gets the Cloudlet object 获取Cloudlet对象
			Cloudlet cl = (Cloudlet) ev.getData();
			
			// checks whether this Cloudlet has finished or not �?查这个云任务是否已经完成
			//若没有执行完成则跳过，执行完则打印消息并发�?�云任务事件返回标记
			if (cl.isFinished()) {
				String name = CloudSim.getEntityName(cl.getUserId());
				Log.printConcatLine(getName(), ": Warning - Cloudlet #", cl.getCloudletId(), " owned by ", name,
						" is already completed/finished.");
				Log.printLine("Therefore, it is not being executed again");
				Log.printLine();

				// NOTE: If a Cloudlet has finished, then it won't be processed.
				// So, if ack is required, this method sends back a result.
				// If ack is not required, this method don't send back a result.
				// Hence, this might cause CloudSim to be hanged since waiting
				// for this Cloudlet back.
				if (ack) {
					int[] data = new int[3];
					data[0] = getId();
					data[1] = cl.getCloudletId();
					data[2] = CloudSimTags.FALSE;

					// unique tag = operation tag 操作标记
					int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
					sendNow(cl.getUserId(), tag, data);
				}

				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);

				return;
			}

			// process this Cloudlet to this CloudResource处理这个云任务到这个云资源，将云任务打包成云资源（将此云任务的花费打包）
			cl.setResourceParameter(
                                getId(), getCharacteristics().getCostPerSecond(), 
                                getCharacteristics().getCostPerBw());

			int userId = cl.getUserId();//得到此云任务上用户id
			int vmId = cl.getVmId();//得到此云任务上的虚拟机id

			// time to transfer the files 转移文件时间
			double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());//预测执行该任务时，传输文件列表的总时�?

			Host host = getVmAllocationPolicy().getHost(vmId, userId);//根据vmId和userId获得虚拟机分配策略（前面已经为VM分配好主机）中的主机
			Vm vm = host.getVm(vmId, userId);//得到此主机上的vm
			CloudletScheduler scheduler = vm.getCloudletScheduler();//得到此虚拟机的云任务调度策略
			double estimatedFinishTime = scheduler.cloudletSubmit(cl, fileTransferTime);//估计此云任务的完成时�?

			// if this cloudlet is in the exec queue如果这个cloudlet在exec队列�?
			if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
				estimatedFinishTime += fileTransferTime;//重置估计完成时间为：估计完成时间加上文件传输时间
				send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);//创建内部事件
			}

			if (ack) {
				int[] data = new int[3];
				data[0] = getId();
				data[1] = cl.getCloudletId();
				data[2] = CloudSimTags.TRUE;

				// unique tag = operation tag
				int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
				sendNow(cl.getUserId(), tag, data);
			}
		} catch (ClassCastException c) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
			c.printStackTrace();
		} catch (Exception e) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
			e.printStackTrace();
		}

		checkCloudletCompletion();//�?查每个主机上的虚拟机上的云任务，将已经完成的云任务发送给代理
	}

	/**
	 * Predict the total time to transfer a list of files.
	 * 预测传输文件列表的�?�时�?
	 * @param requiredFiles the files to be transferred
	 * @return the predicted time
	 */
	protected double predictFileTransferTime(List<String> requiredFiles) {
		double time = 0.0;

		Iterator<String> iter = requiredFiles.iterator();
		while (iter.hasNext()) {
			String fileName = iter.next();
			for (int i = 0; i < getStorageList().size(); i++) {
				Storage tempStorage = getStorageList().get(i);
				File tempFile = tempStorage.getFile(fileName);
				if (tempFile != null) {
					time += tempFile.getSize() / tempStorage.getMaxTransferRate();
					break;
				}
			}
		}
		return time;
	}        

	/**
	 * Processes a Cloudlet resume request.
	 * 
	 * @param cloudletId ID of the cloudlet to be resumed
	 * @param userId ID of the cloudlet's owner
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
	 * @param vmId the id of the VM where the cloudlet has to be resumed
         * 
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletResume(int cloudletId, int userId, int vmId, boolean ack) {
		double eventTime = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
				.getCloudletScheduler().cloudletResume(cloudletId);

		boolean status = false;
		if (eventTime > 0.0) { // if this cloudlet is in the exec queue
			status = true;
			if (eventTime > CloudSim.clock()) {
				schedule(getId(), eventTime, CloudSimTags.VM_DATACENTER_EVENT);
			}
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (status) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(userId, CloudSimTags.CLOUDLET_RESUME_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet pause request.
	 * 
	 * @param cloudletId ID of the cloudlet to be paused
	 * @param userId ID of the cloudlet's owner
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
	 * @param vmId the id of the VM where the cloudlet has to be paused
         * 
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletPause(int cloudletId, int userId, int vmId, boolean ack) {
		boolean status = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
				.getCloudletScheduler().cloudletPause(cloudletId);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (status) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(userId, CloudSimTags.CLOUDLET_PAUSE_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet cancel request.
	 * 
	 * @param cloudletId ID of the cloudlet to be canceled
	 * @param userId ID of the cloudlet's owner
	 * @param vmId the id of the VM where the cloudlet has to be canceled
         * 
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletCancel(int cloudletId, int userId, int vmId) {
		Cloudlet cl = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
				.getCloudletScheduler().cloudletCancel(cloudletId);
		sendNow(userId, CloudSimTags.CLOUDLET_CANCEL, cl);
	}

	/**
	 * Updates processing of each cloudlet running in this Datacenter. It is necessary because
	 * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events and
	 * updating cloudlets inside them must be called from the outside.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void updateCloudletProcessing() {
		// if some time passed since last processing 自从上次过程已经已经过了一些时间
		// R: for term is to allow loop at simulation start. 术语是在仿真始时允许循环 Otherwise, one initial
		// simulation step is skipped and schedulers are not properly initialized否则，将跳过个模拟步骤，并且调度器没有正确初始化
		double scurrentTime = CloudSim.clock();
		if (scurrentTime < 0.111 || scurrentTime > getLastProcessTime() + CloudSim.getMinTimeBetweenEvents()) {
			
			List<? extends Host> list = getVmAllocationPolicy().getHostList();//得到vm分配策略当中的主机列表
			double smallerTime = Double.MAX_VALUE;
			// for each host...层次遍历主机列表，返回所有主机的虚拟机中执行任务用时最少虚拟机的执行时间
			for (int i = 0; i < list.size(); i++) {
				Host host = list.get(i);
				// inform VMs to update processing 通知VMs更新处理
				double time = host.updateVmsProcessing(scurrentTime);//更新在这个主机上的所有虚拟机，返回任务执行用时最少的执行时间
				// what time do we expect that the next cloudlet will finish?我们预计下一次的云任务什么时候会结束?
				if (time < smallerTime) {
					smallerTime = time;
				}
				
			}
			// gurantees a minimal interval before scheduling the event在调度事件之前的�?小间�?
			if (smallerTime < CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01) {
				smallerTime = CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01;
			}
			if (smallerTime != Double.MAX_VALUE) {
				schedule(getId(), (smallerTime - CloudSim.clock()), CloudSimTags.VM_DATACENTER_EVENT);
			}
			setLastProcessTime(CloudSim.clock());
		}
	}

	/**
	 * Verifies if some cloudlet inside this Datacenter already finished. 
         * If yes, send it to the User/Broker
	 * 验证此数据中心中的一些cloudlet是否已经完成，如果是，发送给用户/代理
	 * @pre $none
	 * @post $none
	 */
	protected void checkCloudletCompletion() {
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			for (Vm vm : host.getVmList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					if (cl != null) {
						sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);//将已经完成的云任务发送给代理（�?�过包装在事件里�?
					}
				}
			}
		}
	}

	/**
	 * Adds a file into the resource's storage before the experiment starts. 
         * If the file is a master file, then it will be registered to the RC 
         * when the experiment begins.
	 * 
	 * @param file a DataCloud file
	 * @return a tag number denoting whether this operation is a success or not
	 */
	public int addFile(File file) {
		if (file == null) {
			return DataCloudTags.FILE_ADD_ERROR_EMPTY;
		}

		if (contains(file.getName())) {
			return DataCloudTags.FILE_ADD_ERROR_EXIST_READ_ONLY;
		}

		// check storage space first
		if (getStorageList().size() <= 0) {
			return DataCloudTags.FILE_ADD_ERROR_STORAGE_FULL;
		}

		Storage tempStorage = null;
		int msg = DataCloudTags.FILE_ADD_ERROR_STORAGE_FULL;

		for (int i = 0; i < getStorageList().size(); i++) {
			tempStorage = getStorageList().get(i);
			if (tempStorage.getAvailableSpace() >= file.getSize()) {
				tempStorage.addFile(file);
				msg = DataCloudTags.FILE_ADD_SUCCESSFUL;
				break;
			}
		}

		return msg;
	}

	/**
	 * Checks whether the datacenter has the given file.
	 * 
	 * @param file a file to be searched
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	protected boolean contains(File file) {
		if (file == null) {
			return false;
		}
		return contains(file.getName());
	}

	/**
	 * Checks whether the datacenter has the given file.
	 * 
	 * @param fileName a file name to be searched
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	protected boolean contains(String fileName) {
		if (fileName == null || fileName.length() == 0) {
			return false;
		}

		Iterator<Storage> it = getStorageList().iterator();
		Storage storage = null;
		boolean result = false;

		while (it.hasNext()) {
			storage = it.next();
			if (storage.contains(fileName)) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Deletes the file from the storage. 
         * Also, check whether it is possible to delete the file from the storage.
	 * 
	 * @param fileName the name of the file to be deleted
	 * @return the tag denoting the status of the operation,
         * either {@link DataCloudTags#FILE_DELETE_ERROR} or 
         *  {@link DataCloudTags#FILE_DELETE_SUCCESSFUL}
	 */
	private int deleteFileFromStorage(String fileName) {
		Storage tempStorage = null;
		File tempFile = null;
		int msg = DataCloudTags.FILE_DELETE_ERROR;

		for (int i = 0; i < getStorageList().size(); i++) {
			tempStorage = getStorageList().get(i);
			tempFile = tempStorage.getFile(fileName);
			tempStorage.deleteFile(fileName, tempFile);
			msg = DataCloudTags.FILE_DELETE_SUCCESSFUL;
		} // end for

		return msg;
	}

	@Override
	public void shutdownEntity() {
		Log.printConcatLine(getName(), " is shutting down...");
	}

	@Override
	public void startEntity() {
		Log.printConcatLine(getName(), " is starting...");
		// this resource should register to regional CIS.
		// However, if not specified, then register to system CIS (the
		// default CloudInformationService) entity.
		int gisID = CloudSim.getEntityId(regionalCisName);
		if (gisID == -1) {
			gisID = CloudSim.getCloudInfoServiceEntityId();
		}

		// send the registration to CIS
		sendNow(gisID, CloudSimTags.REGISTER_RESOURCE, getId());
		// Below method is for a child class to override
		registerOtherEntity();
	}

	/**
	 * Gets the host list.
	 * 
	 * @return the host list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Host> List<T> getHostList() {
		return (List<T>) getCharacteristics().getHostList();
	}

	/**
	 * Gets the datacenter characteristics.
	 * 
	 * @return the datacenter characteristics
	 */
	protected DatacenterCharacteristics getCharacteristics() {
		return characteristics;
	}

	/**
	 * Sets the datacenter characteristics.
	 * 
	 * @param characteristics the new datacenter characteristics
	 */
	protected void setCharacteristics(DatacenterCharacteristics characteristics) {
		this.characteristics = characteristics;
	}

	/**
	 * Gets the regional Cloud Information Service (CIS) name. 
	 * 
	 * @return the regional CIS name
	 */
	protected String getRegionalCisName() {
		return regionalCisName;
	}

	/**
	 * Sets the regional cis name.
	 * 
	 * @param regionalCisName the new regional cis name
	 */
	protected void setRegionalCisName(String regionalCisName) {
		this.regionalCisName = regionalCisName;
	}

	/**
	 * Gets the vm allocation policy.
	 * 
	 * @return the vm allocation policy
	 */
	public VmAllocationPolicy getVmAllocationPolicy() {
		return vmAllocationPolicy;
	}

	/**
	 * Sets the vm allocation policy.
	 * 
	 * @param vmAllocationPolicy the new vm allocation policy
	 */
	protected void setVmAllocationPolicy(VmAllocationPolicy vmAllocationPolicy) {
		this.vmAllocationPolicy = vmAllocationPolicy;
	}

	/**
	 * Gets the last time some cloudlet was processed in the datacenter.
	 * 获取在数据中心中，上一次执行的cloudlet的时间
	 * @return the last process time
	 */
	protected double getLastProcessTime() {
		return lastProcessTime;
	}

	/**
	 * Sets the last process time.
	 * 
	 * @param lastProcessTime the new last process time
	 */
	protected void setLastProcessTime(double lastProcessTime) {
		this.lastProcessTime = lastProcessTime;
	}

	/**
	 * Gets the storage list.
	 * 
	 * @return the storage list
	 */
	protected List<Storage> getStorageList() {
		return storageList;
	}

	/**
	 * Sets the storage list.
	 * 
	 * @param storageList the new storage list
	 */
	protected void setStorageList(List<Storage> storageList) {
		this.storageList = storageList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the scheduling interval.
	 * 获取调度间隔
	 * @return the scheduling interval
	 */
	protected double getSchedulingInterval() {
		return schedulingInterval;
	}

	/**
	 * Sets the scheduling interval.
	 * 
	 * @param schedulingInterval the new scheduling interval
	 */
	protected void setSchedulingInterval(double schedulingInterval) {
		this.schedulingInterval = schedulingInterval;
	}

}
