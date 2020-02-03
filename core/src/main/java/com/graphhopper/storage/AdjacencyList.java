package com.graphhopper.storage;

import com.graphhopper.util.EdgeIteratorState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AdjacencyList {
    Map<Integer, List<EdgeIteratorState>> adjacency = new HashMap<>();

    public List<EdgeIteratorState> getNeighbors(final int node) {
        return adjacency.get(node);
    }

    public Iterator<EdgeIteratorState> getIterator(final int node) {
        return getNeighbors(node).iterator();
    }
}
