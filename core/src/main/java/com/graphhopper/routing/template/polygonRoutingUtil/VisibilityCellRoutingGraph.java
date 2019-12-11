package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.GridIndex;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.Polygon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisibilityCellRoutingGraph implements EdgeFilter {
    private final List<GridIndex.VisibilityCell> visibilityCells;
    private final Polygon regionOfInterest;
    private final LocationIndex index;
    private final NodeAccess nodeAccess;
    private final int entryNode;
    private final int exitNode;
    private final Map<Integer, Boolean> nodeInRoutingGraph = new HashMap<>();

    public VisibilityCellRoutingGraph(List<GridIndex.VisibilityCell> visibilityCells, Polygon regionOfInterest, LocationIndex index, NodeAccess nodeAccess, final int entryNode,
                                      final int exitNode) {
        this.visibilityCells = visibilityCells;
        this.regionOfInterest = regionOfInterest;
        this.index = index;
        this.nodeAccess = nodeAccess;
        this.entryNode = entryNode;
        this.exitNode = exitNode;

        buildHashFunction();
    }

    private void buildHashFunction() {
        addEntryExitNodesToHashFunction();
        addVCGraphToHashFunction();
    }

    private void addEntryExitNodesToHashFunction() {
        this.nodeInRoutingGraph.put(this.entryNode, true);
        this.nodeInRoutingGraph.put(this.exitNode, true);
    }

    private void addVCGraphToHashFunction() {
        for (GridIndex.VisibilityCell visibilityCell : visibilityCells) {
            final BBox visibilityCellBoundingBox = visibilityCell.getMinimalBoundingBox();
            final NodesInVCButNotInROIFindingVisitor allNodesFindingVisitor = new NodesInVCButNotInROIFindingVisitor(visibilityCell, regionOfInterest, nodeAccess);

            final List<Integer> nodesInVCButNotInROI = executeQuery(visibilityCellBoundingBox, allNodesFindingVisitor);

            addListToHashFunctionAsTrue(nodesInVCButNotInROI);
        }
    }

    private List<Integer> executeQuery(BBox visibilityCellBoundingBox, NodesInVCButNotInROIFindingVisitor allNodesFindingVisitor) {
        index.query(visibilityCellBoundingBox, allNodesFindingVisitor);
        return allNodesFindingVisitor.getNodesInVCButNotInROI();
    }

    private void addListToHashFunctionAsTrue(List<Integer> nodesInVCButNotInROI) {
        for (Integer node : nodesInVCButNotInROI) {
            nodeInRoutingGraph.put(node, true);
        }
    }

    @Override
    public boolean accept(EdgeIteratorState edgeState) {
        final Integer baseNode = edgeState.getBaseNode();
        final Integer adjNode = edgeState.getAdjNode();
        final Boolean baseNodeInGraph = nodeInRoutingGraph.get(baseNode);
        final Boolean adjNodeInGraph = nodeInRoutingGraph.get(adjNode);

        boolean result = areBothNodesInRoutingGraph(baseNodeInGraph, adjNodeInGraph);

        return result;
    }

    private boolean areBothNodesInRoutingGraph(Boolean baseNodeInGraph, Boolean adjNodeInGraph) {
        boolean result = true;

        result &= isNodeInGraph(baseNodeInGraph);
        result &= isNodeInGraph(adjNodeInGraph);
        return result;
    }

    private boolean isNodeInGraph(Boolean node) {
        return node != null && node == true;
    }
}
