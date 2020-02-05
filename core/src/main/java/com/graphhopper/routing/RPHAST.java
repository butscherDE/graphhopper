package com.graphhopper.routing;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RPHAST {
    private final CHGraph graph;
    private final Weighting weighting;

    private Set<Integer> targetSet;
    private List<EdgeIteratorState> restrictedDownwardsGraphEdges;

    public RPHAST(final GraphHopperStorage graph, final Weighting weighting) {
        this.graph = graph.getCHGraph();
        this.weighting = weighting;
    }

    public void prepareForTargetSet(final Set<Integer> targetSet) {
        this.targetSet = targetSet;

        getMarkedEdges();
    }

    private void getMarkedEdges() {
        TargetSetReverseUpwardPathsExplorer targetExplorer = new TargetSetReverseUpwardPathsExplorer(graph, targetSet);
        restrictedDownwardsGraphEdges = targetExplorer.getMarkedEdges();
        sortDescending(restrictedDownwardsGraphEdges);
    }

    private void sortDescending(List<EdgeIteratorState> markedEdges) {
        Collections.sort(markedEdges, (edge1, edge2) -> {
            final int edge1AdjNode = edge1.getAdjNode();
            final int edge2AdjNode = edge2.getAdjNode();

            final int edge1AdjNodeRank = graph.getLevel(edge1AdjNode);
            final int edge2AdjNodeRank = graph.getLevel(edge2AdjNode);

            return Integer.compare(edge1AdjNodeRank, edge2AdjNodeRank) * -1;
        });
    }

    public void calcPaths(final List<Integer> sourceNodes) {


    }
}
