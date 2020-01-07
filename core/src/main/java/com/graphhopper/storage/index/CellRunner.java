package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

abstract class CellRunner {

    final List<Integer> nodesOnCell = new ArrayList<>();
    private Stack<EdgeIteratorState> lastEdges = new Stack<>();
    private final EdgeExplorer neighborExplorer;
    final NodeAccess nodeAccess;
    final VisitedManager visitedManager;
    private final VectorAngleCalculator vectorAngleCalculator;
    private final int startNode;
    private final int currentRunEndNode;

    EdgeIteratorState currentEdge;
    EdgeIterator neighbors;
    int lala = 0;

    public CellRunner(final EdgeExplorer neighborExplorer, final NodeAccess nodeAccess, final VisitedManager visitedManager, final VectorAngleCalculator vectorAngleCalculator,
                      final EdgeIteratorState startEdge) {
        this.neighborExplorer = neighborExplorer;
        this.nodeAccess = nodeAccess;
        this.visitedManager = visitedManager;
        this.vectorAngleCalculator = vectorAngleCalculator;

        currentEdge = startEdge;
        currentEdge = this.visitedManager.forceNodeIdsAscending(currentEdge);
        this.startNode = currentEdge.getAdjNode();
        this.currentRunEndNode = currentEdge.getBaseNode();
    }

    public VisibilityCell runAroundCellAndLogNodes() {
        addStartAndEndNodeOfCell();

        initializeNeighborIterator();
        do {
            processNextNeighborOnCell();
//                    System.out.println(nodesOnCell);
        }
        while (lastCellNotReached());

        return createVisibilityCell();
    }

    private void addStartAndEndNodeOfCell() {
        nodesOnCell.add(currentRunEndNode);
        nodesOnCell.add(startNode);
    }

    private void initializeNeighborIterator() {
        neighbors = neighborExplorer.setBaseNode(startNode);
        neighbors.next();
    }

    private void processNextNeighborOnCell() {
        final SubNeighborVisitor
                leftOrRightmostNeighborChain = getMostLeftOrRightOrientedEdge(neighbors, new SubNeighborVisitor());

        for (EdgeIteratorState edge : leftOrRightmostNeighborChain) {
            settleNextNeighbor(edge);
        }

        getNextNeighborIterator(leftOrRightmostNeighborChain.getLast());
    }

    private void settleNextNeighbor(EdgeIteratorState leftOrRightmostNeighbor) {
        settleEdge(leftOrRightmostNeighbor);
        nodesOnCell.add(leftOrRightmostNeighbor.getAdjNode());
    }

    private void getNextNeighborIterator(EdgeIteratorState leftOrRightmostNeighbor) {
        neighbors = neighborExplorer.setBaseNode(leftOrRightmostNeighbor.getAdjNode());
        neighbors.next();
    }

    private boolean lastCellNotReached() {
        return nodesOnCell.get(nodesOnCell.size() - 1) != currentRunEndNode;
    }

    private SubNeighborVisitor getMostLeftOrRightOrientedEdge(final EdgeIterator neighbors, final SubNeighborVisitor subNeighborVisitor) {
        final int lastEdgeReversedBaseNode = nodesOnCell.get(nodesOnCell.size() - 1);
        final int lastEdgeReversedAdjNode = nodesOnCell.get(nodesOnCell.size() - 2);
        SubNeighborVisitor leftOrRightMostNeighborVisitedChain = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());// null;
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
