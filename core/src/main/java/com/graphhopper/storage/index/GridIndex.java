package com.graphhopper.storage.index;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.*;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.Polygon;

import java.util.*;

public class GridIndex implements LocationIndex {
    private final static double MAX_LATITUDE = 90;
    private final static double MAX_LONGITUDE = 180;

    private final Graph graph;
    private final NodeAccess nodeAccess;
    private final DataAccess dataAccess;

    private int resolution = -1;
    private GridCell[][] index;
    private DistanceCalc distanceCalculator = Helper.DIST_PLANE;

    public GridIndex(final Graph graph, Directory dir) {
        this.graph = graph;
        this.nodeAccess = graph.getNodeAccess();
        dataAccess = dir.find("location_index", DAType.getPreferredInt(dir.getDefaultType()));
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
        final double[] latitudeIntervalSteps = getIntervalValuesForDirection((-1) * MAX_LATITUDE);
        final double[] longitudeIntervalSteps = getIntervalValuesForDirection(MAX_LONGITUDE);

        index = new GridCell[this.resolution][this.resolution];

        for (int i = 0; i < this.resolution; i++) {
            for (int j = 0; j < this.resolution; j++) {
                final BBox gridCellBoundingBox = new BBox(longitudeIntervalSteps[j], longitudeIntervalSteps[j + 1], latitudeIntervalSteps[i + 1], latitudeIntervalSteps[i]);
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
        final double latitude = this.nodeAccess.getLatitude(nodeId) * (-1);
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
        return new QueryResultCreator(lat, lon, edgeFilter).createQueryResult();
    }

    @Override
    public LocationIndex setApproximation(boolean approxDist) {
        if (approxDist)
            distanceCalculator = Helper.DIST_PLANE;
        else
            distanceCalculator = Helper.DIST_EARTH;
        return this;
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

        callVisitorOnNodesInCellIfBBoxOverlapsCell(queryBBox, function, i, j, gridCellBoundingBox);
    }

    private void callVisitorOnNodesInCellIfBBoxOverlapsCell(BBox queryBBox, Visitor function, int i, int j, BBox gridCellBoundingBox) {
        if (queryBBoxOverlapsCell(queryBBox, gridCellBoundingBox)) {
            final List<Integer> cellsNodes = index[i][j].nodes;

            callVisitorOnEachNodeInCell(function, cellsNodes);
        }
    }

    private boolean queryBBoxOverlapsCell(BBox queryBBox, BBox gridCellBoundingBox) {
        return gridCellBoundingBox.isOverlapping(queryBBox);
    }

    private void callVisitorOnEachNodeInCell(Visitor function, List<Integer> cellsNodes) {
        for (final int node : cellsNodes) {
            function.onNode(node);
        }
    }

    @Override
    public boolean loadExisting() {
        this.resolution = dataAccess.getHeader(0);
        this.prepareIndex();
        return true;
    }

    @Override
    public LocationIndex create(long byteCount) {
        return prepareIndex();
    }

    @Override
    public void flush() {
        dataAccess.setHeader(0, this.resolution);
    }

    @Override
    public void close() {
        dataAccess.close();
    }

    @Override
    public boolean isClosed() {
        return dataAccess.isClosed();
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

    private class QueryResultCreator {
        private final double queryLatitude;
        private final double queryLongitude;
        private final EdgeFilter edgeFilter;
        private final QueryResult queryResult;

        private GridCell[] currentSearchSpace;
        private int latitudeIndex;
        private int longitudeIndex;
        private int[] latitudeIndices;
        private int[] longitudeIndices;
        private int closestNode = -1;
        private double closestNodeDistance = Double.MAX_VALUE;

        private EdgeIteratorState closestEdge = null;
        private double closestEdgeDistance = Double.MAX_VALUE;

        private QueryResult.Position position = null;

        public QueryResultCreator(final double queryLatitude, final double queryLongitude, final EdgeFilter edgeFilter) {
            this.queryLatitude = queryLatitude;
            this.queryLongitude = queryLongitude;
            this.queryResult = new QueryResult(queryLatitude, queryLongitude);
            this.edgeFilter = edgeFilter;
        }

        public QueryResult createQueryResult() {
            findClosestNode();
            findClosestEdge();
            definePosition();
            addValuesToQueryResult();

            return queryResult;
        }

        private void findClosestNode() {
            createIntialSearchSpace();

            searchClosestNodesInEnlargingSpaces();

            throwErrorIfNoNodeWasFound();
        }

        private void searchClosestNodesInEnlargingSpaces() {
            while (closestNodeNotFound() && searchSpaceNotExhausted()) {
                findClosestNodeInSourrondingGridCells();

                setEnlargedIndices();
                setEnlargedSearchSpace();
            }
        }

        private void throwErrorIfNoNodeWasFound() {
            if (closestNode == -1) {
                throw new IllegalStateException("No closest node found. Most likely the index wasn't prepared with prepareIndex()");
            }
        }

        private boolean closestNodeNotFound() {
            return closestNode == -1;
        }

        private boolean searchSpaceNotExhausted() {
            return this.longitudeIndices.length < 2 * resolution;
        }

        private void createIntialSearchSpace() {
            latitudeIndex = getIndexByCoordinate(queryLatitude * (-1), MAX_LATITUDE);
            longitudeIndex = getIndexByCoordinate(queryLongitude, MAX_LONGITUDE);

            currentSearchSpace = getGridCellQueriedAndItsNeighbors(latitudeIndex, longitudeIndex);
        }

        private void findClosestNodeInSourrondingGridCells() {
            for (final GridCell cell : currentSearchSpace) {
                for (final int otherNode : cell.nodes) {
                    updateClosestNodeIfCloser(otherNode);
                }
            }
        }

        private void updateClosestNodeIfCloser(int otherNode) {
            final double otherNodeLatitude = nodeAccess.getLatitude(otherNode);
            final double otherNodeLongitude = nodeAccess.getLongitude(otherNode);

            final double distance = distanceCalculator.calcNormalizedDist(queryLatitude, queryLongitude, otherNodeLatitude, otherNodeLongitude);
            if (distance < closestNodeDistance) {
                updateCloserNodeBasedOnEdgeFilter(otherNode, distance);
            }
        }

        private void updateCloserNodeBasedOnEdgeFilter(int otherNode, double distance) {
            final EdgeIterator neighborExplorer = graph.createEdgeExplorer().setBaseNode(otherNode);
            while (neighborExplorer.next() && closestNode != otherNode) {
                if (edgeFilter.accept(neighborExplorer)) {
                    closestNode = otherNode;
                    closestNodeDistance = distance;
                }
            }
        }

        private void setEnlargedIndices() {
            this.latitudeIndices = getEnlargedIndices(this.latitudeIndices);
            this.longitudeIndices = getEnlargedIndices(this.longitudeIndices);
        }

        private int[] getEnlargedIndices(final int[] indices) {
            final int[] newIndices = new int[indices.length + 2];

            newIndices[0] = checkAndCorrectIndexForOutOfBounds(indices[0] - 1);
            System.arraycopy(indices, 0, newIndices, 1, indices.length);
            newIndices[newIndices.length - 1] = checkAndCorrectIndexForOutOfBounds(indices[indices.length - 1] + 1);

            return newIndices;
        }

        private GridCell[] getGridCellQueriedAndItsNeighbors(int latitudeIndex, int longitudeIndex) {
            getInitialLatIndices(latitudeIndex);
            getInitialLonIndices(longitudeIndex);

            return new GridCell[]{index[latitudeIndices[0]][longitudeIndices[0]],
                                  index[latitudeIndices[0]][longitudeIndices[1]],
                                  index[latitudeIndices[0]][longitudeIndices[2]],
                                  index[latitudeIndices[1]][longitudeIndices[0]],
                                  index[latitudeIndices[1]][longitudeIndices[1]],
                                  index[latitudeIndices[1]][longitudeIndices[2]],
                                  index[latitudeIndices[2]][longitudeIndices[0]],
                                  index[latitudeIndices[2]][longitudeIndices[1]],
                                  index[latitudeIndices[2]][longitudeIndices[2]]
            };
        }

        private void getInitialLatIndices(int latitudeIndex) {
            latitudeIndices = new int[] {checkAndCorrectIndexForOutOfBounds(latitudeIndex - 1),
                                                             latitudeIndex,
                                                             checkAndCorrectIndexForOutOfBounds(latitudeIndex + 1)};

        }

        private void getInitialLonIndices(int longitudeIndex) {
            longitudeIndices = new int [] {checkAndCorrectIndexForOutOfBounds(longitudeIndex - 1),
                                                              longitudeIndex,
                                                              checkAndCorrectIndexForOutOfBounds(longitudeIndex + 1)};
        }

        private void setEnlargedSearchSpace() {
            final GridCell[] newSearchSpace = new GridCell[numberOfCurrentSearchSpaceSurroundingCells()];

            int i = 0;
            for (int j = 0; j < latitudeIndices.length; j++) {
                newSearchSpace[i++] = index[latitudeIndices[j]][longitudeIndices[0]];
                newSearchSpace[i++] = index[latitudeIndices[j]][longitudeIndices[longitudeIndices.length - 1]];
            }
            for (int j = 1; j < longitudeIndices.length - 1; j++) {
                newSearchSpace[i++] = index[latitudeIndices[0]][longitudeIndices[j]];
                newSearchSpace[i++] = index[latitudeIndices[latitudeIndices.length - 1]][longitudeIndices[j]];
            }

            this.currentSearchSpace = newSearchSpace;
        }

        private int numberOfCurrentSearchSpaceSurroundingCells() {
            return latitudeIndices.length * 4  - 4;
        }

        private int checkAndCorrectIndexForOutOfBounds(final int index) {
            final int minIndex = 0;
            final int maxIndex = resolution - 1;

            int correctIndex = index;
            if (correctIndex > maxIndex) {
                correctIndex = minIndex;
            } else if (correctIndex < minIndex) {
                correctIndex = maxIndex;
            }

            return correctIndex;
        }

        private void findClosestEdge() {
            checkAllNeighborsOfClosestNodeForClosestEdge();
        }

        private void checkAllNeighborsOfClosestNodeForClosestEdge() {
            final EdgeIterator neighbourFinder = graph.createEdgeExplorer().setBaseNode(this.closestNode);
            while (neighbourFinder.next()) {
                updateClosestEdgeIfCloser(neighbourFinder);
            }
        }

        private void updateClosestEdgeIfCloser(EdgeIterator neighbourFinder) {
            final double neighbourLatitude = nodeAccess.getLatitude(neighbourFinder.getAdjNode());
            final double neighbourLongitude = nodeAccess.getLongitude(neighbourFinder.getAdjNode());

            final double distance = distanceCalculator.calcNormalizedDist(queryLatitude, queryLongitude, neighbourLatitude, neighbourLongitude);
            if (distance < closestEdgeDistance && edgeFilter.accept(neighbourFinder)) {
                closestEdge = neighbourFinder.detach(false);
                closestEdgeDistance = distance;
            }
        }

        private void definePosition() {
            this.position = QueryResult.Position.TOWER;
        }

        private boolean isDifferenceBetweenNodesSmallerDelta(double latitudeDifference, double longitudeDifference) {
            return (latitudeDifference + longitudeDifference) < 0.001;
        }

        private void addValuesToQueryResult() {
            queryResult.setClosestNode(this.closestNode);
            queryResult.setQueryDistance(this.closestNodeDistance);
            queryResult.setClosestEdge(this.closestEdge);
            queryResult.setSnappedPosition(this.position);
            queryResult.setWayIndex(0);
            queryResult.calcSnappedPoint(distanceCalculator);
        }
    }

    private class VisibilityCell {
        private final Polygon cellShape;

        private VisibilityCell(final List<Integer> nodeIds) {
            final double[] latitudes = new double[nodeIds.size()];
            final double[] longitudes = new double[nodeIds.size()];

            for (int i = 0; i < nodeIds.size(); i++) {
                latitudes[i] = nodeAccess.getLatitude(nodeIds.get(i));
                longitudes[i] = nodeAccess.getLongitude(nodeIds.get(i));
            }

            this.cellShape = new Polygon(latitudes, longitudes, 0);
        }

        public boolean isOverlapping(final GridCell gridCell) {
            return this.cellShape.isOverlapping(gridCell.boundingBox);
        }
    }

    /**
     * "Left" and "Right" are always imagined as walking from baseNode to adjacent node and then turn left or right.
     *
     * General schema: For each edge in the allEdgesIterator: Check if it was used in a left run, if not run left. Check if it was used in a right run if not run right
     */
    private class VisibilityCellsCreator {
        final EdgeIterator allEdges = graph.getAllEdges();
        final Map<EdgeIteratorState, Boolean> visitedLeft = new HashMap<>(graph.getEdges());
        final Map<EdgeIteratorState, Boolean> visitedRight = new HashMap<>(graph.getEdges());
        final EdgeExplorer neighborExplorer = graph.createEdgeExplorer();

        EdgeIteratorState currentEdge;
        int currentRunStartNode;
        int currentRunEndNode;
        EdgeIterator neighbors;

        final List<VisibilityCell> allFoundCells = new ArrayList<>(graph.getNodes());

        public List<VisibilityCell> create() {
            startRunsOnEachEdgeInTheGraph();

            return allFoundCells;
        }

        private void startRunsOnEachEdgeInTheGraph() {
            while (allEdges.next()) {
                currentEdge = allEdges.detach(false);
                currentRunStartNode = currentEdge.getAdjNode();
                currentRunEndNode = currentEdge.getBaseNode();

                if (!visibilityCellOnTheLeftFound()) {
                    allFoundCells.add(new CellRunnerLeft(visitedLeft).run());
                }

                if (!visibilityCellOnTheRightFound()) {
                    allFoundCells.add(new CellRunnerRight(visitedRight).run());
                }
            }
        }

        private Boolean visibilityCellOnTheLeftFound() {
            return visitedLeft.get(currentEdge);
        }

        private Boolean visibilityCellOnTheRightFound() {
            return visitedRight.get(currentEdge);
        }

        private abstract class CellRunner {
            private final Map<EdgeIteratorState, Boolean> visitedLeftOrRight;
            final List<Integer> nodesOnCell = new ArrayList<>();

            protected CellRunner(Map<EdgeIteratorState, Boolean> visitedLeftOrRight) {
                this.visitedLeftOrRight = visitedLeftOrRight;
            }

            public VisibilityCell run() {
                nodesOnCell.add(currentRunStartNode);

                neighbors = neighborExplorer.setBaseNode(currentRunStartNode);
                while (neighbors.getAdjNode() != currentRunEndNode) {
                    final EdgeIteratorState rightmostNeighbor = getMostLeftOrRightOrientedEdge(neighbors);
                    visitedRight.put(rightmostNeighbor, true);
                    neighbors = neighborExplorer.setBaseNode(rightmostNeighbor.getAdjNode());
                    nodesOnCell.add(rightmostNeighbor.getAdjNode());
                }

                return createVisibilityCell();
            }

            private EdgeIteratorState getMostLeftOrRightOrientedEdge(final EdgeIterator neighbors) {
                final int baseNode = neighbors.getBaseNode();

                neighbors.next();
                EdgeIteratorState leftOrRightMostNeighbor = neighbors.detach(false);
                while (neighbors.next()) {
                    final int possibleLeftOrRightPoint = neighbors.getAdjNode();
                    if (queryPointLeftOrRight(baseNode, leftOrRightMostNeighbor.getAdjNode(), possibleLeftOrRightPoint)) {
                        leftOrRightMostNeighbor = neighbors.detach(false);
                    }
                }

                return leftOrRightMostNeighbor;
            }

            abstract boolean queryPointLeftOrRight(int baseNode, int adjNode, int possibleLeftPoint);

            abstract VisibilityCell createVisibilityCell();

            double getDeterminant(final int baseNode, final int adjNode, final int possibleLeftPoint) {
                final double baseNodeX = nodeAccess.getLongitude(baseNode);
                final double baseNodeY = nodeAccess.getLatitude(baseNode);
                final double adjNodeX = nodeAccess.getLongitude(adjNode);
                final double adjNodeY = nodeAccess.getLatitude(adjNode);
                final double possibleLeftPointX = nodeAccess.getLongitude(possibleLeftPoint);
                final double possibleLeftPointY = nodeAccess.getLatitude(possibleLeftPoint);

                return (adjNodeX - baseNodeX)*(possibleLeftPointY - baseNodeY) - (adjNodeY - baseNodeY)*(possibleLeftPointX - baseNodeX);
            }
        }

        private class CellRunnerLeft extends CellRunner {
            private CellRunnerLeft(final Map<EdgeIteratorState, Boolean> visitedLeft) {
                super(visitedLeft);
            }

            @Override
            boolean queryPointLeftOrRight(int baseNode, int adjNode, int possibleLeftPoint) {
                return queryPointLeft(baseNode, adjNode, possibleLeftPoint);
            }

            private boolean queryPointLeft(final int baseNode, final int adjNode, final int possibleLeftPoint) {
                return getDeterminant(baseNode, adjNode, possibleLeftPoint) > 0;
            }

            @Override
            VisibilityCell createVisibilityCell() {
                Collections.reverse(nodesOnCell);
                return new VisibilityCell(nodesOnCell);
            }
        }

        private class CellRunnerRight extends CellRunner {
            private CellRunnerRight(final Map<EdgeIteratorState, Boolean> visitedRight) {
                super(visitedRight);
            }

            @Override
            boolean queryPointLeftOrRight(int baseNode, int adjNode, int possibleLeftPoint) {
                return queryPointRight(baseNode, adjNode, possibleLeftPoint);
            }

            private boolean queryPointRight(final int baseNode, final int adjNode, final int possibleRightPoint) {
                return getDeterminant(baseNode, adjNode, possibleRightPoint) < 0;
            }

            @Override
            VisibilityCell createVisibilityCell() {
                return new VisibilityCell(nodesOnCell);
            }
        }
    }
}
