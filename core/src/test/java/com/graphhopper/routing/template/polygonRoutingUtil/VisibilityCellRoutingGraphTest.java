package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.storage.index.VisibilityCell;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class VisibilityCellRoutingGraphTest {
    final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph();

    @Test
    public void testEdgeAcceptance() {
        final List<VisibilityCell> visibilityCells = createNodeVisibilityShape();
        final Polygon regionOfInterest = createRegionOfInterest();

        final VisibilityCellRoutingGraph acceptor =
                new VisibilityCellRoutingGraph(visibilityCells, regionOfInterest, graphMocker.locationIndex, graphMocker.nodeAccess);
        acceptor.prepareForEntryExitNodes(42,32);

        final List<Integer> acceptedEdges = getAcceptedEdges(acceptor);
        final List<Integer> acceptableEdges = getGroundTruth();

        assertEquals(acceptableEdges, acceptedEdges);
    }

    private List<Integer> getAcceptedEdges(VisibilityCellRoutingGraph acceptor) {
        final List<Integer> acceptedEdges = new ArrayList<>();
        final AllEdgesIterator allEdges = graphMocker.graph.getAllEdges();
        while (allEdges.next()) {
            if (acceptor.accept(allEdges)) {
                acceptedEdges.add(allEdges.getEdge());
            }
        }
        return acceptedEdges;
    }

    private List<Integer> getGroundTruth() {
        final List<Integer> acceptableEdges = Arrays.asList(new Integer[]{84, 101, 109, 110, 112, 115, 117, 120, 122, 125});
        return acceptableEdges;
    }

    private List<VisibilityCell> createNodeVisibilityShape() {
        final List<VisibilityCell> visibilityCells = new ArrayList<>(12);

        final Polygon p464754 = new Polygon(new double[]{17, 18, 15}, new double[]{16, 19, 18});
        visibilityCells.add(new VisibilityCell(p464754));

        final Polygon p475554 = new Polygon(new double[]{18, 15, 15}, new double[]{19, 20, 18});
        visibilityCells.add(new VisibilityCell(p475554));

        final Polygon p474855 = new Polygon(new double[]{18, 17, 15}, new double[]{19, 22, 20});
        visibilityCells.add(new VisibilityCell(p474855));

        final Polygon p484955 = new Polygon(new double[]{17, 14, 15}, new double[]{22, 23, 20});
        visibilityCells.add(new VisibilityCell(p484955));

        final Polygon p495655 = new Polygon(new double[]{14, 13, 15}, new double[]{23, 20, 20});
        visibilityCells.add(new VisibilityCell(p495655));

        final Polygon p495056 = new Polygon(new double[]{14, 11, 13}, new double[]{23, 22, 20});
        visibilityCells.add(new VisibilityCell(p495056));

        final Polygon p505156 = new Polygon(new double[]{11, 10, 13}, new double[]{22, 19, 20});
        visibilityCells.add(new VisibilityCell(p505156));

        final Polygon p515756 = new Polygon(new double[]{10, 13, 13}, new double[]{19, 18, 20});
        visibilityCells.add(new VisibilityCell(p515756));

        final Polygon p515257 = new Polygon(new double[]{10, 11, 13}, new double[]{19, 16, 18});
        visibilityCells.add(new VisibilityCell(p515257));

        final Polygon p525357 = new Polygon(new double[]{11, 14, 13}, new double[]{16, 15, 18});
        visibilityCells.add(new VisibilityCell(p525357));

        final Polygon p535457 = new Polygon(new double[]{14, 15, 13}, new double[]{15, 18, 18});
        visibilityCells.add(new VisibilityCell(p535457));

        final Polygon p534654 = new Polygon(new double[]{14, 17, 15}, new double[]{15, 16, 18});
        visibilityCells.add(new VisibilityCell(p534654));

        return visibilityCells;
    }

    private Polygon createRegionOfInterest() {
        return new Polygon(new double[]{16, 16, 12, 12}, new double[]{17, 21, 21, 17});
    }
}
