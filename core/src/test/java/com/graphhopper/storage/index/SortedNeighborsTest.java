package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.Edge;
import com.graphhopper.routing.template.util.Node;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SortedNeighborsTest {
    private final static PolygonRoutingTestGraph GRAPH_MOCKER = new PolygonRoutingTestGraph();

    @Test
    public void orderingWithoutCollinearEdges() {
        final SortedNeighbors sortedNeighbors = getSortedNeighbors(GRAPH_MOCKER, 57, 52);

        final int[] expectedOrder = new int[] {52, 51, 56, 55, 54, 53};

        assertOrdering(expectedOrder, sortedNeighbors);
    }

    @Test
    public void orderingWithMultiEdges() {
        final PolygonRoutingTestGraph graphMocker = getMultiEdgeTestGraph();

        final SortedNeighbors sortedNeighbors = getSortedNeighbors(graphMocker, 1, 0);

        assertEquals(0, sortedNeighbors.get(0).getEdge());
        assertEquals(1, sortedNeighbors.get(1).getEdge());
    }

    private PolygonRoutingTestGraph getMultiEdgeTestGraph() {
        final Node[] nodes = new Node[] {
                new Node(0, 0, 0),
                new Node(1, 0, 1)
        };
        final Edge[] edges = new Edge[] {
                new Edge(0, 1, 1, true),
                new Edge(0, 1, 1, true)
        };
        return new PolygonRoutingTestGraph(nodes, edges);
    }

    @Test
    public void collinearInEdges() {
        final PolygonRoutingTestGraph graphMocker = getCollinearInEdgesTestGraph();

        final SortedNeighbors sortedNeighbors = getSortedNeighbors(graphMocker, 2, 0);

        final int[] expectedOrder = new int[] {0, 1, 3};

        assertOrdering(expectedOrder, sortedNeighbors);
    }

    private PolygonRoutingTestGraph getCollinearInEdgesTestGraph() {
        final Node[] nodes = new Node[] {
                new Node(0, 0, 0),
                new Node(1, 0, 1),
                new Node(2, 0, 2),
                new Node(3, 0, 3)
        };
        final Edge[] edges = new Edge[] {
                new Edge(0, 2, 1, true),
                new Edge(1, 2, 1, true),
                new Edge(2, 3, 1, true)
        };
        return new PolygonRoutingTestGraph(nodes, edges);
    }

    @Test
    public void collinearOutEdges() {
        final PolygonRoutingTestGraph graphMocker = getCollinearOutEdgesTestGraph();

        final SortedNeighbors sortedNeighbors = getSortedNeighbors(graphMocker, 1, 0);

        final int[] expectedOrder = new int[] {0, 2, 3};

        assertOrdering(expectedOrder, sortedNeighbors);
    }

    private PolygonRoutingTestGraph getCollinearOutEdgesTestGraph() {
        final Node[] nodes = new Node[] {
                new Node(0, 0, 0),
                new Node(1, 0, 1),
                new Node(2, 0, 2),
                new Node(3, 0, 3)
        };
        final Edge[] edges = new Edge[] {
                new Edge(0, 1, 1, true),
                new Edge(1, 2, 1, true),
                new Edge(1, 3, 1, true)
        };
        return new PolygonRoutingTestGraph(nodes, edges);
    }

    @Test
    public void multipleNeighborsWithSameCoordinates() {
        final PolygonRoutingTestGraph graphMocker = getMultipleNeighborsWithSameCoordinatesTestGraph();

        final SortedNeighbors sortedNeighbors = getSortedNeighbors(graphMocker, 1, 0);

        final int[] expectedOrder = new int[] {0, 2, 3};

        assertOrdering(expectedOrder, sortedNeighbors);
    }

    private PolygonRoutingTestGraph getMultipleNeighborsWithSameCoordinatesTestGraph() {
        final Node[] nodes = new Node[] {
                new Node(0, 0, 0),
                new Node(1, 0, 1),
                new Node(2, 0, 2),
                new Node(3, 0, 2)
        };
        final Edge[] edges = new Edge[] {
                new Edge(0, 1, 1, true),
                new Edge(1, 2, 1, true),
                new Edge(1, 3, 1, true)
        };
        return new PolygonRoutingTestGraph(nodes, edges);
    }

    private SortedNeighbors getSortedNeighbors(final PolygonRoutingTestGraph graphMocker, final int baseNode, final int adjNode) {
        final VectorAngleCalculator vac = new VectorAngleCalculatorLeft(graphMocker.nodeAccess);

        final EdgeIteratorState lastEdgeReversed = graphMocker.getEdge(baseNode, adjNode);
        return new SortedNeighbors(vac, graphMocker.graph, lastEdgeReversed.getBaseNode(), lastEdgeReversed);
    }

    private void assertOrdering(int[] expectedOrder, SortedNeighbors sortedNeighbors) {
        final int[] actualOrder = extractAdjNodes(sortedNeighbors);
        System.out.println("Expected: " + Arrays.toString(expectedOrder));
        System.out.println("Actual: " + Arrays.toString(actualOrder));
        assertArrayEquals(expectedOrder, actualOrder);
    }

    private int[] extractAdjNodes(SortedNeighbors sortedNeighbors) {
        final int[] actualOrder = new int[sortedNeighbors.size()];

        for (int i = 0; i < sortedNeighbors.size(); i++) {
            actualOrder[i] = sortedNeighbors.get(i).getAdjNode();
        }
        return actualOrder;
    }
}
