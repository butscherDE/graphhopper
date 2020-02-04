package com.graphhopper.storage;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.ArrayList;
import java.util.List;

public class AdjacencyListIn extends AdjacencyList {
    public AdjacencyListIn(final EdgeIterator edgeIterator, final Weighting weighting) {
        super(edgeIterator, weighting);
    }

    @Override
    int getNodeToAddAdjacencyTo(EdgeIteratorState edge) {
        return edge.getAdjNode();
    }
}
