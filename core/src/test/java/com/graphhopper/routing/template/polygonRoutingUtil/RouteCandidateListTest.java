package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.GHRequest;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.PathMerge;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.template.PolygonRoutingTemplate;
import com.graphhopper.routing.template.PolygonThroughRoutingTemplate;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RouteCandidateListTest {
    private final RouteCandidateList<RouteCandidateMocker> candidateList = new RouteCandidateList<>();
    private final GHRequest ghRequest = new GHRequest(0, 0, 10, 10).setPolygon(new Polygon(new double [] {5, 10, 10, 5}, new double [] {5, 5, 10, 10}));
    private final PolygonThroughRoutingTemplate routingTemplate = new PolygonThroughRoutingTemplate(ghRequest, null, null, null, null, null);
    private final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();

    private void addTestingCandidates() {
        this.candidateList.getCandidates().clear();
        this.candidateList.getCandidates().add(new RouteCandidateMocker(routingTemplate, 3, 6, 1, "a"));
        this.candidateList.getCandidates().get(0).startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(0).detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(0).detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting);

        this.candidateList.getCandidates().add(new RouteCandidateMocker(routingTemplate, 2, 3, 1, "b"));
        this.candidateList.getCandidates().get(1).startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(1).detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(1).detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting);

        this.candidateList.getCandidates().add(new RouteCandidateMocker(routingTemplate, 6, 6, 5, "c"));
        this.candidateList.getCandidates().get(2).startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(2).detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(2).detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting);

        this.candidateList.getCandidates().add(new RouteCandidateMocker(routingTemplate, 4, 5, 3, "d"));
        this.candidateList.getCandidates().get(3).startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(3).detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(3).detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting);

        this.candidateList.getCandidates().add(new RouteCandidateMocker(routingTemplate, 8, 4, 6, "e"));
        this.candidateList.getCandidates().get(4).startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(4).detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(4).detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting);

        this.candidateList.getCandidates().add(new RouteCandidateMocker(routingTemplate, 6, 1, 3, "f"));
        this.candidateList.getCandidates().get(5).startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(5).detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(5).detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting);

        this.candidateList.getCandidates().add(new RouteCandidateMocker(routingTemplate, 7, 1, 6, "g"));
        this.candidateList.getCandidates().get(6).startToDetourEntry = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(6).detourEntryToDetourExit = new PathMerge(graphMocker.graph, graphMocker.weighting);
        this.candidateList.getCandidates().get(6).detourExitToEnd = new PathMerge(graphMocker.graph, graphMocker.weighting);
    }

    @Test
    public void assertCorrectListContentAfterPruning() {
        addTestingCandidates();
        this.candidateList.pruneDominatedCandidateRoutes();

        assertEquals("a", this.candidateList.getCandidates().get(0).name);
        assertEquals("c", this.candidateList.getCandidates().get(1).name);
        assertEquals("b", this.candidateList.getCandidates().get(2).name);
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

    class RouteCandidateMocker extends RouteCandidatePolygon {
        final double polygonRouteDistance;
        final double roiDistance;
        final double directDistance;
        final String name;

        RouteCandidateMocker(PolygonRoutingTemplate polygonRoutingTemplate, final double polygonRouteDistance, final double distanceInROI, final double directDistance,
                             final String name) {
            super(polygonRoutingTemplate, 0,3,1,2);

            this.polygonRouteDistance = polygonRouteDistance;
            this.roiDistance = distanceInROI;
            this.directDistance = directDistance;
            this.name = name;
        }

        @Override
        public void calcPaths() {

        }

        @Override
        public double getDistance() { return this.polygonRouteDistance; }

        @Override
        public double getDistanceInROI() { return this.roiDistance; }

        @Override
        public double getDetourDistance() { return this.getDistance() - this.directDistance; }

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
