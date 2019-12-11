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

import com.graphhopper.util.shapes.intersection.CrossProductRedBlueSegmentIntersection;
import com.graphhopper.util.shapes.intersection.CrossProductRedBlueSegmentIntersectionWithoutCollinear;
import com.graphhopper.util.shapes.intersection.SegmentIntersectionAlgorithm;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents a polygon that is defined by a set of points.
 * Every point i is connected to point i-1 and i+1.
 * <p>
 * TODO: Howto design inner rings in the polygon?
 *
 * @author Robin Boldt
 */
public class Polygon implements Shape {

    private final double[] lat;
    private final double[] lon;

    private double minLat;
    private double minLon;
    private double maxLat;
    private double maxLon;

    private final double epsilon;

    public Polygon(double[] lat, double[] lon, double growFactor) {
        if (lat.length != lon.length) {
            throw new IllegalArgumentException("Points must be of equal length but was " + lat.length + " vs. " + lon.length);
        }

        this.lat = lat;
        this.lon = lon;

        for (int i = 0; i < lat.length; i++) {
            if (i == 0) {
                minLat = lat[i];
                maxLat = lat[i];
                minLon = lon[i];
                maxLon = lon[i];
            } else {
                if (lat[i] < minLat) {
                    minLat = lat[i];
                } else if (lat[i] > maxLat) {
                    maxLat = lat[i];
                }
                if (lon[i] < minLon) {
                    minLon = lon[i];
                } else if (lon[i] > maxLon) {
                    maxLon = lon[i];
                }
            }
        }

        minLat -= growFactor;
        minLon -= growFactor;
        maxLat += growFactor;
        maxLon += growFactor;

        epsilon = (maxLat - minLat) / 10;
    }

    public Polygon(double[] lat, double[] lon) {
        this(lat, lon, 0);
    }

    public static Polygon createPolygonFromGHPoints(List<GHPoint> points) {
        int pointsLength = points.size();
        double[] lat = new double[pointsLength];
        double[] lon = new double[pointsLength];

        for (int i = 0; i < pointsLength; i++) {
            GHPoint point = points.get(i);

            lat[i] = point.getLat();
            lon[i] = point.getLon();
        }

        return new Polygon(lat, lon);
    }

    /**
     * Lossy conversion to a GraphHopper Polygon.
     */
    public static Polygon create(org.locationtech.jts.geom.Polygon polygon) {
        double[] lats = new double[polygon.getNumPoints()];
        double[] lons = new double[polygon.getNumPoints()];
        for (int i = 0; i < polygon.getNumPoints(); i++) {
            lats[i] = polygon.getCoordinates()[i].y;
            lons[i] = polygon.getCoordinates()[i].x;
        }
        return new Polygon(lats, lons);
    }

    /**
     * Wrapper method for {@link Polygon#contains(double, double)}.
     */
    public boolean contains(GHPoint point) {
        return contains(point.lat, point.lon);
    }

    @Override
    public boolean intersects(Shape o) {
        if (o instanceof Circle) {
            return intersects((Circle) o);
        } else if (o instanceof BBox) {
            return intersects((BBox) o);
        } else if (o instanceof Polygon) {
            return intersects((Polygon) o);
        } else {
            throw new IllegalArgumentException("This shape Implementation is unknown.");
        }
    }

    private boolean intersects(Circle o) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private boolean intersects(BBox o) {
        return checkAllPolygonPointsIfOneLiesInBoundingBox(o);
    }

