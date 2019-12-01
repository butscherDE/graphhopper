package com.graphhopper.util.shapes.intersection;

import java.util.List;

public interface SegmentIntersectionAlgorithm {
    boolean isIntersectionPresent();

    int getIntersectionCount();

    List<LineSegment> getIntersections();
}
