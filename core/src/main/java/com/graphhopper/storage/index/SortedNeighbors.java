package com.graphhopper.storage.index;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.storage.Graph;

import java.util.*;

public class SortedNeighbors {
    public final static int DONT_IGNORE_NODE = -1;
    private final Graph graph;
    private final VectorAngleCalculator vectorAngleCalculator;
    private final EdgeIteratorState baseEdge;

    private final Map<Integer, SortedNeighbors> subIterators = new HashMap<>();

    private final List<EdgeIteratorState> sortedEdges;

    public SortedNeighbors(Graph graph, final int baseNode, final int ignore, VectorAngleCalculator vectorAngleCalculator, EdgeIteratorState baseEdge) {
        this.graph = graph;
        this.vectorAngleCalculator = vectorAngleCalculator;
        this.baseEdge = baseEdge;

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
            if (isNodeToIgnore(ignore, neighborIterator) && !isImpasseSubNode(neighborIterator)) {
                comparableEdges.add(new ComparableEdge(neighborIterator.detach(false)));
            }
        }
    }

    private boolean isNodeToIgnore(int ignore, EdgeIterator neighborIterator) {
        return neighborIterator.getAdjNode() != ignore;
    }

    private boolean isImpasseSubNode(final EdgeIteratorState edge) {
        boolean isImpasse = true;

        if (hasEdgeEqualCoordinates(edge)) {
            final List<EdgeIteratorState> neighbors = getNeighbors(edge.getAdjNode());
//            if (isImpassThatLeadsBackToEqualCoordinateNode(neighbors)) {
//                return true;
//            }

            isImpasse &= !hasANeighborNonZeroLengthEdge(edge, neighbors);
        } else {
            isImpasse &=  false;
        }

        return isImpasse;
    }

    private boolean hasANeighborNonZeroLengthEdge(EdgeIteratorState edge, List<EdgeIteratorState> neighbors) {
        boolean hasNeighborNonZeroEdge = false;
        for (EdgeIteratorState neighbor : neighbors) {
            final boolean hasEqualCoords = hasEdgeEqualCoordinates(neighbor);
            if (hasEqualCoords && !areEdgesEqual(edge, neighbor)) {
                hasNeighborNonZeroEdge |= isImpasseSubNode(neighbor);
            } else if (!hasEqualCoords){
                hasNeighborNonZeroEdge |= true;
            }
        }
        return hasNeighborNonZeroEdge;
    }

    private boolean isImpassThatLeadsBackToEqualCoordinateNode(List<EdgeIteratorState> neighbors) {
        return neighbors.size() == 1;
    }

    private boolean areEdgesEqual(final EdgeIteratorState edge1, final EdgeIteratorState edge2) {
        final int edge1BaseNode = edge1.getBaseNode();
        final int edge1AdjNode = edge1.getAdjNode();
        final int edge2BaseNode = edge2.getBaseNode();
        final int edge2AdjNode = edge2.getAdjNode();

        return (edge1BaseNode == edge2BaseNode && edge1AdjNode == edge2AdjNode) || (edge1BaseNode == edge2AdjNode && edge1AdjNode == edge2BaseNode);
    }

    private boolean hasEdgeEqualCoordinates(final EdgeIteratorState edge) {
        final int baseNode = edge.getBaseNode();
        final int adjNode = edge.getAdjNode();

        final boolean latitudeIsEqual = isLatitudeIsEqual(baseNode, adjNode);
        final boolean longitudeIsEqual = isLongitudeEqual(baseNode, adjNode);

        return latitudeIsEqual && longitudeIsEqual;
    }

    private boolean isLatitudeIsEqual(int baseNode, int adjNode) {
        final NodeAccess nodeAccess = graph.getNodeAccess();

        final double baseNodeLatitude = nodeAccess.getLatitude(baseNode);
        final double adjNodeLatitude = nodeAccess.getLatitude(adjNode);

        return baseNodeLatitude == adjNodeLatitude;
    }

    private boolean isLongitudeEqual(int baseNode, int adjNode) {
        final NodeAccess nodeAccess = graph.getNodeAccess();

        final double baseNodeLongitude = nodeAccess.getLongitude(baseNode);
        final double adjNodeLongitude = nodeAccess.getLongitude(adjNode);

        return baseNodeLongitude == adjNodeLongitude;
    }

    private List<EdgeIteratorState> getNeighbors(final int node) {
        final EdgeIterator neighbors = graph.createEdgeExplorer().setBaseNode(node);
        List<EdgeIteratorState> neighborEdges = new LinkedList<>();

        while (neighbors.next()) {
            neighborEdges.add(neighbors.detach(false));
        }

        return neighborEdges;
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
