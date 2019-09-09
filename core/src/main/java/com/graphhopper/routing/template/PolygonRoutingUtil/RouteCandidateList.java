package com.graphhopper.routing.template.PolygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RouteCandidateList {
    public List<RouteCandidate> candidates;

    private RouteCandidateList(final List<RouteCandidate> candidates) {
        this.candidates = candidates;
    }

    public static RouteCandidateList createEmptyCandidateList() {
        return new RouteCandidateList(new ArrayList<RouteCandidate>());
    }

    public void sortByGainAscending() {
        Collections.sort(this.candidates);
    }

    public void sortRouteCandidatesToDistanceInROIDescending() {
        Collections.sort(this.candidates, new Comparator<RouteCandidate>() {
            @Override
            public int compare(RouteCandidate rc1, RouteCandidate rc2) {
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

        for (int i = 0; i < nOfFirstElements; i++) {
            paths.add(this.candidates.get(i).getMergedPath(queryGraph, algorithmOptions));
        }

        return paths;
    }
}
