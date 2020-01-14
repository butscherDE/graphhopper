package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIteratorState;

public class VectorAngleCalculatorLeft extends VectorAngleCalculator {
    public VectorAngleCalculatorLeft(NodeAccess nodeAccess) {
        super(nodeAccess);
    }

    @Override
    public double getAngleOfVectorsOriented(EdgeIteratorState lastEdgeReversed, EdgeIteratorState candidateEdge) {
        return getAngle(lastEdgeReversed, candidateEdge);
    }
}
