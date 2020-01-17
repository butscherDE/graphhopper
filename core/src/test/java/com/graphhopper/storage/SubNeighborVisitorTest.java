package com.graphhopper.storage;

public class SubNeighborVisitorTest {
//    @Rule
//    public ExpectedException exceptionRule = ExpectedException.none();
//
//    private final static PolygonRoutingTestGraph GRAPH_MOCKER = new PolygonRoutingTestGraph(PolygonRoutingTestGraph.getDefaultNodeList(), PolygonRoutingTestGraph.getDefaultEdgeList());
//
//    @Test
//    public void getLastOnEmptyVisitor() {
//        final SubNeighborVisitor subNeighborVisitor = new SubNeighborVisitor(GRAPH_MOCKER.getAllEdges().get(0));
//
//        exceptionRule.expect(NoSuchElementException.class);
//        subNeighborVisitor.getLast();
//    }
//
//    @Test
//    public void repeatedlyFindingLastAddedElement() {
//        final SubNeighborVisitor subNeighborVisitor = new SubNeighborVisitor(GRAPH_MOCKER.getAllEdges().get(0));
//        final EdgeIterator allEdgesIterator = GRAPH_MOCKER.graph.getAllEdges();
//
//        while (allEdgesIterator.next()) {
//            subNeighborVisitor.onEdge(allEdgesIterator.detach(false));
//
//            final int expectedEdge = allEdgesIterator.detach(false).getEdge();
//            final int actualEdge = subNeighborVisitor.getLast().getEdge();
//            assertEquals(expectedEdge, actualEdge);
//        }
//    }
//
//    private SubNeighborVisitor createSubNeighborVisitorContainingAllTestEdges() {
//        final SubNeighborVisitor subNeighborVisitor = new SubNeighborVisitor(GRAPH_MOCKER.getAllEdges().get(0));
//
//        final EdgeIterator allEdgesIteratorToFillSubNeighborVisitor = GRAPH_MOCKER.graph.getAllEdges();
//        while (allEdgesIteratorToFillSubNeighborVisitor.next()) {
//            subNeighborVisitor.onEdge(allEdgesIteratorToFillSubNeighborVisitor.detach(false));
//        }
//        return subNeighborVisitor;
//    }
//
//    @Test
//    public void testIterator() {
//        final SubNeighborVisitor subNeighborVisitor = createSubNeighborVisitorContainingAllTestEdges();
//
//        final EdgeIterator allEdgesIteratorExpected = GRAPH_MOCKER.graph.getAllEdges();
//        for (EdgeIteratorState edgeIteratorState : subNeighborVisitor) {
//            allEdgesIteratorExpected.next();
//            final int expectedEdge = allEdgesIteratorExpected.detach(false).getEdge();
//            final int actualEdge = edgeIteratorState.getEdge();
//
//            assertEquals(expectedEdge, actualEdge);
//        }
//    }
//
//    @Test
//    public void createFromEdgeList() {
//        final SubNeighborVisitor subNeighborVisitor = createSubNeighborVisitorContainingAllTestEdges();
//
//        final Iterator<EdgeIteratorState> actualEdgeIterator = createIteratorForAllEdges(subNeighborVisitor);
//
//        for (EdgeIteratorState expectedEdgeState : subNeighborVisitor) {
//            final int expectedEdge = expectedEdgeState.getEdge();
//            final int actualEdge = actualEdgeIterator.next().getEdge();
//
//            assertEquals(expectedEdge, actualEdge);
//        }
//
//        assertFalse(actualEdgeIterator.hasNext());
//    }
//
//    private Iterator<EdgeIteratorState> createIteratorForAllEdges(SubNeighborVisitor subNeighborVisitor) {
//        final List<EdgeIteratorState> allEdges = new ArrayList<>(GRAPH_MOCKER.graph.getEdges());
//        for (EdgeIteratorState edgeIteratorState : subNeighborVisitor) {
//            allEdges.add(edgeIteratorState);
//        }
//
//        final SubNeighborVisitor fromEdgesCreatedVisitor = new SubNeighborVisitor(allEdges, GRAPH_MOCKER.getAllEdges().get(0));
//        return fromEdgesCreatedVisitor.iterator();
//    }
//
//    @Test
//    public void testCloneAllDataPresent() {
//        final SubNeighborVisitor subNeighborVisitor = createSubNeighborVisitorContainingAllTestEdges();
//        final SubNeighborVisitor clonedSubNeighborVisitor = subNeighborVisitor.clone();
//
//        final Iterator<EdgeIteratorState> clonedSubneighborsIterator = clonedSubNeighborVisitor.iterator();
//
//        for (EdgeIteratorState expectedEdgeIteratorState : subNeighborVisitor) {
//            final int expectedEdge = expectedEdgeIteratorState.getEdge();
//            final int actualEdge = clonedSubneighborsIterator.next().getEdge();
//
//            assertEquals(expectedEdge, actualEdge);
//        }
//    }
//
//    @Test
//    public void testCloneChangesNotReflected() {
//        final SubNeighborVisitor subNeighborVisitor = createSubNeighborVisitorContainingAllTestEdges();
//        final SubNeighborVisitor clonedSubNeighborVisitor = subNeighborVisitor.clone();
//        clonedSubNeighborVisitor.onEdge(clonedSubNeighborVisitor.iterator().next());
//
//        assertNotEquals(subNeighborVisitor.getLast(), clonedSubNeighborVisitor.getLast());
//    }

}
