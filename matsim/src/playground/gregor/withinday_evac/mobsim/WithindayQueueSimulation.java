/* *********************************************************************** *
 * project: org.matsim.*
 * WithindayQueueSimulation.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.gregor.withinday_evac.mobsim;

import org.matsim.events.Events;
import org.matsim.mobsim.QueueSimulation;
import org.matsim.mobsim.Vehicle;
import org.matsim.network.NetworkLayer;
import org.matsim.plans.Person;
import org.matsim.plans.Plans;

import playground.gregor.withinday_evac.BDIAgent;

/**
 * This extension of the QueueSimulation is used for withinday replanning. It contains functionality
 * to trigger the replanning of the WithindayAgents and provides methods to set an accident, i.e. a
 * capacity change of a link.
 *
 * @author dgrether
 *
 */
public class WithindayQueueSimulation extends QueueSimulation {

	private final playground.gregor.withinday_evac.WithindayControler controler;


	public WithindayQueueSimulation(final NetworkLayer net,
			final Plans plans, final Events events, final playground.gregor.withinday_evac.WithindayControler withindayControler) {
		super(net, plans, events);
		this.controler = withindayControler;
		this.setVehiclePrototye(OccupiedVehicle.class);
	}
	
	@Override
	protected void initVehicle(final Vehicle veh) {
		super.initVehicle(veh);
		createAgent(veh.getDriver(), (OccupiedVehicle)veh);
	}
	
	/**
	 * Is currently used to create the WithindayAgent objects with the default belief and desire (intentions are still fixed by
	 * the game theory plans) modules.
	 * @param person
	 * @param veh
	 */
	private void createAgent(final Person person, final OccupiedVehicle veh) {
		new BDIAgent(person, veh);
	}






}
