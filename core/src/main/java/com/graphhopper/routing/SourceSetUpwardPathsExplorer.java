package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SourceSetUpwardPathsExplorer extends SetPathExplorer {
    public SourceSetUpwardPathsExplorer(CHGraph chGraph, Set<Integer> sources, EdgeFilter edgeFilter) {
        super(chGraph, sources, edgeFilter);
        nonVisited = new OnlyNonVisitedNeighborsEdgeFilter(nodesVisited);
        chFilter = new CHUpwardsEdgeFilter();
    }

    @Override
    Iterator<EdgeIteratorState> getIncidentEdgeIterator(int node) {
        return chGraph.getOutgoingEdges(node);
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

    class CHUpwardsEdgeFilter implements EdgeFilter {
        @Override
        public boolean accept(EdgeIteratorState edgeState) {
            final int baseNode = edgeState.getBaseNode();
            final int adjNode = edgeState.getAdjNode();

            return compareNodesRanks(baseNode, adjNode);
        }

        private boolean compareNodesRanks(int baseNode, int adjNode) {
            final int baseRank = chGraph.getLevel(baseNode);
            final int adjRank = chGraph.getLevel(adjNode);

            return baseRank <= adjRank;
        }
    }

    void addNodeToVisitIfNotAlreadyVisited(EdgeIteratorState incidentEdge) {
        if (nonVisited.accept(incidentEdge)) {
            int adjNode = incidentEdge.getAdjNode();
            nodesToExplore.add(adjNode);
            nodesVisited.put(adjNode, true);
        }
    }
}
