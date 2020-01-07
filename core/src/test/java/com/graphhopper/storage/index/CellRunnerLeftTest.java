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

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER);
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
        assertTrue(cti.visitedManager.isEdgeSettledLeft(GRAPH_MOCKER.graph.getEdgeIteratorState(50, 18)));
        assertTrue(cti.visitedManager.isEdgeSettledLeft(GRAPH_MOCKER.graph.getEdgeIteratorState(46, 17)));
    }

    private void assertExploredButNotWalkedEdgesNotVisited(CellRunnerTestInputs cti) {
        assertFalse(cti.visitedManager.isEdgeSettledLeft(GRAPH_MOCKER.graph.getEdgeIteratorState(72, 35)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(GRAPH_MOCKER.graph.getEdgeIteratorState(40, 14)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(GRAPH_MOCKER.graph.getEdgeIteratorState(43, 15)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(GRAPH_MOCKER.graph.getEdgeIteratorState(51, 27)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(GRAPH_MOCKER.graph.getEdgeIteratorState(136, 100)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(GRAPH_MOCKER.graph.getEdgeIteratorState(137, 108)));
    }

    private void assertNoViewedEdgeSettledForRightRun(CellRunnerTestInputs cti) {
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.startingEdge));
        assertFalse(cti.visitedManager.isEdgeSettledRight(GRAPH_MOCKER.graph.getEdgeIteratorState(50, 18)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(GRAPH_MOCKER.graph.getEdgeIteratorState(46, 17)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(GRAPH_MOCKER.graph.getEdgeIteratorState(72, 35)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(GRAPH_MOCKER.graph.getEdgeIteratorState(40, 14)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(GRAPH_MOCKER.graph.getEdgeIteratorState(43, 15)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(GRAPH_MOCKER.graph.getEdgeIteratorState(51, 27)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(GRAPH_MOCKER.graph.getEdgeIteratorState(136, 100)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(GRAPH_MOCKER.graph.getEdgeIteratorState(137, 108)));
    }

}
