/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package com.shiyan.core.predicates;

import com.shiyan.core.SimEvent;

/**
 * A predicate to select events with specific tags.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 * @see PredicateNotType
 * @see Predicate
 */
public class PredicateType extends Predicate {

	/** Array of tags to verify if the tag of received events correspond to. 
	 * 用于验证接收事件的标记是否对应
	 * */
	private final int[] tags;

	/**
	 * Constructor used to select events with the given tag value.
	 * 构造函数，用于选择具有给定标记值的事件。
	 * @param t1 an event tag value
	 */
	public PredicateType(int t1) {
		tags = new int[] { t1 };
	}

	/**
	 * Constructor used to select events with a tag value equal to any of the specified tags.
	 * 构造函数，用于选择标记值等于任何指定标记的事件
	 * @param tags the list of tags标记列表
	 */
	public PredicateType(int[] tags) {
		this.tags = tags.clone();
	}

	/**
	 * Matches any event that has one of the specified {@link #tags}.
	 * 匹配任何具有指定标记之一的事件
	 * @param ev {@inheritDoc}
	 * @return {@inheritDoc}
         * @see #tags
	 */
	@Override
	public boolean match(SimEvent ev) {
		int tag = ev.getTag();
		for (int tag2 : tags) {
			if (tag == tag2) {
				return true;
			}
		}
		return false;
	}

}
