package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.template.polygonRoutingUtil.PathSkeletonGraph;
import com.graphhopper.routing.template.polygonRoutingUtil.VisibilityCellRoutingGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.GridIndex;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.VisibilityCell;
import com.graphhopper.util.shapes.Polygon;

import java.util.List;

public class PolygonAroundRoutingTemplate extends PolygonRoutingTemplate{

    public PolygonAroundRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex,
                                         EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);
    }

    PathSkeletonGraph getPathSkeletonEdgeFilter() {
        final Polygon regionOfInterest = this.getGhRequest().getPolygon();

        final GridIndex locationIndexAsGrid = (GridIndex) locationIndex;
        final List<VisibilityCell> visibilityCells = locationIndexAsGrid.getIntersectingVisibilityCells(regionOfInterest);

        return new VisibilityCellRoutingGraph(visibilityCells, regionOfInterest, locationIndex, nodeAccess);
    }

}
