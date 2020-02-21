package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RPHAST;
import com.graphhopper.storage.GraphHopperStorage;

import java.util.LinkedHashSet;
import java.util.List;

public class RPHASTManyToMany extends MultiRouting {
    private final PathSkeletonGraph pathSkeletonGraph;
    private final List<Integer> nodesToBuildRoutesWith;
    private final RPHAST rphast;

    public RPHASTManyToMany(PathSkeletonGraph pathSkeletonGraph, List<Integer> nodesToBuildRoutesWith, GraphHopperStorage graph, final AlgorithmOptions algorithmOptions) {
        this.pathSkeletonGraph = pathSkeletonGraph;
//        this.pathSkeletonGraph.prepareForEntryExitNodes(nodesToBuildRoutesWith, nodesToBuildRoutesWith);
        this.nodesToBuildRoutesWith = nodesToBuildRoutesWith;
        this.rphast = new RPHAST(graph, algorithmOptions.getWeighting(), pathSkeletonGraph);
    }

    @Override
    void calculatePaths() {
        rphast.prepareForTargetSet(new LinkedHashSet<>(nodesToBuildRoutesWith));

        final List<Path> paths = rphast.calcPaths(nodesToBuildRoutesWith);

        for (Path path : paths) {
            final NodeIdPair pair = new NodeIdPair(path.getFromNode(), path.getEndNode());
            this.allFoundPaths.put(pair, path);
        }
    }
}
