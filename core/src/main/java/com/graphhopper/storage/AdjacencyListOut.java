package com.graphhopper.storage;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

public class AdjacencyListOut extends AdjacencyList {
    public AdjacencyListOut(final Graph graph, final EdgeIterator edgeIterator, final Weighting weighting) {
        super(graph, edgeIterator, weighting);
    }

    int getNodeToAddAdjacencyTo(final EdgeIteratorState edge) {
        return edge.getBaseNode();
    }
}
