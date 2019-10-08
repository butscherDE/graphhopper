package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.*;
import com.graphhopper.routing.template.polygonRoutingUtil.RouteCandidateList;
import com.graphhopper.routing.template.polygonRoutingUtil.RouteCandidatePolygon;
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
    RouteCandidateList<RouteCandidatePolygon> routeCandidates;

    PolygonRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex, EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);
        this.ghRequest = ghRequest;
        this.locationIndex = locationIndex;
        this.pathList = new ArrayList<>(ghRequest.getPoints().size() - 1);
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

    private void extractBestPathCandidate() {
        // TODO Maybe more? Dont know what happens in the gui then.
        this.routeCandidates.sortByGainAscending();
        final List<Path> bestPath = this.routeCandidates.getFirstAsPathList(1, this.graph, this.algorithmOptions);
        this.pathList.addAll(bestPath);
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

    protected abstract void findCandidateRoutes();

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

    public RoutingAlgorithm getNewRoutingAlgorithm() {
        return this.algoFactory.createAlgo(this.graph, this.algorithmOptions);
    }
}
