/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package com.shiyan.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class implements the future event queue used by {@link CloudSim}. 这个类实现了CloudSim 的未来事件队列。
 * The event queue uses a {@link TreeSet} in order to store the events. 此事件队列使用一个TreeSet来存储事件
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 * @see Simulation
 * @see java.util.TreeSet
 * 
 * @todo It would be used a common interface for queues
 * such as this one and {@link DeferredQueue}
 */
public class FutureQueue {

	/** The sorted set of events. 事件的排序集*/
	private final SortedSet<SimEvent> sortedSet = new TreeSet<SimEvent>();

	/** A incremental number used for {@link SimEvent#serial} event attribute.用于SimEvent系列事件属性的增量数
         */
	private long serial = 0;

	/**
	 * Adds a new event to the queue. Adding a new event to the queue preserves the temporal order of
	 * the events in the queue.在队列中添加一个新事件。新事件保留队列中事件的时间顺序
	 * 
	 * @param newEvent The event to be put in the queue.
	 */
	public void addEvent(SimEvent newEvent) {
		newEvent.setSerial(serial++);
		sortedSet.add(newEvent);
	}

	/**
	 * Adds a new event to the head of the queue.向队列头部添加一个新事件
	 * 
	 * @param newEvent The event to be put in the queue.
	 */
	public void addEventFirst(SimEvent newEvent) {
		newEvent.setSerial(0);
		sortedSet.add(newEvent);
	}

	/**
	 * Returns an iterator to the queue.将迭代器返回到队列。
	 * 
	 * @return the iterator
	 */
	public Iterator<SimEvent> iterator() {
		return sortedSet.iterator();
	}

	/**
	 * Returns the size of this event queue.返回事件队列的大小
	 * 
	 * @return the size
	 */
	public int size() {
		return sortedSet.size();
	}

	/**
	 * Removes the event from the queue.从队列中移除事件
	 * 
	 * @param event the event
	 * @return true, if successful
	 */
	public boolean remove(SimEvent event) {
		return sortedSet.remove(event);
	}

	/**
	 * Removes all the events from the queue.从队列中移除所有的事件
	 * 
	 * @param events the events
	 * @return true, if successful
	 */
	public boolean removeAll(Collection<SimEvent> events) {
		return sortedSet.removeAll(events);
	}

	/**
	 * Clears the queue.清空队列
	 */
	public void clear() {
		sortedSet.clear();
	}

}