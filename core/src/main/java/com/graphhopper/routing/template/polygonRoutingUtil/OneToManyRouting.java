package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.*;
import com.graphhopper.routing.template.RoutingTemplate;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.GHPoint;

import java.util.ArrayList;
import java.util.List;

public class OneToManyRouting {
    private final int fromNode;
    private final List<Integer> toNodes;
    private final List<Integer> nodesToConsiderForRouting;
    private final QueryGraph queryGraph;
    private final RoutingAlgorithmFactory routingAlgorithmFactory;
    private final AlgorithmOptions algorithmOptions;
    private final List<Path> allFoundPaths;

    public OneToManyRouting(final int fromNode, final List<Integer> toNodes, List<Integer> nodesToConsiderForRouting, final QueryGraph queryGraph,
                            final RoutingAlgorithmFactory routingAlgorithmFactory, final AlgorithmOptions algorithmOptions) {
        this.fromNode = fromNode;
        this.toNodes = toNodes;
        this.allFoundPaths = new ArrayList<>(toNodes.size());
        this.nodesToConsiderForRouting = nodesToConsiderForRouting;
        this.queryGraph = queryGraph;
        this.routingAlgorithmFactory = routingAlgorithmFactory;
        this.algorithmOptions = algorithmOptions;
    }

    public void calcAllPaths() {
        for (final int toNode : toNodes) {
            final RoutingAlgorithm routingAlgorithm = buildRoutingAlgorithmForFromToPair(toNode);
            final Path path = routingAlgorithm.calcPath(this.fromNode, toNode);
            this.allFoundPaths.add(path);
        }
    }

    private RoutingAlgorithm buildRoutingAlgorithmForFromToPair(int toNode) {
        final AbstractRoutingAlgorithm routingAlgorithm = (AbstractRoutingAlgorithm) routingAlgorithmFactory.createAlgo(queryGraph, algorithmOptions);
        final EdgeFilter considerableNodesEdgeFilter = new NodesToConsiderEdgeFilter(this.nodesToConsiderForRouting, fromNode, toNode);
        routingAlgorithm.setEdgeFilter(considerableNodesEdgeFilter);
        return routingAlgorithm;
    }

    public List<Path> getAllFoundPaths() {
        return this.allFoundPaths;
    }

    private class NodesToConsiderEdgeFilter implements EdgeFilter {
        List<Integer> nodesToConsiderInclusiveStartEndPoint;

        public NodesToConsiderEdgeFilter(final List<Integer> nodesToConsiderForRouting, final int fromNode, final int toNode) {
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
