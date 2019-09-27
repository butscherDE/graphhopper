package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.template.util.QueryGraphCreator;
import com.graphhopper.storage.Graph;

import java.util.List;

public class ManyToManyRouting extends MultiRouting {
    private final List<Integer> nodesToConsiderForRouting;
    private final List<Integer> nodesToBuildRoutesWith;
    private final Graph graph;
    private final QueryGraph queryGraph;
    private final RoutingAlgorithmFactory routingAlgorithmFactory;
    private final AlgorithmOptions algorithmOptions;

    public ManyToManyRouting(final List<Integer> nodesToConsiderForRouting, final List<Integer> nodesToBuildRoutesWith,
                             Graph graph, RoutingAlgorithmFactory routingAlgorithmFactory, AlgorithmOptions algorithmOptions) {
        this.nodesToConsiderForRouting = nodesToConsiderForRouting;
        this.nodesToBuildRoutesWith = nodesToBuildRoutesWith;
        this.graph = graph;
        this.queryGraph = prepareQueryGraph();
        this.routingAlgorithmFactory = routingAlgorithmFactory;
        this.algorithmOptions = algorithmOptions;
    }

    private QueryGraph prepareQueryGraph() {
        final QueryGraphCreator queryGraphCreator = new QueryGraphCreator(this.graph, this.nodesToBuildRoutesWith);
        return queryGraphCreator.createQueryGraph();
    }

    void calculatePaths() {
        for (int fromNode : nodesToBuildRoutesWith) {
            final OneToManyRouting
                    oneToManyRouting = new OneToManyRouting(fromNode, this.nodesToBuildRoutesWith, nodesToConsiderForRouting, this.queryGraph, this.routingAlgorithmFactory, this.algorithmOptions);
            oneToManyRouting.findPathBetweenAllNodePairs();
            this.allFoundPaths.putAll(oneToManyRouting.getAllFoundPathsMap());
        }
    }
}
