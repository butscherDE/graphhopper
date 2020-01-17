package com.graphhopper.storage.index;

import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.Collections;

class CellRunnerLeft extends CellRunner {

    public CellRunnerLeft(final Graph graph, final VisitedManagerDual globalVisitedManager, final EdgeIteratorState startEdge) {
        super(graph, globalVisitedManager, new VectorAngleCalculatorLeft(graph.getNodeAccess()), startEdge);
    }

    @Override
    VisibilityCell createVisibilityCell() {
        Collections.reverse(edgesOnCell);
        return VisibilityCell.createVisibilityCellFromNodeIDs(extractNodesFromVisitedEdges(), nodeAccess);
    }

    void markGloballyVisited(final EdgeIteratorState edge) {
        globalVisitedManager.settleEdgeLeft(edge);
    }
}
