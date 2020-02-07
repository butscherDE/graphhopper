package com.graphhopper.storage;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class AdjacencyListInTest {
    private final static PolygonRoutingTestGraph GRAPH_MOCKER = PolygonRoutingTestGraph.DEFAULT_INSTANCE;
    private final static AdjacencyListIn ADJLIST = new AdjacencyListIn(GRAPH_MOCKER.graph, GRAPH_MOCKER.graph.getAllEdges(), GRAPH_MOCKER.weighting);
    private final static AdjacencyListIn ADJLISTCH = new AdjacencyListIn(GRAPH_MOCKER.graphWithCh.getCHGraph(), GRAPH_MOCKER.graph.getAllEdges(), GRAPH_MOCKER.weighting);

    @Test
    public void adjacencyListOf0() {
        final List<EdgeIteratorState> expectedAdj = Arrays.asList(
                GRAPH_MOCKER.getEdge(1,0),
                GRAPH_MOCKER.getEdge(7,0),
                GRAPH_MOCKER.getEdge(19, 0));

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
        final List<EdgeIteratorState> expectedAdj = Arrays.asList(
                GRAPH_MOCKER.getEdge(1,0),
                GRAPH_MOCKER.getEdge(7,0),
                GRAPH_MOCKER.getEdge(19, 0));

        final Iterator<EdgeIteratorState> actualAdj = ADJLIST.getIterator(0);

        for (int i = 0; i < expectedAdj.size(); i++) {
            final int expectedAdjNode = expectedAdj.get(i).getAdjNode();
            final int actualAdjNode = actualAdj.next().getAdjNode();

            assertEquals(String.valueOf(i), expectedAdjNode, actualAdjNode);
        }
        assertFalse(actualAdj.hasNext());
    }

    @Test
    public void uniDirectionalNeighborsOf1() {
        final PolygonRoutingTestGraph graphMocker = AdjacencyListTest.getUnidirectionalTestCase();
        final AdjacencyListIn adjList = new AdjacencyListIn(graphMocker.graph, graphMocker.graph.getAllEdges(), graphMocker.weighting);
        final List<EdgeIteratorState> expectedAdj = Arrays.asList(graphMocker.getEdge(0, 1));
        final List<EdgeIteratorState> actualAdj = adjList.getNeighbors(1);

        assertEquals(expectedAdj.size(), actualAdj.size());
        for (int i = 0; i < expectedAdj.size(); i++) {
            final int expectedAdjNode = expectedAdj.get(i).getAdjNode();
            final int actualAdjNode = actualAdj.get(i).getAdjNode();

            assertEquals(String.valueOf(i), expectedAdjNode, actualAdjNode);
        }
    }

    @Test
    public void neighborsOfNonExistingNode() {
        final PolygonRoutingTestGraph graphMocker = AdjacencyListTest.getUnidirectionalTestCase();
        final AdjacencyListIn adjList = new AdjacencyListIn(graphMocker.graph, graphMocker.graph.getAllEdges(), graphMocker.weighting);

        assertEquals(0, adjList.getNeighbors(300).size());
    }

    @Test
    public void iteratorOfNonExistingNode() {
        final PolygonRoutingTestGraph graphMocker = AdjacencyListTest.getUnidirectionalTestCase();
        final AdjacencyListIn adjList = new AdjacencyListIn(graphMocker.graph, graphMocker.graph.getAllEdges(), graphMocker.weighting);

        assertFalse(adjList.getIterator(300).hasNext());
    }

    @Test
    public void node3Adjacency() {
        final List<EdgeIteratorState> actualNeighbors = ADJLISTCH.getNeighbors(3);

        for (EdgeIteratorState actualNeighbor : actualNeighbors) {
            if (actualNeighbor.getBaseNode() == 1 && actualNeighbor.getAdjNode() == 3) {
                return;
            }
        }

        fail("Edge not contained");
    }
}
