package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.shapes.Polygon;

import java.util.ArrayList;
import java.util.List;

public class NodesInPolygonFindingVisitor extends LocationIndex.Visitor {
    private final List<Integer> nodesInPolygon = new ArrayList<>();
    private final Polygon polygon;
    private final NodeAccess nodeAccess;

    public NodesInPolygonFindingVisitor(final Polygon polygon, final NodeAccess nodeAccess) {
        this.polygon = polygon;
        this.nodeAccess = nodeAccess;
    }

    @Override
    public void onNode(int nodeId) {
        final double lat = nodeAccess.getLat(nodeId);
        final double lon = nodeAccess.getLon(nodeId);

        if (polygon.contains(lat, lon)) {
            this.nodesInPolygon.add(nodeId);
        }
    }

    public List<Integer> getNodesInPolygon() {
        return this.nodesInPolygon;
    }
}
