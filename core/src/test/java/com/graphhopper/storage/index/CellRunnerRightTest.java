package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CellRunnerRightTest {
    private final static PolygonRoutingTestGraph GRAPH_MOCKER = new PolygonRoutingTestGraph();

    @Test
    public void simpleCell17to26() {
        final Polygon expectedCellShape = new Polygon(new double[]{3, 6, 7}, new double[]{33, 25, 32});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 17, 26);
        final CellRunner cr = new CellRunnerRight(cti.neighborExplorer, cti.nodeAccess, cti.visitedManager, cti.startingEdge);

        final VisibilityCell vc = cr.runAroundCellAndLogNodes();
        assertEquals(expectedCellShape, vc.cellShape);

        visibilityManagerAsserts(cti);
    }

    private void visibilityManagerAsserts(CellRunnerTestInputs cti) {
        assertWalkedEdgesMarkedAsVisited(cti);
        assertExploredButNotWalkedEdgesNotVisited(cti);
        assertNoViewedEdgeSettledForRightRun(cti);
    }

    private void assertWalkedEdgesMarkedAsVisited(CellRunnerTestInputs cti) {
        assertTrue(cti.visitedManager.isEdgeSettledRight(cti.startingEdge));
        assertTrue(cti.visitedManager.isEdgeSettledRight(cti.getEdge(26, 35)));
        assertTrue(cti.visitedManager.isEdgeSettledRight(cti.getEdge(35, 17)));
    }

    private void assertExploredButNotWalkedEdgesNotVisited(CellRunnerTestInputs cti) {
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(17, 18)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(17, 15)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(17, 34)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(26, 18)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(35, 25)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(35, 34)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(35, 36)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(35, 50)));
    }

    private void assertNoViewedEdgeSettledForRightRun(CellRunnerTestInputs cti) {
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.startingEdge));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(26, 35)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(35, 17)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(17, 18)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(17, 15)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(17, 34)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(26, 18)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(35, 25)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(35, 34)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(35, 36)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(35, 50)));
    }
}
