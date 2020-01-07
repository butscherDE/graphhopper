package com.graphhopper.storage.index;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
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
    private final EdgeExplorer neighborExplorer;
    private final VisitedManager visitedManager;

    final List<VisibilityCell> allFoundCells;

    public VisibilityCellsCreator(final GridIndex gridIndex, final Graph graph, final NodeAccess nodeAccess) {
        this.gridIndex = gridIndex;
        this.graph = graph;
        this.nodeAccess = nodeAccess;
        this.allEdges = graph.getAllEdges();
        this.neighborExplorer = graph.createEdgeExplorer();
        this.allFoundCells = new ArrayList<>(graph.getNodes());
        this.visitedManager = new VisitedManager(graph);
    }

    public List<VisibilityCell> create() {
        startRunsOnEachEdgeInTheGraph();

        return allFoundCells;
    }

    private void startRunsOnEachEdgeInTheGraph() {
        int i = 0;
        while (allEdges.next()) {
            System.out.println("###################################################################");
            System.out.println(allEdges.getEdge() + ":" + allEdges.getBaseNode() + ":" + allEdges.getAdjNode());
            StopWatch sw1 = new StopWatch("run on one edge " + allEdges.getEdge() + ", " + i++ + "/" + graph.getEdges()).start();

            final EdgeIteratorState currentEdge = allEdges.detach(false);
            if (!visibilityCellOnTheLeftFound(currentEdge)) {
                addVisibilityCellToResults(new CellRunnerLeft(this, neighborExplorer, nodeAccess, visitedManager, currentEdge).runAroundCellAndLogNodes());
            }

            if (!visibilityCellOnTheRightFound(currentEdge)) {
                addVisibilityCellToResults(new CellRunnerRight(this, neighborExplorer, nodeAccess, visitedManager, currentEdge).runAroundCellAndLogNodes());
            }

            System.out.println(sw1.stop());
        }
        System.out.println("finished");
    }

    private void addVisibilityCellToResults(VisibilityCell visibilityCell) {
        allFoundCells.add(visibilityCell);
    }

    private Boolean visibilityCellOnTheLeftFound(final EdgeIteratorState currentEdge) {
        return visitedManager.isEdgeSettledLeft(visitedManager.forceNodeIdsAscending(currentEdge));
    }

    private Boolean visibilityCellOnTheRightFound(final EdgeIteratorState currentEdge) {
        return visitedManager.isEdgeSettledRight(visitedManager.forceNodeIdsAscending(currentEdge));
    }

}
