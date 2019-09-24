package com.graphhopper.routing.template.PolygonRoutingUtil;

import com.graphhopper.GHRequest;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
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
    final RouteCandidateList<RouteCandidateMocker> candidateList = new RouteCandidateList<>();
    final GHRequest ghRequest = new GHRequest(0,0, 10, 10).setPolygon(new Polygon(new double [] {5,10,10,5}, new double [] {5,5,10,10}));
    final PolygonThroughRoutingTemplate routingTemplate = new PolygonThroughRoutingTemplate(ghRequest, null, null, null, null, null);
    final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();

    private void addTestingCandidates() {
        this.candidateList.candidates.clear();
        this.candidateList.candidates.add(new RouteCandidateMocker(routingTemplate, 3,6,1, "a"));
        this.candidateList.candidates.add(new RouteCandidateMocker(routingTemplate, 2,3,1, "b"));
        this.candidateList.candidates.add(new RouteCandidateMocker(routingTemplate, 6,6,5, "c"));
        this.candidateList.candidates.add(new RouteCandidateMocker(routingTemplate, 4,5,3, "d"));
        this.candidateList.candidates.add(new RouteCandidateMocker(routingTemplate, 8,4,6, "e"));
        this.candidateList.candidates.add(new RouteCandidateMocker(routingTemplate, 6,1,3, "f"));
        this.candidateList.candidates.add(new RouteCandidateMocker(routingTemplate, 7,1,6, "g"));
    }

    @Test
    public void assertCorrectListContentAfterPruning() {
        addTestingCandidates();
        this.candidateList.pruneDominatedCandidateRoutes();

        assertEquals("a", this.candidateList.candidates.get(0).name);
        assertEquals("c", this.candidateList.candidates.get(1).name);
        assertEquals("b", this.candidateList.candidates.get(2).name);
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
    public void assertCorrectBestCandidate() {

    }

    public class RouteCandidateMocker extends RouteCandidatePolygon {
        protected final double polygonRouteDistance;
        protected final double roiDistance;
        protected final double directDistance;
        protected final String name;

        public RouteCandidateMocker(PolygonRoutingTemplate polygonRoutingTemplate, final double polygonRouteDistance, final double distanceInROI, final double directDistance,
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
            StringBuilder sb = new StringBuilder();

            sb.append(super.toString());
            sb.append(", name: ");
            sb.append(name);

            return sb.toString();
        }

        @Override
        public Path getMergedPath(final QueryGraph queryGraph, final AlgorithmOptions algorithmOptions) {
            Path mergedPath = new TestPath(graphMocker.graph, graphMocker.weighting, this.name);
            return mergedPath;
        }
    }

    private class TestPath extends Path {
        final String name;

        public TestPath(Graph graph, Weighting weighting, final String name) {
            super(graph, weighting);
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
