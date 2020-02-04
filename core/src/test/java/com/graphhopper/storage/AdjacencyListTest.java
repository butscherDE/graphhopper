package com.graphhopper.storage;

import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.template.util.Edge;
import com.graphhopper.routing.template.util.Node;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdjacencyListTest {
    public static PolygonRoutingTestGraph getUnidirectionalTestCase() {
        final Node[] nodes = new Node[]{
                new Node(0, 0, 0),
                new Node(1, Double.MIN_VALUE, 1),
                new Node(2, 0, 2)};
        final Edge[] edges = new Edge[] {
                new Edge(0, 1, 1, false),
                new Edge(1, 2, 1, false)
        };

        return new PolygonRoutingTestGraph(nodes, edges);
    }

    @Test
    public void flagEncoderLearningTest() {
        final PolygonRoutingTestGraph graphMocker = getUnidirectionalTestCase();
        final FlagEncoder flagEncoder = graphMocker.flagEncoder;
        final BooleanEncodedValue accessEnc = flagEncoder.getAccessEnc();

        EdgeIteratorState edge = graphMocker.getEdge(0, 1);
        assertTrue(edge.get(accessEnc));
        assertFalse(edge.detach(true).get(accessEnc));

        edge = graphMocker.graph.edge(0,2, 1, true);
        assertTrue(edge.get(accessEnc));
        assertTrue(edge.detach(true).get(accessEnc));
    }
}
