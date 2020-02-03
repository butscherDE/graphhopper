package com.graphhopper.storage;

import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.ArrayList;
import java.util.List;

public class AdjacencyListOut extends AdjacencyList {
    public AdjacencyListOut(final EdgeIterator edgeIterator) {
        super();
        createFrom(edgeIterator);
    }

    private void createFrom(final EdgeIterator edgeIterator) {
        while (edgeIterator.next()) {
            final int baseNode = edgeIterator.getBaseNode();

            addAdjacencyListIfNotPresent(baseNode);
            addEdgeToAdjacency(edgeIterator, baseNode);
        }
    }

    private void addAdjacencyListIfNotPresent(int baseNode) {
        if (adjacency.get(baseNode) == null) {
            adjacency.put(baseNode, new ArrayList<>());
        }
    }

    private void addEdgeToAdjacency(EdgeIterator edgeIterator, int baseNode) {
        final List<EdgeIteratorState> baseNodesAdjacency = adjacency.get(baseNode);
        baseNodesAdjacency.add(edgeIterator.detach(false));
    }
}
