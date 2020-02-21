package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.Polygon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionOfInterestRoutingGraph extends PathSkeletonGraph {
    private final Map<Integer, Boolean> nodeInRoutingGraph = new HashMap<>();

    public RegionOfInterestRoutingGraph(Polygon regionOfInterest, LocationIndex index, NodeAccess nodeAccess) {
        super(regionOfInterest, index, nodeAccess);
        buildHashFunction();
    }

    @Override
    public void prepareForEntryExitNodes(final List<Integer> entryNode, final List<Integer> exitNode) {
    }

    private void buildHashFunction() {
        final BBox regionOfInterestBoundingBox = this.regionOfInterest.getMinimalBoundingBox();
        final NodesInPolygonFindingVisitor allNodesFindingVisitor = new NodesInPolygonFindingVisitor(regionOfInterest, nodeAccess);

        final List<Integer> nodesInRegionOfInterest = executeQuery(regionOfInterestBoundingBox, allNodesFindingVisitor);

        addListToHashFunctionAsTrue(nodesInRegionOfInterest);
    }

    private List<Integer> executeQuery(final BBox regionOfInterestBoundingBox, final NodesInPolygonFindingVisitor allNodesFindingVisitor) {
        index.query(regionOfInterestBoundingBox, allNodesFindingVisitor);
        return allNodesFindingVisitor.getNodesInPolygon();
    }
}
