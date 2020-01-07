package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIteratorState;

public class CellRunnerTestInputs {
    public final EdgeExplorer neighborExplorer;
    public final NodeAccess nodeAccess;
    public final VisitedManager visitedManager;
    public final EdgeIteratorState startingEdge;

    public CellRunnerTestInputs(final PolygonRoutingTestGraph graphMocker) {
        this.neighborExplorer = graphMocker.graph.createEdgeExplorer();
        this.nodeAccess = graphMocker.nodeAccess;
        this.visitedManager = new VisitedManager(graphMocker.graph);
        this.startingEdge = graphMocker.graph.getEdgeIteratorState(47, 26);
    }
}
