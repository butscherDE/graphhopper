package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

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

    private List<EdgeIteratorState> getUpwardsEdges(TargetSetReverseUpwardPathsExplorer targetExplorer) {
        final EdgeFilter chFilter = new CHUpwardsEdgeFilter(targetExplorer);

        final LinkedList<EdgeIteratorState> upwardsEdges = new LinkedList<>();
        final Stack<Integer> nodesToExplore = getStartingNodeSet();

        exploreUpwardsEdges(chFilter, upwardsEdges, nodesToExplore);
        pruneDuplicates(upwardsEdges);

        return upwardsEdges;
    }

    Iterator<EdgeIteratorState> getNeighborExplorer(int currentNode, CHGraph graph) {
        return graph.getIngoingEdges(currentNode);
    }

    int getExploreNode(EdgeIteratorState incidentEdge) {
        return incidentEdge.getBaseNode();
    }

    boolean isEdgeAccepted(EdgeFilter chFilter, EdgeIteratorState incidentEdge) {
        return !chFilter.accept(incidentEdge);
    }

    private TargetSetReverseUpwardPathsExplorer getTargetExplorerInstance() {
        CHGraph chGraph = GRAPH_MOCKER.graphWithCh.getCHGraph();
        LinkedHashSet<Integer> targets = new LinkedHashSet<>(Arrays.asList(6, 25, 20, 0));
        return new TargetSetReverseUpwardPathsExplorer(chGraph, targets);
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
