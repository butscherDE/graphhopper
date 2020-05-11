package com.graphhopper.storage.index;

import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class NeighborPreSorter {
    private final Graph graph;

    private final Map<Integer, SortedNeighbors> allSortedNeighborsLeft;
    private final Map<Integer, SortedNeighbors> allSortedNeighborsRight;

    public NeighborPreSorter(Graph graph) {
        this.graph = graph;

        allSortedNeighborsLeft = new HashMap<>(graph.getNodes());
        allSortedNeighborsRight = new HashMap<>(graph.getNodes());

        createSortedNeighbors();
    }

    public Map<Integer, SortedNeighbors> getAllSortedNeighborsLeft() {
        return allSortedNeighborsLeft;
    }

    public Map<Integer, SortedNeighbors> getAllSortedNeighborsRight() {
        return allSortedNeighborsRight;
    }

    private void createSortedNeighbors() {
        final Set<Integer> allNodes = getAllNodes();

        addSortedNeighbors(allNodes);
    }

    private Set<Integer> getAllNodes() {
        final Set<Integer> allNodes = new LinkedHashSet<>();

        final EdgeIterator allEdges = graph.getAllEdges();
        while (allEdges.next()) {
            allNodes.add(allEdges.getBaseNode());
            allNodes.add(allEdges.getAdjNode());
        }
        return allNodes;
    }

    private void addSortedNeighbors(final Set<Integer> allNodes) {
        for (Integer node : allNodes) {
            final SortedNeighbors sortedNeighborsLeft = new SortedNeighbors(graph, node, SortedNeighbors.DO_NOT_IGNORE_NODE, new VectorAngleCalculatorLeft(graph.getNodeAccess()));
            allSortedNeighborsLeft.put(node, sortedNeighborsLeft);
            final SortedNeighbors sortedNeighborsRight = new SortedNeighbors(graph, node, SortedNeighbors.DO_NOT_IGNORE_NODE, new VectorAngleCalculatorRight(graph.getNodeAccess()));
            allSortedNeighborsRight.put(node, sortedNeighborsRight);
        }
    }
}
