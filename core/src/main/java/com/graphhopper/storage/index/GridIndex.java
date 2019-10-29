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
    private List<List<List<Integer>>> index;

    public GridIndex(final Graph graph) {
        this.graph = graph;
        this.nodeAccess = graph.getNodeAccess();
    }

    @Override
    public LocationIndex setResolution(int resolution) {
        failOnInvalidResolutionGiven(resolution);

        this.resolution = resolution;
        addRowsToGrid(resolution);
        addColumnsToGrid(resolution);

        return this;
    }

    private void addRowsToGrid(int resolution) {
        this.index = new ArrayList<>(resolution);
    }

    private void addColumnsToGrid(int resolution) {
        for (int i = 0; i < resolution; i++) {
            this.index.add(new ArrayList<List<Integer>>(resolution));

            addIndexForGridCell(resolution, i);
        }
    }

    private void addIndexForGridCell(int resolution, int i) {
        for (int j = 0; j < resolution; j++) {
            this.index.get(i).add(new ArrayList<Integer>());
        }
    }

    private void failOnInvalidResolutionGiven(int resolution) {
        if (resolution < 1) {
            throw new IllegalArgumentException("Resolution must be > 0.");
        }
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

        this.index.get(latitudeIndex).get(longitudeIndex).add(nodeId);
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
}
