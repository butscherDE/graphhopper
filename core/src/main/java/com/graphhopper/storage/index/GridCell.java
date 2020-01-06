package com.graphhopper.storage.index;

import com.graphhopper.util.shapes.BBox;

import java.util.ArrayList;
import java.util.List;

public class GridCell {
    public final List<VisibilityCell> visibilityCells = new ArrayList<>();
    public final BBox boundingBox;

    public GridCell(final BBox boundingBox) {
        this.boundingBox = boundingBox;
    }
}
