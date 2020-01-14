package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIteratorState;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

public abstract class VectorAngleCalculator {
    final static double ANGLE_WHEN_COORDINATES_ARE_EQUAL = -Double.MAX_VALUE;

    private final NodeAccess nodeAccess;

    public VectorAngleCalculator(NodeAccess nodeAccess) {
        this.nodeAccess = nodeAccess;
    }

    public abstract double getAngleOfVectorsOriented(EdgeIteratorState lastEdgeReversed, final EdgeIteratorState candidateEdge);

    double getAngle(final EdgeIteratorState lastEdgeReversed, final EdgeIteratorState candidateEdge) {
        try {
            return getAngleAfterErrorHandling(lastEdgeReversed, candidateEdge);
        } catch (IllegalArgumentException e) {
            return ANGLE_WHEN_COORDINATES_ARE_EQUAL;
        }
    }

    private double getAngleAfterErrorHandling(final EdgeIteratorState lastEdgeReversed, final EdgeIteratorState candidateEdge) {
        final Vector2D lastEdgeVector = createVectorCorrespondingToEdge(lastEdgeReversed);
        final Vector2D candidateEdgeVector = createVectorCorrespondingToEdge(candidateEdge);

        return getAngle(lastEdgeVector, candidateEdgeVector);
    }

    private double getAngle(Vector2D lastEdgeVector, Vector2D candidateEdgeVector) {
        final double angleTo = lastEdgeVector.angleTo(candidateEdgeVector);
        final double angleToContinuousInterval = transformAngleToContinuousInterval(angleTo);
        final double angleToZeroIfVeryCloseTo2Pi = getAngleAsZeroIfCloseToTwoPi(angleToContinuousInterval);

        return angleToZeroIfVeryCloseTo2Pi;
    }

    private double getAngleAsZeroIfCloseToTwoPi(double angleToContinuousInterval) {
        final double differenceToTwoPi = Math.abs(2 * Math.PI - angleToContinuousInterval);
        return differenceToTwoPi < 0.000000000000001 ? 0 : angleToContinuousInterval;
    }

    private Vector2D createVectorCorrespondingToEdge(EdgeIteratorState candidateEdge) {
        return createVectorByNodeIds(candidateEdge.getBaseNode(), candidateEdge.getAdjNode());
    }

    private Vector2D createVectorByNodeIds(final int baseNode, final int adjNode) {
        final Coordinate lastEdgeBaseNodeCoordinate = new Coordinate(nodeAccess.getLongitude(baseNode), nodeAccess.getLatitude(baseNode));
        final Coordinate lastEdgeAdjNodeCoordinate = new Coordinate(nodeAccess.getLongitude(adjNode), nodeAccess.getLatitude(adjNode));
        if (lastEdgeBaseNodeCoordinate.equals2D(lastEdgeAdjNodeCoordinate)) {
            throw new IllegalArgumentException("Coordinates of both edge end points shall not be equal");
        }
        return new Vector2D(lastEdgeBaseNodeCoordinate, lastEdgeAdjNodeCoordinate);
    }

    private double transformAngleToContinuousInterval(final double angleTo) {
        return angleTo > 0 ? angleTo : angleTo + 2 * Math.PI;
    }
}
