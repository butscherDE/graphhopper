package com.graphhopper.storage.index;

import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

public class SubNeighborVisitor implements Iterable<EdgeIteratorState>, Cloneable {
    private final List<EdgeIteratorState> visitedEdges = new ArrayList<>();
    private boolean collinearEdgeFound = false;
    private final EdgeIteratorState previousEdge;

    public SubNeighborVisitor(final EdgeIteratorState previousEdge) {
        this.previousEdge = previousEdge;
    }

    public SubNeighborVisitor(List<EdgeIteratorState> visitedEdges, final EdgeIteratorState previousEdge) {
        this.visitedEdges.addAll(visitedEdges);
        this.previousEdge = previousEdge;
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

        final SubNeighborVisitor newVisitor = new SubNeighborVisitor(newVisitedList, previousEdge);
        if (collinearEdgeFound) {
            newVisitor.collinearEdgeFound();
        }
        return newVisitor;
    }

    public EdgeIteratorState getLast() {
        return visitedEdges.get(visitedEdges.size() - 1);
    }

    public void collinearEdgeFound() {
        collinearEdgeFound = true;
    }

    public Map<Integer, Integer> getNextNodeHints() {
        final Map<Integer, Integer> hints = new HashMap<>();

        if (collinearEdgeFound) {
            final int limitToSkipTheLastElement = visitedEdges.size() - 1;
            for (int i = 0; i < limitToSkipTheLastElement; i++) {
                final EdgeIteratorState edge = visitedEdges.get(i);
                hints.put(edge.getAdjNode(), edge.getBaseNode());
            }
            hints.put(previousEdge.getAdjNode(), previousEdge.getBaseNode());
        }

        return hints;
    }
}
