package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NeighborPreSorterTest {
    @Test
    public void allNodesExist() {
        final Map<Integer, SortedNeighbors> presortedNeighbors = createPresortedNeighbors();

        assertEquals(73, presortedNeighbors.size());
    }

    @Test
    public void correctOrderingExample() {
        final SortedNeighbors node7Neighbors = createPresortedNeighbors().get(7);

        EdgeIteratorState lastEdge = PolygonRoutingTestGraph.DEFAULT_INSTANCE.getEdge(0, 7);
        assertEquals(19, node7Neighbors.getMostOrientedEdge(lastEdge).getAdjNode());
    }

    private Map<Integer, SortedNeighbors> createPresortedNeighbors() {
        final PolygonRoutingTestGraph graphMocker = PolygonRoutingTestGraph.DEFAULT_INSTANCE;

        return new NeighborPreSorter(graphMocker.graph).getAllSortedNeighborsLeft();
    }
}
