package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.Path;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MultiRouting {
    protected final Map<Pair<Integer, Integer>, Path> allFoundPaths;

    public MultiRouting() {
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
     * @return
     */
    public List<Path> getAllFoundPaths() {
        final List<Path> allFoundPathsList = new ArrayList<>(this.allFoundPaths.size());

        for (final Path path : this.allFoundPaths.values()) {
            allFoundPathsList.add(path);
        }

        return allFoundPathsList;
    }

    /**
     * Gets a specific path between two nodes.
     *
     * @param fromNodeId node where the path shall start from.
     * @param toNodeId   node where the path shall end to.
     * @return
     */
    public Path getPathByFromEndNodeID(Integer fromNodeId, Integer toNodeId) {
        return this.allFoundPaths.get(new Pair<Integer, Integer>(fromNodeId, toNodeId));
    }
}
