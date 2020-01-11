package com.graphhopper.storage.index;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.StopWatch;

import java.util.*;

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
    private final VisitedManagerDual visitedManagerDual;

    final List<VisibilityCell> allFoundCells;

    public VisibilityCellsCreator(final GridIndex gridIndex, final Graph graph, final NodeAccess nodeAccess) {
        this.gridIndex = gridIndex;
        this.graph = graph;
        this.nodeAccess = nodeAccess;
        this.allEdges = graph.getAllEdges();
        this.allFoundCells = new ArrayList<>(graph.getNodes());
        this.visitedManagerDual = new VisitedManagerDual(graph);
    }

    public List<VisibilityCell> create() {
        startRunsOnEachEdgeInTheGraph();

        return allFoundCells;
    }

    private void startRunsOnEachEdgeInTheGraph() {
//        final NodesAndNeighborDump nnd = new NodesAndNeighborDump(graph, Arrays.asList(646944, 7222461, 5277586, 5277585, 5277584/*, 5277583, 5277582, 5277581, 5277580,
//        5277579*/));
//        nnd.dump();
//        SwingGraphGUI gui = new SwingGraphGUI(nnd.getNodes(), nnd.getEdges());
//        gui.visualizeGraph();
//        try {
//            Thread.sleep(10_000_000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        int i = 0;
        while (allEdges.next()) {
            System.out.println("###################################################################" + i++);
            if (i < 755755) {
                continue;
            }
            System.out.println(allEdges.getEdge() + ":" + allEdges.getBaseNode() + ":" + allEdges.getAdjNode());
            StopWatch sw1 = new StopWatch("run on one edge " + allEdges.getEdge() + ", " + i + "/" + graph.getEdges()).start();

            final int baseNode = allEdges.getBaseNode();
            final int adjNode = allEdges.getAdjNode();
            final double baseNodeLatitude = nodeAccess.getLatitude(baseNode);
            final double baseNodeLongitude = nodeAccess.getLongitude(baseNode);
            final double adjNodeLatitude = nodeAccess.getLatitude(adjNode);
            final double adjNodeLongitude = nodeAccess.getLongitude(adjNode);
            if (baseNodeLatitude == adjNodeLatitude && baseNodeLongitude == adjNodeLongitude) {
                continue;
            }


            final EdgeIteratorState currentEdge = allEdges.detach(false);
            if (!visibilityCellOnTheLeftFound(currentEdge)) {
                addVisibilityCellToResults(new CellRunnerLeft(graph, nodeAccess, visitedManagerDual, currentEdge).runAroundCellAndLogNodes());
            }

            if (!visibilityCellOnTheRightFound(currentEdge)) {
                addVisibilityCellToResults(new CellRunnerRight(graph, nodeAccess, visitedManagerDual, currentEdge).runAroundCellAndLogNodes());
            }

            System.out.println(sw1.stop());
        }
        System.out.println("finished");
    }

    private void addVisibilityCellToResults(VisibilityCell visibilityCell) {
        allFoundCells.add(visibilityCell);
    }

    private Boolean visibilityCellOnTheLeftFound(final EdgeIteratorState currentEdge) {
        return visitedManagerDual.isEdgeSettledLeft(VisitedManager.forceNodeIdsAscending(currentEdge));
    }

    private Boolean visibilityCellOnTheRightFound(final EdgeIteratorState currentEdge) {
        return visitedManagerDual.isEdgeSettledRight(VisitedManager.forceNodeIdsAscending(currentEdge));
    }

}
