package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RPHASTManyToManyTest {
    private final static PolygonRoutingTestGraph graphMocker = PolygonRoutingTestGraph.DEFAULT_INSTANCE;
    private static MultiRouting rphast;

    @BeforeClass
    public static void setUp() {
        final List<Integer> sourceDestinations = prepareSourceDestination();
        final RegionOfInterestRoutingGraph nodesToConsiderForRouting = new OneToManyRoutingTest().prepareInteriorGraph();
        final AlgorithmOptions algorithmOptions = graphMocker.algorithmOptions;

        rphast = new RPHASTManyToMany(nodesToConsiderForRouting, sourceDestinations, graphMocker.graphWithCh, algorithmOptions);
        rphast.findPathBetweenAllNodePairs();
    }

    private static List<Integer> prepareSourceDestination() {
        return new ArrayList<>(Arrays.asList(new Integer[] {28, 29, 30, 32, 40}));
    }

    @Test
    public void validate28To28() {
        final List<Integer> firstPathOption = createPathCandidate(28);

        validatePath(firstPathOption);
    }

    @Test
    public void validate28to29() {
        final List<Integer> firstPathOption = createPathCandidate(28, 29);

        validatePath(firstPathOption);
    }

    @Test
    public void validate28To30() {
        final List<Integer> firstPathOption = createPathCandidate(28, 47, 30);

        validatePath(firstPathOption);
    }

    @Test
    public void validate28to32() {
        final List<Integer> firstPathOption = createPathCandidate(28, 47, 48, 49, 32);
        final List<Integer> secondPathOption = createPathCandidate(28, 47, 55, 49, 32);

        validatePath(firstPathOption, secondPathOption);
    }

    @Test
    public void validate28To40() {
        final List<Integer> firstPathOption = createPathCandidate(28, 46, 53, 52, 40);

        validatePath(firstPathOption);
    }

    @Test
    public void validate29To28() {
        final List<Integer> firstPathOption = createPathCandidate(29, 28);

        validatePath(firstPathOption);
    }

    @Test
    public void validate29To29() {
        final List<Integer> firstPathOption = createPathCandidate(29);

        validatePath(firstPathOption);
    }

    @Test
    public void validate29To30() {
        final List<Integer> firstPathOption = createPathCandidate(29, 30);

        validatePath(firstPathOption);
    }

    @Test
    public void validate29To32() {
        final List<Integer> firstPathOption = createPathCandidate(29, 48, 49, 32);

        validatePath(firstPathOption);
    }

    @Test
    public void validate29To40() {
        final List<Integer> firstPathOption = createPathCandidate(29, 48, 55, 57, 52, 40);

        validatePath(firstPathOption);
    }

    @Test
    public void validate30To28() {
        final List<Integer> firstPathOption = createPathCandidate(30, 47, 28);

        validatePath(firstPathOption);
    }

    @Test
    public void validate30To29() {
        final List<Integer> firstPathOption = createPathCandidate(30, 29);

        validatePath(firstPathOption);
    }

    @Test
    public void validate30To30() {
        final List<Integer> firstPathOption = createPathCandidate(30);

        validatePath(firstPathOption);
    }

    @Test
    public void validate30To32() {
        final List<Integer> firstPathOption = createPathCandidate(30, 48, 49, 32);

        validatePath(firstPathOption);
    }

    @Test
    public void validate30To40() {
        final List<Integer> firstPathOption = createPathCandidate(30, 48, 55, 57, 52, 40);

        validatePath(firstPathOption);
    }

    @Test
    public void validate32To28() {
        final List<Integer> firstPathOption = createPathCandidate(32, 49, 48, 47, 28);

        validatePath(firstPathOption);
    }

    @Test
    public void validate32To29() {
        final List<Integer> firstPathOption = createPathCandidate(32, 49, 48, 29);

        validatePath(firstPathOption);
    }

    @Test
    public void validate32To30() {
        final List<Integer> firstPathOption = createPathCandidate(32, 49, 48, 30);

        validatePath(firstPathOption);
    }

    @Test
    public void validate32To32() {
        final List<Integer> firstPathOption = createPathCandidate(32);

        validatePath(firstPathOption);
    }

    @Test
    public void validate32To40() {
        final List<Integer> firstPathOption = createPathCandidate(32, 49, 56, 57, 52, 40);

        validatePath(firstPathOption);
    }

    @Test
    public void validate40To28() {
        final List<Integer> firstPathOption = createPathCandidate(40, 52, 53, 46, 28);

        validatePath(firstPathOption);
    }

    @Test
    public void validate40To29() {
        final List<Integer> firstPathOption = createPathCandidate(40, 52, 57, 55, 48, 29);

        validatePath(firstPathOption);
    }

    @Test
    public void validate40To30() {
        final List<Integer> firstPathOption = createPathCandidate(40, 52, 57, 55, 48, 30);

        validatePath(firstPathOption);
    }

    @Test
    public void validate40To32() {
        final List<Integer> firstPathOption = createPathCandidate(40, 52, 57, 56, 49, 32);

        validatePath(firstPathOption);
    }

    @Test
    public void validate40To40() {
        final List<Integer> firstPathOption = createPathCandidate(40);

        validatePath(firstPathOption);
    }

    private void validatePath(List<Integer>... possiblePaths) {
        final int fromNode = possiblePaths[0].get(0);
        final int toNode = possiblePaths[0].get(possiblePaths[0].size() - 1);

        final List<Integer> nodesInPathOrder = this.retrieveFoundPathsNode(fromNode, toNode);
        OneToManyRoutingTest.validatePath(nodesInPathOrder, possiblePaths);
    }

    private List<Integer> retrieveFoundPathsNode(final int fromNode, final int toNode) {
        return this.rphast.getPathByFromEndNodeID(fromNode, toNode).getNodesInPathOrder();
    }

    private List<Integer> createPathCandidate(Integer... nodeIdsOnPath) {
        return new ArrayList<>(Arrays.asList(nodeIdsOnPath));
    }
}
