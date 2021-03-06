/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package org.matsim.integration.daily.accessibility;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.accessibility.AccessibilityConfigGroup;
import org.matsim.contrib.accessibility.AccessibilityStartupListener;
import org.matsim.contrib.accessibility.FacilityTypes;
import org.matsim.contrib.accessibility.Modes4Accessibility;
import org.matsim.contrib.accessibility.utils.AccessibilityUtils;
import org.matsim.contrib.accessibility.utils.VisualizationUtils;
import org.matsim.contrib.matrixbasedptrouter.MatrixBasedPtRouterConfigGroup;
import org.matsim.contrib.matrixbasedptrouter.utils.BoundingBox;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.config.groups.VspExperimentalConfigGroup.VspDefaultsCheckingLevel;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.testcases.MatsimTestUtils;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author dziemke
 */
public class AccessibilityComputationCapeTownTest {
	public static final Logger log = Logger.getLogger(AccessibilityComputationCapeTownTest.class);

//	private static final double cellSize = 1000.;
	private static final Double cellSize = 10000.;
//	private static final double timeOfDay = 8.*60*60;

	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;

	@Test
	public void doAccessibilityTest() throws IOException {
		// Input and output
		String folderStructure = "../../";
		String networkFile = "matsimExamples/countries/za/capetown/2015-10-15_network.xml";
		// Adapt folder structure that may be different on different machines, in particular on server
		folderStructure = PathUtils.tryANumberOfFolderStructures(folderStructure, networkFile);
		networkFile = folderStructure + networkFile ;
		final String facilitiesFile = folderStructure + "matsimExamples/countries/za/capetown/2015-10-15_facilities.xml";
		final String outputDirectory = utils.getOutputDirectory();
//		final String travelTimeMatrixFile = folderStructure + "matsimExamples/countries/za/nmb/regular-pt/travelTimeMatrix_space.csv";
//		final String travelDistanceMatrixFile = folderStructure + "matsimExamples/countries/za/nmb/regular-pt/travelDistanceMatrix_space.csv";
//		final String ptStopsFile = folderStructure + "matsimExamples/countries/za/nmb/regular-pt/ptStops.csv";
		
		// Parameters
		final String crs = TransformationFactory.WGS84_SA_Albers;
		final String runId = "za_capetown_" + PathUtils.getDate() + "_" + cellSize.toString().split("\\.")[0];
		final boolean push2Geoserver = false;

		// QGis parameters
		boolean createQGisOutput = false;
		final boolean includeDensityLayer = true;
		final Double lowerBound = 2.; // (upperBound - lowerBound) is ideally easily divisible by 7
		final Double upperBound = 5.5;
		final Integer range = 9; // in the current implementation, this must always be 9
		final int symbolSize = 1010;
		final int populationThreshold = (int) (200 / (1000/cellSize * 1000/cellSize));
		
		// Config and scenario
		final Config config = ConfigUtils.createConfig(new AccessibilityConfigGroup(), new MatrixBasedPtRouterConfigGroup());
		config.network().setInputFile(networkFile);
		config.facilities().setInputFile(facilitiesFile);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory(outputDirectory);
		config.controler().setLastIteration(0);
		
		// Choose modes for accessibility computation
		AccessibilityConfigGroup acg = ConfigUtils.addOrGetModule(config, AccessibilityConfigGroup.GROUP_NAME, AccessibilityConfigGroup.class);
		acg.setComputingAccessibilityForMode(Modes4Accessibility.freeSpeed, true);
		acg.setComputingAccessibilityForMode(Modes4Accessibility.car, true);
		acg.setComputingAccessibilityForMode(Modes4Accessibility.bike, true);
		acg.setComputingAccessibilityForMode(Modes4Accessibility.walk, true);
		acg.setComputingAccessibilityForMode(Modes4Accessibility.pt, false);
		
		// Some (otherwise irrelevant) settings to make the vsp check happy
		config.timeAllocationMutator().setMutationRange(7200.);
		config.timeAllocationMutator().setAffectingDuration(false);
		config.plans().setRemovingUnneccessaryPlanAttributes(true);
		config.plans().setActivityDurationInterpretation(PlansConfigGroup.ActivityDurationInterpretation.tryEndTimeThenDuration);
		
		config.vspExperimental().setVspDefaultsCheckingLevel(VspDefaultsCheckingLevel.warn);
		
		StrategySettings stratSets = new StrategySettings();
		stratSets.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta.toString());
		stratSets.setWeight(1.);
		config.strategy().addStrategySettings(stratSets);
		
		final Scenario scenario = ScenarioUtils.loadScenario(config);
		
		// Matrix-based pt
//		MatrixBasedPtRouterConfigGroup mbpcg = (MatrixBasedPtRouterConfigGroup) config.getModule(MatrixBasedPtRouterConfigGroup.GROUP_NAME);
//		mbpcg.setPtStopsInputFile(ptStopsFile);
//		mbpcg.setUsingTravelTimesAndDistances(true);
//		mbpcg.setPtTravelDistancesInputFile(travelDistanceMatrixFile);
//		mbpcg.setPtTravelTimesInputFile(travelTimeMatrixFile);

		assertNotNull(config);
		
		// Network bounds
		BoundingBox networkBounds = BoundingBox.createBoundingBox(scenario.getNetwork());
		Envelope networkEnvelope = new Envelope(networkBounds.getXMin(), networkBounds.getXMax(), networkBounds.getYMin(), networkBounds.getYMax());
		
		// Envelope by network
		BoundingBox boundingBox = BoundingBox.createBoundingBox(scenario.getNetwork());
		Envelope envelope = new Envelope(boundingBox.getXMin(), boundingBox.getXMax(),
										 boundingBox.getYMin(), boundingBox.getYMin());
		
		// Collect activity types
//		final List<String> activityTypes = AccessibilityRunUtils.collectAllFacilityOptionTypes(scenario);
//		log.info("Found activity types: " + activityTypes);
		final List<String> activityTypes = new ArrayList<>();
		activityTypes.add(FacilityTypes.SHOPPING);
		
		// Network density points
		ActivityFacilities measuringPoints = AccessibilityUtils.createMeasuringPointsFromNetworkBounds(scenario.getNetwork(), cellSize);
		double maximumAllowedDistance = 0.5 * cellSize;
		final ActivityFacilities densityFacilities = AccessibilityUtils.createNetworkDensityFacilities(
				scenario.getNetwork(), measuringPoints, maximumAllowedDistance);

		// Controller
		final Controler controler = new Controler(scenario);
		controler.addControlerListener(new AccessibilityStartupListener(activityTypes, densityFacilities, crs, runId, networkEnvelope, cellSize, push2Geoserver));
		controler.run();
		
		// QGis
		if (createQGisOutput == true) {
			String osName = System.getProperty("os.name");
			String workingDirectory = config.controler().getOutputDirectory();
			for (String actType : activityTypes) {
				String actSpecificWorkingDirectory = workingDirectory + actType + "/";
				for ( Modes4Accessibility mode : Modes4Accessibility.values()) {
					if (acg.getIsComputingMode().contains(mode)) {
						VisualizationUtils.createQGisOutput(actType, mode, envelope, workingDirectory, crs, includeDensityLayer,
								lowerBound, upperBound, range, symbolSize, populationThreshold);
						VisualizationUtils.createSnapshot(actSpecificWorkingDirectory, mode, osName);
					}
				}
			}  
		}
	}
}