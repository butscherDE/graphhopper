package com.graphhopper.storage.index;

import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

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

//        testMemKill(allNodes);
//        System.out.println("#!#!#!");
//        try {
//            Thread.sleep(10_000);
//        } catch (Exception e) {
//
//        }

        addSortedNeighbors(allNodes);
    }

//    private List<EdgeIteratorState> allEdges = new LinkedList<>();
//    private void testMemKill(final Set<Integer> allNodes) {
//        for (Integer allNode : allNodes) {
//            final EdgeIterator neighbors = graph.createEdgeExplorer().setBaseNode(allNode);
//            while(neighbors.next()) {
//                allEdges.add(neighbors.detach(false));
//                final double lat = graph.getNodeAccess().getLat(neighbors.getAdjNode());
//                final double lon = graph.getNodeAccess().getLon(neighbors.getAdjNode());
//            }
//        }
//    }

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
        int i = 0;
        for (Integer node : allNodes) {
            final SortedNeighbors sortedNeighborsLeft = new SortedNeighbors(graph, node, SortedNeighbors.DO_NOT_IGNORE_NODE, new VectorAngleCalculatorLeft(graph.getNodeAccess()));
            allSortedNeighborsLeft.put(node, sortedNeighborsLeft);
            final SortedNeighbors sortedNeighborsRight = new SortedNeighbors(graph, node, SortedNeighbors.DO_NOT_IGNORE_NODE, new VectorAngleCalculatorRight(graph.getNodeAccess()));
            allSortedNeighborsRight.put(node, sortedNeighborsRight);

            if (i % 1_000_000 == 0) {
                System.out.println(i + ": " + i / (double) allNodes.size() + " % processed, global size: " + estimateSize());
            }
            i++;
        }
    }

    private int estimateSize() {
        int globalSize = 0;

        for (Map.Entry<Integer, SortedNeighbors> entry : allSortedNeighborsLeft.entrySet()) {
            globalSize += entry.getValue().size();
        }
        for (Map.Entry<Integer, SortedNeighbors> entry : allSortedNeighborsRight.entrySet()) {
            globalSize += entry.getValue().size();
        }

        return globalSize;
    }
}
