package com.graphhopper.routing.template.polygonRoutingUtil;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.Polygon;

import java.util.*;

public abstract class PathSkeletonGraph implements EdgeFilter, Iterable<Integer> {
    final Polygon regionOfInterest;
    final LocationIndex index;
    final NodeAccess nodeAccess;
    private List<Integer> entryNode = Collections.emptyList();
    private List<Integer> exitNode = Collections.emptyList();
    final Map<Integer, Boolean> nodeContainedHashFunction = new HashMap<>();
    final List<Integer> nodesContainedList = new ArrayList<>();

    public PathSkeletonGraph(Polygon regionOfInterest, LocationIndex index, NodeAccess nodeAccess) {
        this.regionOfInterest = regionOfInterest;
        this.index = index;
        this.nodeAccess = nodeAccess;
    }

    public void prepareForEntryExitNodes(final int entryNode, final int exitNode) {
        prepareForEntryExitNodes(Collections.singletonList(entryNode), Collections.singletonList(exitNode));
    }

    public void prepareForEntryExitNodes(final List<Integer> entryNode, final List<Integer> exitNode) {
        unacceptLastEntryExitPoints();
        replaceEntryExitPoint(entryNode, exitNode);
        addNewEntryExitPointAsAcceptable();
    }

    private void unacceptLastEntryExitPoints() {
        for (int i = 0; i < entryNode.size(); i++) {
            final int entryNode = this.entryNode.get(i);
            final int exitNode = this.exitNode.get(i);

            if (this.entryNode != null) {
                this.nodeContainedHashFunction.replace(entryNode, false);
                this.nodeContainedHashFunction.replace(exitNode, false);
            }
        }
    }

    private void replaceEntryExitPoint(List<Integer> entryNode, List<Integer> exitNode) {
        this.entryNode = entryNode;
        this.exitNode = exitNode;
    }

    private void addNewEntryExitPointAsAcceptable() {
        for (int i = 0; i < entryNode.size(); i++) {
            final int entryNode = this.entryNode.get(i);
            final int exitNode = this.exitNode.get(i);

            this.nodeContainedHashFunction.put(entryNode, true);
            this.nodeContainedHashFunction.put(exitNode, true);
        }
    }

    @Override
    public boolean accept(EdgeIteratorState edgeState) {
        final Integer baseNode = edgeState.getBaseNode();
        final Integer adjNode = edgeState.getAdjNode();
        final Boolean baseNodeInGraph = nodeContainedHashFunction.get(baseNode);
        final Boolean adjNodeInGraph = nodeContainedHashFunction.get(adjNode);

        boolean result = areBothNodesInRoutingGraph(baseNodeInGraph, adjNodeInGraph);

        return result;
    }

    private boolean areBothNodesInRoutingGraph(Boolean baseNodeInGraph, Boolean adjNodeInGraph) {
        boolean result = true;

        result &= isNodeInGraph(baseNodeInGraph);
        result &= isNodeInGraph(adjNodeInGraph);
        return result;
    }

    private boolean isNodeInGraph(Boolean node) {
        return node != null && node == true;
    }

    void addListToHashFunctionAsTrue(List<Integer> newNodesThatMeetCriterion) {
        for (Integer node : newNodesThatMeetCriterion) {
            nodeContainedHashFunction.put(node, true);
            nodesContainedList.add(node);
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        return nodesContainedList.iterator();
    }

    public boolean contains(final Integer key) {
        final Boolean rawHashResult = this.nodeContainedHashFunction.get(key);
        return rawHashResult != null && rawHashResult;
    }

    public int size() {
        return this.nodesContainedList.size();
    }
}
