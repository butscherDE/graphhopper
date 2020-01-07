package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIteratorState;

import java.util.Collections;

class CellRunnerLeft extends CellRunner {

    public CellRunnerLeft(final EdgeExplorer neighborExplorer, final NodeAccess nodeAccess, final VisitedManager visitedManager, final EdgeIteratorState startEdge) {
        super(neighborExplorer, nodeAccess, visitedManager, new VectorAngleCalculatorLeft(nodeAccess), startEdge);
    }

    @Override
    VisibilityCell createVisibilityCell() {
        Collections.reverse(nodesOnCell);
        return VisibilityCell.createVisibilityCellFromNodeIDs(nodesOnCell, nodeAccess);
    }

    void settleEdge(final EdgeIteratorState edge) {
        visitedManager.settleEdgeLeft(edge);
    }
}
