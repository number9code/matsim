/* *********************************************************************** *
 * project: org.matsim.*
 * TransitRouterConfig.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package org.matsim.pt.router;

import org.matsim.core.api.internal.MatsimParameters;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.pt.config.TransitRouterConfigGroup;

/**
 * Design decisions:<ul>
 * <li>At this point, only those variables that are needed elsewhere (in particular in the scoring) are set from the
 * config.  All other variables are considered internal computational variables.  kai, feb'11
 * </ul>
 *
 */
public class TransitRouterConfig implements MatsimParameters {

	/**
	 * The distance in meters in which stop facilities should be searched for
	 * around the start and end coordinate.
	 */
	public double searchRadius = 1000.0;

	/**
	 * If no stop facility is found around start or end coordinate (see
	 * {@link #searchRadius}), the nearest stop location is searched for
	 * and the distance from start/end coordinate to this location is
	 * extended by the given amount.<br />
	 * If only one stop facility is found within {@link #searchRadius},
	 * the radius is also extended in the hope to find more stop
	 * facilities (e.g. in the opposite direction of the already found
	 * stop).
	 */
	public double extensionRadius = 200.0;

	/**
	 * The distance in meters that agents can walk to get from one stop to
	 * another stop of a nearby transit line.
	 * <p/>
	 * Is this really needed?  If the marg utl of walk is correctly set, this should come out automagically.
	 * kai, feb'11
	 * This value is used to generate the walk connections between stop facilities. If they are used,
	 * depends on the scoring/cost calculation. But when they are missing, they cannot be used at all.
	 * mrieser, mar'11
	 */
	public double beelineWalkConnectionDistance = 100.0;

	/**
	 * The minimum time needed for a transfer is calculated based on the distance and the beeline walk speed
	 * between two stop facilities. Due to passengers probably not being able to immediately (=in the same
	 * second) leave a transit vehicle, or a vehicle being delayed by a small amount of time, an additional
	 * "savety" time can be added to transfers when searching for connecting trips. This could help to find
	 * "better" transfer connections such as they can indeed be realized by the simulation. This value only
	 * affects the routing process, not the simulation itself.
	 */
	public double additionalTransferTime = 0.0;

	// =============================================================================================================================
	// no more public variables below this line

	private double beelineWalkSpeed; // meter / second

	private double effectiveMarginalUtilityOfTravelTimeWalk_utl_s;

	private double effectiveMarginalUtilityOfTravelTimeTransit_utl_s;

	private double effectiveMarginalUtiltityOfWaiting_utl_s;

	private double marginalUtilityOfTravelDistanceTransit_utl_m;

	private double utilityOfLineSwitch_utl;

	// =============================================================================================================================
	// only setters and getters below this line

	public TransitRouterConfig(final PlanCalcScoreConfigGroup pcsConfig, final PlansCalcRouteConfigGroup pcrConfig, final TransitRouterConfigGroup trConfig) {
		// walk:
		this.beelineWalkSpeed = pcrConfig.getWalkSpeed() / pcrConfig.getBeelineDistanceFactor();
		this.effectiveMarginalUtilityOfTravelTimeWalk_utl_s = pcsConfig.getTravelingWalk_utils_hr()/3600.0 - pcsConfig.getPerforming_utils_hr() / 3600.0;
		// pt:
		this.effectiveMarginalUtilityOfTravelTimeTransit_utl_s = pcsConfig.getTravelingPt_utils_hr()/3600.0 - pcsConfig.getPerforming_utils_hr() / 3600.0;
		this.marginalUtilityOfTravelDistanceTransit_utl_m = pcsConfig.getMarginalUtilityOfMoney() * pcsConfig.getMonetaryDistanceCostRatePt();
		this.effectiveMarginalUtiltityOfWaiting_utl_s = pcsConfig.getWaiting_utils_hr() / 3600.0 - pcsConfig.getPerforming_utils_hr() / 3600.0;
		this.utilityOfLineSwitch_utl = pcsConfig.getUtilityOfLineSwitch();
		// router:
		this.searchRadius = trConfig.getSearchRadius();
		this.extensionRadius = trConfig.getExtensionRadius();
		this.beelineWalkConnectionDistance = trConfig.getMaxBeelineWalkConnectionDistance();
		this.additionalTransferTime = trConfig.getAdditionalTransferTime();
	}

	public void setUtilityOfLineSwitch_utl(final double utilityOfLineSwitch_utl_sec) {
		this.utilityOfLineSwitch_utl = utilityOfLineSwitch_utl_sec;
	}

	/**
	 * The additional utility to be added when an agent switches lines.  Normally negative
	 * <p/>
	 * The "_utl" can go as soon as we are confident that there are no more utilities in "Eu".  kai, feb'11
	 */
	public double getUtilityOfLineSwitch_utl() {
		return this.utilityOfLineSwitch_utl;
	}

	public void setEffectiveMarginalUtilityOfTravelTimeWalk_utl_s(final double marginalUtilityOfTravelTimeWalk_utl_s) {
		this.effectiveMarginalUtilityOfTravelTimeWalk_utl_s = marginalUtilityOfTravelTimeWalk_utl_s;
	}

	public double getEffectiveMarginalUtilityOfTravelTimeWalk_utl_s() {
		return this.effectiveMarginalUtilityOfTravelTimeWalk_utl_s;
	}

	public void setEffectiveMarginalUtilityOfTravelTimePt_utl_s(final double marginalUtilityOfTravelTimeTransit_utl_s) {
		this.effectiveMarginalUtilityOfTravelTimeTransit_utl_s = marginalUtilityOfTravelTimeTransit_utl_s;
	}

	/**
	 * @return the effective marginal utility of travel time by public transit.  Includes the opportunity cost of time
	 */
	public double getEffectiveMarginalUtilityOfTravelTimePt_utl_s() {
		return this.effectiveMarginalUtilityOfTravelTimeTransit_utl_s;
	}

	public void setMarginalUtilityOfTravelDistancePt_utl_m(final double marginalUtilityOfTravelDistanceTransit_utl_m) {
		this.marginalUtilityOfTravelDistanceTransit_utl_m = marginalUtilityOfTravelDistanceTransit_utl_m;
	}

	public double getEffectiveMarginalUtiltityOfWaiting_utl_s() {
		return this.effectiveMarginalUtiltityOfWaiting_utl_s;
	}

	public void setEffectiveMarginalUtiltityOfWaiting_utl_s(final double effectiveMarginalUtiltityOfWaiting_utl_s) {
		this.effectiveMarginalUtiltityOfWaiting_utl_s = effectiveMarginalUtiltityOfWaiting_utl_s;
	}

	/**
	 * in the config, this is distanceCostRate * margUtlOfMoney.  For the router, the conversion to
	 * utils seems ok.  kai, feb'11
	 */
	public double getMarginalUtilityOfTravelDistancePt_utl_m() {
		return this.marginalUtilityOfTravelDistanceTransit_utl_m;
	}

	public void setBeelineWalkSpeed(final double beelineWalkSpeed) {
		this.beelineWalkSpeed = beelineWalkSpeed;
	}

	/**
	 * Walking speed of agents on transfer links, beeline distance.
	 */
	public double getBeelineWalkSpeed() {
		return this.beelineWalkSpeed;
	}

}
