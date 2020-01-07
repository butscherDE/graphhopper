package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CellRunnerLeftTest {
    private final static PolygonRoutingTestGraph GRAPH_MOCKER = new PolygonRoutingTestGraph();

    @Test
    public void simpleCell17to26() {
        final Polygon expectedCellShape = new Polygon(new double[]{7, 3, 7}, new double[]{38, 33, 32});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 17, 26);
        final CellRunner cr = new CellRunnerLeft(cti.neighborExplorer, cti.nodeAccess, cti.visitedManager, cti.startingEdge);

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
        assertTrue(cti.visitedManager.isEdgeSettledLeft(cti.startingEdge));
        assertTrue(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(26, 18)));
        assertTrue(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(18, 17)));
    }

    private void assertExploredButNotWalkedEdgesNotVisited(CellRunnerTestInputs cti) {
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(26, 35)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(18, 14)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(18, 15)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(18, 27)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(18, 100)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(18, 108)));
    }

    private void assertNoViewedEdgeSettledForRightRun(CellRunnerTestInputs cti) {
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.startingEdge));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(26, 18)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(18, 17)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(26, 35)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(18, 14)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(18, 15)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(18, 27)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(18, 100)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(18, 108)));
    }

    @Test
    public void duplicateCoordinatesTriangleStartedOnNonDuplicatedCoordinates() {
        final Polygon expectedCellShape = new Polygon(new double[]{11, 11, 10, 15}, new double[]{43, 43, 47, 43});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 106, 14);
        final CellRunner cr = new CellRunnerLeft(cti.neighborExplorer, cti.nodeAccess, cti.visitedManager, cti.startingEdge);

        final VisibilityCell vc = cr.runAroundCellAndLogNodes();
        assertEquals(expectedCellShape, vc.cellShape);
    }

}
