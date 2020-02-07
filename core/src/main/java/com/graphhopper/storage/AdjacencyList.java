package com.graphhopper.storage;

import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.VisitedManager;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

public abstract class AdjacencyList {
    private final Graph graph;
    final Map<Integer, List<EdgeIteratorState>> adjacency = new HashMap<>();
    final BooleanEncodedValue accessEnc;
    final VisitedManager visitedManager;

    public AdjacencyList(Graph graph, final EdgeIterator edgeIterator, final Weighting weighting) {
        this.graph = graph;
        this.accessEnc = weighting.getFlagEncoder().getAccessEnc();
        this.visitedManager = new VisitedManager(graph);
        createFrom(graph.getAllEdges());
//        createFrom(edgeIterator);
    }

    private void createFrom(final EdgeIterator edgeIterator) {
        final Set<Integer> allNodes = new LinkedHashSet<>();
        while(edgeIterator.next()) {
            allNodes.add(edgeIterator.getBaseNode());
            allNodes.add(edgeIterator.getAdjNode());
        }

        for (Integer node : allNodes) {
            final EdgeIterator incidentEdges = graph.createEdgeExplorer().setBaseNode(node);
            while (incidentEdges.next()) {
                addEdge(incidentEdges);
                addReverseEdge(incidentEdges);
            }
        }
    }

    private void addEdge(final EdgeIteratorState edge) {
        if (edge.get(accessEnc) && !visitedManager.isEdgeSettled(edge)) {
            final int nodeToAddAdjacencyTo = getNodeToAddAdjacencyTo(edge);

            addAdjacencyListIfNotPresent(nodeToAddAdjacencyTo);
            addEdgeToAdjacency(edge, nodeToAddAdjacencyTo);
            visitedManager.settleEdge(edge);
        }
    }

    private void addReverseEdge(final EdgeIteratorState edge) {
        final EdgeIteratorState reverseEdge = edge.detach(true);
        if (reverseEdge.get(accessEnc)) {
            addEdge(reverseEdge);
        }
    }

    private void addAdjacencyListIfNotPresent(int nodeToAddAdjacencyTo) {
        if (adjacency.get(nodeToAddAdjacencyTo) == null) {
            adjacency.put(nodeToAddAdjacencyTo, new ArrayList<EdgeIteratorState>());
        }
    }

    private void addEdgeToAdjacency(EdgeIteratorState edgeIterator, int nodeToAddAdjacencyTo) {
        final List<EdgeIteratorState> baseNodesAdjacency = adjacency.get(nodeToAddAdjacencyTo);
        baseNodesAdjacency.add(edgeIterator.detach(false));
    }

    public List<EdgeIteratorState> getNeighbors(final int node) {
        List<EdgeIteratorState> incidentEdges = adjacency.get(node);

        if (incidentEdges != null) {
            return incidentEdges;
        } else {
            ArrayList<EdgeIteratorState> emptyIncidenceList = new ArrayList<>(0);
            return emptyIncidenceList;
        }
    }

    public Iterator<EdgeIteratorState> getIterator(final int node) {
        return getNeighbors(node).iterator();
    }

    abstract int getNodeToAddAdjacencyTo(final EdgeIteratorState edge);
}
