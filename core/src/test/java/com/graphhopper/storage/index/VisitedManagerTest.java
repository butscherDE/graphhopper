package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.storage.index.VisitedManager;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class VisitedManagerTest {
    private static PolygonRoutingTestGraph GRAPH_MOCKER = new PolygonRoutingTestGraph(PolygonRoutingTestGraph.getDefaultNodeList(), PolygonRoutingTestGraph.getDefaultEdgeList());
    private static List<EdgeIteratorState> ALL_EDGES;

    @BeforeClass
    public static void getAllEdges() {
        ALL_EDGES = GRAPH_MOCKER.getAllEdges();
    }

    @Test
    public void isEdgeSettledLeft() {
        final VisitedManager visitedManager = new VisitedManager(GRAPH_MOCKER.graph);

        visitedManager.settleEdgeLeft(ALL_EDGES.get(0));

        assertTrue(visitedManager.isEdgeSettledLeft(ALL_EDGES.get(0)));
    }

    @Test
    public void isReverseEdgeSettledLeft() {
        final VisitedManager visitedManager = new VisitedManager(GRAPH_MOCKER.graph);

        visitedManager.settleEdgeLeft(ALL_EDGES.get(0));

        assertTrue(visitedManager.isEdgeSettledLeft(ALL_EDGES.get(0).detach(true)));
    }

    @Test
    public void isEdgeNotSettledRight() {
        final VisitedManager visitedManager = new VisitedManager(GRAPH_MOCKER.graph);

        visitedManager.settleEdgeLeft(ALL_EDGES.get(0));

        assertFalse(visitedManager.isEdgeSettledRight(ALL_EDGES.get(0)));
    }

    @Test
    public void isReverseEdgeNotSettledRight() {
        final VisitedManager visitedManager = new VisitedManager(GRAPH_MOCKER.graph);

        visitedManager.settleEdgeLeft(ALL_EDGES.get(0));

        assertFalse(visitedManager.isEdgeSettledRight(ALL_EDGES.get(0).detach(true)));
    }

    @Test
    public void isEdgeSettledRight() {
        final VisitedManager visitedManager = new VisitedManager(GRAPH_MOCKER.graph);

        visitedManager.settleEdgeRight(ALL_EDGES.get(0));

        assertTrue(visitedManager.isEdgeSettledRight(ALL_EDGES.get(0)));
    }

    @Test
    public void isReverseEdgeSettledRight() {
        final VisitedManager visitedManager = new VisitedManager(GRAPH_MOCKER.graph);

        visitedManager.settleEdgeRight(ALL_EDGES.get(0));

        assertTrue(visitedManager.isEdgeSettledRight(ALL_EDGES.get(0).detach(true)));
    }

    @Test
    public void isEdgeNotSettledLeft() {
        final VisitedManager visitedManager = new VisitedManager(GRAPH_MOCKER.graph);

        visitedManager.settleEdgeRight(ALL_EDGES.get(0));

        assertFalse(visitedManager.isEdgeSettledLeft(ALL_EDGES.get(0)));
    }

    @Test
    public void isReverseEdgeNotSettledLeft() {
        final VisitedManager visitedManager = new VisitedManager(GRAPH_MOCKER.graph);

        visitedManager.settleEdgeRight(ALL_EDGES.get(0));

        assertFalse(visitedManager.isEdgeSettledLeft(ALL_EDGES.get(0).detach(true)));
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
