package com.graphhopper.routing.template.util;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.storage.index.GridIndex;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.DistanceCalc2D;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.Polygon;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

import static com.graphhopper.util.Parameters.Routing.MAX_VISITED_NODES;

public class PolygonRoutingTestGraph {
    public static PolygonRoutingTestGraph DEFAULT_INSTANCE = new PolygonRoutingTestGraph();

    private final TurnCostExtension turnCostExtension;
    private final Node[] nodes;
    private final Edge[] edges;
    private final DistanceCalc2D distanceCalculator;
    public EncodingManager encodingManager;
    public FlagEncoder flagEncoder;
    public GraphHopperStorage graph;
    public Polygon polygon;
    public LocationIndex locationIndex;
    public NodeAccess nodeAccess;
    public TraversalMode traversalMode;
    public String algorithmName;
    public HintsMap algorithmHints;
    public Weighting weighting;
    public AlgorithmOptions algorithmOptions;

    private PolygonRoutingTestGraph() {
        this(getDefaultNodeList(), getDefaultEdgeList());
    }

    public PolygonRoutingTestGraph(final Node[] nodes, final Edge[] edges) {
        this.nodes = nodes;
        this.edges = edges;

        this.turnCostExtension = new TurnCostExtension();
        this.distanceCalculator = new DistanceCalc2D();
        this.createEncodingManager();
        this.createTestGraph();
        this.createTestPolygon();
        this.createLocationIndex();
        this.getNodeAccess();
        this.setTraversalMode();
        this.setAlgorithmName();
        this.buildHintsMap();
        this.setWeighting();
        this.setAlgorithmOptions();
    }

    private GraphHopperStorage createPolygonTestGraph() {
        this.graph = new GraphHopperStorage(new RAMDirectory(), this.encodingManager, false, turnCostExtension);
        this.graph.create(1000);

        // Exterior this.graph including to Entry / Exit nodes
        buildEdges();
        buildNodes();

        return graph;
    }

    private void buildNodes() {
        setDistanceToEuclidean();
    }

    public static Node[] getDefaultNodeList() {
        return new Node[]{new Node(0, 25, 0),
                          new Node(1, 25, 8),
                          new Node(2, 25, 16),
                          new Node(3, 25, 25),
                          new Node(4, 25, 34),
                          new Node(5, 25, 43),
                          new Node(6, 25, 46),
                          new Node(7, 20, 3),
                          new Node(8, 22, 8),
                          new Node(9, 22, 29),
                          new Node(10, 21, 35),
                          new Node(11, 20, 42),
                          new Node(12, 16, 34),
                          new Node(13, 17, 38),
                          new Node(14, 15, 43),
                          new Node(15, 11, 34),
                          new Node(16, 12, 38),
                          new Node(17, 7, 32),
                          new Node(18, 7, 38),
                          new Node(19, 7, 1),
                          new Node(20, 1, 1),
                          new Node(21, 3, 7),
                          new Node(22, 2, 13),
                          new Node(23, 0, 16),
                          new Node(24, 0, 21),
                          new Node(25, 2, 25),
                          new Node(26, 3, 33),
                          new Node(27, 2, 36),
                          new Node(28, 22, 16),
                          new Node(29, 22, 20),
                          new Node(30, 21, 23),
                          new Node(31, 19, 25),
                          new Node(32, 14, 30),
                          new Node(33, 11, 30),
                          new Node(34, 7, 27),
                          new Node(35, 6, 25),
                          new Node(36, 5, 22),
                          new Node(37, 5, 20),
                          new Node(38, 5, 17),
                          new Node(39, 6, 14),
                          new Node(40, 7, 11),
                          new Node(41, 10, 10),
                          new Node(42, 13, 9),
                          new Node(43, 15, 9),
                          new Node(44, 19, 10),
                          new Node(45, 21, 12),
                          new Node(46, 17, 16),
                          new Node(47, 18, 19),
                          new Node(48, 17, 22),
                          new Node(49, 14, 23),
                          new Node(50, 11, 22),
                          new Node(51, 10, 19),
                          new Node(52, 11, 16),
                          new Node(53, 14, 15),
                          new Node(54, 15, 18),
                          new Node(55, 15, 20),
                          new Node(56, 13, 20),
                          new Node(57, 13, 18),
                          new Node(100, 7, 42),
                          new Node(101, 9, 44),
                          new Node(102, 5, 44),
                          new Node(103, 12, 51),
                          new Node(104, 10, 51),
                          new Node(105, 8, 51),
                          new Node(106, 10, 47),
                          new Node(107, 3, 47),
                          new Node(108, 3, 41),
                          new Node(109, 11, 43),
                          new Node(110, 11, 43),
                          new Node(111, 9, 41),

                          // Multi node test part
                          new Node(200, 19, 28),
                          new Node(201, 19, 28),
                          new Node(202, 19, 31)};
    }

