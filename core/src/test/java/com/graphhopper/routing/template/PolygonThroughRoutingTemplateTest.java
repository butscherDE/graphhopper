package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.*;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.graphhopper.util.Parameters.Routing.*;
import static org.junit.Assert.assertEquals;

public class PolygonThroughRoutingTemplateTest {
    private final static PolygonRoutingTestGraph graphMocker = PolygonRoutingTestGraph.DEFAULT_INSTANCE;

    @Test
    public void quickStartingTest() {
        // Just to let something run
        final GHRequest request = buildRequest(new GHPoint(25, 0), new GHPoint(25, 46));
        final GHResponse response = new GHResponse();
        final RoutingTemplate routingTemplate = new PolygonThroughRoutingTemplate(request, response, this.graphMocker.locationIndex, this.graphMocker.encodingManager);
        final RoutingAlgorithmFactory algorithmFactory = new RoutingAlgorithmFactorySimple();
        final AlgorithmOptions algorithmOptions = graphMocker.algorithmOptions;
        final QueryGraph queryGraph = createQueryGraph(request, routingTemplate);

        List<Path> paths = routingTemplate.calcPaths(queryGraph, algorithmFactory, algorithmOptions);

        printPath(paths);

        assertEquals(new ArrayList<Integer>(Arrays.asList(new Integer[]{0, 7, 44, 46, 54, 55, 49, 32, 12, 13, 5, 6})), paths.get(0).getNodesInPathOrder());
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

    @Test
    public void showAllEdgesWithIDs() {
        AllEdgesIterator aei = this.graphMocker.graph.getAllEdges();
        while (aei.next()) {
            System.out.println(aei.toString());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void failOnEmptyPolygon() {
        final Polygon emptyPolygon = new Polygon(new double[]{24, 24, 23, 23}, new double[]{2, 3, 3, 2});
        final GHRequest request = buildRequest(new GHPoint(25, 0), new GHPoint(25, 46));
        request.setPolygon(emptyPolygon);
        final GHResponse response = new GHResponse();
        final RoutingTemplate routingTemplate = new PolygonThroughRoutingTemplate(request, response, this.graphMocker.locationIndex, this.graphMocker.encodingManager);
        final RoutingAlgorithmFactory algorithmFactory = new RoutingAlgorithmFactorySimple();
        final AlgorithmOptions algorithmOptions = graphMocker.algorithmOptions;
        final QueryGraph queryGraph = createQueryGraph(request, routingTemplate);

        List<Path> paths = routingTemplate.calcPaths(queryGraph, algorithmFactory, algorithmOptions);
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

}
