package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class VisitedManagerDualTest {
    private static PolygonRoutingTestGraph GRAPH_MOCKER = new PolygonRoutingTestGraph(PolygonRoutingTestGraph.getDefaultNodeList(), PolygonRoutingTestGraph.getDefaultEdgeList());
    private static List<EdgeIteratorState> ALL_EDGES;

    @BeforeClass
    public static void getAllEdges() {
        ALL_EDGES = GRAPH_MOCKER.getAllEdges();
    }

    @Test
    public void isEdgeSettledLeft() {
        final VisitedManagerDual visitedManagerDual = new VisitedManagerDual(GRAPH_MOCKER.graph);

        visitedManagerDual.settleEdgeLeft(ALL_EDGES.get(0));

        assertTrue(visitedManagerDual.isEdgeSettledLeft(ALL_EDGES.get(0)));
    }

    @Test
    public void isReverseEdgeSettledLeft() {
        final VisitedManagerDual visitedManagerDual = new VisitedManagerDual(GRAPH_MOCKER.graph);

        visitedManagerDual.settleEdgeLeft(ALL_EDGES.get(0));

        assertTrue(visitedManagerDual.isEdgeSettledLeft(ALL_EDGES.get(0).detach(true)));
    }

    @Test
    public void isEdgeNotSettledRight() {
        final VisitedManagerDual visitedManagerDual = new VisitedManagerDual(GRAPH_MOCKER.graph);

        visitedManagerDual.settleEdgeLeft(ALL_EDGES.get(0));

        assertFalse(visitedManagerDual.isEdgeSettledRight(ALL_EDGES.get(0)));
    }

    @Test
    public void isReverseEdgeNotSettledRight() {
        final VisitedManagerDual visitedManagerDual = new VisitedManagerDual(GRAPH_MOCKER.graph);

        visitedManagerDual.settleEdgeLeft(ALL_EDGES.get(0));

        assertFalse(visitedManagerDual.isEdgeSettledRight(ALL_EDGES.get(0).detach(true)));
    }

    @Test
    public void isEdgeSettledRight() {
        final VisitedManagerDual visitedManagerDual = new VisitedManagerDual(GRAPH_MOCKER.graph);

        visitedManagerDual.settleEdgeRight(ALL_EDGES.get(0));

        assertTrue(visitedManagerDual.isEdgeSettledRight(ALL_EDGES.get(0)));
    }

    @Test
    public void isReverseEdgeSettledRight() {
        final VisitedManagerDual visitedManagerDual = new VisitedManagerDual(GRAPH_MOCKER.graph);

        visitedManagerDual.settleEdgeRight(ALL_EDGES.get(0));

        assertTrue(visitedManagerDual.isEdgeSettledRight(ALL_EDGES.get(0).detach(true)));
    }

    @Test
    public void isEdgeNotSettledLeft() {
        final VisitedManagerDual visitedManagerDual = new VisitedManagerDual(GRAPH_MOCKER.graph);

        visitedManagerDual.settleEdgeRight(ALL_EDGES.get(0));

        assertFalse(visitedManagerDual.isEdgeSettledLeft(ALL_EDGES.get(0)));
    }

    @Test
    public void isReverseEdgeNotSettledLeft() {
        final VisitedManagerDual visitedManagerDual = new VisitedManagerDual(GRAPH_MOCKER.graph);

        visitedManagerDual.settleEdgeRight(ALL_EDGES.get(0));

        assertFalse(visitedManagerDual.isEdgeSettledLeft(ALL_EDGES.get(0).detach(true)));
    }

    @Test
    public void forceNodeAscendingNotTurned() {
        final EdgeIteratorState forcedEdge = VisitedManager.forceNodeIdsAscending(ALL_EDGES.get(0));
        assertTrue(forcedEdge.getBaseNode() < forcedEdge.getAdjNode());
    }

    @Test
    public void forceNodeAscendingTurned() {
        final EdgeIteratorState forcedEdge = VisitedManager.forceNodeIdsAscending(ALL_EDGES.get(0).detach(true));
        assertTrue(forcedEdge.getBaseNode() < forcedEdge.getAdjNode());
    }
}
