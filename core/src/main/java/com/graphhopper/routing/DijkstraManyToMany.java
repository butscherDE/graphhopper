package com.graphhopper.routing;

import com.carrotsearch.hppc.IntIndexedContainer;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * Implements a many to many Dijkstra Search that serves the purpose of building a path skeleton of Professor Storandts Region-Aware Route Planning paper.
 */
public class DijkstraManyToMany extends DijkstraOneToMany {
    private final List<Integer> interiorGraphNode;
    private final List<Integer> entryExitPoints;
    private final List<Path> allPaths;
    private int currentTo;

    public DijkstraManyToMany(Graph graph, Weighting weighting, TraversalMode tMode, final List<Integer> interiorGraphNode, final List<Integer> entryExitPoints) {
        super(graph, weighting, tMode);
        this.interiorGraphNode = interiorGraphNode;
        this.entryExitPoints = entryExitPoints;
        this.allPaths = new LinkedList<>();
    }

    @Override
    protected boolean accept(EdgeIteratorState iter, int prevOrNextEdgeId) {
        boolean superAcceptance = super.accept(iter, prevOrNextEdgeId);
        final int adjacentNode = iter.getAdjNode();
        boolean nodeInteriorOrTarget = adjacentNode == currentTo || interiorGraphNode.contains(adjacentNode);
        return superAcceptance && nodeInteriorOrTarget;
    }

    public void findAllPathsBetweenEntryExitPoints() {
        if (allPaths.size() > 0) {
            throw new IllegalStateException("The algorithm was already run. Retrieve results with getAllPaths()");
        }

        for (int from : entryExitPoints) {
            pathsOneToMany(from);
            this.clear();
        }
    }

    private void pathsOneToMany(int from) {
        for (int to : entryExitPoints) {
            this.currentTo = to;

            Path newPath = this.calcPath(from, to);
            allPaths.add(newPath);
        }
    }

    public List<Integer> buildPathSkeleton() {
        Set<Integer> unionedNodesFromPaths = new HashSet<>(allPaths.size() * 10);

        for (Path path : allPaths) {
            addPathNodesToSet(unionedNodesFromPaths, path);
        }

        return new ArrayList<>(unionedNodesFromPaths);
    }

    private void addPathNodesToSet(Set<Integer> unionedNodesFromPaths, Path path) {
        IntIndexedContainer nodesInPath = path.calcNodes();
        for (int i = 0; i < nodesInPath.size(); i++) {
            int node = nodesInPath.get(i);
            unionedNodesFromPaths.add(node);
        }
    }
}
