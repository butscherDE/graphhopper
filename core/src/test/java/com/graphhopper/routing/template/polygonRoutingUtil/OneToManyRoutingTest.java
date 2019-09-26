package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.*;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.DistanceCalc2D;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class OneToManyRoutingTest {
    private final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();
    private final QueryGraph queryGraph = new QueryGraph(graphMocker.graph);
    private OneToManyRouting oneToManyRouting;

    @Before
    public void setupOneToManyRouting() {
        final int fromNode = 28;
        final List<Integer> toNodes = prepareToNodes();
        final List<Integer> nodesToConsiderForRouting = prepareInteriorGraph();
        final RoutingAlgorithmFactory routingAlgorithmFactory = new RoutingAlgorithmFactorySimple();
        final AlgorithmOptions algorithmOptions = this.graphMocker.algorithmOptions;

        this.prepareQueryGraph(fromNode, toNodes);
        this.oneToManyRouting = new OneToManyRouting(fromNode, toNodes, nodesToConsiderForRouting, queryGraph, routingAlgorithmFactory, algorithmOptions);
        this.oneToManyRouting.calcAllPaths();
    }

    private List<Integer> prepareToNodes() {
        final Integer[] toNodesArray = new Integer[] {32, 40, 45};
        return new ArrayList<>(Arrays.asList(toNodesArray));
    }

    private List<Integer> prepareInteriorGraph() {
        final Integer[] nodesToConsiderForRoutingArray = new Integer[] {46 ,47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57};
        return new ArrayList<>(Arrays.asList(nodesToConsiderForRoutingArray));
    }

    private void prepareQueryGraph(final int fromNode, final List<Integer> toNodes) {
        List<Integer> allNodes = prepareAllNodesList(fromNode, toNodes);

        List<QueryResult> queryResults = getQueryResults( allNodes);
        this.queryGraph.lookup(queryResults);
    }

    private List<Integer> prepareAllNodesList(int fromNode, List<Integer> toNodes) {
        final List<Integer> allNodes = new ArrayList<>(toNodes.size() + 1);
        allNodes.add(fromNode);
        allNodes.addAll(toNodes);
        return allNodes;
    }

    private List<QueryResult> getQueryResults(List<Integer> allNodes) {
        final List <QueryResult> queryResults = new ArrayList<>(allNodes.size());

        for (final int node : allNodes) {
            final double latitude = this.graphMocker.nodeAccess.getLatitude(node);
            final double longitude = this.graphMocker.nodeAccess.getLongitude(node);

            QueryResult queryResult = createQueryReult(node, latitude, longitude);
            queryResults.add(queryResult);
        }
        return queryResults;
    }

    private QueryResult createQueryReult(int node, double latitude, double longitude) {
        QueryResult queryResult = new QueryResult(latitude, longitude);
        queryResult.setClosestNode(node);
        queryResult.setWayIndex(0);
        queryResult.setClosestEdge(findClosestEdge(node));
        queryResult.calcSnappedPoint(new DistanceCalc2D());
        return queryResult;
    }

    private EdgeIteratorState findClosestEdge(final int baseNode) {
        EdgeExplorer edgeExplorer = this.graphMocker.graph.createEdgeExplorer();
        EdgeIterator edgeIterator = edgeExplorer.setBaseNode(baseNode);
        edgeIterator.next();
        return edgeIterator;
    }

    @Test
    public void validateFirstPath() {
        final List<Integer> nodesInPathOrder = this.retrieveFoundPathsNode(0);

        final List<Integer> firstPathOption = new ArrayList<>(Arrays.asList(new Integer[] {28, 47, 48, 49,32}));
        final List<Integer> secondPathOption = new ArrayList<>(Arrays.asList(new Integer[] {28, 47, 55, 49,32}));

        assertTrue(nodesInPathOrder.equals(firstPathOption) || nodesInPathOrder.equals(secondPathOption));
    }

    @Test
    public void validateSecondPath() {
        final List<Integer> nodesInPathOrder = this.retrieveFoundPathsNode(1);


    }

    @Test
    public void validateThirdPath() {
        final List<Integer> nodesInPathOrder = this.retrieveFoundPathsNode(2);

    }

    private List<Integer> retrieveFoundPathsNode(final int index) {
        return this.oneToManyRouting.getAllFoundPaths().get(index).getNodesInPathOrder();
    }
}
