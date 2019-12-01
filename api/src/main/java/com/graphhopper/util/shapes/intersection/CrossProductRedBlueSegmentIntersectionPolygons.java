package com.graphhopper.util.shapes.intersection;

import com.graphhopper.util.shapes.Polygon;
import org.locationtech.jts.geom.LineSegment;

import java.util.List;

public class CrossProductRedBlueSegmentIntersectionPolygons extends CrossProductRedBlueSegmentIntersection {
    public CrossProductRedBlueSegmentIntersectionPolygons(final Polygon redPolygon, final Polygon bluePolygon) {
        redSegments.addAll(extractLineSegments(redPolygon));
        blueSegments.addAll(extractLineSegments(bluePolygon));
    }

    private static List<LineSegment> extractLineSegments(final Polygon polygon) {
        return polygon.getAsLineSegments();
    }
}
