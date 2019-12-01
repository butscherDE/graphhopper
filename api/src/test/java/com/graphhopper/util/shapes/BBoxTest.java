/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util.shapes;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import org.junit.Test;
import org.locationtech.jts.geom.LineSegment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Peter Karich
 */
public class BBoxTest {
    final BBox mainOverlappingTestBox = new BBox(1,2,1,2);

    @Test
    public void testCreate() {
        DistanceCalc c = new DistanceCalcEarth();
        BBox b = c.createBBox(52, 10, 100000);

        // The calclulated bounding box has no negative values (also for southern hemisphere and negative meridians)
        // and the ordering is always the same (top to bottom and left to right)
        assertEquals(52.8993, b.maxLat, 1e-4);
        assertEquals(8.5393, b.minLon, 1e-4);

        assertEquals(51.1007, b.minLat, 1e-4);
        assertEquals(11.4607, b.maxLon, 1e-4);
    }

    @Test
    public void testContains() {
        assertTrue(new BBox(1, 2, 0, 1).contains(new BBox(1, 2, 0, 1)));
        assertTrue(new BBox(1, 2, 0, 1).contains(new BBox(1.5, 2, 0.5, 1)));
        assertFalse(new BBox(1, 2, 0, 0.5).contains(new BBox(1.5, 2, 0.5, 1)));

        Circle c = new Circle(10, 10, 120000);
        assertTrue(c.getBounds().contains(c));
        assertFalse(new BBox(8.9, 11.09, 8.9, 11.2).contains(c));
    }

    @Test
    public void testGetCenter() {
        BBox bBox = new BBox(0, 2, 0, 2);
        GHPoint center = bBox.getCenter();
        assertEquals(1, center.getLat(), .00001);
        assertEquals(1, center.getLon(), .00001);
    }

    @Test
    public void testIntersect() {
        //    ---
        //    | |
        // ---------
        // |  | |  |
        // --------
        //    |_|
        //

        // use ISO 19115 standard (minLon, maxLon followed by minLat(south!),maxLat)
        assertTrue(new BBox(12, 15, 12, 15).intersects(new BBox(13, 14, 11, 16)));
        // assertFalse(new BBox(15, 12, 12, 15).intersects(new BBox(16, 15, 11, 14)));

        // DOES NOT WORK: use bottom to top coord for lat
        // assertFalse(new BBox(6, 2, 11, 6).intersects(new BBox(5, 3, 12, 5)));
        // so, use bottom-left and top-right corner!
        assertTrue(new BBox(2, 6, 6, 11).intersects(new BBox(3, 5, 5, 12)));

        // DOES NOT WORK: use bottom to top coord for lat and right to left for lon
        // assertFalse(new BBox(6, 11, 11, 6).intersects(new BBox(5, 10, 12, 7)));
        // so, use bottom-right and top-left corner
        assertTrue(new BBox(6, 11, 6, 11).intersects(new BBox(7, 10, 5, 12)));
    }

    @Test
    public void testCalculateIntersection() {

        BBox b1 = new BBox(0, 2, 0, 1);
        BBox b2 = new BBox(-1, 1, -1, 2);
        BBox expected = new BBox(0, 1, 0, 1);

        assertEquals(expected, b1.calculateIntersection(b2));

        //No intersection
        b2 = new BBox(100, 200, 100, 200);
        assertNull(b1.calculateIntersection(b2));

        //Real Example
        b1 = new BBox(8.8591,9.9111,48.3145,48.8518);
        b2 = new BBox(5.8524,17.1483,46.3786,55.0653);

        assertEquals(b1, b1.calculateIntersection(b2));
    }

    @Test
    public void testBasicJavaOverload() {
        new BBox(2, 4, 0, 1) {
            @Override
            public boolean intersects(Circle c) {
                assertTrue(true);
                return super.intersects(c);
            }

            @Override
            public boolean intersects(Shape c) {
                assertTrue(false);
                return true;
            }

            @Override
            public boolean intersects(BBox c) {
                assertTrue(false);
                return true;
            }
        }.intersects(new Circle(1, 2, 3) {
            @Override
            public boolean intersects(Circle c) {
                assertTrue(false);
                return true;
            }

            @Override
            public boolean intersects(Shape b) {
                assertTrue(false);
                return true;
            }

            @Override
            public boolean intersects(BBox b) {
                assertTrue(true);
                return true;
            }
        });
    }

    @Test
    public void testParseTwoPoints() {
        assertEquals(new BBox(2, 4, 1, 3), BBox.parseTwoPoints("1,2,3,4"));
        // stable parsing, i.e. if first point is in north or south it does not matter:
        assertEquals(new BBox(2, 4, 1, 3), BBox.parseTwoPoints("3,2,1,4"));
    }

    @Test
    public void testParseBBoxString() {
        assertEquals(new BBox(2, 4, 1, 3), BBox.parseBBoxString("2,4,1,3"));
    }

