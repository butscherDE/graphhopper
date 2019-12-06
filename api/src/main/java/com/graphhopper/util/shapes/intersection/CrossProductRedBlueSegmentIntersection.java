package com.graphhopper.util.shapes.intersection;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import java.util.ArrayList;
import java.util.List;

public class CrossProductRedBlueSegmentIntersection implements SegmentIntersectionAlgorithm {
     final List<LineSegment> redSegments;
     final List<LineSegment> blueSegments;

    public CrossProductRedBlueSegmentIntersection(final List<LineSegment> redSegments, final List<LineSegment> blueSegments) {
        this.redSegments = redSegments;
        this.blueSegments = blueSegments;
    }

    @Override
    public boolean isIntersectionPresent() {
        for (int i = 0; i < this.redSegments.size(); i++) {
            final LineSegment redLineSegment = redSegments.get(i);

            for (int j = 0; j < this.blueSegments.size(); j++) {
                final LineSegment blueLineSegment = blueSegments.get(j);

                if (intersectionExists(redLineSegment, blueLineSegment)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int getIntersectionCount() {
        int count = 0;

        for (int i = 0; i < this.redSegments.size(); i++) {
            final LineSegment redLineSegment = redSegments.get(i);

            for (int j = 0; j < this.blueSegments.size(); j++) {
                final LineSegment blueLineSegment = blueSegments.get(j);

                if (intersectionExists(redLineSegment, blueLineSegment)) {
                    count++;
                }
            }
        }

        return count;
    }

    @Override
    public List<Coordinate> getIntersections() {
        final List<Coordinate> intersections = new ArrayList<>();

        for (int i = 0; i < this.redSegments.size(); i++) {
            final LineSegment redLineSegment = redSegments.get(i);

            for (int j = 0; j < this.blueSegments.size(); j++) {
                final LineSegment blueLineSegment = blueSegments.get(j);

                final Coordinate intersection = redLineSegment.intersection(blueLineSegment);
                if (intersectionExists(intersection)) {
                    intersections.add(intersection);
                }
            }
        }

        return intersections;
    }

    private boolean intersectionExists(LineSegment redLineSegment, LineSegment blueLineSegment) {
        return redLineSegment.intersection(blueLineSegment) != null;
    }

    private boolean intersectionExists(Coordinate intersection) {
        return intersection != null;
    }
}
