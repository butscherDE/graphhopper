package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TargetSetReverseUpwardPathsExplorer extends SetPathExplorer {
    public TargetSetReverseUpwardPathsExplorer(CHGraph chGraph, Set<Integer> targets, EdgeFilter edgeFilter) {
        super(chGraph, targets, edgeFilter);
        nonVisited = new OnlyNonVisitedNeighborsEdgeFilter(nodesVisited);
        chFilter = new CHDownwardsEdgeFilter();
    }

    @Override
    Iterator<EdgeIteratorState> getIncidentEdgeIterator(final int node) {
        return chGraph.getIngoingEdges(node);
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

    void addNodeToVisitIfNotAlreadyVisited(EdgeIteratorState incidentEdge) {
        if (nonVisited.accept(incidentEdge)) {
            int baseNode = incidentEdge.getBaseNode();
            nodesToExplore.add(baseNode);
            nodesVisited.put(baseNode, true);
        }
    }
}
