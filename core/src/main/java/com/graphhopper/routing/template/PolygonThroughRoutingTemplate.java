package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.template.polygonRoutingUtil.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.Polygon;

import java.util.*;

public class PolygonThroughRoutingTemplate extends PolygonRoutingTemplate {
    private ManyToManyRouting pathSkeletonRouter;
    private final FlagEncoder flagEncoder;
    private LOTNodeExtractor lotNodes;
    private List<Integer> nodesInPolygon;
    private List<Integer> polygonEntryExitPoints;
    private LOTNodeExtractor lotNodes1;

    public PolygonThroughRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex,
                                         EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);

        this.flagEncoder = encodingManager.getEncoder(ghRequest.getVehicle());
    }

    protected void findCandidateRoutes() {
        final StopWatch swFindNodesInPolygon = generateNodesInPolygonAndMeasureTime();
        final StopWatch swFindEntryExitPoints = findPolygonEntryExitPointsAndMeasureTime();
        final List<Integer> viaPointNodeIds = extractNodeIdsFromQueryResults();
        final StopWatch swLOTNodes = findLotNodesAndMeasureTime(viaPointNodeIds);
        final List<QueryResult> queryResults = createQueryResults(polygonEntryExitPoints, flagEncoder);
        final StopWatch swPathSkeleton = findPathSkeletonAndMeasureTime(queryResults);

        System.out.println("Candidate Routes found\n" +
                           "Nodes in polygon : " + nodesInPolygon.size() + " in " + swFindNodesInPolygon.getSeconds() + "\n" +
                           "Entry/Exit points: " + polygonEntryExitPoints.size() + " in " + swFindEntryExitPoints.getSeconds() + "\n" +
                           "LOT Nodes        : " + lotNodes.size() + " in " + swLOTNodes.getSeconds() + "\n" +
                           "Path Skeleton    : " + "in " + swPathSkeleton.getSeconds());

        for (int i = 0; i < viaPointNodeIds.size() - 1; i++) {
            final int viaPointNodeId = viaPointNodeIds.get(i);
            final int nextViaPointNodeId = viaPointNodeIds.get(i + 1);
            buildRouteCandidatesForCurrentPoint(viaPointNodeId, nextViaPointNodeId, lotNodes.getLotNodesFor(viaPointNodeId));
        }
    }

    private StopWatch findPathSkeletonAndMeasureTime(List<QueryResult> queryResults) {
        final StopWatch swPathSkeleton = new StopWatch("Generate path skeleton");
        swPathSkeleton.start();

        this.pathSkeletonRouter = new ManyToManyRouting(nodesInPolygon, polygonEntryExitPoints, this.graph, queryResults, this.algoFactory, this.algorithmOptions);
        this.pathSkeletonRouter.findPathBetweenAllNodePairs();

        swPathSkeleton.stop();
        return swPathSkeleton;
    }

    private StopWatch findLotNodesAndMeasureTime(List<Integer> viaPointNodeIds) {
        final StopWatch swLOTNodes = new StopWatch("LOT node generation");
        swLOTNodes.start();

        this.lotNodes = LOTNodeExtractor.createExtractedData(this.graph, this.algoFactory, this.algorithmOptions, viaPointNodeIds, polygonEntryExitPoints);
        failIfNotEnoughLotNodes();

        swLOTNodes.stop();
        return swLOTNodes;
    }

    private void failIfNotEnoughLotNodes() {
        if (this.lotNodes.size() < 1) {
            throw new IllegalStateException("Not enough entry / exit points found to enter or exit the given polygon");
        }
    }

    private StopWatch findPolygonEntryExitPointsAndMeasureTime() {
        StopWatch swFindEntryExitPoints = new StopWatch("finding entry exit points");
        swFindEntryExitPoints.start();

        this.polygonEntryExitPoints = findPolygonEntryExitPoints(nodesInPolygon);

        swFindEntryExitPoints.stop();
        return swFindEntryExitPoints;
    }

    private StopWatch generateNodesInPolygonAndMeasureTime() {
        StopWatch swFindNodesInPolygon = new StopWatch("finding nodes in polygon");
        swFindNodesInPolygon.start();

        this.nodesInPolygon = getNodesInPolygon();
        failOnNotEnoughNodesInPolygon();

        swFindNodesInPolygon.stop();
        return swFindNodesInPolygon;
    }

    private void failOnNotEnoughNodesInPolygon() {
        if (this.nodesInPolygon.size() < 1) {
            throw new IllegalStateException("Not enough nodes in polygon. Most likely the polygon doesn't contain intersections.");
        }
    }

    private List<QueryResult> createQueryResults(final List<Integer> nodes, final FlagEncoder flagEncoder) {
        final List<GHPoint> points = nodeIdsToGhPoints(nodes);

        return this.lookup(points, flagEncoder);
    }

    private List<GHPoint> nodeIdsToGhPoints(List<Integer> nodes) {
        final List<GHPoint> points = new ArrayList<>(nodes.size());
        final NodeAccess nodeAccess = this.graph.getNodeAccess();

        for (final int node : nodes) {
            final double latitude = nodeAccess.getLatitude(node);
            final double longitude = nodeAccess.getLongitude(node);

            points.add(new GHPoint(latitude, longitude));
        }
        return points;
    }

    private List<Integer> extractNodeIdsFromQueryResults() {
        final List<Integer> nodeIds = new ArrayList<>(this.queryResults.size());

        for (final QueryResult queryResult : this.queryResults) {
            nodeIds.add(queryResult.getClosestNode());
        }

        return nodeIds;
    }

    private void buildRouteCandidatesForCurrentPoint(final int currentViaPoint, final int nextViaPoint, List<Integer> currentPointsLOTNodes) {
        for (final int LOTNodeL : currentPointsLOTNodes) {
            for (final int LOTNodeLPrime : currentPointsLOTNodes) {
                if (LOTNodeL != LOTNodeLPrime) {
                    this.routeCandidates.add(buildCandidatePath(currentViaPoint, nextViaPoint, LOTNodeL, LOTNodeLPrime));
                }
            }
        }
    }

    private RouteCandidate buildCandidatePath(int currentPointID, int nextPointID, int lotNodeL, int lotNodeLPrime) {
        final Path startToDetourEntry = this.lotNodes.getLotNodePathFor(currentPointID, lotNodeL);
        final Path detourEntryToDetourExit = this.pathSkeletonRouter.getPathByFromEndNodeID(lotNodeL, lotNodeLPrime);
        final Path detourExitToEnd = this.lotNodes.getLotNodePathFor(lotNodeLPrime, nextPointID);
        final Path directRoute = this.getNewRoutingAlgorithm().calcPath(currentPointID, nextPointID);

        final RouteCandidate routeCandidate = new RouteCandidate(currentPointID, nextPointID, lotNodeL, lotNodeLPrime, startToDetourEntry, detourEntryToDetourExit,
                                                                 detourExitToEnd, directRoute);

        return routeCandidate;
    }

    private List<Integer> findPolygonEntryExitPoints(final List<Integer> nodesInPolygon) {
        final List<Integer> entryExitPoints = new ArrayList<>();
        final EdgeExplorer edgeExplorer = this.graph.createEdgeExplorer();

        addAllNodesNotInPolygonButDirectlyAccessibleFromThereToEntryExitPoints(nodesInPolygon, entryExitPoints, edgeExplorer);

        return entryExitPoints;
    }

    private void addAllNodesNotInPolygonButDirectlyAccessibleFromThereToEntryExitPoints(List<Integer> nodesInPolygon, List<Integer> entryExitPoints, EdgeExplorer edgeExplorer) {
        for (int node : nodesInPolygon) {
            final EdgeIterator edgeIterator = edgeExplorer.setBaseNode(node);

            while (edgeIterator.next()) {
                addToEntryExitIfNotExistentAndNotInPolygon(nodesInPolygon, entryExitPoints, edgeIterator);
            }
        }
    }

    private void addToEntryExitIfNotExistentAndNotInPolygon(List<Integer> nodesInPolygon, List<Integer> entryExitPoints, EdgeIterator edgeIterator) {
        final int adjacentNode = edgeIterator.getAdjNode();
        if (!nodesInPolygon.contains(adjacentNode) && !entryExitPoints.contains(adjacentNode)) {
            entryExitPoints.add(adjacentNode);
        }
    }

    private List<Integer> getNodesInPolygon() {
        final Polygon polygon = this.getGhRequest().getPolygon();

        BBox minimumPolygonBoundingBox = BBox.createMinimalBoundingBoxFromPolygon(polygon);
        final NodesInPolygonFindingVisitor visitor = new NodesInPolygonFindingVisitor(polygon, nodeAccess);
        this.locationIndex.query(minimumPolygonBoundingBox, visitor);
        return visitor.getNodesInPolygon();
    }

    private static class NodesInPolygonFindingVisitor extends LocationIndex.Visitor {
        private final List<Integer> nodesInPolygon = new ArrayList<>();
        private final Polygon polygon;
        private final NodeAccess nodeAccess;

        NodesInPolygonFindingVisitor(final Polygon polygon, final NodeAccess nodeAccess) {
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

        List<Integer> getNodesInPolygon() {
            return this.nodesInPolygon;
        }
    }

}
