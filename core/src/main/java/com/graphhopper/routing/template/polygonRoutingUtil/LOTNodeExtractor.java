package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import javafx.util.Pair;

import java.util.*;

/**
 * Takes a set of polygon entry exit points as well as a set of via routing points and extracts the local optimal touch nodes for each point.
 */
public class LOTNodeExtractor {
    private final Graph graph;
    private final RoutingAlgorithmFactory routingAlgorithmFactory;
    private final AlgorithmOptions algorithmOptions;
    private final EdgeExplorer edgeExplorer;
    private final List<Integer> viaPoints;
    private final List<Integer> entryExitPoints;
    private final Map<Integer, List<Integer>> viaPointToLOTNodes;
    private final Map<Pair<Integer, Integer>, Path> viaPointToEntryExitPointPath;

    private LOTNodeExtractor(final Graph graph, final RoutingAlgorithmFactory routingAlgorithmFactory, final AlgorithmOptions algorithmOptions,
                             final List<Integer> viaPoints, final List<Integer> entryExitPoints) {
        this.graph = graph;
        this.routingAlgorithmFactory = routingAlgorithmFactory;
        this.algorithmOptions = algorithmOptions;
        this.edgeExplorer = graph.createEdgeExplorer();
        this.viaPoints = viaPoints;
        this.entryExitPoints = entryExitPoints;
        this.viaPointToLOTNodes = new HashMap<>();
        this.viaPointToEntryExitPointPath = new HashMap<>();

        this.extractData();
    }

    public static LOTNodeExtractor createExtractedData(final Graph graph, final RoutingAlgorithmFactory routingAlgorithmFactory, final AlgorithmOptions algorithmOptions,
                                                       final List<Integer> viaPoints, final List<Integer> entryExitPoints) {
        return new LOTNodeExtractor(graph, routingAlgorithmFactory, algorithmOptions, viaPoints, entryExitPoints);
    }

    private void extractData() {
        this.saveDistancesBetweenAllViaPointsAndEntryExitPoints();
        this.saveLOTNodesForEachViaPoint();
    }

    private void saveDistancesBetweenAllViaPointsAndEntryExitPoints() {
        for (final int viaPoint : this.viaPoints) {
            for (final int entryExitPoint : this.entryExitPoints) {
                saveDistanceBetween(viaPoint, entryExitPoint);
            }
        }
    }

    private void saveDistanceBetween(final int viaPoint, final int entryExitPoint) {
        final Path path = this.calcPathBetween(viaPoint, entryExitPoint);
        this.viaPointToEntryExitPointPath.put(new Pair<>(viaPoint, entryExitPoint), path);
    }

    private Path calcPathBetween(final int viaPoint, final int entryExitPoint) {
        final RoutingAlgorithm routingAlgorithm = this.routingAlgorithmFactory.createAlgo(graph, algorithmOptions);
        final Path path = routingAlgorithm.calcPath(viaPoint, entryExitPoint);
        return path;
    }

    private void saveLOTNodesForEachViaPoint() {
        for (final int viaPoint : this.viaPoints) {
            this.saveLOTNodesFor(viaPoint);
        }
    }

    private void saveLOTNodesFor(final int viaPoint) {
        final List<Integer> lotNodes = createLotNodesFor(viaPoint);

        this.viaPointToLOTNodes.put(viaPoint, lotNodes);
    }

    private List<Integer> createLotNodesFor(final int viaPoint) {
        final List<Integer> lotNodes = new ArrayList<>(entryExitPoints.size());

        for (final int possibleLotNode : this.entryExitPoints) {
            if (!hasPossibleLotNodeShorterDistanceNeighbor(viaPoint, possibleLotNode)) {
                lotNodes.add(possibleLotNode);
            }
        }

        return lotNodes;
    }

    private boolean hasPossibleLotNodeShorterDistanceNeighbor(final int viaPoint, final int possibleLotNode) {
        final EdgeIterator neighborIterator = this.edgeExplorer.setBaseNode(possibleLotNode);
        boolean betterNeighborFound = false;
        final double distanceOfThisPossibleLotNode = this.viaPointToEntryExitPointPath.get(new Pair<>(viaPoint, possibleLotNode)).getDistance();

        while (neighborIterator.next()) {
            final int neighbor = neighborIterator.getAdjNode();
            final Path othersShortestPath = this.viaPointToEntryExitPointPath.get(new Pair<>(viaPoint, neighbor));

            if (othersShortestPath != null) {
                final double otherDistance = othersShortestPath.getDistance();
                betterNeighborFound |= distanceOfThisPossibleLotNode > otherDistance;
            }
        }

        return betterNeighborFound;
    }

    public List<Integer> getLotNodesFor(final int viaPoint) {
        return this.viaPointToLOTNodes.get(viaPoint);
    }

    public Path getLotNodePathFor(final int viaPoint, final int lotNode) {
        return this.viaPointToEntryExitPointPath.get(new Pair<>(viaPoint, lotNode));
    }

    public int size() {
        return this.viaPointToLOTNodes.size();
    }
}
