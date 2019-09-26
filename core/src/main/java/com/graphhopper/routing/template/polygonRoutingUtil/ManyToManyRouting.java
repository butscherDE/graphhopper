package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.template.RoutingTemplate;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.shapes.GHPoint;

import java.util.ArrayList;
import java.util.List;

public class ManyToManyRouting {
    private final List<Integer> nodesToConsiderForRouting;
    private final List<Integer> nodesToBuildRoutesWith;
    private final List<Path> allFoundPaths;
    private final QueryGraph queryGraph;
    private final RoutingAlgorithmFactory routingAlgorithmFactory;
    private final AlgorithmOptions algorithmOptions;

    public ManyToManyRouting(final List<Integer> nodesToConsiderForRouting, final List<Integer> nodesToBuildRoutesWith,
                             Graph graph, RoutingAlgorithmFactory routingAlgorithmFactory, AlgorithmOptions algorithmOptions) {
        this.nodesToConsiderForRouting = nodesToConsiderForRouting;
        this.nodesToBuildRoutesWith = nodesToBuildRoutesWith;
        this.allFoundPaths = new ArrayList<>(nodesToBuildRoutesWith.size() * nodesToBuildRoutesWith.size());
        this.queryGraph = new QueryGraph(graph);
        this.routingAlgorithmFactory = routingAlgorithmFactory;
        this.algorithmOptions = algorithmOptions;
    }

    public void lookup(RoutingTemplate routingTemplate, NodeAccess nodeAccess, FlagEncoder flagEncoder) {
        List<GHPoint> pointsOfNodes = allNodeIDsToGHPoints(nodeAccess);
        addLookupResultsToQueryGraph(routingTemplate, flagEncoder, pointsOfNodes);
    }

    private List<GHPoint> allNodeIDsToGHPoints(NodeAccess nodeAccess) {
        final List<GHPoint> pointsOfNodes = new ArrayList<>(this.nodesToBuildRoutesWith.size());

        for (final int nodeId : this.nodesToBuildRoutesWith) {
            pointsOfNodes.add(new GHPoint(nodeAccess.getLatitude(nodeId), nodeAccess.getLongitude(nodeId)));
        }
        return pointsOfNodes;
    }

    private void addLookupResultsToQueryGraph(RoutingTemplate routingTemplate, FlagEncoder flagEncoder, List<GHPoint> pointsOfNodes) {
        final List<QueryResult> queryResults = routingTemplate.lookup(pointsOfNodes, flagEncoder);
        this.queryGraph.lookup(queryResults);
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
}
