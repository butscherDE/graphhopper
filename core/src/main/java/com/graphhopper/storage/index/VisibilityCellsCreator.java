package com.graphhopper.storage.index;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.graphvisualizer.NodesAndNeighborDump;
import com.graphhopper.util.graphvisualizer.SwingGraphGUI;

import java.util.*;
import java.util.function.Consumer;

/**
 * "Left" and "Right" are always imagined as walking from baseNode to adjacent node and then turn left or right.
 * <p>
 * General schema: For each edge in the allEdgesIterator: Check if it was used in a left run, if not run left. Check if it was used in a right run if not run right
 */
class VisibilityCellsCreator {
    private final GridIndex gridIndex;
    private final Graph graph;
    private final NodeAccess nodeAccess;
    private final EdgeIterator allEdges;
    private final VisitedManagerDual globalVisitedManager;

    final List<VisibilityCell> allFoundCells;

    public VisibilityCellsCreator(final GridIndex gridIndex, final Graph graph, final NodeAccess nodeAccess) {
        this.gridIndex = gridIndex;
        this.graph = graph;
        this.nodeAccess = nodeAccess;
        this.allEdges = graph.getAllEdges();
        this.allFoundCells = new ArrayList<>(graph.getNodes());
        this.globalVisitedManager = new VisitedManagerDual(graph);
    }

    public List<VisibilityCell> create() {
        startRunsOnEachEdgeInTheGraph();

        return allFoundCells;
    }

    private void startRunsOnEachEdgeInTheGraph() {
//        final NodesAndNeighborDump nnd = new NodesAndNeighborDump(graph, Arrays.asList(1308555, 2161331, 6318267, 3182139, 5712100, 7895450, 8987113));
//        nnd.dump();
//        SwingGraphGUI gui = new SwingGraphGUI(nnd.getNodes(), nnd.getEdges());
//        gui.visualizeGraph();
//        try {
//            Thread.sleep(10_000_000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        int i = 0;
        StopWatch sw1 = null;
        while (allEdges.next()) {
            if (i % 1000 == 0) {
                System.out.println("###################################################################" + i);
                System.out.println(allEdges.getEdge() + ":" + allEdges.getBaseNode() + ":" + allEdges.getAdjNode());
                final VisibilityCellConsumer vcCoordinateCounter = new VisibilityCellConsumer();
                allFoundCells.forEach(vcCoordinateCounter);
                System.out.println("Num edges visited: " + globalVisitedManager.visitedLeft.edgeIdVisited.size() + " num VC-coordinates: " + vcCoordinateCounter.getCount());
                sw1 = new StopWatch("run on one edge " + allEdges.getEdge() + ", " + i + "/" + graph.getEdges()).start();
            }

//            if (i < 559000) {
//                i++;
//                continue;
//            }
            if (continueOnLengthZeroEdge()) {
                continue;
            }


            final EdgeIteratorState currentEdge = allEdges.detach(false);
            if (!visibilityCellOnTheLeftFound(currentEdge)) {
                addVisibilityCellToResults(new CellRunnerLeft(graph, globalVisitedManager, currentEdge).extractVisibilityCell());
            }

            if (!visibilityCellOnTheRightFound(currentEdge)) {
                addVisibilityCellToResults(new CellRunnerRight(graph, globalVisitedManager, currentEdge).extractVisibilityCell());
            }

            if (i % 999 == 0) {
                System.out.println(sw1.stop());
            }
            i++;
        }
        System.out.println("finished");
    }

    private boolean continueOnLengthZeroEdge() {
        if (isCurrentEdgeLengthZero()) {
            globalVisitedManager.settleEdgeLeft(allEdges);
            globalVisitedManager.settleEdgeRight(allEdges);
            return true;
        }
        return false;
    }

    private boolean isCurrentEdgeLengthZero() {
        final int baseNode = allEdges.getBaseNode();
        final int adjNode = allEdges.getAdjNode();

        final double baseNodeLatitude = nodeAccess.getLatitude(baseNode);
        final double baseNodeLongitude = nodeAccess.getLongitude(baseNode);
        final double adjNodeLatitude = nodeAccess.getLatitude(adjNode);
        final double adjNodeLongitude = nodeAccess.getLongitude(adjNode);

        return baseNodeLatitude == adjNodeLatitude && baseNodeLongitude == adjNodeLongitude;
    }

    private void addVisibilityCellToResults(VisibilityCell visibilityCell) {
        allFoundCells.add(visibilityCell);
    }

    private Boolean visibilityCellOnTheLeftFound(final EdgeIteratorState currentEdge) {
        return globalVisitedManager.isEdgeSettledLeft(VisitedManager.forceNodeIdsAscending(currentEdge));
    }

    private Boolean visibilityCellOnTheRightFound(final EdgeIteratorState currentEdge) {
        return globalVisitedManager.isEdgeSettledRight(VisitedManager.forceNodeIdsAscending(currentEdge));
    }

    private static class VisibilityCellConsumer implements Consumer<VisibilityCell> {
        int c = 0;

        @Override
        public void accept(VisibilityCell visibilityCell) {
            c += visibilityCell.size();
        }

        public int getCount() {
            return c;
        }
    }
}
