package com.graphhopper.util.shapes.intersection;

public class LineSegment {
    final double[] latitudes;
    final double[] longitudes;

    private LineSegment(final double[] latitudes, final double[] longitudes) {
        this.latitudes = latitudes;
        this.longitudes = longitudes;
    }

    public static LineSegment createOrderedByLongitude(final double[] latitudes, final double[] longitudes) {
        if (isOrderedByLongitude(longitudes)) {
            return new LineSegment(latitudes, longitudes);
        } else {
            final double[] latitudesReversed = reverseLatitudes(latitudes);
            final double[] longitudesReversed = reverseLongitudes(longitudes);
            return new LineSegment(latitudesReversed, longitudesReversed);
        }
    }

    private static double[] reverseLatitudes(double[] latitudes) {
        return reverseLengthTwoArray(latitudes);
    }

    private static double[] reverseLongitudes(double[] longitudes) {
        return reverseLengthTwoArray(longitudes);
    }

    private static double[] reverseLengthTwoArray(double[] array) {
        return new double[] {array[1], array[0]};
    }

    private static boolean isOrderedByLongitude(final double[] longitudes) {
        if (longitudes.length != 2) {
            throw new IllegalArgumentException("Wrong number of longitudes. Must be exactly 2 for a line segment");
        }

        return longitudes[0] <= longitudes[1];
    }
}
