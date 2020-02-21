package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.shapes.Polygon;

public class RegionOfInterestRphastRoutingGraph extends RegionOfInterestRoutingGraph {
    public RegionOfInterestRphastRoutingGraph(Polygon regionOfInterest, LocationIndex index, NodeAccess nodeAccess) {
        super(regionOfInterest, index, nodeAccess);
    }

    @Override
    public boolean accept(final boolean baseNodeInGraph, final boolean adjNodeInGraph) {
        int i = 0;
        i += baseNodeInGraph ? 1 : 0;
        i += adjNodeInGraph ? 1 : 0;

        return i >= 1;
    }
}
