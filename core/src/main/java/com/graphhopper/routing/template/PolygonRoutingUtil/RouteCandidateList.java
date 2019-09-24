package com.graphhopper.routing.template.PolygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RouteCandidateList <T extends RouteCandidatePolygon> {
    public List<T> candidates;

    public RouteCandidateList() {
        this.candidates = new ArrayList<T>();
    }

    public void sortByGainAscending() {
        Collections.sort(this.candidates);
    }

    private void sortRouteCandidatesToDistanceInROIDescending() {
        Collections.sort(this.candidates, new Comparator<RouteCandidatePolygon>() {
            @Override
            public int compare(RouteCandidatePolygon rc1, RouteCandidatePolygon rc2) {
                double distanceDifference = rc1.getDistanceInROI() - rc2.getDistanceInROI();
                int output;
                if (distanceDifference < 0) {
                    output = 1;
                } else if (distanceDifference == 0) {
                    output = 0;
                } else {
                    output = -1;
                }

                return output;
            }
        });
    }

    public List<Path> getFirstAsPathList(final int nOfFirstElements, final QueryGraph queryGraph, final AlgorithmOptions algorithmOptions) {
        final List<Path> paths = new ArrayList<>(nOfFirstElements);

        final int endOfCandidates = candidates.size() - 1;
        final int endOfIteration = endOfCandidates - nOfFirstElements;
        for (int i = endOfCandidates; i > endOfIteration; i--) {
            paths.add(this.candidates.get(i).getMergedPath(queryGraph, algorithmOptions));
        }

        return paths;
    }

    // Do it in a skyline problem pruning fashion
    public void pruneDominatedCandidateRoutes() {
        this.sortRouteCandidatesToDistanceInROIDescending();

        int currentPruningCandidateIndex = 1;
        while (indexInCandidateBounds(currentPruningCandidateIndex)) {
            RouteCandidatePolygon currentPruningCandidate = this.candidates.get(currentPruningCandidateIndex);

            boolean foundDominatingPath = isThisCandidateDominatedByAny(currentPruningCandidateIndex, currentPruningCandidate);

            currentPruningCandidateIndex = pruneOrUpdateIndex(currentPruningCandidateIndex, foundDominatingPath);
        }
    }

    private boolean isThisCandidateDominatedByAny(int currentPruningCandidateIndex, RouteCandidatePolygon currentPruningCandidate) {
        boolean foundDominatingPath = false;
        for (int i = currentPruningCandidateIndex - 1; i >= 0 && !foundDominatingPath; i--) {
            // routeCandidates must be sorted by now. Therefore dominators can only bbe found on lower indices than the current pruning candidate.
            RouteCandidatePolygon possiblyBetterRouteCandidate = this.candidates.get(i);

            if (isPruningCandidateDominated(currentPruningCandidate, possiblyBetterRouteCandidate)) {
                foundDominatingPath = true;
            }
        }
        return foundDominatingPath;
    }

    private int pruneOrUpdateIndex(int currentPruningCandidateIndex, boolean foundDominatingPath) {
        if (foundDominatingPath) {
            this.candidates.remove(currentPruningCandidateIndex);
        } else {
            currentPruningCandidateIndex++;
        }
        return currentPruningCandidateIndex;
    }

    private boolean isPruningCandidateDominated(RouteCandidatePolygon currentPruningCandidate, RouteCandidatePolygon possiblyBetterRouteCandidate) {
        return possiblyBetterRouteCandidate.getDistance() < currentPruningCandidate.getDistance() &&
               possiblyBetterRouteCandidate.getDistanceInROI() > currentPruningCandidate.getDistanceInROI();
    }

    private boolean indexInCandidateBounds(int currentPruningCandidateIndex) {
        return currentPruningCandidateIndex < this.candidates.size();
    }
}
