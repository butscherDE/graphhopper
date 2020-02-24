package com.graphhopper.routing;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.List;

public class PathSimpled extends Path {
    private PathSimpled(Graph graph, Weighting weighting, List<EdgeIteratorState> edges, final double weight, final boolean found) {
        super(graph, weighting);

        setEdges(edges);
        setDistance(edges);
        setTime(edges);
        setStartEndNode(edges, found);
        setWeight(weight);
        setFound(found);
    }

    private void setEdges(List<EdgeIteratorState> edges) {
        for (EdgeIteratorState edge : edges) {
            this.edgeIds.add(edge.getEdge());
        }
    }

    private void setDistance(List<EdgeIteratorState> edges) {
        this.distance = 0;
        for (EdgeIteratorState edge : edges) {
            this.distance += edge.getDistance();
        }
    }

    private void setTime(List<EdgeIteratorState> edges) {
        int lastEdge = EdgeIterator.NO_EDGE;
        for (EdgeIteratorState edge : edges) {
            this.time += weighting.calcMillis(edge, false, lastEdge);
            lastEdge = edge.getEdge();
        }
    }

    private void setStartEndNode(List<EdgeIteratorState> edges, boolean found) {
        if (found) {
            this.fromNode = edges.get(0).getBaseNode();
            this.endNode = edges.get(edges.size() - 1).getAdjNode();
        } else {
            this.fromNode = -1;
            this.endNode = -1;
        }
    }

    private PathSimpled(Graph graph, Weighting weighting, int source, int target, final double weight,
                        final boolean found) {
        super(graph, weighting);
        this.fromNode = source;
        this.endNode = target;
        this.weight = weight;
        this.found = found;
    }

    public static PathSimpled create(Graph graph, Weighting weighting, List<EdgeIteratorState> edges, int source,
                                     int target, final double weight, final boolean found) {
        if (edges.size() > 0) {
            return new PathSimpled(graph, weighting, edges, weight, found);
        } else {
            return new PathSimpled(graph, weighting, source, target, weight, found);
        }
    }

    @Override
    public Path extract() {
        return this;
    }
}
