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

        if (startEdge.getEdge() == 769105) {
            int j = 0;
        }

        addStartAndEndNodeOfCell();

        if (startEdge.getBaseNode() == 61442 && startEdge.getAdjNode() == 2276168) {
            int i = 0;
        }

        initializeNeighborIterator();
        boolean endNotReached;
        int i = 0;
        do {
            if (i == 7584) {
                int j = 0;
            }
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
//        System.out.println(nodesOnCell);

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
//        System.out.println(leftOrRightmostNeighborChain.getNextNodeHints());

        for (EdgeIteratorState edge : leftOrRightmostNeighborChain) {
            if (lastEdgeNotReached(edge)) {
                settleEdge(edge);
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
        final int lastEdgeReversedBaseNode = nodesOnCell.get(nodesOnCell.size() - 1);
        final int lastEdgeReversedAdjNode = nodesOnCell.get(nodesOnCell.size() - 2);

        SubNeighborVisitor leftOrRightMostNeighborVisitedChain = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());
        double leftOrRightMostAngle = vectorAngleCalculator.getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode,
                                                                                      leftOrRightMostNeighborVisitedChain.getLast());
        boolean collinearEdgeFound = false;
        if (leftOrRightMostAngle == 0 && areNodesDifferent(neighbors.getAdjNode(), lastEdgeReversedAdjNode)) {
            leftOrRightMostNeighborVisitedChain.collinearEdgeFound();
            subNeighborVisitor.collinearEdgeFound();
        }

        while (neighbors.next()) {
            SubNeighborVisitor candidateEdgeContainingVisitor = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());

            final double angleToLastNode =
                    vectorAngleCalculator.getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdgeContainingVisitor.getLast());

            if (angleToLastNode > leftOrRightMostAngle ||
                (angleToLastNode == leftOrRightMostAngle && candidateEdgeContainingVisitor.size() > leftOrRightMostNeighborVisitedChain.size())) {
                leftOrRightMostAngle = angleToLastNode;
                leftOrRightMostNeighborVisitedChain = candidateEdgeContainingVisitor;
            }

            if (angleToLastNode == 0 && areNodesDifferent(neighbors.getAdjNode(), lastEdgeReversedAdjNode)) {
                collinearEdgeFound = true;
//                candidateEdgeContainingVisitor.collinearEdgeFound();
//                subNeighborVisitor.collinearEdgeFound();
            }
        }

        if (collinearEdgeFound) {
            leftOrRightMostNeighborVisitedChain.collinearEdgeFound();
        }

        if (nodeHintExists(neighbors) && localVisitedManager.isEdgeSettled(leftOrRightMostNeighborVisitedChain.getLast())) {
            neighbors = graph.createEdgeExplorer().setBaseNode(lastEdgeReversedBaseNode);
//            neighbors.next();
            leftOrRightMostNeighborVisitedChain = subNeighborVisitor;
//            if (neighbors.getAdjNode() == nextNodeHints.get(neighbors.getBaseNode())) {
//                subNeighborVisitor.onEdge(neighbors.detach(false));
//                neighbors = graph.createEdgeExplorer().setBaseNode(neighbors.getAdjNode());
//            }
            while (nodeHintExists(neighbors)) {
                while (neighbors.next()) {
                    if (neighbors.getAdjNode() == nextNodeHints.get(neighbors.getBaseNode())) {
                        subNeighborVisitor.onEdge(neighbors.detach(false));
                        neighbors = graph.createEdgeExplorer().setBaseNode(neighbors.getAdjNode());
                        break;
                    }
                }
                nextNodeHints.clear();
            }
        }

        return leftOrRightMostNeighborVisitedChain;
    }

    private boolean areAllNeighborsVisited(int lastEdgeReversedBaseNode) {
        final EdgeIterator neighborExplorer = graph.createEdgeExplorer().setBaseNode(lastEdgeReversedBaseNode);
        boolean allNeighborsVisited = true;

        while (neighborExplorer.next()) {
            allNeighborsVisited &= localVisitedManager.isEdgeSettled(neighborExplorer.detach(false));
        }

        return allNeighborsVisited;
    }

    private boolean areNodesDifferent(int currentAdjNode, int lastEdgeReversedAdjNode) {
        return currentAdjNode != lastEdgeReversedAdjNode/* && nodeAccess.getLat(currentAdjNode) != nodeAccess.getLat(lastEdgeReversedAdjNode) &&
               nodeAccess.getLon(currentAdjNode) != nodeAccess.getLon(lastEdgeReversedAdjNode)*/;
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
        final EdgeIterator subNeighborIterator = graph.createEdgeExplorer().setBaseNode(neighbors.getAdjNode());
        subNeighborIterator.next();
        final SubNeighborVisitor bestSubNeighbor = getMostLeftOrRightOrientedEdge(subNeighborIterator, subNeighborVisitor);
        return bestSubNeighbor;
    }

    abstract VisibilityCell createVisibilityCell();

    abstract void markGloballyVisited(EdgeIteratorState edge);
}
