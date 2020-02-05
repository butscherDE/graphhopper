package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

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

    private void sortFoundEdgesByBaseAndAdjNodeId(List<EdgeIteratorState> expectedUpwardsEdges, List<EdgeIteratorState> actualUpwardsEdges) {
        Comparator<EdgeIteratorState> edgeComparator = (o1, o2) -> {
            int baseNodeDif = o1.getBaseNode() - o2.getBaseNode();
            int adjNodeDif = o1.getAdjNode() - o2.getAdjNode();

            return baseNodeDif != 0 ? baseNodeDif : adjNodeDif;
        };
        Collections.sort(expectedUpwardsEdges, edgeComparator);
        Collections.sort(actualUpwardsEdges, edgeComparator);
    }

    private void assertFoundAllUpwardsEdges(List<EdgeIteratorState> expectedUpwardsEdges, List<EdgeIteratorState> actualUpwardsEdges) {
        assertEquals(expectedUpwardsEdges.size(), actualUpwardsEdges.size());

        final Iterator<EdgeIteratorState> expectedIterator = expectedUpwardsEdges.iterator();
        final Iterator<EdgeIteratorState> actualIterator = actualUpwardsEdges.iterator();
        for (int i = 0; i < expectedUpwardsEdges.size(); i++) {
            assertEdgesEqual(expectedIterator.next(), actualIterator.next());
        }
    }

    private void assertEdgesEqual(EdgeIteratorState expectedEdge, EdgeIteratorState actualEdge) {
        String message = expectedEdge.toString() + " : " + actualEdge.toString();
        assertEquals(message, expectedEdge.getBaseNode(), actualEdge.getBaseNode());
        assertEquals(message, expectedEdge.getAdjNode(), actualEdge.getAdjNode());
    }

    private List<EdgeIteratorState> getUpwardsEdges(SourceSetUpwardPathsExplorer sourceExplorer) {
        final EdgeFilter chFilter = sourceExplorer.new CHUpwardsEdgeFilter();

        final LinkedList<EdgeIteratorState> upwardsEdges = new LinkedList<>();
        final Stack<Integer> nodesToExplore = getStartingNodeSet();

        exploreUpwardsEdges(chFilter, upwardsEdges, nodesToExplore);
        pruneDuplicates(upwardsEdges);

        return upwardsEdges;
    }

    Iterator<EdgeIteratorState> getNeighborExplorer(int currentNode, CHGraph graph) {
        return graph.getOutgoingEdges(currentNode);
    }

    int getExploreNode(EdgeIteratorState incidentEdge) {
        return incidentEdge.getAdjNode();
    }

    boolean isEdgeAccepted(EdgeFilter chFilter, EdgeIteratorState incidentEdge) {
        return chFilter.accept(incidentEdge);
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
