package com.graphhopper.routing;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RPHAST implements RoutingAlgorithm {
    private final Graph graph;
    private final Weighting weighting;

    private Set<Integer> targetSet;

    public RPHAST(final Graph graph, final Weighting weighting) {
        this.graph = graph;
        this.weighting = weighting;
    }

    public void prepareForTargetSet(final Set<Integer> targetSet) {
        this.targetSet = targetSet;


    }

    @Override
    public Path calcPath(int from, int to) {
        return null;
    }

    @Override
    public List<Path> calcPaths(int from, int to) {
        return null;
    }

    @Override
    public void setMaxVisitedNodes(int numberOfNodes) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getVisitedNodes() {
        return 0;
    }
}
