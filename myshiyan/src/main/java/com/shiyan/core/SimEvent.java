/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package com.shiyan.core;

/**
 * This class represents a simulation event which is passed between the entities in the simulation.这个类表示在模拟中通过各个实体之间的模拟事件
 * @author Costas Simatos
 * @see Simulation
 * @see SimEntity
 */
public class SimEvent implements Cloneable, Comparable<SimEvent> {

	/** Internal event type. 内部事件类型**/
	private final int etype;

	/** The time that this event was scheduled, at which it should occur.这个事件被安排的时间，它应该在此时发生 **/
	private final double time;

	/** Time that the event was removed from the queue to start service.从队列中删除事件以启动服务的时间**/
	private double endWaitingTime;

	/** Id of entity who scheduled the event. 调度事件的实体的Id**/
	private int entSrc;

	/** Id of entity that the event will be sent to. 事件将被发送到的实体的Id**/
	private int entDst;

	/** The user defined type of the event. 用户定义的事件类型**/
	private final int tag;

	/** 
         * Any data the event is carrying. 
         * @todo I would be used generics to define the type of the event data.
         * But this modification would incur several changes in the simulator core
         * that has to be assessed first.事件携带的任何数据。将使用泛型来定义事件数据的类型
         **/
	private final Object data;

        /**
         * An attribute to help CloudSim to identify the order of received events
         * when multiple events are generated at the same time.
         * If two events have the same {@link #time}, to know
         * what event is greater than other (i.e. that happens after other),
         * the {@link #compareTo(org.cloudbus.cloudsim.core.SimEvent)}
         * makes use of this field.用于帮助CloudSim识别接收事件的顺序的属性
         */
	private long serial = -1;

	// Internal event types 内部事件类型

	public static final int ENULL = 0;

	public static final int SEND = 1;

	public static final int HOLD_DONE = 2;

	public static final int CREATE = 3;

	/**
	 * Creates a blank event.创建空白事件
	 */
	public SimEvent() {
		etype = ENULL;
		time = -1L;
		endWaitingTime = -1.0;
		entSrc = -1;
		entDst = -1;
		tag = -1;
		data = null;
	}

	// ------------------- PACKAGE LEVEL METHODS --------------------------
	SimEvent(int evtype, double time, int src, int dest, int tag, Object edata) {
		etype = evtype;
		this.time = time;
		entSrc = src;
		entDst = dest;
		this.tag = tag;
		data = edata;
	}

	SimEvent(int evtype, double time, int src) {
		etype = evtype;
		this.time = time;
		entSrc = src;
		entDst = -1;
		tag = -1;
		data = null;
	}

	protected void setSerial(long serial) {
		this.serial = serial;
	}

	/**
	 * Sets the time that the event was removed from the queue to start service. 
	 * 
	 * @param end_waiting_time
	 */
	protected void setEndWaitingTime(double end_waiting_time) {
		endWaitingTime = end_waiting_time;
	}
        
	// ------------------- PUBLIC METHODS --------------------------        

	@Override
	public String toString() {
		return "Event tag = " + tag + " source = " + CloudSim.getEntity(entSrc).getName() + " destination = "
				+ CloudSim.getEntity(entDst).getName();
	}

	/**
	 * Gets the internal type
	 * 
	 * @return
	 */
	public int getType() {
		return etype;
	}


	@Override
	public int compareTo(SimEvent event) {
		if (event == null) {
			return 1;
		} else if (time < event.time) {
			return -1;
		} else if (time > event.time) {
			return 1;
		} else if (serial < event.serial) {
			return -1;
		} else if (this == event) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Get the unique id number of the entity which received this event.
	 * 获取接收此事件的实体的唯一id号。
	 * @return the id number
	 */
	public int getDestination() {
		return entDst;
	}

	/**
	 * Get the unique id number of the entity which scheduled this event.
	 * 获取调度此事件的实体的唯一id号。
	 * @return the id number
	 */
	public int getSource() {
		return entSrc;
	}

	/**
	 * Get the simulation time that this event was scheduled.获得此事件被调度的模拟时间
	 * 
	 * @return The simulation time
	 */
	public double eventTime() {
		return time;
	}

	/**
	 * Get the simulation time that this event was removed from the queue for service.获取从服务队列中删除此事件的模拟时间
	 * 
	 * @return The simulation time
	 */
	public double endWaitingTime() {
		return endWaitingTime;
	}

	/**
	 * Get the user-defined tag of this event
	 * 
	 * @return The tag
	 */
	public int type() {
		return tag;
	}

	/**
	 * Get the unique id number of the entity which scheduled this event.
	 * 
	 * @return the id number
	 */
	public int scheduledBy() {
		return entSrc;
	}

	/**
	 * Get the user-defined tag of this event.
	 * 
	 * @return The tag
	 */
	public int getTag() {
		return tag;
	}

	/**
	 * Get the data passed in this event.
	 * 获取在此事件中传递的数据。
	 * @return A reference to the data
	 */
	public Object getData() {
		return data;
	}

	@Override
	public Object clone() {
		return new SimEvent(etype, time, entSrc, entDst, tag, data);
	}

	/**
	 * Set the source entity of this event.
	 * 
	 * @param s The unique id number of the entity
	 */
	public void setSource(int s) {
		entSrc = s;
	}

	/**
	 * Set the destination entity of this event.
	 * 
	 * @param d The unique id number of the entity
	 */
	public void setDestination(int d) {
		entDst = d;
	}
}
