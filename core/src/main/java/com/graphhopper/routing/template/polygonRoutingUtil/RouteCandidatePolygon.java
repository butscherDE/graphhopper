package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.*;
import com.graphhopper.routing.template.PolygonRoutingTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * A route candidate as in Prof. Dr. Sabine Storandts Paper Region-Aware Route Planning.
 */
public class RouteCandidatePolygon implements Comparable<RouteCandidatePolygon> {
    Path startToDetourEntry;
    Path detourEntryToDetourExit;
    Path detourExitToEnd;
    Path directRouteStartEnd;
    RoutingAlgorithm routingAlgorithm;
    final int startNodeID, endNodeID, polygonEntryNodeID, polygonExitNodeID;
    PathMerge mergedPath = null;

    public RouteCandidatePolygon(final int startNodeID, final int endNodeID, final int polygonEntryNodeID,
                                 final int polygonExitNodeID, final Path startToDetourEntry, final Path detourEntryToDetourExit, final Path detourExitToEnd,
                                 final Path directRouteStartEnd) {
        this.startNodeID = startNodeID;
        this.endNodeID = endNodeID;
        this.polygonEntryNodeID = polygonEntryNodeID;
        this.polygonExitNodeID = polygonExitNodeID;
        this.startToDetourEntry = startToDetourEntry;
        this.detourEntryToDetourExit = detourEntryToDetourExit;
        this.detourExitToEnd = detourExitToEnd;
        this.directRouteStartEnd = directRouteStartEnd;
    }

    public Path getMergedPath(final QueryGraph queryGraph, final AlgorithmOptions algoOpts) {
        mergePathIfNotDone(queryGraph, algoOpts);

        return this.mergedPath;
    }

    private void mergePath(QueryGraph queryGraph, AlgorithmOptions algoOpts) {
        PathMerge completePathCandidate = new PathMerge(queryGraph, algoOpts.getWeighting());

        System.out.println(this.toString());
        completePathCandidate.addPath(startToDetourEntry);
        completePathCandidate.addPath(detourEntryToDetourExit);
        completePathCandidate.addPath(detourExitToEnd);

        completePathCandidate.setFromNode(startNodeID);
        completePathCandidate.extract();

        this.mergedPath = completePathCandidate;
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
    public boolean isDetourSelfIntersecting(final QueryGraph queryGraph, final AlgorithmOptions algoOpts) {
        mergePathIfNotDone(queryGraph, algoOpts);

        return checkForRedundantNodes();
        // TODO: Selfintersecting: Complete route or detour part?
    }

    private void mergePathIfNotDone(QueryGraph queryGraph, AlgorithmOptions algoOpts) {
        if (mergedPath == null) {
            mergePath(queryGraph, algoOpts);
        }
    }

    private boolean checkForRedundantNodes() {
        Map<Integer, Boolean> foundNodes = new HashMap<>();

        for (final int node : this.mergedPath.getNodesInPathOrder()) {
            if (foundNodes.get(node) == null) {
                foundNodes.put(node, true);
            } else {
                return true;
            }
        }

        return false;
    }

    @Override
    public int compareTo(RouteCandidatePolygon o) {
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
        String sb = "startNodeID: " + startNodeID + ", " +
                    "endNodeID: " + endNodeID + ", " +
                    "polygonEntryNodeID: " + polygonEntryNodeID + ", " +
                    "polygonExitNodeID: " + polygonExitNodeID + ", " +
                    "Distance: " + this.getDistance() + ", " +
                    "DistanceInROI: " + getDistanceInROI() + ", " +
                    "detour distance: " + getDetourDistance() + ", " +
                    "gain: " + this.getGain();
        return sb;
    }

    public boolean isLegalCandidate() {
        return isAllSubpathsValid();
    }

    private boolean isAllSubpathsValid() {
        boolean allValid = true;
        allValid &= isSubpathValid(this.startToDetourEntry);
        allValid &= isSubpathValid(this.detourEntryToDetourExit);
        allValid &= isSubpathValid(this.detourExitToEnd);

        return allValid;
    }

    private static boolean isSubpathValid(final Path path) {
        if (path != null) {
            return path.isFound();
        } else {
            throw new IllegalStateException("Calculate paths before validating them.");
        }
    }
}
