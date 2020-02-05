package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

public class SourceSetUpwardPathsExplorer {
    private final CHGraph graph;
    private final Set<Integer> sources;
    private Stack<Integer> nodesToExplore;
    private final Map<Integer, Boolean> nodesVisited = new HashMap<>();
    private final OnlyNonVisitedNeighborsEdgeFilter nonVisited = new OnlyNonVisitedNeighborsEdgeFilter(nodesVisited);
    private final CHUpwardsEdgeFilter chUpwardsEdgeFilter = new CHUpwardsEdgeFilter();
    private List<EdgeIteratorState> markedEdges;


    public SourceSetUpwardPathsExplorer(CHGraph graph, Set<Integer> sources) {
        this.graph = graph;
        this.graph.prepareAdjacencyLists();
        this.sources = sources;

        this.markedEdges = new LinkedList<>();
        prepareNodesToExplore(sources);
        addAllTargetsAsVisited();
    }

    private void prepareNodesToExplore(Set<Integer> targets) {
        this.nodesToExplore = new Stack<>();
        for (Integer target : targets) {
            this.nodesToExplore.push(target);
        }
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

    private void addAllTargetsAsVisited() {
        for (Integer target : sources) {
            nodesVisited.put(target, true);
        }
    }

    private void exploreNeighborhood(Integer node) {
        final Iterator<EdgeIteratorState> neighborExplorer = graph.getOutgoingEdges(node);
        while (neighborExplorer.hasNext()) {
            final EdgeIteratorState incidentEdge = neighborExplorer.next();

            addEdgeIfUpwards(incidentEdge);
        }
    }

    private void addEdgeIfUpwards(EdgeIteratorState incidentEdge) {
        if (chUpwardsEdgeFilter.accept(incidentEdge)) {
            markedEdges.add(incidentEdge);

            addBaseNodeToVisitTaskIfNotAlreadyVisited(incidentEdge);
        }
    }

    private void addBaseNodeToVisitTaskIfNotAlreadyVisited(EdgeIteratorState incidentEdge) {
        if (nonVisited.accept(incidentEdge)) {
            int adjNode = incidentEdge.getAdjNode();
            nodesToExplore.add(adjNode);
            nodesVisited.put(adjNode, true);
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

    class CHUpwardsEdgeFilter implements EdgeFilter {
        @Override
        public boolean accept(EdgeIteratorState edgeState) {
            final int baseNode = edgeState.getBaseNode();
            final int adjNode = edgeState.getAdjNode();

            return compareNodesRanks(baseNode, adjNode);
        }

        private boolean compareNodesRanks(int baseNode, int adjNode) {
            final int baseRank = graph.getLevel(baseNode);
            final int adjRank = graph.getLevel(adjNode);

            return baseRank <= adjRank;
        }
    }
}
