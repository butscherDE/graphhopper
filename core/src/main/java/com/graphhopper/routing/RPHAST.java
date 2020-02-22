package com.graphhopper.routing;

import com.graphhopper.routing.ch.PreparationWeighting;
import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.profiles.EnumEncodedValue;
import com.graphhopper.routing.profiles.IntEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.storage.ShortcutUnpacker;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

import java.util.*;

public class RPHAST {
    private final CHGraph chGraph;
    private final Weighting weighting;
    private final Weighting chWeighting;
    private final EdgeFilter edgeFilter;

    private Set<Integer> targetSet;
    private List<EdgeIteratorState> upwardsGraphEdges;
    private List<EdgeIteratorState> restrictedDownwardsGraphEdges = null;

    private Map<Integer, Double> cost = new HashMap<>();
    private Map<Integer, EdgeIteratorState> predecessors = new HashMap<>();

    public RPHAST(final GraphHopperStorage graph, final Weighting weighting, final EdgeFilter edgeFilter) {
        this.chGraph = graph.getCHGraph();
        this.weighting = weighting;
        this.chWeighting = new PreparationWeighting(weighting);
        this.edgeFilter = edgeFilter;
    }

    public void prepareForTargetSet(final Set<Integer> targetSet) {
        this.targetSet = targetSet;

        getMarkedTargetEdges();
    }

    private void getMarkedTargetEdges() {
        TargetSetReverseUpwardPathsExplorer targetExplorer = new TargetSetReverseUpwardPathsExplorer(chGraph, targetSet, edgeFilter);
        restrictedDownwardsGraphEdges = targetExplorer.getMarkedEdges();
        sortDescending(restrictedDownwardsGraphEdges);
    }

    private void sortDescending(List<EdgeIteratorState> markedEdges) {
        Collections.sort(markedEdges, (edge1, edge2) -> {
            final int edge1AdjNode = edge1.getAdjNode();
            final int edge2AdjNode = edge2.getAdjNode();

            final int edge1AdjNodeRank = chGraph.getLevel(edge1AdjNode);
            final int edge2AdjNodeRank = chGraph.getLevel(edge2AdjNode);

            return Integer.compare(edge1AdjNodeRank, edge2AdjNodeRank) * -1;
        });
    }

    public List<Path> calcPaths(final int sourceNode) {
        return calcPaths(Collections.singletonList(sourceNode));
    }

    public List<Path> calcPaths(final List<Integer> sourceNodes) {
//        testIfCHCreationWorked();
//        printAllCHGraphEdges();
        if (restrictedDownwardsGraphEdges == null) {
            throw new IllegalStateException("Call prepareForTagetSet first");
        }

        final List<Path> paths = new ArrayList<>(sourceNodes.size() * targetSet.size());
        for (final int sourceNode : sourceNodes) {
            getMarkedSourceEdges(Collections.singletonList(sourceNode));

            cost.put(sourceNode, 0.0);
            predecessors.put(sourceNode, new NonExistentEdge(sourceNode));

            findPathsForThisSource(sourceNode, paths);


            // TODO we do not need to throw away all data.
            cost.clear();
            predecessors.clear();
        }

        return paths;
    }

    private void getMarkedSourceEdges(final List<Integer> sourceNodes) {
        SourceSetUpwardPathsExplorer sourceExplorer = new SourceSetUpwardPathsExplorer(chGraph, new LinkedHashSet<>(sourceNodes), edgeFilter);
        upwardsGraphEdges = sourceExplorer.getMarkedEdges();
        sortNonDescending(upwardsGraphEdges);
    }

    private void sortNonDescending(List<EdgeIteratorState> markedEdges) {
        Collections.sort(markedEdges, (edge1, edge2) -> {
            final int edge1AdjNode = edge1.getAdjNode();
            final int edge2AdjNode = edge2.getAdjNode();

            final int edge1AdjNodeRank = chGraph.getLevel(edge1AdjNode);
            final int edge2AdjNodeRank = chGraph.getLevel(edge2AdjNode);

            return Integer.compare(edge1AdjNodeRank, edge2AdjNodeRank);
        });
    }

    private void findPathsForThisSource(final int source, List<Path> paths) {
        try {
            exploreUpThenDownGraph(source, paths);
        } catch (NullPointerException sourceDoesntExistException) {
            addInvalidPaths(source, paths);
        }
    }

    private void exploreUpThenDownGraph(final int source, List<Path> paths) {
        exploreGraph(upwardsGraphEdges);
        exploreGraph(restrictedDownwardsGraphEdges);

        paths.addAll(backtrackPathForEachTarget(source));
    }

    private void addInvalidPaths(final int source, List<Path> paths) {
        for (Integer target : targetSet) {
            paths.add(getInvalidPath(source, target));
        }
    }

    private void exploreGraph(List<EdgeIteratorState> edgeList) {
        for (EdgeIteratorState currentEdge : edgeList) {
            int baseNode = currentEdge.getBaseNode();
            int adjNode = currentEdge.getAdjNode();

            if (cost.get(adjNode) == null) {
                cost.put(adjNode, Double.MAX_VALUE);
            }

            final double currentCostOfAdjNode = cost.get(adjNode);
            int previousEdgeId = predecessors.get(baseNode).getEdge();
            double costWithCurrentEdge = calcWeight(currentEdge, baseNode, previousEdgeId);
            if (costWithCurrentEdge < currentCostOfAdjNode) {
                cost.put(adjNode, costWithCurrentEdge);
                predecessors.put(adjNode, currentEdge);
            }
        }
    }

