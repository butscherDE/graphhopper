package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIteratorState;

class CellRunnerRight extends CellRunner {

    public CellRunnerRight(final VisibilityCellsCreator visibilityCellsCreator, final EdgeExplorer neighborExplorer, final NodeAccess nodeAccess,
                          final VisitedManager visitedManager) {
        super(visibilityCellsCreator, neighborExplorer, nodeAccess, visitedManager, new VectorAngleCalculatorRight(nodeAccess));
    }

    @Override
    VisibilityCell createVisibilityCell() {
        return VisibilityCell.createVisibilityCellFromNodeIDs(nodesOnCell, nodeAccess);
    }

    void settleEdge(final EdgeIteratorState edge) {
        visitedManager.settleEdgeRight(edge);
    }
}
