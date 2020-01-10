package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

abstract class CellRunner {

    final LinkedList<Integer> nodesOnCell = new LinkedList<>();
    final Map<Integer, Integer> nextNodeHints = new HashMap<>();
    private final Stack<EdgeIteratorState> lastEdges = new Stack<>();
    private final EdgeExplorer neighborExplorer;
    final NodeAccess nodeAccess;
    final VisitedManager visitedManager;
    private final VectorAngleCalculator vectorAngleCalculator;
    private final EdgeIteratorState startEdge;
    private final int startNode;
    private final int endNode;

    EdgeIteratorState lastEdge;
    EdgeIterator neighbors;

    public CellRunner(final EdgeExplorer neighborExplorer, final NodeAccess nodeAccess, final VisitedManager visitedManager, final VectorAngleCalculator vectorAngleCalculator,
                      final EdgeIteratorState startEdge) {
        this.neighborExplorer = neighborExplorer;
        this.nodeAccess = nodeAccess;
        this.visitedManager = visitedManager;
        this.vectorAngleCalculator = vectorAngleCalculator;

        this.startEdge = this.visitedManager.forceNodeIdsAscending(startEdge);
        this.lastEdge = this.startEdge;
        this.startNode = this.startEdge.getAdjNode();
        this.endNode = this.startEdge.getBaseNode();
    }

    public VisibilityCell runAroundCellAndLogNodes() {
        if (vectorAngleCalculator.getAngle(startEdge.getBaseNode(), startEdge.getAdjNode(), startEdge) == VectorAngleCalculator.ANGLE_WHEN_COORDINATES_ARE_EQUAL) {
            throw new IllegalArgumentException("Cannot start run on an edge with equal coordinates on both end nodes");
        }

        addStartAndEndNodeOfCell();

        if (startEdge.getBaseNode() == 61442 && startEdge.getAdjNode() == 2276168) {
            int i = 0;
        }

        initializeNeighborIterator();
        boolean endNotReached;
        int i = 0;
        do {
            endNotReached = processNextNeighborOnCell();
            if (i == 2_000) {
                System.out.println(i);
                if (RepititionFinder.isRepitition(nodesOnCell, 10)) {
                    System.out.println(this.getClass().getSimpleName());
                    System.out.println(nodesOnCell);
                    System.exit(-1);
                }
            }
            i++;
        }
        while (endNotReached);
//        System.out.println(nodesOnCell);

        return createVisibilityCell();
    }

    private void addStartAndEndNodeOfCell() {
        nodesOnCell.add(endNode);
        nodesOnCell.add(startNode);
        settleEdge(startEdge);

        if (hasNeighborSameCoordinates(startEdge)) {
            lastEdges.push(startEdge);
        }
    }

    private void initializeNeighborIterator() {
        neighbors = neighborExplorer.setBaseNode(startNode);
        neighbors.next();
    }

    private boolean processNextNeighborOnCell() {
        final SubNeighborVisitor leftOrRightmostNeighborChain = getMostLeftOrRightOrientedEdge(neighbors, new SubNeighborVisitor(lastEdge));

        for (EdgeIteratorState edge : leftOrRightmostNeighborChain) {
            if (lastEdgeNotReached(edge)) {
                settledEdge(edge);
            } else {
                final int removedNode = nodesOnCell.removeLast();
//                System.out.println(nodesOnCell);
                return false;
            }
        }

        nextNodeHints.putAll(leftOrRightmostNeighborChain.getNextNodeHints());
        lastEdge = leftOrRightmostNeighborChain.getLast();
        getNextNeighborIterator(leftOrRightmostNeighborChain.getLast());
        return true;
    }

    private void settledEdge(EdgeIteratorState edge) {
        settleEdge(edge);
        nodesOnCell.add(edge.getAdjNode());
    }

    private void getNextNeighborIterator(EdgeIteratorState leftOrRightmostNeighbor) {
        neighbors = neighborExplorer.setBaseNode(leftOrRightmostNeighbor.getAdjNode());
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
        SubNeighborVisitor leftOrRightMostNeighborVisitedChain;
        if (nodeHintExists(neighbors)) {
            leftOrRightMostNeighborVisitedChain = subNeighborVisitor;
            if (neighbors.getAdjNode() == nextNodeHints.get(neighbors.getBaseNode())) {
                subNeighborVisitor.onEdge(neighbors.detach(false));
                neighbors = neighborExplorer.setBaseNode(neighbors.getAdjNode());
            }
            while (nodeHintExists(neighbors)) {
                while (neighbors.next()) {
                    if (neighbors.getAdjNode() == nextNodeHints.get(neighbors.getBaseNode())) {
                        subNeighborVisitor.onEdge(neighbors.detach(false));
                        neighbors = neighborExplorer.setBaseNode(neighbors.getAdjNode());
                        break;
                    }
                }
            }
        } else {
            final int lastEdgeReversedBaseNode = nodesOnCell.get(nodesOnCell.size() - 1);
            final int lastEdgeReversedAdjNode = nodesOnCell.get(nodesOnCell.size() - 2);

            leftOrRightMostNeighborVisitedChain = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());
            double leftOrRightMostAngle = vectorAngleCalculator.getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode,
                                                                                          leftOrRightMostNeighborVisitedChain.getLast());
            if (leftOrRightMostAngle == 0 && neighbors.getAdjNode() != lastEdgeReversedAdjNode) {
                leftOrRightMostNeighborVisitedChain.collinearEdgeFound();
                subNeighborVisitor.collinearEdgeFound();
            }

            while (neighbors.next()) {
                SubNeighborVisitor candidateEdgeContainingVisitor = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());

                final double angleToLastNode =
                        vectorAngleCalculator.getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdgeContainingVisitor.getLast());

                if (angleToLastNode > leftOrRightMostAngle) {
                    leftOrRightMostAngle = angleToLastNode;
                    leftOrRightMostNeighborVisitedChain = candidateEdgeContainingVisitor;
                }

                if (angleToLastNode == 0 && neighbors.getAdjNode() != lastEdgeReversedAdjNode) {
                    candidateEdgeContainingVisitor.collinearEdgeFound();
                    subNeighborVisitor.collinearEdgeFound();
                }
            }
        }

        return leftOrRightMostNeighborVisitedChain;
    }

    private boolean nodeHintExists(EdgeIterator neighbors) {
        return nextNodeHints.get(neighbors.getBaseNode()) != null;
    }

    private EdgeIteratorState skipUntilNotLastEdge(final EdgeIterator neighbors, final int lastEdgeReversedBaseNode, final int lastEdgeReversedAdjNode) {
        EdgeIteratorState skippedReverseEdge = null;

        while (neighbors.getBaseNode() == lastEdgeReversedBaseNode && neighbors.getAdjNode() == lastEdgeReversedAdjNode) {
//            skippedReverseEdge.detach(false);
            neighbors.next();
        }

        return skippedReverseEdge;
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
        final EdgeIterator subNeighborIterator = neighborExplorer.setBaseNode(neighbors.getAdjNode());
        subNeighborIterator.next();
        final SubNeighborVisitor bestSubNeighbor = getMostLeftOrRightOrientedEdge(subNeighborIterator, subNeighborVisitor);
        return bestSubNeighbor;
    }

    abstract VisibilityCell createVisibilityCell();

    abstract void settleEdge(EdgeIteratorState edge);
}
