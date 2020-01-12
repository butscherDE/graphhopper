package com.graphhopper.storage.index;

import com.graphhopper.util.EdgeIterator;
import com.graphhopper.storage.Graph;

import java.util.List;
import java.util.Map;

public class CellRunnerUpdateData {
    private final Graph graph;
    private final VisitedManager localVisitedManager;
    private final VectorAngleCalculator vectorAngleCalculator;
    final int lastEdgeReversedBaseNode;
    final int lastEdgeReversedAdjNode;
    SubNeighborVisitor leftOrRightMostNeighborVisitedChain;
    double leftOrRightMostAngle;
    boolean collinearEdgeFound = false;
    double lastCandidateAngle;

    public CellRunnerUpdateData(Graph graph, VisitedManager localVisitedManager, VectorAngleCalculator vectorAngleCalculator,
                                final List<Integer> nodesOnCell,
                                SubNeighborVisitor leftOrRightMostNeighborVisitedChain) {
        this.graph = graph;
        this.localVisitedManager = localVisitedManager;
        this.vectorAngleCalculator = vectorAngleCalculator;
        this.lastEdgeReversedBaseNode = nodesOnCell.get(nodesOnCell.size() - 1);
        this.lastEdgeReversedAdjNode = nodesOnCell.get(nodesOnCell.size() - 2);
        this.leftOrRightMostNeighborVisitedChain = leftOrRightMostNeighborVisitedChain;
        this.leftOrRightMostAngle = this.vectorAngleCalculator.getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode,
                                                                                         leftOrRightMostNeighborVisitedChain.getLast());
        this.lastCandidateAngle = this.leftOrRightMostAngle;
    }

    public void saveNewChaintIfGreaterAngle(final SubNeighborVisitor candidateEdgeContainingVisitor) {
        final double candidateAngle = vectorAngleCalculator.getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdgeContainingVisitor.getLast());
        if (updateRequired(leftOrRightMostNeighborVisitedChain, leftOrRightMostAngle, candidateEdgeContainingVisitor, candidateAngle)) {
            leftOrRightMostAngle = candidateAngle;
            leftOrRightMostNeighborVisitedChain = candidateEdgeContainingVisitor;
        }
        lastCandidateAngle = candidateAngle;
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
        if (lastCandidateAngle == 0 && areNodesDifferent(neighbors.getAdjNode())) {
            collinearEdgeFound = true;
        }
    }

    private boolean areNodesDifferent(int currentAdjNode) {
        return currentAdjNode != lastEdgeReversedAdjNode;
    }

    public void setCollinearEdgeFound() {
        if (collinearEdgeFound) {
            leftOrRightMostNeighborVisitedChain.collinearEdgeFound();
        }
    }

    public SubNeighborVisitor replaceWithNextNodeHintChainIfApplicable(EdgeIterator neighbors, SubNeighborVisitor subNeighborVisitor, final Map<Integer, Integer> nextNodeHints) {
        if (isNextNodeHintsChainToBuild(neighbors, leftOrRightMostNeighborVisitedChain, nextNodeHints)) {
            leftOrRightMostNeighborVisitedChain = replaceWithNextNodeHintChain(subNeighborVisitor, nextNodeHints);
        }
        return leftOrRightMostNeighborVisitedChain;
    }

    private SubNeighborVisitor replaceWithNextNodeHintChain(SubNeighborVisitor subNeighborVisitor, final Map<Integer, Integer> nextNodeHints) {
        EdgeIterator neighbors = graph.createEdgeExplorer().setBaseNode(lastEdgeReversedBaseNode);
        SubNeighborVisitor leftOrRightMostNeighborVisitedChain = subNeighborVisitor;
        while (nodeHintExists(neighbors, nextNodeHints)) {
            neighbors = findNeighborThatsHintedAsNextNodeAndSaveEdge(neighbors, subNeighborVisitor, nextNodeHints);
        }
        nextNodeHints.clear();
        return leftOrRightMostNeighborVisitedChain;
    }

    private boolean isNextNodeHintsChainToBuild(EdgeIterator neighbors, SubNeighborVisitor leftOrRightMostNeighborVisitedChain, final Map<Integer, Integer> nextNodeHints) {
        return nodeHintExists(neighbors, nextNodeHints) && isEdgePickedAsMostOrientedAlreadyVisited(leftOrRightMostNeighborVisitedChain);
    }

    private boolean isEdgePickedAsMostOrientedAlreadyVisited(SubNeighborVisitor leftOrRightMostNeighborVisitedChain) {
        return localVisitedManager.isEdgeSettled(leftOrRightMostNeighborVisitedChain.getLast());
    }

    private EdgeIterator findNeighborThatsHintedAsNextNodeAndSaveEdge(EdgeIterator neighbors, SubNeighborVisitor subNeighborVisitor, final Map<Integer, Integer> nextNodeHints) {
        while (neighbors.next()) {
            if (isThisEdgeAdjNodeTheHintedNeighbor(neighbors, nextNodeHints)) {
                neighbors = saveEdgeAndCreateNeighborIteratorForAdjNode(neighbors, subNeighborVisitor);
                break;
            }
        }
        return neighbors;
    }

    private boolean isThisEdgeAdjNodeTheHintedNeighbor(EdgeIterator neighbors, final Map<Integer, Integer> nextNodeHints) {
        return neighbors.getAdjNode() == nextNodeHints.get(neighbors.getBaseNode());
    }

    private EdgeIterator saveEdgeAndCreateNeighborIteratorForAdjNode(EdgeIterator neighbors, SubNeighborVisitor subNeighborVisitor) {
        subNeighborVisitor.onEdge(neighbors.detach(false));
        neighbors = graph.createEdgeExplorer().setBaseNode(neighbors.getAdjNode());
        return neighbors;
    }

    private boolean nodeHintExists(EdgeIterator neighbors, final Map<Integer, Integer> nextNodeHints) {
        return nextNodeHints.get(neighbors.getBaseNode()) != null;
    }
}