    private void setDistanceToEuclidean() {
        for (Node node : nodes) {
            node.updateDistance(graph);
        }
    }

    public static Edge[] getDefaultEdgeList() {
        return new Edge[]{new Edge(0, 1, 1, true),
                          new Edge(0, 7, 1, true),
                          new Edge(0, 19, 1, true),
                          new Edge(1, 2, 1, true),
                          new Edge(1, 7, 1, true),
                          new Edge(1, 8, 1, true),
                          new Edge(1, 45, 1, true),
                          new Edge(1, 28, 1, true),
                          new Edge(2, 3, 1, true),
                          new Edge(2, 28, 1, true),
                          new Edge(2, 29, 1, true),
                          new Edge(3, 4, 1, true),
                          new Edge(3, 29, 1, true),
                          new Edge(3, 30, 1, true),
                          new Edge(3, 9, 1, true),
                          new Edge(4, 5, 1, true),
                          new Edge(4, 9, 1, true),
                          new Edge(4, 10, 1, true),
                          new Edge(4, 13, 1, true),
                          new Edge(5, 6, 1, true),
                          new Edge(5, 10, 1, true),
                          new Edge(5, 11, 1, true),
                          new Edge(5, 13, 1, true),
                          new Edge(5, 14, 1, true),
                          new Edge(6, 14, 1, true),
                          new Edge(7, 19, 1, true),
                          new Edge(7, 43, 1, true),
                          new Edge(7, 44, 1, true),
                          new Edge(8, 44, 1, true),
                          new Edge(8, 45, 1, true),
                          new Edge(9, 30, 1, true),
                          new Edge(9, 31, 1, true),
                          new Edge(10, 12, 1, true),
                          new Edge(11, 16, 1, true),
                          new Edge(12, 13, 1, true),
                          new Edge(12, 15, 1, true),
                          new Edge(12, 32, 1, true),
                          new Edge(13, 15, 1, true),
                          new Edge(13, 16, 1, true),
                          new Edge(14, 16, 1, true),
                          new Edge(14, 18, 1, true),
                          new Edge(15, 16, 1, true),
                          new Edge(15, 17, 1, true),
                          new Edge(15, 18, 1, true),
                          new Edge(15, 33, 1, true),
                          new Edge(15, 34, 1, true),
                          new Edge(17, 18, 1, true),
                          new Edge(17, 26, 1, true),
                          new Edge(17, 34, 1, true),
                          new Edge(17, 35, 1, true),
                          new Edge(18, 26, 1, true),
                          new Edge(18, 27, 1, true),
                          new Edge(19, 20, 1, true),
                          new Edge(19, 21, 1, true),
                          new Edge(19, 41, 1, true),
                          new Edge(19, 42, 1, true),
                          new Edge(20, 21, 1, true),
                          new Edge(20, 23, 1, true),
                          new Edge(21, 22, 1, true),
                          new Edge(21, 39, 1, true),
                          new Edge(21, 40, 1, true),
                          new Edge(22, 23, 1, true),
                          new Edge(22, 25, 1, true),
                          new Edge(22, 39, 1, true),
                          new Edge(23, 24, 1, true),
                          new Edge(23, 38, 1, true),
                          new Edge(24, 25, 1, true),
                          new Edge(24, 37, 1, true),
                          new Edge(25, 27, 1, true),
                          new Edge(25, 35, 1, true),
                          new Edge(25, 36, 1, true),
                          new Edge(25, 37, 1, true),
                          new Edge(26, 35, 1, true),

                          // Entry/Exit to Interior this.graph
                          new Edge(28, 29, 1, true),
                          new Edge(28, 46, 1, true),
                          new Edge(28, 47, 1, true),
                          new Edge(29, 30, 1, true),
                          new Edge(29, 48, 1, true),
                          new Edge(30, 31, 1, true),
                          new Edge(30, 47, 1, true),
                          new Edge(30, 48, 1, true),
                          new Edge(31, 48, 1, true),
                          new Edge(31, 49, 1, true),
                          new Edge(32, 33, 1, true),
                          new Edge(32, 49, 1, true),
                          new Edge(33, 49, 1, true),
                          new Edge(34, 35, 1, true),
                          new Edge(34, 50, 1, true),
                          new Edge(35, 36, 1, true),
                          new Edge(35, 50, 1, true),
                          new Edge(36, 37, 1, true),
                          new Edge(36, 50, 1, true),
                          new Edge(37, 38, 1, true),
                          new Edge(37, 51, 1, true),
                          new Edge(38, 39, 1, true),
                          new Edge(38, 50, 1, true),
                          new Edge(39, 40, 1, true),
                          new Edge(39, 51, 1, true),
                          new Edge(40, 52, 1, true),
                          new Edge(41, 52, 1, true),
                          new Edge(41, 53, 1, true),
                          new Edge(42, 53, 1, true),
                          new Edge(43, 44, 1, true),
                          new Edge(43, 46, 1, true),
                          new Edge(43, 53, 1, true),
                          new Edge(44, 45, 1, true),
                          new Edge(44, 46, 1, true),
                          new Edge(44, 53, 1, true),
                          new Edge(45, 46, 1, true),

                          // Interior this.graph
                          new Edge(46, 47, 1, true),
                          new Edge(46, 53, 1, true),
                          new Edge(46, 54, 1, true),
                          new Edge(47, 48, 1, true),
                          new Edge(47, 54, 1, true),
                          new Edge(47, 55, 1, true),
                          new Edge(48, 49, 1, true),
                          new Edge(48, 55, 1, true),
                          new Edge(49, 50, 1, true),
                          new Edge(49, 55, 1, true),
                          new Edge(49, 56, 1, true),
                          new Edge(50, 51, 1, true),
                          new Edge(50, 56, 1, true),
                          new Edge(51, 52, 1, true),
                          new Edge(51, 56, 1, true),
                          new Edge(51, 57, 1, true),
                          new Edge(52, 53, 1, true),
                          new Edge(52, 57, 1, true),
                          new Edge(53, 57, 1, true),
                          new Edge(53, 54, 1, true),
                          new Edge(54, 55, 1, true),
                          new Edge(54, 56, 1, true),
                          new Edge(54, 57, 1, true),
                          new Edge(55, 56, 1, true),
                          new Edge(55, 57, 1, true),
                          new Edge(56, 57, 1, true),

                          // VisibilityCellTestScenarios
                          new Edge(14, 106, 1, true),
                          new Edge(18, 100, 1, true),
                          new Edge(18, 108, 1, true),
                          new Edge(100, 101, 1, true),
                          new Edge(100, 102, 1, true),
                          new Edge(103, 104, 1, true),
                          new Edge(104, 105, 1, true),
                          new Edge(104, 106, 1, true),
                          new Edge(106, 107, 1, true),
                          new Edge(107, 108, 1, true),
                          new Edge(14, 109, 1, true),
                          new Edge(109, 110, 1, true),
                          new Edge(110, 106, 1, true),
                          new Edge(110, 111, 1, true),

                          //Multi node test part
                          new Edge(31, 200, 1, true),
                          new Edge(200, 201, 1, true),
                          new Edge(201, 202, 1, true)
        };
    }

