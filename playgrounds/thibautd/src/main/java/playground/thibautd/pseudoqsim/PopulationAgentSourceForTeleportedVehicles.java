/* *********************************************************************** *
 * project: org.matsim.*
 * PopulationAgentSourceForTeleportedVehicles.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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
package playground.thibautd.pseudoqsim;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.agents.AgentFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * lots of copy paste here...
 * @author thibautd
 */
public class PopulationAgentSourceForTeleportedVehicles implements AgentSource {
	private final Population population;
	private final AgentFactory agentFactory;
	private final ParkedVehicleProvider vehicles;
	private final Map<String, VehicleType> modeVehicleTypes;

	private final QSim qsim;

	public PopulationAgentSourceForTeleportedVehicles(
			final Population population,
			final AgentFactory agentFactory,
			final QSim qsim,
			final ParkedVehicleProvider vehicles) {
		this.population = population;
		this.agentFactory = agentFactory;
		this.vehicles = vehicles;
		this.modeVehicleTypes = new HashMap<String, VehicleType>();
		this.qsim = qsim;
		for (String mode : qsim.getScenario().getConfig().qsim().getMainModes()) {
			modeVehicleTypes.put(mode, VehicleUtils.getDefaultVehicleType());
		}
	}

	@Override
	public void insertAgentsIntoMobsim() {
		final Set<Id> alreadyParked = new HashSet<Id>();
		for (Person p : population.getPersons().values()) {
			final MobsimAgent agent = this.agentFactory.createMobsimAgentFromPerson(p);
			final Plan plan = p.getSelectedPlan();
			for (PlanElement planElement : plan.getPlanElements()) {
				if (planElement instanceof Leg) {
					Leg leg = (Leg) planElement;
					if ( modeVehicleTypes.containsKey(leg.getMode()) ) { // only simulated modes get vehicles
						final Id vehicleId = getVehicleId( p , leg );
						if ( !alreadyParked.add( vehicleId ) ) {
							// only park each vehicle once
							continue;
						}

						final Id vehicleLink = findVehicleLink(p);
						vehicles.addVehicle(
								new QVehicle(
									VehicleUtils.getFactory().createVehicle(
										vehicleId,
										modeVehicleTypes.get(leg.getMode()))),
								vehicleLink);
					}
				}
			}
			// this MUST be the last action, because stuff already happens at
			// insertion (even if run was not called yet...)
			qsim.insertAgentIntoMobsim(agent);
		}
	}

	private boolean usedPersonId = false;
	private boolean usedRouteField = false;
	private Id getVehicleId(
			final Person p,
			final Leg leg) {
		final Route route = leg.getRoute();

		if (route instanceof NetworkRoute &&
				((NetworkRoute) route).getVehicleId() != null) {
			if ( usedPersonId ) throw new InconsistentVehiculeSpecificationsException();
			usedRouteField = true;
			return ((NetworkRoute) route).getVehicleId();
		}

		if ( usedRouteField )  throw new InconsistentVehiculeSpecificationsException();
		usedPersonId = true;
		return p.getId();
	}

	private static Id findVehicleLink(final Person p) {
		// A more careful way to decide where this agent should have its vehicles created
		// than to ask agent.getCurrentLinkId() after creation.
		for (PlanElement planElement : p.getSelectedPlan().getPlanElements()) {
			if (planElement instanceof Activity) {
				Activity activity = (Activity) planElement;
				if (activity.getLinkId() != null) {
					return activity.getLinkId();
				}
			} else if (planElement instanceof Leg) {
				Leg leg = (Leg) planElement;
				if (leg.getRoute().getStartLinkId() != null) {
					return leg.getRoute().getStartLinkId();
				}
			}
		}
		throw new RuntimeException("Don't know where to put a vehicle for this agent.");
	}

	public void setModeVehicleTypes(final Map<String, VehicleType> modeVehicleTypes) {
		this.modeVehicleTypes.putAll( modeVehicleTypes );
	}

	public void setModeVehicleType(final String mode, final VehicleType type) {
		this.modeVehicleTypes.put( mode , type );
	}

	public static class InconsistentVehiculeSpecificationsException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
}
