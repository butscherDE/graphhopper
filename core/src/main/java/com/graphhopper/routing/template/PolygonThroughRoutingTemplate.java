package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.template.polygonRoutingUtil.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.Polygon;

import java.util.*;

public class PolygonThroughRoutingTemplate extends PolygonRoutingTemplate {
    private ManyToManyRouting pathSkeletonRouter;
    private final FlagEncoder flagEncoder;

    public PolygonThroughRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex,
                                         EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);

        this.flagEncoder = encodingManager.getEncoder(ghRequest.getVehicle());
    }

    protected void findCandidateRoutes() {
        final List<Integer> nodesInPolygon = getNodesInPolygon();
        final List<Integer> polygonEntryExitPoints = findPolygonEntryExitPoints(nodesInPolygon);
        final List<Integer> viaPointNodeIds = this.extractNodeIdsFromQueryResults();
        final LOTNodeExtractor lotNodes = LOTNodeExtractor.createExtractedData(this.graph, this.algoFactory, this.algorithmOptions, viaPointNodeIds, polygonEntryExitPoints);

        final List<QueryResult> queryResults = createQueryResults(polygonEntryExitPoints, flagEncoder);
        this.pathSkeletonRouter = new ManyToManyRouting(nodesInPolygon, polygonEntryExitPoints, this.graph, queryResults, this.algoFactory, this.algorithmOptions);
        this.pathSkeletonRouter.findPathBetweenAllNodePairs();


        for (int i = 0; i < viaPointNodeIds.size() - 1; i++) {
            final int viaPointNodeId = viaPointNodeIds.get(i);
            final int nextViaPointNodeId = viaPointNodeIds.get(i + 1);
            buildRouteCandidatesForCurrentPoint(viaPointNodeId, nextViaPointNodeId, lotNodes.getLotNodesFor(viaPointNodeId));
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
                    this.routeCandidates.getCandidates().add(buildCandidatePath(currentViaPoint, nextViaPoint, LOTNodeL, LOTNodeLPrime));
                }
            }
        }
    }

    private RouteCandidatePolygon buildCandidatePath(int currentPointID, int nextPointID, int LOTNodeL, int LOTNodeLPrime) {
        final RouteCandidatePolygon routeCandidate = new RouteCandidatePolygonThrough(this, currentPointID, nextPointID, LOTNodeL, LOTNodeLPrime);
        routeCandidate.calcPaths();

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

    public ManyToManyRouting getPathSkeletonRouter() {
        return this.pathSkeletonRouter;
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
