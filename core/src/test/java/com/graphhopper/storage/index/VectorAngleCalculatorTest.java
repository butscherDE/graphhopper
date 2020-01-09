package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VectorAngleCalculatorTest {
    private final static PolygonRoutingTestGraph GRAPH_MOCKER = new PolygonRoutingTestGraph(PolygonRoutingTestGraph.getDefaultNodeList(), PolygonRoutingTestGraph.getDefaultEdgeList());

    @Test
    public void anglesToNode39to38right() {
        GRAPH_MOCKER.nodeAccess.setNode(21, 6, 15);
        GRAPH_MOCKER.nodeAccess.setNode(38, 6, 15);
        final VectorAngleCalculator vac = new VectorAngleCalculatorLeft(GRAPH_MOCKER.nodeAccess);
        final EdgeIteratorState edgeToCalcAngleTo = GRAPH_MOCKER.graph.getEdgeIteratorState(59, 21);

        assertEquals(0, vac.getAngle(39, 38, edgeToCalcAngleTo), 0);

        GRAPH_MOCKER.nodeAccess.setNode(21, 3, 7);
        GRAPH_MOCKER.nodeAccess.setNode(38, 5, 17);
    }

    @Test
    public void anglesToNode39to38left() {
        GRAPH_MOCKER.nodeAccess.setNode(21, 6, 13);
        GRAPH_MOCKER.nodeAccess.setNode(38, 6, 15);
        final VectorAngleCalculator vac = new VectorAngleCalculatorLeft(GRAPH_MOCKER.nodeAccess);
        final EdgeIteratorState edgeToCalcAngleTo = GRAPH_MOCKER.graph.getEdgeIteratorState(59, 21);

        assertEquals(Math.PI, vac.getAngle(39, 38, edgeToCalcAngleTo), 0);

        GRAPH_MOCKER.nodeAccess.setNode(21, 3, 7);
        GRAPH_MOCKER.nodeAccess.setNode(38, 5, 17);
    }

    @Test
    public void anglesToNode39to38below() {
        GRAPH_MOCKER.nodeAccess.setNode(21, 5, 14);
        GRAPH_MOCKER.nodeAccess.setNode(38, 6, 15);
        final VectorAngleCalculator vac = new VectorAngleCalculatorLeft(GRAPH_MOCKER.nodeAccess);
        final EdgeIteratorState edgeToCalcAngleTo = GRAPH_MOCKER.graph.getEdgeIteratorState(59, 21);

        assertEquals(Math.PI * 1.5, vac.getAngle(39, 38, edgeToCalcAngleTo), 0);

        GRAPH_MOCKER.nodeAccess.setNode(21, 3, 7);
        GRAPH_MOCKER.nodeAccess.setNode(38, 5, 17);
    }

    @Test
    public void anglesToNode39to38above() {
        GRAPH_MOCKER.nodeAccess.setNode(21, 7, 14);
        GRAPH_MOCKER.nodeAccess.setNode(38, 6, 15);
        final VectorAngleCalculator vac = new VectorAngleCalculatorLeft(GRAPH_MOCKER.nodeAccess);
        final EdgeIteratorState edgeToCalcAngleTo = GRAPH_MOCKER.graph.getEdgeIteratorState(59, 21);

        assertEquals(Math.PI * 0.5, vac.getAngle(39, 38, edgeToCalcAngleTo), 0);

        GRAPH_MOCKER.nodeAccess.setNode(21, 3, 7);
        GRAPH_MOCKER.nodeAccess.setNode(38, 5, 17);
    }
}
