package com.graphhopper.storage.index;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.*;
import com.graphhopper.util.shapes.BBox;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class GridIndex implements LocationIndex {
    private final static double MAX_LATITUDE = 90;
    private final static double MAX_LONGITUDE = 180;

    private final Graph graph;
    private final NodeAccess nodeAccess;
    private int resolution = -1;
    private GridCell[][] index;

    public GridIndex(final Graph graph) {
        this.graph = graph;
        this.nodeAccess = graph.getNodeAccess();
    }

    @Override
    public LocationIndex setResolution(int resolution) {
        failOnInvalidResolutionGiven(resolution);

        this.resolution = resolution;
        initIndex();

        return this;
    }

    private void failOnInvalidResolutionGiven(int resolution) {
        if (resolution < 1) {
            throw new IllegalArgumentException("Resolution must be > 0.");
        }
    }

    private void initIndex() {
        final double[] latitudeIntervalSteps = getIntervalValuesForDirection(MAX_LATITUDE);
        final double[] longitudeIntervalSteps = getIntervalValuesForDirection(MAX_LONGITUDE);

        index = new GridCell[this.resolution][this.resolution];

        for (int i = 0; i < this.resolution; i++) {
            for (int j = 0; j < this.resolution; j++) {
                final BBox gridCellBoundingBox = new BBox(longitudeIntervalSteps[j + 1], longitudeIntervalSteps[j], latitudeIntervalSteps[j], latitudeIntervalSteps[j + 1]);
                index[i][j] = new GridCell(gridCellBoundingBox);
            }
        }
    }

    private double[] getIntervalValuesForDirection(final double maxValueOfDirection) {
        final int numValues = this.resolution + 1;
        final double[] intervalValues = new double[numValues];

        for (int i = 0; i < numValues; i++) {
            intervalValues[i] = (maxValueOfDirection * 2 * i) / this.resolution - maxValueOfDirection;
        }

        return intervalValues;
    }

    @Override
    public LocationIndex prepareIndex() {
        failOnInvalidResolutionSet();

        addAllNodesOfGraphToIndex();

        return this;
    }

    private void failOnInvalidResolutionSet() {
        if (this.resolution < 0) {
            throw new IllegalStateException("Resolution was not set or set to an invalid value. Must be > 0.");
        }
    }

    private void addAllNodesOfGraphToIndex() {
        final AllEdgesIterator allEdges = this.graph.getAllEdges();
        final BitSet nodeFound = new BitSet(this.graph.getNodes());

        while (allEdges.next()) {
            settleNodesOfEdgeToIndex(allEdges, nodeFound);
        }
    }

    private void settleNodesOfEdgeToIndex(AllEdgesIterator allEdges, BitSet nodeFound) {
        final int baseNode = allEdges.getBaseNode();
        final int adjNode = allEdges.getAdjNode();

        settleNodeIfNotDone(nodeFound, baseNode);
        settleNodeIfNotDone(nodeFound, adjNode);
    }

    private void settleNodeIfNotDone(BitSet nodeFound, int nodeId) {
        if (nodeNotAlreadySettled(nodeFound, nodeId)) {
            settleNode(nodeFound, nodeId);
        }
    }

    private boolean nodeNotAlreadySettled(BitSet nodeFound, int nodeId) {
        return !nodeFound.get(nodeId);
    }

    private void settleNode(BitSet nodeFound, int nodeId) {
        nodeFound.set(nodeId);

        final int latitudeIndex = getLatitudeIndex(nodeId);
        final int longitudeIndex = getLongitudeIndex(nodeId);

        this.index[latitudeIndex][longitudeIndex].nodes.add(nodeId);
    }

    private int getLatitudeIndex(int nodeId) {
        final double latitude = this.nodeAccess.getLatitude(nodeId);
        return getIndexByCoordinate(latitude, MAX_LATITUDE);
    }

    private int getLongitudeIndex(int nodeId) {
        final double longitude = this.nodeAccess.getLongitude(nodeId);
        return getIndexByCoordinate(longitude, MAX_LONGITUDE);
    }

    private int getIndexByCoordinate(final double latOrLong, final double maxValue) {
        final double nonNegativeLatitude = latOrLong + maxValue;
        return (int) (nonNegativeLatitude * this.resolution / (maxValue * 2));
    }

    @Override
    public QueryResult findClosest(double lat, double lon, EdgeFilter edgeFilter) {
        return null;
    }

    @Override
    public LocationIndex setApproximation(boolean approxDist) {
        return null;
    }

    @Override
    public void setSegmentSize(int bytes) {

    }

    @Override
    public void query(BBox queryBBox, Visitor function) {
        executeQueryForEachIndexCell(queryBBox, function);
    }

    private void executeQueryForEachIndexCell(BBox queryBBox, Visitor function) {
        for (int i = 0; i < this.resolution; i++) {
            for (int j = 0; j < this.resolution; j++) {
                executeQueryOnCell(queryBBox, function, i, j);
            }
        }
    }

    private void executeQueryOnCell(BBox queryBBox, Visitor function, int i, int j) {
        final BBox gridCellBoundingBox = this.index[i][j].boundingBox;

        callVisitorOnNodesInCellIfBBoxIntersectsCell(queryBBox, function, i, j, gridCellBoundingBox);
    }

    private void callVisitorOnNodesInCellIfBBoxIntersectsCell(BBox queryBBox, Visitor function, int i, int j, BBox gridCellBoundingBox) {
        if (queryBBoxIntersectsCell(queryBBox, gridCellBoundingBox)) {
            final List<Integer> cellsNodes = index[i][j].nodes;

            callVisitorOnEachNodeInCell(function, cellsNodes);
        }
    }

    private boolean queryBBoxIntersectsCell(BBox queryBBox, BBox gridCellBoundingBox) {
        return gridCellBoundingBox.intersects(queryBBox);
    }

    private void callVisitorOnEachNodeInCell(Visitor function, List<Integer> cellsNodes) {
        for (final int node : cellsNodes) {
            function.onNode(node);
        }
    }

    @Override
    public boolean loadExisting() {
        return false;
    }

    @Override
    public LocationIndex create(long byteCount) {
        return null;
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public long getCapacity() {
        return 0;
    }

    private class GridCell {
        public final List<Integer> nodes;
        public final BBox boundingBox;

        public GridCell(final BBox boundingBox) {
            this.nodes = new ArrayList<>();
            this.boundingBox = boundingBox;
        }
    }
}
