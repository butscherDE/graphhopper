package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class GridIndexTest {
    private final static PolygonRoutingTestGraph graphMocker = PolygonRoutingTestGraph.DEFAULT_INSTANCE;
    private final static GridIndex locationIndex = (GridIndex) new GridIndex(graphMocker.graph, new RAMDirectory()).setResolution(300).prepareIndex();
    private static List<VisibilityCell> completeTestAreaCells;

    @BeforeClass
    public static void completeTestArea() {
        // Surrounding nodes 14, 18, 100, 101, 102, 103, 104, 105, 106, 107, 108
        final double[] latitudes = new double[]{  2,  7, 16, 16, 14,  7,  2};
        final double[] longitudes = new double[]{40, 37, 42, 44, 52, 52, 48};
        final Polygon polygon = new Polygon(latitudes, longitudes, 0);

        completeTestAreaCells = locationIndex.getIntersectingVisibilityCells(polygon);
    }

    @Test
    public void assertCompleteTestAreaSize() {
        assertEquals(10, completeTestAreaCells.size());
    }

    @Test
    public void assertCompleteTestAreaPolygon0() {
        final double[] latitudes =
                new double[]{7.0, 1.0, 0.0, 0.0, 2.0, 2.0, 7.0, 3.0, 3.0, 10.0, 10.0, 8.0, 10.0, 12.0, 10.0, 10.0, 15.0, 25.0, 25.0, 25.0, 25.0, 25.0, 25.0, 25.0};
        final double[] longitudes =
                new double[]{1.0, 1.0, 16.0, 21.0, 25.0, 36.0, 38.0, 41.0, 47.0, 47.0, 51.0, 51.0, 51.0, 51.0, 51.0, 47.0, 43.0, 46.0, 43.0, 34.0, 25.0, 16.0, 8.0, 0.0};
        assertPolygonEqual(completeTestAreaCells.get(0), latitudes, longitudes);
    }

    @Test
    public void assertCompleteTestAreaPolygon1() {
        final double[] latitudes =
                new double[]{25.0, 7.0, 1.0, 0.0, 0.0, 2.0, 2.0, 7.0, 3.0, 3.0, 10.0, 10.0, 8.0, 10.0, 12.0, 10.0, 10.0, 15.0, 25.0, 25.0, 25.0, 25.0, 25.0, 25.0};
        final double[] longitudes =
                new double[]{0.0, 1.0, 1.0, 16.0, 21.0, 25.0, 36.0, 38.0, 41.0, 47.0, 47.0, 51.0, 51.0, 51.0, 51.0, 51.0, 47.0, 43.0, 46.0, 43.0, 34.0, 25.0, 16.0, 8.0};
        assertPolygonEqual(completeTestAreaCells.get(1), latitudes, longitudes);
    }

    @Test
    public void assertCompleteTestAreaPolygon2() {
        final double[] latitudes =
                new double[]{15.0, 12.0, 20.0, 25.0};
        final double[] longitudes =
                new double[]{43.0, 38.0, 42.0, 43.0};
        assertPolygonEqual(completeTestAreaCells.get(2), latitudes, longitudes);
    }

    @Test
    public void assertCompleteTestAreaPolygon3() {
        final double[] latitudes =
                new double[]{25.0, 15.0, 12.0, 20.0};
        final double[] longitudes =
                new double[]{43.0, 43.0, 38.0, 42.0};
        assertPolygonEqual(completeTestAreaCells.get(3), latitudes, longitudes);
    }

    @Test
    public void assertCompleteTestAreaPolygon4() {
        final double[] latitudes =
                new double[]{15.0, 7.0, 11.0, 12.0};
        final double[] longitudes =
                new double[]{43.0, 38.0, 34.0, 38.0};
        assertPolygonEqual(completeTestAreaCells.get(4), latitudes, longitudes);
    }

    @Test
    public void assertCompleteTestAreaPolygon5() {
        final double[] latitudes =
                new double[]{7.0, 7.0, 11.0};
        final double[] longitudes =
                new double[]{38.0, 32.0, 34.0};
        assertPolygonEqual(completeTestAreaCells.get(5), latitudes, longitudes);
    }

    @Test
    public void assertCompleteTestAreaPolygon6() {
        final double[] latitudes =
                new double[]{7.0, 7.0, 3.0};
        final double[] longitudes =
                new double[]{32.0, 38.0, 33.0};
        assertPolygonEqual(completeTestAreaCells.get(6), latitudes, longitudes);
    }

    @Test
    public void assertCompleteTestAreaPolygon7() {
        final double[] latitudes =
                new double[]{7.0, 3.0, 7.0};
        final double[] longitudes =
                new double[]{38.0, 33.0, 32.0};
        assertPolygonEqual(completeTestAreaCells.get(7), latitudes, longitudes);
    }

    @Test
    public void assertCompleteTestAreaPolygon8() {
        final double[] latitudes =
                new double[]{2.0, 6.0, 3.0, 7.0, 2.0};
        final double[] longitudes =
                new double[]{25.0, 25.0, 33.0, 38.0, 36.0};
        assertPolygonEqual(completeTestAreaCells.get(8), latitudes, longitudes);
    }

    @Test
    public void assertCompleteTestAreaPolygon9() {
        final double[] latitudes =
                new double[]{7.0, 2.0, 2.0, 6.0, 3.0};
        final double[] longitudes =
                new double[]{38.0, 36.0, 25.0, 25.0, 33.0};
        assertPolygonEqual(completeTestAreaCells.get(9), latitudes, longitudes);
    }

    public void assertPolygonEqual(VisibilityCell visibilityCell, double[] latitudes, double[] longitudes) {
        final Polygon polygon = new Polygon(latitudes, longitudes, 0);
        assertEquals(polygon, visibilityCell.cellShape);
    }
}
