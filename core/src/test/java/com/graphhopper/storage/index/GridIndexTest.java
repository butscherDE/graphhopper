package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.util.shapes.BBox;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


public class GridIndexTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();

    @Test
    public void objectCanBeConstructed() {
        final LocationIndex locationIndex = new GridIndex(this.graphMocker.graph);
    }

    @Test
    public void prepareIndexCallWithoutResolutionSet() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Resolution was not set or set to an invalid value. Must be > 0.");

        LocationIndex locationIndex = new GridIndex(this.graphMocker.graph);
        Assert.assertNotNull(locationIndex.prepareIndex());
    }

    @Test
    public void indexGetsPrepared() {
        LocationIndex locationIndex = new GridIndex(this.graphMocker.graph).setResolution(10);
        Assert.assertNotNull(locationIndex.prepareIndex());
    }

    @Test
    public void errorHandlingOnWrongResolution() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Resolution must be > 0.");

        new GridIndex(this.graphMocker.graph).setResolution(-1);
    }

    @Test
    public void legalValuesForResolution() {
        new GridIndex(this.graphMocker.graph).setResolution(1).setResolution(2).setResolution(1337);
    }

    @Test
    public void testCorrectQueryExecution() {
        final LocationIndex locationIndex = new GridIndex(this.graphMocker.graph).setResolution(180);
        final LoggingVisitor visitor = new LoggingVisitor();

        final BBox queryBBox = new BBox(8, 11, 9, 16);
        locationIndex.query(queryBBox, visitor);

        assertTrue(visitor.nodesFound.contains(41));
        assertTrue(visitor.nodesFound.contains(42));
        assertTrue(visitor.nodesFound.contains(43));
        assertEquals(3, visitor.nodesFound.size());
    }

    private class LoggingVisitor extends LocationIndex.Visitor {
        public final List<Integer> nodesFound = new ArrayList<Integer>();

        @Override
        public void onNode(int nodeId) {
            this.nodesFound.add(nodeId);
        }
    }
}
