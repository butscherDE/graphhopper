package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.RoutingAlgorithmFactorySimple;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.template.util.QueryGraphCreator;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.QueryResult;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManyToManyRoutingTest {
    private final static PolygonRoutingTestGraph graphMocker = PolygonRoutingTestGraph.DEFAULT_INSTANCE;
    private static ManyToManyRouting manyToManyRouting;

    @BeforeClass
    public static void setUp() {
        final List<Integer> sourceDestinations = prepareSourceDestination();
        final RegionOfInterestRoutingGraph nodesToConsiderForRouting = new OneToManyRoutingTest().prepareInteriorGraph();
        final RoutingAlgorithmFactory routingAlgorithmFactory = new RoutingAlgorithmFactorySimple();
        final AlgorithmOptions algorithmOptions = graphMocker.algorithmOptions;
        final List<QueryResult> queryResults = createQueryResults(graphMocker.graph, sourceDestinations);

        manyToManyRouting = new ManyToManyRouting(nodesToConsiderForRouting, sourceDestinations, graphMocker.graph, queryResults, routingAlgorithmFactory,
                                                       algorithmOptions);
        manyToManyRouting.findPathBetweenAllNodePairs();
    }

    private static List<QueryResult> createQueryResults(final Graph graph, final List<Integer> nodesToLookup) {
        final QueryGraphCreator queryGraphCreator = new QueryGraphCreator(graph, nodesToLookup);

        return queryGraphCreator.getQueryResults();
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
        final List<Integer> firstPathOption = createPathCandidate(28, 47, 48, 29);

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
        final List<Integer> firstPathOption = createPathCandidate(29, 48, 47, 28);

        validatePath(firstPathOption);
    }

    @Test
    public void validate29To29() {
        final List<Integer> firstPathOption = createPathCandidate(29);

        validatePath(firstPathOption);
    }

    @Test
    public void validate29To30() {
        final List<Integer> firstPathOption = createPathCandidate(29, 48, 30);

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
        final List<Integer> firstPathOption = createPathCandidate(30, 48, 29);

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
        return this.manyToManyRouting.getPathByFromEndNodeID(fromNode, toNode).getNodesInPathOrder();
    }

    private List<Integer> createPathCandidate(Integer... nodeIdsOnPath) {
        return new ArrayList<>(Arrays.asList(nodeIdsOnPath));
    }
}
