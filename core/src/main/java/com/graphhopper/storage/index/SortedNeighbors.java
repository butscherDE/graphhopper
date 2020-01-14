package com.graphhopper.storage.index;

import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;

import java.util.*;

public class SortedNeighbors implements List<EdgeIteratorState> {
    private final VectorAngleCalculator vectorAngleCalculator;
    private final Graph graph;
    private final EdgeIteratorState baseEdge;

    private ArrayList<EdgeIteratorState> sortedEdges = new ArrayList<>();

    public SortedNeighbors(VectorAngleCalculator vectorAngleCalculator, Graph graph, int baseNode, EdgeIteratorState baseEdge) {
        this.vectorAngleCalculator = vectorAngleCalculator;
        this.graph = graph;
        this.baseEdge = baseEdge;

        sort(baseNode);
    }

    private void sort(final int baseNode) {
        final List<ComparableEdge> comparableEdges = getAllNeighbors(baseNode);

        Collections.sort(comparableEdges);

        unpackEdgesFromWrapperObjects(comparableEdges);
    }

    private List<ComparableEdge> getAllNeighbors(int baseNode) {
        final EdgeIterator neighborIterator = graph.createEdgeExplorer().setBaseNode(baseNode);
        final List<ComparableEdge> comparableEdges = new ArrayList<>();

        while(neighborIterator.next()) {
            comparableEdges.add(new ComparableEdge(neighborIterator.detach(false)));
        }
        return comparableEdges;
    }

    private void unpackEdgesFromWrapperObjects(List<ComparableEdge> comparableEdges) {
        for (ComparableEdge comparableEdge : comparableEdges) {
            sortedEdges.add(comparableEdge.edge);
        }
    }

    @Override
    public int size() {
        return sortedEdges.size();
    }

    @Override
    public boolean isEmpty() {
        return sortedEdges.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return sortedEdges.contains(o);
    }

    @Override
    public Iterator<EdgeIteratorState> iterator() {
        return sortedEdges.iterator();
    }

    @Override
    public Object[] toArray() {
        return sortedEdges.toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        return (T[]) sortedEdges.toArray();
    }

    @Override
    public boolean add(EdgeIteratorState edgeIteratorState) {
        throw new UnsupportedOperationException("This object already knows all neighbors of the given base node.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("This object wants to know all neighbors of the given base node.");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return sortedEdges.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends EdgeIteratorState> c) {
        throw new UnsupportedOperationException("This object already knows all neighbors of the given base node.");
    }

    @Override
    public boolean addAll(int index, Collection<? extends EdgeIteratorState> c) {
        throw new UnsupportedOperationException("This object already knows all neighbors of the given base node.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("This object wants to know all neighbors of the given base node.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("This object wants to know all neighbors of the given base node.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("This object wants to know all neighbors of the given base node.");
    }

    @Override
    public EdgeIteratorState get(int index) {
        return sortedEdges.get(index);
    }

    @Override
    public EdgeIteratorState set(int index, EdgeIteratorState element) {
        throw new UnsupportedOperationException("This object already knows all neighbors of the given base node.");
    }

    @Override
    public void add(int index, EdgeIteratorState element) {
        throw new UnsupportedOperationException("This object already knows all neighbors of the given base node.");
    }

    @Override
    public EdgeIteratorState remove(int index) {
        throw new UnsupportedOperationException("This object wants to know all neighbors of the given base node.");
    }

    @Override
    public int indexOf(Object o) {
        return sortedEdges.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return sortedEdges.indexOf(o);
    }

    @Override
    public ListIterator<EdgeIteratorState> listIterator() {
        return sortedEdges.listIterator();
    }

    @Override
    public ListIterator<EdgeIteratorState> listIterator(int index) {
        return sortedEdges.listIterator(index);
    }

    @Override
    public List<EdgeIteratorState> subList(int fromIndex, int toIndex) {
        return sortedEdges.subList(fromIndex, toIndex);
    }

    private class ComparableEdge implements Comparable<ComparableEdge> {
        private final EdgeIteratorState edge;

        public ComparableEdge(final EdgeIteratorState edge) {
            this.edge = edge;
        }

        @Override
        public int compareTo(ComparableEdge o) {
            final Double thisAngleToBaseEdge = vectorAngleCalculator.getAngleOfVectorsOriented(baseEdge, edge);
            final Double otherAngleToBaseEdge = vectorAngleCalculator.getAngleOfVectorsOriented(baseEdge, o.edge);
            final Integer thisId = edge.getEdge();
            final Integer otherId = o.edge.getEdge();

            int resultBasedOnAngle = thisAngleToBaseEdge.compareTo(otherAngleToBaseEdge);
            int resultBasedOnId = thisId.compareTo(otherId);

            return resultBasedOnAngle != 0 ? resultBasedOnAngle : resultBasedOnId;
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof ComparableEdge) {
                final ComparableEdge oAsCE = (ComparableEdge) o;

                return edge.getEdge() == oAsCE.edge.getEdge();
            } else {
                return false;
            }
        }
    }
}
