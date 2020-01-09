package com.graphhopper.storage.index;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.graphvisualizer.NodesAndNeighborDump;
import com.graphhopper.util.graphvisualizer.SwingGraphGUI;

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
//        System.out.println(nodeAccess.getLongitude(61442) + ", " + nodeAccess.getLat(61442));
//        System.out.println(nodeAccess.getLongitude(2276168) + ", " + nodeAccess.getLat(2276168));
//        System.out.println(nodeAccess.getLongitude(8020459) + ", " + nodeAccess.getLat(8020459));
//        System.out.println(nodeAccess.getLongitude(3883532) + ", " + nodeAccess.getLat(3883532));
//        System.out.println(nodeAccess.getLongitude(6336509) + ", " + nodeAccess.getLat(6336509));
//        final NodesAndNeighborDump nnd = new NodesAndNeighborDump(graph, Arrays.asList(
//                new Integer[]{3096452, 3331230, 242513, 3809843, 3809830, 3809845, 3809841, 3809834, 3809847, 3809836, }));
//        nnd.dump();
//        SwingGraphGUI gui = new SwingGraphGUI(nnd.getNodes(), nnd.getEdges());
//        gui.visualizeGraph();
//        try {
//            Thread.sleep(100000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        int i = 0;
        while (allEdges.next()) {
            System.out.println("###################################################################" + i);
            System.out.println(allEdges.getEdge() + ":" + allEdges.getBaseNode() + ":" + allEdges.getAdjNode());
            StopWatch sw1 = new StopWatch("run on one edge " + allEdges.getEdge() + ", " + i++ + "/" + graph.getEdges()).start();

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
                addVisibilityCellToResults(new CellRunnerLeft(neighborExplorer, nodeAccess, visitedManager, currentEdge).runAroundCellAndLogNodes());
            }

            if (!visibilityCellOnTheRightFound(currentEdge)) {
                addVisibilityCellToResults(new CellRunnerRight(neighborExplorer, nodeAccess, visitedManager, currentEdge).runAroundCellAndLogNodes());
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
