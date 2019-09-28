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
    private final Map<Pair<Integer, Integer>, Double> viaPointToEntryExitPointDistances;

    private LOTNodeExtractor(final Graph graph, final RoutingAlgorithmFactory routingAlgorithmFactory, final AlgorithmOptions algorithmOptions, final EdgeExplorer edgeExplorer,
                             final List<Integer> viaPoints, final List<Integer> entryExitPoints) {
        this.graph = graph;
        this.routingAlgorithmFactory = routingAlgorithmFactory;
        this.algorithmOptions = algorithmOptions;
        this.edgeExplorer = edgeExplorer;
        this.viaPoints = viaPoints;
        this.entryExitPoints = entryExitPoints;
        this.viaPointToLOTNodes = new HashMap<>();
        this.viaPointToEntryExitPointDistances = new HashMap<>();

        this.extractData();
    }

    public static LOTNodeExtractor createExtractedData(final Graph graph, final RoutingAlgorithmFactory routingAlgorithmFactory, final AlgorithmOptions algorithmOptions,
                                                       final EdgeExplorer edgeExplorer, final List<Integer> viaPoints, final List<Integer> entryExitPoints) {
        return new LOTNodeExtractor(graph, routingAlgorithmFactory, algorithmOptions, edgeExplorer, viaPoints, entryExitPoints);
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
        final double distance = this.calcDistanceBetween(viaPoint, entryExitPoint);
        this.viaPointToEntryExitPointDistances.put(new Pair<>(viaPoint, entryExitPoint), distance);
    }

    private double calcDistanceBetween(final int viaPoint, final int entryExitPoint) {
        final RoutingAlgorithm routingAlgorithm = this.routingAlgorithmFactory.createAlgo(graph, algorithmOptions);
        final Path path = routingAlgorithm.calcPath(viaPoint, entryExitPoint);
        return path.getDistance();
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
            if (hasPossibleLotNodeShorterDistanceNeighbor(viaPoint, possibleLotNode)) {
                lotNodes.add(possibleLotNode);
            }
        }

        return lotNodes;
    }

    private boolean hasPossibleLotNodeShorterDistanceNeighbor(final int viaPoint, final int possibleLotNode) {
        final EdgeIterator neighborIterator = this.edgeExplorer.setBaseNode(possibleLotNode);
        boolean betterNeighborFound = false;
        final double distanceOfThisPossibleLotNode = this.viaPointToEntryExitPointDistances.get(new Pair<>(viaPoint, possibleLotNode));

        while (neighborIterator.next()) {
            final int neighbor = neighborIterator.getAdjNode();
            final Double otherDistance = this.viaPointToEntryExitPointDistances.get(new Pair<>(viaPoint, neighbor));

            if (otherDistance != null) {
                betterNeighborFound |= distanceOfThisPossibleLotNode > otherDistance;
            }
        }

        return betterNeighborFound;
    }

    public List<Integer> getLotNodesFor(final int viaPoint) {
        return this.viaPointToLOTNodes.get(viaPoint);
    }

    public int size() {
        return this.viaPointToLOTNodes.size();
    }
}
