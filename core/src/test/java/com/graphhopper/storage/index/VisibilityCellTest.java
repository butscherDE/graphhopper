package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.Polygon;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

// TODO Visibility Cell now created by ingoring the first node, change it to have the first nodeId to be the first element of the polygon (cell shape).
public class VisibilityCellTest {
    private static PolygonRoutingTestGraph graphMocker;

    @BeforeClass
    public static void createGraphMocker() {
        graphMocker = new PolygonRoutingTestGraph();
    }

    private VisibilityCell createDefaultVisibilityCell() {
        final List<Integer> visibilityCellNodeIds = Arrays.asList(new Integer[]{17, 15, 18});
        return VisibilityCell.createVisibilityCellFromNodeIDs(visibilityCellNodeIds, graphMocker.nodeAccess);
    }

    @Test
    public void cellShape() {
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();
        final Polygon visibilityCellExpectedCellShape = createDefaultVisibilityCellsExpectedCellShape();

        assertEquals(visibilityCellExpectedCellShape, visibilityCell.cellShape);
    }

    private Polygon createDefaultVisibilityCellsExpectedCellShape() {
        final double[] createExpectedCellShapeLatitudes = new double[] {7, 11, 7};
        final double[] expectedCellShapeLongitudes = new double[] {32, 34, 38};
        return new Polygon(createExpectedCellShapeLatitudes, expectedCellShapeLongitudes);
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

    @Test
    public void equalsOtherVisiblityCell() {
        final VisibilityCell visibilityCell1 = createDefaultVisibilityCell();
        final VisibilityCell visibilityCell2 = createDefaultVisibilityCell();

        assertEquals(visibilityCell1, visibilityCell2);
    }

    @Test
    public void unEqualOtherVisibilityCell() {
        final VisibilityCell visibilityCell1 = createDefaultVisibilityCell();
        final VisibilityCell visibilityCell2 = createVisibilityCellOtherThanDefaultVisibilityCell();

        assertNotEquals(visibilityCell1, visibilityCell2);
    }

    private VisibilityCell createVisibilityCellOtherThanDefaultVisibilityCell() {
        final List<Integer> visibilityCell2NodeIDs = Arrays.asList(new Integer[] {34, 15, 17});
        return VisibilityCell.createVisibilityCellFromNodeIDs(visibilityCell2NodeIDs, graphMocker.nodeAccess);
    }

    @Test
    public void equalWithCellShape() {
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();
        final Polygon cellShape = createDefaultVisibilityCellsExpectedCellShape();

        assertEquals(visibilityCell, cellShape);
    }

    @Test
    public void unequalWithCellShape() {
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();
        final Polygon otherCellShape = createVisibilityCellOtherThanDefaultVisibilityCell().cellShape;

        assertNotEquals(visibilityCell, otherCellShape);
    }

    @Test
    public void containsInner() {
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();
        final double internalLatitude = 8;
        final double internalLongitude = 35;

        assertTrue(visibilityCell.contains(internalLatitude, internalLongitude));
    }

    @Test
    public void containsMinimallyInner() {
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();
        final double internalLatitude = 7 + Double.MIN_VALUE;
        final double internalLongitude = 35;

        assertTrue(visibilityCell.contains(internalLatitude, internalLongitude));
    }

    @Test
    public void notContainsOutside() {
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();
        final double outerLatitude = 6;
        final double innerLongitude = 35;

        assertFalse(visibilityCell.contains(outerLatitude, innerLongitude));
    }

    @Test
    public void notContainsMinimallyOutside() {
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();
        final double outerLatitude = 6.999999999;
        final double innerLongitude = 35;

        assertFalse(visibilityCell.contains(outerLatitude, innerLongitude));
    }

    @Test
    public void containsLine() {
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();

        assertTrue(visibilityCell.contains(7, 35));
    }

    @Test
    public void containsCorner() {
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();

        assertTrue(visibilityCell.contains(7, 32));
    }

    @Test
    public void getMinimalBoundingBox() {
        final VisibilityCell visibilityCell = createDefaultVisibilityCell();
        final BBox boundingBox = new BBox(32, 38, 7, 11);

        assertEquals(boundingBox, visibilityCell.getMinimalBoundingBox());
    }
}
