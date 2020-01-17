package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.template.polygonRoutingUtil.PathSkeletonGraph;
import com.graphhopper.routing.template.polygonRoutingUtil.RegionOfInterestRoutingGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.shapes.Polygon;

public class PolygonThroughRoutingTemplate extends PolygonRoutingTemplate {

    public PolygonThroughRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex,
                                         EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);
    }

    PathSkeletonGraph getPathSkeletonEdgeFilter() {
        final Polygon regionOfInterest = this.getGhRequest().getPolygon();

        return new RegionOfInterestRoutingGraph(regionOfInterest, locationIndex, nodeAccess);
    }

}
