package com.graphhopper.routing;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class RPHASTTest {
    private final static PolygonRoutingTestGraph GRAPH_MOCKER = PolygonRoutingTestGraph.DEFAULT_INSTANCE;

    @Rule
    public final ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void failIfTargetSetNotPrepared() {
        final RPHAST rphast = new RPHAST(GRAPH_MOCKER.graphWithCh, GRAPH_MOCKER.weighting);

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Call prepareForTagetSet first");
        rphast.calcPaths(0);
    }

    @Test
    public void prepareForTargetSetNodeDoesntExist() {
        final Set<Integer> targetSet = new LinkedHashSet<>(Arrays.asList(0,300));

        final RPHAST rphast = new RPHAST(GRAPH_MOCKER.graphWithCh, GRAPH_MOCKER.weighting);
        rphast.prepareForTargetSet(targetSet);
    }
}
