package com.graphhopper.storage.index;

import java.util.HashMap;
import java.util.Map;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;

public class VisitedManager {
    final Map<Integer, Boolean> visited;

    public VisitedManager(final Graph graph) {
        this.visited = new HashMap<>(graph.getEdges());
    }

    public void settleEdge(EdgeIteratorState edge) {
        edge = forceNodeIdsAscending(edge);
        if (visited.get(edge.getEdge()) == null) {
            visited.put(edge.getEdge(), true);
        }
    }

    public boolean isEdgeSettled(EdgeIteratorState edge) {
        final Boolean isVisited = visited.get(edge.getEdge());
        return isVisited == null ? false : isVisited;
    }

    public static EdgeIteratorState forceNodeIdsAscending(final EdgeIteratorState edge) {
        return edge.getBaseNode() < edge.getAdjNode() ? edge : edge.detach(true);
    }
}
