package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.template.polygonRoutingUtil.MultiRouting;
import com.graphhopper.routing.template.polygonRoutingUtil.PathSkeletonGraph;
import com.graphhopper.routing.template.polygonRoutingUtil.RPHASTManyToMany;
import com.graphhopper.routing.template.polygonRoutingUtil.RegionOfInterestRoutingGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.shapes.Polygon;

import java.util.List;

public class PolygonThroughRoutingTemplate extends PolygonRoutingTemplate {

    public PolygonThroughRoutingTemplate(GHRequest ghRequest, GHResponse ghRsp, LocationIndex locationIndex,
                                         EncodingManager encodingManager) {
        super(ghRequest, ghRsp, locationIndex, encodingManager);
    }

    @Override
    public MultiRouting getPathSkeletonRouter(List<QueryResult> queryResults) {
        final Graph graph = this.graph.getMainGraph();
        if (graph instanceof GraphHopperStorage) {
            return new RPHASTManyToMany(pathSkeletonEdgeFilter, lotNodes.getAllLotNodes(),
                    (GraphHopperStorage) this.graph.getMainGraph(), this.algorithmOptions);
        } else if (graph instanceof CHGraph) {
            return new RPHASTManyToMany(pathSkeletonEdgeFilter, lotNodes.getAllLotNodes(),
                    (CHGraph) this.graph.getMainGraph(), this.algorithmOptions);
        } else {
            throw new IllegalArgumentException("No implementation found where a CH Graph can be retrieved from.");
        }
    }

    PathSkeletonGraph getPathSkeletonEdgeFilter() {
        final Polygon regionOfInterest = this.getGhRequest().getPolygon();

        return new RegionOfInterestRoutingGraph(regionOfInterest, locationIndex, nodeAccess);
    }

}
