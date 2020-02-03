package com.graphhopper.storage;

import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.ArrayList;
import java.util.List;

public class AdjacencyListIn extends AdjacencyList {
    public AdjacencyListIn(final EdgeIterator edgeIterator) {
        createFrom(edgeIterator);
    }

    private void createFrom(final EdgeIterator edgeIterator) {
        while (edgeIterator.next()) {
            final int adjNode = edgeIterator.getAdjNode();

            addAdjacencyListIfNotPresent(adjNode);
            addEdgeToAdjacency(edgeIterator, adjNode);
        }
    }

    private void addAdjacencyListIfNotPresent(int adjNode) {
        if (adjacency.get(adjNode) == null) {
            adjacency.put(adjNode, new ArrayList<>());
        }
    }

    private void addEdgeToAdjacency(EdgeIterator edgeIterator, int adjNode) {
        final List<EdgeIteratorState> adjNodesAdjacency = adjacency.get(adjNode);
        adjNodesAdjacency.add(edgeIterator.detach(false));
    }


}
