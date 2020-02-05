package com.graphhopper.storage;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

public class AdjacencyListIn extends AdjacencyList {
    public AdjacencyListIn(final Graph graph, final EdgeIterator edgeIterator, final Weighting weighting) {
        super(graph, edgeIterator, weighting);
    }

    @Override
    int getNodeToAddAdjacencyTo(EdgeIteratorState edge) {
        return edge.getAdjNode();
    }
}
