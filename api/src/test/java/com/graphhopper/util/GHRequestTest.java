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
package com.graphhopper.util;

import com.graphhopper.GHRequest;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Karich
 */
public class GHRequestTest {
    private static double LAT0 = 51, LON0 = 1, LAT1 = 52, LON1 = 2, LAT2 = 53, LON2 = 3;
    private ArrayList<GHPoint> points;
    private List<Double> favoredHeadings;
    private List<Double> emptyHeadings;


    @Before

    public void setupPointsAndHeadings() {
        points = createTestPoints(LAT0, LON0, LAT1, LON1, LAT2, LON2);
        favoredHeadings = Arrays.asList(3.14, 4.15, Double.NaN);
        emptyHeadings = Arrays.asList(Double.NaN, Double.NaN, Double.NaN);
    }

    @Test
    public void testGetHint() {
        GHRequest instance = new GHRequest(10, 12, 12, 10);
        instance.getHints().put("something", "1");
        assertEquals(1, instance.getHints().getInt("something", 2));
        // #173 - will throw an error: Integer cannot be cast to Double
        assertEquals(1, instance.getHints().getDouble("something", 2d), 1e1);
    }

    @Test
    public void testCorrectInit() {
        correctInitWithHeadings();
        correctInitWithoutHeadings();
    }

    private void correctInitWithoutHeadings() {
        correctInitWithAllPointsButNoHeadings();
        correctInitWithFirstTwoPointsAndNoHeadings();
        correctInitWithFirstTwoLatLongButNoHeadings();
        correctInitWithoutConstructorAndNoHeadingsAndSizeHint();
    }

    private void correctInitWithHeadings() {
        correctInitWithAllPointsAndHeadings();
        correctInitWithFirstTwoPointsAndHeadings();
        correctInitWithFirstTwoLatLongAndHeadings();
        correctInitWithoutConstructorWithAllPointsAndHeadingsWithSizeHint();
        correctInitWithoutConstructorWithAllPointsAndHeadingsWithoutSizeHint();
    }

    private void correctInitWithoutConstructorAndNoHeadingsAndSizeHint() {
        GHRequest instance = new GHRequest().addPoint(points.get(0)).addPoint(points.get(1)).addPoint(points.get(2));
        assertEquals("Points not initialized correct", points, instance.getPoints());
        compareFavoredHeadings(instance, emptyHeadings);
    }

    private void correctInitWithFirstTwoLatLongButNoHeadings() {
        GHRequest instance = new GHRequest(LAT0, LON0, LAT1, LON1);
        assertEquals("Points not initialized correct", points.subList(0, 2), instance.getPoints());
        compareFavoredHeadings(instance, emptyHeadings.subList(0, 2));
    }

    private void correctInitWithFirstTwoPointsAndNoHeadings() {
        GHRequest instance = new GHRequest(points.get(0), points.get(1));
        assertEquals("Points not initialized correct", points.subList(0, 2), instance.getPoints());
        compareFavoredHeadings(instance, emptyHeadings.subList(0, 2));
    }

    private void correctInitWithAllPointsButNoHeadings() {
        GHRequest instance = new GHRequest(points);
        assertEquals("Points not initialized correct", points, instance.getPoints());
        compareFavoredHeadings(instance, emptyHeadings);
    }

    private void correctInitWithoutConstructorWithAllPointsAndHeadingsWithoutSizeHint() {
        GHRequest instance = new GHRequest().addPoint(points.get(0), favoredHeadings.get(0)).
                addPoint(points.get(1), favoredHeadings.get(1)).
                addPoint(points.get(2), favoredHeadings.get(2));
        assertEquals("Points not initialized correct", points, instance.getPoints());
        compareFavoredHeadings(instance, favoredHeadings);
    }

    private void correctInitWithoutConstructorWithAllPointsAndHeadingsWithSizeHint() {
        GHRequest instance = new GHRequest(3).addPoint(points.get(0), favoredHeadings.get(0)).
                addPoint(points.get(1), favoredHeadings.get(1)).
                addPoint(points.get(2), favoredHeadings.get(2));
        compareFavoredHeadings(instance, favoredHeadings);
        assertEquals("Points not initialized correct", points, instance.getPoints());
    }

    private void correctInitWithFirstTwoLatLongAndHeadings() {
        GHRequest instance = new GHRequest(LAT0, LON0, LAT1, LON1, favoredHeadings.get(0), favoredHeadings.get(1));
        compareFavoredHeadings(instance, favoredHeadings.subList(0, 2));
        assertEquals("Points not initialized correct", points.subList(0, 2), instance.getPoints());
    }

    private void correctInitWithFirstTwoPointsAndHeadings() {
        GHRequest instance = new GHRequest(points.get(0), points.get(1), favoredHeadings.get(0), favoredHeadings.get(1));
        compareFavoredHeadings(instance, favoredHeadings.subList(0, 2));
        assertEquals("Points not initialized correct", points.subList(0, 2), instance.getPoints());
    }

    private void correctInitWithAllPointsAndHeadings() {
        GHRequest instance = new GHRequest(points, favoredHeadings);
        compareFavoredHeadings(instance, favoredHeadings);
        assertEquals("Points not initialized correct", points, instance.getPoints());
    }

    private ArrayList<GHPoint> createTestPoints(double lat0, double lon0, double lat1, double lon1, double lat2, double lon2) {
        ArrayList<GHPoint> points = new ArrayList<>(3);
        points.add(new GHPoint(lat0, lon0));
        points.add(new GHPoint(lat1, lon1));
        points.add(new GHPoint(lat2, lon2));
        return points;
    }

    private void compareFavoredHeadings(GHRequest request, List<Double> expected) {
        for (int ind = 0; ind < expected.size(); ind++) {
            double favoredHeading = request.getFavoredHeading(ind);
            assertEquals(ind + " favored Heading does not match" + expected.get(ind) + " vs ." + favoredHeading,
                    expected.get(ind), favoredHeading, 0.01);
        }

    }
}
