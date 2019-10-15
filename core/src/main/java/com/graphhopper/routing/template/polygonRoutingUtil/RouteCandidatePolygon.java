package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.*;

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

        completePathCandidate.addPath(startToDetourEntry);
        completePathCandidate.addPath(detourEntryToDetourExit);
        completePathCandidate.addPath(detourExitToEnd);

        completePathCandidate.setFromNode(startNodeID);
        completePathCandidate.extract();

        this.mergedPath = completePathCandidate;
    }

    public double getTime() {
        return this.startToDetourEntry.getTime() + this.detourEntryToDetourExit.getTime() + this.detourExitToEnd.getTime();
    }

    /**
     * According to 5.2 in Storandts Region-Aware route planning paper.
     *
     * @return The approximated time spent in the region of interest
     */
    public double getTimeInROI() {
        return this.detourEntryToDetourExit.getTime();
    }

    public double getGain() {
        // + 1 to avoid division by zero
        return this.getTimeInROI() / (this.getDetourTime() + 1);
    }

    public double getDetourTime() {
        return this.getTime() - this.directRouteStartEnd.getTime();
    }

    public boolean isDetourSelfIntersecting(final QueryGraph queryGraph, final AlgorithmOptions algoOpts) {
        mergePathIfNotDone(queryGraph, algoOpts);

        return checkForRedundantNodes();
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
                    "Time: " + this.getTime() + ", " +
                    "TimeInROI: " + getTimeInROI() + ", " +
                    "TimeDetour: " + getDetourTime() + ", " +
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
