package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.QueryResult;

import java.util.List;

public class ManyToManyRouting extends MultiRouting {
    private final PathSkeletonGraph pathSkeletonGraph;
    private final List<Integer> nodesToBuildRoutesWith;
    private final Graph graph;
    private final QueryGraph queryGraph;
    private final RoutingAlgorithmFactory routingAlgorithmFactory;
    private final AlgorithmOptions algorithmOptions;

    // TODO from / to ?! lot nodes for start and end point.
    public ManyToManyRouting(final PathSkeletonGraph pathSkeletonGraph, final List<Integer> nodesToBuildRoutesWith, final Graph graph, final List<QueryResult> queryResults,
                             final RoutingAlgorithmFactory routingAlgorithmFactory, final AlgorithmOptions algorithmOptions) {
        this.pathSkeletonGraph = pathSkeletonGraph;
        this.nodesToBuildRoutesWith = nodesToBuildRoutesWith;
        this.graph = graph;
        this.queryGraph = prepareQueryGraph(queryResults);
        this.routingAlgorithmFactory = routingAlgorithmFactory;
        this.algorithmOptions = algorithmOptions;
    }

    private QueryGraph prepareQueryGraph(final List<QueryResult> queryResults) {
        final QueryGraph queryGraph = new QueryGraph(this.graph);
        queryGraph.lookup(queryResults);
        return queryGraph;
    }

    void calculatePaths() {
        for (int fromNode : nodesToBuildRoutesWith) {
            final OneToManyRouting oneToManyRouting =
                    new OneToManyRouting(fromNode, this.nodesToBuildRoutesWith, pathSkeletonGraph, this.queryGraph, this.routingAlgorithmFactory, this.algorithmOptions);
            oneToManyRouting.findPathBetweenAllNodePairs();
            this.allFoundPaths.putAll(oneToManyRouting.getAllFoundPathsMap());
        }
    }
}
