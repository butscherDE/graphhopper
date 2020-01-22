package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;

public class VectorAngleCalculatorRight extends VectorAngleCalculator{
    public VectorAngleCalculatorRight(NodeAccess nodeAccess) {
        super(nodeAccess);
    }

    @Override
    public double getAngleOfVectorsOriented(final int baseNode, final int adjNode) {
        final double angle = getAngle(baseNode, adjNode);
        return angle == 0 || angle == ANGLE_WHEN_COORDINATES_ARE_EQUAL ? angle : angle * (-1) + 2 * Math.PI;
    }
}