    private void buildEdges() {
        for (Edge edge : edges) {
            this.graph.edge(edge.baseNode, edge.adjNode, edge.distance, edge.bothDirections);
        }
    }

    private Polygon createPolygon() {
        return new Polygon(new double[]{19, 19, 8, 8}, new double[]{14, 24, 24, 14});
    }

    private LocationIndex getCorrespondingIndex() {
        return new PolygonRoutingTestIndex(graph, new RAMDirectory()).setResolution(300).prepareIndex();
    }

    private class MinDistanceNodeFinder {
        private final double lat;
        private final double lon;
        private double minDistance;
        private Node minNode;

        MinDistanceNodeFinder(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        double getMinDistance() {
            return minDistance;
        }

        Node getMinNode() {
            return minNode;
        }

        MinDistanceNodeFinder invoke() {
            minDistance = Double.MAX_VALUE;
            minNode = null;

            for (final Node node : nodes) {
                final double distanceToThisNode = distanceCalculator.calcNormalizedDist(lat, lon, node.latitude, node.longitude);

                if (minDistance > distanceToThisNode) {
                    minDistance = distanceToThisNode;
                    minNode = node;
                }
            }
            return this;
        }
    }

    private void createEncodingManager() {
        final FlagEncoder carFlagEncoder = new CarFlagEncoder();
        this.flagEncoder = carFlagEncoder;
        this.encodingManager = EncodingManager.create(carFlagEncoder);
    }

    private void createTestGraph() {
        this.graph = this.createPolygonTestGraph();
        this.graph.flush();
    }

    private void createTestPolygon() {
        this.polygon = createPolygon();
    }

    private void createLocationIndex() {
        this.locationIndex = this.getCorrespondingIndex();
    }

    private void getNodeAccess() {
        this.nodeAccess = this.graph.getNodeAccess();
    }

    private void setTraversalMode() {
        this.traversalMode = TraversalMode.NODE_BASED;
    }

