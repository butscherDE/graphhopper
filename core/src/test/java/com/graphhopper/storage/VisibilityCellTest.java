package com.graphhopper.storage;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.storage.index.GridCell;
import com.graphhopper.storage.index.VisibilityCell;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

// TODO Visibility Cell now created by ingoring the first node, change it to have the first nodeId to be the first element of the polygon (cell shape).
public class VisibilityCellTest {
    private final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();

    private VisibilityCell createDefaultVisibilityCell() {
        final List<Integer> visibilityCellNodeIds = Arrays.asList(new Integer[]{17, 15, 18, 17});
        return VisibilityCell.createVisibilityCellFromNodeIDs(visibilityCellNodeIds, graphMocker.nodeAccess);
    }

    @Test
    public void cellShape() {
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();
        final Polygon visibilityCellExpectedCellShape = createDefaultVisibilityCellsExpectedCellShape();

        assertEquals(visibilityCellExpectedCellShape, visibilityCell.cellShape);
    }

    private Polygon createDefaultVisibilityCellsExpectedCellShape() {
        final double[] expectedCellShapeLatitudes = new double[] {11, 7, 7};
        final double[] expectedCellShapeLongitudes = new double[] {34, 38, 32};
        return new Polygon(expectedCellShapeLatitudes, expectedCellShapeLongitudes);
    }

    @Test
    public void surroundingPolygonNotIntersected() {
        final Polygon polygon = createSurrndingPolygon();
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();

        assertFalse(visibilityCell.intersects(polygon));
    }

    private Polygon createSurrndingPolygon() {
        final double[] polygonLatitudes = {6, 12, 12, 6};
        final double[] polygonLongitudes = {31, 31, 39, 39};
        return new Polygon(polygonLatitudes, polygonLongitudes);
    }

    @Test
    public void intersectingPolygonIntersected() {
        final Polygon polygon = createIntersectingPolygon();
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();

        assertTrue(visibilityCell.intersects(polygon));
    }

    private Polygon createIntersectingPolygon() {
        final double[] polygonLatitudes = new double[] {6, 8, 8, 6};
        final double[] polygonLongitudes = new double[] {34, 34, 36, 36};
        return new Polygon(polygonLatitudes, polygonLongitudes);
    }

    @Test
    public void surroundingBoundingBoxOverlapping() {
        final GridCell gridCell = createSurroundingGridCell();
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();

        assertTrue(visibilityCell.isOverlapping(gridCell));
    }

    private GridCell createSurroundingGridCell() {
        final BBox boundingBox = new BBox(31, 39, 6, 12);
        return new GridCell(boundingBox);
    }

    @Test
    public void visibilityCellInternalGridCellIsOverlapping() {
        final GridCell gridCell = createInternalGridCell();
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();

        assertTrue(visibilityCell.isOverlapping(gridCell));
    }

    private GridCell createInternalGridCell() {
        final BBox boundingBox = new BBox(34, 35, 8, 9);
        return new GridCell(boundingBox);
    }
}
