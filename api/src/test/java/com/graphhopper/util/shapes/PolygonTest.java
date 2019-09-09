package com.graphhopper.util.shapes;

import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void startUp() {
        this.square = createSquarePolygon();
        this.squareHole = createSquareHolePolygon();
        this.smallSquare = createSmallSquarePolygon();
        this.smallSquareHole = createSmallSquareHolePolygon();
    }

    @Test
    public void testContainsInSquare() {
        assertTrue(this.square.contains(10,10));
        assertTrue(this.square.contains(16,10));
        assertFalse(this.square.contains(10,-20));
        assertTrue(this.square.contains(10,0));
        assertFalse(this.square.contains(10,20));
        assertTrue(this.square.contains(10,16));
        assertFalse(this.square.contains(20,20));
    }

    @Test
    public void testContainsInSquareHole() {
        assertFalse(this.squareHole.contains(10,10));
        assertTrue(this.squareHole.contains(16,10));
        assertFalse(this.squareHole.contains(10,-20));
        assertFalse(this.squareHole.contains(10,0));
        assertFalse(this.squareHole.contains(10,20));
        assertTrue(this.squareHole.contains(10,16));
        assertFalse(this.squareHole.contains(20,20));
    }

    @Test
    public void testContainsInSmallSquare() {
        assertTrue(this.smallSquare.contains(1.5,1.5));
        assertFalse(this.smallSquare.contains(0.5,1.5));
    }

    @Test
    public void testContainsInSmallSquareHole() {
        assertTrue(this.smallSquareHole.contains(1.1,1.1));
        assertFalse(this.smallSquareHole.contains(1.5,1.5));
        assertFalse(this.smallSquareHole.contains(0.5,1.5));
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
        points.add(new GHPoint(0,0));
        points.add(new GHPoint(0,20));
        points.add(new GHPoint(20,20));
        points.add(new GHPoint(20, 0));
        return points;
    }

    /*
     * |----|
     * |    |
     * |----|
     */
    private static Polygon createSquarePolygon() {
        return new Polygon(new double[]{0,0,20,20}, new double[]{0,20,20,0});
    }

    /*
     * \-----|
     *   --| |
     *   --| |
     *  /----|
     */
    private static Polygon createSquareHolePolygon() {
        return new Polygon(new double[]{0,0,20,20,15,15,5,5}, new double[]{0,20,20,0,5,15,15,5});
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
}