    private boolean checkAllPolygonPointsIfOneLiesInBoundingBox(BBox o) {
        for (int i = 0; i < this.lat.length; i++) {
            if (checkIfCurrentPolygonPointIsInBoundingBox(o, i)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIfCurrentPolygonPointIsInBoundingBox(BBox o, int i) {
        final double polygonPointLatitude = this.lat[i];
        final double polygonPointLongitude = this.lon[i];

        if (o.contains(polygonPointLatitude, polygonPointLongitude)) {
            return true;
        }
        return false;
    }

    private boolean intersects(Polygon o) {
        final List<LineSegment> thisLineSegments = this.getLineSegmentRepresentation();
        final List<LineSegment> oLineSegments = o.getLineSegmentRepresentation();

        final SegmentIntersectionAlgorithm segmentIntersection = new CrossProductRedBlueSegmentIntersection(thisLineSegments, oLineSegments);
        return segmentIntersection.isIntersectionPresent();
    }

    /**
     * Implements the ray casting algorithm
     * Code is inspired from here: http://stackoverflow.com/a/218081/1548788
     *
     * @param lat Latitude of the point to be checked
     * @param lon Longitude of the point to be checked
     * @return true if point is inside polygon
     */
    public boolean contains(double lat, double lon) {
        if (lat < minLat || lat > maxLat || lon < minLon || lon > maxLon) {
            return false;
        }

        final Coordinate pointToCheck = new Coordinate(lon, lat);
        final List<LineSegment> polygonLineSegments = this.getLineSegmentRepresentation();
        final boolean rayCastInside = isRayCastInside(pointToCheck, polygonLineSegments);
        final boolean latLonOnPolygonBorder = isPointOnPolygonBorder(pointToCheck, polygonLineSegments);

        return rayCastInside || latLonOnPolygonBorder;
    }

    private boolean isRayCastInside(Coordinate pointToCheck, List<LineSegment> polygonLineSegments) {
        final List<LineSegment> rayLineSegments = getRayAsLineSegmentList(pointToCheck);
        final CrossProductRedBlueSegmentIntersection intersections = new CrossProductRedBlueSegmentIntersectionWithoutCollinear(rayLineSegments, polygonLineSegments);

        return intersections.getIntersectionCount() % 2 == 1;
    }

    private List<LineSegment> getRayAsLineSegmentList(Coordinate pointToCheck) {
        final Coordinate rayStart = new Coordinate(minLon - epsilon, maxLat - (minLat / 2));
        final LineSegment ray = new LineSegment(rayStart, pointToCheck);
        return Arrays.asList(new LineSegment[]{ray});
    }

    private boolean isPointOnPolygonBorder(Coordinate pointToCheck, List<LineSegment> polygonLineSegments) {
        boolean latLonOnPolygonBorder = false;
        for (LineSegment polygonLineSegment : polygonLineSegments) {
            latLonOnPolygonBorder |= polygonLineSegment.distance(pointToCheck) == 0.0;
        }
        return latLonOnPolygonBorder;
    }

    public boolean isOverlapping(final BBox boundingBox) {
        boolean isOverlapping = isPolygonWithAtLeastOnePointInsideBoundingBox(boundingBox);

        isOverlapping |= isBoundingBoxCompletelyInsideThisPolygon(boundingBox);

        return isOverlapping;
    }

    private boolean isPolygonWithAtLeastOnePointInsideBoundingBox(BBox boundingBox) {
        boolean isOverlapping = false;

        for (int i = 0; i < this.lat.length && !isOverlapping; i++) {
            isOverlapping |= boundingBox.contains(this.lat[i], this.lon[i]);
        }
        return isOverlapping;
    }

    private boolean isBoundingBoxCompletelyInsideThisPolygon(final BBox boundingBox) {
        return this.contains(boundingBox.minLat, boundingBox.minLon) &&
               this.contains(boundingBox.minLat, boundingBox.maxLon) &&
               this.contains(boundingBox.maxLat, boundingBox.minLon) &&
               this.contains(boundingBox.maxLat, boundingBox.maxLon);
    }

    @Override
    public boolean contains(Shape s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BBox getBounds() {
        return new BBox(minLon, maxLon, minLat, maxLat);
    }

    @Override
    public GHPoint getCenter() {
        return new GHPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2);
    }

    @Override
    public double calculateArea() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public double getMaxLon() {
        return maxLon;
    }

    @Override
    public String toString() {
        String polygon = "";
        polygon += "polygon (" + lat.length + " points)\n";
        polygon += Arrays.toString(this.lat) + "\n";
        polygon += Arrays.toString(this.lon);

        return polygon;
    }

    public static Polygon parsePoints(String pointsStr, double growFactor) {
        String[] arr = pointsStr.split(",");

        if (arr.length % 2 == 1) {
            throw new IllegalArgumentException("incorrect polygon specified");
        }

        double[] lats = new double[arr.length / 2];
        double[] lons = new double[arr.length / 2];

        for (int j = 0; j < arr.length; j++) {
            if (j % 2 == 0) {
                lats[j / 2] = Double.parseDouble(arr[j]);
            } else {
                lons[(j - 1) / 2] = Double.parseDouble(arr[j]);
            }
        }

        return new Polygon(lats, lons, growFactor);
    }

    public List<GHPoint> getCoordinatesAsGHPoints() {
        int numPoints = this.lat.length;
        List<GHPoint> points = new ArrayList<>(numPoints);

        addAllLatLonToPoints(numPoints, points);

        return points;
    }

    private void addAllLatLonToPoints(int numPoints, List<GHPoint> points) {
        for (int i = 0; i < numPoints; i++) {
            final GHPoint point = new GHPoint(lat[i], lon[i]);
            points.add(point);
        }
    }

    public int size() {
        return this.lat.length;
    }

    public boolean isRepresentingArea() {
        return this.size() > 2;
    }

    public BBox getMinimalBoundingBox() {
        double minLongitude = Double.MAX_VALUE;
        double maxLongitude = Double.MIN_VALUE;
        double minLatitude = Double.MAX_VALUE;
        double maxLatitude = Double.MIN_VALUE;

        for (int i = 0; i < this.lat.length; i++) {
            minLongitude = Math.min(minLongitude, this.lon[i]);
            maxLongitude = Math.max(maxLongitude, this.lon[i]);
            minLatitude = Math.min(minLatitude, this.lat[i]);
            maxLatitude = Math.max(maxLatitude, this.lat[i]);
        }

        return new BBox(minLongitude, maxLongitude, minLatitude, maxLatitude);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Polygon) {
            final Polygon oPolygon = (Polygon) o;
            return Arrays.equals(this.lat, oPolygon.lat) && Arrays.equals(this.lon, oPolygon.lon);
        } else {
            return super.equals(o);
        }
    }

    public List<LineSegment> getLineSegmentRepresentation() {
        final List<LineSegment> segments = new ArrayList<>(this.lat.length);

        for (int i = 0; i < this.lat.length - 1; i++) {
            segments.add(new LineSegment(this.lon[i], this.lat[i], this.lon[i + 1], this.lat[i + 1]));
        }
        segments.add(new LineSegment(this.lon[this.lon.length - 1], this.lat[this.lat.length - 1], this.lon[0], this.lat[0]));

        return segments;
    }
}
