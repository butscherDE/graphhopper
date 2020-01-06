package com.graphhopper.storage.index;

import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;

import java.util.HashMap;
import java.util.Map;

class VisitedManager {
    final Map<Integer, Boolean> visitedLeft;
    final Map<Integer, Boolean> visitedRight;

    public VisitedManager(final Graph graph) {
        this.visitedLeft = new HashMap<>(graph.getEdges());
        this.visitedRight = new HashMap<>(graph.getEdges());
    }

    public void settleEdgeLeft(EdgeIteratorState edge) {
        edge = forceNodeIdsAscending(edge);
        if (visitedLeft.get(edge.getEdge()) == null) {
            visitedLeft.put(edge.getEdge(), true);
        }
    }

    public void settleEdgeRight(EdgeIteratorState edge) {
        edge = forceNodeIdsAscending(edge);
        if (visitedRight.get(edge.getEdge()) == null) {
            visitedRight.put(edge.getEdge(), true);
        }
    }

    boolean isEdgeSettledLeft(EdgeIteratorState edge) {
        return isEdgeSettled(edge, visitedLeft);
    }

    boolean isEdgeSettledRight(EdgeIteratorState edge) {
        return isEdgeSettled(edge, visitedRight);
    }

    private boolean isEdgeSettled(EdgeIteratorState edge, Map<Integer, Boolean> isVisited) {
        final Boolean visited = isVisited.get(edge.getEdge());
        return visited == null ? false : visited;
    }

    public EdgeIteratorState forceNodeIdsAscending(final EdgeIteratorState edge) {
        return edge.getBaseNode() < edge.getAdjNode() ? edge : edge.detach(true);
    }
}
