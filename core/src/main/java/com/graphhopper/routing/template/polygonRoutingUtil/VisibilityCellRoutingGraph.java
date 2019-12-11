package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.GridIndex;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.Polygon;

import java.util.List;

public class VisibilityCellRoutingGraph extends PathSkeletonGraph {
    private final List<GridIndex.VisibilityCell> visibilityCells;

    public VisibilityCellRoutingGraph(List<GridIndex.VisibilityCell> visibilityCells, Polygon regionOfInterest, LocationIndex index, NodeAccess nodeAccess) {
        super(regionOfInterest, index, nodeAccess);
        this.visibilityCells = visibilityCells;

        buildHashFunction();
    }

    private void buildHashFunction() {
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
}
