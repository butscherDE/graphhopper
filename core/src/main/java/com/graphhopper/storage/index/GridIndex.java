package com.graphhopper.storage.index;

import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.Polygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class GridIndex extends LocationIndexTree {
    private final static double MAX_LATITUDE = 90;
    private final static double MAX_LONGITUDE = 180;

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

        for (GridCell[] relevantGridCell : relevantGridCells) {
            for (int j = 0; j < relevantGridCells[0].length; j++) {
                addAllIntersectingVisibilityCellsOfGridCell(intersectingVisibilityCells, relevantGridCell[j], polygon);
            }
        }

        return new ArrayList<>(intersectingVisibilityCells);
    }

    private GridCell[][] getGridCellsOverlapping(Polygon polygon) {
        final BBox polygonMinBoundingBox = polygon.getMinimalBoundingBox();

        return getGridCellsThatOverlapPolygonBoundingBox(polygonMinBoundingBox);
    }

    private void addAllIntersectingVisibilityCellsOfGridCell(Collection<VisibilityCell> overlappingVisibilityCells, GridCell gridCell, final Polygon polygon) {
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
        final List<VisibilityCell> visibilityCells = new VisibilityCellsCreator(graph, nodeAccess).create();

        for (VisibilityCell visibilityCell : visibilityCells) {
            addThisToAllOverlappingGridCells(visibilityCell);
        }
    }

    private void addThisToAllOverlappingGridCells(VisibilityCell visibilityCell) {
        final GridCell[][] relevantGridCells = getGridCellsOverlapping(visibilityCell.cellShape);

        for (GridCell[] relevantGridCell : relevantGridCells) {
            for (int j = 0; j < relevantGridCells[0].length; j++) {
                addIfGridCellOverlapsVisibilityCell(visibilityCell, relevantGridCell[j]);
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


    private void addIfGridCellOverlapsVisibilityCell(VisibilityCell visibilityCell, GridCell gridCell) {
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

}
