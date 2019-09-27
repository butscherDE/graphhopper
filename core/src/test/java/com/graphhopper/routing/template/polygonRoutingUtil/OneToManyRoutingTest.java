package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.*;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.template.util.QueryGraphCreator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class OneToManyRoutingTest {
    private final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();
    private QueryGraph queryGraph;
    private OneToManyRouting oneToManyRouting;
    private int fromNode;
    private List<Integer> toNodes;

    @Before
    public void setupOneToManyRouting() {
        this.fromNode = 28;
        this.toNodes = prepareToNodes();
        final List<Integer> nodesToConsiderForRouting = prepareInteriorGraph();
        final RoutingAlgorithmFactory routingAlgorithmFactory = new RoutingAlgorithmFactorySimple();
        final AlgorithmOptions algorithmOptions = this.graphMocker.algorithmOptions;

        this.prepareQueryGraph(fromNode, toNodes);
        this.oneToManyRouting = new OneToManyRouting(fromNode, toNodes, nodesToConsiderForRouting, queryGraph, routingAlgorithmFactory, algorithmOptions);
        this.oneToManyRouting.findPathBetweenAllNodePairs();
    }

    private List<Integer> prepareToNodes() {
        final Integer[] toNodesArray = new Integer[] {29, 30, 32, 40};
        return new ArrayList<>(Arrays.asList(toNodesArray));
    }

    static List<Integer> prepareInteriorGraph() {
        final Integer[] nodesToConsiderForRoutingArray = new Integer[] {46 ,47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57};
        return new ArrayList<>(Arrays.asList(nodesToConsiderForRoutingArray));
    }

    private void prepareQueryGraph(final int fromNode, final List<Integer> toNodes) {
        List<Integer> allNodes = prepareAllNodesList(fromNode, toNodes);

        this.queryGraph = new QueryGraphCreator(this.graphMocker.graph, allNodes).createQueryGraph();
    }

    private List<Integer> prepareAllNodesList(int fromNode, List<Integer> toNodes) {
        final List<Integer> allNodes = new ArrayList<>(toNodes.size() + 1);
        allNodes.add(fromNode);
        allNodes.addAll(toNodes);
        return allNodes;
    }

    @Test
    public void validate28to29() {
        final List<Integer> nodesInPathOrder = this.retrieveFoundPathsNode(28, 29);
        final List<Integer> firstPathOption = new ArrayList<>(Arrays.asList(new Integer[] {28, 29}));

        validatePath(nodesInPathOrder, firstPathOption);
    }

    @Test
    public void validate28to30() {
        final List<Integer> nodesInPathOrder = this.retrieveFoundPathsNode(28, 30);
        final List<Integer> firstPathOption = new ArrayList<>(Arrays.asList(new Integer[] {28, 47, 30}));

        validatePath(nodesInPathOrder, firstPathOption);
    }

    @Test
    public void validate28to32() {
        final List<Integer> nodesInPathOrder = this.retrieveFoundPathsNode(28, 32);
        final List<Integer> firstPathOption = new ArrayList<>(Arrays.asList(new Integer[] {28, 47, 48, 49, 32}));
        final List<Integer> secondPathOption = new ArrayList<>(Arrays.asList(new Integer[] {28, 47, 55, 49, 32}));

        validatePath(nodesInPathOrder, firstPathOption, secondPathOption);
    }

    @Test
    public void validate28to40() {
        final List<Integer> nodesInPathOrder = this.retrieveFoundPathsNode(28, 40);
        final List<Integer> firstPathOption = new ArrayList<>(Arrays.asList(new Integer[] {28, 46, 53, 52, 40}));

        validatePath(nodesInPathOrder, firstPathOption);
    }

    @Test
    public void numberOfPathsFound() {
        final List<Path> allPaths = this.oneToManyRouting.getAllFoundPaths();

        assertEquals(4, allPaths.size());
    }

    @Test
    public void noDuplicatesFound() {
        final List<Path> allPaths = this.oneToManyRouting.getAllFoundPaths();

        crossValidateAllPathsToFindDuplicates(allPaths);
    }

    private void crossValidateAllPathsToFindDuplicates(List<Path> allPaths) {
        for (int i = 0; i < allPaths.size(); i++) {
            for (int j = 0; j < allPaths.size(); j++) {
                if (i != j) {
                    assertNotEquals(allPaths.get(i), allPaths.get(j));
                }
            }
        }
    }

    private List<Integer> retrieveFoundPathsNode(final int fromNode, final int toNode) {
        return this.oneToManyRouting.getPathByFromEndNodeID(fromNode, toNode).getNodesInPathOrder();
    }

    static void validatePath(final List<Integer> foundPath, final List<Integer>... possibleShortestPaths) {
        boolean correctPathFound = false;

        for (final List<Integer> candidate : possibleShortestPaths) {
            correctPathFound |= foundPath.equals(candidate);
        }

        assertTrue(correctPathFound);
    }
}
