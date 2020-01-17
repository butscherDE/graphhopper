package com.graphhopper.storage.index;

import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;

class CellRunnerRight extends CellRunner {

    public CellRunnerRight(final Graph graph, final VisitedManagerDual visitedManagerDual, final EdgeIteratorState startEdge) {
        super(graph, visitedManagerDual, new VectorAngleCalculatorRight(graph.getNodeAccess()), startEdge);
    }

    @Override
    VisibilityCell createVisibilityCell() {
        return VisibilityCell.createVisibilityCellFromNodeIDs(extractNodesFromVisitedEdges(), nodeAccess);
    }

    void markGloballyVisited(final EdgeIteratorState edge) {
        globalVisitedManager.settleEdgeRight(edge);
    }
}
