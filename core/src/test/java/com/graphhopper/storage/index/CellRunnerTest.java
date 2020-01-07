package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CellRunnerTest {
    private final static PolygonRoutingTestGraph GRAPH_MOCKER = new PolygonRoutingTestGraph();

    @Test
    public void simpleCell17to26Left() {
        final Polygon expectedCellShape = new Polygon(new double[]{7, 3, 7}, new double[]{38, 33, 32});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 17, 26);
        final CellRunner cr = new CellRunnerLeft(cti.neighborExplorer, cti.nodeAccess, cti.visitedManager, cti.startingEdge);

        final VisibilityCell vc = cr.runAroundCellAndLogNodes();
        assertEquals(expectedCellShape, vc.cellShape);

        visibilityManagerAsserts17to26Left(cti);
    }

    private void visibilityManagerAsserts17to26Left(CellRunnerTestInputs cti) {
        assertWalkedEdgesMarkedAsVisited17to26Left(cti);
        assertExploredButNotWalkedEdgesNotVisited17to26Left(cti);
        assertNoViewedEdgeSettledForRightRun17to26Left(cti);
    }

    private void assertWalkedEdgesMarkedAsVisited17to26Left(CellRunnerTestInputs cti) {
        assertTrue(cti.visitedManager.isEdgeSettledLeft(cti.startingEdge));
        assertTrue(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(26, 18)));
        assertTrue(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(18, 17)));
    }

    private void assertExploredButNotWalkedEdgesNotVisited17to26Left(CellRunnerTestInputs cti) {
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(26, 35)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(18, 14)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(18, 15)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(18, 27)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(18, 100)));
        assertFalse(cti.visitedManager.isEdgeSettledLeft(cti.getEdge(18, 108)));
    }

    private void assertNoViewedEdgeSettledForRightRun17to26Left(CellRunnerTestInputs cti) {
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
    public void simpleCell17to26Right() {
        final Polygon expectedCellShape = new Polygon(new double[]{7, 3, 6}, new double[]{32, 33, 25});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 17, 26);
        final CellRunner cr = new CellRunnerRight(cti.neighborExplorer, cti.nodeAccess, cti.visitedManager, cti.startingEdge);

        final VisibilityCell vc = cr.runAroundCellAndLogNodes();
        assertEquals(expectedCellShape, vc.cellShape);

        visibilityManagerAsserts17to26Right(cti);
    }

    private void visibilityManagerAsserts17to26Right(CellRunnerTestInputs cti) {
        assertWalkedEdgesMarkedAsVisited17to26Right(cti);
        assertExploredButNotWalkedEdgesNotVisited17to26Right(cti);
        assertNoViewedEdgeSettledForRightRun17to26Right(cti);
    }

    private void assertWalkedEdgesMarkedAsVisited17to26Right(CellRunnerTestInputs cti) {
        assertTrue(cti.visitedManager.isEdgeSettledRight(cti.startingEdge));
        assertTrue(cti.visitedManager.isEdgeSettledRight(cti.getEdge(26, 35)));
        assertTrue(cti.visitedManager.isEdgeSettledRight(cti.getEdge(35, 17)));
    }

    private void assertExploredButNotWalkedEdgesNotVisited17to26Right(CellRunnerTestInputs cti) {
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(17, 18)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(17, 15)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(17, 34)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(26, 18)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(35, 25)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(35, 34)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(35, 36)));
        assertFalse(cti.visitedManager.isEdgeSettledRight(cti.getEdge(35, 50)));
    }

    private void assertNoViewedEdgeSettledForRightRun17to26Right(CellRunnerTestInputs cti) {
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

    @Test
    public void duplicateCoordinatesTriangleStartedOnNonDuplicatedCoordinates() {
        final Polygon expectedCellShape = new Polygon(new double[]{15, 10, 11, 11}, new double[]{43, 47, 43, 43});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 14, 106);
        final CellRunner cr = new CellRunnerRight(cti.neighborExplorer, cti.nodeAccess, cti.visitedManager, cti.startingEdge);

        final VisibilityCell vc = cr.runAroundCellAndLogNodes();
        assertEquals(expectedCellShape, vc.cellShape);
    }

    @Test
    public void duplicateCoordinatesTriangleStartedOnAdjNodeHasDuplicate() {
        final Polygon expectedCellShape = new Polygon(new double[] {10, 11, 11, 15}, new double[]{47, 43, 43, 43});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 106, 110);
        final CellRunner cr = new CellRunnerRight(cti.neighborExplorer, cti.nodeAccess, cti.visitedManager, cti.startingEdge);

        final VisibilityCell vc = cr.runAroundCellAndLogNodes();
        assertEquals(expectedCellShape, vc.cellShape);
    }

    @Test
    public void duplicateCoordinatesTriangleStartedOnBothNodesHasDuplicate() {
        final Polygon expectedCellShape = new Polygon(new double[] {15, 10, 11, 11}, new double[]{43, 47, 43, 43});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 109, 110);
        final CellRunner cr = new CellRunnerLeft(cti.neighborExplorer, cti.nodeAccess, cti.visitedManager, cti.startingEdge);

        final VisibilityCell vc = cr.runAroundCellAndLogNodes();
        assertEquals(expectedCellShape, vc.cellShape);
    }

    public static class CellRunnerTestInputs {
        private final PolygonRoutingTestGraph graphMocker;
        public final EdgeExplorer neighborExplorer;
        public final NodeAccess nodeAccess;
        public final VisitedManager visitedManager;
        public final EdgeIteratorState startingEdge;

        public CellRunnerTestInputs(final PolygonRoutingTestGraph graphMocker, final int startBaseNode, final int startAdjNode) {
            this.graphMocker = graphMocker;
            this.neighborExplorer = graphMocker.graph.createEdgeExplorer();
            this.nodeAccess = graphMocker.nodeAccess;
            this.visitedManager = new VisitedManager(graphMocker.graph);
            this.startingEdge = getEdge(startBaseNode, startAdjNode);
        }

        public EdgeIteratorState getEdge(final int startBaseNode, final int startAdjNode) {
            final List<EdgeIteratorState> edges = graphMocker.getAllEdges();

            for (EdgeIteratorState edge : edges) {
                if (edge.getBaseNode() == startBaseNode && edge.getAdjNode() == startAdjNode) {
                    return edge;
                }

                edge = edge.detach(true);
                if (edge.getBaseNode() == startBaseNode && edge.getAdjNode() == startAdjNode) {
                    return edge;
                }
            }

            throw new IllegalArgumentException("Edge doesn't exist.");
        }
    }
}
