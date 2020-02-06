package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TargetSetReverseUpwardPathsExplorerTest extends SetPathExplorerTest {
    @Test
    public void testCHDownwardsEdgeFilter() {
        final TargetSetReverseUpwardPathsExplorer targetExplorer = getTargetExplorerInstance();

        EdgeFilter chDownwards = targetExplorer.new CHDownwardsEdgeFilter();
        assertFalse(chDownwards.accept(GRAPH_MOCKER.getEdge(0, 1)));
        assertTrue(chDownwards.accept(GRAPH_MOCKER.getEdge(1,0)));
    }

    @Test
    public void testOnlyNonVisitedNeighborsEdgeFilter() {
        final TargetSetReverseUpwardPathsExplorer targetExplorer = getTargetExplorerInstance();
        final Map<Integer, Boolean> visitedNodes = getVisitedNodesMocker();
        final EdgeFilter onlyNonVisited = targetExplorer.new OnlyNonVisitedNeighborsEdgeFilter(visitedNodes);

        assertFalse(onlyNonVisited.accept(GRAPH_MOCKER.getEdge(0,1)));
        assertTrue(onlyNonVisited.accept(GRAPH_MOCKER.getEdge(1,0)));
        assertFalse(onlyNonVisited.accept(GRAPH_MOCKER.getEdge(6,5)));
        assertTrue(onlyNonVisited.accept(GRAPH_MOCKER.getEdge(5,6)));
    }

    private Map<Integer, Boolean> getVisitedNodesMocker() {
        final Map<Integer, Boolean> visitedNodes = new HashMap<>();
        visitedNodes.put(0, true);
        visitedNodes.put(6, true);
        return visitedNodes;
    }

    @Test
    public void getUpwardsEdges() {
        final TargetSetReverseUpwardPathsExplorer targetExplorer = getTargetExplorerInstance();
        final List<EdgeIteratorState> expectedUpwardsEdges = getUpwardsEdges(targetExplorer);
        final List<EdgeIteratorState> actualUpwardsEdges = targetExplorer.getMarkedEdges();

        sortFoundEdgesByBaseAndAdjNodeId(expectedUpwardsEdges, actualUpwardsEdges);

        System.out.println(expectedUpwardsEdges);
        System.out.println(actualUpwardsEdges);

        assertFoundAllUpwardsEdges(expectedUpwardsEdges, actualUpwardsEdges);
    }

    @Test
    public void getUpwardsEdgesTwice() {
        final TargetSetReverseUpwardPathsExplorer targetExplorer = getTargetExplorerInstance();
        final List<EdgeIteratorState> expectedUpwardsEdges = getUpwardsEdges(targetExplorer);
        targetExplorer.getMarkedEdges();
        final List<EdgeIteratorState> actualUpwardsEdges = targetExplorer.getMarkedEdges();

        sortFoundEdgesByBaseAndAdjNodeId(expectedUpwardsEdges, actualUpwardsEdges);

        assertFoundAllUpwardsEdges(expectedUpwardsEdges, actualUpwardsEdges);
    }

    private List<EdgeIteratorState> getUpwardsEdges(TargetSetReverseUpwardPathsExplorer targetExplorer) {
        final EdgeFilter chFilter = targetExplorer.new CHDownwardsEdgeFilter();

        return getUpwardsEdges(chFilter);
    }

    Iterator<EdgeIteratorState> getNeighborExplorer(int currentNode, CHGraph graph) {
        return graph.getIngoingEdges(currentNode);
    }

    int getExploreNode(EdgeIteratorState incidentEdge) {
        return incidentEdge.getBaseNode();
    }

    @Override
    SetPathExplorer getInstance(EdgeFilter edgeFilter) {
        return getTargetExplorerInstance(edgeFilter);
    }

    private TargetSetReverseUpwardPathsExplorer getTargetExplorerInstance() {
        return getTargetExplorerInstance(EdgeFilter.ALL_EDGES);
    }

    private TargetSetReverseUpwardPathsExplorer getTargetExplorerInstance(EdgeFilter edgeFilter) {
        CHGraph chGraph = GRAPH_MOCKER.graphWithCh.getCHGraph();
        LinkedHashSet<Integer> targets = new LinkedHashSet<>(Arrays.asList(6, 25, 20, 0));
        return new TargetSetReverseUpwardPathsExplorer(chGraph, targets, edgeFilter);
    }

    private class CHUpwardsEdgeFilter implements EdgeFilter {
        private final EdgeFilter cHDownwardsEdgeFilter;

        private CHUpwardsEdgeFilter(final TargetSetReverseUpwardPathsExplorer targetExplorer) {
            cHDownwardsEdgeFilter = targetExplorer.new CHDownwardsEdgeFilter();
        }


        @Override
        public boolean accept(EdgeIteratorState edgeState) {
            return !cHDownwardsEdgeFilter.accept(edgeState);
        }
    }
}
