package com.graphhopper.util.shapes.intersection;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.LineSegment;

import java.util.List;

public class CrossProductRedBlueSegmentIntersectionWithoutCollinear extends CrossProductRedBlueSegmentIntersection {
    public CrossProductRedBlueSegmentIntersectionWithoutCollinear(List<LineSegment> redSegments, List<LineSegment> blueSegments) {
        super(redSegments, blueSegments);
    }

    @Override
    boolean intersectionExists(LineSegment redLineSegment, LineSegment blueLineSegment) {
        final boolean isCollinear = isCollinear(redLineSegment, blueLineSegment);
        final boolean isIntersecting = isIntersecting(redLineSegment, blueLineSegment);

        return (!isCollinear) && isIntersecting;
    }

    private boolean isCollinear(LineSegment redLineSegment, LineSegment blueLineSegment) {
        final double orientationIndexP0 = Orientation.index(redLineSegment.p0, redLineSegment.p1, blueLineSegment.p0);
        final double orientationIndexP1 = Orientation.index(redLineSegment.p0, redLineSegment.p1, blueLineSegment.p1);
        return bothPointsOfBlueLineCollinear(orientationIndexP0, orientationIndexP1);
    }

    private boolean bothPointsOfBlueLineCollinear(double orientationIndexP0, double orientationIndexP1) {
        return orientationIndexP0 == Orientation.COLLINEAR && orientationIndexP1 == Orientation.COLLINEAR;
    }

    private boolean isIntersecting(LineSegment redLineSegment, LineSegment blueLineSegment) {
        return redLineSegment.intersection(blueLineSegment) != null;
    }
}
