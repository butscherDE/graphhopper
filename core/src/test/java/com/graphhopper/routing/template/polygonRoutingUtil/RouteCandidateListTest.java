package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.GHRequest;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.PathMerge;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RouteCandidateListTest {
    private final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();
    private final RouteCandidateList<RouteCandidateMocker> candidateList = new RouteCandidateList<>();
    private final GHRequest ghRequest = new GHRequest(0, 0, 10, 10).setPolygon(new Polygon(new double[]{5, 10, 10, 5}, new double[]{5, 5, 10, 10})).setVehicle("car");

    private void addTestingCandidates() {
        this.candidateList.clear();

        Path startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        Path detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        Path detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        this.candidateList.add(new RouteCandidateMocker(3, 6, 1, "a", startToDetourEntry,
                                                                        detourEntryToDetourExit, detourExitToEnd, null));

        startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        this.candidateList.add(new RouteCandidateMocker(2, 3, 1, "b", startToDetourEntry,
                                                                        detourEntryToDetourExit, detourExitToEnd, null));

        startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        this.candidateList.add(new RouteCandidateMocker(6, 6, 5, "c", startToDetourEntry,
                                                                        detourEntryToDetourExit, detourExitToEnd, null));

        startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        this.candidateList.add(new RouteCandidateMocker(4, 5, 3, "d", startToDetourEntry,
                                                                        detourEntryToDetourExit, detourExitToEnd, null));

        startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        this.candidateList.add(new RouteCandidateMocker(8, 4, 6, "e", startToDetourEntry,
                                                                        detourEntryToDetourExit, detourExitToEnd, null));

        startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        this.candidateList.add(new RouteCandidateMocker(6, 1, 3, "f", startToDetourEntry,
                                                                        detourEntryToDetourExit, detourExitToEnd, null));

        startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting).setFound(true);
        this.candidateList.add(new RouteCandidateMocker(7, 1, 6, "g", startToDetourEntry,
                                                                        detourEntryToDetourExit, detourExitToEnd, null));
    }

    @Test
    public void assertCorrectListContentAfterPruning() {
        addTestingCandidates();
        this.candidateList.pruneDominatedCandidateRoutes();

        assertEquals("a", this.candidateList.get(0).name);
        assertEquals("c", this.candidateList.get(1).name);
        assertEquals("b", this.candidateList.get(2).name);
    }

    @Test
    public void assertCorrectTopThreeRoutes() {
        addTestingCandidates();
        this.candidateList.pruneDominatedCandidateRoutes();
        this.candidateList.sortByGainAscending();

        List<Path> topCandidates = this.candidateList.getFirstAsPathList(3, new QueryGraph(graphMocker.graph), new AlgorithmOptions("dijkstrabi", graphMocker.weighting,
                                                                                                                                    graphMocker.traversalMode));

        assertEquals("c", ((TestPath) topCandidates.get(0)).name);
        assertEquals("a", ((TestPath) topCandidates.get(1)).name);
        assertEquals("b", ((TestPath) topCandidates.get(2)).name);
        assertEquals(3, topCandidates.size());
    }

    @Test
    public void testIllegalStartToDetourSubpath() {
        addTestingCandidates();
        final RouteCandidateMocker testingCandidate = this.candidateList.get(0);
        testingCandidate.startToDetourEntry.setFound(false);

        illegalCandidateNotAdded(testingCandidate);
    }

    @Test
    public void testIllegalDetourEntryToDetourExitSubpath() {
        addTestingCandidates();
        final RouteCandidateMocker testingCandidate = this.candidateList.get(0);
        testingCandidate.detourEntryToDetourExit.setFound(false);

        illegalCandidateNotAdded(testingCandidate);
    }

    @Test
    public void testIllegalDetourExitToEndSubpath() {
        addTestingCandidates();
        final RouteCandidateMocker testingCandidate = this.candidateList.get(0);
        testingCandidate.detourExitToEnd.setFound(false);

        illegalCandidateNotAdded(testingCandidate);
    }

    private void illegalCandidateNotAdded(RouteCandidateMocker testingCandidate) {
        final int sizeBeforeAdding = this.candidateList.size();
        this.candidateList.add(testingCandidate);
        assertEquals(sizeBeforeAdding, this.candidateList.size());
    }

    class RouteCandidateMocker extends RouteCandidatePolygon {
        final double polygonRouteDistance;
        final double roiDistance;
        final double directDistance;
        final String name;

        RouteCandidateMocker(final double polygonRouteDistance, final double distanceInROI, final double directDistance,
                             final String name, final Path startToDetourEntry, final Path detourEntryToDetourExit, final Path detourExitToEnd,
                             final Path directRouteStartEnd) {
            super(0, 3, 1, 2, startToDetourEntry, detourEntryToDetourExit, detourExitToEnd, directRouteStartEnd);

            this.polygonRouteDistance = polygonRouteDistance;
            this.roiDistance = distanceInROI;
            this.directDistance = directDistance;
            this.name = name;
        }

        @Override
        public double getDistance() {
            return this.polygonRouteDistance;
        }

        @Override
        public double getDistanceInROI() {
            return this.roiDistance;
        }

        @Override
        public double getDetourDistance() {
            return this.getDistance() - this.directDistance;
        }

        @Override
        public String toString() {

            String sb = super.toString() +
                        ", name: " +
                        name;
            return sb;
        }

        @Override
        public Path getMergedPath(final QueryGraph queryGraph, final AlgorithmOptions algorithmOptions) {
            return new TestPath(graphMocker.graph, graphMocker.weighting, this.name);
        }
    }

    private static class TestPath extends Path {
        final String name;

        TestPath(Graph graph, Weighting weighting, final String name) {
            super(graph, weighting);
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
