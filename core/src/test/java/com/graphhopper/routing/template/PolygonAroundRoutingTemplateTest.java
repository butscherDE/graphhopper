package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.*;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.graphhopper.util.Parameters.Routing.*;
import static org.junit.Assert.assertEquals;

public class PolygonAroundRoutingTemplateTest {
    private final static PolygonRoutingTestGraph graphMocker = PolygonRoutingTestGraph.DEFAULT_INSTANCE;

    @Test
    public void easyRoutingTest() {
        final int startNode = 19;
        final double startLatitude = graphMocker.nodeAccess.getLatitude(startNode);
        final double startLongitude = graphMocker.nodeAccess.getLongitude(startNode);
        final GHPoint startGhPoint = new GHPoint(startLatitude, startLongitude);
        final int endNode = 4;
        final double endLatitude = graphMocker.nodeAccess.getLatitude(endNode);
        final double endLongitude = graphMocker.nodeAccess.getLongitude(endNode);
        final GHPoint endGhPoint = new GHPoint(endLatitude, endLongitude);

        final GHRequest request = buildRequest(startGhPoint, endGhPoint);
        final GHResponse response = new GHResponse();
        final RoutingTemplate routingTemplate = new PolygonAroundRoutingTemplate(request, response, this.graphMocker.locationIndex, this.graphMocker.encodingManager);
        final RoutingAlgorithmFactory algorithmFactory = new RoutingAlgorithmFactorySimple();
        final AlgorithmOptions algorithmOptions = graphMocker.algorithmOptions;
        final QueryGraph queryGraph = createQueryGraph(request, routingTemplate);

        List<Path> paths = routingTemplate.calcPaths(queryGraph, algorithmFactory, algorithmOptions);

        printPath(paths);

        System.out.println(paths.get(0).getDistance());
        showAllEdgesWithIDs(routingTemplate);
        assertEquals(new ArrayList<Integer>(Arrays.asList(new Integer[]{19,0,1,28,29,3,4})), paths.get(0).getNodesInPathOrder());
    }

    private void showAllEdgesWithIDs(final RoutingTemplate template) {
        AllEdgesIterator aei = this.graphMocker.graph.getAllEdges();
        final PolygonRoutingTemplate polygonTemplate = (PolygonRoutingTemplate) template;

        while (aei.next()) {
            if (polygonTemplate.pathSkeletonEdgeFilter.accept(aei)) {
                System.out.println(aei.toString());
            } else {
                System.out.print("\u001B[31m");
                System.out.print(aei.toString());
                System.out.println("\u001B[0m");
            }
        }
    }

    private GHRequest buildRequest(GHPoint... startViaEndPoints) {
        List<GHPoint> startViaEndPointList = convertPointsToListFormat(startViaEndPoints);
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
                setPolygon(this.graphMocker.polygon).
                getHints().
                put(CALC_POINTS, calcPoints).
                put(INSTRUCTIONS, instructions).
                put(WAY_POINT_MAX_DISTANCE, minPathPrecision);
        return request;
    }

    private static List<GHPoint> convertPointsToListFormat(GHPoint[] startViaEndPoints) {
        List<GHPoint> startViaEndPointList = new ArrayList<>(Arrays.asList(startViaEndPoints));

        return startViaEndPointList;
    }

    private QueryGraph createQueryGraph(GHRequest request, RoutingTemplate routingTemplate) {
        final QueryGraph queryGraph = new QueryGraph(this.graphMocker.graph);
        List<QueryResult> results = routingTemplate.lookup(request.getPoints(), this.graphMocker.flagEncoder);
        queryGraph.lookup(results);
        return queryGraph;
    }

    private void printPath(List<Path> paths) {
        System.out.println(paths.get(0).getNodesInPathOrder());
        System.out.println(paths.toString());
    }
}
