package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;

class CellRunnerRight extends CellRunner {

    public CellRunnerRight(final Graph graph, final NodeAccess nodeAccess, final VisitedManager visitedManager,
                           final EdgeIteratorState startEdge) {
        super(graph, nodeAccess, visitedManager, new VectorAngleCalculatorRight(nodeAccess), startEdge);
    }

    @Override
    VisibilityCell createVisibilityCell() {
        return VisibilityCell.createVisibilityCellFromNodeIDs(nodesOnCell, nodeAccess);
    }

    void settleEdge(final EdgeIteratorState edge) {
        visitedManager.settleEdgeRight(edge);
    }
}
