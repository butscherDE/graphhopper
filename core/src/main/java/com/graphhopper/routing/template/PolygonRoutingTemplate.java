package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.*;
import com.graphhopper.routing.template.polygonRoutingUtil.RouteCandidateList;
import com.graphhopper.routing.template.polygonRoutingUtil.RouteCandidatePolygon;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.PathMerger;
import com.graphhopper.util.Translation;
import com.graphhopper.util.shapes.Polygon;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public abstract class PolygonRoutingTemplate extends ViaRoutingTemplate {
    final GHRequest ghRequest;
    final Polygon polygon;
    final GraphHopperStorage ghStorage;
    final NodeAccess nodeAccess;
    final LocationIndex locationIndex;
    final Graph graph;
    QueryGraph queryGraph;
    AlgorithmOptions algorithmOptions;
    RoutingAlgorithmFactory algoFactory;
    RoutingAlgorithm routingAlgorithm;
    RouteCandidateList<RouteCandidatePolygon> routeCandidates;

    public PolygonRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex, Graph graph, NodeAccess nodeAccess, GraphHopperStorage ghStorage,
                                  EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);
        this.ghRequest = ghRequest;
        this.polygon = ghRequest.getPolygon();
        this.ghStorage = ghStorage;
        this.nodeAccess = nodeAccess;
        this.locationIndex = locationIndex;
        this.graph = graph;
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
        this.queryGraph = queryGraph;
        this.algoFactory = algoFactory;
        this.algorithmOptions = algoOpts;
        this.routingAlgorithm = algoFactory.createAlgo(queryGraph, algoOpts);
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
        final List<Path> bestPath = this.routeCandidates.getFirstAsPathList(1, this.queryGraph, this.algorithmOptions);
        this.pathList.addAll(bestPath);
    }

    private void prepareRouteCandidateList() {
        this.findCandidateRoutes();
        this.routeCandidates.pruneDominatedCandidateRoutes();
        this.pruneLowerQuantileInROIcandidateRoutes();
    }

    private void pruneLowerQuantileInROIcandidateRoutes() {
        // Assumes that routeCandidates was already sorted descending to roi distance after pruning dominated route candidates
        int startIndex = (int) (this.routeCandidates.getCandidates().size() * 0.75) + 1;

        for (int i = startIndex; i < this.routeCandidates.getCandidates().size(); i++) {
            this.routeCandidates.getCandidates().remove(i);
        }
    }

    protected abstract RouteCandidateList findCandidateRoutes();

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
        return this.algoFactory.createAlgo(queryGraph, algorithmOptions);
    }
}
