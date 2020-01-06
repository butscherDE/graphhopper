package com.graphhopper.storage.index;

import com.graphhopper.storage.*;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.Polygon;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class GridIndex extends LocationIndexTree {
    private final static double MAX_LATITUDE = 90;
    private final static double MAX_LONGITUDE = 180;
    private final static double ANGLE_WHEN_COORDINATES_ARE_EQUAL = -Double.MAX_VALUE;

    private final Graph graph;
    private final NodeAccess nodeAccess;

    private int resolution = -1;
    private GridCell[][] index;

    private boolean existingSuperIndexLoaded = false;

    public GridIndex(final Graph graph, Directory dir) {
        super(graph, dir);
        this.graph = graph;
        this.nodeAccess = graph.getNodeAccess();
    }

    public List<VisibilityCell> getIntersectingVisibilityCells(final Polygon polygon) {
        final Set<VisibilityCell> intersectingVisibilityCells = new CopyOnWriteArraySet<>();
        final GridCell[][] relevantGridCells = getGridCellsOverlapping(polygon);

        for (int i = 0; i < relevantGridCells.length; i++) {
            for (int j = 0; j < relevantGridCells[0].length; j++) {
                addAllIntersectingVisiblityCellsOfGridCell(intersectingVisibilityCells, relevantGridCells[i][j], polygon);
            }
        }

        return new ArrayList<>(intersectingVisibilityCells);
    }

    private GridCell[][] getGridCellsOverlapping(Polygon polygon) {
        final BBox polygonMinBoundingBox = polygon.getMinimalBoundingBox();

        return getGridCellsThatOverlapPolygonBoundingBox(polygonMinBoundingBox);
    }

    private void addAllIntersectingVisiblityCellsOfGridCell(Collection<VisibilityCell> overlappingVisibilityCells, GridCell gridCell, final Polygon polygon) {
        for (VisibilityCell visibilityCell : gridCell.visibilityCells) {
            if (visibilityCell.intersects(polygon)) {
                addVisibilityCellToResults(overlappingVisibilityCells, visibilityCell);
            }
        }
    }

    private void addVisibilityCellToResults(Collection<VisibilityCell> overlappingVisibilityCells, VisibilityCell visibilityCell) {
        overlappingVisibilityCells.add(visibilityCell);
    }

    @Override
    public LocationIndex setResolution(int resolution) {
        super.setResolution(resolution);

        this.resolution = (int) MAX_LONGITUDE * 10;
        initIndex();


        return this;
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

        if (!existingSuperIndexLoaded) {
            super.prepareIndex();
        }
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
        final GridCell[][] relevantGridCells = getGridCellsOverlapping(visibilityCell.cellShape);

        for (int i = 0; i < relevantGridCells.length; i++) {
            for (int j = 0; j < relevantGridCells[0].length; j++) {
                addIfGridCellOverlapsVisibilityCell(visibilityCell, relevantGridCells[i][j]);
            }
        }
    }

    private GridCell[][] getGridCellsThatOverlapPolygonBoundingBox(final BBox minBoundingBox) {
        final int minLongitudeIndex = getIndexByCoordinate(minBoundingBox.minLon, MAX_LONGITUDE);
        final int maxLongitudeIndex = getIndexByCoordinate(minBoundingBox.maxLon, MAX_LONGITUDE);
        final int minLatitudeIndex = getIndexByCoordinate(minBoundingBox.maxLat * (-1), MAX_LATITUDE);
        final int maxLatitudeIndex = getIndexByCoordinate(minBoundingBox.minLat * (-1), MAX_LATITUDE);

        final int lengthLongitude = maxLongitudeIndex - minLongitudeIndex + 1;
        final int lengthLatitude = maxLatitudeIndex - minLatitudeIndex + 1;
        GridCell[][] relevantGridCells = new GridCell[lengthLatitude][lengthLongitude];

        for (int i = minLatitudeIndex; i <= maxLatitudeIndex; i++) {
            copyRelevantLatitudeCellsOfThisLongitude(relevantGridCells[i - minLatitudeIndex], minLongitudeIndex, lengthLongitude, i);
        }

        return relevantGridCells;
    }

    private void copyRelevantLatitudeCellsOfThisLongitude(GridCell[] dest, int minLongitudeIndex, int lengthLongitude, int i) {
        System.arraycopy(index[i], minLongitudeIndex, dest, 0, lengthLongitude);
    }

    private int getIndexByCoordinate(final double latOrLong, final double maxValue) {
        final double nonNegativeLatitude = latOrLong + maxValue;
        return (int) (nonNegativeLatitude * this.resolution / (maxValue * 2));
    }


    private void addIfGridCellOverlapsVisibilityCell(VisibilityCell visibilityCell, GridCell index) {
        final GridCell gridCell = index;
        if (visibilityCell.isOverlapping(gridCell)) {
            addVisibilityCellToResults(gridCell.visibilityCells, visibilityCell);
        }
    }

    @Override
    public LocationIndexTree create(long byteCount) {
        prepareIndex();

        return super.create(byteCount);
    }

    @Override
    public boolean loadExisting() {
        existingSuperIndexLoaded = super.loadExisting();
        return false;
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
        private boolean visualize;

        public List<VisibilityCell> create() {
            startRunsOnEachEdgeInTheGraph();

            return allFoundCells;
        }

        private void startRunsOnEachEdgeInTheGraph() {
            int i = 0;
            while (allEdges.next()) {
                System.out.println("###################################################################");
                System.out.println(allEdges.getEdge() + ":" + allEdges.getBaseNode() + ":" + allEdges.getAdjNode());
                StopWatch sw1 = new StopWatch("run on one edge " + allEdges.getEdge() + ", " + i++ + "/" + graph.getEdges()).start();
                currentEdge = allEdges.detach(false);
                currentEdge = visitedManager.forceNodeIdsAscending(currentEdge);
                currentRunStartNode = currentEdge.getAdjNode();
                currentRunEndNode = currentEdge.getBaseNode();


                if (!visibilityCellOnTheLeftFound()) {
                    addVisibilityCellToResults(allFoundCells, new CellRunnerLeft().runAroundCellAndLogNodes());
                }
                System.out.println("--------------------------------------------------------------------");

                if (allEdges.getEdge() == 69) {
                    visualize = true;
                }
                if (!visibilityCellOnTheRightFound()) {
                    addVisibilityCellToResults(allFoundCells, new CellRunnerRight().runAroundCellAndLogNodes());
                }
                visualize = false;

                System.out.println(sw1.stop());
            }
            System.out.println("finished");
        }

        private Boolean visibilityCellOnTheLeftFound() {
            return visitedManager.isEdgeSettledLeft(visitedManager.forceNodeIdsAscending(currentEdge));
        }

        private Boolean visibilityCellOnTheRightFound() {
            return visitedManager.isEdgeSettledRight(visitedManager.forceNodeIdsAscending(currentEdge));
        }

        private abstract class CellRunner {
            final List<Integer> nodesOnCell = new ArrayList<>();
            private Stack<EdgeIteratorState> lastEdges = new Stack<>();
            int lala = 0;

            public VisibilityCell runAroundCellAndLogNodes() {
                addStartAndEndNodeOfCell();

                initializeNeighborIterator();
                do {
                    processNextNeighborOnCell();
//                    System.out.println(nodesOnCell);
                }
                while (lastCellNotReached());

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
                final SubNeighborVisitor leftOrRightmostNeighborChain = getMostLeftOrRightOrientedEdge(neighbors, new SubNeighborVisitor());

                for (EdgeIteratorState edge : leftOrRightmostNeighborChain) {
                    settleNextNeighbor(edge);
                }

                getNextNeighborIterator(leftOrRightmostNeighborChain.getLast());
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

            private SubNeighborVisitor getMostLeftOrRightOrientedEdge(final EdgeIterator neighbors, final SubNeighborVisitor subNeighborVisitor) {
                final int lastEdgeReversedBaseNode = nodesOnCell.get(nodesOnCell.size() - 1);
                final int lastEdgeReversedAdjNode = nodesOnCell.get(nodesOnCell.size() - 2);
                SubNeighborVisitor leftOrRightMostNeighborVisitedChain = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());// null;
                double leftOrRightMostAngle = getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, leftOrRightMostNeighborVisitedChain.getLast());
                //-Double.MAX_VALUE;
                while (neighbors.next()) {
//                do {
                    SubNeighborVisitor candidateEdgeContainingVisitor = setEdgeToCalcAngleTo(neighbors, subNeighborVisitor.clone());

                    final double angleToLastNode = getAngleOfVectorsOriented(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdgeContainingVisitor.getLast());

                    if (angleToLastNode >= leftOrRightMostAngle) {
                        leftOrRightMostAngle = angleToLastNode;
                        leftOrRightMostNeighborVisitedChain = candidateEdgeContainingVisitor;
                    }
                }
//                while (neighbors.next());

                if (leftOrRightMostNeighborVisitedChain == null) {
                    System.out.println();
                }

                return leftOrRightMostNeighborVisitedChain;
            }

            private SubNeighborVisitor setEdgeToCalcAngleTo(EdgeIterator neighbors, SubNeighborVisitor subNeighborVisitor) {
                SubNeighborVisitor candidateVisitor;

                final EdgeIteratorState detachedNeighbor = neighbors.detach(false);
                subNeighborVisitor.onEdge(detachedNeighbor);
                if (hasNeighborSameCoordinates(neighbors) && !isLastNode(detachedNeighbor)) {
//                    if (neighbors.getBaseNode() == 7254909 && neighbors.getAdjNode() == 93620) {
//                        System.out.println("");
//                        if (++lala == 10) {
//                            System.out.println(nodesOnCell.toString());
//                            final NodesAndNeighborDump nnd = new NodesAndNeighborDump(graph, Arrays.asList(new Integer[] {7254909, 93620, 48452, 5348789, 3560230, 5880975,
//                                                                                                                          9085088, 5880975, 48464, 47591, 47260, 47261}));
//                            nnd.dump();
//                            SwingGraphGUI gui = new SwingGraphGUI(nnd.getNodes(), nnd.getEdges());
//                            gui.visualizeGraph();
//                            try {
//                                Thread.sleep(100000);
//                            } catch (Exception e) {
//
//                            }
//                            System.exit(-1);
//                        }
//                    }
//                    System.out.println("\u001B[31m" + neighbors + "\u001B[30m");
                    lastEdges.push(detachedNeighbor);
                    candidateVisitor = findMostOrientedNeighborOfNeighbor(neighbors, subNeighborVisitor);
                    lastEdges.pop();
                } else {
                    candidateVisitor = subNeighborVisitor;
                }
                return candidateVisitor;
            }

            private boolean hasNeighborSameCoordinates(EdgeIterator neighbors) {
                return nodeAccess.getLongitude(neighbors.getBaseNode()) == nodeAccess.getLongitude(neighbors.getAdjNode()) &&
                       nodeAccess.getLatitude(neighbors.getBaseNode()) == nodeAccess.getLatitude(neighbors.getAdjNode());
            }

            private boolean isLastNode(final EdgeIteratorState edge) {
                boolean lastEdgeEqualsParameterEdge = false;

                try {
                    final EdgeIteratorState lastEdge = lastEdges.peek();
                    lastEdgeEqualsParameterEdge |= lastEdge.getBaseNode() == edge.getAdjNode();
                    lastEdgeEqualsParameterEdge &= lastEdge.getAdjNode() == edge.getBaseNode();
                } catch (EmptyStackException e) {
                    lastEdgeEqualsParameterEdge = false;
                }

                return lastEdgeEqualsParameterEdge;
            }

            private SubNeighborVisitor findMostOrientedNeighborOfNeighbor(EdgeIterator neighbors, final SubNeighborVisitor subNeighborVisitor) {
                final EdgeIterator subNeighborIterator = neighborExplorer.setBaseNode(neighbors.getAdjNode());
                subNeighborIterator.next();
                final SubNeighborVisitor bestSubNeighbor = getMostLeftOrRightOrientedEdge(subNeighborIterator, subNeighborVisitor);
                return bestSubNeighbor;
            }

            abstract double getAngleOfVectorsOriented(int lastEdgeReversedBaseNode, int lastEdgeReversedAdjNode, final EdgeIteratorState candidateEdge);

            abstract VisibilityCell createVisibilityCell();

            abstract void settleEdge(EdgeIteratorState edge);

            double getAngle(final int lastEdgeReversedBaseNode, final int lastEdgeReversedAdjNode, final EdgeIteratorState candidateEdge) {
                try {
                    return getAngleAfterErrorHandling(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdge);
                } catch (IllegalArgumentException e) {
                    return ANGLE_WHEN_COORDINATES_ARE_EQUAL;
                }
            }

            private double getAngleAfterErrorHandling(int lastEdgeReversedBaseNode, int lastEdgeReversedAdjNode, EdgeIteratorState candidateEdge) {
                final Vector2D lastEdgeVector = createLastEdgeVector(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode);
                final Vector2D candidateEdgeVector = createCandidateEdgeVector(candidateEdge);

                final double angleTo = lastEdgeVector.angleTo(candidateEdgeVector);
                final double angleToContinuousInterval = transformAngleToContinuousInterval(angleTo);
                final double differenceToTwoPi = Math.abs(2 * Math.PI - angleToContinuousInterval);
                final double angleToZeroIfVeryCloseTo2Pi = differenceToTwoPi < 0.000001 ? 0 : angleToContinuousInterval;

                return angleToZeroIfVeryCloseTo2Pi;
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
                if (candidateEdgeAdjNodeCoordinate.equals2D(candidateEdgeBaseNodeCoordinate)) {
                    throw new IllegalArgumentException("Coordinates of both edge end points shall not be equal");
                }
                return new Vector2D(candidateEdgeBaseNodeCoordinate, candidateEdgeAdjNodeCoordinate);
            }

            private double transformAngleToContinuousInterval(final double angleTo) {
                return angleTo > 0 ? angleTo : angleTo + 2 * Math.PI;
            }
        }

        private class SubNeighborVisitor implements Iterable<EdgeIteratorState>, Cloneable {
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

        private class CellRunnerLeft extends CellRunner {
            @Override
            double getAngleOfVectorsOriented(int lastEdgeReversedBaseNode, int lastEdgeReversedAdjNode, EdgeIteratorState candidateEdge) {
                return getAngle(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdge);
            }

            @Override
            VisibilityCell createVisibilityCell() {
                Collections.reverse(nodesOnCell);
                return VisibilityCell.createVisibilityCellFromNodeIDs(nodesOnCell, nodeAccess);
            }

            void settleEdge(final EdgeIteratorState edge) {
                visitedManager.settleEdgeLeft(edge);
            }
        }

        private class CellRunnerRight extends CellRunner {
            @Override
            double getAngleOfVectorsOriented(int lastEdgeReversedBaseNode, int lastEdgeReversedAdjNode, EdgeIteratorState candidateEdge) {
                final double angle = getAngle(lastEdgeReversedBaseNode, lastEdgeReversedAdjNode, candidateEdge);
                return angle == 0 || angle == ANGLE_WHEN_COORDINATES_ARE_EQUAL ? angle : angle * (-1) + 2 * Math.PI;
            }

            @Override
            VisibilityCell createVisibilityCell() {
                return VisibilityCell.createVisibilityCellFromNodeIDs(nodesOnCell, nodeAccess);
            }

            void settleEdge(final EdgeIteratorState edge) {
                visitedManager.settleEdgeRight(edge);
            }
        }

        private class VisitedManager {
            final Map<Integer, Boolean> visitedLeft = new HashMap<>(graph.getEdges());
            final Map<Integer, Boolean> visitedRight = new HashMap<>(graph.getEdges());

            public void settleEdgeLeft(EdgeIteratorState edge) {
                edge = forceNodeIdsAscending(edge);
                if (visitedLeft.get(edge.getEdge()) == null) {
                    visitedLeft.put(edge.getEdge(), true);
                }
            }

            public void settleEdgeRight(EdgeIteratorState edge) {
                edge = forceNodeIdsAscending(edge);
                if (visitedRight.get(edge.getEdge()) == null) {
                    visitedRight.put(edge.getEdge(), true);
                }
            }

            private boolean isEdgeSettledLeft(EdgeIteratorState edge) {
                return isEdgeSettled(edge, visitedLeft);
            }

            private boolean isEdgeSettledRight(EdgeIteratorState edge) {
                return isEdgeSettled(edge, visitedRight);
            }

            private boolean isEdgeSettled(EdgeIteratorState edge, Map<Integer, Boolean> isVisited) {
                final Boolean visited = isVisited.get(edge.getEdge());
                return visited == null ? false : visited;
            }

            public EdgeIteratorState forceNodeIdsAscending(final EdgeIteratorState edge) {
                return edge.getBaseNode() < edge.getAdjNode() ? edge : edge.detach(true);
            }
        }
    }
}
