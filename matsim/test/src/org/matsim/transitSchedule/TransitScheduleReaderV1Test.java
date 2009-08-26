/* *********************************************************************** *
 * project: org.matsim.*
 * TransitScheduleReaderV1Test.java
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

package org.matsim.transitSchedule;

import java.util.Stack;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Id;
import org.matsim.api.basic.v01.TransportMode;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.population.routes.NetworkRouteWRefs;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.misc.Time;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.testcases.utils.AttributesBuilder;
import org.matsim.transitSchedule.api.Departure;
import org.matsim.transitSchedule.api.TransitLine;
import org.matsim.transitSchedule.api.TransitRoute;
import org.matsim.transitSchedule.api.TransitRouteStop;
import org.matsim.transitSchedule.api.TransitSchedule;
import org.matsim.transitSchedule.api.TransitStopFacility;
import org.xml.sax.Attributes;

/**
 * Detailed tests for {@link TransitScheduleReaderV1}, also testing special cases
 * and possible Exceptions.
 *
 * @author mrieser
 */
public class TransitScheduleReaderV1Test extends TestCase {

	private static final Logger log = Logger.getLogger(TransitScheduleReaderV1Test.class);
	private static final String EMPTY_STRING = "";

	public void testStopFacility_Minimalistic() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		Attributes emptyAtts = AttributesBuilder.getEmpty();
		reader.startTag(Constants.TRANSIT_SCHEDULE, emptyAtts, context);
		context.push(Constants.TRANSIT_SCHEDULE);
		reader.startTag(Constants.TRANSIT_STOPS, emptyAtts, context);
		context.push(Constants.TRANSIT_STOPS);
		Attributes atts = new AttributesBuilder().add(Constants.ID, "stop1").
				add(Constants.X, "79").add(Constants.Y, "80").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);
		reader.endTag(context.pop(), EMPTY_STRING, context);// TRANSIT_STOPS
		reader.endTag(context.pop(), EMPTY_STRING, context);// TRANSIT_SCHEDULE

		assertEquals(1, schedule.getFacilities().size());
		TransitStopFacility stop = schedule.getFacilities().get(new IdImpl("stop1"));
		assertNotNull(stop);
		assertEquals(79.0, stop.getCoord().getX(), MatsimTestCase.EPSILON);
		assertEquals(80.0, stop.getCoord().getY(), MatsimTestCase.EPSILON);
		assertNull(stop.getLink());
		assertNull(stop.getLinkId());
		assertNull(stop.getName());
		assertFalse(stop.getIsBlockingLane());
	}

	public void testStopFacility_withLink() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		NetworkLayer network = new NetworkLayer();
		NodeImpl node1 = network.createNode(new IdImpl(1), new CoordImpl(10, 5));
		NodeImpl node2 = network.createNode(new IdImpl(2), new CoordImpl(5, 11));
		LinkImpl link3 = network.createLink(new IdImpl(3), node1, node2, 1000, 10.0, 2000.0, 1.0);

		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, network);
		Stack<String> context = new Stack<String>();
		Attributes emptyAtts = AttributesBuilder.getEmpty();
		reader.startTag(Constants.TRANSIT_SCHEDULE, emptyAtts, context);
		context.push(Constants.TRANSIT_SCHEDULE);
		reader.startTag(Constants.TRANSIT_STOPS, emptyAtts, context);
		context.push(Constants.TRANSIT_STOPS);
		Attributes atts = new AttributesBuilder().add(Constants.ID, "stop1").
				add(Constants.X, "79").add(Constants.Y, "80").add(Constants.LINK_REF_ID, "3").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);
		reader.endTag(context.pop(), EMPTY_STRING, context);// TRANSIT_STOPS
		reader.endTag(context.pop(), EMPTY_STRING, context);// TRANSIT_SCHEDULE

		assertEquals(1, schedule.getFacilities().size());
		TransitStopFacility stop = schedule.getFacilities().get(new IdImpl("stop1"));
		assertNotNull(stop);
		assertEquals(79.0, stop.getCoord().getX(), MatsimTestCase.EPSILON);
		assertEquals(80.0, stop.getCoord().getY(), MatsimTestCase.EPSILON);
		assertEquals(link3, stop.getLink());
		assertEquals(link3.getId(), stop.getLinkId());
	}

	public void testStopFacility_withBadLink() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		NetworkLayer network = new NetworkLayer();
		NodeImpl node1 = network.createNode(new IdImpl(1), new CoordImpl(10, 5));
		NodeImpl node2 = network.createNode(new IdImpl(2), new CoordImpl(5, 11));
		network.createLink(new IdImpl(3), node1, node2, 1000, 10.0, 2000.0, 1.0);

		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, network);
		Stack<String> context = new Stack<String>();
		Attributes emptyAtts = AttributesBuilder.getEmpty();
		reader.startTag(Constants.TRANSIT_SCHEDULE, emptyAtts, context);
		context.push(Constants.TRANSIT_SCHEDULE);
		reader.startTag(Constants.TRANSIT_STOPS, emptyAtts, context);
		context.push(Constants.TRANSIT_STOPS);
		Attributes atts = new AttributesBuilder().add(Constants.ID, "stop1").
				add(Constants.X, "79").add(Constants.Y, "80").add(Constants.LINK_REF_ID, "4").get();
		try {
			reader.startTag(Constants.STOP_FACILITY, atts, context);
			fail("missing exception.");
		}
		catch (RuntimeException e) {
			log.info("catched expected exception: ", e);
		}
	}

	public void testStopFacility_withName() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		Attributes emptyAtts = AttributesBuilder.getEmpty();
		reader.startTag(Constants.TRANSIT_SCHEDULE, emptyAtts, context);
		context.push(Constants.TRANSIT_SCHEDULE);
		reader.startTag(Constants.TRANSIT_STOPS, emptyAtts, context);
		context.push(Constants.TRANSIT_STOPS);
		Attributes atts = new AttributesBuilder().add(Constants.ID, "stop1").
				add(Constants.X, "79").add(Constants.Y, "80").add(Constants.NAME, "some stop name").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);
		reader.endTag(context.pop(), EMPTY_STRING, context);// TRANSIT_STOPS
		reader.endTag(context.pop(), EMPTY_STRING, context);// TRANSIT_SCHEDULE

		assertEquals(1, schedule.getFacilities().size());
		TransitStopFacility stop = schedule.getFacilities().get(new IdImpl("stop1"));
		assertNotNull(stop);
		assertEquals(79.0, stop.getCoord().getX(), MatsimTestCase.EPSILON);
		assertEquals(80.0, stop.getCoord().getY(), MatsimTestCase.EPSILON);
		assertEquals("some stop name", stop.getName());
	}

	public void testStopFacility_isBlocking() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();

		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		Attributes emptyAtts = AttributesBuilder.getEmpty();
		reader.startTag(Constants.TRANSIT_SCHEDULE, emptyAtts, context);
		context.push(Constants.TRANSIT_SCHEDULE);
		reader.startTag(Constants.TRANSIT_STOPS, emptyAtts, context);
		context.push(Constants.TRANSIT_STOPS);
		Attributes atts = new AttributesBuilder().add(Constants.ID, "stop1").
				add(Constants.X, "79").add(Constants.Y, "80").add(Constants.IS_BLOCKING, "true").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);
		reader.endTag(context.pop(), EMPTY_STRING, context);// TRANSIT_STOPS
		reader.endTag(context.pop(), EMPTY_STRING, context);// TRANSIT_SCHEDULE

		assertEquals(1, schedule.getFacilities().size());
		TransitStopFacility stop = schedule.getFacilities().get(new IdImpl("stop1"));
		assertNotNull(stop);
		assertEquals(79.0, stop.getCoord().getX(), MatsimTestCase.EPSILON);
		assertEquals(80.0, stop.getCoord().getY(), MatsimTestCase.EPSILON);
		assertTrue(stop.getIsBlockingLane());
	}

	public void testStopFacility_Multiple() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		Attributes emptyAtts = AttributesBuilder.getEmpty();
		reader.startTag(Constants.TRANSIT_SCHEDULE, emptyAtts, context);
		context.push(Constants.TRANSIT_SCHEDULE);
		reader.startTag(Constants.TRANSIT_STOPS, emptyAtts, context);
		context.push(Constants.TRANSIT_STOPS);

		Attributes atts = new AttributesBuilder().add(Constants.ID, "stop1").
				add(Constants.X, "79").add(Constants.Y, "80").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);

		atts = new AttributesBuilder().add(Constants.ID, "stop2").
		add(Constants.X, "51").add(Constants.Y, "42").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context);// TRANSIT_STOPS
		reader.endTag(context.pop(), EMPTY_STRING, context);// TRANSIT_SCHEDULE

		assertEquals(2, schedule.getFacilities().size());
		TransitStopFacility stop1 = schedule.getFacilities().get(new IdImpl("stop1"));
		assertNotNull(stop1);
		assertEquals(79.0, stop1.getCoord().getX(), MatsimTestCase.EPSILON);
		assertEquals(80.0, stop1.getCoord().getY(), MatsimTestCase.EPSILON);
		TransitStopFacility stop2 = schedule.getFacilities().get(new IdImpl("stop2"));
		assertNotNull(stop2);
		assertEquals(51.0, stop2.getCoord().getX(), MatsimTestCase.EPSILON);
		assertEquals(42.0, stop2.getCoord().getY(), MatsimTestCase.EPSILON);
	}

	public void testTransitLine_Single() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);
		Id lineId = new IdImpl("23");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		reader.endTag(Constants.TRANSIT_LINE, EMPTY_STRING, context);// TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context);// TRANSIT_SCHEDULE

		assertEquals(1, schedule.getTransitLines().size());
		TransitLine line = schedule.getTransitLines().get(lineId);
		assertNotNull(line);
		assertEquals(lineId, line.getId());
	}

	public void testTransitLine_Multiple() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);
		Id lineId1 = new IdImpl("23");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId1.toString()).get(), context);
		reader.endTag(Constants.TRANSIT_LINE, EMPTY_STRING, context);// TRANSIT_LINE
		Id lineId2 = new IdImpl("42");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId2.toString()).get(), context);
		reader.endTag(Constants.TRANSIT_LINE, EMPTY_STRING, context);// TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context);// TRANSIT_SCHEDULE

		assertEquals(2, schedule.getTransitLines().size());
		TransitLine line1 = schedule.getTransitLines().get(lineId1);
		assertNotNull(line1);
		assertEquals(lineId1, line1.getId());
		TransitLine line2 = schedule.getTransitLines().get(lineId2);
		assertNotNull(line2);
		assertEquals(lineId2, line2.getId());
	}

	public void testTransitRoute_Single() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("foo");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		reader.endTag(Constants.DEPARTURES, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitLine line = schedule.getTransitLines().get(lineId);
		assertEquals(1, line.getRoutes().size());
		TransitRoute route1 = line.getRoutes().get(routeId1);
		assertNotNull(route1);
		assertEquals(routeId1, route1.getId());
		assertNull(route1.getDescription());
		assertEquals(TransportMode.bus, route1.getTransportMode());
		assertEquals(0, route1.getStops().size());
		assertNull(route1.getRoute());
		assertEquals(0, route1.getDepartures().size());
	}

	public void testTransitRoute_Multiple() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("foo");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		reader.endTag(Constants.DEPARTURES, EMPTY_STRING, context);
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE

		Id routeId2 = new IdImpl("bar");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId2.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "train", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		reader.endTag(Constants.DEPARTURES, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitLine line = schedule.getTransitLines().get(lineId);
		assertEquals(2, line.getRoutes().size());

		TransitRoute route1 = line.getRoutes().get(routeId1);
		assertNotNull(route1);
		assertEquals(routeId1, route1.getId());
		assertEquals(TransportMode.bus, route1.getTransportMode());

		TransitRoute route2 = line.getRoutes().get(routeId2);
		assertNotNull(route2);
		assertEquals(routeId2, route2.getId());
		assertEquals(TransportMode.train, route2.getTransportMode());

	}

	public void testTransitRoute_Description() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("foo");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		String description = "This could be some really long text, even containing line\nbreaks\n\nand other\tspecial characters.";
		reader.startTag(Constants.DESCRIPTION, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.DESCRIPTION, description, context);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		reader.endTag(Constants.DEPARTURES, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitLine line = schedule.getTransitLines().get(lineId);
		assertEquals(1, line.getRoutes().size());
		TransitRoute route1 = line.getRoutes().get(routeId1);
		assertNotNull(route1);
		assertEquals(description, route1.getDescription());
	}

	public void testRouteProfile_SingleStop() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);

		// define some transit stops
		reader.startTag(Constants.TRANSIT_STOPS, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_STOPS);
		Id stopId = new IdImpl("stop1");
		Attributes atts = new AttributesBuilder().add(Constants.ID, stopId.toString()).
				add(Constants.X, "79").add(Constants.Y, "80").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);

		// now the other stuff
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		context.push(Constants.ROUTE_PROFILE);

		reader.startTag(Constants.STOP, new AttributesBuilder().add(Constants.REF_ID, "stop1").get(), context);
		reader.endTag(Constants.STOP, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // ROUTE_PROFILE
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		reader.endTag(Constants.DEPARTURES, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitRoute route = schedule.getTransitLines().get(lineId).getRoutes().get(routeId1);
		assertEquals(1, route.getStops().size());
		TransitRouteStop stop1 = route.getStops().get(0);
		assertNotNull(stop1);
		assertEquals(schedule.getFacilities().get(stopId), stop1.getStopFacility());
		assertEquals(Time.UNDEFINED_TIME, stop1.getArrivalOffset(), MatsimTestCase.EPSILON);
		assertEquals(Time.UNDEFINED_TIME, stop1.getDepartureOffset(), MatsimTestCase.EPSILON);
		assertEquals(false, stop1.isAwaitDepartureTime());
	}

	public void testRouteProfile_MultipleStop() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);

		// define some transit stops
		reader.startTag(Constants.TRANSIT_STOPS, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_STOPS);
		Id stopId1 = new IdImpl("stop1");
		Attributes atts = new AttributesBuilder().add(Constants.ID, stopId1.toString()).
				add(Constants.X, "79").add(Constants.Y, "80").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);
		Id stopId2 = new IdImpl("stop2");
		atts = new AttributesBuilder().add(Constants.ID, stopId2.toString()).
				add(Constants.X, "51").add(Constants.Y, "42").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);
		Id stopId3 = new IdImpl("stop3");
		atts = new AttributesBuilder().add(Constants.ID, stopId3.toString()).
				add(Constants.X, "76").add(Constants.Y, "80").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);

		// now the other stuff
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		context.push(Constants.ROUTE_PROFILE);

		reader.startTag(Constants.STOP, new AttributesBuilder().add(Constants.REF_ID, "stop1").get(), context);
		reader.endTag(Constants.STOP, EMPTY_STRING, context);
		reader.startTag(Constants.STOP, new AttributesBuilder().add(Constants.REF_ID, "stop2").get(), context);
		reader.endTag(Constants.STOP, EMPTY_STRING, context);
		reader.startTag(Constants.STOP, new AttributesBuilder().add(Constants.REF_ID, "stop3").get(), context);
		reader.endTag(Constants.STOP, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // ROUTE_PROFILE
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		reader.endTag(Constants.DEPARTURES, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitRoute route = schedule.getTransitLines().get(lineId).getRoutes().get(routeId1);
		assertEquals(3, route.getStops().size());
		TransitRouteStop stop1 = route.getStops().get(0);
		assertNotNull(stop1);
		assertEquals(schedule.getFacilities().get(stopId1), stop1.getStopFacility());
		TransitRouteStop stop2 = route.getStops().get(0);
		assertNotNull(stop2);
		assertEquals(schedule.getFacilities().get(stopId1), stop2.getStopFacility());
		TransitRouteStop stop3 = route.getStops().get(0);
		assertNotNull(stop3);
		assertEquals(schedule.getFacilities().get(stopId1), stop3.getStopFacility());
	}

	public void testRouteProfileStop_Offsets() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);

		// define some transit stops
		reader.startTag(Constants.TRANSIT_STOPS, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_STOPS);
		Id stopId1 = new IdImpl("stop1");
		Attributes atts = new AttributesBuilder().add(Constants.ID, stopId1.toString()).
				add(Constants.X, "79").add(Constants.Y, "80").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);
		Id stopId2 = new IdImpl("stop2");
		atts = new AttributesBuilder().add(Constants.ID, stopId2.toString()).
				add(Constants.X, "51").add(Constants.Y, "42").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);
		Id stopId3 = new IdImpl("stop3");
		atts = new AttributesBuilder().add(Constants.ID, stopId3.toString()).
				add(Constants.X, "76").add(Constants.Y, "80").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);
		Id stopId4 = new IdImpl("stop4");
		atts = new AttributesBuilder().add(Constants.ID, stopId4.toString()).
		add(Constants.X, "5").add(Constants.Y, "11").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);

		// now the other stuff
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		context.push(Constants.ROUTE_PROFILE);

		reader.startTag(Constants.STOP, new AttributesBuilder().add(Constants.REF_ID, "stop1").
				add(Constants.ARRIVAL_OFFSET, "60").get(), context);
		reader.endTag(Constants.STOP, EMPTY_STRING, context);
		reader.startTag(Constants.STOP, new AttributesBuilder().add(Constants.REF_ID, "stop2").
				add(Constants.DEPARTURE_OFFSET, "90").get(), context);
		reader.endTag(Constants.STOP, EMPTY_STRING, context);
		reader.startTag(Constants.STOP, new AttributesBuilder().add(Constants.REF_ID, "stop3").
				add(Constants.ARRIVAL_OFFSET, "120").add(Constants.DEPARTURE_OFFSET, "150").get(), context);
		reader.endTag(Constants.STOP, EMPTY_STRING, context);
		reader.startTag(Constants.STOP, new AttributesBuilder().add(Constants.REF_ID, "stop4").get(), context);
		reader.endTag(Constants.STOP, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // ROUTE_PROFILE
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		reader.endTag(Constants.DEPARTURES, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitRoute route = schedule.getTransitLines().get(lineId).getRoutes().get(routeId1);
		assertEquals(4, route.getStops().size());
		TransitRouteStop stop1 = route.getStops().get(0);

		assertNotNull(stop1);
		assertEquals(schedule.getFacilities().get(stopId1), stop1.getStopFacility());
		assertEquals(60.0, stop1.getArrivalOffset(), MatsimTestCase.EPSILON);
		assertEquals(Time.UNDEFINED_TIME, stop1.getDepartureOffset(), MatsimTestCase.EPSILON);

		TransitRouteStop stop2 = route.getStops().get(1);
		assertNotNull(stop2);
		assertEquals(schedule.getFacilities().get(stopId2), stop2.getStopFacility());
		assertEquals(Time.UNDEFINED_TIME, stop2.getArrivalOffset(), MatsimTestCase.EPSILON);
		assertEquals(90.0, stop2.getDepartureOffset(), MatsimTestCase.EPSILON);

		TransitRouteStop stop3 = route.getStops().get(2);
		assertNotNull(stop3);
		assertEquals(schedule.getFacilities().get(stopId3), stop3.getStopFacility());
		assertEquals(120.0, stop3.getArrivalOffset(), MatsimTestCase.EPSILON);
		assertEquals(150.0, stop3.getDepartureOffset(), MatsimTestCase.EPSILON);

		TransitRouteStop stop4 = route.getStops().get(3);
		assertNotNull(stop4);
		assertEquals(schedule.getFacilities().get(stopId4), stop4.getStopFacility());
		assertEquals(Time.UNDEFINED_TIME, stop4.getArrivalOffset(), MatsimTestCase.EPSILON);
		assertEquals(Time.UNDEFINED_TIME, stop4.getDepartureOffset(), MatsimTestCase.EPSILON);
	}

	public void testRouteProfileStop_AwaitDeparture() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);

		// define some transit stops
		reader.startTag(Constants.TRANSIT_STOPS, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_STOPS);
		Id stopId1 = new IdImpl("stop1");
		Attributes atts = new AttributesBuilder().add(Constants.ID, stopId1.toString()).
				add(Constants.X, "79").add(Constants.Y, "80").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);
		Id stopId2 = new IdImpl("stop2");
		atts = new AttributesBuilder().add(Constants.ID, stopId2.toString()).
				add(Constants.X, "51").add(Constants.Y, "42").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);
		Id stopId3 = new IdImpl("stop3");
		atts = new AttributesBuilder().add(Constants.ID, stopId3.toString()).
				add(Constants.X, "76").add(Constants.Y, "80").get();
		reader.startTag(Constants.STOP_FACILITY, atts, context);
		reader.endTag(Constants.STOP_FACILITY, EMPTY_STRING, context);

		// now the other stuff
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		context.push(Constants.ROUTE_PROFILE);

		reader.startTag(Constants.STOP, new AttributesBuilder().add(Constants.REF_ID, "stop1").get(), context); // awaitDeparture not specified
		reader.endTag(Constants.STOP, EMPTY_STRING, context);
		reader.startTag(Constants.STOP, new AttributesBuilder().add(Constants.REF_ID, "stop2").
				add(Constants.AWAIT_DEPARTURE, "true").get(), context);
		reader.endTag(Constants.STOP, EMPTY_STRING, context);
		reader.startTag(Constants.STOP, new AttributesBuilder().add(Constants.REF_ID, "stop3").
				add(Constants.AWAIT_DEPARTURE, "false").get(), context);
		reader.endTag(Constants.STOP, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // ROUTE_PROFILE
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		reader.endTag(Constants.DEPARTURES, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitRoute route = schedule.getTransitLines().get(lineId).getRoutes().get(routeId1);
		assertEquals(3, route.getStops().size());
		TransitRouteStop stop1 = route.getStops().get(0);
		assertEquals(false, stop1.isAwaitDepartureTime());
		TransitRouteStop stop2 = route.getStops().get(1);
		assertEquals(true, stop2.isAwaitDepartureTime());
		TransitRouteStop stop3 = route.getStops().get(2);
		assertEquals(false, stop3.isAwaitDepartureTime());
	}

	public void testRouteProfileRoute_NoLink() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);

		// now the other stuff
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty, but must exist
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);

		reader.startTag(Constants.ROUTE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.ROUTE, EMPTY_STRING, context);

		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty, but must exist
		reader.endTag(Constants.DEPARTURES, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitRoute route = schedule.getTransitLines().get(lineId).getRoutes().get(routeId1);
		assertNull(route.getRoute());
	}

	public void testRouteProfileRoute_OneLink() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();

		NetworkLayer network = new NetworkLayer();
		NodeImpl node1 = network.createNode(new IdImpl(1), new CoordImpl(10, 5));
		NodeImpl node2 = network.createNode(new IdImpl(2), new CoordImpl(5, 11));
		NodeImpl node3 = network.createNode(new IdImpl(3), new CoordImpl(5, 11));
		NodeImpl node4 = network.createNode(new IdImpl(4), new CoordImpl(5, 11));
		NodeImpl node5 = network.createNode(new IdImpl(5), new CoordImpl(5, 11));
		/*LinkImpl link1 =*/ network.createLink(new IdImpl(1), node1, node2, 1000, 10.0, 2000.0, 1.0);
		LinkImpl link2 = network.createLink(new IdImpl(2), node2, node3, 1000, 10.0, 2000.0, 1.0);
		/*LinkImpl link3 =*/network.createLink(new IdImpl(3), node3, node4, 1000, 10.0, 2000.0, 1.0);
		/*LinkImpl link4 =*/network.createLink(new IdImpl(4), node4, node5, 1000, 10.0, 2000.0, 1.0);

		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, network);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);

		// now the other stuff
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);

		reader.startTag(Constants.ROUTE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.ROUTE);

		reader.startTag(Constants.LINK, new AttributesBuilder().add(Constants.REF_ID, "2").get(), context);
		reader.endTag(Constants.LINK, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // ROUTE

		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		reader.endTag(Constants.DEPARTURES, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitRoute route = schedule.getTransitLines().get(lineId).getRoutes().get(routeId1);
		NetworkRouteWRefs netRoute = route.getRoute();
		assertNotNull(netRoute);
		assertEquals(link2, netRoute.getStartLink());
		assertEquals(link2, netRoute.getEndLink());
		assertEquals(0, netRoute.getLinks().size());
		assertEquals(0, netRoute.getNodes().size());
	}

	public void testRouteProfileRoute_TwoLinks() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();

		NetworkLayer network = new NetworkLayer();
		NodeImpl node1 = network.createNode(new IdImpl(1), new CoordImpl(10, 5));
		NodeImpl node2 = network.createNode(new IdImpl(2), new CoordImpl(5, 11));
		NodeImpl node3 = network.createNode(new IdImpl(3), new CoordImpl(5, 11));
		NodeImpl node4 = network.createNode(new IdImpl(4), new CoordImpl(5, 11));
		NodeImpl node5 = network.createNode(new IdImpl(5), new CoordImpl(5, 11));
		/*LinkImpl link1 =*/network.createLink(new IdImpl(1), node1, node2, 1000, 10.0, 2000.0, 1.0);
		/*LinkImpl link2 =*/network.createLink(new IdImpl(2), node2, node3, 1000, 10.0, 2000.0, 1.0);
		LinkImpl link3 = network.createLink(new IdImpl(3), node3, node4, 1000, 10.0, 2000.0, 1.0);
		LinkImpl link4 = network.createLink(new IdImpl(4), node4, node5, 1000, 10.0, 2000.0, 1.0);

		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, network);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);

		// now the other stuff
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);

		reader.startTag(Constants.ROUTE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.ROUTE);

		reader.startTag(Constants.LINK, new AttributesBuilder().add(Constants.REF_ID, "3").get(), context);
		reader.endTag(Constants.LINK, EMPTY_STRING, context);
		reader.startTag(Constants.LINK, new AttributesBuilder().add(Constants.REF_ID, "4").get(), context);
		reader.endTag(Constants.LINK, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // ROUTE

		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		reader.endTag(Constants.DEPARTURES, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitRoute route = schedule.getTransitLines().get(lineId).getRoutes().get(routeId1);
		NetworkRouteWRefs netRoute = route.getRoute();
		assertNotNull(netRoute);
		assertEquals(link3, netRoute.getStartLink());
		assertEquals(link4, netRoute.getEndLink());
		assertEquals(0, netRoute.getLinks().size());
		assertEquals(1, netRoute.getNodes().size());
		assertEquals(node4, netRoute.getNodes().get(0));
	}

	public void testRouteProfileRoute_MoreLinks() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();

		NetworkLayer network = new NetworkLayer();
		NodeImpl node1 = network.createNode(new IdImpl(1), new CoordImpl(10, 5));
		NodeImpl node2 = network.createNode(new IdImpl(2), new CoordImpl(5, 11));
		NodeImpl node3 = network.createNode(new IdImpl(3), new CoordImpl(5, 11));
		NodeImpl node4 = network.createNode(new IdImpl(4), new CoordImpl(5, 11));
		NodeImpl node5 = network.createNode(new IdImpl(5), new CoordImpl(5, 11));
		LinkImpl link1 = network.createLink(new IdImpl(1), node1, node2, 1000, 10.0, 2000.0, 1.0);
		LinkImpl link2 = network.createLink(new IdImpl(2), node2, node3, 1000, 10.0, 2000.0, 1.0);
		LinkImpl link3 = network.createLink(new IdImpl(3), node3, node4, 1000, 10.0, 2000.0, 1.0);
		LinkImpl link4 = network.createLink(new IdImpl(4), node4, node5, 1000, 10.0, 2000.0, 1.0);

		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, network);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);

		// now the other stuff
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);

		reader.startTag(Constants.ROUTE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.ROUTE);

		reader.startTag(Constants.LINK, new AttributesBuilder().add(Constants.REF_ID, "1").get(), context);
		reader.endTag(Constants.LINK, EMPTY_STRING, context);
		reader.startTag(Constants.LINK, new AttributesBuilder().add(Constants.REF_ID, "2").get(), context);
		reader.endTag(Constants.LINK, EMPTY_STRING, context);
		reader.startTag(Constants.LINK, new AttributesBuilder().add(Constants.REF_ID, "3").get(), context);
		reader.endTag(Constants.LINK, EMPTY_STRING, context);
		reader.startTag(Constants.LINK, new AttributesBuilder().add(Constants.REF_ID, "4").get(), context);
		reader.endTag(Constants.LINK, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // ROUTE

		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		reader.endTag(Constants.DEPARTURES, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitRoute route = schedule.getTransitLines().get(lineId).getRoutes().get(routeId1);
		NetworkRouteWRefs netRoute = route.getRoute();
		assertNotNull(netRoute);
		assertEquals(link1, netRoute.getStartLink());
		assertEquals(link4, netRoute.getEndLink());
		assertEquals(2, netRoute.getLinks().size());
		assertEquals(link2, netRoute.getLinks().get(0));
		assertEquals(link3, netRoute.getLinks().get(1));
		assertEquals(3, netRoute.getNodes().size());
		assertEquals(node2, netRoute.getNodes().get(0));
		assertEquals(node3, netRoute.getNodes().get(1));
		assertEquals(node4, netRoute.getNodes().get(2));
	}

	public void testDepartures_Single() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("foo");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		context.push(Constants.DEPARTURES);

		Id depId1 = new IdImpl("23");
		String depTime1 = "07:35:42";
		reader.startTag(Constants.DEPARTURE, new AttributesBuilder().add(Constants.ID, depId1.toString()).
				add(Constants.DEPARTURE_TIME, depTime1).get(), context);
		reader.endTag(Constants.DEPARTURE, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // DEPATURES
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitRoute route = schedule.getTransitLines().get(lineId).getRoutes().get(routeId1);
		assertEquals(1, route.getDepartures().size());
		Departure dep1 = route.getDepartures().get(depId1);
		assertNotNull(dep1);
		assertEquals(depId1, dep1.getId());
		assertEquals(Time.parseTime(depTime1), dep1.getDepartureTime(), MatsimTestCase.EPSILON);
	}

	public void testDepartures_Multiple() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("foo");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		context.push(Constants.DEPARTURES);

		Id depId1 = new IdImpl("23");
		String depTime1 = "07:35:42";
		reader.startTag(Constants.DEPARTURE, new AttributesBuilder().add(Constants.ID, depId1.toString()).
				add(Constants.DEPARTURE_TIME, depTime1).get(), context);
		reader.endTag(Constants.DEPARTURE, EMPTY_STRING, context);

		Id depId2 = new IdImpl("42");
		String depTime2 = "07:35:42";
		reader.startTag(Constants.DEPARTURE, new AttributesBuilder().add(Constants.ID, depId2.toString()).
				add(Constants.DEPARTURE_TIME, depTime2).get(), context);
		reader.endTag(Constants.DEPARTURE, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // DEPATURES
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitRoute route = schedule.getTransitLines().get(lineId).getRoutes().get(routeId1);
		assertEquals(2, route.getDepartures().size());
		Departure dep1 = route.getDepartures().get(depId1);
		assertNotNull(dep1);
		assertEquals(depId1, dep1.getId());
		assertEquals(Time.parseTime(depTime1), dep1.getDepartureTime(), MatsimTestCase.EPSILON);
		Departure dep2 = route.getDepartures().get(depId2);
		assertNotNull(dep2);
		assertEquals(depId2, dep2.getId());
		assertEquals(Time.parseTime(depTime2), dep2.getDepartureTime(), MatsimTestCase.EPSILON);
	}

	public void testDepartures_withVehicleRef() {
		TransitSchedule schedule = new TransitScheduleBuilderImpl().createTransitSchedule();
		TransitScheduleReaderV1 reader = new TransitScheduleReaderV1(schedule, null);
		Stack<String> context = new Stack<String>();
		reader.startTag(Constants.TRANSIT_SCHEDULE, AttributesBuilder.getEmpty(), context);
		context.push(Constants.TRANSIT_SCHEDULE);
		Id lineId = new IdImpl("1");
		reader.startTag(Constants.TRANSIT_LINE, new AttributesBuilder().add(Constants.ID, lineId.toString()).get(), context);
		context.push(Constants.TRANSIT_LINE);

		Id routeId1 = new IdImpl("foo");
		reader.startTag(Constants.TRANSIT_ROUTE, new AttributesBuilder().add(Constants.ID, routeId1.toString()).get(), context);
		context.push(Constants.TRANSIT_ROUTE);

		// by definition of the file format, transitRoute *must* have transportMode, routeProfile and departures defined
		reader.startTag(Constants.TRANSPORT_MODE, AttributesBuilder.getEmpty(), context);
		reader.endTag(Constants.TRANSPORT_MODE, "bus", context);
		reader.startTag(Constants.ROUTE_PROFILE, AttributesBuilder.getEmpty(), context); // route profile can be empty
		reader.endTag(Constants.ROUTE_PROFILE, EMPTY_STRING, context);
		reader.startTag(Constants.DEPARTURES, AttributesBuilder.getEmpty(), context); // departures can be empty
		context.push(Constants.DEPARTURES);

		Id depId1 = new IdImpl("23");
		String depTime1 = "07:35:42";
		reader.startTag(Constants.DEPARTURE, new AttributesBuilder().add(Constants.ID, depId1.toString()).
				add(Constants.DEPARTURE_TIME, depTime1).add(Constants.VEHICLE_REF_ID, "v 975").get(), context);
		reader.endTag(Constants.DEPARTURE, EMPTY_STRING, context);

		Id depId2 = new IdImpl("42");
		String depTime2 = "07:35:42";
		reader.startTag(Constants.DEPARTURE, new AttributesBuilder().add(Constants.ID, depId2.toString()).
				add(Constants.DEPARTURE_TIME, depTime2).get(), context);
		reader.endTag(Constants.DEPARTURE, EMPTY_STRING, context);

		reader.endTag(context.pop(), EMPTY_STRING, context); // DEPATURES
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_ROUTE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_LINE
		reader.endTag(context.pop(), EMPTY_STRING, context); // TRANSIT_SCHEDULE

		TransitRoute route = schedule.getTransitLines().get(lineId).getRoutes().get(routeId1);
		assertEquals(2, route.getDepartures().size());
		Departure dep1 = route.getDepartures().get(depId1);
		assertNotNull(dep1);
		assertEquals(depId1, dep1.getId());
		assertEquals(Time.parseTime(depTime1), dep1.getDepartureTime(), MatsimTestCase.EPSILON);
		assertEquals(new IdImpl("v 975"), dep1.getVehicleId());
		Departure dep2 = route.getDepartures().get(depId2);
		assertNotNull(dep2);
		assertEquals(depId2, dep2.getId());
		assertEquals(Time.parseTime(depTime2), dep2.getDepartureTime(), MatsimTestCase.EPSILON);
		assertNull(dep2.getVehicleId());
	}


}
