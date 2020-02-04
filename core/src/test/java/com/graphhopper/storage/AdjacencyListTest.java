package com.graphhopper.storage;

import com.graphhopper.routing.template.util.Edge;
import com.graphhopper.routing.template.util.Node;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;

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
}