    private double calcWeight(EdgeIteratorState currentEdge, int baseNode, int previousEdgeId) {
        final double edgeCost;
        if (currentEdge instanceof CHEdgeIteratorState) {
            edgeCost = chWeighting.calcWeight(currentEdge, false, previousEdgeId);
        } else {
            edgeCost = weighting.calcWeight(currentEdge, false, previousEdgeId);
        }

        return edgeCost + cost.get(baseNode);
    }

    private List<Path> backtrackPathForEachTarget(final int source) {
        final List<Path> paths = new ArrayList<>(targetSet.size());
        for (Integer target : targetSet) {
            paths.add(backtrackPath(source, target));
        }
        return paths;
    }

    private Path backtrackPath(final int source, final int target) {
        try {
            return getBacktrackedPath(source, target);
        } catch (NullPointerException noPathFoundException) {
            return getInvalidPath(source, target);
        }
    }

    private Path getInvalidPath(final int source, final int target) {
        return PathSimpled.create(chGraph, weighting, Collections.emptyList(), source, target, Double.MAX_VALUE, false);
    }

    private Path getBacktrackedPath(int source, int target) {
        int currentNode = target;
        final List<EdgeIteratorState> backtrackedEdgesWithShortcuts = getBacktrackedEdgesWithShortcuts(currentNode);
        final List<EdgeIteratorState> backtrackedEdges = getBaseEdgesFromShortcuttingPath(backtrackedEdgesWithShortcuts);

        return PathSimpled.create(chGraph, weighting, backtrackedEdges, source, target, cost.get(target), true);
    }

    private LinkedList<EdgeIteratorState> getBacktrackedEdgesWithShortcuts(int currentNode) {
        final LinkedList<EdgeIteratorState> backtrackedEdges = new LinkedList<>();

        EdgeIteratorState currentEdge = predecessors.get(currentNode);
        while (currentEdge.getEdge() != EdgeIterator.NO_EDGE) {
            backtrackedEdges.addFirst(currentEdge);
            currentNode = currentEdge.getBaseNode();
            currentEdge = predecessors.get(currentNode);
        }
        return backtrackedEdges;
    }

    public List<EdgeIteratorState> getBaseEdgesFromShortcuttingPath(final List<EdgeIteratorState> edges) {
        final EdgeRecordingVisitor edgeRecordingVisitor = new EdgeRecordingVisitor();
        final ShortcutUnpacker shortcutUnpacker = new ShortcutUnpacker(chGraph, edgeRecordingVisitor, false);

        int lastEdge = EdgeIterator.NO_EDGE;
        for (EdgeIteratorState edge : edges) {
            shortcutUnpacker.visitOriginalEdgesFwd(edge.getEdge(), edge.getAdjNode(), false, lastEdge);
            edgeRecordingVisitor.getLastEdgeId();
        }

        return edgeRecordingVisitor.getEdges();
    }

    static class NonExistentEdge implements EdgeIteratorState {
        private final int adjNode;

        NonExistentEdge(int adjNode) {
            this.adjNode = adjNode;
        }

        @Override
        public int getEdge() {
            return EdgeIterator.NO_EDGE;
        }

        @Override
        public int getOrigEdgeFirst() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getOrigEdgeLast() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getBaseNode() {
            return -1;
        }

        @Override
        public int getAdjNode() {
            return adjNode;
        }

        @Override
        public PointList fetchWayGeometry(int mode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeIteratorState setWayGeometry(PointList list) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getDistance() {
            return 0;
        }

        @Override
        public EdgeIteratorState setDistance(double dist) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IntsRef getFlags() {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeIteratorState setFlags(IntsRef edgeFlags) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getAdditionalField() {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeIteratorState setAdditionalField(int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean get(BooleanEncodedValue property) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeIteratorState set(BooleanEncodedValue property, boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean getReverse(BooleanEncodedValue property) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeIteratorState setReverse(BooleanEncodedValue property, boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int get(IntEncodedValue property) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeIteratorState set(IntEncodedValue property, int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getReverse(IntEncodedValue property) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeIteratorState setReverse(IntEncodedValue property, int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double get(DecimalEncodedValue property) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeIteratorState set(DecimalEncodedValue property, double value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getReverse(DecimalEncodedValue property) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeIteratorState setReverse(DecimalEncodedValue property, double value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends Enum> T get(EnumEncodedValue<T> property) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends Enum> EdgeIteratorState set(EnumEncodedValue<T> property, T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends Enum> T getReverse(EnumEncodedValue<T> property) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends Enum> EdgeIteratorState setReverse(EnumEncodedValue<T> property, T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeIteratorState setName(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeIteratorState detach(boolean reverse) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeIteratorState copyPropertiesFrom(EdgeIteratorState e) {
            throw new UnsupportedOperationException();
        }
    }

    private class EdgeRecordingVisitor implements ShortcutUnpacker.Visitor {
        final LinkedList<EdgeIteratorState> edges = new LinkedList<>();

        @Override
        public void visit(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
            edges.add(edge);
        }

        public List<EdgeIteratorState> getEdges() {
            return edges;
        }

        public int getLastEdgeId() {
            return edges.getLast().getEdge();
        }
    }
}
