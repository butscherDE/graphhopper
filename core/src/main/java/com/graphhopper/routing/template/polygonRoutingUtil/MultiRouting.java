package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MultiRouting {
    final Map<NodeIdPair, Path> allFoundPaths;

    MultiRouting() {
        this.allFoundPaths = new HashMap<>();
    }

    /**
     * Called once to find all paths between the nodes given in the class.
     */
    public void findPathBetweenAllNodePairs() {
        if (pathsNotAlreadySearched()) {
            calculatePaths();
        }
    }

    private boolean pathsNotAlreadySearched() {
        return allFoundPaths.size() == 0;
    }

    abstract void calculatePaths();

    /**
     * Outputs a list of all found paths
     *
     * @return the list of all found paths
     */
    public List<Path> getAllFoundPaths() {
        final List<Path> allFoundPathsList = new ArrayList<>(this.allFoundPaths.size());

        allFoundPathsList.addAll(this.allFoundPaths.values());

        return allFoundPathsList;
    }

    /**
     * Gets a specific path between two nodes.
     *
     * @param fromNodeId node where the path shall start from.
     * @param toNodeId   node where the path shall end to.
     * @return the path that starts at fromNodeId and ends at toNodeId
     */
    public Path getPathByFromEndNodeID(Integer fromNodeId, Integer toNodeId) {
        return this.allFoundPaths.get(new NodeIdPair(fromNodeId, toNodeId));
    }
}
