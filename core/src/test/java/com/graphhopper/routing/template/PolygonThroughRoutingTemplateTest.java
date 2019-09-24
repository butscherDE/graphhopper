package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.*;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.graphhopper.util.Parameters.Routing.*;

public class PolygonThroughRoutingTemplateTest {
    private PolygonRoutingTestGraph testGraph = new PolygonRoutingTestGraph();


    @Before
    public void initVariables() {
    }

    @Test
    public void quickStartingTest() {
        // Just to let something run
        GHRequest request = buildRequest(new GHPoint(25, 0), new GHPoint(25, 46));
        GHResponse response = new GHResponse();
        final int maxVisitedNodes = this.testGraph.algorithmHints.getInt(MAX_VISITED_NODES, Integer.MAX_VALUE);

        RoutingTemplate routingTemplate = new PolygonThroughRoutingTemplate(request, response, this.testGraph.locationIndex, this.testGraph.nodeAccess, this.testGraph.graph,
                                                                            this.testGraph.encodingManager);
        RoutingAlgorithmFactory algorithmFactory = new RoutingAlgorithmFactorySimple();
        AlgorithmOptions algorithmOptions = buildAlgorithmOptions(testGraph.algorithmHints, this.testGraph.traversalMode, this.testGraph.algorithmName, this.testGraph.weighting, maxVisitedNodes);

        List<Path> paths = routingTemplate.calcPaths(new QueryGraph(this.testGraph.graph), algorithmFactory, algorithmOptions);
        System.out.println(paths.get(0).getNodesInPathOrder());
        System.out.println(paths.toString());
    }

    @Test
    public void showAllEdgesWithIDs() {
        AllEdgesIterator aei = this.testGraph.graph.getAllEdges();
        while (aei.next()) {
            System.out.println(aei.toString());
        }
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
                setPolygon(this.testGraph.polygon).
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
