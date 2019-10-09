package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.GHRequest;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.PathMerge;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

// TODO Test with empty subpaths
public class RouteCandidateTest {
    private final GHRequest ghRequest = new GHRequest(0, 0, 10, 10).setPolygon(new Polygon(new double[]{5, 10, 10, 5}, new double[]{5, 5, 10, 10})).setVehicle("car");
    private final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();

    @Test
    public void testCorrectDistanceMetricsSmaller() {
        RouteCandidatePolygon testSmaller = setupSmallerRouteCandidate();

        assertEquals(1, testSmaller.getTimeInROI(), 0);
        assertEquals(3, testSmaller.getTime(), 0);
        assertEquals(2, testSmaller.directRouteStartEnd.getTime(), 0);
        assertEquals(1, testSmaller.getDetourTime(), 0);
        assertEquals(0.5, testSmaller.getGain(), 0);
    }

    @Test
    public void testCorrectDistanceMetricsGreater() {
        RouteCandidatePolygon testGreater = setupGreaterRouteCandidate();

        assertEquals(2, testGreater.getTimeInROI(), 0);
        assertEquals(6, testGreater.getTime(), 0);
        assertEquals(5, testGreater.directRouteStartEnd.getTime(), 0);
        assertEquals(1, testGreater.getDetourTime(), 0);
        assertEquals(1, testGreater.getGain(), 0);
    }

    @Test
    public void testCorrectComparision() {
        RouteCandidatePolygon testSmaller = setupSmallerRouteCandidate();
        RouteCandidatePolygon testGreater = setupGreaterRouteCandidate();

        final int comparisionResult = testSmaller.compareTo(testGreater);
        assertEquals(-1, comparisionResult);
    }

    private RouteCandidatePolygon setupSmallerRouteCandidate() {
        Path startToDetourEntry = createTestSubPath(7, 28, 1);
        Path detourEntryToDetourExit = createTestSubPath(72, 29, 1).setFromNode(0);
        Path detourExitToEnd = createTestSubPath(12, 3, 1);
        Path directRouteStartEnd = createDirectRoute(2);

        RouteCandidatePolygon test = new RouteCandidatePolygon( 1, 3, 28, 29, startToDetourEntry, detourEntryToDetourExit,
                                                                detourExitToEnd, directRouteStartEnd);

        return test;
    }

    private RouteCandidatePolygon setupGreaterRouteCandidate() {
        Path startToDetourEntry = createTestSubPath(7, 28, 2);
        Path detourEntryToDetourExit = createTestSubPath(72, 29, 2).setFromNode(0);
        Path detourExitToEnd = createTestSubPath(12, 3, 2);
        Path directRouteStartEnd = createDirectRoute(5);

        RouteCandidatePolygon test = new RouteCandidatePolygon(1, 3, 28, 29, startToDetourEntry, detourEntryToDetourExit,
                                                               detourExitToEnd, directRouteStartEnd);

        return test;
    }

    private Path createTestSubPath(int edgeId, int endNode, int time) {
        Path startPolygon = new PathMerge(graphMocker.graph, graphMocker.weighting);

        startPolygon.addEdge(edgeId);
        startPolygon.setEndNode(endNode);
        startPolygon.setTime(time);
        startPolygon.setFound(true);

        return startPolygon;
    }

    private Path createDirectRoute(int time) {
        Path directRoute = new PathMerge(graphMocker.graph, graphMocker.weighting);

        directRoute.addEdge(3);
        directRoute.addEdge(8);
        directRoute.setEndNode(3);
        directRoute.setTime(time);
        directRoute.setFound(true);

        return directRoute;
    }

    @Test
    public void testSelfintersection() {
        final RouteCandidatePolygon testNonSelfintersecting = setupNonSelfintersectingRouteCandidate();
        final RouteCandidatePolygon testSelfintersecting = setupSelfintersectingRouteCandidate();

        final QueryGraph queryGraph = new QueryGraph(graphMocker.graph);
        final AlgorithmOptions algorithmOptions = new AlgorithmOptions("dijkstrabi", graphMocker.weighting);

        assertFalse(testNonSelfintersecting.isDetourSelfIntersecting(queryGraph, algorithmOptions));
        assertTrue(testSelfintersecting.isDetourSelfIntersecting(queryGraph, algorithmOptions));
    }

    private RouteCandidatePolygon setupNonSelfintersectingRouteCandidate() {
        return createSimpleMergedCandidate(0, 3, 0, 3, 8);
    }

    private RouteCandidatePolygon setupSelfintersectingRouteCandidate() {
        return createSimpleMergedCandidate(0, 3, 0, 3, 9, 73, 10, 8);
    }

    private RouteCandidatePolygon createSimpleMergedCandidate(final int from, final int to, final int... edgeIds) {
        RouteCandidatePolygon candidate = createRouteCandidateWithMergedPath(from, to);
        setupRouteCandidatesParameters(from, to, candidate, edgeIds);

        return candidate;
    }

    private RouteCandidatePolygon createRouteCandidateWithMergedPath(int from, int to) {
        RouteCandidatePolygon candidate = new RouteCandidatePolygon(from, to, -1, -1, null, null, null, null);
        candidate.mergedPath = new PathMerge(graphMocker.graph, graphMocker.weighting);
        return candidate;
    }

    private void setupRouteCandidatesParameters(int from, int to, RouteCandidatePolygon candidate, int[] edgeIds) {
        for (final int edgeId : edgeIds) {
            candidate.mergedPath.addEdge(edgeId);
        }
        candidate.mergedPath.setFromNode(from);
        candidate.mergedPath.setEndNode(to);
        candidate.mergedPath.extract();
    }
}
