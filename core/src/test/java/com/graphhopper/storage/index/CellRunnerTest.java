package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.Edge;
import com.graphhopper.routing.template.util.Node;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CellRunnerTest {
    private static PolygonRoutingTestGraph GRAPH_MOCKER = new PolygonRoutingTestGraph(PolygonRoutingTestGraph.getDefaultNodeList(), PolygonRoutingTestGraph.getDefaultEdgeList());

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();


    @Test
    public void simpleCell17to26Left() {
        final Polygon expectedCellShape = new Polygon(new double[]{7, 3, 7}, new double[]{38, 33, 32});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 17, 26);
        final CellRunner cr = new CellRunnerLeft(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsLeft);

        final VisibilityCell vc = cr.extractVisibilityCell();
        assertEquals(expectedCellShape, vc.cellShape);

        visibilityManagerAsserts17to26Left(cti);
    }

    @Test
    public void simpleCell17to26LeftInverseEdge() {
        final Polygon expectedCellShape = new Polygon(new double[]{7, 3, 7}, new double[]{38, 33, 32});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 26, 17);
        final CellRunner cr = new CellRunnerLeft(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsLeft);

        final VisibilityCell vc = cr.extractVisibilityCell();
        assertEquals(expectedCellShape, vc.cellShape);

        visibilityManagerAsserts17to26Left(cti);
    }

    private void visibilityManagerAsserts17to26Left(CellRunnerTestInputs cti) {
        assertWalkedEdgesMarkedAsVisited17to26Left(cti);
        assertExploredButNotWalkedEdgesNotVisited17to26Left(cti);
        assertNoViewedEdgeSettledForRightRun17to26Left(cti);
    }

    private void assertWalkedEdgesMarkedAsVisited17to26Left(CellRunnerTestInputs cti) {
        assertTrue(cti.visitedManagerDual.isEdgeSettledLeft(cti.startingEdge));
        assertTrue(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(26, 18)));
        assertTrue(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(18, 17)));
    }

    private void assertExploredButNotWalkedEdgesNotVisited17to26Left(CellRunnerTestInputs cti) {
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(26, 35)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(18, 14)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(18, 15)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(18, 27)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(18, 100)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(18, 108)));
    }

    private void assertNoViewedEdgeSettledForRightRun17to26Left(CellRunnerTestInputs cti) {
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.startingEdge));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(26, 18)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(18, 17)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(26, 35)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(18, 14)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(18, 15)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(18, 27)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(18, 100)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(18, 108)));
    }

    @Test
    public void simpleCell17to26Right() {
        final Polygon expectedCellShape = new Polygon(new double[]{7, 3, 6}, new double[]{32, 33, 25});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 17, 26);
        final CellRunner cr = new CellRunnerRight(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsRight);

        final VisibilityCell vc = cr.extractVisibilityCell();
        assertEquals(expectedCellShape, vc.cellShape);

        visibilityManagerAsserts17to26Right(cti);
    }

    @Test
    public void simpleCell17to26RightInverseEdge() {
        final Polygon expectedCellShape = new Polygon(new double[]{7, 3, 6}, new double[]{32, 33, 25});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 26, 17);
        final CellRunner cr = new CellRunnerRight(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsRight);

        final VisibilityCell vc = cr.extractVisibilityCell();
        assertEquals(expectedCellShape, vc.cellShape);

        visibilityManagerAsserts17to26Right(cti);
    }

    private void visibilityManagerAsserts17to26Right(CellRunnerTestInputs cti) {
        assertWalkedEdgesMarkedAsVisited17to26Right(cti);
        assertExploredButNotWalkedEdgesNotVisited17to26Right(cti);
        assertNoViewedEdgeSettledForRightRun17to26Right(cti);
    }

    private void assertWalkedEdgesMarkedAsVisited17to26Right(CellRunnerTestInputs cti) {
        assertTrue(cti.visitedManagerDual.isEdgeSettledRight(cti.startingEdge));
        assertTrue(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(26, 35)));
        assertTrue(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(35, 17)));
    }

    private void assertExploredButNotWalkedEdgesNotVisited17to26Right(CellRunnerTestInputs cti) {
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(17, 18)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(17, 15)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(17, 34)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(26, 18)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(35, 25)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(35, 34)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(35, 36)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledRight(cti.getEdge(35, 50)));
    }

    private void assertNoViewedEdgeSettledForRightRun17to26Right(CellRunnerTestInputs cti) {
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.startingEdge));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(26, 35)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(35, 17)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(17, 18)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(17, 15)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(17, 34)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(26, 18)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(35, 25)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(35, 34)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(35, 36)));
        assertFalse(cti.visitedManagerDual.isEdgeSettledLeft(cti.getEdge(35, 50)));
    }

    @Test
    public void duplicateCoordinatesTriangleStartedOnNonDuplicatedCoordinates() {
        final Polygon expectedCellShape = new Polygon(new double[]{15, 10, 11}, new double[]{43, 47, 43});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 14, 106);
        final CellRunner cr = new CellRunnerRight(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsRight);

        final VisibilityCell vc = cr.extractVisibilityCell();
        assertEquals(expectedCellShape, vc.cellShape);
    }

    @Test
    public void duplicateCoordinatesTriangleStartedOnAdjNodeHasDuplicate() {
        final Polygon expectedCellShape = new Polygon(new double[]{10, 11, 15}, new double[]{47, 43, 43});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 106, 110);
        final CellRunner cr = new CellRunnerRight(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsRight);

        final VisibilityCell vc = cr.extractVisibilityCell();
        assertEquals(expectedCellShape, vc.cellShape);
    }

    @Test
    public void duplicateCoordinatesTriangleStartedOnBothNodesHasDuplicateLeft() {
        final Polygon expectedCellShape = new Polygon(new double[]{15, 10, 11, 9, 11, 11}, new double[]{43, 47, 43, 41, 43, 43});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 109, 110);
        final CellRunner cr = new CellRunnerLeft(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsLeft);

        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Cannot start run on an edge with equal coordinates on both end nodes");
        final VisibilityCell vc = cr.extractVisibilityCell();
        assertEquals(expectedCellShape, vc.cellShape);
    }

    @Test
    public void duplicateCoordinatesTriangleStartedOnBothNodesHasDuplicateRight() {
        final double[] expectedCellShapeLatitudes = {11.0, 11.0, 9.0, 11.0, 10.0, 3.0, 3.0, 7.0, 7.0, 5.0, 7.0, 9.0, 7.0, 7.0, 15.0};
        final double[] expectedCellShapeLongitudes = {43.0, 43.0, 41.0, 43.0, 47.0, 47.0, 41.0, 38.0, 42.0, 44.0, 42.0, 44.0, 42.0, 38.0, 43.0};
        final Polygon expectedCellShape = new Polygon(expectedCellShapeLatitudes, expectedCellShapeLongitudes);

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 109, 110);
        final CellRunner cr = new CellRunnerRight(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsRight);

        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Cannot start run on an edge with equal coordinates on both end nodes");
        final VisibilityCell vc = cr.extractVisibilityCell();
        assertEquals(expectedCellShape, vc.cellShape);
    }

    //    @Test
    public void duplicateStartEdge() {
        GRAPH_MOCKER.graph.edge(17, 26, 1, true);

        final Polygon expectedCellShape = new Polygon(new double[]{7, 3, 7}, new double[]{38, 33, 32});

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(GRAPH_MOCKER, 17, 26);
        final CellRunner cr = new CellRunnerLeft(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsLeft);

        final VisibilityCell vc = cr.extractVisibilityCell();
        assertEquals(expectedCellShape, vc.cellShape);

        GRAPH_MOCKER = new PolygonRoutingTestGraph(PolygonRoutingTestGraph.getDefaultNodeList(), PolygonRoutingTestGraph.getDefaultEdgeList());
    }

    @Test
    public void collinearEdges() {
        final Polygon expectedCellShape = new Polygon(new double[]{0.0, 0.0, -1.0, -1.0, 0.0, 0.0, 0.0}, new double[]{4.0, 3.0, 3.0, 5.0, 5.0, 4.0, 2.0});

        final PolygonRoutingTestGraph customTestGraph = createCustomTestGraphToTryTrapTheAlgorithmInEndlessLoop();

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(customTestGraph, 0, 1);
        final CellRunner cr = new CellRunnerLeft(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsLeft);
        final VisibilityCell vc = cr.extractVisibilityCell();

        assertEquals(expectedCellShape, vc.cellShape);
    }

    private PolygonRoutingTestGraph createCustomTestGraphToTryTrapTheAlgorithmInEndlessLoop() {
        final Node[] nodes = new Node[]{
                new Node(0, 0, 2), //s
                new Node(1, 0, 4), //v
                new Node(2, 0, 5), //w
                new Node(3, -1, 5),
                new Node(4, -1, 3),
                new Node(5, 0, 3)  //u
        };
        final Edge[] edges = new Edge[]{
                new Edge(0, 1, 1, true),
                new Edge(1, 2, 1, true),
                new Edge(2, 3, 1, true),
                new Edge(3, 4, 1, true),
                new Edge(4, 5, 1, true),
                new Edge(5, 1, 1, true)
        };
        return new PolygonRoutingTestGraph(nodes, edges);
    }

    @Test
    public void startOnImpasse() {
        final Polygon expectedCellShape = new Polygon(new double[]{0, 0, 0, 0}, new double[]{1, 2, 1, 0});

        final PolygonRoutingTestGraph customTestGraph = createSimpleImpasseTestGraph();

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(customTestGraph, 0, 1);
        final CellRunner cr = new CellRunnerLeft(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsLeft);
        final VisibilityCell vc = cr.extractVisibilityCell();

        assertEquals(expectedCellShape, vc.cellShape);
    }

    private PolygonRoutingTestGraph createSimpleImpasseTestGraph() {
        final Node[] nodes = new Node[]{
                new Node(0, 0, 0),
                new Node(1, Double.MIN_VALUE, 1),
                new Node(2, 0, 2)
        };
        final Edge[] edges = new Edge[]{
                new Edge(0, 1, 1, true),
                new Edge(1, 2, 1, true)
        };
        return new PolygonRoutingTestGraph(nodes, edges);
    }

    @Test
    public void startOnImpasse2() {
        final Polygon expectedCellShape = new Polygon(new double[]{0, -1, -1, 0, 0, 0, 0}, new double[]{3, 3, 5, 5, 4, 3, 2});

        final PolygonRoutingTestGraph customTestGraph = createAdvancedImpasseTestGraph();

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(customTestGraph, 0, 5);
        final CellRunner cr = new CellRunnerLeft(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsLeft);
        final VisibilityCell vc = cr.extractVisibilityCell();

        assertEquals(expectedCellShape, vc.cellShape);
    }

    private PolygonRoutingTestGraph createAdvancedImpasseTestGraph() {
        final Node[] nodes = new Node[]{
                new Node(0, 0, 2),
                new Node(1, 0, 4),
                new Node(2, 0, 5),
                new Node(3, -1, 5),
                new Node(4, -1, 3),
                new Node(5, 0, 3)
        };
        final Edge[] edges = new Edge[]{
                new Edge(0, 5, 1, true),
                new Edge(1, 2, 1, true),
                new Edge(2, 3, 1, true),
                new Edge(3, 4, 1, true),
                new Edge(4, 5, 1, true),
                new Edge(5, 1, 1, true)
        };
        return new PolygonRoutingTestGraph(nodes, edges);
    }

    @Test
    public void twoLength1ImpassesInARow() {
        final double[] expectedCellShapeLatitudes = {0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        final double[] expectedCellShapeLongitudes = {1.0, 2.0, 2.0, 2.0, 3.0, 2.0, 1.0, 0.0};
        final Polygon expectedCellShape = new Polygon(expectedCellShapeLatitudes, expectedCellShapeLongitudes);
        final PolygonRoutingTestGraph customTestGraph = twoImpassesInARowGraph();

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(customTestGraph, 0, 1);
        final CellRunner cr = new CellRunnerLeft(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsLeft);
        final VisibilityCell vc = cr.extractVisibilityCell();

        assertEquals(expectedCellShape, vc.cellShape);
    }

    private PolygonRoutingTestGraph twoImpassesInARowGraph() {
        final Node[] nodes = new Node[]{
                new Node(0, 0, 0),
                new Node(1, 0, 1),
                new Node(2, 0, 2),
                new Node(3, -1, 2),
                new Node(100, -1, 2),
                new Node(4, 0, 3)
        };

        final Edge[] edges = new Edge[]{
                new Edge(0, 1, 1, true),
                new Edge(1, 2, 1, true),
                new Edge(2, 3, 1, true),
                new Edge(2, 4, 1, true),
                new Edge(3, 100, 1, true),
                new Edge(100, 2, 1, true),
                };

        return new PolygonRoutingTestGraph(nodes, edges);
    }

    @Test
    public void twoNodesSameCoordinatesButNoEdgeBetween() {
        final Polygon expectedCellShape = new Polygon(new double[]{0.0, -1.0, 0.0, -1.0, 0.0, 0.0}, new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 0.0});
        final PolygonRoutingTestGraph customTestGraph = createTwoNodesSameCoordinatesNoEdgeTestGraph();

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(customTestGraph, 0, 1);
        final CellRunner cr = new CellRunnerLeft(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsLeft);
        final VisibilityCell vc = cr.extractVisibilityCell();

        assertEquals(expectedCellShape, vc.cellShape);
    }

    private PolygonRoutingTestGraph createTwoNodesSameCoordinatesNoEdgeTestGraph() {
        final Node[] nodes = new Node[]{
                new Node(0, 0, 0),
                new Node(1, 0, 1),
                new Node(2, 0, 1),
                new Node(3, -1, 1)
        };
        final Edge[] edges = new Edge[]{
                new Edge(0, 1, 1, true),
                new Edge(1, 3, 1, true),
                new Edge(2, 3, 1, true)
        };
        return new PolygonRoutingTestGraph(nodes, edges);
    }

    @Test
    public void collinearEdgeWhereNextNodeHintShallNotBeTaken() {
        final Polygon expectedCellShape = new Polygon(new double[]{0.0, -1.0, -2.0, -3.0, -2.0, -1.0, 0.0}, new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0});
        final PolygonRoutingTestGraph customTestGraph = collinearEdgeWhereNextNodeHintShallNotBeTakenGraph();

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(customTestGraph, 0, 2);
        final CellRunner cr = new CellRunnerLeft(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsLeft);

        final VisibilityCell vc = cr.extractVisibilityCell();

        assertEquals(expectedCellShape, vc.cellShape);
    }

    private PolygonRoutingTestGraph collinearEdgeWhereNextNodeHintShallNotBeTakenGraph() {
        final Node[] nodes = new Node[]{
                new Node(0, 0, -1),
                new Node(1, 0, 1),
                new Node(2, -1, 0),
                new Node(3, -1, 0),
                new Node(4, -2, 0),
                new Node(5, -3, 0)
        };
        final Edge[] edges = new Edge[]{
                new Edge(0, 2, 1, true),
                new Edge(2, 4, 1, true),
                new Edge(4, 5, 1, true),
                new Edge(4, 3, 1, true),
                new Edge(3, 1, 1, true),
                new Edge(1, 0, 1, true)
        };
        return new PolygonRoutingTestGraph(nodes, edges);
    }

    @Test
    public void collinearityStackOverFlowLeft() {
        final Polygon expectedCellShape = new Polygon(new double[]{-1.0, -3.0, -4.0, -2.0, -1.0, 0.0}, new double[]{1.0, 0.0, 1.0, 1.0, 1.0, 1.0});
        final PolygonRoutingTestGraph customTestGraph = collinearEdgeWithNoOtherNeighborsThanBackwardsTestGraph();

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(customTestGraph, 1, 2);
        final CellRunner cr = new CellRunnerLeft(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsLeft);

        final VisibilityCell vc = cr.extractVisibilityCell();

        assertEquals(expectedCellShape, vc.cellShape);
    }

    @Test
    public void collinearityStackOverFlowRight() {
        final Polygon expectedCellShape = new Polygon(new double[]{0.0, -1.0, -3.0, -4.0, -3.0, -2.0, -1.0}, new double[]{1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0});
        final PolygonRoutingTestGraph customTestGraph = collinearEdgeWithNoOtherNeighborsThanBackwardsTestGraph();

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(customTestGraph, 1, 2);
        final CellRunner cr = new CellRunnerRight(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsRight);

        final VisibilityCell vc = cr.extractVisibilityCell();

        assertEquals(expectedCellShape, vc.cellShape);
    }

    private PolygonRoutingTestGraph collinearEdgeWithNoOtherNeighborsThanBackwardsTestGraph() {
        final Node[] nodes = new Node[]{
                new Node(1, 0, 1),
                new Node(2, -1, 1),
                new Node(3, -2, 1),
                new Node(4, -3, 1),
                new Node(5, -4, 1),
                new Node(6, -3, 0)
        };
        final Edge[] edges = new Edge[]{
                new Edge(1, 2, 1, true),
                new Edge(2, 3, 1, true),
                new Edge(2, 6, 1, true),
                new Edge(3, 4, 1, true),
                new Edge(3, 5, 1, true),
                new Edge(4, 5, 1, true),
                new Edge(4, 6, 1, true),
                new Edge(5, 6, 1, true)
        };
        return new PolygonRoutingTestGraph(nodes, edges);
    }

    @Test
    public void issueOnEdgeGermanyLeft() {
        final double[] expectedLatitudes = {-1.0, -2.0, -2.0, -2.0, -2.0, -2.0, -1.0, -1.0, -1.0, 0.0};
        final double[] expectedLongitudes = {0.0, 0.0, -1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0};
        final Polygon expectedCellShape = new Polygon(expectedLatitudes, expectedLongitudes);
        final PolygonRoutingTestGraph customTestGraph = issueOnEdgeGermanyTestGraph();

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(customTestGraph, 0, 1);
        final CellRunner cr = new CellRunnerLeft(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsLeft);

        final VisibilityCell vc = cr.extractVisibilityCell();

        assertEquals(expectedCellShape, vc.cellShape);
    }

    @Test
    public void issueOnEdgeGermanyRight() {
        final double[] expectedLatitudes = {0.0, -1.0, -2.0, -2.0, -2.0, -2.0, -2.0, -1.0, -1.0, -1.0};
        final double[] expectedLongitudes = {0.0, 0.0, 0.0, -1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0};
        final Polygon expectedCellShape = new Polygon(expectedLatitudes, expectedLongitudes);
        final PolygonRoutingTestGraph customTestGraph = issueOnEdgeGermanyTestGraph();

        final CellRunnerTestInputs cti = new CellRunnerTestInputs(customTestGraph, 0, 1);
        final CellRunner cr = new CellRunnerRight(cti.graph, cti.visitedManagerDual, cti.startingEdge, cti.preSortedNeighborsRight);

        final VisibilityCell vc = cr.extractVisibilityCell();

        assertEquals(expectedCellShape, vc.cellShape);
    }

    private PolygonRoutingTestGraph issueOnEdgeGermanyTestGraph() {
        final Node[] nodes = new Node[]{
                new Node(0, 0, 0),
                new Node(1, -1, 0),
                new Node(2, -2, 0),
                new Node(3, -2, 1),
                new Node(4, -1, 1),
                new Node(5, -2, 0),
                new Node(6, -2, -1)
        };
        final Edge[] edges = new Edge[]{
                new Edge(0, 1, 1, true),
                new Edge(1, 2, 1, true),
                new Edge(1, 4, 1, true),
                new Edge(2, 3, 1, true),
                new Edge(2, 5, 1, true),
                new Edge(2, 6, 1, true)
        };
        return new PolygonRoutingTestGraph(nodes, edges);
    }

    public static class CellRunnerTestInputs {
        private final PolygonRoutingTestGraph graphMocker;
        public final Graph graph;
        public final VisitedManagerDual visitedManagerDual;
        public final EdgeIteratorState startingEdge;
        public final Map<Integer, SortedNeighbors> preSortedNeighborsLeft;
        public final Map<Integer, SortedNeighbors> preSortedNeighborsRight;

        public CellRunnerTestInputs(final PolygonRoutingTestGraph graphMocker, final int startBaseNode, final int startAdjNode) {
            this.graphMocker = graphMocker;
            this.graph = graphMocker.graph;
            this.visitedManagerDual = new VisitedManagerDual(graphMocker.graph);
            this.startingEdge = getEdge(startBaseNode, startAdjNode);

            final NeighborPreSorter neighborPreSorter = new NeighborPreSorter(graph);
            this.preSortedNeighborsLeft = neighborPreSorter.getAllSortedNeighborsLeft();
            this.preSortedNeighborsRight = neighborPreSorter.getAllSortedNeighborsRight();
        }

        public EdgeIteratorState getEdge(final int startBaseNode, final int startAdjNode) {
            final List<EdgeIteratorState> edges = graphMocker.getAllEdges();

            for (EdgeIteratorState edge : edges) {
                if (edge.getBaseNode() == startBaseNode && edge.getAdjNode() == startAdjNode) {
                    return edge;
                }

                edge = edge.detach(true);
                if (edge.getBaseNode() == startBaseNode && edge.getAdjNode() == startAdjNode) {
                    return edge;
                }
            }

            throw new IllegalArgumentException("Edge doesn't exist.");
        }
    }
}
