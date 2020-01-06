package com.graphhopper.storage.index;

import com.graphhopper.util.EdgeIteratorState;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SubNeighborVisitor implements Iterable<EdgeIteratorState>, Cloneable {
    private final LinkedList<EdgeIteratorState> visitedEdges = new LinkedList<>();

    public SubNeighborVisitor() {

    }

    public SubNeighborVisitor(List<EdgeIteratorState> visitedEdges) {
        this.visitedEdges.addAll(visitedEdges);
    }

    public void onEdge(final EdgeIteratorState edge) {
        this.visitedEdges.add(edge);
    }

    @Override
    public Iterator<EdgeIteratorState> iterator() {
        return visitedEdges.iterator();
    }

    @Override
    public SubNeighborVisitor clone() {
        final List<EdgeIteratorState> newVisitedList = new LinkedList<>(visitedEdges);

        final SubNeighborVisitor newVisitor = new SubNeighborVisitor(newVisitedList);
        return newVisitor;
    }

    public EdgeIteratorState getLast() {
        return visitedEdges.getLast();
    }
}
