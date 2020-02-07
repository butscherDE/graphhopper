package com.graphhopper.routing;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.List;

public class PathSimpleized extends Path {
    public PathSimpleized(Graph graph, Weighting weighting, List<EdgeIteratorState> edges, final double weight, final boolean found) {
        super(graph, weighting);

        for (EdgeIteratorState edge : edges) {
            this.edgeIds.add(edge.getEdge());
        }

        this.distance = 0;
        for (EdgeIteratorState edge : edges) {
            this.distance += edge.getDistance();
        }
        if (found) {
            this.fromNode = edges.get(0).getBaseNode();
            this.endNode = edges.get(edges.size() - 1).getAdjNode();
        } else {
            this.fromNode = -1;
            this.endNode = -1;
        }

        this.weight = weight;

        setFound(found);
    }

    @Override
    public Path extract() {
        return this;
    }
}
