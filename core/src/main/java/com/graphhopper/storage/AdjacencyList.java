package com.graphhopper.storage;

import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

public abstract class AdjacencyList {
    final Map<Integer, List<EdgeIteratorState>> adjacency = new HashMap<>();
    final BooleanEncodedValue accessEnc;

    public AdjacencyList(final EdgeIterator edgeIterator, final Weighting weighting) {
        this.accessEnc = weighting.getFlagEncoder().getAccessEnc();
        createFrom(edgeIterator);
    }

    private void createFrom(final EdgeIterator edgeIterator) {
        while (edgeIterator.next()) {
            addEdge(edgeIterator);

            if (isBidirectional(edgeIterator)) {
                addReversedEdge(edgeIterator);
            }
        }
    }

    private boolean isBidirectional(EdgeIterator edgeIterator) {
        return edgeIterator.detach(true).get(accessEnc);
    }

    private void addReversedEdge(EdgeIterator edgeIterator) {
        final EdgeIteratorState reverseEdge = edgeIterator.detach(true);
        addEdge(reverseEdge);
    }

    private void addEdge(final EdgeIteratorState edge) {
        final int nodeToAddAdjacencyTo = getNodeToAddAdjacencyTo(edge);

        addAdjacencyListIfNotPresent(nodeToAddAdjacencyTo);
        addEdgeToAdjacency(edge, nodeToAddAdjacencyTo);
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
        return adjacency.get(node);
    }

    public Iterator<EdgeIteratorState> getIterator(final int node) {
        return getNeighbors(node).iterator();
    }

    abstract int getNodeToAddAdjacencyTo(final EdgeIteratorState edge);
}
