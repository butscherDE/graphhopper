package com.graphhopper.storage.index;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.BBox;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


public class GridIndexTest {
    final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();
    LocationIndex locationIndex = new GridIndex(graphMocker.graph, new RAMDirectory()).setResolution(300).prepareIndex();

    @Test
    public void nothing() {

    }
}
