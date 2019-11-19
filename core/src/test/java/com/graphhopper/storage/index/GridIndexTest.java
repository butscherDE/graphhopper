package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class GridIndexTest {
    final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();
    LocationIndex locationIndex = new GridIndex(graphMocker.graph, new RAMDirectory()).setResolution(300).prepareIndex();

    @Test
    public void completeTestArea() {
        // Surrounding nodes 14, 18, 100, 101, 102, 103, 104, 105, 106, 107, 108
        final double[] latitudes = new double[]{2, 7, 16, 16, 14, 7, 2};
        final double[] longitudes = new double[]{40, 37, 42, 44, 52, 52, 48};
        final Polygon polygon = new Polygon(latitudes, longitudes, 0);

        final List<GridIndex.VisibilityCell> visibilityCells = ((GridIndex) locationIndex).getOverlappingVisibilityCells(polygon);

        assertEquals(6, visibilityCells.size());
    }
}
