package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class SortedNeighborsTest {
    private final static PolygonRoutingTestGraph GRAPH_MOCKER = new PolygonRoutingTestGraph();

    @Test
    public void orderingWithoutCollinearEdges() {
        final SortedNeighbors sortedNeighbors = getSortedNeighbors(GRAPH_MOCKER, 57, 52);

        final int[] expectedOrder = new int[] {52, 51, 56, 55, 54, 53};

        assertOrdering(expectedOrder, sortedNeighbors);
    }

    private SortedNeighbors getSortedNeighbors(final PolygonRoutingTestGraph graphMocker, final int baseNode, final int adjNode) {
        final VectorAngleCalculator vac = new VectorAngleCalculatorLeft(graphMocker.nodeAccess);

        final EdgeIteratorState lastEdgeReversed = graphMocker.getEdge(57, 52);
        return new SortedNeighbors(vac, GRAPH_MOCKER.graph, lastEdgeReversed.getBaseNode(), lastEdgeReversed);
    }

    private void assertOrdering(int[] expectedOrder, SortedNeighbors sortedNeighbors) {
        final int[] actualOrder = extractAdjNodes(sortedNeighbors);

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
