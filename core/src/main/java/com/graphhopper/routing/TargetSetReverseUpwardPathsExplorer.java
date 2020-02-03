package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

public class TargetSetReverseUpwardPathsExplorer {
    private final CHGraph graph;
    private final Set<Integer> targets;
    private LinkedHashSet<Integer> nodesToExplore;
    private LinkedHashSet<Integer> nodesFoundToExploreNext;
    private Map<Integer, Boolean> nodesVisited = new HashMap<>();
    private OnlyNonVisitedNeighborsEdgeFilter backwardsEdgeFilter = new OnlyNonVisitedNeighborsEdgeFilter(nodesVisited);
    private CHDownwardsEdgeFilter downwardsEdgeFilter = new CHDownwardsEdgeFilter();
    private List<EdgeIteratorState> markedEdges;
    private Map<Integer, Boolean> newNodesVisited;


    public TargetSetReverseUpwardPathsExplorer(CHGraph graph, Set<Integer> targets) {
        this.graph = graph;
        this.targets = targets;
    }

    public List<EdgeIteratorState> getMarkedEdges() {
        prepareMarkedEdgeData();

        return markedEdges;
    }

    private void prepareMarkedEdgeData() {
        markedEdges = new LinkedList<>();
        nodesToExplore = new LinkedHashSet<>(targets);

        while (nodesToExplore.size() > 0) {
            nodesFoundToExploreNext = new LinkedHashSet<>();

            newNodesVisited = new HashMap<>();
            for (Integer node : nodesToExplore) {
                exploreNeighborhood(node);
            }

            nodesVisited.putAll(newNodesVisited);
            nodesToExplore = nodesFoundToExploreNext;
        }
    }

    private void exploreNeighborhood(Integer node) {
        newNodesVisited.put(node, true);

        final List<Integer> neighbors = getAllNeighbors(node);

        findEdgesToNodeFromAllNeighbors(node, neighbors);
    }

    private List<Integer> getAllNeighbors(Integer node) {
        final EdgeIterator neighborExplorer = graph.createEdgeExplorer(backwardsEdgeFilter).setBaseNode(node);
        final List<Integer> neighbors = new LinkedList<>();
        while (neighborExplorer.next()) {
            neighbors.add(neighborExplorer.getAdjNode());
        }
        return neighbors;
    }

    private void findEdgesToNodeFromAllNeighbors(Integer node, List<Integer> neighbors) {
        for (Integer neighbor : neighbors) {
            final EdgeFilter downwardsReverseEdgeFilter = new CombinedEdgeFilter(downwardsEdgeFilter, new InEdgeHelper(node));
            final EdgeIterator reverseNeighborExplorer = graph.createEdgeExplorer(downwardsReverseEdgeFilter).setBaseNode(neighbor);
            while (reverseNeighborExplorer.next()) {
                EdgeIteratorState edge = reverseNeighborExplorer.detach(false);
                nodesFoundToExploreNext.add(edge.getBaseNode());
                markedEdges.add(edge);
            }
        }
    }

    class OnlyNonVisitedNeighborsEdgeFilter implements EdgeFilter {
        private final Map<Integer, Boolean> nodesVisited;

        OnlyNonVisitedNeighborsEdgeFilter(Map<Integer, Boolean> nodesVisited) {
            this.nodesVisited = nodesVisited;
        }

        @Override
        public boolean accept(EdgeIteratorState edgeState) {
            int adjNode = edgeState.getAdjNode();
            Boolean visited = nodesVisited.get(adjNode);
            return visited == null || visited == false;
        }
    }

    class CHDownwardsEdgeFilter implements EdgeFilter {
        @Override
        public boolean accept(EdgeIteratorState edgeState) {
            final int baseNode = edgeState.getBaseNode();
            final int adjNode = edgeState.getAdjNode();

            final int baseRank = graph.getLevel(baseNode);
            final int adjRank = graph.getLevel(adjNode);

            return baseRank > adjRank;
        }
    }

    class InEdgeHelper implements EdgeFilter {
        private final int expectedAdjNode;

        InEdgeHelper(int expectedAdjNode) {
            this.expectedAdjNode = expectedAdjNode;
        }


        @Override
        public boolean accept(EdgeIteratorState edgeState) {
            return edgeState.getAdjNode() == expectedAdjNode;
        }
    }

    class CombinedEdgeFilter implements EdgeFilter {
        private final EdgeFilter a;
        private final EdgeFilter b;

        public CombinedEdgeFilter(EdgeFilter a, EdgeFilter b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean accept(EdgeIteratorState edgeState) {
            return a.accept(edgeState) && b.accept(edgeState);
        }
    }
}
