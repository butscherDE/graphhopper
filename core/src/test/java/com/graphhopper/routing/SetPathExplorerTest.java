package com.graphhopper.routing;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.graphvisualizer.NodesAndNeighborDump;
import com.graphhopper.util.graphvisualizer.SwingGraphGUI;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public abstract class SetPathExplorerTest {
    final static PolygonRoutingTestGraph GRAPH_MOCKER = PolygonRoutingTestGraph.DEFAULT_INSTANCE;

    @Test
    public void edgeFilterWorking() {
        final EdgeFilter filterOutAllNode1IncidentEdges = edgeState -> !(edgeState.getBaseNode() == 1 || edgeState.getAdjNode() == 1);

        SetPathExplorer instance = getInstance(filterOutAllNode1IncidentEdges);
        List<EdgeIteratorState> edges = instance.getMarkedEdges();

        for (EdgeIteratorState edge : edges) {
            System.out.println(edge);
            assertNotEquals(1, edge.getBaseNode());
            assertNotEquals(1, edge.getAdjNode());
        }
    }

    @Test
    public void testIfCHCreationWorked() {
        final GraphHopperStorage ghs = GRAPH_MOCKER.graphWithCh;
        final CHGraph chGraph = ghs.getCHGraph();

        final List<Integer> allNodes = getAllNodes(chGraph);
        Collections.sort(allNodes, Comparator.comparingInt(chGraph::getLevel));

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

    void sortFoundEdgesByBaseAndAdjNodeId(List<EdgeIteratorState> expectedUpwardsEdges, List<EdgeIteratorState> actualUpwardsEdges) {
        Comparator<EdgeIteratorState> edgeComparator = (o1, o2) -> {
            int baseNodeDif = o1.getBaseNode() - o2.getBaseNode();
            int adjNodeDif = o1.getAdjNode() - o2.getAdjNode();

            return baseNodeDif != 0 ? baseNodeDif : adjNodeDif;
        };
        Collections.sort(expectedUpwardsEdges, edgeComparator);
        Collections.sort(actualUpwardsEdges, edgeComparator);
    }

    void assertFoundAllUpwardsEdges(List<EdgeIteratorState> expectedUpwardsEdges, List<EdgeIteratorState> actualUpwardsEdges) {
        assertEquals(expectedUpwardsEdges.size(), actualUpwardsEdges.size());

        final Iterator<EdgeIteratorState> expectedIterator = expectedUpwardsEdges.iterator();
        final Iterator<EdgeIteratorState> actualIterator = actualUpwardsEdges.iterator();
        for (int i = 0; i < expectedUpwardsEdges.size(); i++) {
            assertEdgesEqual(expectedIterator.next(), actualIterator.next());
        }
    }

    void assertEdgesEqual(EdgeIteratorState expectedEdge, EdgeIteratorState actualEdge) {
        String message = expectedEdge.toString() + " : " + actualEdge.toString();
        assertEquals(message, expectedEdge.getBaseNode(), actualEdge.getBaseNode());
        assertEquals(message, expectedEdge.getAdjNode(), actualEdge.getAdjNode());
    }

    List<EdgeIteratorState> getUpwardsEdges(EdgeFilter chFilter) {
        final LinkedList<EdgeIteratorState> upwardsEdges = new LinkedList<>();
        final Stack<Integer> nodesToExplore = getStartingNodeSet();

        exploreUpwardsEdges(chFilter, upwardsEdges, nodesToExplore);
        pruneDuplicates(upwardsEdges);

        return upwardsEdges;
    }

    Stack<Integer> getStartingNodeSet() {
        final Stack<Integer> nodesToExplore = new Stack<>();
        nodesToExplore.push(6);
        nodesToExplore.push(25);
        nodesToExplore.push(20);
        nodesToExplore.push(0);
        return nodesToExplore;
    }

    void exploreUpwardsEdges(EdgeFilter chFilter, LinkedList<EdgeIteratorState> upwardsEdges, Stack<Integer> nodesToExplore) {
        final List<Integer> exploredNodes = new LinkedList<>();
        while (!nodesToExplore.isEmpty()) {
            final int currentNode = nodesToExplore.pop();
            exploredNodes.add(currentNode);

            CHGraph graph = GRAPH_MOCKER.graphWithCh.getCHGraph();
            final Iterator<EdgeIteratorState> neighborExplorer = getNeighborExplorer(currentNode, graph);
            while (neighborExplorer.hasNext()) {
                final EdgeIteratorState incidentEdge = neighborExplorer.next();
                if (isEdgeAccepted(chFilter, incidentEdge)) {
                    int node = getExploreNode(incidentEdge);
                    upwardsEdges.add(incidentEdge);
                    if (!exploredNodes.contains(node)) {
                        nodesToExplore.push(node);
                    }
                }
            }
        }
    }

    abstract Iterator<EdgeIteratorState> getNeighborExplorer(int currentNode, CHGraph graph);

    abstract int getExploreNode(EdgeIteratorState incidentEdge);

    boolean isEdgeAccepted(EdgeFilter chFilter, EdgeIteratorState incidentEdge) {
        return chFilter.accept(incidentEdge);
    }

    void pruneDuplicates(LinkedList<EdgeIteratorState> upwardsEdges) {
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

    abstract SetPathExplorer getInstance(EdgeFilter edgeFilter);
}
