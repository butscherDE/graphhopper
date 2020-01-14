package com.graphhopper.storage.index;

import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;

import java.util.*;

public class SortedNeighbors {
    public final static int DONT_IGNORE_NODE = -1;
    private final Graph graph;
    private final VectorAngleCalculator vectorAngleCalculator;
    private final EdgeIteratorState baseEdge;
//    private final EdgeIteratorState compareEdge;

    private final Map<Integer, SortedNeighbors> subIterators = new HashMap<>();

    private final List<EdgeIteratorState> sortedEdges;


//    public SortedNeighbors(final Graph graph, final int baseNode, final int ignore, final VectorAngleCalculator vectorAngleCalculator, final EdgeIteratorState baseEdge,
//                           final EdgeIteratorState compareEdge) {
//        this.graph = graph;
//        this.vectorAngleCalculator = vectorAngleCalculator;
//        this.baseEdge = baseEdge;
//        this.compareEdge = compareEdge;
//
//        List<EdgeIteratorState> sortedEdges = sort(baseNode, ignore);
//        this.sortedEdges = rearrangeSuchThatMostOrientedEdgeComesLast(sortedEdges);
//    }

    public SortedNeighbors(Graph graph, final int baseNode, final int ignore, VectorAngleCalculator vectorAngleCalculator, EdgeIteratorState baseEdge) {
//        this(graph, baseNode, ignore, vectorAngleCalculator, baseEdge, baseEdge);
        this.graph = graph;
        this.vectorAngleCalculator = vectorAngleCalculator;
        this.baseEdge = baseEdge;
//        this.compareEdge = compareEdge;

        List<EdgeIteratorState> sortedEdges = sort(baseNode, ignore);
        this.sortedEdges = rearrangeSuchThatMostOrientedEdgeComesLast(sortedEdges);
    }

    private List<EdgeIteratorState> sort(final int baseNode, final int ignore) {
        final List<ComparableEdge> comparableEdges = getAllNeighbors(baseNode, ignore);

        Collections.sort(comparableEdges);

        return unpackEdgesFromWrapperObjects(comparableEdges);
    }

    private List<ComparableEdge> getAllNeighbors(int baseNode, final int ignore) {
        final EdgeIterator neighborIterator = graph.createEdgeExplorer().setBaseNode(baseNode);
        final List<ComparableEdge> comparableEdges = new ArrayList<>();

        addAllNeighborsMaybeIncludingCompareEdge(ignore, neighborIterator, comparableEdges);
        addBaseEdgeIfNotAlready(comparableEdges);

        return comparableEdges;
    }

    private void addAllNeighborsMaybeIncludingCompareEdge(int ignore, EdgeIterator neighborIterator, List<ComparableEdge> comparableEdges) {
        while(neighborIterator.next()) {
            if (neighborIterator.getAdjNode() != ignore) {
                comparableEdges.add(new ComparableEdge(neighborIterator.detach(false)));
            }
        }
    }

    private void addBaseEdgeIfNotAlready(List<ComparableEdge> comparableEdges) {
        final ComparableEdge compareEdge = new ComparableEdge(this.baseEdge);
        if (!comparableEdges.contains(compareEdge)) {
            comparableEdges.add(compareEdge);
        }
    }

    private List<EdgeIteratorState> unpackEdgesFromWrapperObjects(List<ComparableEdge> comparableEdges) {
        List<EdgeIteratorState> sortedEdges = new ArrayList<>(comparableEdges.size());

        for (ComparableEdge comparableEdge : comparableEdges) {
            sortedEdges.add(comparableEdge.edge);
        }

        return sortedEdges;
    }

    private List<EdgeIteratorState> rearrangeSuchThatMostOrientedEdgeComesLast(final List<EdgeIteratorState> sortedEdges) {
        final int baseEdgeIndex = indexOfBaseEdge(sortedEdges);

        final List<EdgeIteratorState> rearrangedSortedEdges = new ArrayList<>(sortedEdges.size());
        rearrangedSortedEdges.addAll(sortedEdges.subList(baseEdgeIndex, sortedEdges.size()));
        rearrangedSortedEdges.addAll(sortedEdges.subList(0, baseEdgeIndex));

        return rearrangedSortedEdges;
    }

    private int indexOfBaseEdge(List<EdgeIteratorState> sortedEdges) {
        for (int i = 0; i < sortedEdges.size(); i++) {
            final EdgeIteratorState edge = sortedEdges.get(i);
            if ((edge.getBaseNode() == baseEdge.getBaseNode() && edge.getAdjNode() == baseEdge.getAdjNode() && edge.getEdge() == baseEdge.getEdge())) {
                return i;
            }
        }

        throw new IllegalArgumentException("List does not contain the base edge");
    }

    public EdgeIteratorState getMostOrientedEdge() {
        return sortedEdges.get(sortedEdges.size() - 1);
    }

    public EdgeIteratorState get(final int index) {
        return sortedEdges.get(index);
    }

    public int size() {
        return sortedEdges.size();
    }

    @Override
    public String toString() {
        return sortedEdges.toString();
    }

    private class ComparableEdge implements Comparable<ComparableEdge> {
        final EdgeIteratorState edge;

        public ComparableEdge(EdgeIteratorState edge) {
            this.edge = edge;
        }

        @Override
        public int compareTo(ComparableEdge o) {
            final double angleThis = getAngle(edge);
            final double angleOther = getAngle(o.edge);
            final double angleDifference = angleThis - angleOther;
            final int angleResult = angleDifference > 0 ? 1 : angleDifference == 0 ? 0 : -1;
            final int idDifference = edge.getEdge() - o.edge.getEdge();

            return angleResult != 0 ? angleResult : idDifference;
        }

        private Double getAngle(final EdgeIteratorState edge) {
            Double angle = vectorAngleCalculator.getAngleOfVectorsOriented(edge);

            if (angle == -Double.MAX_VALUE) {
                final int baseNode = edge.getBaseNode();
                final int adjNode = edge.getAdjNode();
                if (subIterators.get(adjNode) == null) {
                    subIterators.put(adjNode, new SortedNeighbors(graph, adjNode, baseNode, vectorAngleCalculator, baseEdge));
                }

                angle = getAngle(subIterators.get(adjNode).getMostOrientedEdge());
            }

            return angle;
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof ComparableEdge) {
                final ComparableEdge ce = (ComparableEdge) o;
                return edge.getEdge() == ce.edge.getEdge() && edge.getBaseNode() == ce.edge.getBaseNode();
            } else {
                return false;
            }
        }
    }
}