    @Test
    public void overlapFalseWhenOtherBoxIsCompletelyLeft() {
        final BBox completelyLeftSameHeight = new BBox(0, 0.5, 1, 2);
        final BBox completelyLeftPartiallyAbove = new BBox(0, 0.5, 1.5, 2.5);
        final BBox completelyLeftBorderAbove = new BBox(0, 0.5, 2, 3);
        final BBox completelyLeftCompletelyAbove = new BBox(0, 0.5, 3, 4);
        final BBox completelyLeftPartiallyBelow = new BBox(0, 0.5, 0.5, 1.5);
        final BBox completelyLeftBorderBelow = new BBox(0, 0.5, 0, 1);
        final BBox completelyLeftCompletelyBelow = new BBox(0, 0.5, -1, 0);

        assertFalse(mainOverlappingTestBox.isOverlapping(completelyLeftSameHeight));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyLeftPartiallyAbove));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyLeftBorderAbove));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyLeftCompletelyAbove));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyLeftPartiallyBelow));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyLeftBorderBelow));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyLeftCompletelyBelow));
    }

    @Test
    public void overlapFalseWhenOtherBoxIsCompletelyRight() {
        final BBox completelyRightSameHeight = new BBox(2.5, 3, 1, 2);
        final BBox completelyRightPartiallyAbove = new BBox(2.5, 3, 1.5, 2.5);
        final BBox completelyRightBorderAbove = new BBox(2.5, 3, 2, 3);
        final BBox completelyRightCompletelyAbove = new BBox(2.5, 3, 3, 4);
        final BBox completelyRightPartiallyBelow = new BBox(2.5, 3, 0.5, 1.5);
        final BBox completelyRightBorderBelow = new BBox(2.5, 3, 0, 1);
        final BBox completelyRightCompletelyBelow = new BBox(2.5, 3, -1, 0);

        assertFalse(mainOverlappingTestBox.isOverlapping(completelyRightSameHeight));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyRightPartiallyAbove));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyRightBorderAbove));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyRightCompletelyAbove));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyRightPartiallyBelow));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyRightBorderBelow));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyRightCompletelyBelow));
    }

    @Test
    public void overlapFalseWhenOtherBoxIsCompletelyBelow() {
        final BBox completelyBelowCenter = new BBox(1, 2, 0, 0.5);
        final BBox completelyBelowPartiallyRight = new BBox(1.5, 2.5, 0, 0.5);
        final BBox completelyBelowBorderRight = new BBox(2, 3, 0, 0.5);
        final BBox completelyBelowCompletelyRight = new BBox(3, 4, 0, 0.5);
        final BBox completelyBelowPartiallyLeft = new BBox(0.5, 1.5, 0, 0.5);
        final BBox completelyBelowBorderLeft = new BBox(0, 1, 0, 0.5);
        final BBox completelyBelowCompletelyLeft = new BBox(-1, 0, 0, 0.5);

        assertFalse(mainOverlappingTestBox.isOverlapping(completelyBelowCenter));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyBelowPartiallyRight));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyBelowBorderRight));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyBelowCompletelyRight));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyBelowPartiallyLeft));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyBelowBorderLeft));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyBelowCompletelyLeft));
    }

    @Test
    public void overlapFalseWhenOtherBoxIsCompletelyAbove() {
        final BBox completelyAboveCenter = new BBox(1, 2, 2.5, 3);
        final BBox completelyAbovePartiallyRight = new BBox(1.5, 2.5, 2.5, 3);
        final BBox completelyAboveBorderRight = new BBox(2, 3, 2.5, 3);
        final BBox completelyAboveCompletelyRight = new BBox(3, 4, 2.5, 3);
        final BBox completelyAbovePartiallyLeft = new BBox(0.5, 1.5, 2.5, 3);
        final BBox completelyAboveBorderLeft = new BBox(0, 1, 2.5, 3);
        final BBox completelyAboveCompletelyLeft = new BBox(-1, 0, 2.5, 3);

        assertFalse(mainOverlappingTestBox.isOverlapping(completelyAboveCenter));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyAbovePartiallyRight));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyAboveBorderRight));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyAboveCompletelyRight));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyAbovePartiallyLeft));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyAboveBorderLeft));
        assertFalse(mainOverlappingTestBox.isOverlapping(completelyAboveCompletelyLeft));
    }

    @Test
    public void overlapTrueBordercasesLeft() {
        final BBox borderLeftLowerCorner = new BBox(0,1, 0,1);
        final BBox borderLeftPartiallyLower = new BBox(0,1,0.5, 1.5);
        final BBox borderLeftCenter = new BBox(0,1,1,2);
        final BBox borderLeftPartiallyAbove = new BBox(0,1,1.5, 2.5);
        final BBox borderLeftUpperCorner = new BBox(0,1,2,3);

        assertTrue(mainOverlappingTestBox.isOverlapping(borderLeftLowerCorner));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderLeftPartiallyLower));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderLeftCenter));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderLeftPartiallyAbove));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderLeftUpperCorner));
    }

    @Test
    public void overlapTrueBordercasesRight() {
        final BBox borderRightLowerCorner = new BBox(2,3, 0,1);
        final BBox borderRightPartiallyLower = new BBox(2,3, 0.5, 1.5);
        final BBox borderRightCenter = new BBox(2,3, 1,2);
        final BBox borderRightPartiallyAbove = new BBox(2,3, 1.5, 2.5);
        final BBox borderRightUpperCorner = new BBox(2,3,2,3);

        assertTrue(mainOverlappingTestBox.isOverlapping(borderRightLowerCorner));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderRightPartiallyLower));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderRightCenter));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderRightPartiallyAbove));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderRightUpperCorner));
    }

    @Test
    public void overlapTrueBordercasesAbove() {
        final BBox borderAboveLeftCorner = new BBox(0,1,2,3);
        final BBox borderAbovePartiallyLeft = new BBox(0.5, 1.5, 2,3);
        final BBox borderAboveCenter = new BBox(1,2,0,1);
        final BBox borderAbovePartiallyRight = new BBox(1.5, 2.5, 2,3);
        final BBox borderAboveRightCorner = new BBox(2,3, 2,3);

        assertTrue(mainOverlappingTestBox.isOverlapping(borderAboveLeftCorner));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderAbovePartiallyLeft));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderAboveCenter));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderAbovePartiallyRight));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderAboveRightCorner));
    }

    @Test
    public void overlapTrueBordercasesBelow() {
        final BBox borderBelowLeftCorner = new BBox(0,1,0,1);
        final BBox borderBelowPartiallyLeft = new BBox(0.5, 1.5, 0,1);
        final BBox borderBelowCenter = new BBox(1,2,0,1);
        final BBox borderBelowPartiallyRight = new BBox(1.5, 2.5, 0,1);
        final BBox borderBelowRightCorner = new BBox(2,3, 0,1);

        assertTrue(mainOverlappingTestBox.isOverlapping(borderBelowLeftCorner));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderBelowPartiallyLeft));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderBelowCenter));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderBelowPartiallyRight));
        assertTrue(mainOverlappingTestBox.isOverlapping(borderBelowRightCorner));
    }

    @Test
    public void overlapTrueAreaShared() {
        final BBox partiallyLeft = new BBox(0.5, 1.5, 1, 2);
        final BBox partiallyRight = new BBox(1.5, 2.5, 1, 2);
        final BBox partiallyAbove = new BBox(1, 2, 1.5, 2.5);
        final BBox partiallyBelow = new BBox(1, 2, 0.5, 1.5);
        final BBox partiallyLeftAbove = new BBox(0.5, 1.5, 1.5, 2.5);
        final BBox partiallyRightAbove = new BBox(1.5, 2.5, 1.5, 2.5);
        final BBox partiallyLeftBelow = new BBox(0.5, 1.5, 0.5, 1.5);
        final BBox partiallyRightBelow = new BBox(1.5, 2.5, 0.5, 1.5);

        assertTrue(mainOverlappingTestBox.isOverlapping(partiallyLeft));
        assertTrue(mainOverlappingTestBox.isOverlapping(partiallyRight));
        assertTrue(mainOverlappingTestBox.isOverlapping(partiallyAbove));
        assertTrue(mainOverlappingTestBox.isOverlapping(partiallyBelow));
        assertTrue(mainOverlappingTestBox.isOverlapping(partiallyLeftAbove));
        assertTrue(mainOverlappingTestBox.isOverlapping(partiallyRightAbove));
        assertTrue(mainOverlappingTestBox.isOverlapping(partiallyLeftBelow));
        assertTrue(mainOverlappingTestBox.isOverlapping(partiallyRightBelow));
    }

    @Test
    public void overlapTrueSameArea() {
        final BBox sameBbox = new BBox(1,2,1,2);

        assertTrue(mainOverlappingTestBox.isOverlapping(sameBbox));
    }

    @Test
    public void lineSegmentRepresentation() {
        final Shape testBox = new BBox(-1, 1, -1, 1);
        final List<LineSegment> testBoxLineSegments = testBox.getLineSegmentRepresentation();

        final List<LineSegment> groundTruth = new ArrayList<>(4);
        groundTruth.add(new LineSegment(-1, -1, 1, -1));
        groundTruth.add(new LineSegment(1, -1, 1, 1));
        groundTruth.add(new LineSegment(1, 1, -1, 1));
        groundTruth.add(new LineSegment(-1, 1, -1, -1));

        assertEquals(groundTruth, testBoxLineSegments);
    }
}
