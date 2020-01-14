package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIteratorState;

public class VectorAngleCalculatorRight extends VectorAngleCalculator{
    public VectorAngleCalculatorRight(NodeAccess nodeAccess) {
        super(nodeAccess);
    }

    @Override
    public double getAngleOfVectorsOriented(EdgeIteratorState candidateEdge) {
        final double angle = getAngle(candidateEdge);
        return angle == 0 || angle == ANGLE_WHEN_COORDINATES_ARE_EQUAL ? angle : angle * (-1) + 2 * Math.PI;
    }
}
