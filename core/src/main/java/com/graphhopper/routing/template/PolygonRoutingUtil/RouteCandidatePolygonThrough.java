package com.graphhopper.routing.template.PolygonRoutingUtil;

import com.graphhopper.routing.DijkstraManyToMany;
import com.graphhopper.routing.template.PolygonThroughRoutingTemplate;

public class RouteCandidatePolygonThrough extends RouteCandidatePolygon {
    private final DijkstraManyToMany pathSkeletonRouter;
    public RouteCandidatePolygonThrough(PolygonThroughRoutingTemplate polygonRoutingTemplate, int startNodeID, int endNodeID,
                                        int polygonEntryNodeID, int polygonExitNodeID) {
        super(polygonRoutingTemplate, startNodeID, endNodeID, polygonEntryNodeID, polygonExitNodeID);
        this.pathSkeletonRouter = polygonRoutingTemplate.getPathSkeletonRouter();
    }

    private void calcPathFromStartToDetourEntry() {
        this.routingAlgorithm = this.polygonRoutingTemplate.getNewRoutingAlgorithm();
        this.startToDetourEntry = this.routingAlgorithm.calcPath(startNodeID, polygonEntryNodeID);
    }

    private void calcDetourPath() {
        this.detourEntryToDetourExit = this.pathSkeletonRouter.getPathByFromEndPoint(polygonEntryNodeID, polygonExitNodeID);
    }

    private void calcPathFromDetourExitToEnd() {
        this.routingAlgorithm = this.polygonRoutingTemplate.getNewRoutingAlgorithm();
        this.detourExitToEnd = this.routingAlgorithm.calcPath(polygonExitNodeID, endNodeID);
    }

    @Override
    public void calcPaths() {
        calcPathFromStartToDetourEntry();
        calcDetourPath();
        calcPathFromDetourExitToEnd();
        calcDirectRouteFromStartToEnd();
    }
}
