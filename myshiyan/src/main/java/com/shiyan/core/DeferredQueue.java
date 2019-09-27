/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package com.shiyan.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This class implements the deferred event queue used by {@link CloudSim}. 这个类实现了CloudSim的延迟事件队列。
 * The event queue uses a linked list to store the events.事件队列使用链表来存储事件。
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 * @see CloudSim
 * @see SimEvent
 */
public class DeferredQueue {

	/** The list of events. */
	private final List<SimEvent> list = new LinkedList<SimEvent>();

	/** The max time that an added event is scheduled. 指定添加的事件的最大时间*/
	private double maxTime = -1;

	/**
	 * Adds a new event to the queue. Adding a new event to the queue preserves the temporal order
	 * of the events.在队列中添加一个新事件。新的事件保存了发生的时间顺序
	 * @param newEvent The event to be added to the queue.
	 */
	public void addEvent(SimEvent newEvent) {
		// The event has to be inserted as the last of all events 该事件必须被插入到具有相同event_time的所有事件的最后
		// with the same event_time(). Yes, this matters.
		double eventTime = newEvent.eventTime();
		if (eventTime >= maxTime) {
			list.add(newEvent);
			maxTime = eventTime;
			return;
		}

		ListIterator<SimEvent> iterator = list.listIterator();
		SimEvent event;
		while (iterator.hasNext()) {
			event = iterator.next();
			if (event.eventTime() > eventTime) {
				iterator.previous();
				iterator.add(newEvent);
				return;
			}
		}

		list.add(newEvent);
	}

	/**
	 * Returns an iterator to the events in the queue.返回队列中的事件的迭代器
	 * 
	 * @return the iterator
	 */
	public Iterator<SimEvent> iterator() {
		return list.iterator();
	}

	/**
	 * Returns the size of this event queue.返回事件队列的大小
	 * 
	 * @return the number of events in the queue.
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Clears the queue.清除队列
	 */
	public void clear() {
		list.clear();
	}

}