package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

abstract class CellRunner {

    final LinkedList<Integer> nodesOnCell = new LinkedList<>();
    private Stack<EdgeIteratorState> lastEdges = new Stack<>();
    private final EdgeExplorer neighborExplorer;
    final NodeAccess nodeAccess;
    final VisitedManager visitedManager;
    private final VectorAngleCalculator vectorAngleCalculator;
    private final int startNode;
    private final int endNode;

    private final EdgeIteratorState startEdge;
    EdgeIteratorState lastEdge;
    EdgeIterator neighbors;
    int lala = 0;

    public CellRunner(final EdgeExplorer neighborExplorer, final NodeAccess nodeAccess, final VisitedManager visitedManager, final VectorAngleCalculator vectorAngleCalculator,
                      final EdgeIteratorState startEdge) {
        this.neighborExplorer = neighborExplorer;
        this.nodeAccess = nodeAccess;
        this.visitedManager = visitedManager;
        this.vectorAngleCalculator = vectorAngleCalculator;

        this.startEdge = this.visitedManager.forceNodeIdsAscending(startEdge);
        this.lastEdge = startEdge;
        this.startNode = startEdge.getAdjNode();
        this.endNode = startEdge.getBaseNode();
    }

    public VisibilityCell runAroundCellAndLogNodes() {
        addStartAndEndNodeOfCell();

        initializeNeighborIterator();
        boolean endNotReached = true;
        do {
            endNotReached = processNextNeighborOnCell();
//                    System.out.println(nodesOnCell);
        }
        while (endNotReached);

        return createVisibilityCell();
    }

    private void addStartAndEndNodeOfCell() {
        nodesOnCell.add(endNode);
        nodesOnCell.add(startNode);
        settleEdge(startEdge);
    }

    private void initializeNeighborIterator() {
        neighbors = neighborExplorer.setBaseNode(startNode);
        neighbors.next();
    }

    private boolean processNextNeighborOnCell() {
        final SubNeighborVisitor leftOrRightmostNeighborChain = getMostLeftOrRightOrientedEdge(neighbors, new SubNeighborVisitor());

        for (EdgeIteratorState edge : leftOrRightmostNeighborChain) {
            if (lastEdgeNotReached(edge)) {
                settleNextNeighbor(edge);
            } else {
                nodesOnCell.removeLast();
                return false;
            }
        }

        getNextNeighborIterator(leftOrRightmostNeighborChain.getLast());
        return true;
    }

    private void settleNextNeighbor(EdgeIteratorState leftOrRightmostNeighbor) {
        settleEdge(leftOrRightmostNeighbor);
        nodesOnCell.add(leftOrRightmostNeighbor.getAdjNode());
    }

    private void getNextNeighborIterator(EdgeIteratorState leftOrRightmostNeighbor) {
        neighbors = neighborExplorer.setBaseNode(leftOrRightmostNeighbor.getAdjNode());
        neighbors.next();
    }

    private boolean lastEdgeNotReached(final EdgeIteratorState lastEdge) {
        System.out.println(lastEdge + " ### " + startEdge);
        final boolean edgeIdEqual = lastEdge.getEdge() == startEdge.getEdge();
        final boolean baseNodeEqual = lastEdge.getBaseNode() == startEdge.getBaseNode();
        final boolean adjNodeEqual = lastEdge.getAdjNode() == startEdge.getAdjNode();
        final boolean sameDirection = baseNodeEqual && adjNodeEqual;
        final boolean edgeEqual = edgeIdEqual && sameDirection;
        return !edgeEqual;
//        return nodesOnCell.get(nodesOnCell.size() - 1) != endNode;
    }

    private SubNeighborVisitor getMostLeftOrRightOrientedEdge(final EdgeIterator neighbors, final SubNeighborVisitor subNeighborVisitor) {
        final int lastEdgeReversedBaseNode = nodesOnCell.get(nodesOnCell.size() - 1);
        final int lastEdgeReversedAdjNode = nodesOnCell.get(nodesOnCell.size() - 2);
        SubNeighborVisitor leftOrRightMostNeighborVisitedChain = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());
        double leftOrRightMostAngle = vectorAngleCalculator.getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode,
                                                                                      leftOrRightMostNeighborVisitedChain.getLast());

        while (neighbors.next()) {
            SubNeighborVisitor candidateEdgeContainingVisitor = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());

            final double angleToLastNode =
                    vectorAngleCalculator.getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdgeContainingVisitor.getLast());

            if (angleToLastNode >= leftOrRightMostAngle) {
                leftOrRightMostAngle = angleToLastNode;
                leftOrRightMostNeighborVisitedChain = candidateEdgeContainingVisitor;
            }
        }

        if (leftOrRightMostNeighborVisitedChain == null) {
            System.out.println();
        }

        return leftOrRightMostNeighborVisitedChain;
    }

    private SubNeighborVisitor setEdgeToCalcAngleTo(EdgeIterator neighbors, SubNeighborVisitor subNeighborVisitor) {
        SubNeighborVisitor candidateVisitor;

        final EdgeIteratorState detachedNeighbor = neighbors.detach(false);
        subNeighborVisitor.onEdge(detachedNeighbor);
        if (hasNeighborSameCoordinates(neighbors) && !isLastNode(detachedNeighbor)) {
//                    if (neighbors.getBaseNode() == 7254909 && neighbors.getAdjNode() == 93620) {
//                        System.out.println("");
//                        if (++lala == 10) {
//                            System.out.println(nodesOnCell.toString());
//                            final NodesAndNeighborDump nnd = new NodesAndNeighborDump(graph, Arrays.asList(new Integer[] {7254909, 93620, 48452, 5348789, 3560230, 5880975,
//                                                                                                                          9085088, 5880975, 48464, 47591, 47260, 47261}));
//                            nnd.dump();
//                            SwingGraphGUI gui = new SwingGraphGUI(nnd.getNodes(), nnd.getEdges());
//                            gui.visualizeGraph();
//                            try {
//                                Thread.sleep(100000);
//                            } catch (Exception e) {
//
//                            }
//                            System.exit(-1);
//                        }
//                    }
            System.out.println("\u001B[31m" + neighbors + "\u001B[30m");
            lastEdges.push(detachedNeighbor);
            candidateVisitor = findMostOrientedNeighborOfNeighbor(neighbors, subNeighborVisitor);
            lastEdges.pop();
        } else {
            candidateVisitor = subNeighborVisitor;
        }
        return candidateVisitor;
    }

    private boolean hasNeighborSameCoordinates(EdgeIterator neighbors) {
        return nodeAccess.getLongitude(neighbors.getBaseNode()) == nodeAccess.getLongitude(neighbors.getAdjNode()) &&
               nodeAccess.getLatitude(neighbors.getBaseNode()) == nodeAccess.getLatitude(neighbors.getAdjNode());
    }

    private boolean isLastNode(final EdgeIteratorState edge) {
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
