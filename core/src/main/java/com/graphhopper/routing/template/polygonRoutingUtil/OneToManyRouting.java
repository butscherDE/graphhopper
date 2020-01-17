package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.*;

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
            final RoutingAlgorithm routingAlgorithm = buildRoutingAlgorithmForFromToPair(toNode);
            final Path path = routingAlgorithm.calcPath(this.fromNode, toNode);
            this.allFoundPaths.put(new NodeIdPair(this.fromNode, toNode), path);
        }
    }

    private RoutingAlgorithm buildRoutingAlgorithmForFromToPair(int toNode) {
        final AbstractRoutingAlgorithm routingAlgorithm = (AbstractRoutingAlgorithm) routingAlgorithmFactory.createAlgo(queryGraph, algorithmOptions);
        prepareEdgeFilterWithFromToNode(toNode);
        routingAlgorithm.setEdgeFilter(pathSkeletonGraph);
        return routingAlgorithm;
    }

    private void prepareEdgeFilterWithFromToNode(final int toNode) {
        this.pathSkeletonGraph.prepareForEntryExitNodes(this.fromNode, toNode);
    }

    public Map<NodeIdPair, Path> getAllFoundPathsMap() {
        return this.allFoundPaths;
    }
}
