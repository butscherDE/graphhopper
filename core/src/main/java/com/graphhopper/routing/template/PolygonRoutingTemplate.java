package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.*;
import com.graphhopper.routing.template.polygonRoutingUtil.RouteCandidateList;
import com.graphhopper.routing.template.polygonRoutingUtil.RouteCandidate;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.PathMerger;
import com.graphhopper.util.Translation;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public abstract class PolygonRoutingTemplate extends ViaRoutingTemplate {
    private final GHRequest ghRequest;
    final LocationIndex locationIndex;
    NodeAccess nodeAccess;
    QueryGraph graph;
    AlgorithmOptions algorithmOptions;
    RoutingAlgorithmFactory algoFactory;
    RouteCandidateList<RouteCandidate> routeCandidates;

    PolygonRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex, EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);
        this.ghRequest = ghRequest;
        this.locationIndex = locationIndex;
        this.pathList = new ArrayList<>(ghRequest.getPoints().size() - 1);
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
        sb.append("All non pruned route Candidates: ");

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

    protected abstract void findCandidateRoutes();

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
}
