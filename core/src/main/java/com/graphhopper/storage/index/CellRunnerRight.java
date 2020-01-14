package com.graphhopper.storage.index;

import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;

class CellRunnerRight extends CellRunner {

    public CellRunnerRight(final Graph graph, final VisitedManagerDual visitedManagerDual, final EdgeIteratorState startEdge) {
        super(graph, visitedManagerDual, new VectorAngleCalculatorRight(graph.getNodeAccess()), startEdge);
    }

    public CellRunnerRight(final Graph graph, final VisitedManagerDual globalVisitedManager, final VectorAngleCalculator vectorAngleCalculator, final EdgeIteratorState startEdge,
                          final EdgeIteratorState endEdge) {
        super(graph, globalVisitedManager, vectorAngleCalculator, startEdge, endEdge);
    }

    @Override
    VisibilityCell createVisibilityCell() {
        return VisibilityCell.createVisibilityCellFromNodeIDs(extractNodesFromVisitedEdges(), nodeAccess);
    }

    void markGloballyVisited(final EdgeIteratorState edge) {
        globalVisitedManager.settleEdgeRight(edge);
    }
}
