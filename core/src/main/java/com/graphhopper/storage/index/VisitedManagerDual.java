package com.graphhopper.storage.index;

import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;

public class VisitedManagerDual {
    final VisitedManager visitedLeft;
    final VisitedManager visitedRight;

    public VisitedManagerDual(final Graph graph) {
        this.visitedLeft = new VisitedManager(graph);
        this.visitedRight = new VisitedManager(graph);
    }


    public void settleEdgeLeft(EdgeIteratorState edge) {
        visitedLeft.settleEdge(edge);
    }

    public void settleEdgeRight(EdgeIteratorState edge) {
        visitedRight.settleEdge(edge);
    }

    public boolean isEdgeSettledLeft(EdgeIteratorState edge) {
        return visitedLeft.isEdgeSettled(edge);
    }

    public boolean isEdgeSettledRight(EdgeIteratorState edge) {
        return visitedRight.isEdgeSettled(edge);
    }
}
