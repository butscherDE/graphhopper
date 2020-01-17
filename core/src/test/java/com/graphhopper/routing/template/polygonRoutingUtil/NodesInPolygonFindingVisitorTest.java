package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NodesInPolygonFindingVisitorTest {
    private final static PolygonRoutingTestGraph graphMocker = PolygonRoutingTestGraph.DEFAULT_INSTANCE;

    @Test
    public void testCase() {
        // Single nodes, small and high degrees, edge intersections included
        final Polygon polygon = new Polygon(new double[] {30, 30, 20, 12, 14, 17, 24}, new double[] {32, 36, 36, 24, 21, 28, 28});

        final List<Integer> nodesInPolygon = extractNodesInPolygon(polygon);

        assertEquals(new ArrayList<>(Arrays.asList(4, 9, 10, 49, 200, 201, 202)), nodesInPolygon);
    }

    private List<Integer> extractNodesInPolygon(Polygon polygon) {
        final NodesInPolygonFindingVisitor nodeFindingVisitor = executeQueryWithVisitor(polygon);

        final List<Integer> nodesInPolygon = nodeFindingVisitor.getNodesInPolygon();
        Collections.sort(nodesInPolygon);

        return nodesInPolygon;
    }

    private NodesInPolygonFindingVisitor executeQueryWithVisitor(Polygon polygon) {
        final NodesInPolygonFindingVisitor nodeFindingVisitor = new NodesInPolygonFindingVisitor(polygon, graphMocker.nodeAccess);
        graphMocker.locationIndex.query(polygon.getMinimalBoundingBox(), nodeFindingVisitor);

        return nodeFindingVisitor;
    }
}
