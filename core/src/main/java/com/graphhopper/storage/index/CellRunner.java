package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.StopWatch;

import java.util.*;

abstract class CellRunner {
    final LinkedList<EdgeIteratorState> edgesOnCell = new LinkedList<>();
    final Graph graph;
    final NodeAccess nodeAccess;
    final VisitedManager localVisitedManager;
    final VisitedManagerDual globalVisitedManager;
    final VectorAngleCalculator vectorAngleCalculator;
    private final EdgeIteratorState startEdge;
    private final EdgeIteratorState endEdge;

    EdgeIteratorState lastNonZeroLengthEdge;

    public CellRunner(final Graph graph, final VisitedManagerDual globalVisitedManager, final VectorAngleCalculator vectorAngleCalculator,
                      final EdgeIteratorState startEdge) {
        this(graph, globalVisitedManager, vectorAngleCalculator, startEdge, startEdge);
    }

    public CellRunner(final Graph graph, final VisitedManagerDual globalVisitedManager, final VectorAngleCalculator vectorAngleCalculator,
                      final EdgeIteratorState startEdge, final EdgeIteratorState endEdge) {
        this.graph = graph;
        this.nodeAccess = graph.getNodeAccess();
        this.localVisitedManager = new VisitedManager(graph);
        this.globalVisitedManager = globalVisitedManager;
        this.vectorAngleCalculator = vectorAngleCalculator;

        this.startEdge = VisitedManager.forceNodeIdsAscending(startEdge);
        this.endEdge = VisitedManager.forceNodeIdsAscending(endEdge);
        this.lastNonZeroLengthEdge = this.startEdge;

    }

    public VisibilityCell extractVisibilityCell() {
        try {
            runAroundCellAndLogNodes();
        } catch (StackOverflowError e) {
            e.printStackTrace();
            System.out.println(this.getClass().getSimpleName());
            System.out.println(extractNodesFromVisitedEdges());
            System.exit(-1);
        }
        return createVisibilityCell();
    }

    private void runAroundCellAndLogNodes() {
        if (hasEdgeEndPointsWithEqualCoordinates(startEdge)) {
            throw new IllegalArgumentException("Cannot start run on an edge with equal coordinates on both end nodes");
        }

        addStartAndEndNodeOfCell();
        boolean endNotReached;
        int i = 0;
        do {
            endNotReached = processNextNeighborOnCell();
            checkRepetition(i);
            i++;
        }
        while (endNotReached);
    }

    private void checkRepetition(int i) {
        if (i == 10000) {
//                System.out.println(i);
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

        boolean cellRunHasNotEnded = settleAllFoundEdgesAndSetWhenRunHasStopped(leftOrRightMostNeighbor);

        return cellRunHasNotEnded;
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
        final boolean edgeIdEqual = true;
        final boolean baseNodeEqual = lastEdge.getBaseNode() == startEdge.getBaseNode();
        final boolean adjNodeEqual = lastEdge.getAdjNode() == startEdge.getAdjNode();
        final boolean sameDirection = baseNodeEqual && adjNodeEqual;
        final boolean edgeEqual = edgeIdEqual && sameDirection;
        return !edgeEqual;
    }

    private EdgeIteratorState getMostLeftOrRightOrientedEdge() {
        final EdgeIteratorState lastEdge = edgesOnCell.getLast();
        final int lastEdgeBaseNode = lastEdge.getBaseNode();
        final int lastEdgeAdjNode = lastEdge.getAdjNode();
        final int ignoreBackwardsEdge = hasEdgeEndPointsWithEqualCoordinates(lastEdge) ? lastEdgeBaseNode : SortedNeighbors.DONT_IGNORE_NODE;
        final SortedNeighbors sortedNeighbors = new SortedNeighbors(graph, lastEdgeAdjNode, ignoreBackwardsEdge, vectorAngleCalculator, lastNonZeroLengthEdge.detach(true));
        final EdgeIteratorState mostOrientedEdge = sortedNeighbors.getMostOrientedEdge();

        if (!hasEdgeEndPointsWithEqualCoordinates(mostOrientedEdge)) {
            this.lastNonZeroLengthEdge = mostOrientedEdge;
        }

        return mostOrientedEdge;
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
