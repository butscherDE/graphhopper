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

public abstract class SetPathExplorerTest {
    final static PolygonRoutingTestGraph GRAPH_MOCKER = PolygonRoutingTestGraph.DEFAULT_INSTANCE;

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

    abstract boolean isEdgeAccepted(EdgeFilter edgeFilter, EdgeIteratorState incidentEdge);

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
}
