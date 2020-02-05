package com.graphhopper.routing;

import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

public abstract class SetPathExplorer {
    final CHGraph chGraph;
    final Set<Integer> startSet;
    Stack<Integer> nodesToExplore;
    List<EdgeIteratorState> markedEdges;
    final Map<Integer, Boolean> nodesVisited = new HashMap<>();

    public SetPathExplorer(final CHGraph chGraph, Set<Integer> startSet) {
        this.chGraph = chGraph;
        this.chGraph.prepareAdjacencyLists();
        this.startSet = startSet;
        this.markedEdges = new LinkedList<>();
        prepareNodesToExplore(startSet);
        addAllTargetsAsVisited();
    }

    private void prepareNodesToExplore(Set<Integer> targets) {
        this.nodesToExplore = new Stack<>();
        for (Integer target : targets) {
            this.nodesToExplore.push(target);
        }
    }

    private void addAllTargetsAsVisited() {
        for (Integer target : startSet) {
            nodesVisited.put(target, true);
        }
    }
}
