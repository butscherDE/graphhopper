package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.Polygon;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class PolygonRoutingTemplate extends ViaRoutingTemplate {
    private final GHRequest ghRequest;
    private final Polygon polygon;
    private final GraphHopper gh;
    private final GraphHopperStorage ghStorage;
    private final NodeAccess nodeAccess;
    private QueryGraph queryGraph;
    private AlgorithmOptions algoOpts;
    private RoutingAlgorithmFactory algoFactory;
    private DijkstraOneToMany dijkstraForLOTNodes;
    private DijkstraManyToMany dijkstraForPathSkeleton;
    private RoutingAlgorithm routingAlgorithm;
    private List<RouteCandidate> routeCandidates;

    public PolygonRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex, GraphHopper gh,
                                  EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);
        this.ghRequest = ghRequest;
        this.polygon = ghRequest.getPolygon();
        this.gh = gh;
        this.ghStorage = this.gh.getGraphHopperStorage();
        this.nodeAccess = this.ghStorage.getNodeAccess();
    }

    @Override
    public List<Path> calcPaths(QueryGraph queryGraph, RoutingAlgorithmFactory algoFactory, AlgorithmOptions algoOpts) {
        this.queryGraph = queryGraph;
        this.algoFactory = algoFactory;
        this.algoOpts = algoOpts;
        this.routingAlgorithm = algoFactory.createAlgo(queryGraph, algoOpts);
        this.routeCandidates = new LinkedList<>();
        return routeWithPolygon();
    }

    private boolean isInvalidParameterSet(QueryGraph queryGraph, RoutingAlgorithmFactory algoFactory, AlgorithmOptions algoOpts) {
        return !queryGraph.equals(this.queryGraph) || !algoFactory.equals(this.algoFactory) || !algoOpts.equals(this.algoOpts);
    }

    private List<Path> routeWithPolygon() {
        this.findCandidateRoutes();

        throw new NotImplementedException();
    }

    private void findCandidateRoutes() {
        List<Integer> nodesInPolygon = getNodesInPolygon();
        List<Integer> polygonEntryExitPoints = findPolygonEntryExitPoints(nodesInPolygon);
        List<List<Integer>> LOTNodes = findLocalOptimalTouchnodes(polygonEntryExitPoints);

        for (int i = 0; i < LOTNodes.size() - 1; i++) {
            lookUpStartEndNodes(i);
            buildRouteCandidatesForCurrentPoint(LOTNodes.get(i), i);

        }
    }

    private void buildRouteCandidatesForCurrentPoint(List<Integer> currentPointsLOTNodes, int pointsIndex) {
        int pointInQueryResultsIndex = this.queryResults.size() - 2;
        int currentPointID = this.queryResults.get(pointInQueryResultsIndex).getClosestNode();
        int nextPointID = this.queryResults.get(pointInQueryResultsIndex + 1).getClosestNode();

        for (final int LOTNodeL : currentPointsLOTNodes) {
            for (final int LOTNodeLPrime : currentPointsLOTNodes) {
                this.routeCandidates.add(buildCandidatePath(currentPointID, nextPointID, LOTNodeL, LOTNodeLPrime));
            }
        }
    }

    private RouteCandidate buildCandidatePath(int currentPointID, int nextPointID, int LOTNodeL, int LOTNodeLPrime) {
        RouteCandidate routeCandidate = new RouteCandidate(this, currentPointID, nextPointID, LOTNodeL, LOTNodeLPrime);

        return routeCandidate;
    }

    private void lookUpStartEndNodes(int pointsIndex) {
        final GHPoint currentPoint = this.ghRequest.getPoints().get(pointsIndex);
        final GHPoint nextPoint = this.ghRequest.getPoints().get(pointsIndex + 1);

        List<GHPoint> LOTNodesGHPoints = Arrays.asList(currentPoint, nextPoint);
        super.lookup(LOTNodesGHPoints, this.encodingManager.getEncoder(this.ghRequest.getVehicle()));
    }

    // Definition 6 in Storandts paper Region-Aware Routing Planning
    private List<List<Integer>> findLocalOptimalTouchnodes(final List<Integer> polygonEntryExitPoints) {
        // TODO: Is there an A* one to many option? Does this make sense at all?

        final EdgeExplorer edgeExplorer = this.queryGraph.createEdgeExplorer();

        List<QueryResult> fixedUserSpecifiedPoints = this.queryResults;
        List<List<Integer>> LOTNodes = new ArrayList<>();
        for (final QueryResult point : fixedUserSpecifiedPoints) {
            makeLOTNodeListForThisPoint(polygonEntryExitPoints, edgeExplorer, LOTNodes, point);
        }


        return LOTNodes;
    }

    private void makeLOTNodeListForThisPoint(List<Integer> polygonEntryExitPoints, EdgeExplorer edgeExplorer, List<List<Integer>> LOTNodes, QueryResult point) {
        Map<Integer, Double> distancesToPolygonEntryExit = getDistancesFromPointToEntryExitPoints(point, polygonEntryExitPoints);
        addEntryExitPointsCopyTo(polygonEntryExitPoints, LOTNodes);
        for (final int entryExitPoint :  LOTNodes.get(LOTNodes.size() - 1)) {
            checkIfThisIsAValidLOTNode(edgeExplorer, LOTNodes, distancesToPolygonEntryExit, entryExitPoint);
        }
    }

    private void checkIfThisIsAValidLOTNode(EdgeExplorer edgeExplorer, List<List<Integer>> LOTNodes, Map<Integer, Double> distancesToPolygonEntryExit, int entryExitPoint) {
        EdgeIterator neighborFinder = edgeExplorer.setBaseNode(entryExitPoint);
        Double distanceOfThisEntryExitPointFromPoint = distancesToPolygonEntryExit.get(entryExitPoint);

        boolean foundABetterLOTNode =
                lookForNeighborsThatMakeABetterLOTNode(distancesToPolygonEntryExit, neighborFinder, distanceOfThisEntryExitPointFromPoint);

        pruneThisNoteFromLOTIfBetterWasFound(LOTNodes, entryExitPoint, foundABetterLOTNode);
    }

    private void pruneThisNoteFromLOTIfBetterWasFound(List<List<Integer>> LOTNodes, int entryExitPoint, boolean foundABetterLOTNode) {
        if (foundABetterLOTNode) {
            LOTNodes.get(LOTNodes.size() - 1).remove(entryExitPoint);
        }
    }

    private boolean lookForNeighborsThatMakeABetterLOTNode(Map<Integer, Double> distancesToPolygonEntryExit, EdgeIterator neighborFinder,
                                                           Double distanceOfThisEntryExitPointFromPoint) {
        boolean foundABetterLOTNode = false;
        do {
            foundABetterLOTNode = foundABetterLOTNode(distancesToPolygonEntryExit, neighborFinder, distanceOfThisEntryExitPointFromPoint);
        } while (neighborFinder.next() && !foundABetterLOTNode);
        return foundABetterLOTNode;
    }

    private boolean foundABetterLOTNode(Map<Integer, Double> distancesToPolygonEntryExit, EdgeIterator neighborFinder, Double distanceOfThisEntryExitPointFromPoint) {
        final int currentNeighbor = neighborFinder.getAdjNode();
        Double distanceOfNeighborFromPoint = distancesToPolygonEntryExit.get(currentNeighbor);
        if (distanceOfNeighborFromPoint != null) {
            if (distanceOfNeighborFromPoint < distanceOfThisEntryExitPointFromPoint) {
                return true;
            }
        }
        return false;
    }

    private void addEntryExitPointsCopyTo(List<Integer> polygonEntryExitPoints, List<List<Integer>> LOTNodes) {
        int index = LOTNodes.size();
        LOTNodes.add(index, (ArrayList<Integer>)  ((ArrayList<Integer>) polygonEntryExitPoints).clone());
    }

    private Map<Integer, Double> getDistancesFromPointToEntryExitPoints(QueryResult point, List<Integer> polygonEntryExitPoints) {
        final Map<Integer, Double> weightsOfEntryExitPoints = new HashMap<Integer, Double>();
        for (final int entryExitPoint : polygonEntryExitPoints) {
            this.dijkstraForLOTNodes.calcPath(point.getClosestNode(), entryExitPoint);
            weightsOfEntryExitPoints.put(entryExitPoint, this.dijkstraForLOTNodes.getWeight(entryExitPoint));
        }

        return weightsOfEntryExitPoints;
    }

    private List<Integer> findPolygonEntryExitPoints(final List<Integer> nodesInPolygon) {
        final List<Integer> entryExitPoints = new LinkedList<>();
        final EdgeExplorer edgeExplorer = ghStorage.getBaseGraph().createEdgeExplorer();

        addAllNodesNotInPolygonButDirectlyAccessibleFromThereToEntryExitPoints(nodesInPolygon, entryExitPoints, edgeExplorer);

        return entryExitPoints;
    }

    private void addAllNodesNotInPolygonButDirectlyAccessibleFromThereToEntryExitPoints(List<Integer> nodesInPolygon, List<Integer> entryExitPoints, EdgeExplorer edgeExplorer) {
        for (int node : nodesInPolygon) {
            final EdgeIterator edgeIterator = edgeExplorer.setBaseNode(node);

            do {
                addToEntryExitIfNotExistentAndNotInPolygon(nodesInPolygon, entryExitPoints, edgeIterator);
            } while (edgeIterator.next());
        }
    }

    private void addToEntryExitIfNotExistentAndNotInPolygon(List<Integer> nodesInPolygon, List<Integer> entryExitPoints, EdgeIterator edgeIterator) {
        final int adjacentNode = edgeIterator.getAdjNode();
        if (!nodesInPolygon.contains(adjacentNode) && !entryExitPoints.contains(adjacentNode)) {
            entryExitPoints.add(adjacentNode);
        }
    }

    // According to Prof. Storandts paper Region-Aware Route Planning Definition 2.
    private void calculatePathSkeleton(List<Integer> subGraphNodes, final List<Integer> polygonEntryExitPoints) {
        this.dijkstraForPathSkeleton = new DijkstraManyToMany(this.queryGraph, algoOpts.getWeighting(), this.algoOpts.getTraversalMode(), subGraphNodes,
                                                                polygonEntryExitPoints);
        this.dijkstraForPathSkeleton.findAllPathsBetweenEntryExitPoints();
    }

    private List<Integer> filterOutNodesNotInPolygon(final List<Integer> nodes, final Polygon polygon) {
        final List<Integer> filterResult = new LinkedList<>();

        while (!nodes.isEmpty()) {
            filterNextNode(nodes, polygon, filterResult);
        }

        return filterResult;
    }

    private void filterNextNode(List<Integer> nodes, Polygon polygon, List<Integer> filterResult) {
        int nodeToFilter = popNode(nodes);
        final NodeAccess nodeAccess = this.ghStorage.getNodeAccess();
        final double lat = nodeAccess.getLat(nodeToFilter);
        final double lon = nodeAccess.getLon(nodeToFilter);

        if (polygon.contains(lat, lon)) {
            filterResult.add(nodeToFilter);
        }
    }

    private int popNode(List<Integer> nodes) {
        final int nodeToFilter = nodes.get(0);
        nodes.remove(0);
        return nodeToFilter;
    }

    private List<Integer> getNodesInPolygon() {
        final Polygon polygon = this.getGhRequest().getPolygon();
        final NodeAccess nodeAccess = gh.getGraphHopperStorage().getNodeAccess();

        BBox minimumPolygonBoundingBox = BBox.createMinimalBoundingBoxFromPolygon(polygon);
        final NodesInPolygonFindingVisitor visitor =new NodesInPolygonFindingVisitor(polygon, nodeAccess);
        this.gh.getLocationIndex().query(minimumPolygonBoundingBox, visitor);
        return visitor.getNodesInPolygon();
    }

    private class NodesInPolygonFindingVisitor extends LocationIndex.Visitor {
        private final List<Integer> nodesInPolygon = new LinkedList<>();
        private final Polygon polygon;
        private final NodeAccess nodeAccess;

        public NodesInPolygonFindingVisitor(final Polygon polygon, final NodeAccess nodeAccess) {
            this.polygon = polygon;
            this.nodeAccess = nodeAccess;
        }

        @Override
        public void onNode(int nodeId) {
            final double lat = nodeAccess.getLat(nodeId);
            final double lon = nodeAccess.getLon(nodeId);

            if (polygon.contains(lat, lon)) {
                this.nodesInPolygon.add(nodeId);
            }
        }

        public List<Integer> getNodesInPolygon() { return this.nodesInPolygon; }
    }

    public RoutingAlgorithm getRoutingAlgorithm() {
        return this.routingAlgorithm;
    }

    public DijkstraManyToMany getPathSkeletonRouter() {
        return this.dijkstraForPathSkeleton;
    }

    private class RouteCandidate {
        private final Path startToPolygonEntry;
        private final Path polygonEntryToPolygonExit;
        private final Path polygonExitToEnd;
        private final Path directRouteStartEnd;
        private final PolygonRoutingTemplate polygonRoutingTemplate;
        private final RoutingAlgorithm routingAlgorithm;
        private final DijkstraManyToMany pathSkeletonRouter;
        private final double distance;

        public RouteCandidate(final PolygonRoutingTemplate polygonRoutingTemplate, final int startNodeID, final int endNodeID, final int polygonEntryNodeID,
                              final int polygonExitNodeID) {
            this.polygonRoutingTemplate = polygonRoutingTemplate;
            this.routingAlgorithm = polygonRoutingTemplate.getRoutingAlgorithm();
            this.pathSkeletonRouter = polygonRoutingTemplate.getPathSkeletonRouter();

            this.startToPolygonEntry = this.routingAlgorithm.calcPath(startNodeID, polygonEntryNodeID);
            this.polygonEntryToPolygonExit = this.pathSkeletonRouter.getPathByStartEndPoint(polygonEntryNodeID, polygonExitNodeID);
            this.polygonExitToEnd = this.routingAlgorithm.calcPath(polygonExitNodeID, endNodeID);
            this.directRouteStartEnd = this.routingAlgorithm.calcPath(startNodeID, endNodeID);

            this.distance = this.startToPolygonEntry.getDistance() + this.polygonEntryToPolygonExit.getDistance() + this. polygonExitToEnd.getDistance();
        }

        public Path getMergedPath(final QueryGraph queryGraph, final AlgorithmOptions algoOpts) {
            Path completePathCandidate = new Path(queryGraph, algoOpts.getWeighting());
            completePathCandidate.addPath(startToPolygonEntry);
            completePathCandidate.addPath(polygonEntryToPolygonExit);
            completePathCandidate.addPath(polygonExitToEnd);
            return completePathCandidate;
        }

        public double getDistance() {
            return this.distance;
        }

        /**
         * According to 5.2 in Storandts Region-Aware route planning paper.
         *
         * @return The approximated time spent in the region of interest
         */
        public double getDistanceInROI() {
            return this.polygonEntryToPolygonExit.getDistance();
        }

        public double getGain() {
            return this.polygonEntryToPolygonExit.getDistance() / (this.getDetourDistance() + 1);
        }

        public double getDetourDistance() {
            return this.getDistance() - this.directRouteStartEnd.getDistance();
        }

        public boolean isDetourSelfIntersecting() {
            throw new NotImplementedException();
        }
    }
}
