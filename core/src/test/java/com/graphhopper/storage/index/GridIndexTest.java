package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.storage.RAMDirectory;
import org.junit.Test;


public class GridIndexTest {
    final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();
    LocationIndex locationIndex = new GridIndex(graphMocker.graph, new RAMDirectory()).setResolution(300).prepareIndex();

    @Test
    public void nothing() {

    }
}
