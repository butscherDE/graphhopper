package com.graphhopper.storage;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AdjacencyListInTest {
    private final static PolygonRoutingTestGraph GRAPH_MOCKER = PolygonRoutingTestGraph.DEFAULT_INSTANCE;
    private final static AdjacencyListIn ADJLIST = new AdjacencyListIn(GRAPH_MOCKER.graph.getAllEdges());

    @Test
    public void adjacencyListOf0() {
        final List<EdgeIteratorState> expectedAdj = Arrays.asList(GRAPH_MOCKER.getEdge(0,1),
                GRAPH_MOCKER.getEdge(0,7),
                GRAPH_MOCKER.getEdge(0,19));

        final List<EdgeIteratorState> actualAdj = ADJLIST.getNeighbors(0);

        assertEquals(expectedAdj.size(), actualAdj.size());
        for (int i = 0; i < expectedAdj.size(); i++) {
            final int expectedAdjNode = expectedAdj.get(i).getAdjNode();
            final int actualAdjNode = actualAdj.get(i).getAdjNode();

            assertEquals(String.valueOf(i), expectedAdjNode, actualAdjNode);
        }
    }

    @Test
    public void adjacencyIteratorOf0() {
        final List<EdgeIteratorState> expectedAdj = Arrays.asList(GRAPH_MOCKER.getEdge(0,1),
                GRAPH_MOCKER.getEdge(0,7),
                GRAPH_MOCKER.getEdge(0,19));

        final Iterator<EdgeIteratorState> actualAdj = ADJLIST.getIterator(0);

        for (int i = 0; i < expectedAdj.size(); i++) {
            final int expectedAdjNode = expectedAdj.get(i).getAdjNode();
            final int actualAdjNode = actualAdj.next().getAdjNode();

            assertEquals(String.valueOf(i), expectedAdjNode, actualAdjNode);
        }
        assertFalse(actualAdj.hasNext());
    }
}
