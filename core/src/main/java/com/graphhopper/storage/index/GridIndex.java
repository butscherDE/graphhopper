package com.graphhopper.storage.index;

import com.graphhopper.storage.*;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.Polygon;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.util.*;

public class GridIndex extends LocationIndexTree {
    private final static double MAX_LATITUDE = 90;
    private final static double MAX_LONGITUDE = 180;

    private final Graph graph;
    private final NodeAccess nodeAccess;
    private final DataAccess dataAccess;

    private int resolution = -1;
    private GridCell[][] index;
    private DistanceCalc distanceCalculator = Helper.DIST_PLANE;

    public GridIndex(final Graph graph, Directory dir) {
        super(graph, dir);
        this.graph = graph;
        this.nodeAccess = graph.getNodeAccess();
        dataAccess = dir.find("location_index", DAType.getPreferredInt(dir.getDefaultType()));
    }

    @Override
    public LocationIndex setResolution(int resolution) {
        super.setResolution(resolution);
//        failOnInvalidResolutionGiven(resolution);
//
//        this.resolution = resolution;
        this.resolution = (int) MAX_LONGITUDE * 10;
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

        super.prepareIndex();
        addAllVisibilityCellsOfGraphToIndex();

        return this;
    }

    private void failOnInvalidResolutionSet() {
        if (this.resolution < 0) {
            throw new IllegalStateException("Resolution was not set or set to an invalid value. Must be > 0.");
        }
    }

    private void addAllVisibilityCellsOfGraphToIndex() {
        final List<VisibilityCell> visibilityCells = new VisibilityCellsCreator().create();

        for (VisibilityCell visibilityCell : visibilityCells) {
            addThisToAllOverlappingGridCells(visibilityCell);
        }
    }

    private void addThisToAllOverlappingGridCells(VisibilityCell visibilityCell) {
        for (int i = 0; i < this.index.length; i++) {
            for (int j = 0; j < this.index[0].length; j++) {
                addIfGridCellOverlapsVisibilityCell(visibilityCell, this.index[i][j]);
            }
        }
    }

    private void addIfGridCellOverlapsVisibilityCell(VisibilityCell visibilityCell, GridCell index) {
        final GridCell gridCell = index;
        if (visibilityCell.isOverlapping(gridCell)) {
            gridCell.visibilityCells.add(visibilityCell);
        }
    }

    @Override
    public LocationIndexTree create(long byteCount) {
        prepareIndex();

        return super.create(byteCount);
    }

    private class GridCell {
        public final List<VisibilityCell> visibilityCells = new ArrayList<>();
        public final BBox boundingBox;

        public GridCell(final BBox boundingBox) {
            this.boundingBox = boundingBox;
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
     * <p>
     * General schema: For each edge in the allEdgesIterator: Check if it was used in a left run, if not run left. Check if it was used in a right run if not run right
     */
    private class VisibilityCellsCreator {
        final EdgeIterator allEdges = graph.getAllEdges();
        final VisitedManager visitedManager = new VisitedManager();
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
                currentEdge = visitedManager.forceNodeIdsAscending(currentEdge);
                currentRunStartNode = currentEdge.getAdjNode();
                currentRunEndNode = currentEdge.getBaseNode();

                if (!visibilityCellOnTheLeftFound()) {
                    allFoundCells.add(new CellRunnerLeft().runAroundCellAndLogNodes());
                }

                if (!visibilityCellOnTheRightFound()) {
                    allFoundCells.add(new CellRunnerRight().runAroundCellAndLogNodes());
                }
            }
        }

        private Boolean visibilityCellOnTheLeftFound() {
            return visitedManager.isEdgeSettledLeft(currentEdge);
        }

        private Boolean visibilityCellOnTheRightFound() {
            return visitedManager.isEdgeSettledRight(currentEdge);
        }

        private abstract class CellRunner {
            final List<Integer> nodesOnCell = new ArrayList<>();

            public VisibilityCell runAroundCellAndLogNodes() {
                addStartAndEndNodeOfCell();

                initializeNeighborIterator();
                do {
                    processNextNeighborOnCell();
                } while (lastCellNotReached());

                return createVisibilityCell();
            }

            private void addStartAndEndNodeOfCell() {
                nodesOnCell.add(currentRunEndNode);
                nodesOnCell.add(currentRunStartNode);
            }

            private void initializeNeighborIterator() {
                neighbors = neighborExplorer.setBaseNode(currentRunStartNode);
                neighbors.next();
            }

            private void processNextNeighborOnCell() {
                final EdgeIteratorState leftOrRightmostNeighbor = getMostLeftOrRightOrientedEdge(neighbors);
                settleNextNeighbor(leftOrRightmostNeighbor);
                getNextNeighborIterator(leftOrRightmostNeighbor);
            }

            private void settleNextNeighbor(EdgeIteratorState leftOrRightmostNeighbor) {
                settleEdge(leftOrRightmostNeighbor);
                nodesOnCell.add(leftOrRightmostNeighbor.getAdjNode());
            }

            private void getNextNeighborIterator(EdgeIteratorState leftOrRightmostNeighbor) {
                neighbors = neighborExplorer.setBaseNode(leftOrRightmostNeighbor.getAdjNode());
                neighbors.next();
            }

            private boolean lastCellNotReached() {
                return nodesOnCell.get(nodesOnCell.size() - 1) != currentRunEndNode;
            }

