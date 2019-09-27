package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.template.util.QueryGraphCreator;
import com.graphhopper.storage.Graph;


import java.util.ArrayList;
import java.util.List;

public class ManyToManyRouting {
    private final List<Integer> nodesToConsiderForRouting;
    private final List<Integer> nodesToBuildRoutesWith;
    private final List<Path> allFoundPaths;
    private final Graph graph;
    private final QueryGraph queryGraph;
    private final RoutingAlgorithmFactory routingAlgorithmFactory;
    private final AlgorithmOptions algorithmOptions;

    public ManyToManyRouting(final List<Integer> nodesToConsiderForRouting, final List<Integer> nodesToBuildRoutesWith,
                             Graph graph, RoutingAlgorithmFactory routingAlgorithmFactory, AlgorithmOptions algorithmOptions) {
        this.nodesToConsiderForRouting = nodesToConsiderForRouting;
        this.nodesToBuildRoutesWith = nodesToBuildRoutesWith;
        this.allFoundPaths = new ArrayList<>(nodesToBuildRoutesWith.size() * nodesToBuildRoutesWith.size());
        this.graph = graph;
        this.queryGraph = prepareQueryGraph();
        this.routingAlgorithmFactory = routingAlgorithmFactory;
        this.algorithmOptions = algorithmOptions;
    }

    private QueryGraph prepareQueryGraph() {
        final QueryGraphCreator queryGraphCreator = new QueryGraphCreator(this.graph, this.nodesToBuildRoutesWith);
        return queryGraphCreator.createQueryGraph();
    }

    public void findAllPathsBetweenEntryExitPoints() {
        if (allFoundPaths.size() > 0) {
            throw new IllegalStateException("The algorithm was already run. Retrieve results with getAllPaths() or invoke clear first");
        }

        for (int fromNode : nodesToBuildRoutesWith) {
            final OneToManyRouting oneToManyRouting = new OneToManyRouting(fromNode, this.nodesToBuildRoutesWith, nodesToConsiderForRouting, this.queryGraph, this.routingAlgorithmFactory, this.algorithmOptions);
            oneToManyRouting.calcAllPaths();
            this.allFoundPaths.addAll(oneToManyRouting.getAllFoundPaths());
        }
    }

    public void clear() {
        this.allFoundPaths.clear();
    }

    public List<Path> getAllFoundPaths() {
        return this.allFoundPaths;
    }
}
