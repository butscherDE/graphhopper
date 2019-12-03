package com.graphhopper.util.shapes;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.locationtech.jts.geom.LineSegment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Robin Boldt
 */
public class PolygonTest {
    private Polygon square;
    private Polygon squareHole;
    private Polygon smallSquare;
    private Polygon smallSquareHole;
    final Polygon intersectionTest = new Polygon(new double[]{0, 2, 2, 0}, new double[]{0, 0, 2, 2});

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void startUp() {
        this.square = createSquarePolygon();
        this.squareHole = createSquareHolePolygon();
        this.smallSquare = createSmallSquarePolygon();
        this.smallSquareHole = createSmallSquareHolePolygon();
    }

    @Test
    public void testContainsInSquare() {
        assertTrue(this.square.contains(10, 10));
        assertTrue(this.square.contains(16, 10));
        assertFalse(this.square.contains(10, -20));
        assertTrue(this.square.contains(10, 0));
        assertFalse(this.square.contains(10, 20));
        assertTrue(this.square.contains(10, 16));
        assertFalse(this.square.contains(20, 20));
    }

    @Test
    public void testContainsInSquareHole() {
        assertFalse(this.squareHole.contains(10, 10));
        assertTrue(this.squareHole.contains(16, 10));
        assertFalse(this.squareHole.contains(10, -20));
        assertFalse(this.squareHole.contains(10, 0));
        assertFalse(this.squareHole.contains(10, 20));
        assertTrue(this.squareHole.contains(10, 16));
        assertFalse(this.squareHole.contains(20, 20));
    }

    @Test
    public void testContainsInSmallSquare() {
        assertTrue(this.smallSquare.contains(1.5, 1.5));
        assertFalse(this.smallSquare.contains(0.5, 1.5));
    }

    @Test
    public void testContainsInSmallSquareHole() {
        assertTrue(this.smallSquareHole.contains(1.1, 1.1));
        assertFalse(this.smallSquareHole.contains(1.5, 1.5));
        assertFalse(this.smallSquareHole.contains(0.5, 1.5));
    }

    @Test
    public void testCorrectMinMaxLatLonSquare() {
        assertEquals(0, this.square.getMinLat(), 0);
        assertEquals(0, this.square.getMinLon(), 0);
        assertEquals(20, this.square.getMaxLat(), 0);
        assertEquals(20, this.square.getMaxLon(), 0);
    }

    @Test
    public void testCorrectMinMaxLatLonSquareHole() {
        assertEquals(0, this.squareHole.getMinLat(), 0);
        assertEquals(0, this.squareHole.getMinLon(), 0);
        assertEquals(20, this.squareHole.getMaxLat(), 0);
        assertEquals(20, this.squareHole.getMaxLon(), 0);
    }

    @Test
    public void testCorrectMinMaxLatLonSmallSquare() {
        assertEquals(1, this.smallSquare.getMinLat(), 0);
        assertEquals(1, this.smallSquare.getMinLon(), 0);
        assertEquals(2, this.smallSquare.getMaxLat(), 0);
        assertEquals(2, this.smallSquare.getMaxLon(), 0);
    }

    @Test
    public void testCorrectMinMaxLatLonSmallSquareHole() {
        assertEquals(1, this.smallSquareHole.getMinLat(), 0);
        assertEquals(1, this.smallSquareHole.getMinLon(), 0);
        assertEquals(2, this.smallSquareHole.getMaxLat(), 0);
        assertEquals(2, this.smallSquareHole.getMaxLon(), 0);
    }

    @Test
    public void testCreateFromGHPoints() {
        List<GHPoint> points = createSquareAsGHPoints();
        final Polygon polygonFromGHPoints = Polygon.createPolygonFromGHPoints(points);

        this.square = polygonFromGHPoints;

        testContainsInSquare();
        testCorrectMinMaxLatLonSquare();
    }

    @Test
    public void testPolygonAsGHPointList() {
        final List<GHPoint> expectedPointsList = createSquareAsGHPoints();
        final List<GHPoint> actualPointsList = this.square.getCoordinatesAsGHPoints();

        for (int i = 0; i < expectedPointsList.size(); i++) {
            assertEquals(expectedPointsList.get(i), actualPointsList.get(i));
        }

        assertEquals(expectedPointsList.size(), actualPointsList.size());
    }

