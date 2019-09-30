package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.*;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OneToManyRouting extends MultiRouting {
    private final int fromNode;
    private final List<Integer> toNodes;
    private final List<Integer> nodesToConsiderForRouting;
    private final QueryGraph queryGraph;
    private final RoutingAlgorithmFactory routingAlgorithmFactory;
    private final AlgorithmOptions algorithmOptions;

    public OneToManyRouting(final int fromNode, final List<Integer> toNodes, List<Integer> nodesToConsiderForRouting, final QueryGraph queryGraph,
                            final RoutingAlgorithmFactory routingAlgorithmFactory, final AlgorithmOptions algorithmOptions) {
        this.fromNode = fromNode;
        this.toNodes = toNodes;
        this.nodesToConsiderForRouting = nodesToConsiderForRouting;
        this.queryGraph = queryGraph;
        this.routingAlgorithmFactory = routingAlgorithmFactory;
        this.algorithmOptions = algorithmOptions;
    }

    void calculatePaths() {
        for (final int toNode : toNodes) {
            final RoutingAlgorithm routingAlgorithm = buildRoutingAlgorithmForFromToPair(toNode);
            final Path path = routingAlgorithm.calcPath(this.fromNode, toNode);
            this.allFoundPaths.put(new Pair<>(this.fromNode, toNode), path);
        }
    }

    private RoutingAlgorithm buildRoutingAlgorithmForFromToPair(int toNode) {
        final AbstractRoutingAlgorithm routingAlgorithm = (AbstractRoutingAlgorithm) routingAlgorithmFactory.createAlgo(queryGraph, algorithmOptions);
        final EdgeFilter considerableNodesEdgeFilter = new NodesToConsiderEdgeFilter(this.nodesToConsiderForRouting, fromNode, toNode);
        routingAlgorithm.setEdgeFilter(considerableNodesEdgeFilter);
        return routingAlgorithm;
    }

    public Map<Pair<Integer, Integer>, Path> getAllFoundPathsMap() {
        return this.allFoundPaths;
    }

    private static class NodesToConsiderEdgeFilter implements EdgeFilter {
        List<Integer> nodesToConsiderInclusiveStartEndPoint;

        NodesToConsiderEdgeFilter(final List<Integer> nodesToConsiderForRouting, final int fromNode, final int toNode) {
            prepareConsiderableNodesList(nodesToConsiderForRouting, fromNode, toNode);
        }

        private void prepareConsiderableNodesList(List<Integer> nodesToConsiderForRouting, int fromNode, int toNode) {
            this.nodesToConsiderInclusiveStartEndPoint = new ArrayList<>(nodesToConsiderForRouting.size() + 2);
            this.nodesToConsiderInclusiveStartEndPoint.addAll(nodesToConsiderForRouting);
            this.nodesToConsiderInclusiveStartEndPoint.add(fromNode);
            this.nodesToConsiderInclusiveStartEndPoint.add(toNode);
        }

        @Override
        public boolean accept(EdgeIteratorState edgeState) {
            final int fromNode = edgeState.getBaseNode();
            final int toNode = edgeState.getAdjNode();

            return areBothNodesConsiderable(fromNode, toNode);
        }

        private boolean areBothNodesConsiderable(int fromNode, int toNode) {
            return this.nodesToConsiderInclusiveStartEndPoint.contains(fromNode) && this.nodesToConsiderInclusiveStartEndPoint.contains(toNode);
        }
    }
}
