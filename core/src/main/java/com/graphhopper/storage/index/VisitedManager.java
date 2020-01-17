package com.graphhopper.storage.index;

import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.HashMap;
import java.util.Map;

public class VisitedManager {
    final Map<Edge, Boolean> edgeIdVisited;

    public VisitedManager(final Graph graph) {
        this.edgeIdVisited = new HashMap<>(graph.getEdges());
    }

    public void settleEdge(EdgeIteratorState edge) {
        final Edge wrappedEdge = new Edge(edge);
        edgeIdVisited.putIfAbsent(wrappedEdge, true);
    }

    public boolean isEdgeSettled(EdgeIteratorState edge) {
        final Edge wrappedEdge = new Edge(edge);
        final Boolean isVisited = edgeIdVisited.get(wrappedEdge);
        return isVisited == null ? false : isVisited;
    }

    public static EdgeIteratorState forceNodeIdsAscending(final EdgeIteratorState edge) {
        return edge.getBaseNode() < edge.getAdjNode() ? edge : edge.detach(true);
    }

    private static class Edge {
        private final int hashCode;
        private final int baseNode;
        private final int adjNode;

        Edge(EdgeIteratorState edge) {
            this.hashCode = setHashCode(edge);
            this.baseNode = edge.getBaseNode();
            this.adjNode = edge.getAdjNode();
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
                return baseNode == oEdge.baseNode && adjNode == oEdge.adjNode;
            } else {
                return false;
            }
        }
    }
}
