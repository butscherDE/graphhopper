package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.RoutingAlgorithmFactorySimple;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManyToManyRoutingTest {
    private final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();
    private ManyToManyRouting manyToManyRouting;

    @Before
    public void setUp() {
        final List<Integer> sourceDestinations = prepareSourceDestination();
        final List<Integer> nodesToConsiderForRouting = OneToManyRoutingTest.prepareInteriorGraph();
        final RoutingAlgorithmFactory routingAlgorithmFactory = new RoutingAlgorithmFactorySimple();
        final AlgorithmOptions algorithmOptions = this.graphMocker.algorithmOptions;

        this.manyToManyRouting = new ManyToManyRouting(nodesToConsiderForRouting, sourceDestinations, this.graphMocker.graph, routingAlgorithmFactory, algorithmOptions);
        this.manyToManyRouting.findAllPathsBetweenEntryExitPoints();
    }

    private List<Integer> prepareSourceDestination() {
        return new ArrayList<>(Arrays.asList(new Integer[] {28, 29, 30, 32, 40}));
    }

    @Test
    public void validate28To28() {
        final List<Integer> firstPathOption = createPathCandidate(28);

        validatePath(0, firstPathOption);
    }

    @Test
    public void validate28to29() {
        final List<Integer> firstPathOption = createPathCandidate(28, 29);

        validatePath(1, firstPathOption);
    }

    @Test
    public void validate28To30() {
        final List<Integer> firstPathOption = createPathCandidate(28, 47, 30);

        validatePath(2, firstPathOption);
    }

    @Test
    public void validate28to32() {
        final List<Integer> firstPathOption = createPathCandidate(28, 47, 48, 49, 32);
        final List<Integer> secondPathOption = createPathCandidate(28, 47, 55, 49, 32);

        validatePath(3, firstPathOption, secondPathOption);
    }

    @Test
    public void validate28To40() {
        final List<Integer> firstPathOption = createPathCandidate(28, 46, 53, 52, 40);

        validatePath(4, firstPathOption);
    }

    @Test
    public void validate29To28() {
        final List<Integer> firstPathOption = createPathCandidate(29, 28);

        validatePath(5, firstPathOption);
    }

    @Test
    public void validate29To29() {
        final List<Integer> firstPathOption = createPathCandidate(29);

        validatePath(6, firstPathOption);
    }

    @Test
    public void validate29To30() {
        final List<Integer> firstPathOption = createPathCandidate(29, 30);

        validatePath(7, firstPathOption);
    }

    @Test
    public void validate29To32() {
        final List<Integer> firstPathOption = createPathCandidate(29, 48, 49, 32);

        validatePath(8, firstPathOption);
    }

    @Test
    public void validate29To40() {
        final List<Integer> firstPathOption = createPathCandidate(29, 48, 55, 57, 52, 40);

        validatePath(9, firstPathOption);
    }

    @Test
    public void validate30To28() {
        final List<Integer> firstPathOption = createPathCandidate(30, 47, 28);

        validatePath(10, firstPathOption);
    }

    @Test
    public void validate30To29() {
        final List<Integer> firstPathOption = createPathCandidate(30, 29);

        validatePath(11, firstPathOption);
    }

    @Test
    public void validate30To30() {
        final List<Integer> firstPathOption = createPathCandidate(30);

        validatePath(12, firstPathOption);
    }

    @Test
    public void validate30To32() {
        final List<Integer> firstPathOption = createPathCandidate(30, 48, 49, 32);

        validatePath(13, firstPathOption);
    }

    @Test
    public void validate30To40() {
        final List<Integer> firstPathOption = createPathCandidate(30, 48, 55, 57, 52, 40);

        validatePath(14, firstPathOption);
    }

    @Test
    public void validate32To28() {
        final List<Integer> firstPathOption = createPathCandidate(32, 49, 48, 47, 28);

        validatePath(15, firstPathOption);
    }

    @Test
    public void validate32To29() {
        final List<Integer> firstPathOption = createPathCandidate(32, 49, 48, 29);

        validatePath(16, firstPathOption);
    }

    @Test
    public void validate32To30() {
        final List<Integer> firstPathOption = createPathCandidate(32, 49, 48, 30);

        validatePath(17, firstPathOption);
    }

    @Test
    public void validate32To32() {
        final List<Integer> firstPathOption = createPathCandidate(32);

        validatePath(18, firstPathOption);
    }

    @Test
    public void validate32To40() {
        final List<Integer> firstPathOption = createPathCandidate(32, 49, 50, 51, 52, 40);

        validatePath(19, firstPathOption);
    }

    @Test
    public void validate40To28() {
        final List<Integer> firstPathOption = createPathCandidate(40, 52, 53, 46, 28);

        validatePath(20, firstPathOption);
    }

    @Test
    public void validate40To29() {
        final List<Integer> firstPathOption = createPathCandidate(40, 52, 57, 55, 48, 29);

        validatePath(21, firstPathOption);
    }

    @Test
    public void validate40To30() {
        final List<Integer> firstPathOption = createPathCandidate(40, 52, 57, 55, 48, 30);

        validatePath(22, firstPathOption);
    }

    @Test
    public void validate40To32() {
        final List<Integer> firstPathOption = createPathCandidate(40, 52, 51, 50, 49, 32);

        validatePath(23, firstPathOption);
    }

    @Test
    public void validate40To40() {
        final List<Integer> firstPathOption = createPathCandidate(40);

        validatePath(24, firstPathOption);
    }

    private void validatePath(final int pathIndex, List<Integer>... possiblePaths) {
        final List<Integer> nodesInPathOrder = this.retrieveFoundPathsNode(pathIndex);
        OneToManyRoutingTest.validatePath(nodesInPathOrder, possiblePaths);
    }

    private List<Integer> retrieveFoundPathsNode(final int index) {
        return this.manyToManyRouting.getAllFoundPaths().get(index).getNodesInPathOrder();
    }

    private List<Integer> createPathCandidate(Integer... nodeIdsOnPath) {
        return new ArrayList<>(Arrays.asList(nodeIdsOnPath));
    }
}
