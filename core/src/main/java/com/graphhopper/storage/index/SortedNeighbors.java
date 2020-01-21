package com.graphhopper.storage.index;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

public class SortedNeighbors {
    public final static int DO_NOT_IGNORE_NODE = -1;
    private final Graph graph;
    private final VectorAngleCalculator vectorAngleCalculator;

    private final Map<Integer, SortedNeighbors> subIterators = new HashMap<>();

    private final List<ComparableEdge> sortedEdges;

    public SortedNeighbors(final Graph graph, final int baseNode, final int ignore, final VectorAngleCalculator vectorAngleCalculator) {
        this.graph = graph;
        this.vectorAngleCalculator = vectorAngleCalculator;

        this.sortedEdges = sort(baseNode, ignore);
    }

    private List<ComparableEdge> sort(final int baseNode, final int ignore) {
        final List<ComparableEdge> comparableEdges = getAllNeighbors(baseNode, ignore);

        Collections.sort(comparableEdges);

        return comparableEdges;
    }

    private List<ComparableEdge> getAllNeighbors(int baseNode, final int ignore) {
        final EdgeIterator neighborIterator = graph.createEdgeExplorer().setBaseNode(baseNode);
        final List<ComparableEdge> comparableEdges = new ArrayList<>();

        addAllNeighborsMaybeIncludingCompareEdge(ignore, neighborIterator, comparableEdges);

        return comparableEdges;
    }

    private void addAllNeighborsMaybeIncludingCompareEdge(int ignore, EdgeIterator neighborIterator, List<ComparableEdge> comparableEdges) {
        while(neighborIterator.next()) {
            if (isNodeToIgnore(ignore, neighborIterator) && !isImpasseSubNode(neighborIterator)) {
                if (!hasEdgeEqualCoordinates(neighborIterator)) {
                    comparableEdges.add(new ComparableEdge(neighborIterator.detach(false)));
                } else {
                    comparableEdges.addAll(getAllNeighbors(neighborIterator.getAdjNode(), neighborIterator.getBaseNode()));
                }
            }
        }
    }

    private boolean isNodeToIgnore(int ignore, EdgeIterator neighborIterator) {
        return neighborIterator.getAdjNode() != ignore;
    }

    private boolean isImpasseSubNode(final EdgeIteratorState edge) {
        boolean isImpasse;

        if (hasEdgeEqualCoordinates(edge)) {
            isImpasse = !hasANeighborNonZeroLengthEdge(edge);
        } else {
            isImpasse = false;
        }

        return isImpasse;
    }

    private boolean hasANeighborNonZeroLengthEdge(EdgeIteratorState edge) {
        final List<EdgeIteratorState> neighbors = getNeighbors(edge.getAdjNode());

        boolean hasAnyNeighborNonZeroEdge = false;
        for (EdgeIteratorState neighbor : neighbors) {
            hasAnyNeighborNonZeroEdge |= hasNeighborNonZeroLengthEdge(edge, neighbor);
        }
        return hasAnyNeighborNonZeroEdge;
    }

    private boolean hasNeighborNonZeroLengthEdge(final EdgeIteratorState neighborPredecessor, final EdgeIteratorState neighbor) {
        final boolean hasEqualCoordinates = hasEdgeEqualCoordinates(neighbor);

        if (hasEqualCoordinates && !areEdgesEqual(neighborPredecessor, neighbor)) {
            return !isImpasseSubNode(neighbor);
        } else {
            return !hasEqualCoordinates;
        }
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

    private int indexIfEdgeWasAdded(final EdgeIteratorState lastEdge) {
        final ComparableEdge lastEdgeComparable = new ComparableEdge(lastEdge);
        final Iterator<ComparableEdge> allNeighborEdges = sortedEdges.iterator();

        int i = findIndex(lastEdgeComparable, allNeighborEdges);

        return i;
    }

    private int findIndex(ComparableEdge lastEdgeComparable, Iterator<ComparableEdge> allNeighborEdges) {
        boolean lastEdgeIsGreater = true;
        int i = 0;
        while (allNeighborEdges.hasNext() && lastEdgeIsGreater) {
            lastEdgeIsGreater = isLastEdgeGreater(lastEdgeComparable, allNeighborEdges);

            i = incrementIndex(lastEdgeIsGreater, i);
        }
        return i;
    }

    private boolean isLastEdgeGreater(ComparableEdge lastEdgeComparable, Iterator<ComparableEdge> allNeighborEdges) {
        final ComparableEdge currentEdge = allNeighborEdges.next();
        return lastEdgeComparable.compareTo(currentEdge) > 0;
    }

    private int incrementIndex(boolean lastEdgeIsGreater, int i) {
        i = lastEdgeIsGreater ? i + 1 : i;
        return i;
    }

    public EdgeIteratorState getMostOrientedEdge(final EdgeIteratorState lastEdge) {
        final int addIndex = indexIfEdgeWasAdded(lastEdge);
        int addIndexPredecessor = addIndex - 1;
        int indexOfEndOfList = sortedEdges.size() - 1;
        int indexOfPredecessorOfLastEdge = addIndexPredecessor < 0 ? indexOfEndOfList : addIndexPredecessor;

        return sortedEdges.get(indexOfPredecessorOfLastEdge).edge;
    }

    public EdgeIteratorState get(final int index) {
        return sortedEdges.get(index).edge;
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

        ComparableEdge(EdgeIteratorState edge) {
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
                    subIterators.put(adjNode, new SortedNeighbors(graph, adjNode, baseNode, vectorAngleCalculator));
                }

                angle = getAngle(subIterators.get(adjNode).getMostOrientedEdge(edge));
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
