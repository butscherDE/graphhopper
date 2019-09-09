package com.graphhopper.routing.template.PolygonRoutingUtil;

import com.graphhopper.routing.*;
import com.graphhopper.routing.template.PolygonRoutingTemplate;

/**
 * A route candidate as in Prof. Dr. Sabine Storandts Paper Region-Aware Route Planning.
 */
public class RouteCandidate implements Comparable<RouteCandidate> {
    private final Path startToPolygonEntry;
    private final Path polygonEntryToPolygonExit;
    private final Path polygonExitToEnd;
    private final Path directRouteStartEnd;
    private final PolygonRoutingTemplate polygonRoutingTemplate;
    private final RoutingAlgorithm routingAlgorithm;
    private final DijkstraManyToMany pathSkeletonRouter;
    private final double distance;

    public RouteCandidate(final PolygonRoutingTemplate polygonRoutingTemplate, final int startNodeID, final int endNodeID, final int polygonEntryNodeID,
                          final int polygonExitNodeID) {
        this.polygonRoutingTemplate = polygonRoutingTemplate;
        this.routingAlgorithm = polygonRoutingTemplate.getRoutingAlgorithm();
        this.pathSkeletonRouter = polygonRoutingTemplate.getPathSkeletonRouter();

        this.startToPolygonEntry = this.routingAlgorithm.calcPath(startNodeID, polygonEntryNodeID);
        this.polygonEntryToPolygonExit = this.pathSkeletonRouter.getPathByStartEndPoint(polygonEntryNodeID, polygonExitNodeID);
        this.polygonExitToEnd = this.routingAlgorithm.calcPath(polygonExitNodeID, endNodeID);
        this.directRouteStartEnd = this.routingAlgorithm.calcPath(startNodeID, endNodeID);

        this.distance = this.startToPolygonEntry.getDistance() + this.polygonEntryToPolygonExit.getDistance() + this.polygonExitToEnd.getDistance();
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
