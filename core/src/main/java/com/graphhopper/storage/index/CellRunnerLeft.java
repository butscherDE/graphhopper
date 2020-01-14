package com.graphhopper.storage.index;

import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;

import java.util.Collections;

class CellRunnerLeft extends CellRunner {

    public CellRunnerLeft(final Graph graph, final VisitedManagerDual globalVisitedManager, final EdgeIteratorState startEdge) {
        super(graph, globalVisitedManager, new VectorAngleCalculatorLeft(graph.getNodeAccess()), startEdge);
    }

    public CellRunnerLeft(final Graph graph, final VisitedManagerDual globalVisitedManager, final VectorAngleCalculator vectorAngleCalculator, final EdgeIteratorState startEdge,
                          final EdgeIteratorState endEdge) {
        super(graph, globalVisitedManager, vectorAngleCalculator, startEdge, endEdge);
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
