package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.storage.index.VisibilityCell;
import com.graphhopper.util.shapes.Polygon;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NodesInVCButNotInROIFindingVisitorTest {
    private final static PolygonRoutingTestGraph graphMocker = PolygonRoutingTestGraph.DEFAULT_INSTANCE;

    @Test
    public void fromPolygon() {
        final VisibilityCell visibilityCell = createPolygonVisibilityShape();
        final Polygon regionOfInterest = createRegionOfInterest();

        final List<Integer> nodesInVCButNotInROI = executeQuery(visibilityCell, regionOfInterest);

        final List<Integer> groundTruth = createGroundTruth();
        assertEquals(groundTruth, nodesInVCButNotInROI);
    }

    private VisibilityCell createPolygonVisibilityShape() {
        final Polygon visibilityCellShape = new Polygon(new double[] {23, 23, 14, 10, 4, 4, 8, 18}, new double[] {13, 22, 31, 31, 25, 14, 8, 8});
        return new VisibilityCell(visibilityCellShape);
    }

    private Polygon createRegionOfInterest() {
        return new Polygon(new double[]{16, 16, 12, 12}, new double[]{17, 21, 21, 17});
    }

    private List<Integer> executeQuery(VisibilityCell visibilityCell, Polygon regionOfInterest) {
        final NodesInVCButNotInROIFindingVisitor visitor = new NodesInVCButNotInROIFindingVisitor(visibilityCell, regionOfInterest, graphMocker.nodeAccess);
        graphMocker.locationIndex.query(visibilityCell.getMinimalBoundingBox(), visitor);
        final List<Integer> nodesInVCButNotInROI = visitor.getNodesInVCButNotInROI();
        Collections.sort(nodesInVCButNotInROI);
        return nodesInVCButNotInROI;
    }

    private List<Integer> createGroundTruth() {
        final Integer[] nodeIdsGroundTruth = {28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        return new ArrayList<>(Arrays.asList(nodeIdsGroundTruth));
    }

    @Test
    public void fromNodes() {
        final VisibilityCell visibilityCell = createPolygonVisibilityShape();
        final Polygon regionOfInterest = createRegionOfInterest();

        final List<Integer> nodesInVCButNotInROI = executeQuery(visibilityCell, regionOfInterest);

        final List<Integer> groundTruth = createGroundTruth();
        assertEquals(groundTruth, nodesInVCButNotInROI);
    }

    private VisibilityCell createNodeVisibilityShape() {
        final double[] vcShapeLatitudes = {22, 22, 21, 19, 14, 11, 7, 6, 5, 5, 5, 6, 7, 10, 13, 15, 19, 21};
        final double[] vcShapeLongitudes = {20, 23, 25, 30, 30, 27, 25, 22, 20, 17, 14, 11, 10, 9, 9, 10, 12};
        final Polygon visibilityCellShape = new Polygon(vcShapeLatitudes, vcShapeLongitudes);
        return new VisibilityCell(visibilityCellShape);
    }
}
