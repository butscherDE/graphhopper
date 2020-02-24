package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

public abstract class SetPathExplorer {
    final CHGraph chGraph;
    final Set<Integer> startSet;
    private final EdgeFilter edgeFilter;
    EdgeFilter nonVisited;
    EdgeFilter chFilter;

    Stack<Integer> nodesToExplore;
    List<EdgeIteratorState> markedEdges;
    final Map<Integer, Boolean> nodesVisited = new HashMap<>();


    public SetPathExplorer(final CHGraph chGraph, Set<Integer> startSet, EdgeFilter edgeFilter) {
        this.chGraph = chGraph;
        this.startSet = startSet;
        this.edgeFilter = edgeFilter;

        this.chGraph.prepareAdjacencyLists();
        this.markedEdges = new LinkedList<>();
        prepareNodesToExplore(startSet);
        addAllStartNodesAsVisited();
    }

    private void prepareNodesToExplore(Set<Integer> startSet) {
        this.nodesToExplore = new Stack<>();
        for (Integer startNode : startSet) {
            this.nodesToExplore.push(startNode);
        }
    }

    private void addAllStartNodesAsVisited() {
        for (Integer startNode : startSet) {
            nodesVisited.put(startNode, true);
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

    private void exploreNeighborhood(Integer node) {
        final Iterator<EdgeIteratorState> neighborExplorer = getIncidentEdgeIterator(node);
        while (neighborExplorer.hasNext()) {
            final EdgeIteratorState incidentEdge = neighborExplorer.next();

            addEdgeBasedOnFilter(incidentEdge);
        }
    }

    abstract Iterator<EdgeIteratorState> getIncidentEdgeIterator(int node);

    private void addEdgeBasedOnFilter(EdgeIteratorState incidentEdge) {
        boolean chAcceptance = chFilter.accept(incidentEdge);
        boolean edgeFilterAcceptance = edgeFilter.accept(incidentEdge);
        if (chAcceptance && edgeFilterAcceptance) {
            markedEdges.add(incidentEdge);

            addNodeToVisitIfNotAlreadyVisited(incidentEdge);
        }
    }

    abstract void addNodeToVisitIfNotAlreadyVisited(EdgeIteratorState incidentEdge);
}
