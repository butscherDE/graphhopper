package com.graphhopper.routing;

import com.graphhopper.routing.ch.PreparationWeighting;
import com.graphhopper.routing.template.util.Edge;
import com.graphhopper.routing.template.util.Node;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RPHASTTest {
    private final static PolygonRoutingTestGraph GRAPH_MOCKER = PolygonRoutingTestGraph.DEFAULT_INSTANCE;

    @Rule
    public final ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void printAllCHGraphEdges() {
        final EdgeIterator allEdges = GRAPH_MOCKER.graphWithCh.getCHGraph().getAllEdges();

        while(allEdges.next()) {
            System.out.println(allEdges.toString());
        }
    }

    @Test
    public void weightingLearningTest() {
        final CHGraph chGraph = GRAPH_MOCKER.graphWithCh.getCHGraph();
        final Weighting weighting = GRAPH_MOCKER.weighting;
        final Weighting prepWeighting = new PreparationWeighting(weighting);

        CHEdgeIteratorState edge1to2 = chGraph.getEdgeIteratorState(3, 2);
        CHEdgeIteratorState edge1to3 = chGraph.getEdgeIteratorState(174, 3);
        CHEdgeIteratorState edge1to28 = chGraph.getEdgeIteratorState(7, 28);
        CHEdgeIteratorState edge1to29 = chGraph.getEdgeIteratorState(183, 29);
        CHEdgeIteratorState edge2to3 = chGraph.getEdgeIteratorState(8, 3);
        CHEdgeIteratorState edge28to29 = chGraph.getEdgeIteratorState(73, 29);
        CHEdgeIteratorState edge29to3 = chGraph.getEdgeIteratorState(12, 3);

        double weight1to2 = weighting.calcWeight(edge1to2, false, new RPHAST.NonExistentEdge(1).getEdge());
        double weight1to3 = prepWeighting.calcWeight(edge1to3, false, new RPHAST.NonExistentEdge(1).getEdge());
        double weight1to28 = weighting.calcWeight(edge1to28, false, new RPHAST.NonExistentEdge(1).getEdge());
        double weight1to29 = prepWeighting.calcWeight(edge1to29, false, new RPHAST.NonExistentEdge(1).getEdge());
        double weight2to3 = weighting.calcWeight(edge2to3, false, new RPHAST.NonExistentEdge(2).getEdge());
        double weight28to29 = weighting.calcWeight(edge28to29, false, new RPHAST.NonExistentEdge(29).getEdge());
        double weight29to3 = weighting.calcWeight(edge29to3, false, new RPHAST.NonExistentEdge(29).getEdge());

        System.out.println("weight1to2: " + weight1to2);
        System.out.println("weight1to3: " + weight1to3);
        System.out.println("weight1to28: " + weight1to28);
        System.out.println("weight1to29: " + weight1to29);
        System.out.println("weight2to3: " + weight2to3);
        System.out.println("weight28to29: " + weight28to29);
        System.out.println("weight29to3: " + weight29to3);

        System.out.println("1-2-3: " + (weight1to2 + weight2to3));
        System.out.println("1-3: " + weight1to3);
        System.out.println("1-28-29-3: " + (weight1to28 + weight28to29 + weight29to3));
        System.out.println("1-29-3: " + (weight1to29 + weight29to3));

        // Distances of shortcuts shall be equal to single edges distances
        assertEquals(edge1to2.getDistance() + edge2to3.getDistance(), edge1to3.getDistance(), 0.001);
        assertEquals(edge1to28.getDistance() + edge28to29.getDistance(), edge1to29.getDistance(), 0.001);

        // Euclidean distance -> direct path 1-2-3 must be smaller or equal than any other path.
        assertTrue(weight1to28 + weight28to29 + weight29to3 >= weight1to2 + weight2to3);
        // Same as before but with shortcuts
        assertTrue(weight1to29 + weight29to3 >= weight1to3);

        // Shortcuts shall have equal costs to the edges they were built from
        assertEquals(weight1to2 + weight2to3, weight1to3, 0);
        assertEquals(weight1to28 + weight28to29, weight1to29, 0.001);
    }

    @Test
    public void shortcutLearningTest() {
        final Weighting weighting = new PreparationWeighting(GRAPH_MOCKER.weighting);
        final EdgeIterator incidenceExplorer = GRAPH_MOCKER.graphWithCh.getCHGraph().createEdgeExplorer().setBaseNode(1);
        while (incidenceExplorer.getAdjNode() != 29) {
            incidenceExplorer.next();
        }

        final double weight1to29_1 = weighting.calcWeight(incidenceExplorer, false, EdgeIterator.NO_EDGE);
        final double weight1to29_2 = weighting.calcWeight(incidenceExplorer.detach(false), false, EdgeIterator.NO_EDGE);
        final double weight1to29_3 = weighting.calcWeight(incidenceExplorer.detach(true), false, EdgeIterator.NO_EDGE);

        assertEquals(weight1to29_1, weight1to29_2, 0);
        assertEquals(weight1to29_1, weight1to29_3, 0);

        int edgeId = incidenceExplorer.getEdge();
        int detachedEdgeId = incidenceExplorer.detach(false).getEdge();
        int detachedReveredEdgeID = incidenceExplorer.detach(true).getEdge();

        assertEquals(edgeId, detachedEdgeId);
        assertEquals(edgeId, detachedReveredEdgeID);
    }

    @Test
    public void shortcutLearningTest2() {
        final Weighting weighting = GRAPH_MOCKER.weighting;
        final Weighting prepWeighting = new PreparationWeighting(weighting);
        final Graph graph = GRAPH_MOCKER.graphWithCh.getCHGraph();
        final EdgeIterator incidenceExplorer = graph.createEdgeExplorer().setBaseNode(1);
        EdgeIteratorState edge1to28 = null;
        EdgeIteratorState edge1to29 = null;
        while (incidenceExplorer.next()) {
            if (incidenceExplorer.getAdjNode() == 28) {
                edge1to28 = incidenceExplorer.detach(false);
            }
            if (incidenceExplorer.getAdjNode() == 29) {
                edge1to29 = incidenceExplorer.detach(false);
            }
        }

        EdgeIteratorState edge28to29 = getEdge28to29(graph);

        final double weight1to28 = weighting.calcWeight(edge1to28, false, EdgeIterator.NO_EDGE);
        final double weight28to29 = weighting.calcWeight(edge28to29, false, EdgeIterator.NO_EDGE);
        final double weight1to29 = prepWeighting.calcWeight(edge1to29, false, EdgeIterator.NO_EDGE);

        assertEquals(7, edge1to28.getEdge());
        assertEquals(73, edge28to29.getEdge());
        assertEquals(183, edge1to29.getEdge());
        assertEquals(weight1to28 + weight28to29, weight1to29, 0.001);
    }

    private EdgeIteratorState getEdge28to29(final Graph graph) {
        final EdgeIterator incidenceExplorer2 = graph.createEdgeExplorer().setBaseNode(28);
        EdgeIteratorState edge28to29 = null;
        while (incidenceExplorer2.next()) {
            if (incidenceExplorer2.getAdjNode() == 29) {
                edge28to29 = incidenceExplorer2.detach(false);
            }
        }
        return edge28to29;
    }

    @Test
    public void failIfTargetSetNotPrepared() {
        final RPHAST rphast = new RPHAST(GRAPH_MOCKER.graphWithCh, GRAPH_MOCKER.weighting, EdgeFilter.ALL_EDGES);

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Call prepareForTagetSet first");
        rphast.calcPaths(0);
    }

    @Test
    public void prepareForTargetSetNodeDoesntExist() {
        final Set<Integer> targetSet = new LinkedHashSet<>(Arrays.asList(0,300));

        final RPHAST rphast = new RPHAST(GRAPH_MOCKER.graphWithCh, GRAPH_MOCKER.weighting, EdgeFilter.ALL_EDGES);
        rphast.prepareForTargetSet(targetSet);
    }

    @Test
    public void queryValidSourceTargetSetNumberOfPathsFound() {
        final List<Integer> sourceList = Arrays.asList(0, 1);
        final Set<Integer> targetSet = new LinkedHashSet<>(Arrays.asList(5,6));

        final RPHAST rphast = new RPHAST(GRAPH_MOCKER.graphWithCh, GRAPH_MOCKER.weighting, EdgeFilter.ALL_EDGES);
        rphast.prepareForTargetSet(targetSet);

        final List<Path> paths = rphast.calcPaths(sourceList);
        assertEquals(4, paths.size());
    }

    @Test
    public void queryValidSourceTargetPathStarts() {
        final List<Integer> sourceList = Arrays.asList(0, 1);
        final Set<Integer> targetSet = new LinkedHashSet<>(Arrays.asList(5,6));

        final RPHAST rphast = new RPHAST(GRAPH_MOCKER.graphWithCh, GRAPH_MOCKER.weighting, EdgeFilter.ALL_EDGES);
        rphast.prepareForTargetSet(targetSet);

        final List<Path> paths = rphast.calcPaths(sourceList);
        assertEquals(0, paths.get(0).fromNode);
        assertEquals(0, paths.get(1).fromNode);
        assertEquals(1, paths.get(2).fromNode);
        assertEquals(1, paths.get(3).fromNode);

    }

    @Test
    public void queryValidSourceTargetPathEnds() {
        final List<Integer> sourceList = Arrays.asList(0, 1);
        final Set<Integer> targetSet = new LinkedHashSet<>(Arrays.asList(5,6));

        final RPHAST rphast = new RPHAST(GRAPH_MOCKER.graphWithCh, GRAPH_MOCKER.weighting, EdgeFilter.ALL_EDGES);
        rphast.prepareForTargetSet(targetSet);

        final List<Path> paths = rphast.calcPaths(sourceList);
        assertEquals(5, paths.get(0).endNode);
        assertEquals(6, paths.get(1).endNode);
        assertEquals(5, paths.get(2).endNode);
        assertEquals(6, paths.get(3).endNode);
    }

    @Test
    public void queryValidSourceTargetPathEdgeIds() {
        final int[] expectedEdgesPath0to6 = new int[] {0, 174, 11, 15, 19};
        final int[] expectedEdgesPath0to5 = new int[] {0, 174, 11, 15};
        final int[] expectedDistancesPath1to5 = new int[] {174, 11, 15};
        final int[] expectedDistancesPath1to6 = new int[] {174, 11, 15, 19};


        final List<Integer> sourceList = Arrays.asList(0, 1);
        final Set<Integer> targetSet = new LinkedHashSet<>(Arrays.asList(5,6));

        final RPHAST rphast = new RPHAST(GRAPH_MOCKER.graphWithCh, GRAPH_MOCKER.weighting, EdgeFilter.ALL_EDGES);
        rphast.prepareForTargetSet(targetSet);

        final List<Path> paths = rphast.calcPaths(sourceList);
        assertPath(expectedEdgesPath0to5, paths.get(0));
        assertPath(expectedEdgesPath0to6, paths.get(1));
        assertPath(expectedDistancesPath1to5, paths.get(2));
        assertPath(expectedDistancesPath1to6, paths.get(3));
    }

    private void assertPath(final int[] expectedEdges, final Path path) {
        assertEquals(expectedEdges.length, path.edgeIds.size());
        for (int i = 0; i < expectedEdges.length; i++) {
            assertEquals("Element " + i, expectedEdges[i], path.edgeIds.get(i));
        }
    }

    private double getPath0to5Distance() {
        double distance = 0;

        distance += GRAPH_MOCKER.getEdge(0,1).getDistance();
        distance += GRAPH_MOCKER.getEdge(1,2).getDistance();
        distance += GRAPH_MOCKER.getEdge(2,3).getDistance();
        distance += GRAPH_MOCKER.getEdge(3,4).getDistance();
        distance += GRAPH_MOCKER.getEdge(4,5).getDistance();

        return distance;
    }

    private double getPath0to6Distance() {
        return getPath0to5Distance() + GRAPH_MOCKER.getEdge(5,6).getDistance();
    }

    @Test
    public void queryNonExistentTarget() {
        final List<Integer> sourceList = Collections.singletonList(0);
        final Set<Integer> targetSet = Collections.singleton(300);

        final RPHAST rphast = new RPHAST(GRAPH_MOCKER.graphWithCh, GRAPH_MOCKER.weighting, EdgeFilter.ALL_EDGES);
        rphast.prepareForTargetSet(targetSet);

        final List<Path> paths = rphast.calcPaths(sourceList);
        assertEquals(Double.MAX_VALUE, paths.get(0).getWeight(), 0);
    }

    @Test
    public void queryNonExistentSource() {
        final List<Integer> sourceList = Collections.singletonList(300);
        final Set<Integer> targetSet = Collections.singleton(0);

        final RPHAST rphast = new RPHAST(GRAPH_MOCKER.graphWithCh, GRAPH_MOCKER.weighting, EdgeFilter.ALL_EDGES);
        rphast.prepareForTargetSet(targetSet);

        final List<Path> paths = rphast.calcPaths(sourceList);
        assertEquals(Double.MAX_VALUE, paths.get(0).getWeight(), 0);
    }

    @Test
    public void queryDisconnectedNodes() {
        final List<Integer> sourceList = Collections.singletonList(0);
        final Set<Integer> targetSet = Collections.singleton(3);

        final PolygonRoutingTestGraph graphMocker = getDisconnectedGraphMocker();
        final RPHAST rphast = new RPHAST(graphMocker.graphWithCh, graphMocker.weighting, EdgeFilter.ALL_EDGES);
        rphast.prepareForTargetSet(targetSet);

        final List<Path> paths = rphast.calcPaths(sourceList);
        assertEquals(Double.MAX_VALUE, paths.get(0).getWeight(), 0);
    }

    private PolygonRoutingTestGraph getDisconnectedGraphMocker() {
        final Node[] nodes = new Node[] {
                new Node(0, Double.MIN_VALUE, 0),
                new Node(1, 0, 1),
                new Node(2, 0, 2),
                new Node(3, 0, 3)
        };
        final Edge[] edges = new Edge[] {
                new Edge(0, 1, 1, true),
                new Edge(2, 3, 1, true)
        };
        return new PolygonRoutingTestGraph(nodes, edges);
    }
}
