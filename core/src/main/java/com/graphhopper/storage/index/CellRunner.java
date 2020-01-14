package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;

import java.util.*;

abstract class CellRunner {
    final LinkedList<EdgeIteratorState> edgesOnCell = new LinkedList<>();
    private final Stack<EdgeIteratorState> lastEdges = new Stack<>();
    final Graph graph;
    final NodeAccess nodeAccess;
    final VisitedManager localVisitedManager;
    final VisitedManagerDual globalVisitedManager;
    final VectorAngleCalculator vectorAngleCalculator;
    private final EdgeIteratorState startEdge;
    private final EdgeIteratorState endEdge;
    private final int startNode;
    private final int endNode;

    EdgeIteratorState lastEdge;
    EdgeIterator neighbors;

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
        this.localVisitedManager.settleEdge(startEdge);
        this.lastEdge = this.startEdge;
        this.startNode = this.startEdge.getAdjNode();
        this.endNode = this.startEdge.getBaseNode();

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
        if (vectorAngleCalculator.getAngle(startEdge, startEdge) == VectorAngleCalculator.ANGLE_WHEN_COORDINATES_ARE_EQUAL) {
            throw new IllegalArgumentException("Cannot start run on an edge with equal coordinates on both end nodes");
        }

        addStartAndEndNodeOfCell();

