package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.Polygon;

import java.util.List;

public class VisibilityCell {
    public final Polygon cellShape;

    public VisibilityCell(final Polygon polygon) {
        this.cellShape = polygon;
    }

    public static VisibilityCell createVisibilityCellFromNodeIDs(final List<Integer> nodeIds, final NodeAccess nodeAccess) {
        final Polygon cellShape = createCellShape(nodeIds, nodeAccess);

        return new VisibilityCell(cellShape);
    }

    private static Polygon createCellShape(List<Integer> nodeIds, final NodeAccess nodeAccess) {
        final double[] latitudes = new double[nodeIds.size()];
        final double[] longitudes = new double[nodeIds.size()];

        fillLatLonArrayWithCoordinatesOfNodes(nodeIds, nodeAccess, latitudes, longitudes);

        return new Polygon(latitudes, longitudes, 0);
    }

    private static void fillLatLonArrayWithCoordinatesOfNodes(List<Integer> nodeIds, NodeAccess nodeAccess, double[] latitudes, double[] longitudes) {
        int i = 0;
        for (Integer nodeId : nodeIds) {
            latitudes[i] = nodeAccess.getLatitude(nodeId);
            longitudes[i] = nodeAccess.getLongitude(nodeId);
            i++;
        }
    }

    public boolean isOverlapping(final GridCell gridCell) {
        return this.cellShape.isOverlapping(gridCell.boundingBox);
    }

    public boolean intersects(final Polygon polygon) {
        return this.cellShape.intersects(polygon);
    }

    @Override
    public String toString() {
        return cellShape.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VisibilityCell) {
            return returnEqualsExtractingTheOtherPolygon((VisibilityCell) o);
        } else if (o instanceof Polygon) {
            return returnEqualsIfOtherIsAlreadyPolygon(o);
        } else {
            return returnEqualsOnForeignObject(o);
        }
    }

    private boolean returnEqualsExtractingTheOtherPolygon(VisibilityCell o) {
        final VisibilityCell oAsVisibilityCell = o;
        return this.cellShape.equals(oAsVisibilityCell.cellShape);
    }

    private boolean returnEqualsIfOtherIsAlreadyPolygon(Object o) {
        return this.cellShape.equals(o);
    }

    private boolean returnEqualsOnForeignObject(Object o) {
        return super.equals(o);
    }

    public BBox getMinimalBoundingBox() {
        return this.cellShape.getMinimalBoundingBox();
    }

    public boolean contains(final double lat, final double lon) {
        return this.cellShape.contains(lat, lon);
    }
}
