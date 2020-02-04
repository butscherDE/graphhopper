package com.graphhopper.routing;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.*;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.graphvisualizer.NodesAndNeighborDump;
import com.graphhopper.util.graphvisualizer.SwingGraphGUI;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TargetSetReverseUpwardPathsExplorerTest {
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
    public void testInEdgeHelper() {
        final TargetSetReverseUpwardPathsExplorer targetExplorer = getTargetExplorerInstance();
        final EdgeFilter inEdgeHelper = targetExplorer.new InEdgeHelper(1);

        assertTrue(inEdgeHelper.accept(GRAPH_MOCKER.getEdge(0,1)));
        assertFalse(inEdgeHelper.accept(GRAPH_MOCKER.getEdge(1,0)));
        assertFalse(inEdgeHelper.accept(GRAPH_MOCKER.getEdge(1,2)));
    }

    @Test
    public void getUpwardsEdges() {
        final TargetSetReverseUpwardPathsExplorer targetExplorer = getTargetExplorerInstance();
        final List<EdgeIteratorState> expectedUpwardsEdges = getUpwardsEdges(targetExplorer);
        final List<EdgeIteratorState> actualUpwardsEdges = targetExplorer.getMarkedEdges();

        Comparator<EdgeIteratorState> edgeComparator = new Comparator<EdgeIteratorState>() {
            @Override
            public int compare(EdgeIteratorState o1, EdgeIteratorState o2) {
                int baseNodeDif = o1.getBaseNode() - o2.getBaseNode();
                int adjNodeDif = o1.getAdjNode() - o2.getAdjNode();

                return baseNodeDif != 0 ? baseNodeDif : adjNodeDif;
            }
        };
        Collections.sort(expectedUpwardsEdges, edgeComparator);
        Collections.sort(actualUpwardsEdges, edgeComparator);

        System.out.println(expectedUpwardsEdges);
        System.out.println(actualUpwardsEdges);

        assertEquals(expectedUpwardsEdges.size(), actualUpwardsEdges.size());
        assertTrue(actualUpwardsEdges.containsAll(expectedUpwardsEdges));
    }

    private List<EdgeIteratorState> getUpwardsEdges(TargetSetReverseUpwardPathsExplorer targetExplorer) {
        final EdgeFilter chFilter = new CHUpwardsEdgeFilter(targetExplorer);

        final LinkedList<EdgeIteratorState> upwardsEdges = new LinkedList<>();
        final Stack<Integer> nodesToExplore = getStartingNodeSet();

        exploreUpwardsEdges(chFilter, upwardsEdges, nodesToExplore);

        pruneDuplicates(upwardsEdges);

        return upwardsEdges;
    }

    private Stack<Integer> getStartingNodeSet() {
        final Stack<Integer> nodesToExplore = new Stack<>();
//        nodesToExplore.push(6);
//        nodesToExplore.push(25);
//        nodesToExplore.push(20);
        nodesToExplore.push(0);
        return nodesToExplore;
    }

    private void exploreUpwardsEdges(EdgeFilter chFilter, LinkedList<EdgeIteratorState> upwardsEdges, Stack<Integer> nodesToExplore) {
        while (!nodesToExplore.isEmpty()) {
            final int currentNode = nodesToExplore.pop();

            final EdgeIterator neighborExplorer = GRAPH_MOCKER.graph.createEdgeExplorer(chFilter).setBaseNode(currentNode);
            while (neighborExplorer.next()) {
                int adjNode = neighborExplorer.getAdjNode();
                upwardsEdges.add(neighborExplorer.detach(true));
                nodesToExplore.push(adjNode);
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

    private TargetSetReverseUpwardPathsExplorer getTargetExplorerInstance() {
        CHGraph chGraph = GRAPH_MOCKER.graphWithCh.getCHGraph();
        LinkedHashSet<Integer> targets = new LinkedHashSet<>(Arrays.asList(/*6, 25, 20, */0));
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

//    @Test
    public void visualizeGraph() {
        GraphHopperStorage graph = GRAPH_MOCKER.graphWithCh;
        NodesAndNeighborDump dump = new NodesAndNeighborDump(graph, getAllNodes(graph));
        dump.dump();
        SwingGraphGUI swingGraphGUI = new SwingGraphGUI(dump.getNodes(), dump.getEdges());
        swingGraphGUI.visualizeGraph();
        try {
//            Thread.sleep(60000);
        } catch (Exception e) {

        }
    }
}