    private void setAlgorithmName() {
        this.algorithmName = "dijkstrabi";
    }

    private void buildHintsMap() {
        this.algorithmHints = new HintsMap();
        this.algorithmHints.put("elevation", "false");
        this.algorithmHints.put("instructions", "true");
        this.algorithmHints.put("way_point_max_distance", "1.0");
        this.algorithmHints.put("calc_points", "true");
        this.algorithmHints.put("type", "json");
        this.algorithmHints.put("locale", "de-DE");
        this.algorithmHints.put("weighting", "fastest");
        this.algorithmHints.put("key", "");
        this.algorithmHints.put("vehicle", "car");
    }

    private void setWeighting() {
        this.weighting = new FastestWeighting(this.flagEncoder, this.algorithmHints);
    }

    private void setAlgorithmOptions() {
        final int maxVisitedNodes = this.algorithmHints.getInt(MAX_VISITED_NODES, Integer.MAX_VALUE);
        this.algorithmOptions = AlgorithmOptions.start().
                algorithm(algorithmName).traversalMode(traversalMode).weighting(weighting).
                maxVisitedNodes(maxVisitedNodes).
                hints(algorithmHints).
                build();
    }

    public List<EdgeIteratorState> getAllEdges() {
        final List<EdgeIteratorState> allEdges = new ArrayList<>(graph.getEdges());
        final EdgeIterator allEdgesIterator = graph.getAllEdges();

        while (allEdgesIterator.next()) {
            allEdges.add(allEdgesIterator.detach(false));
        }

        return allEdges;
    }

    public EdgeIteratorState getEdge(final int baseNode, final int adjNode) {
        final List<EdgeIteratorState> allEdges = getAllEdges();

        for (EdgeIteratorState edge : allEdges) {
            if (isEdgeEqual(baseNode, adjNode, edge)) {
                return edge;
            }
            edge = edge.detach(true);
            if (isEdgeEqual(baseNode, adjNode, edge)) {
                return edge;
            }
        }

        throw new IllegalArgumentException("Such a Edge doesn't exist");
    }

    private boolean isEdgeEqual(int baseNode, int adjNode, EdgeIteratorState edge) {
        return edge.getBaseNode() == baseNode && edge.getAdjNode() == adjNode;
    }

    public class PolygonRoutingTestIndex extends GridIndex {
        public PolygonRoutingTestIndex(Graph graph, Directory dir) {
            super(graph, dir);
        }

        @Override
        public LocationIndex setResolution(int resolution) {
            return super.setResolution(resolution);
        }

        @Override
        public LocationIndex prepareIndex() {
            return super.prepareIndex();
        }

        @Override
        public QueryResult findClosest(double lat, double lon, EdgeFilter edgeFilter) {
            MinDistanceNodeFinder minDistanceNodeFinder = new MinDistanceNodeFinder(lat, lon).invoke();

            double minDistance = minDistanceNodeFinder.getMinDistance();
            Node minNode = minDistanceNodeFinder.getMinNode();
            EdgeIteratorState firstEdgeAdjacentToMinNode = findClosestEdge(minNode);

            return createQueryResult(minDistance, minNode, firstEdgeAdjacentToMinNode);
        }

        private EdgeIteratorState findClosestEdge(Node minNode) {
            EdgeExplorer edgeExplorer = graph.createEdgeExplorer();
            EdgeIterator edgeIterator = edgeExplorer.setBaseNode(minNode.id);
            edgeIterator.next();
            return edgeIterator;
        }

        private QueryResult createQueryResult(double minDistance, Node minNode, EdgeIteratorState firstEdgeAdjacentToMinNode) {
            QueryResult result = new QueryResult(minNode.latitude, minNode.longitude);
            result.setClosestNode(minNode.id);
            result.setQueryDistance(minDistance);
            result.setClosestEdge(firstEdgeAdjacentToMinNode);
            result.setWayIndex(0);
            result.calcSnappedPoint(new DistanceCalc2D());
            return result;
        }

        @Override
        public LocationIndex setApproximation(boolean approxDist) {
            throw new NotImplementedException();
        }

        @Override
        public void setSegmentSize(int bytes) {
            throw new NotImplementedException();
        }

        @Override
        public void query(BBox queryBBox, Visitor function) {
            for (final Node node : nodes) {
                if (queryBBox.contains(node.latitude, node.longitude)) {
                    function.onNode(node.id);
                }
            }
        }

        @Override
        public boolean loadExisting() {
            throw new NotImplementedException();
        }

        @Override
        public LocationIndexTree create(long byteCount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flush() {
            super.flush();
        }

        @Override
        public void close() {
            throw new NotImplementedException();
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public long getCapacity() {
            return 0;
        }
    }
}
