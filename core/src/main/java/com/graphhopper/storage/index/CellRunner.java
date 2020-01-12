package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;

import java.util.*;

abstract class CellRunner {
    final LinkedList<Integer> nodesOnCell = new LinkedList<>();
    final Map<Integer, Integer> nextNodeHints = new HashMap<>();
    private final Stack<EdgeIteratorState> lastEdges = new Stack<>();
    private final Graph graph;
    final NodeAccess nodeAccess;
    final VisitedManager localVisitedManager;
    final VisitedManagerDual globalVisitedManager;
    private final VectorAngleCalculator vectorAngleCalculator;
    private final EdgeIteratorState startEdge;
    private final EdgeIteratorState endEdge;
    private final int startNode;
    private final int endNode;

    EdgeIteratorState lastEdge;
    EdgeIterator neighbors;

    public CellRunner(final Graph graph, final NodeAccess nodeAccess, final VisitedManagerDual globalVisitedManager, final VectorAngleCalculator vectorAngleCalculator,
                      final EdgeIteratorState startEdge) {
        this(graph, nodeAccess, globalVisitedManager, vectorAngleCalculator, startEdge, startEdge);
    }

    public CellRunner(final Graph graph, final NodeAccess nodeAccess, final VisitedManagerDual globalVisitedManager, final VectorAngleCalculator vectorAngleCalculator,
                      final EdgeIteratorState startEdge, final EdgeIteratorState endEdge) {
        this.graph = graph;
        this.nodeAccess = nodeAccess;
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

    public VisibilityCell runAroundCellAndLogNodes() {
        if (vectorAngleCalculator.getAngle(startEdge.getBaseNode(), startEdge.getAdjNode(), startEdge) == VectorAngleCalculator.ANGLE_WHEN_COORDINATES_ARE_EQUAL) {
            throw new IllegalArgumentException("Cannot start run on an edge with equal coordinates on both end nodes");
        }

        addStartAndEndNodeOfCell();

        initializeNeighborIterator();
        boolean endNotReached;
        int i = 0;
        do {
            endNotReached = processNextNeighborOnCell();
            if (i == 10000) {
//                System.out.println(i);
                if (RepititionFinder.isRepitition(nodesOnCell, 10)) {
                    System.out.println(i + ": " + this.getClass().getSimpleName());
                    System.out.println(nodesOnCell);
                    System.exit(-1);
                }
            }
            i++;
        }
        while (endNotReached);

        return createVisibilityCell();
    }

    private void addStartAndEndNodeOfCell() {
        nodesOnCell.add(endNode);
        nodesOnCell.add(startNode);
        markGloballyVisited(startEdge);

        if (hasNeighborSameCoordinates(startEdge)) {
            lastEdges.push(startEdge);
        }
    }

    private void initializeNeighborIterator() {
        neighbors = graph.createEdgeExplorer().setBaseNode(startNode);
        neighbors.next();
    }

    private boolean processNextNeighborOnCell() {
        final SubNeighborVisitor leftOrRightmostNeighborChain = getMostLeftOrRightOrientedEdge(neighbors, new SubNeighborVisitor(lastEdge));
        boolean cellRunHasNotEnded;

        cellRunHasNotEnded = settleAllFoundEdgesAndSetWhenRunHasStopped(leftOrRightmostNeighborChain);

        updateDatastructureForNextEdge(leftOrRightmostNeighborChain);
        return cellRunHasNotEnded;
    }

    private boolean settleAllFoundEdgesAndSetWhenRunHasStopped(SubNeighborVisitor leftOrRightmostNeighborChain) {
        for (EdgeIteratorState edge : leftOrRightmostNeighborChain) {
            if (lastEdgeNotReached(edge)) {
                settleEdge(edge);
            } else {
                final int removedNode = nodesOnCell.removeLast();
//                System.out.println(nodesOnCell);
                return false;
            }
        }
        return true;
    }

    private void updateDatastructureForNextEdge(SubNeighborVisitor leftOrRightmostNeighborChain) {
        nextNodeHints.putAll(leftOrRightmostNeighborChain.getNextNodeHints());
        lastEdge = leftOrRightmostNeighborChain.getLast();
        getNextNeighborIterator(leftOrRightmostNeighborChain.getLast());
    }

    private void settleEdge(EdgeIteratorState edge) {
        markGloballyVisited(edge);
        localVisitedManager.settleEdge(edge);
        nodesOnCell.add(edge.getAdjNode());
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
//        System.out.println(lastEdge + " # " + startEdge);
        return !edgeEqual;
    }

    private SubNeighborVisitor getMostLeftOrRightOrientedEdge(EdgeIterator neighbors, final SubNeighborVisitor subNeighborVisitor) {
        final CellRunnerUpdateData data = createDataStructureWithFirstEdge(neighbors, subNeighborVisitor);
        data.updateCollinearEdgeFound(neighbors);

        while (neighbors.next()) {
            SubNeighborVisitor candidateEdgeContainingVisitor = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());
            data.saveNewChaintIfGreaterAngle(candidateEdgeContainingVisitor);
            data.updateCollinearEdgeFound(neighbors);
        }

        data.setCollinearEdgeFound();
        data.replaceWithNextNodeHintChainIfApplicable(neighbors, subNeighborVisitor, nextNodeHints);

        return data.leftOrRightMostNeighborVisitedChain;
    }

    private CellRunnerUpdateData createDataStructureWithFirstEdge(EdgeIterator neighbors, SubNeighborVisitor subNeighborVisitor) {
        final SubNeighborVisitor leftOrRightMostNeighborVisitedChainStart = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());
        return new CellRunnerUpdateData(graph, localVisitedManager, vectorAngleCalculator, nodesOnCell, leftOrRightMostNeighborVisitedChainStart);
    }

    private SubNeighborVisitor setEdgeToCalcAngleTo(EdgeIterator neighbors, SubNeighborVisitor subNeighborVisitor) {
        SubNeighborVisitor candidateVisitor;

        final EdgeIteratorState detachedNeighbor = neighbors.detach(false);
        subNeighborVisitor.onEdge(detachedNeighbor);
        if (hasNeighborSameCoordinates(neighbors) && !isLastSubIteratedNode(detachedNeighbor)) { // TODO check if hasNeighborSameCoordinates can be called on detachedNeighbor
//            System.out.println("\u001B[31m" + neighbors + "\u001B[30m");
            lastEdges.push(detachedNeighbor);
            candidateVisitor = findMostOrientedNeighborOfNeighbor(neighbors, subNeighborVisitor);
            lastEdges.pop();
        } else {
            candidateVisitor = subNeighborVisitor;
        }
        return candidateVisitor;
    }

    private boolean hasNeighborSameCoordinates(EdgeIteratorState neighbors) {
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

    abstract VisibilityCell createVisibilityCell();

    abstract void markGloballyVisited(EdgeIteratorState edge);
}
