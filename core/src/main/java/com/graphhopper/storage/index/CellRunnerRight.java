package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;

class CellRunnerRight extends CellRunner {

    public CellRunnerRight(final Graph graph, final NodeAccess nodeAccess, final VisitedManagerDual visitedManagerDual,
                           final EdgeIteratorState startEdge) {
        super(graph, nodeAccess, visitedManagerDual, new VectorAngleCalculatorRight(nodeAccess), startEdge);
    }

    @Override
    VisibilityCell createVisibilityCell() {
        return VisibilityCell.createVisibilityCellFromNodeIDs(nodesOnCell, nodeAccess);
    }

    void markGloballyVisited(final EdgeIteratorState edge) {
        globalVisitedManager.settleEdgeRight(edge);
    }
}
