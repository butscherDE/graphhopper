package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.template.polygonRoutingUtil.ManyToManyRouting;
import com.graphhopper.routing.template.polygonRoutingUtil.MultiRouting;
import com.graphhopper.routing.template.polygonRoutingUtil.PathSkeletonGraph;
import com.graphhopper.routing.template.polygonRoutingUtil.VisibilityCellRoutingGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.GridIndex;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.storage.index.VisibilityCell;
import com.graphhopper.util.shapes.Polygon;

import java.util.List;

public class PolygonAroundRoutingTemplate extends PolygonRoutingTemplate{

    public PolygonAroundRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex,
                                         EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);
    }

    @Override
    public MultiRouting getPathSkeletonRouter(List<QueryResult> queryResults) {
        return new ManyToManyRouting(pathSkeletonEdgeFilter, lotNodes.getAllLotNodes(), this.graph, queryResults,
                this.algoFactory, this.algorithmOptions);
    }

    PathSkeletonGraph getPathSkeletonEdgeFilter() {
        final Polygon regionOfInterest = getGhRequest().getPolygon();

        final GridIndex locationIndexAsGrid = (GridIndex) locationIndex;
        final List<VisibilityCell> visibilityCells = locationIndexAsGrid.getIntersectingVisibilityCells(regionOfInterest);

        return new VisibilityCellRoutingGraph(visibilityCells, regionOfInterest, locationIndex, nodeAccess);
    }

}
