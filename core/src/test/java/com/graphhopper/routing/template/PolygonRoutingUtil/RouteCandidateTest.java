package com.graphhopper.routing.template.PolygonRoutingUtil;

import com.graphhopper.GHRequest;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.PathMerge;
import com.graphhopper.routing.template.PolygonThroughRoutingTemplate;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RouteCandidateTest {
    final GHRequest ghRequest = new GHRequest(0, 0, 10, 10).setPolygon(new Polygon(new double [] {5, 10, 10, 5}, new double [] {5, 5, 10, 10}));
    final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();
    final PolygonThroughRoutingTemplate routingTemplate = new PolygonThroughRoutingTemplate(ghRequest, null, graphMocker.locationIndex, graphMocker.nodeAccess, graphMocker.graph
            , graphMocker.encodingManager);


    @Test
    public void testCorrectDistanceMetricsSmaller() {
        RouteCandidatePolygon testSmaller = setupSmallerRouteCandidate();

        assertEquals(1, testSmaller.getDistanceInROI(), 0);
        assertEquals(3, testSmaller.getDistance(), 0);
        assertEquals(2, testSmaller.directRouteStartEnd.getDistance(), 0);
        assertEquals(1, testSmaller.getDetourDistance(), 0);
        assertEquals(0.5, testSmaller.getGain(), 0);
    }

    @Test
    public void testCorrectDistanceMetricsGreater() {
        RouteCandidatePolygon testGreater = setupGreaterRouteCandidate();

        assertEquals(2, testGreater.getDistanceInROI(), 0);
        assertEquals(6, testGreater.getDistance(), 0);
        assertEquals(5, testGreater.directRouteStartEnd.getDistance(), 0);
        assertEquals(1, testGreater.getDetourDistance(), 0);
        assertEquals(1, testGreater.getGain(), 0);
    }

    @Test
    public void testCorrectComparision() {
        RouteCandidatePolygon testSmaller = setupSmallerRouteCandidate();
        RouteCandidatePolygon testGreater = setupGreaterRouteCandidate();

        final int comparisionResult = testSmaller.compareTo(testGreater);
        assertEquals(-1, comparisionResult);
    }

    private RouteCandidatePolygon setupGreaterRouteCandidate() {
        RouteCandidatePolygon test = new RouteCandidatePolygonThrough(routingTemplate, 0, 3, 1, 2);
        test.startToDetourEntry = createTestSubPath(7, 28, 2);
        test.detourEntryToDetourExit = createTestSubPath(72, 29, 2);
        test.detourExitToEnd = createTestSubPath(12, 3, 2);
        test.directRouteStartEnd = createDirectRoute(5);
        return test;
    }

    private RouteCandidatePolygon setupSmallerRouteCandidate() {
        RouteCandidatePolygon test = new RouteCandidatePolygonThrough(routingTemplate, 0, 3, 1, 2);
        test.startToDetourEntry = createTestSubPath(7, 28, 1);
        test.detourEntryToDetourExit = createTestSubPath(72, 29, 1);
        test.detourExitToEnd = createTestSubPath(12, 3, 1);
        test.directRouteStartEnd = createDirectRoute(2);
        return test;
    }

    private Path createTestSubPath(int edgeId, int endNode, int distance) {
        Path startPolygon = new PathMerge(graphMocker.graph, graphMocker.weighting);
        startPolygon.addEdge(edgeId);
        startPolygon.setEndNode(endNode);
        startPolygon.setDistance(distance);
        startPolygon.setFound(true);
        return startPolygon;
    }

    private Path createDirectRoute(int distance) {
        Path directRoute = new PathMerge(graphMocker.graph, graphMocker.weighting);
        directRoute.addEdge(3);
        directRoute.addEdge(8);
        directRoute.setEndNode(3);
        directRoute.setDistance(distance);
        directRoute.setFound(true);
        return directRoute;
    }
}
