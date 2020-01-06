package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIteratorState;

class CellRunnerRight extends CellRunner {

    public CellRunnerRight(final VisibilityCellsCreator visibilityCellsCreator, final EdgeExplorer neighborExplorer, final NodeAccess nodeAccess,
                          final VisitedManager visitedManager) {
        super(visibilityCellsCreator, neighborExplorer, nodeAccess, visitedManager);
    }

    @Override
    double getAngleOfVectorsOriented(int lastEdgeReversedBaseNode, int lastEdgeReversedAdjNode, EdgeIteratorState candidateEdge) {
        final double angle = getAngle(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdge);
        return angle == 0 || angle == ANGLE_WHEN_COORDINATES_ARE_EQUAL ? angle : angle * (-1) + 2 * Math.PI;
    }

    @Override
    VisibilityCell createVisibilityCell() {
        return VisibilityCell.createVisibilityCellFromNodeIDs(nodesOnCell, nodeAccess);
    }

    void settleEdge(final EdgeIteratorState edge) {
        visitedManager.settleEdgeRight(edge);
    }
}
