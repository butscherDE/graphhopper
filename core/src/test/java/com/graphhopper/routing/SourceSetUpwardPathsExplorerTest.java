package com.graphhopper.routing;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SourceSetUpwardPathsExplorerTest {
    private final static PolygonRoutingTestGraph GRAPH_MOCKER = PolygonRoutingTestGraph.DEFAULT_INSTANCE;

    @Test
    public void testIfCHCreationWorked() {
        final GraphHopperStorage ghs = GRAPH_MOCKER.graphWithCh;
        final CHGraph chGraph = ghs.getCHGraph();

        final List<Integer> allNodes = getAllNodes(chGraph);

        printAllNodesWithRankWellAligned(chGraph, allNodes);
    }

    private List<Integer> getAllNodes(Graph chGraph) {
        final EdgeIterator allEdges = chGraph.getAllEdges();
        final Set<Integer> allNodes = new LinkedHashSet<>();
        while(allEdges.next()) {
            allNodes.add(allEdges.getBaseNode());
            allNodes.add(allEdges.getAdjNode());
        }
        final List<Integer> nodesAsList = new ArrayList<>(allNodes);
        Collections.sort(nodesAsList);
        return nodesAsList;
    }

    private void printAllNodesWithRankWellAligned(CHGraph chGraph, List<Integer> allNodes) {
        for (Integer node : allNodes) {
            int log = (int) Math.log10(node) + 1;
            final int nodeDigits = log >= 0 ? log : 1;

            System.out.print(node + ":");
            for (int i = nodeDigits; i < 5; i++) {
                System.out.print(" ");
            }
            System.out.println(chGraph.getLevel(node));
        }
    }

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

    private Stack<Integer> getStartingNodeSet() {
        final Stack<Integer> nodesToExplore = new Stack<>();
        nodesToExplore.push(6);
        nodesToExplore.push(25);
        nodesToExplore.push(20);
        nodesToExplore.push(0);
        return nodesToExplore;
    }

    private void exploreUpwardsEdges(EdgeFilter chFilter, LinkedList<EdgeIteratorState> upwardsEdges, Stack<Integer> nodesToExplore) {
        final List<Integer> exploredNodes = new LinkedList<>();
        while (!nodesToExplore.isEmpty()) {
            final int currentNode = nodesToExplore.pop();
            exploredNodes.add(currentNode);

            CHGraph graph = GRAPH_MOCKER.graphWithCh.getCHGraph();
            final Iterator<EdgeIteratorState> neighborExplorer = graph.getOutgoingEdges(currentNode);
            while (neighborExplorer.hasNext()) {
                final EdgeIteratorState incidentEdge = neighborExplorer.next();
                if (chFilter.accept(incidentEdge)) {
                    int adjNode = incidentEdge.getAdjNode();
                    upwardsEdges.add(incidentEdge);
                    if (!exploredNodes.contains(adjNode)) {
                        nodesToExplore.push(adjNode);
                    }
                }
            }
        }
    }

    private void pruneDuplicates(LinkedList<EdgeIteratorState> upwardsEdges) {
        for (int i = upwardsEdges.size() - 1; i >= 0; i--) {
            final EdgeIteratorState edgeToPossiblyPrune = upwardsEdges.get(i);
            int firstIndex = -1;
            for (final EdgeIteratorState possibleDuplicate : upwardsEdges) {
                firstIndex++;
                if (edgeToPossiblyPrune.getEdge() == possibleDuplicate.getEdge() &&
                        edgeToPossiblyPrune.getAdjNode() == possibleDuplicate.getAdjNode()) {
                    break;
                }
            }

            if (firstIndex < i) {
                upwardsEdges.remove(i);
            }
        }
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
