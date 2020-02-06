package com.graphhopper.routing;

import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.profiles.EnumEncodedValue;
import com.graphhopper.routing.profiles.IntEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

import java.util.*;

public class RPHAST {
    private final CHGraph chGraph;
    private final Weighting weighting;
    private final EdgeFilter edgeFilter;

    private Set<Integer> targetSet;
    private List<EdgeIteratorState> upwardsGraphEdges;
    private List<EdgeIteratorState> restrictedDownwardsGraphEdges = null;

    private Map<Integer, Double> cost = new HashMap<>();
    private Map<Integer, EdgeIteratorState> predecessors = new HashMap<>();

    public RPHAST(final GraphHopperStorage graph, final Weighting weighting, final EdgeFilter edgeFilter) {
        this.chGraph = graph.getCHGraph();
        this.weighting = weighting;
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
        testIfCHCreationWorked();
        printAllCHGraphEdges();
        if (restrictedDownwardsGraphEdges == null) {
            throw new IllegalStateException("Call prepareForTagetSet first");
        }

        final List<Path> paths = new ArrayList<>(sourceNodes.size() * targetSet.size());
        for (final int sourceNode : sourceNodes) {
            getMarkedSourceEdges(Collections.singletonList(sourceNode));

            cost.put(sourceNode, 0.0);
            predecessors.put(sourceNode, new NonExistentEdge(sourceNode));

            exploreGraph(upwardsGraphEdges);
            exploreGraph(restrictedDownwardsGraphEdges);

            paths.addAll(backtrackPathForEachTarget());

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

    private void exploreGraph(List<EdgeIteratorState> edgeList) {
        for (EdgeIteratorState currentEdge : edgeList) {
            int baseNode = currentEdge.getBaseNode();
            int adjNode = currentEdge.getAdjNode();

            if (cost.get(adjNode) == null) {
                cost.put(adjNode, Double.MAX_VALUE);
            }

            final double currentCostOfAdjNode = cost.get(adjNode);
            int previousEdgeId = predecessors.get(baseNode).getEdge();
            double costWithCurrentEdge = weighting.calcWeight(currentEdge, false, previousEdgeId) + cost.get(baseNode);
            if (costWithCurrentEdge < currentCostOfAdjNode) {
                cost.put(adjNode, costWithCurrentEdge);
                predecessors.put(adjNode, currentEdge);
            }
        }
    }

    private List<Path> backtrackPathForEachTarget() {
        final List<Path> paths = new ArrayList<>(targetSet.size());
        for (Integer target : targetSet) {
            paths.add(backtrackPath(target));
        }
        return paths;
    }

    private Path backtrackPath(final int node) {
        int currentNode = node;
        final LinkedList<EdgeIteratorState> backtrackedEdges = new LinkedList<>();

        EdgeIteratorState currentEdge = predecessors.get(currentNode);
        while (currentEdge.getEdge() != EdgeIterator.NO_EDGE) {
            backtrackedEdges.addFirst(currentEdge);
            currentNode = currentEdge.getBaseNode();
            currentEdge = predecessors.get(currentNode);
        }

        return new PathSimpleized(chGraph, weighting, backtrackedEdges, cost.get(node));
    }


    // TODO Delete the following methods
    public void testIfCHCreationWorked() {
        final List<Integer> allNodes = getAllNodes(chGraph);
        Collections.sort(allNodes, Comparator.comparingInt(chGraph::getLevel));

        printAllNodesWithRankWellAligned(chGraph, allNodes);
    }

    private List<Integer> getAllNodes(Graph chGraph) {
        final EdgeIterator allEdges = chGraph.getAllEdges();
        final Set<Integer> allNodes = new LinkedHashSet<>();
        while(allEdges.next()) {
            allNodes.add(allEdges.getBaseNode());
            allNodes.add(allEdges.getAdjNode());
        }
        final List<Integer> nodesAsList = new ArrayList<>(allNodes);
        Collections.sort(nodesAsList);
        return nodesAsList;
    }

    private void printAllNodesWithRankWellAligned(CHGraph chGraph, List<Integer> allNodes) {
        for (Integer node : allNodes) {
            int log = (int) Math.log10(node) + 1;
            final int nodeDigits = log >= 0 ? log : 1;

            System.out.print(node + ":");
            for (int i = nodeDigits; i < 5; i++) {
                System.out.print(" ");
            }
            System.out.println(chGraph.getLevel(node));
        }
    }

    public void printAllCHGraphEdges() {
        final EdgeIterator allEdges = chGraph.getAllEdges();

        while(allEdges.next()) {
            System.out.println(allEdges.toString());
        }
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
}