    private static List<GHPoint> createSquareAsGHPoints() {
        List<GHPoint> points = new ArrayList<>();
        points.add(new GHPoint(0, 0));
        points.add(new GHPoint(0, 20));
        points.add(new GHPoint(20, 20));
        points.add(new GHPoint(20, 0));
        return points;
    }

    /*
     * |----|
     * |    |
     * |----|
     */
    private static Polygon createSquarePolygon() {
        return new Polygon(new double[]{0, 0, 20, 20}, new double[]{0, 20, 20, 0});
    }

    /*
     * \-----|
     *   --| |
     *   --| |
     *  /----|
     */
    private static Polygon createSquareHolePolygon() {
        return new Polygon(new double[]{0, 0, 20, 20, 15, 15, 5, 5}, new double[]{0, 20, 20, 0, 5, 15, 15, 5});
    }

    /*
     * |----|
     * |    |
     * |----|
     */
    private static Polygon createSmallSquarePolygon() {
        Polygon square;
        square = new Polygon(new double[]{1, 1, 2, 2}, new double[]{1, 2, 2, 1});
        return square;
    }

    /*
     * |----|
     * | /\ |
     * |/  \|
     */
    private static Polygon createSmallSquareHolePolygon() {
        Polygon squareHole;
        squareHole = new Polygon(new double[]{1, 1, 2, 1.1, 2}, new double[]{1, 2, 2, 1.5, 1});
        return squareHole;
    }

    @Test
    public void lineSegmentRepresentation() {
        final Shape testBox = new Polygon(new double[]{-1, -1, 1, 1}, new double[]{-1, 1, 1, -1}, 0);
        final List<LineSegment> testBoxLineSegments = testBox.getLineSegmentRepresentation();

        final List<LineSegment> groundTruth = new ArrayList<>(4);
        groundTruth.add(new LineSegment(-1, -1, 1, -1));
        groundTruth.add(new LineSegment(1, -1, 1, 1));
        groundTruth.add(new LineSegment(1, 1, -1, 1));
        groundTruth.add(new LineSegment(-1, 1, -1, -1));

        assertEquals(groundTruth, testBoxLineSegments);
    }

    @Test
    public void polygonVSCircleIntersection() {
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Not yet implemented");

        final Circle circle = new Circle(1, 1, 1);

        intersectionTest.intersects(circle);
    }

    @Test
    public void polygonVSUnknownIntersection() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("This shape Implementation is unknown.");

        final Shape unknown = new Shape() {
            @Override
            public boolean intersects(Shape o) {
                return false;
            }

            @Override
            public boolean contains(double lat, double lon) {
                return false;
            }

            @Override
            public boolean contains(Shape s) {
                return false;
            }

            @Override
            public BBox getBounds() {
                return null;
            }

            @Override
            public GHPoint getCenter() {
                return null;
            }

            @Override
            public double calculateArea() {
                return 0;
            }

            @Override
            public List<LineSegment> getLineSegmentRepresentation() {
                return null;
            }
        };

