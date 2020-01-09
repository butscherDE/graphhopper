package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.RoutingAlgorithmFactorySimple;
import com.graphhopper.routing.template.util.PolygonRoutingTestGraph;
import com.graphhopper.storage.Graph;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LOTNodeExtractorTest {
    private final PolygonRoutingTestGraph graphMocker = new PolygonRoutingTestGraph(PolygonRoutingTestGraph.getDefaultNodeList(), PolygonRoutingTestGraph.getDefaultEdgeList());
    private LOTNodeExtractor extractor;

    @Before
    public void createDefaultTestCase() {
        final Graph graph = graphMocker.graph;
        final RoutingAlgorithmFactory routingAlgorithmFactory = new RoutingAlgorithmFactorySimple();
        final AlgorithmOptions algorithmOptions = this.graphMocker.algorithmOptions;
        List<Integer> viaPoints = createViaPoints();
        List<Integer> entryExitPoints = createEntryExitPoints();

        this.extractor =  LOTNodeExtractor.createExtractedData(graph, routingAlgorithmFactory, algorithmOptions, viaPoints, entryExitPoints);
    }

    private List<Integer> createViaPoints() {
        final List<Integer> viaPoints = new ArrayList<Integer>();
        viaPoints.add(0);
        viaPoints.add(2);
        return viaPoints;
    }

    private List<Integer> createEntryExitPoints() {
        final List<Integer> entryExitPoints = new ArrayList<>();
        entryExitPoints.add(28);
        entryExitPoints.add(29);
        entryExitPoints.add(30);
        entryExitPoints.add(31);

        entryExitPoints.add(43);
        entryExitPoints.add(44);
        entryExitPoints.add(45);
        return entryExitPoints;
    }

    @Test
    public void assertCorrectSize() {
        assertEquals(2, this.extractor.size());
    }

    @Test
    public void correctLotNodesForViaPoint0() {
        final List<Integer> lotNodesForViaPoint0 = createLotNodesForViaPoint0();

        assertEquals(lotNodesForViaPoint0, this.extractor.getLotNodesFor(0));
    }

    private List<Integer> createLotNodesForViaPoint0() {
        final List<Integer> lotNodesForViaPoint0 = new ArrayList<>();
        lotNodesForViaPoint0.add(28);
        lotNodesForViaPoint0.add(44);
        return lotNodesForViaPoint0;
    }

    @Test
    public void correctLotNodesForViaPoint2() {
        final List<Integer> lotNodesForViaPoint2 = createLotNodesForViaPoint2();

        assertEquals(lotNodesForViaPoint2, this.extractor.getLotNodesFor(2));
    }

    private List<Integer> createLotNodesForViaPoint2() {
        final List<Integer> lotNodesForViaPoint2 = new ArrayList<>();
        lotNodesForViaPoint2.add(28);
        lotNodesForViaPoint2.add(45);
        return lotNodesForViaPoint2;
    }

    @Test
    public void correctPathsViaPoint0() {
        final List<Integer> path0To28 = new ArrayList<>(Arrays.asList(new Integer[] {0, 1, 28}));
        final List<Integer> path0To44 = new ArrayList<>(Arrays.asList(new Integer[] {0, 7, 44}));

        assertEquals(path0To28, this.extractor.getLotNodePathFor(0, 28).getNodesInPathOrder());
        assertEquals(path0To44, this.extractor.getLotNodePathFor(0, 44).getNodesInPathOrder());
    }

    @Test
    public void correctPathsViaPoint2() {
        final List<Integer> path2To28 = new ArrayList<>(Arrays.asList(new Integer[] {2, 28}));
        final List<Integer> path2To44 = new ArrayList<>(Arrays.asList(new Integer[] {2, 1, 45}));

        assertEquals(path2To28, this.extractor.getLotNodePathFor(2, 28).getNodesInPathOrder());
        assertEquals(path2To44, this.extractor.getLotNodePathFor(2, 45).getNodesInPathOrder());
    }
}
