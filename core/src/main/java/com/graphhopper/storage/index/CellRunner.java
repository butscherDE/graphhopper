package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;

import java.util.*;

abstract class CellRunner {
    final LinkedList<EdgeIteratorState> edgesOnCell = new LinkedList<>();
    final Map<Integer, Integer> nextNodeHints = new HashMap<>();
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
        if (vectorAngleCalculator.getAngle(startEdge.getBaseNode(), startEdge.getAdjNode(), startEdge) == VectorAngleCalculator.ANGLE_WHEN_COORDINATES_ARE_EQUAL) {
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
        final SubNeighborVisitor leftOrRightmostNeighborChain = getMostLeftOrRightOrientedEdge(neighbors, new S bNeighborVisitor(lastEdge));

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
        nextNodeHints.putAll(leftOrRightmostNeighborChain.getNextNodeHints());
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
//        System.out.println(lastEdge + " # " + startEdge);
        return !edgeEqual;
    }

    private SubNeighborVisitor getMostLeftOrRightOrientedEdge(EdgeIterator neighbors, final SubNeighborVisitor subNeighborVisitor) {
        final CellRunnerUpdateData data = findMostOrientedEdge(neighbors, subNeighborVisitor);

        boolean replacedWithNextNodeHints = replaceWithNextNodeHintsIfApplicable(neighbors, subNeighborVisitor, data);

        return addSubRunIfMultipleMostOrientedEdges(data.getLeftOrRightMostNeighborVisitedChain(), data, replacedWithNextNodeHints);
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

    private boolean replaceWithNextNodeHintsIfApplicable(EdgeIterator neighbors, SubNeighborVisitor subNeighborVisitor, CellRunnerUpdateData data) {
        data.setCollinearEdgeFound();
        return data.replaceWithNextNodeHintChainIfApplicable(neighbors, subNeighborVisitor, nextNodeHints);
    }

    private SubNeighborVisitor addSubRunIfMultipleMostOrientedEdges(SubNeighborVisitor subNeighborVisitor, CellRunnerUpdateData data, boolean replacedWithNextNodeHints) {
        final List<SubNeighborVisitor> collinearEdges = getAllEdgesCollinearToMostOrientedFoundEdge(data);

        if (isMultipleWaysToContinueFound(collinearEdges) && !replacedWithNextNodeHints) {
            return startSubRunsOnAllCollinearEdgesOfMostOrientedFoundEdge(subNeighborVisitor, data, collinearEdges);
        } else {
            return subNeighborVisitor;
        }
    }

    private SubNeighborVisitor startSubRunsOnAllCollinearEdgesOfMostOrientedFoundEdge(SubNeighborVisitor subNeighborVisitor, CellRunnerUpdateData data,
                                                                                      List<SubNeighborVisitor> collinearEdges) {
//        final List<CellRunner> subRunners = new LinkedList<>();
//        for (SubNeighborVisitor collinearEdge : collinearEdges) {
//            initSubRun(subRunners, collinearEdge);
//        }
//
//        boolean notStopped = true;
//        while (notStopped) {
//            for (CellRunner subRunner : subRunners) {
//                notStopped &= subRunner.processNextNeighborOnCell();
//                if (!notStopped) {
//                    for (EdgeIteratorState edgeIteratorState : subRunner.edgesOnCell) {
//                        data.getLeftOrRightMostNeighborVisitedChain().onEdge(edgeIteratorState);;
//                    }
//                    break;
//                }
//            }
//        }

        final List<Double> angles = new ArrayList<>(collinearEdges.size());
        for (SubNeighborVisitor collinearEdge : collinearEdges) {
            if (collinearEdge.getLast().getBaseNode() != subNeighborVisitor.getLast().getAdjNode() && collinearEdge.getLast().getAdjNode() != subNeighborVisitor.getLast().getBaseNode()) {
                final EdgeIterator collinearSubIterationNeighborIterator = graph.createEdgeExplorer().setBaseNode(collinearEdge.getLast().getAdjNode());
                collinearSubIterationNeighborIterator.next();
                final SubNeighborVisitor subSubNeighborVisitor = subNeighborVisitor.clone();
                subSubNeighborVisitor.onEdge(collinearEdge.getLast());
                final SubNeighborVisitor mostOrientedSubSubNeighborChain = getMostLeftOrRightOrientedEdge(collinearSubIterationNeighborIterator, subSubNeighborVisitor);

                final double angle = vectorAngleCalculator.getAngleOfVectorsOriented(edgesOnCell.getLast().getAdjNode(), edgesOnCell.getLast().getBaseNode(),
                                                                                     mostOrientedSubSubNeighborChain.getLast());
                angles.add(angle);
            } else {
                angles.add(-Double.MAX_VALUE);
            }
        }

        final Double maxAngle = Collections.max(angles);
        final int indexOfMaxAngle = angles.indexOf(maxAngle);
        return collinearEdges.get(indexOfMaxAngle);
    }

    private void initSubRun(List<CellRunner> subRunners, SubNeighborVisitor collinearEdge) {
        final CellRunner newSubRunner = createNewSubRunner(collinearEdge.getLast(), endEdge);
        newSubRunner.addStartAndEndNodeOfCell();
        newSubRunner.initializeNeighborIterator();
        subRunners.add(newSubRunner);
    }

    private List<SubNeighborVisitor> getAllEdgesCollinearToMostOrientedFoundEdge(CellRunnerUpdateData data) {
        final List<SubNeighborVisitor> allNeighbors = data.getAllNeighbors();
        final List<SubNeighborVisitor> collinearEdges = new ArrayList<>();
        for (SubNeighborVisitor neighbor : allNeighbors) {
            final double neighborsAngleToLastEdge = vectorAngleCalculator.getAngleOfVectorsOriented(data.lastEdgeReversedBaseNode, data.lastEdgeReversedAdjNode, neighbor.getLast());
            if (neighborsAngleToLastEdge == data.getLeftOrRightMostAngle()) {
                collinearEdges.add(neighbor);
            }
        }

        cleanMultiEdges(collinearEdges);

        if (isContainingEdgesWithEqualStartEndNodes(collinearEdges)) {
//            throw new IllegalStateException("Found a node with two neighbors at the same coordinates.");
        }

        return collinearEdges;
    }

    private void cleanMultiEdges(final List<SubNeighborVisitor> collinearEdges) {
        int indexOfEdgeWithSameAdjNode = indexOfEdgeWithSameAdjNode(collinearEdges);
        while (indexOfEdgeWithSameAdjNode >= 0) {
            collinearEdges.remove(indexOfEdgeWithSameAdjNode);
            indexOfEdgeWithSameAdjNode = indexOfEdgeWithSameAdjNode(collinearEdges);
        }
    }

    private int indexOfEdgeWithSameAdjNode(final List<SubNeighborVisitor> collinearEdges) {
        for (int i = 0; i < collinearEdges.size(); i++) {
            final EdgeIteratorState edgeA = collinearEdges.get(i).getLast();

            for (int j = i + 1; j < collinearEdges.size(); j++) {
                final EdgeIteratorState edgeB = collinearEdges.get(j).getLast();

                if (edgeA.getAdjNode() == edgeB.getAdjNode()) {
                    return i;
                }
            }
        }

        return -1;
    }

    private boolean isContainingEdgesWithEqualStartEndNodes(final List<SubNeighborVisitor> collinearEdges) {
        for (int i = 0; i < collinearEdges.size(); i++) {
            final EdgeIteratorState collinearEdgeA = collinearEdges.get(i).getLast();

            for (int j = i + 1; j < collinearEdges.size(); j++) {
                final EdgeIteratorState collinearEdgeB = collinearEdges.get(j).getLast();

                if (doEdgesHaveEqualCoordinateEndNodes(collinearEdgeA, collinearEdgeB)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean doEdgesHaveEqualCoordinateEndNodes(final EdgeIteratorState edgeA, final EdgeIteratorState edgeB) {
        final int edgeABaseNode = edgeA.getBaseNode();
        final int edgeAAdjNode = edgeA.getAdjNode();
        final int edgeBBaseNode = edgeB.getBaseNode();
        final int edgeBAdjNode = edgeB.getAdjNode();

        return nodeAccess.getLon(edgeABaseNode) == nodeAccess.getLon(edgeBBaseNode) && nodeAccess.getLat(edgeABaseNode) == nodeAccess.getLat(edgeBBaseNode) &&
            nodeAccess.getLon(edgeAAdjNode) == nodeAccess.getLon(edgeBAdjNode) && nodeAccess.getLat(edgeAAdjNode) == nodeAccess.getLat(edgeBAdjNode);
    }

    private boolean isMultipleWaysToContinueFound(List<SubNeighborVisitor> collinearEdges) {
        return collinearEdges.size() > 1;
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

    abstract CellRunner createNewSubRunner(EdgeIteratorState startEdge, EdgeIteratorState endEdge);
}
