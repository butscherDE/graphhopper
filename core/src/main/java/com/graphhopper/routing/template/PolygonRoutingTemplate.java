package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.*;
import com.graphhopper.routing.template.PolygonRoutingUtil.RouteCandidate;
import com.graphhopper.routing.template.PolygonRoutingUtil.RouteCandidateList;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.shapes.Polygon;

import java.util.List;

public abstract class PolygonRoutingTemplate extends ViaRoutingTemplate {
    final GHRequest ghRequest;
    final Polygon polygon;
    final GraphHopper gh;
    final GraphHopperStorage ghStorage;
    final NodeAccess nodeAccess;
    QueryGraph queryGraph;
    AlgorithmOptions algorithmOptions;
    RoutingAlgorithmFactory algoFactory;
    RoutingAlgorithm routingAlgorithm;
    RouteCandidateList routeCandidates;

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
        this.algorithmOptions = algoOpts;
        this.routingAlgorithm = algoFactory.createAlgo(queryGraph, algoOpts);
        this.routeCandidates = RouteCandidateList.createEmptyCandidateList();
        return routeWithPolygon();
    }

    private List<Path> routeWithPolygon() {
        this.findCandidateRoutes();
        this.pruneDominatedCandidateRoutes();
        this.pruneLowerQuantileInROIcandidateRoutes();


        // TODO Maybe more? Dont know what happens in the gui then.
        this.routeCandidates.sortByGainAscending();
        return this.routeCandidates.getFirstAsPathList(1, this.queryGraph, this.algorithmOptions);
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
        for (int i = currentPruningCandidateIndex - 1; i >= 0 && !foundDominatingPath; i++) {
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
        final int routeCandidatesSize = this.routeCandidates.candidates.size();
        int startIndex = (int) (routeCandidatesSize * 0.75) + 1;

        for (int i = startIndex; i < routeCandidatesSize; i++) {
            this.routeCandidates.candidates.remove(i);
        }
    }

    abstract RouteCandidateList findCandidateRoutes();
}
