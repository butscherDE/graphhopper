package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.RoutingAlgorithmFactorySimple;
import com.graphhopper.routing.profiles.*;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraphs;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.InstructionAnnotation;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.Translation;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Before;
import org.junit.Test;

import javax.management.Query;
import java.util.ArrayList;
import java.util.List;

import static com.graphhopper.util.Parameters.Routing.*;

public class PolygonThroughRoutingTemplateTest {
    private PolygonRoutingTestGraphs testGraphCreator = new PolygonRoutingTestGraphs();
    private EncodingManager encodingManager;
    private GraphHopperStorage testGraph;
    private Polygon polygon;
    private LocationIndex locationIndex;
    private NodeAccess nodeAccess;
    private TraversalMode traversalMode;
    private String algorithmName;
    private HintsMap algorithmHints;
    private FlagEncoder flagEncoder;
    private Weighting weighting;

    @Before
    public void initVariables() {
        this.createEncodingManager();
        this.createTestGraph();
        this.createTestPolygon();
        this.createLocationIndex();
        this.getNodeAccess();
        this.setTraversalMode();
        this.setAlgorithmName();
        this.buildHintsMap();
        this.setWeighting();
    }


    private void createEncodingManager() {
        final FlagEncoder carFlagEncoder = new CarFlagEncoder();
        this.flagEncoder = carFlagEncoder;
        this.encodingManager = EncodingManager.create(carFlagEncoder);
    }

    private void createTestGraph() {
        this.testGraph = this.testGraphCreator.createPolygonTestGraph(encodingManager);
    }

    private void createTestPolygon() {
        this.polygon = new PolygonRoutingTestGraphs().createPolygon();
    }

    private void createLocationIndex() {
        this.locationIndex = this.testGraphCreator.getCorrespondingIndex();
    }

    private void getNodeAccess() {
        this.nodeAccess = this.testGraph.getNodeAccess();
    }

    private void setTraversalMode() {
        this.traversalMode = TraversalMode.NODE_BASED;
    }

    private void setAlgorithmName() {
        this.algorithmName = "dijkstrabi";
    }

    private void buildHintsMap() {
        this.algorithmHints = new HintsMap();
        this.algorithmHints.put("elevation", "false");
        this.algorithmHints.put("instructions", "true");
        this.algorithmHints.put("way_point_max_distance", "1.0");
        this.algorithmHints.put("calc_points", "true");
        this.algorithmHints.put("type", "json");
        this.algorithmHints.put("locale", "de-DE");
        this.algorithmHints.put("weighting", "fastest");
        this.algorithmHints.put("key", "");
        this.algorithmHints.put("vehicle", "car");
    }

    private void setWeighting() {
        this.weighting = new FastestWeighting(this.flagEncoder, this.algorithmHints);
    }

    @Test
    public void quickStartingTest() {
        // Just to let something run
        GHRequest request = buildRequest(new GHPoint(25, 0), new GHPoint(25, 46));
        GHResponse response = new GHResponse();
        final int maxVisitedNodes = this.algorithmHints.getInt(MAX_VISITED_NODES, Integer.MAX_VALUE);

        RoutingTemplate routingTemplate = new PolygonThroughRoutingTemplate(request, response, this.locationIndex, this.nodeAccess, this.testGraph, this.encodingManager);
        RoutingAlgorithmFactory algorithmFactory = new RoutingAlgorithmFactorySimple();
        AlgorithmOptions algorithmOptions = buildAlgorithmOptions(algorithmHints, this.traversalMode, this.algorithmName, this.weighting, maxVisitedNodes);

        routingTemplate.calcPaths(new QueryGraph(this.testGraph), algorithmFactory, algorithmOptions);
    }

    private AlgorithmOptions buildAlgorithmOptions(HintsMap hints, TraversalMode tMode, String algoStr, Weighting weighting, int maxVisitedNodesForRequest) {
        return AlgorithmOptions.start().
                algorithm(algoStr).traversalMode(tMode).weighting(weighting).
                maxVisitedNodes(maxVisitedNodesForRequest).
                hints(hints).
                build();
    }

    private GHRequest buildRequest(GHPoint... startViaEndPoints) {
        List<GHPoint> startViaEndPointList = convertPointsToListFormat(startViaEndPoints);
        List<Double> favoredHeadings = new ArrayList<>(0);
        String vehicleStr = "car";
        String weighting = "fastest";
        String algoStr = "";
        String localeStr = "de-DE";
        boolean calcPoints = true;
        boolean instructions = true;
        double minPathPrecision = 1.0;

        GHRequest request = new GHRequest(startViaEndPointList);
        request.setVehicle(vehicleStr).
                setWeighting(weighting).
                setAlgorithm(algoStr).
                setLocale(localeStr).
                setPointHints(new ArrayList<String>()).
                setSnapPreventions(new ArrayList<String>()).
                setPathDetails(new ArrayList<String>()).
                setPolygon(this.polygon).
                getHints().
                put(CALC_POINTS, calcPoints).
                put(INSTRUCTIONS, instructions).
                put(WAY_POINT_MAX_DISTANCE, minPathPrecision);
        return request;
    }

    private static List<GHPoint> convertPointsToListFormat(GHPoint[] startViaEndPoints) {
        List<GHPoint> startViaEndPointList = new ArrayList<GHPoint>();
        for (GHPoint point : startViaEndPoints) {
            startViaEndPointList.add(point);
        }

        return startViaEndPointList;
    }

}