        initializeNeighborIterator();
        boolean endNotReached;
        int i = 0;
        do {
//            System.out.println(i);
//            System.out.println(nextNodeHints);
//            try {
//                final List<Integer> nodesOnCell = extractNodesFromVisitedEdges();
//                System.out.println(nodesOnCell.subList(nodesOnCell.size() - 10, nodesOnCell.size()));
//            } catch (Exception e) {
//
//            }
            if (extractNodesFromVisitedEdges().get(extractNodesFromVisitedEdges().size() - 1) == 3309699) {
                int j = 0;
            }
            endNotReached = processNextNeighborOnCell();
            if (i == 10000) {
//                System.out.println(i);
                if (RepititionFinder.isRepitition(extractNodesFromVisitedEdges(), 10)) {
                    System.out.println(i + ": " + this.getClass().getSimpleName());
                    System.out.println(extractNodesFromVisitedEdges());
                    System.exit(-1);
                }
            }
            i++;
        }
        while (endNotReached);
    }

    private void addStartAndEndNodeOfCell() {
//        edgesOnCell.add(endNode);
//        edgesOnCell.add(startNode);
        edgesOnCell.add(startEdge);
        markGloballyVisited(startEdge);

        if (hasEdgeEndPointsWithEqualCoordinates(startEdge)) {
            lastEdges.push(startEdge);
        }
    }

    private void initializeNeighborIterator() {
        neighbors = graph.createEdgeExplorer().setBaseNode(startNode);
        neighbors.next();
    }

    private boolean processNextNeighborOnCell() {
        final SubNeighborVisitor leftOrRightmostNeighborChain = getMostLeftOrRightOrientedEdge(neighbors, new SubNeighborVisitor(lastEdge));

        boolean cellRunHasNotEnded = settleAllFoundEdgesAndSetWhenRunHasStopped(leftOrRightmostNeighborChain);

        updateDatastructureForNextEdge(leftOrRightmostNeighborChain);
        return cellRunHasNotEnded;
    }

    private boolean settleAllFoundEdgesAndSetWhenRunHasStopped(SubNeighborVisitor leftOrRightmostNeighborChain) {
        for (EdgeIteratorState edge : leftOrRightmostNeighborChain) {
            if (lastEdgeNotReached(edge)) {
                settleEdge(edge);
            } else {
//                final int removedNode = edgesOnCell.removeLast();
//                System.out.println(nodesOnCell);
                return false;
            }
        }
        return true;
    }

    private void updateDatastructureForNextEdge(SubNeighborVisitor leftOrRightmostNeighborChain) {
        lastEdge = leftOrRightmostNeighborChain.getLast();
        getNextNeighborIterator(leftOrRightmostNeighborChain.getLast());
    }

    private void settleEdge(EdgeIteratorState edge) {
        markGloballyVisited(edge);
        localVisitedManager.settleEdge(edge);
        edgesOnCell.add(edge);
    }

    private void getNextNeighborIterator(EdgeIteratorState leftOrRightmostNeighbor) {
        neighbors = graph.createEdgeExplorer().setBaseNode(leftOrRightmostNeighbor.getAdjNode());
        neighbors.next();
    }

    private boolean lastEdgeNotReached(final EdgeIteratorState lastEdge) {
        final boolean edgeIdEqual = true;
        final boolean baseNodeEqual = lastEdge.getBaseNode() == startEdge.getBaseNode();
        final boolean adjNodeEqual = lastEdge.getAdjNode() == startEdge.getAdjNode();
        final boolean sameDirection = baseNodeEqual && adjNodeEqual;
        final boolean edgeEqual = edgeIdEqual && sameDirection;
        return !edgeEqual;
    }

    private SubNeighborVisitor getMostLeftOrRightOrientedEdge(EdgeIterator neighbors, final SubNeighborVisitor subNeighborVisitor) {
        final CellRunnerUpdateData data = findMostOrientedEdge(neighbors, subNeighborVisitor);

        return data.getLeftOrRightMostNeighborVisitedChain();
    }

    private CellRunnerUpdateData findMostOrientedEdge(EdgeIterator neighbors, SubNeighborVisitor subNeighborVisitor) {
        final CellRunnerUpdateData data = createDataStructureWithFirstEdge(neighbors, subNeighborVisitor);
        data.updateCollinearEdgeFound(neighbors);

        while (neighbors.next()) {
            SubNeighborVisitor candidateEdgeContainingVisitor = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());
            data.saveNewChainIfGreaterAngle(candidateEdgeContainingVisitor);
            data.updateCollinearEdgeFound(neighbors);
        }
        return data;
    }

    private CellRunnerUpdateData createDataStructureWithFirstEdge(EdgeIterator neighbors, SubNeighborVisitor subNeighborVisitor) {
        final SubNeighborVisitor leftOrRightMostNeighborVisitedChainStart = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());
        return new CellRunnerUpdateData(graph, localVisitedManager, vectorAngleCalculator, edgesOnCell, leftOrRightMostNeighborVisitedChainStart);
    }

    private SubNeighborVisitor setEdgeToCalcAngleTo(EdgeIterator neighbors, SubNeighborVisitor subNeighborVisitor) {
        SubNeighborVisitor candidateVisitor;

        final EdgeIteratorState detachedNeighbor = neighbors.detach(false);
        subNeighborVisitor.onEdge(detachedNeighbor);
        if (hasEdgeEndPointsWithEqualCoordinates(neighbors) &&
            !isLastSubIteratedNode(detachedNeighbor)) { // TODO check if hasEdgeEndPointsWithEqualCoordinates can be called on detachedNeighbor
//            System.out.println("\u001B[31m" + neighbors + "\u001B[30m");
            lastEdges.push(detachedNeighbor);
            candidateVisitor = findMostOrientedNeighborOfNeighbor(neighbors, subNeighborVisitor);
            lastEdges.pop();
        } else {
            candidateVisitor = subNeighborVisitor;
        }
        return candidateVisitor;
    }

    private boolean hasEdgeEndPointsWithEqualCoordinates(EdgeIteratorState neighbors) {
        return nodeAccess.getLongitude(neighbors.getBaseNode()) == nodeAccess.getLongitude(neighbors.getAdjNode()) &&
               nodeAccess.getLatitude(neighbors.getBaseNode()) == nodeAccess.getLatitude(neighbors.getAdjNode());
    }

    private boolean isLastSubIteratedNode(final EdgeIteratorState edge) {
        boolean lastEdgeEqualsParameterEdge = false;

        try {
            final EdgeIteratorState lastEdge = lastEdges.peek();
            lastEdgeEqualsParameterEdge |= lastEdge.getBaseNode() == edge.getAdjNode();
            lastEdgeEqualsParameterEdge &= lastEdge.getAdjNode() == edge.getBaseNode();
        } catch (EmptyStackException e) {
            lastEdgeEqualsParameterEdge = false;
        }

        return lastEdgeEqualsParameterEdge;
    }

    private SubNeighborVisitor findMostOrientedNeighborOfNeighbor(EdgeIterator neighbors, final SubNeighborVisitor subNeighborVisitor) {
        final EdgeIterator subNeighborIterator = graph.createEdgeExplorer().setBaseNode(neighbors.getAdjNode());
        subNeighborIterator.next();
        final SubNeighborVisitor bestSubNeighbor = getMostLeftOrRightOrientedEdge(subNeighborIterator, subNeighborVisitor);
        return bestSubNeighbor;
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
