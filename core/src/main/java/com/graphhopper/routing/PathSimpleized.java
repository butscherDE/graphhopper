package com.graphhopper.routing;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.List;

public class PathSimpleized extends Path {
    public PathSimpleized(Graph graph, Weighting weighting, List<EdgeIteratorState> edges, final double weight) {
        super(graph, weighting);

        for (EdgeIteratorState edge : edges) {
            this.edgeIds.add(edge.getEdge());
        }

        this.distance = 0;
        for (EdgeIteratorState edge : edges) {
            this.distance += edge.getDistance();
        }
        this.fromNode = edges.get(0).getBaseNode();
        this.endNode = edges.get(edges.size() - 1).getAdjNode();
        this.weight = weight;
    }

    @Override
    public Path extract() {
        if (isFound())
            throw new IllegalStateException("Extract can only be called once");

        return setFound(true);
    }
}
