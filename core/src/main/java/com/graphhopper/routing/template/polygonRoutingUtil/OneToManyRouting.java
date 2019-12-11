package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.*;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.StopWatch;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OneToManyRouting extends MultiRouting {
    private final int fromNode;
    private final List<Integer> toNodes;
    private final PathSkeletonGraph pathSkeletonGraph;
    private final QueryGraph queryGraph;
    private final RoutingAlgorithmFactory routingAlgorithmFactory;
    private final AlgorithmOptions algorithmOptions;

    public OneToManyRouting(final int fromNode, final List<Integer> toNodes, final PathSkeletonGraph pathSkeletonGraph, final QueryGraph queryGraph,
                            final RoutingAlgorithmFactory routingAlgorithmFactory, final AlgorithmOptions algorithmOptions) {
        this.fromNode = fromNode;
        this.toNodes = toNodes;
        this.pathSkeletonGraph = pathSkeletonGraph;
        this.queryGraph = queryGraph;
        this.routingAlgorithmFactory = routingAlgorithmFactory;
        this.algorithmOptions = algorithmOptions;
    }

    void calculatePaths() {
        for (final int toNode : toNodes) {
            final int lala = toNode;
            final RoutingAlgorithm routingAlgorithm = buildRoutingAlgorithmForFromToPair(toNode);
            final Path path = routingAlgorithm.calcPath(this.fromNode, toNode);
            this.allFoundPaths.put(new Pair<>(this.fromNode, toNode), path);
        }
    }

    private RoutingAlgorithm buildRoutingAlgorithmForFromToPair(int toNode) {
        final AbstractRoutingAlgorithm routingAlgorithm = (AbstractRoutingAlgorithm) routingAlgorithmFactory.createAlgo(queryGraph, algorithmOptions);
        prepapreEdgeFilterWithFromToNode(toNode);
        routingAlgorithm.setEdgeFilter(pathSkeletonGraph);
        return routingAlgorithm;
    }

    private void prepapreEdgeFilterWithFromToNode(final int toNode) {
        this.pathSkeletonGraph.prepareForEntryExitNodes(this.fromNode, toNode);
    }

    public Map<Pair<Integer, Integer>, Path> getAllFoundPathsMap() {
        return this.allFoundPaths;
    }
}
