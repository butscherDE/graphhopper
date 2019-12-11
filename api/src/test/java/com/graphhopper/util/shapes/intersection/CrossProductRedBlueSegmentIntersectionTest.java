package com.graphhopper.util.shapes.intersection;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class CrossProductRedBlueSegmentIntersectionTest {
    public final List<LineSegment> redLineSegments = new ArrayList<>(11);
    public final List<LineSegment> blueLineSegments = new ArrayList<>(12);
    private CrossProductRedBlueSegmentIntersection intersections;

    public CrossProductRedBlueSegmentIntersectionTest() {
        buildIntersections();
        intersections = new CrossProductRedBlueSegmentIntersection(this.redLineSegments, this.blueLineSegments);
    }

    public void setIntersectionAlgorithm(final CrossProductRedBlueSegmentIntersection intersections) {
        this.intersections = intersections;
    }

    private void buildIntersections() {
        this.setupRedLineSegments();
        this.setupBlueLineSegments();
    }

    private void setupRedLineSegments() {
        this.redLineSegments.add(new LineSegment(5, -3, 7, -1));
        this.redLineSegments.add(new LineSegment(13, -1, 15, -1));
        this.redLineSegments.add(new LineSegment(13, -3, 15, -3));
        this.redLineSegments.add(new LineSegment(1, -6, 2, -5));
        this.redLineSegments.add(new LineSegment(2, -7, 3, -6));
        this.redLineSegments.add(new LineSegment(5, -5, 7, -7));
        this.redLineSegments.add(new LineSegment(9, -7, 10, -6));
        this.redLineSegments.add(new LineSegment(10, -7, 11, -6));
        this.redLineSegments.add(new LineSegment(10, -8, 12, -6));
        this.redLineSegments.add(new LineSegment(13, -5, 15, -5));
        this.redLineSegments.add(new LineSegment(13, -7, 15, -7));
    }

    private void setupBlueLineSegments() {
        this.blueLineSegments.add(new LineSegment(1, -1, 3, -3));
        this.blueLineSegments.add(new LineSegment(1, -3, 3, -1));
        this.blueLineSegments.add(new LineSegment(5, -1, 7, -3));
        this.blueLineSegments.add(new LineSegment(9, -1, 9, -3));
        this.blueLineSegments.add(new LineSegment(11, -1, 11, -3));
        this.blueLineSegments.add(new LineSegment(1, -5, 3, -7));
        this.blueLineSegments.add(new LineSegment(5, -6, 6, -5));
        this.blueLineSegments.add(new LineSegment(6, -7, 7, -6));
        this.blueLineSegments.add(new LineSegment(9, -6, 11, -8));
        this.blueLineSegments.add(new LineSegment(10, -5, 12, -7));
        this.blueLineSegments.add(new LineSegment(13, -5, 13, -7));
        this.blueLineSegments.add(new LineSegment(15, -5, 15, -7));
    }

    @Test
    public void intersectionsFound() {
        assertTrue(this.intersections.isIntersectionPresent());
    }

    @Test
    public void correctNumberOfIntersections() {
        assertEquals(14, this.intersections.getIntersectionCount());
    }

    @Test
    public void correctIntersectionsFound() {
        final List<Coordinate> groundTruth = new ArrayList<>(14);
        groundTruth.add(new Coordinate(6, -2));
        groundTruth.add(new Coordinate(1.5, -5.5));
        groundTruth.add(new Coordinate(2.5, -6.5));
        groundTruth.add(new Coordinate(5.5, -5.5));
        groundTruth.add(new Coordinate(6.5, -6.5));
        groundTruth.add(new Coordinate(9.5, -6.5));
        groundTruth.add(new Coordinate(10, -7));
        groundTruth.add(new Coordinate(11, -6));
        groundTruth.add(new Coordinate(10.5, -7.5));
        groundTruth.add(new Coordinate(11.5, -6.5));
        groundTruth.add(new Coordinate(13,-5));
        groundTruth.add(new Coordinate(15, -5));
        groundTruth.add(new Coordinate(13, -7));
        groundTruth.add(new Coordinate(15, -7));

        assertEquals(groundTruth, this.intersections.getIntersections());
    }
}