            private EdgeIteratorState getMostLeftOrRightOrientedEdge(final EdgeIterator neighbors) {
                final int lastEdgeReversedBaseNode = nodesOnCell.get(nodesOnCell.size() - 1);
                final int lastEdgeReversedAdjNode = nodesOnCell.get(nodesOnCell.size() - 2);

                EdgeIteratorState leftOrRightMostNeighbor = neighbors.detach(false);
                double leftOrRightMostAngle = getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, neighbors);
                while (neighbors.next()) {
                    final double angleToLastNode = getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, neighbors);

                    if (angleToLastNode > leftOrRightMostAngle) {
                        leftOrRightMostAngle = angleToLastNode;
                        leftOrRightMostNeighbor = neighbors.detach(false);
                    }
                }

                return leftOrRightMostNeighbor;
            }

            abstract double getAngleOfVectorsOriented(int lastEdgeReversedBaseNode, int lastEdgeReversedAdjNode, final EdgeIteratorState candidateEdge);

            abstract VisibilityCell createVisibilityCell();

            abstract void settleEdge(EdgeIteratorState edge);

            double getAngle(final int lastEdgeReversedBaseNode, final int lastEdgeReversedAdjNode, final EdgeIteratorState candidateEdge) {
                final Vector2D lastEdgeVector = createLastEdgeVector(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode);
                final Vector2D candidateEdgeVector = createCandidateEdgeVector(candidateEdge);

                final double angleTo =  lastEdgeVector.angleTo(candidateEdgeVector);
                final double angleToContinuousInterval = transformAngleToContinuousInterval(angleTo);

                return angleToContinuousInterval;
            }

            private Vector2D createLastEdgeVector(int lastEdgeReversedBaseNode, int lastEdgeReversedAdjNode) {
                final Coordinate lastEdgeBaseNodeCoordinate = new Coordinate(nodeAccess.getLongitude(lastEdgeReversedBaseNode), nodeAccess.getLatitude(lastEdgeReversedBaseNode));
                final Coordinate lastEdgeAdjNodeCoordinate = new Coordinate(nodeAccess.getLongitude(lastEdgeReversedAdjNode), nodeAccess.getLatitude(lastEdgeReversedAdjNode));
                return new Vector2D(lastEdgeBaseNodeCoordinate, lastEdgeAdjNodeCoordinate);
            }

            private Vector2D createCandidateEdgeVector(EdgeIteratorState candidateEdge) {
                final Coordinate candidateEdgeBaseNodeCoordinate = new Coordinate(nodeAccess.getLongitude(candidateEdge.getBaseNode()),
                                                                                  nodeAccess.getLatitude(candidateEdge.getBaseNode()));
                final Coordinate candidateEdgeAdjNodeCoordinate = new Coordinate(nodeAccess.getLongitude(candidateEdge.getAdjNode()),
                                                                                 nodeAccess.getLatitude(candidateEdge.getAdjNode()));
                return new Vector2D(candidateEdgeBaseNodeCoordinate, candidateEdgeAdjNodeCoordinate);
            }

            private double transformAngleToContinuousInterval(final double angleTo) {
                return angleTo >= 0 ? angleTo : angleTo + 2 * Math.PI;
            }
        }

        private class CellRunnerLeft extends CellRunner {
            @Override
            double getAngleOfVectorsOriented(int lastEdgeReversedBaseNode, int lastEdgeReversedAdjNode, EdgeIteratorState candidateEdge) {
                return getAngle(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdge);
            }

            @Override
            VisibilityCell createVisibilityCell() {
                Collections.reverse(nodesOnCell);
                return new VisibilityCell(nodesOnCell);
            }

            void settleEdge(final EdgeIteratorState edge) {
                visitedManager.settleEdgeLeft(edge);
            }
        }

        private class CellRunnerRight extends CellRunner {
            @Override
            double getAngleOfVectorsOriented(int lastEdgeReversedBaseNode, int lastEdgeReversedAdjNode, EdgeIteratorState candidateEdge) {
                final double angle = getAngle(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdge);
                return angle == 0 ? angle : angle * (-1) + 2 * Math.PI;
            }

            @Override
            VisibilityCell createVisibilityCell() {
                return new VisibilityCell(nodesOnCell);
            }

            void settleEdge(final EdgeIteratorState edge) {
                visitedManager.settleEdgeRight(edge);
            }
        }

        private class VisitedManager {
            final Map<EdgeIteratorState, Boolean> visitedLeft = new HashMap<>(graph.getEdges());
            final Map<EdgeIteratorState, Boolean> visitedRight = new HashMap<>(graph.getEdges());

            public void settleEdgeLeft(EdgeIteratorState edge) {
                edge = forceNodeIdsAscending(edge);
                visitedLeft.put(edge, true);
            }

            public void settleEdgeRight(EdgeIteratorState edge) {
                edge = forceNodeIdsAscending(edge);
                visitedRight.put(edge, true);
            }

            private boolean isEdgeSettledLeft(EdgeIteratorState edge) {
                return isEdgeSettled(edge, visitedLeft);
            }

            private boolean isEdgeSettledRight(EdgeIteratorState edge) {
                return isEdgeSettled(edge, visitedRight);
            }

            private boolean isEdgeSettled(EdgeIteratorState edge, Map<EdgeIteratorState, Boolean> isVisited) {
                edge = forceNodeIdsAscending(edge);
                final Boolean visited = isVisited.get(edge);
                return visited == null ? false : visited;
            }

            public EdgeIteratorState forceNodeIdsAscending(final EdgeIteratorState edge) {
                return edge.getBaseNode() < edge.getAdjNode() ? edge : edge.detach(true);
            }
        }
    }
}
