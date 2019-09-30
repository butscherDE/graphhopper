package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RouteCandidateList<T extends RouteCandidatePolygon> {
    private List<T> candidates;

    public RouteCandidateList() {
        this.setCandidates(new ArrayList<T>());
    }

    public void sortByGainAscending() {
        Collections.sort(this.getCandidates());
    }

    private void sortRouteCandidatesToDistanceInROIDescending() {
        Collections.sort(this.getCandidates(), new Comparator<RouteCandidatePolygon>() {
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

    public List<Path> getFirstAsPathList(int nOfFirstElements, QueryGraph queryGraph, AlgorithmOptions algorithmOptions) {
        final List<Path> paths = new ArrayList<>(nOfFirstElements);

        final int endOfCandidates = getCandidates().size() - 1;
        paths.addAll(addPathsBasedOnIntersectionStatus(nOfFirstElements, queryGraph, algorithmOptions, endOfCandidates, false));
        paths.addAll(addPathsBasedOnIntersectionStatus(nOfFirstElements, queryGraph, algorithmOptions, endOfCandidates, true));

        return paths;
    }

    private List<Path> addPathsBasedOnIntersectionStatus(int nOfFirstElements, QueryGraph queryGraph, AlgorithmOptions algorithmOptions, int endOfCandidates,
                                                         boolean addSelfIntersecting) {
        List<Path> paths = new ArrayList<>(nOfFirstElements);
        int indexIntoCandidates = endOfCandidates;
        while (indexIntoCandidates >= 0 && paths.size() < nOfFirstElements) {
            final RouteCandidatePolygon candidate = this.getCandidates().get(indexIntoCandidates);

            if (candidate.isDetourSelfIntersecting(queryGraph, algorithmOptions) == addSelfIntersecting) {
                paths.add(candidate.getMergedPath(queryGraph, algorithmOptions));
            }

            indexIntoCandidates--;
        }

        return paths;
    }

    // Do it in a skyline problem pruning fashion
    public void pruneDominatedCandidateRoutes() {
        this.sortRouteCandidatesToDistanceInROIDescending();

        int currentPruningCandidateIndex = 1;
        while (indexInCandidateBounds(currentPruningCandidateIndex)) {
            RouteCandidatePolygon currentPruningCandidate = this.getCandidates().get(currentPruningCandidateIndex);

            boolean foundDominatingPath = isThisCandidateDominatedByAny(currentPruningCandidateIndex, currentPruningCandidate);

            currentPruningCandidateIndex = pruneOrUpdateIndex(currentPruningCandidateIndex, foundDominatingPath);
        }
    }

    private boolean isThisCandidateDominatedByAny(int currentPruningCandidateIndex, RouteCandidatePolygon currentPruningCandidate) {
        boolean foundDominatingPath = false;
        for (int i = currentPruningCandidateIndex - 1; i >= 0 && !foundDominatingPath; i--) {
            // routeCandidates must be sorted by now. Therefore dominators can only bbe found on lower indices than the current pruning candidate.
            RouteCandidatePolygon possiblyBetterRouteCandidate = this.getCandidates().get(i);

            if (isPruningCandidateDominated(currentPruningCandidate, possiblyBetterRouteCandidate)) {
                foundDominatingPath = true;
            }
        }
        return foundDominatingPath;
    }

    private int pruneOrUpdateIndex(int currentPruningCandidateIndex, boolean foundDominatingPath) {
        if (foundDominatingPath) {
            this.getCandidates().remove(currentPruningCandidateIndex);
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
        return currentPruningCandidateIndex < this.getCandidates().size();
    }

    public List<T> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<T> candidates) {
        this.candidates = candidates;
    }
}
