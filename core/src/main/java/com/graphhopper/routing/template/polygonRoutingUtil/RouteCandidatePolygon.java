package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.*;
import com.graphhopper.routing.template.PolygonRoutingTemplate;

/**
 * A route candidate as in Prof. Dr. Sabine Storandts Paper Region-Aware Route Planning.
 */
public abstract class RouteCandidatePolygon implements Comparable<RouteCandidatePolygon> {
    Path startToDetourEntry;
    Path detourEntryToDetourExit;
    Path detourExitToEnd;
    Path directRouteStartEnd;
    final PolygonRoutingTemplate polygonRoutingTemplate;
    RoutingAlgorithm routingAlgorithm;
    final int startNodeID, endNodeID, polygonEntryNodeID, polygonExitNodeID;

    public RouteCandidatePolygon(final PolygonRoutingTemplate polygonRoutingTemplate, final int startNodeID, final int endNodeID, final int polygonEntryNodeID,
                                 final int polygonExitNodeID) {
        this.polygonRoutingTemplate = polygonRoutingTemplate;

        this.startNodeID = startNodeID;
        this.endNodeID = endNodeID;
        this.polygonEntryNodeID = polygonEntryNodeID;
        this.polygonExitNodeID = polygonExitNodeID;
    }

    public abstract void calcPaths();

    void calcDirectRouteFromStartToEnd() {
        this.routingAlgorithm = this.polygonRoutingTemplate.getNewRoutingAlgorithm();
        this.directRouteStartEnd = this.routingAlgorithm.calcPath(startNodeID, endNodeID);
    }

    public Path getMergedPath(final QueryGraph queryGraph, final AlgorithmOptions algoOpts) {
        PathMerge completePathCandidate = new PathMerge(queryGraph, algoOpts.getWeighting());

        completePathCandidate.addPath(startToDetourEntry);
        completePathCandidate.addPath(detourEntryToDetourExit);
        completePathCandidate.addPath(detourExitToEnd);

        completePathCandidate.extract();

        return completePathCandidate;
    }

    public double getDistance() {
        return this.startToDetourEntry.getDistance() + this.detourEntryToDetourExit.getDistance() + this.detourExitToEnd.getDistance();
    }

    /**
     * According to 5.2 in Storandts Region-Aware route planning paper.
     *
     * @return The approximated time spent in the region of interest
     */
    public double getDistanceInROI() {
        return this.detourEntryToDetourExit.getDistance();
    }

    public double getGain() {
        // + 1 to avoid division by zero
        return this.getDistanceInROI() / (this.getDetourDistance() + 1);
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
        // TODO: Selfintersecting: Complete route or detour part?
    }

    @Override
    public int compareTo(RouteCandidatePolygon o) {
        final double thisGain = this.getGain();
        final double thatGain = o.getGain();
        final double gainDifference = this.getGain() - o.getGain();
        if (gainDifference < 0) {
            return -1;
        } else if (gainDifference == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("startNodeID: " + startNodeID + ", ");
        sb.append("endNodeID: " + endNodeID + ", ");
        sb.append("polygonEntryNodeID: " + polygonEntryNodeID + ", ");
        sb.append("polygonExitNodeID: " + polygonExitNodeID + ", ");
        sb.append("DistanceInROI: " + getDistanceInROI() + ", ");
        sb.append("detour distance: " + getDetourDistance() + ", ");
        sb.append("gain: " + this.getGain());

        return sb.toString();
    }
}
