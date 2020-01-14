package com.graphhopper.storage.index;

import java.util.HashMap;
import java.util.Map;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;

public class VisitedManager {
    final Map<Edge, Boolean> edgeIdVisited;



    public VisitedManager(final Graph graph) {
        this.edgeIdVisited = new HashMap<>(graph.getEdges());
    }

    public void settleEdge(EdgeIteratorState edge) {
        final Edge wrappedEdge = new Edge(edge);
        if (edgeIdVisited.get(wrappedEdge) == null) {
            edgeIdVisited.put(wrappedEdge, true);
        }
    }

    public boolean isEdgeSettled(EdgeIteratorState edge) {
        final Edge wrappedEdge = new Edge(edge);
        final Boolean isVisited = edgeIdVisited.get(wrappedEdge);
        return isVisited == null ? false : isVisited;
    }

    public static EdgeIteratorState forceNodeIdsAscending(final EdgeIteratorState edge) {
        return edge.getBaseNode() < edge.getAdjNode() ? edge : edge.detach(true);
    }

    private class Edge {
        private final int hashCode;
        private final EdgeIteratorState edge;

        public Edge(EdgeIteratorState edge) {
            hashCode = setHashCode(edge);
            this.edge = edge;
        }

        private int setHashCode(EdgeIteratorState edge) {
            int hashCode;
            if (isEdgeNonDescending(edge)) {
                hashCode = edge.getEdge() + 1;
            } else {
                hashCode = edge.getEdge() * -1;
            }
            return hashCode;
        }

        private boolean isEdgeNonDescending(EdgeIteratorState edge) {
            return edge.getBaseNode() <= edge.getAdjNode();
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Edge) {
                final Edge oEdge = (Edge) o;
                return edge.getBaseNode() == oEdge.edge.getBaseNode() && edge.getAdjNode() == oEdge.edge.getAdjNode();
            } else {
                return false;
            }
        }
    }
}
