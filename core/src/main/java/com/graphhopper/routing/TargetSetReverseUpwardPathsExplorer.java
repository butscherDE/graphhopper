package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TargetSetReverseUpwardPathsExplorer extends SetPathExplorer {
    private final OnlyNonVisitedNeighborsEdgeFilter nonVisited = new OnlyNonVisitedNeighborsEdgeFilter(nodesVisited);
    private final CHDownwardsEdgeFilter chDownwardsEdgeFilter = new CHDownwardsEdgeFilter();


    public TargetSetReverseUpwardPathsExplorer(CHGraph chGraph, Set<Integer> targets) {
        super(chGraph, targets);
    }

    public List<EdgeIteratorState> getMarkedEdges() {
        if (isMarkedEdgesNotPrepared()) {
            prepareMarkedEdgeData();
        }

        return markedEdges;
    }

    private boolean isMarkedEdgesNotPrepared() {
        return markedEdges.size() == 0;
    }

    private void prepareMarkedEdgeData() {
        while (nodesToExplore.size() > 0) {
            final int node = nodesToExplore.pop();
            exploreNeighborhood(node);
        }
    }

    private void exploreNeighborhood(Integer node) {
        final Iterator<EdgeIteratorState> neighborExplorer = chGraph.getIngoingEdges(node);
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
            nodesToExplore.add(baseNode);
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
            final int baseRank = chGraph.getLevel(baseNode);
            final int adjRank = chGraph.getLevel(adjNode);

            return baseRank > adjRank;
        }
    }
}
