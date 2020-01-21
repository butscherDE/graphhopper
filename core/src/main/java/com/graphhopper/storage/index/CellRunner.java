package com.graphhopper.storage.index;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIteratorState;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

abstract class CellRunner {
    final LinkedList<EdgeIteratorState> edgesOnCell = new LinkedList<>();
    private final Graph graph;
    final NodeAccess nodeAccess;
    private final VisitedManager localVisitedManager;
    final VisitedManagerDual globalVisitedManager;
    private final VectorAngleCalculator vectorAngleCalculator;
    private final EdgeIteratorState startEdge;
    private final Map<Integer, SortedNeighbors> sortedNeighborsMap;

    private EdgeIteratorState lastNonZeroLengthEdge;


    CellRunner(final Graph graph, final VisitedManagerDual globalVisitedManager, final VectorAngleCalculator vectorAngleCalculator,
               final EdgeIteratorState startEdge, Map<Integer, SortedNeighbors> sortedNeighborsMap) {
        this.graph = graph;
        this.nodeAccess = graph.getNodeAccess();
        this.localVisitedManager = new VisitedManager(graph);
        this.globalVisitedManager = globalVisitedManager;
        this.vectorAngleCalculator = vectorAngleCalculator;

        this.startEdge = VisitedManager.forceNodeIdsAscending(startEdge);
        this.sortedNeighborsMap = sortedNeighborsMap;
        this.lastNonZeroLengthEdge = this.startEdge;
    }

    public VisibilityCell extractVisibilityCell() {
        runAroundCellAndLogNodes();
        return createVisibilityCell();
    }

    private void runAroundCellAndLogNodes() {
        failOnLengthZeroStartEdge();

        addStartAndEndNodeOfCell();
        run();
    }

    private void failOnLengthZeroStartEdge() {
        if (hasEdgeEndPointsWithEqualCoordinates(startEdge)) {
            throw new IllegalArgumentException("Cannot start run on an edge with equal coordinates on both end nodes");
        }
    }

    private void run() {
        boolean endNotReached;
        do {
            endNotReached = processNextNeighborOnCell();
        }
        while (endNotReached);
    }

    private void checkRepetition(int i) {
        if (i == 1000) {
            if (RepititionFinder.isRepitition(extractNodesFromVisitedEdges(), 10)) {
                System.out.println(i + ": " + this.getClass().getSimpleName());
                System.out.println(extractNodesFromVisitedEdges());
                System.exit(-1);
            }
        }
    }

    private void addStartAndEndNodeOfCell() {
        edgesOnCell.add(startEdge);
        markGloballyVisited(startEdge);
    }

    private boolean processNextNeighborOnCell() {
        final EdgeIteratorState leftOrRightMostNeighbor = getMostLeftOrRightOrientedEdge();

        return settleAllFoundEdgesAndSetWhenRunHasStopped(leftOrRightMostNeighbor);
    }

    private boolean settleAllFoundEdgesAndSetWhenRunHasStopped(EdgeIteratorState edge) {
        if (lastEdgeNotReached(edge)) {
            settleEdge(edge);
            return true;
        } else {
            return false;
        }
    }

    private void settleEdge(EdgeIteratorState edge) {
        localVisitedManager.settleEdge(edge);
        markGloballyVisited(edge);
        edgesOnCell.add(edge);
    }

    private boolean lastEdgeNotReached(final EdgeIteratorState lastEdge) {
        final boolean baseNodeEqual = lastEdge.getBaseNode() == startEdge.getBaseNode();
        final boolean adjNodeEqual = lastEdge.getAdjNode() == startEdge.getAdjNode();
        final boolean sameDirection = baseNodeEqual && adjNodeEqual;
        return !sameDirection;
    }

    private EdgeIteratorState getMostLeftOrRightOrientedEdge() {
        final EdgeIteratorState lastEdge = edgesOnCell.getLast();
        final int ignoreBackwardsEdge = getIgnoreBackwardsEdge(lastEdge);
        final EdgeIteratorState mostOrientedEdge = getMostOrientedEdgeFromSortedNeighbors(lastEdge, ignoreBackwardsEdge);

        updateLastNonZeroLengthEdge(mostOrientedEdge);

        return mostOrientedEdge;
    }

    private int getIgnoreBackwardsEdge(EdgeIteratorState lastEdge) {
        final int lastEdgeBaseNode = lastEdge.getBaseNode();
        return hasEdgeEndPointsWithEqualCoordinates(lastEdge) ? lastEdgeBaseNode : SortedNeighbors.DO_NOT_IGNORE_NODE;
    }

    private EdgeIteratorState getMostOrientedEdgeFromSortedNeighbors(EdgeIteratorState lastEdge, int ignoreBackwardsEdge) {
        final int lastEdgeAdjNode = lastEdge.getAdjNode();
        final SortedNeighbors sortedNeighbors = sortedNeighborsMap.get(lastEdgeAdjNode); //new SortedNeighbors(graph, lastEdgeAdjNode, ignoreBackwardsEdge, vectorAngleCalculator);
        return sortedNeighbors.getMostOrientedEdge(lastNonZeroLengthEdge.detach(true));
    }

    private void updateLastNonZeroLengthEdge(EdgeIteratorState mostOrientedEdge) {
        if (!hasEdgeEndPointsWithEqualCoordinates(mostOrientedEdge)) {
            this.lastNonZeroLengthEdge = mostOrientedEdge;
        }
    }

    private boolean hasEdgeEndPointsWithEqualCoordinates(EdgeIteratorState edge) {
        final double baseNodeLongitude = nodeAccess.getLongitude(edge.getBaseNode());
        final double adjNodeLongitude = nodeAccess.getLongitude(edge.getAdjNode());
        final boolean longitudeEqual = baseNodeLongitude == adjNodeLongitude;

        final double baseNodeLatitude = nodeAccess.getLatitude(edge.getBaseNode());
        final double adjNodeLatitude = nodeAccess.getLatitude(edge.getAdjNode());
        final boolean latitudeEqual = baseNodeLatitude == adjNodeLatitude;

        return longitudeEqual && latitudeEqual;
    }

    List<Integer> extractNodesFromVisitedEdges() {
        final List<Integer> nodesOnCell = new LinkedList<>();

        for (EdgeIteratorState edgeIteratorState : edgesOnCell) {
            nodesOnCell.add(edgeIteratorState.getBaseNode());
        }

        return nodesOnCell;
    }

    abstract VisibilityCell createVisibilityCell();

    abstract void markGloballyVisited(EdgeIteratorState edge);
}
