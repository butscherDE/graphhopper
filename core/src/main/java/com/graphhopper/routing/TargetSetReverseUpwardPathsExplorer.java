package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

public class TargetSetReverseUpwardPathsExplorer {
    private final CHGraph graph;
    private final Set<Integer> targets;
    private LinkedHashSet<Integer> nodesToExplore;
    private LinkedHashSet<Integer> nodesFoundToExploreNext;
    private final Map<Integer, Boolean> nodesVisited = new HashMap<>();
    private final OnlyNonVisitedNeighborsEdgeFilter nonVisited = new OnlyNonVisitedNeighborsEdgeFilter(nodesVisited);
    private final CHDownwardsEdgeFilter chDownwardsEdgeFilter = new CHDownwardsEdgeFilter();
    private List<EdgeIteratorState> markedEdges;


    public TargetSetReverseUpwardPathsExplorer(CHGraph graph, Set<Integer> targets) {
        this.graph = graph;
        this.graph.prepareAdjacencyLists();
        this.targets = targets;
    }

    public List<EdgeIteratorState> getMarkedEdges() {
        prepareMarkedEdgeData();

        return markedEdges;
    }

    private void prepareMarkedEdgeData() {
        markedEdges = new LinkedList<>();
        nodesToExplore = new LinkedHashSet<>(targets);
        for (Integer target : targets) {
            nodesVisited.put(target, true);
        }

        while (nodesToExplore.size() > 0) {
            nodesFoundToExploreNext = new LinkedHashSet<>();

            for (Integer node : nodesToExplore) {
                exploreNeighborhood(node);
            }

            nodesToExplore = nodesFoundToExploreNext;
        }
    }

    private void exploreNeighborhood(Integer node) {
        final Iterator<EdgeIteratorState> neighborExplorer = graph.getIngoingEdges(node);
        while (neighborExplorer.hasNext()) {
            final EdgeIteratorState incidentEdge = neighborExplorer.next();

            addEdgeIfDownwards(incidentEdge);
        }
    }

    private void addEdgeIfDownwards(EdgeIteratorState incidentEdge) {
        if (chDownwardsEdgeFilter.accept(incidentEdge)) {
            markedEdges.add(incidentEdge);

            addBaseNodeToVisitTaskIfNotAlreadyVisited(incidentEdge);
        }
    }

    private void addBaseNodeToVisitTaskIfNotAlreadyVisited(EdgeIteratorState incidentEdge) {
        if (nonVisited.accept(incidentEdge)) {
            int baseNode = incidentEdge.getBaseNode();
            nodesFoundToExploreNext.add(baseNode);
            nodesVisited.put(baseNode, true);
        }
    }

    class OnlyNonVisitedNeighborsEdgeFilter implements EdgeFilter {
        private final Map<Integer, Boolean> nodesVisited;

        OnlyNonVisitedNeighborsEdgeFilter(Map<Integer, Boolean> nodesVisited) {
            this.nodesVisited = nodesVisited;
        }

        @Override
        public boolean accept(EdgeIteratorState edgeState) {
            int baseNode = edgeState.getBaseNode();
            Boolean visited = nodesVisited.get(baseNode);
            return visited == null || visited == false;
        }
    }

    class CHDownwardsEdgeFilter implements EdgeFilter {
        @Override
        public boolean accept(EdgeIteratorState edgeState) {
            final int baseNode = edgeState.getBaseNode();
            final int adjNode = edgeState.getAdjNode();

            return compareNodesRanks(baseNode, adjNode);
        }

        private boolean compareNodesRanks(int baseNode, int adjNode) {
            final int baseRank = graph.getLevel(baseNode);
            final int adjRank = graph.getLevel(adjNode);

            return baseRank > adjRank;
        }
    }
}
