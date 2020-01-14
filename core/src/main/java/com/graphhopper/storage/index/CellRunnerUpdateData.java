package com.graphhopper.storage.index;

import com.graphhopper.util.EdgeIterator;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.LinkedList;
import java.util.List;

public class CellRunnerUpdateData {
    private final Graph graph;
    private final VisitedManager localVisitedManager;
    private final VectorAngleCalculator vectorAngleCalculator;
    final int lastEdgeReversedBaseNode;
    final int lastEdgeReversedAdjNode;
    private SubNeighborVisitor leftOrRightMostNeighborVisitedChain;
    private final List<SubNeighborVisitor> allNeighbors = new LinkedList<>();
    private double leftOrRightMostAngle;
    private boolean collinearEdgeFound = false;
    private double lastCandidateAngle;

    public CellRunnerUpdateData(Graph graph, VisitedManager localVisitedManager, VectorAngleCalculator vectorAngleCalculator,
                                final LinkedList<EdgeIteratorState> edgesOnCell,
                                SubNeighborVisitor leftOrRightMostNeighborVisitedChain) {
        this.graph = graph;
        this.localVisitedManager = localVisitedManager;
        this.vectorAngleCalculator = vectorAngleCalculator;
        this.lastEdgeReversedBaseNode = edgesOnCell.getLast().getAdjNode();
        this.lastEdgeReversedAdjNode = edgesOnCell.getLast().getBaseNode();
        this.leftOrRightMostNeighborVisitedChain = leftOrRightMostNeighborVisitedChain;
        this.leftOrRightMostAngle = this.vectorAngleCalculator.getAngleOfVectorsOriented(edgesOnCell.getLast().detach(true), leftOrRightMostNeighborVisitedChain.getLast());
        this.lastCandidateAngle = this.leftOrRightMostAngle;
        allNeighbors.add(leftOrRightMostNeighborVisitedChain);
    }

    public void saveNewChainIfGreaterAngle(final SubNeighborVisitor candidateEdgeContainingVisitor) {
//        allNeighbors.add(candidateEdgeContainingVisitor);
//        final double candidateAngle = vectorAngleCalculator.getAngleOfVectorsOriented();
//        if (updateRequired(leftOrRightMostNeighborVisitedChain, leftOrRightMostAngle, candidateEdgeContainingVisitor, candidateAngle)) {
//            leftOrRightMostAngle = candidateAngle;
//            leftOrRightMostNeighborVisitedChain = candidateEdgeContainingVisitor;
//        }
//        lastCandidateAngle = candidateAngle;
    }

    private boolean updateRequired(SubNeighborVisitor leftOrRightMostNeighborVisitedChain, double leftOrRightMostAngle, SubNeighborVisitor candidateEdgeContainingVisitor,
                                   double angleToLastNode) {
        return isCandidateAngleGreater(leftOrRightMostAngle, angleToLastNode) ||
               isAngleEqualButVisitedChainLonger(leftOrRightMostNeighborVisitedChain, leftOrRightMostAngle, candidateEdgeContainingVisitor, angleToLastNode);
    }

    private boolean isAngleEqualButVisitedChainLonger(SubNeighborVisitor leftOrRightMostNeighborVisitedChain, double leftOrRightMostAngle,
                                                      SubNeighborVisitor candidateEdgeContainingVisitor, double angleToLastNode) {
        return isCandidateAngleEqual(leftOrRightMostAngle, angleToLastNode) && isCandidateVisitedChainLonger(leftOrRightMostNeighborVisitedChain, candidateEdgeContainingVisitor);
    }

    private boolean isCandidateVisitedChainLonger(SubNeighborVisitor leftOrRightMostNeighborVisitedChain, SubNeighborVisitor candidateEdgeContainingVisitor) {
        return candidateEdgeContainingVisitor.size() > leftOrRightMostNeighborVisitedChain.size();
    }

    private boolean isCandidateAngleEqual(double leftOrRightMostAngle, double angleToLastNode) {
        return angleToLastNode == leftOrRightMostAngle;
    }

    private boolean isCandidateAngleGreater(double leftOrRightMostAngle, double angleToLastNode) {
        return angleToLastNode > leftOrRightMostAngle;
    }


    public void updateCollinearEdgeFound(EdgeIterator neighbors) {
        if (lastCandidateAngle == 0 && areAdjNodesDifferent(neighbors.getAdjNode())) {
            collinearEdgeFound = true;
        }
    }

    private boolean areAdjNodesDifferent(int currentAdjNode) {
        return currentAdjNode != lastEdgeReversedAdjNode;
    }

    public void setCollinearEdgeFound() {
        if (collinearEdgeFound) {
            leftOrRightMostNeighborVisitedChain.collinearEdgeFound();
        }
    }

    public SubNeighborVisitor getLeftOrRightMostNeighborVisitedChain() {
        return leftOrRightMostNeighborVisitedChain;
    }

    public List<SubNeighborVisitor> getAllNeighbors() {
        return allNeighbors;
    }

    public double getLeftOrRightMostAngle() {
        return leftOrRightMostAngle;
    }
}
