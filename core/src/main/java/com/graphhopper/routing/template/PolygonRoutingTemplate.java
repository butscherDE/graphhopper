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
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.Polygon;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.LinkedList;
import java.util.List;

public class PolygonRoutingTemplate extends ViaRoutingTemplate {
    private final Polygon polygon;
    private final GraphHopper gh;
    private final GraphHopperStorage ghStorage;

    public PolygonRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex, GraphHopper gh,
                                  EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);
        this.polygon = ghRequest.getPolygon();
        this.gh = gh;
        this.ghStorage = this.gh.getGraphHopperStorage();
    }

    @Override
    public List<Path> calcPaths(QueryGraph queryGraph, RoutingAlgorithmFactory algoFactory, AlgorithmOptions algoOpts) {
        return routeWithPolygon(algoFactory, queryGraph, algoOpts);
    }

    private List<Path> routeWithPolygon(RoutingAlgorithmFactory tmpAlgoFactory, QueryGraph queryGraph, AlgorithmOptions algoOpts) {
        List<QueryResult> additionalPoints = this.findViaPointsToFullfillPolygonOrientedRouting(algoOpts);

        throw new NotImplementedException();
    }

    private List<QueryResult> findViaPointsToFullfillPolygonOrientedRouting(AlgorithmOptions algoOpts) {
        List<Integer> nodesInPolygon = getNodesInPolygon();
        List<Integer> polygonEntryExitPoints = findPolygonEntryExitPoints(nodesInPolygon);
        List<Integer> pathSkeleton = calculatePathSkeleton(nodesInPolygon, polygonEntryExitPoints, algoOpts);

        throw new NotImplementedException();
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
    private List<Integer> calculatePathSkeleton(List<Integer> subGraphNodes, final List<Integer> polygonEntryExitPoints, AlgorithmOptions algoOpts) {
        DijkstraManyToMany dijkstraMTM = new DijkstraManyToMany(this.ghStorage.getBaseGraph(), algoOpts.getWeighting(), algoOpts.getTraversalMode(), subGraphNodes,
                                                                polygonEntryExitPoints);
        dijkstraMTM.findAllPathsBetweenEntryExitPoints();
        return dijkstraMTM.buildPathSkeleton();
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
}
