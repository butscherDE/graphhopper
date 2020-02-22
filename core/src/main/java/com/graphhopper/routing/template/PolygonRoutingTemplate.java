package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.*;
import com.graphhopper.routing.template.polygonRoutingUtil.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.PathMerger;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.Translation;
import com.graphhopper.util.shapes.GHPoint;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class PolygonRoutingTemplate extends ViaRoutingTemplate {
    private final GHRequest ghRequest;
    final LocationIndex locationIndex;
    NodeAccess nodeAccess;
    QueryGraph graph;
    AlgorithmOptions algorithmOptions;
    RoutingAlgorithmFactory algoFactory;
    RouteCandidateList<RouteCandidate> routeCandidates;

    private MultiRouting pathSkeletonRouter;
    private final FlagEncoder flagEncoder;
    LOTNodeExtractor lotNodes;
    PathSkeletonGraph pathSkeletonEdgeFilter;
    private Set<Integer> polygonEntryExitPoints;

    PolygonRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex, EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);
        this.ghRequest = ghRequest;
        this.locationIndex = locationIndex;
        this.pathList = new ArrayList<>(ghRequest.getPoints().size() - 1);
        this.flagEncoder = encodingManager.getEncoder(ghRequest.getVehicle());
    }

    @Override
    public List<Path> calcPaths(QueryGraph queryGraph, RoutingAlgorithmFactory algoFactory, AlgorithmOptions algoOpts) {
        if (this.ghRequest.getPoints().size() != 2) {
            // TODO implement for more than start & endpoint but also via points
            throw new NotImplementedException();
        }
        this.setCalcPathsParams(queryGraph, algoFactory, algoOpts);
        return routeWithPolygon();
    }

    private void setCalcPathsParams(QueryGraph queryGraph, RoutingAlgorithmFactory algoFactory, AlgorithmOptions algoOpts) {
        this.graph = queryGraph;
        this.nodeAccess = graph.getNodeAccess();
        this.algoFactory = algoFactory;
        this.algorithmOptions = algoOpts;
        this.routeCandidates = new RouteCandidateList<>();
    }

    private List<Path> routeWithPolygon() {
        prepareRouteCandidateList();
        extractBestPathCandidate();

        return this.pathList;
    }

    private void prepareRouteCandidateList() {
        this.findCandidateRoutes();
        this.routeCandidates.pruneDominatedCandidateRoutes();
        this.pruneLowerQuantileInROIcandidateRoutes();
    }

    private void pruneLowerQuantileInROIcandidateRoutes() {
        // Assumes that routeCandidates was already sorted descending to roi distance after pruning dominated route candidates
        final int startIndex = (int) (this.routeCandidates.size() * 0.75) + 1;

        for (int i = startIndex; i < this.routeCandidates.size(); i++) {
            this.routeCandidates.remove(i);
        }
    }

    private void extractBestPathCandidate() {
        // TODO Maybe more? Dont know what happens in the gui then.
        this.routeCandidates.sortByGainAscending();
        printAllCandidatesInSortedOrder();
        deleteBestN(0);
        final List<Path> bestPath = this.routeCandidates.getFirstAsPathList(1, this.graph, this.algorithmOptions);
        this.pathList.addAll(bestPath);
    }

    private void printAllCandidatesInSortedOrder() {
        final StringBuilder sb = new StringBuilder();
        sb.append("All non pruned route Candidates: \n");

        for (int i = 0; i < this.routeCandidates.size(); i++) {
            sb.append(this.routeCandidates.get(i).toString());
            sb.append("\n");
        }

        System.out.println(sb.toString());
    }

    // TODO used for experimenting delete when no more needed.
    private void deleteBestN(final int n) {
        for (int i = 0; i < n; i++) {
            this.routeCandidates.remove(this.routeCandidates.size() - 1);
        }
    }

    @Override
    public boolean isReady(PathMerger pathMerger, Translation translation) {
        this.failOnNumPathsInvalid(this.ghRequest, this.pathList);

        this.altResponse.setWaypoints(getWaypoints());
        this.ghResponse.add(this.altResponse);
        pathMerger.doWork(this.altResponse, this.pathList, this.encodingManager, translation);
        return true;
    }

    @Override
    public GHRequest getGhRequest() {
        return this.ghRequest;
    }

    public RoutingAlgorithm getNewRoutingAlgorithm() {
        return this.algoFactory.createAlgo(this.graph, this.algorithmOptions);
    }

    protected void findCandidateRoutes() {
        final StopWatch swFindNodesInPolygon = generateNodesInPolygonAndMeasureTime();
        final StopWatch swFindEntryExitPoints = findPolygonEntryExitPointsAndMeasureTime();
        final List<Integer> viaPointNodeIds = extractNodeIdsFromQueryResults();
        final StopWatch swLOTNodes = findLotNodesAndMeasureTime(viaPointNodeIds);
        final List<QueryResult> queryResults = createQueryResults();
        final StopWatch swPathSkeleton = findPathSkeletonAndMeasureTime(queryResults);

        System.out.println("Candidate Routes found\n" +
                           "Nodes in polygon : " + pathSkeletonEdgeFilter.size() + " in " + swFindNodesInPolygon.getSeconds() + "\n" +
                           "Entry/Exit points: " + polygonEntryExitPoints.size() + " in " + swFindEntryExitPoints.getSeconds() + "\n" +
                           "LOT Nodes        : " + lotNodes.size() + " in " + swLOTNodes.getSeconds() + "\n" +
                           "Path Skeleton    : " + "in " + swPathSkeleton.getSeconds());

        for (int i = 0; i < viaPointNodeIds.size() - 1; i++) {
            final int viaPointNodeId = viaPointNodeIds.get(i);
            final int nextViaPointNodeId = viaPointNodeIds.get(i + 1);
            buildRouteCandidatesForCurrentPoint(viaPointNodeId, nextViaPointNodeId);
        }
    }

    private StopWatch findPathSkeletonAndMeasureTime(List<QueryResult> queryResults) {
        final StopWatch swPathSkeleton = new StopWatch("Generate path skeleton");
        swPathSkeleton.start();

        this.pathSkeletonRouter = getPathSkeletonRouter(queryResults);
        this.pathSkeletonRouter.findPathBetweenAllNodePairs();

        swPathSkeleton.stop();
        return swPathSkeleton;
    }

    public abstract MultiRouting getPathSkeletonRouter(List<QueryResult> queryResults);

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

        this.polygonEntryExitPoints = findPolygonEntryExitPoints(pathSkeletonEdgeFilter);

        swFindEntryExitPoints.stop();
        return swFindEntryExitPoints;
    }

    private StopWatch generateNodesInPolygonAndMeasureTime() {
        StopWatch swFindNodesInPolygon = new StopWatch("finding nodes in polygon");
        swFindNodesInPolygon.start();

        this.pathSkeletonEdgeFilter = getPathSkeletonEdgeFilter();
        failOnNotEnoughNodesInPolygon();

        swFindNodesInPolygon.stop();
        return swFindNodesInPolygon;
    }

    private void failOnNotEnoughNodesInPolygon() {
        if (this.pathSkeletonEdgeFilter.size() < 1) {
            throw new IllegalStateException("Not enough nodes in polygon. Most likely the polygon doesn't contain intersections.");
        }
    }

    private List<QueryResult> createQueryResults() {
        final List<Integer> nodes = lotNodes.getAllLotNodes();
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

    private void buildRouteCandidatesForCurrentPoint(final int currentViaPoint, final int nextViaPoint) {
        final List<Integer> currentPointLotNodes = lotNodes.getLotNodesFor(currentViaPoint);
        final List<Integer> nextPointLotNodes = lotNodes.getLotNodesFor(nextViaPoint);

        for (final int LOTNodeL : currentPointLotNodes) {
            for (final int LOTNodeLPrime : nextPointLotNodes) {
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

    private Set<Integer> findPolygonEntryExitPoints(final PathSkeletonGraph pathSkeletonEdgeFilter) {
        final Set<Integer> entryExitPoints = addAllNodesNotInPolygonButDirectlyAccessibleFromThereToEntryExitPoints(pathSkeletonEdgeFilter);

        return entryExitPoints;
    }

    private Set<Integer> addAllNodesNotInPolygonButDirectlyAccessibleFromThereToEntryExitPoints(PathSkeletonGraph pathSkeletonEdgeFilter) {
        final Set<Integer> entryExitPoints = new LinkedHashSet<>();

        for (int node : pathSkeletonEdgeFilter) {
            final EdgeIterator edgeIterator = graph.createEdgeExplorer().setBaseNode(node);

            while (edgeIterator.next()) {
                addToEntryExitIfNotExistentAndNotInPolygon(pathSkeletonEdgeFilter, entryExitPoints, edgeIterator);
            }
        }

        return entryExitPoints;
    }

    private void addToEntryExitIfNotExistentAndNotInPolygon(PathSkeletonGraph pathSkeletonEdgeFilter, Set<Integer> entryExitPoints, EdgeIterator edgeIterator) {
        final int adjacentNode = edgeIterator.getAdjNode();
        if (!pathSkeletonEdgeFilter.contains(adjacentNode)) {
            entryExitPoints.add(adjacentNode);
        }
    }

    abstract PathSkeletonGraph getPathSkeletonEdgeFilter();
}
