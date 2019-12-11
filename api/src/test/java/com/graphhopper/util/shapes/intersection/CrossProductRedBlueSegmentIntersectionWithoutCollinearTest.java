package com.graphhopper.util.shapes.intersection;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.LineSegment;

import java.util.List;

/**
 * Runs the same test as for CrossProductRedBlueSegmentIntersection once with the same set of line segments and once with added collinear intersections. The results shall be the
 * same.
 */
public class CrossProductRedBlueSegmentIntersectionWithoutCollinearTest {
    private final CrossProductRedBlueSegmentIntersectionTest nonCollinearTest = new CrossProductRedBlueSegmentIntersectionTest();
    private List<LineSegment> redLineSegments;
    private List<LineSegment> blueLineSegments;

    @Before
    public void setLineSegmentsAndAlgorithm() {
        redLineSegments = nonCollinearTest.redLineSegments;
        blueLineSegments = nonCollinearTest.blueLineSegments;
        nonCollinearTest.setIntersectionAlgorithm(new CrossProductRedBlueSegmentIntersectionWithoutCollinear(redLineSegments, blueLineSegments));
    }

    @Test
    public void testWithoutCollinearSegments() {
        runAllTest();
    }

    @Test
    public void testWithCollinearSegments() {
        addCollinearIntersections();

        runAllTest();
    }

    private void runAllTest() {
        nonCollinearTest.correctNumberOfIntersections();
        nonCollinearTest.correctIntersectionsFound();
        nonCollinearTest.intersectionsFound();
    }

    private void addCollinearIntersections() {
        redLineSegments.add(new LineSegment(17, -1, 19, -3));
        redLineSegments.add(new LineSegment(19, -3, 21, -5));
        blueLineSegments.add(new LineSegment(17, -1, 19, -3));

        redLineSegments.add(new LineSegment(17, -5, 17, -7));
        redLineSegments.add(new LineSegment(17, -7, 17, -9));
        blueLineSegments.add(new LineSegment(17, -5, 17, -7));
    }
}
