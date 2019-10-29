package com.graphhopper.storage.index;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


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
}
