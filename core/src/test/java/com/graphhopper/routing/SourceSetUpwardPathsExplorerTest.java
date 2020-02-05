package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SourceSetUpwardPathsExplorerTest extends SetPathExplorerTest {
    @Test
    public void testCHDownwardsEdgeFilter() {
        final SourceSetUpwardPathsExplorer sourceExplorer = getSourceExplorerInstance();

        EdgeFilter chUpwards = new CHUpwardsEdgeFilter(sourceExplorer);
        assertTrue(chUpwards.accept(GRAPH_MOCKER.getEdge(0, 1)));
        assertFalse(chUpwards.accept(GRAPH_MOCKER.getEdge(1,0)));
    }

    @Test
    public void testOnlyNonVisitedNeighborsEdgeFilter() {
        final SourceSetUpwardPathsExplorer sourceExplorer = getSourceExplorerInstance();
        final Map<Integer, Boolean> visitedNodes = getVisitedNodesMocker();
        final EdgeFilter onlyNonVisited = sourceExplorer.new OnlyNonVisitedNeighborsEdgeFilter(visitedNodes);

        assertTrue(onlyNonVisited.accept(GRAPH_MOCKER.getEdge(0,1)));
        assertFalse(onlyNonVisited.accept(GRAPH_MOCKER.getEdge(1,0)));
        assertTrue(onlyNonVisited.accept(GRAPH_MOCKER.getEdge(6,5)));
        assertFalse(onlyNonVisited.accept(GRAPH_MOCKER.getEdge(5,6)));
    }

    private Map<Integer, Boolean> getVisitedNodesMocker() {
        final Map<Integer, Boolean> visitedNodes = new HashMap<>();
        visitedNodes.put(0, true);
        visitedNodes.put(6, true);
        return visitedNodes;
    }

    @Test
    public void getUpwardsEdges() {
        final SourceSetUpwardPathsExplorer sourceExplorer = getSourceExplorerInstance();
        final List<EdgeIteratorState> expectedUpwardsEdges = getUpwardsEdges(sourceExplorer);
        final List<EdgeIteratorState> actualUpwardsEdges = sourceExplorer.getMarkedEdges();

        sortFoundEdgesByBaseAndAdjNodeId(expectedUpwardsEdges, actualUpwardsEdges);

        System.out.println(expectedUpwardsEdges);
        System.out.println(actualUpwardsEdges);

        assertFoundAllUpwardsEdges(expectedUpwardsEdges, actualUpwardsEdges);
    }

    @Test
    public void getUpwardsEdgesTwice() {
        final SourceSetUpwardPathsExplorer sourceExplorer = getSourceExplorerInstance();
        final List<EdgeIteratorState> expectedUpwardsEdges = getUpwardsEdges(sourceExplorer);
        sourceExplorer.getMarkedEdges();
        final List<EdgeIteratorState> actualUpwardsEdges = sourceExplorer.getMarkedEdges();

        sortFoundEdgesByBaseAndAdjNodeId(expectedUpwardsEdges, actualUpwardsEdges);

        assertFoundAllUpwardsEdges(expectedUpwardsEdges, actualUpwardsEdges);
    }

    private List<EdgeIteratorState> getUpwardsEdges(SourceSetUpwardPathsExplorer sourceExplorer) {
        final EdgeFilter chFilter = sourceExplorer.new CHUpwardsEdgeFilter();

        return getUpwardsEdges(chFilter);
    }

    Iterator<EdgeIteratorState> getNeighborExplorer(int currentNode, CHGraph graph) {
        return graph.getOutgoingEdges(currentNode);
    }

    int getExploreNode(EdgeIteratorState incidentEdge) {
        return incidentEdge.getAdjNode();
    }

    private SourceSetUpwardPathsExplorer getSourceExplorerInstance() {
        CHGraph chGraph = GRAPH_MOCKER.graphWithCh.getCHGraph();
        LinkedHashSet<Integer> targets = new LinkedHashSet<>(Arrays.asList(6, 25, 20, 0));
        return new SourceSetUpwardPathsExplorer(chGraph, targets);
    }

    private class CHUpwardsEdgeFilter implements EdgeFilter {
        private final EdgeFilter cHDownwardsEdgeFilter;

        private CHUpwardsEdgeFilter(final SourceSetUpwardPathsExplorer sourceExplorer) {
            cHDownwardsEdgeFilter = sourceExplorer.new CHUpwardsEdgeFilter();
        }


        @Override
        public boolean accept(EdgeIteratorState edgeState) {
            return cHDownwardsEdgeFilter.accept(edgeState);
        }
    }
}
