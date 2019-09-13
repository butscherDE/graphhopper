package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.*;
import com.graphhopper.routing.template.PolygonRoutingUtil.RouteCandidate;
import com.graphhopper.routing.template.PolygonRoutingUtil.RouteCandidateList;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.PathMerger;
import com.graphhopper.util.Translation;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.Polygon;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

public abstract class PolygonRoutingTemplate extends ViaRoutingTemplate {
    final GHRequest ghRequest;
    final Polygon polygon;
    final GraphHopperStorage ghStorage;
    final NodeAccess nodeAccess;
    final LocationIndex locationIndex;
    QueryGraph queryGraph;
    AlgorithmOptions algorithmOptions;
    RoutingAlgorithmFactory algoFactory;
    RoutingAlgorithm routingAlgorithm;
    RouteCandidateList routeCandidates;

    public PolygonRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex, NodeAccess nodeAccess, GraphHopperStorage ghStorage,
                                         EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);
        this.ghRequest = ghRequest;
        this.polygon = ghRequest.getPolygon();
        this.ghStorage = ghStorage;
        this.nodeAccess = nodeAccess;
        this.locationIndex = locationIndex;
    }

    @Override
    public List<Path> calcPaths(QueryGraph queryGraph, RoutingAlgorithmFactory algoFactory, AlgorithmOptions algoOpts) {
        if (this.ghRequest.getPoints().size() != 2) {
            // TODO implement for more than start & endpoint
            throw new NotImplementedException();
        }
        this.setCalcPathsParams(queryGraph, algoFactory, algoOpts);
        return routeWithPolygon();
    }

    private void setCalcPathsParams(QueryGraph queryGraph, RoutingAlgorithmFactory algoFactory, AlgorithmOptions algoOpts) {
        this.queryGraph = queryGraph;
        this.lookupPoints();
        this.algoFactory = algoFactory;
        this.algorithmOptions = algoOpts;
        this.routingAlgorithm = algoFactory.createAlgo(queryGraph, algoOpts);
        this.routeCandidates = RouteCandidateList.createEmptyCandidateList();
    }

    private void lookupPoints() {
        List<GHPoint> points = this.ghRequest.getPoints();
        FlagEncoder flagEncoder = this.encodingManager.getEncoder(ghRequest.getVehicle());
        List<QueryResult> lookupResults = super.lookup(points, flagEncoder);
        queryGraph.lookup(lookupResults);
    }

    private List<Path> routeWithPolygon() {
        prepareRouteCandidateList();
        extractBestPathCandidate();

        return this.pathList;
    }

    private void extractBestPathCandidate() {
        // TODO Maybe more? Dont know what happens in the gui then.
        this.routeCandidates.sortByGainAscending();
        final List<Path> bestPath = this.routeCandidates.getFirstAsPathList(1, this.queryGraph, this.algorithmOptions);
        this.pathList.addAll(bestPath);
    }

    private void prepareRouteCandidateList() {
        this.findCandidateRoutes();
        this.pruneDominatedCandidateRoutes();
        this.pruneLowerQuantileInROIcandidateRoutes();
    }

    // Do it in a skyline problem pruning fashion
    private void pruneDominatedCandidateRoutes() {
        this.routeCandidates.sortRouteCandidatesToDistanceInROIDescending();

        int currentPruningCandidateIndex = 1;
        while (indexInCandidateBounds(currentPruningCandidateIndex)) {
            RouteCandidate currentPruningCandidate = this.routeCandidates.candidates.get(currentPruningCandidateIndex);

            boolean foundDominatingPath = isThisCandidateDominatedByAny(currentPruningCandidateIndex, currentPruningCandidate);

            currentPruningCandidateIndex = pruneOrUpdateIndex(currentPruningCandidateIndex, foundDominatingPath);
        }
    }

    private boolean isThisCandidateDominatedByAny(int currentPruningCandidateIndex, RouteCandidate currentPruningCandidate) {
        boolean foundDominatingPath = false;
        for (int i = currentPruningCandidateIndex - 1; i >= 0 && !foundDominatingPath; i--) {
            // routeCandidates must be sorted by now. Therefore dominators can only bbe found on lower indices than the current pruning candidate.
            RouteCandidate possiblyBetterRouteCandidate = this.routeCandidates.candidates.get(i);

            if (isPruningCandidateDominated(currentPruningCandidate, possiblyBetterRouteCandidate)) {
                foundDominatingPath = true;
            }
        }
        return foundDominatingPath;
    }

    private int pruneOrUpdateIndex(int currentPruningCandidateIndex, boolean foundDominatingPath) {
        if (foundDominatingPath) {
            this.routeCandidates.candidates.remove(currentPruningCandidateIndex);
        } else {
            currentPruningCandidateIndex++;
        }
        return currentPruningCandidateIndex;
    }

    private boolean isPruningCandidateDominated(RouteCandidate currentPruningCandidate, RouteCandidate possiblyBetterRouteCandidate) {
        return possiblyBetterRouteCandidate.getDistance() < currentPruningCandidate.getDistance() &&
               possiblyBetterRouteCandidate.getDistanceInROI() > currentPruningCandidate.getDistanceInROI();
    }

    private boolean indexInCandidateBounds(int currentPruningCandidateIndex) {
        return currentPruningCandidateIndex < this.routeCandidates.candidates.size();
    }

    private void pruneLowerQuantileInROIcandidateRoutes() {
        // Assumes that routeCandidates was already sorted descending to roi distance after pruning dominated route candidates
        int startIndex = (int) (this.routeCandidates.candidates.size() * 0.75) + 1;

        for (int i = startIndex; i < this.routeCandidates.candidates.size(); i++) {
            this.routeCandidates.candidates.remove(i);
        }
    }

    abstract RouteCandidateList findCandidateRoutes();

    @Override
    public boolean isReady(PathMerger pathMerger, Translation translation) {
        this.failOnNumPathsInvalid(this.ghRequest, this.pathList);

        // TODO check if all waypoints have been queried. Respectively: The entry exit points: Are they queried? Do They have to be queried or mustnt they be queried?
        this.altResponse.setWaypoints(getWaypoints());
        this.ghResponse.add(this.altResponse);
        pathMerger.doWork(this.altResponse, this.pathList, this.encodingManager, translation);
        return true;
    }

    @Override
    public GHRequest getGhRequest() {
        return this.ghRequest;
    }
}
