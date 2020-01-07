package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIteratorState;

import java.util.List;

public class CellRunnerTestInputs {
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
