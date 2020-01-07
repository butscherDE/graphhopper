package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CellRunnerLeftTest {
    private final static PolygonRoutingTestGraph GRAPH_MOCKER = new PolygonRoutingTestGraph();

    @Test
    public void simpleCell17to26() {
        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER);
        final CellRunner cr = new CellRunnerLeft(cti.neighborExplorer, cti.nodeAccess, cti.visitedManager, cti.startingEdge);

        final VisibilityCell vc = cr.runAroundCellAndLogNodes();

        final Polygon expectedCellShape = new Polygon(new double[]{7, 3, 7}, new double[]{38, 33, 32});

        assertEquals(expectedCellShape, vc.cellShape);
    }

}
