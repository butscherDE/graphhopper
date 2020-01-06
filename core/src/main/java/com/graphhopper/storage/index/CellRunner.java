package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

abstract class CellRunner {
    final static double ANGLE_WHEN_COORDINATES_ARE_EQUAL = -Double.MAX_VALUE;

    private VisibilityCellsCreator visibilityCellsCreator;
    final List<Integer> nodesOnCell = new ArrayList<>();
    private Stack<EdgeIteratorState> lastEdges = new Stack<>();
    private final EdgeExplorer neighborExplorer;
    final NodeAccess nodeAccess;
    final VisitedManager visitedManager;
    int lala = 0;

    public CellRunner(final VisibilityCellsCreator visibilityCellsCreator, final EdgeExplorer neighborExplorer, final NodeAccess nodeAccess, final VisitedManager visitedManager) {
        this.visibilityCellsCreator = visibilityCellsCreator;
        this.neighborExplorer = neighborExplorer;
        this.nodeAccess = nodeAccess;
        this.visitedManager = visitedManager;
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
        nodesOnCell.add(visibilityCellsCreator.currentRunEndNode);
        nodesOnCell.add(visibilityCellsCreator.currentRunStartNode);
    }

    private void initializeNeighborIterator() {
        visibilityCellsCreator.neighbors = neighborExplorer.setBaseNode(visibilityCellsCreator.currentRunStartNode);
        visibilityCellsCreator.neighbors.next();
    }

    private void processNextNeighborOnCell() {
        final SubNeighborVisitor
                leftOrRightmostNeighborChain = getMostLeftOrRightOrientedEdge(visibilityCellsCreator.neighbors, new SubNeighborVisitor());

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
        visibilityCellsCreator.neighbors = neighborExplorer.setBaseNode(leftOrRightmostNeighbor.getAdjNode());
        visibilityCellsCreator.neighbors.next();
    }

    private boolean lastCellNotReached() {
        return nodesOnCell.get(nodesOnCell.size() - 1) != visibilityCellsCreator.currentRunEndNode;
    }

    private SubNeighborVisitor getMostLeftOrRightOrientedEdge(final EdgeIterator neighbors, final SubNeighborVisitor subNeighborVisitor) {
        final int lastEdgeReversedBaseNode = nodesOnCell.get(nodesOnCell.size() - 1);
        final int lastEdgeReversedAdjNode = nodesOnCell.get(nodesOnCell.size() - 2);
        SubNeighborVisitor leftOrRightMostNeighborVisitedChain = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());// null;
        double leftOrRightMostAngle = getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, leftOrRightMostNeighborVisitedChain.getLast());
        //-Double.MAX_VALUE;
        while (neighbors.next()) {
//                do {
            SubNeighborVisitor candidateEdgeContainingVisitor = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());

            final double angleToLastNode = getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdgeContainingVisitor.getLast());

            if (angleToLastNode >= leftOrRightMostAngle) {
                leftOrRightMostAngle = angleToLastNode;
                leftOrRightMostNeighborVisitedChain = candidateEdgeContainingVisitor;
            }
        }
//                while (neighbors.next());

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

    abstract double getAngleOfVectorsOriented(int lastEdgeReversedBaseNode, int lastEdgeReversedAdjNode, final EdgeIteratorState candidateEdge);

    abstract VisibilityCell createVisibilityCell();

    abstract void settleEdge(EdgeIteratorState edge);

    double getAngle(final int lastEdgeReversedBaseNode, final int lastEdgeReversedAdjNode, final EdgeIteratorState candidateEdge) {
        try {
            return getAngleAfterErrorHandling(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdge);
        } catch (IllegalArgumentException e) {
            return ANGLE_WHEN_COORDINATES_ARE_EQUAL;
        }
    }

    private double getAngleAfterErrorHandling(int lastEdgeReversedBaseNode, int lastEdgeReversedAdjNode, EdgeIteratorState candidateEdge) {
        final Vector2D lastEdgeVector = createLastEdgeVector(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode);
        final Vector2D candidateEdgeVector = createCandidateEdgeVector(candidateEdge);

        final double angleTo = lastEdgeVector.angleTo(candidateEdgeVector);
        final double angleToContinuousInterval = transformAngleToContinuousInterval(angleTo);
        final double differenceToTwoPi = Math.abs(2 * Math.PI - angleToContinuousInterval);
        final double angleToZeroIfVeryCloseTo2Pi = differenceToTwoPi < 0.000001 ? 0 : angleToContinuousInterval;

        return angleToZeroIfVeryCloseTo2Pi;
    }

    private Vector2D createLastEdgeVector(int lastEdgeReversedBaseNode, int lastEdgeReversedAdjNode) {
        final Coordinate lastEdgeBaseNodeCoordinate = new Coordinate(nodeAccess.getLongitude(lastEdgeReversedBaseNode), nodeAccess.getLatitude(lastEdgeReversedBaseNode));
        final Coordinate lastEdgeAdjNodeCoordinate = new Coordinate(nodeAccess.getLongitude(lastEdgeReversedAdjNode), nodeAccess.getLatitude(lastEdgeReversedAdjNode));
        return new Vector2D(lastEdgeBaseNodeCoordinate, lastEdgeAdjNodeCoordinate);
    }

    private Vector2D createCandidateEdgeVector(EdgeIteratorState candidateEdge) {
        final Coordinate candidateEdgeBaseNodeCoordinate = new Coordinate(nodeAccess.getLongitude(candidateEdge.getBaseNode()),
                                                                          nodeAccess.getLatitude(candidateEdge.getBaseNode()));
        final Coordinate candidateEdgeAdjNodeCoordinate = new Coordinate(nodeAccess.getLongitude(candidateEdge.getAdjNode()),
                                                                         nodeAccess.getLatitude(candidateEdge.getAdjNode()));
        if (candidateEdgeAdjNodeCoordinate.equals2D(candidateEdgeBaseNodeCoordinate)) {
            throw new IllegalArgumentException("Coordinates of both edge end points shall not be equal");
        }
        return new Vector2D(candidateEdgeBaseNodeCoordinate, candidateEdgeAdjNodeCoordinate);
    }

    private double transformAngleToContinuousInterval(final double angleTo) {
        return angleTo > 0 ? angleTo : angleTo + 2 * Math.PI;
    }
}
