package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

public abstract class SetPathExplorer {
    final CHGraph chGraph;
    final Set<Integer> startSet;
    EdgeFilter nonVisited;
    EdgeFilter chFilter;

    Stack<Integer> nodesToExplore;
    List<EdgeIteratorState> markedEdges;
    final Map<Integer, Boolean> nodesVisited = new HashMap<>();


    public SetPathExplorer(final CHGraph chGraph, Set<Integer> startSet) {
        this.chGraph = chGraph;
        this.startSet = startSet;

        this.chGraph.prepareAdjacencyLists();
        this.markedEdges = new LinkedList<>();
        prepareNodesToExplore(startSet);
        addAllStartNodesAsVisited();
    }

    private void prepareNodesToExplore(Set<Integer> targets) {
        this.nodesToExplore = new Stack<>();
        for (Integer target : targets) {
            this.nodesToExplore.push(target);
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
        if (chFilter.accept(incidentEdge)) {
            markedEdges.add(incidentEdge);

            addNodeToVisitIfNotAlreadyVisited(incidentEdge);
        }
    }

    abstract void addNodeToVisitIfNotAlreadyVisited(EdgeIteratorState incidentEdge);

//    private void addBaseNodeToVisitTaskIfNotAlreadyVisited(EdgeIteratorState incidentEdge) {
//        if (nonVisited.accept(incidentEdge)) {
//            int baseNode = incidentEdge.getBaseNode();
//            nodesToExplore.add(baseNode);
//            nodesVisited.put(baseNode, true);
//        }
//    }
}