        intersectionTest.intersects(unknown);
    }

    @Test
    public void polygonVSSameBBoxIntersection() {
        /*
         * *----*
         * |    |
         * *----*
         */
        final BBox sameAsPolygon = new BBox(0, 2, 0, 2);
        assertTrue(intersectionTest.intersects(sameAsPolygon));
    }

    @Test
    public void polygonVSBBoxTwoSideIntersection() {
        /*
         * *----*
         * |  *-|--*
         * *--|-*  |
         *    *----*
         */
        final BBox twoSides = new BBox(1, 3, 1, 3);
        assertTrue(intersectionTest.intersects(twoSides));
    }

    @Test
    public void polygonVSBBoxSameSide() {
        /*
         * *----*----*
         * |    |    |
         * *----*----*
         */
        final BBox oneSide = new BBox(2, 4, 0, 2);
        assertTrue(intersectionTest.intersects(oneSide));
    }

    @Test
    public void polygonVSBBoxOnePoint() {
        /*
         *      *----*
         *      |    |
         * *----*----*
         * |    |
         * *----*
         */
        final BBox onePoint = new BBox(2, 4, 2, 4);
        assertTrue(intersectionTest.intersects(onePoint));
    }

    @Test
    public void polygonVSBBoxTooSmall() {
        /*
         * *--------*
         * | *----* |
         * | *----* |
         * *--------*
         */
        final BBox tooSmall = new BBox(0.5, 1.5, 0.5, 1.5);
        assertFalse(intersectionTest.intersects(tooSmall));
    }

    @Test
    public void polygonVSBBoxTooLarge() {
        /*
         * *--------*
         * | *----* |
         * | *----* |
         * *--------*
         */
        final BBox tooLarge = new BBox(-1, 3, -1, -3);
        assertFalse(intersectionTest.intersects(tooLarge));
    }

    @Test
    public void polygonVSBBoxAside() {
        /*
         * *----* *----*
         * |    | |    |
         * *----* *----*
         */
        final BBox aside = new BBox(3, 5, 0, 2);
        assertFalse(intersectionTest.intersects(aside));
    }

    /**
     * Check Region-Aware Route Planning implementation paper for visualization.
     */
    @Test
    public void polygonVSPolygonA() {
        final Polygon triangle = new Polygon(new double[]{0, -2, -2}, new double[]{1, 2, 0});
        final Polygon box = new Polygon(new double[]{0, 0, -2, -2}, new double[]{2.5, 4.5, 4.5, 2.5});

        assertFalse(triangle.intersects(box));
    }

    /**
     * Check Region-Aware Route Planning implementation paper for visualization.
     */
    @Test
    public void polygonVSPolygonB() {
        final Polygon triangle = new Polygon(new double[]{0, -2, -2}, new double[]{1, 2, 0});
        final Polygon box = new Polygon(new double[]{0, 0, -2, -2}, new double[]{0, 2, 2, 0});

        assertTrue(triangle.intersects(box));
    }

    /**
     * Check Region-Aware Route Planning implementation paper for visualization.
     */
    @Test
    public void polygonVSPolygonC() {
        final Polygon triangle = new Polygon(new double[]{0, -2, -2}, new double[]{1, 2, 0});
        final Polygon box = new Polygon(new double[]{-1.2, -1.2, -1.8, -1.8}, new double[]{0.7, 1.3, 1.3, 0.7});

        assertFalse(triangle.intersects(box));
    }

    /**
     * Check Region-Aware Route Planning implementation paper for visualization.
     */
    @Test
    public void polygonVSPolygonD() {
        final Polygon triangle = new Polygon(new double[]{-0.2, -1.8, -1.8}, new double[]{1, 1.8, 0.2});
        final Polygon box = new Polygon(new double[]{0, 0, -2, -2}, new double[]{0, 2, 2, 0});

        assertFalse(triangle.intersects(box));
    }

    /**
     * Check Region-Aware Route Planning implementation paper for visualization.
     */
    @Test
    public void polygonVSPolygonE() {
        final Polygon triangle = new Polygon(new double[]{0, -2, -2}, new double[]{1, 2, 0});
        final Polygon box = new Polygon(new double[]{-1, -1, -3, -3}, new double[]{1, 3, 3, 1});

        assertTrue(triangle.intersects(box));
    }

    /**
     * Check Region-Aware Route Planning implementation paper for visualization.
     */
    @Test
    public void polygonVSPolygonF() {
        final Polygon triangle = new Polygon(new double[]{0, -2, -2}, new double[]{1, 2, 0});
        final Polygon box = new Polygon(new double[]{-2, -2, -4, -4}, new double[]{2, 4, 4, 2});

        assertTrue(triangle.intersects(box));
    }

    /**
     * Check Region-Aware Route Planning implementation paper for visualization.
     */
    @Test
    public void polygonVSPolygonG() {
        final Polygon triangle = new Polygon(new double[]{0, -2, -2}, new double[]{1, 2, 0});
        final Polygon box = new Polygon(new double[]{-1.5, -1.5, -2.5, -2.5}, new double[]{0.5, 1.5, 1.5, 0.5});

        assertTrue(triangle.intersects(box));
    }
}
