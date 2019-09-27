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
 * A predicate which will <b>not</b> match any event on the deferred event queue. 
 * See the publicly accessible instance of this predicate in
 * {@link com.shiyan.core.CloudSim#SIM_NONE}, so no new instances needs to be created. <br/>
 * The idea of simulation predicates was copied from SimJava 2.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 * @see Predicate
 * @see Simulation
 */
public class PredicateNone extends Predicate {

	/**
	 * Considers that no event received by the predicate matches.
	 * 
	 * @param ev {@inheritDoc}
	 * @return always false to indicate that no event is accepted
	 */
	@Override
	public boolean match(SimEvent ev) {
		return false;
	}
}
