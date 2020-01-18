package com.graphhopper.storage.index;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * "Left" and "Right" are always imagined as walking from baseNode to adjacent node and then turn left or right.
 * <p>
 * General schema: For each edge in the allEdgesIterator: Check if it was used in a left run, if not run left. Check if it was used in a right run if not run right
 */
class VisibilityCellsCreator {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Graph graph;
    private final NodeAccess nodeAccess;
    private final EdgeIterator allEdges;
    private final VisitedManagerDual globalVisitedManager;

    private final List<VisibilityCell> allFoundCells;

    public VisibilityCellsCreator(final Graph graph, final NodeAccess nodeAccess) {
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
        StopWatch swAll = new StopWatch("VisibilityCells created").start();
        StopWatch sw1000 = null;
        while (allEdges.next()) {
            if (i % 1000 == 0) {
//                logger.info("###################################################################" + i);
//                logger.info(allEdges.getEdge() + ":" + allEdges.getBaseNode() + ":" + allEdges.getAdjNode());
//                final VisibilityCellConsumer vcCoordinateCounter = new VisibilityCellConsumer();
//                allFoundCells.forEach(vcCoordinateCounter);
//                logger.info("Num edges visited: " + globalVisitedManager.visitedLeft.edgeIdVisited.size() + " num VC-coordinates: " + vcCoordinateCounter.getCount());
//                sw1000 = new StopWatch("run on one edge " + allEdges.getEdge() + ", " + i + "/" + graph.getEdges()).start();
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

            if (i % 1000 == 999) {
//                logger.info(sw1000.stop().toString());
            }
            i++;
        }
//        System.out.println("finished");
        logger.info(swAll.stop().toString());
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

        int getCount() {
            return c;
        }
    }
}
