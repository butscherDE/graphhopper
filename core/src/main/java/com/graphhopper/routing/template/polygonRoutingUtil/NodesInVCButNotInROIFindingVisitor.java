package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.VisibilityCell;
import com.graphhopper.util.shapes.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds all nodes in a VisibilityCell but not inside the region of interest.
 */
public class NodesInVCButNotInROIFindingVisitor extends LocationIndex.Visitor {
    private final List<Integer> nodesInVCButNotInROI = new ArrayList<>();
    private final Polygon regionOfInterest;
    private final VisibilityCell visibilityCell;
    private final NodeAccess nodeAccess;

    public NodesInVCButNotInROIFindingVisitor(final VisibilityCell visibilityCell, final Polygon regionOfInterest, final NodeAccess nodeAccess) {
        this.regionOfInterest = regionOfInterest;
        this.nodeAccess = nodeAccess;
        this.visibilityCell = visibilityCell;
    }

    @Override
    public void onNode(int nodeId) {
        final double lat = nodeAccess.getLat(nodeId);
        final double lon = nodeAccess.getLon(nodeId);

        if (isNodeInVCButNotInROI(lat, lon)) {
            this.nodesInVCButNotInROI.add(nodeId);
        }
    }

    private boolean isNodeInVCButNotInROI(double lat, double lon) {
        return isNodeInVC(lat, lon) && isNodeNotInROI(lat, lon);
    }

    private boolean isNodeNotInROI(double lat, double lon) {
        return !regionOfInterest.contains(lat, lon);
    }

    private boolean isNodeInVC(double lat, double lon) {
        return visibilityCell.contains(lat, lon);
    }

    public List<Integer> getNodesInVCButNotInROI() {
        return this.nodesInVCButNotInROI;
    }
}
