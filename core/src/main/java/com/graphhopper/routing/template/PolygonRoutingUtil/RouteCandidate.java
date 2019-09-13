package com.graphhopper.routing.template.PolygonRoutingUtil;

import com.graphhopper.routing.*;
import com.graphhopper.routing.template.PolygonThroughRoutingTemplate;

/**
 * A route candidate as in Prof. Dr. Sabine Storandts Paper Region-Aware Route Planning.
 */
public class RouteCandidate implements Comparable<RouteCandidate> {
    private Path startToPolygonEntry;
    private Path polygonEntryToPolygonExit;
    private Path polygonExitToEnd;
    private Path directRouteStartEnd;
    private final PolygonThroughRoutingTemplate polygonRoutingTemplate;
    private RoutingAlgorithm routingAlgorithm;
    private final DijkstraManyToMany pathSkeletonRouter;
    private final double distance;

    public RouteCandidate(final PolygonThroughRoutingTemplate polygonRoutingTemplate, final int startNodeID, final int endNodeID, final int polygonEntryNodeID,
                          final int polygonExitNodeID) {
        this.polygonRoutingTemplate = polygonRoutingTemplate;
        this.pathSkeletonRouter = polygonRoutingTemplate.getPathSkeletonRouter();

        calcPathFromStartToPolygonEntry(polygonRoutingTemplate, startNodeID, polygonEntryNodeID);
        calcPathThroughPolygon(polygonEntryNodeID, polygonExitNodeID);
        calcPathFromPolygonExitToEnd(polygonRoutingTemplate, endNodeID, polygonExitNodeID);
        calcDirectRouteFromStartToEnd(polygonRoutingTemplate, startNodeID, endNodeID);

        this.distance = this.startToPolygonEntry.getDistance() + this.polygonEntryToPolygonExit.getDistance() + this.polygonExitToEnd.getDistance();
    }

    private void calcDirectRouteFromStartToEnd(PolygonThroughRoutingTemplate polygonRoutingTemplate, int startNodeID, int endNodeID) {
        this.routingAlgorithm = polygonRoutingTemplate.getNewRoutingAlgorithm();
        this.directRouteStartEnd = this.routingAlgorithm.calcPath(startNodeID, endNodeID);
    }

    private void calcPathFromPolygonExitToEnd(PolygonThroughRoutingTemplate polygonRoutingTemplate, int endNodeID, int polygonExitNodeID) {
        this.routingAlgorithm = polygonRoutingTemplate.getNewRoutingAlgorithm();
        this.polygonExitToEnd = this.routingAlgorithm.calcPath(polygonExitNodeID, endNodeID);
    }

    private void calcPathThroughPolygon(int polygonEntryNodeID, int polygonExitNodeID) {
        this.polygonEntryToPolygonExit = this.pathSkeletonRouter.getPathByStartEndPoint(polygonEntryNodeID, polygonExitNodeID);
    }

    private void calcPathFromStartToPolygonEntry(PolygonThroughRoutingTemplate polygonRoutingTemplate, int startNodeID, int polygonEntryNodeID) {
        this.routingAlgorithm = polygonRoutingTemplate.getNewRoutingAlgorithm();
        this.startToPolygonEntry = this.routingAlgorithm.calcPath(startNodeID, polygonEntryNodeID);
    }

    public Path getMergedPath(final QueryGraph queryGraph, final AlgorithmOptions algoOpts) {
        Path completePathCandidate = new Path(queryGraph, algoOpts.getWeighting());
        completePathCandidate.addPath(startToPolygonEntry);
        completePathCandidate.addPath(polygonEntryToPolygonExit);
        completePathCandidate.addPath(polygonExitToEnd);
        return completePathCandidate;
    }

    public double getDistance() {
        return this.distance;
    }

    /**
     * According to 5.2 in Storandts Region-Aware route planning paper.
     *
     * @return The approximated time spent in the region of interest
     */
    public double getDistanceInROI() {
        return this.polygonEntryToPolygonExit.getDistance();
    }

    public double getGain() {
        // + 1 to avoid division by zero
        return this.polygonEntryToPolygonExit.getDistance() / (this.getDetourDistance() + 1);
    }

    public double getDetourDistance() {
        return this.getDistance() - this.directRouteStartEnd.getDistance();
    }

    /**
     * Uses the sweepline algorithm of Michael Ian Shamos and Dan Hoey to find intersecting line segments induced by the edges of the merged path.
     * <p>
     * Reference:
     * Michael Ian Shamos and Dan Hoey. Geometric intersection problems. In Proceedings
     * of the 17th Annual IEEE Symposium on Foundations of Computer Science
     * (FOCS '76), pages 208{215, 1976.
     *
     * @return true if at least one intersection occurs and false otherwise.
     */
    public boolean isDetourSelfIntersecting() {
        return false;
        // TODO: Check with storandt what she means with intersections
    }

    @Override
    /**
     * @param   o - The Route Candidate to be compared.
     * @return A negative integer, zero, or a positive integer as this RouteCandidate
     *          is less than, equal to, or greater than the supplied RouteCandidate object.
     */
    public int compareTo(RouteCandidate o) {
        double gainDifference = this.getGain() - o.getGain();
        if (gainDifference < 0) {
            return -1;
        } else if (gainDifference == 0) {
            return 0;
        } else {
            return 1;
        }
    }
}
