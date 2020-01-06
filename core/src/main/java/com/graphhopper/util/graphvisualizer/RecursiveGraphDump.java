package com.graphhopper.util.graphvisualizer;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

public class RecursiveGraphDump {
    final Graph graph;
    final NodeAccess nodeAccess;

    public final List<Node> nodes = new ArrayList<>();
    public final List<Edge> edges = new ArrayList<>();

    private EdgeIteratorState edge;
    private SwingGraphGUI gui;

    public RecursiveGraphDump(Graph graph) {
        this.graph = graph;
        this.nodeAccess = this.graph.getNodeAccess();
    }

    public void dumpGraphRecursiveFromEdge(final EdgeIterator edge, final int maxDepth) {
        this.edge = edge;

        nodes.clear();
        edges.clear();

        dumpGraphRecursiveFromNode(edge.getBaseNode(), maxDepth);
        dumpGraphRecursiveFromNode(edge.getAdjNode(), maxDepth);
    }

    private void dumpGraphRecursiveFromNode(final int nodeId, final int maxDepth) {
        if (maxDepth < 1) {
            return;
        }

        final EdgeExplorer edgeExplorer = graph.createEdgeExplorer();
        final EdgeIterator neighborIterator = edgeExplorer.setBaseNode(nodeId);

        final List<Integer> neighbors = new LinkedList<>();

        while (neighborIterator.next()) {
            addNodeIfNotExistent(neighborIterator);
            addEdgeIfNotExistent(neighborIterator);

            neighbors.add(neighborIterator.getAdjNode());

            dumpGraphRecursiveFromNode(neighborIterator.getAdjNode(), maxDepth - 1);
        }

        System.out.println("Neighbors of " + nodeId + ": " + neighbors.toString());

        gui = new SwingGraphGUI(nodes, edges);
    }

    private void addNodeIfNotExistent(EdgeIterator neighborIterator) {
        final int nodeId = neighborIterator.getAdjNode();
        final double latitude = nodeAccess.getLat(nodeId);
        final double longitude = nodeAccess.getLon(nodeId);
        final Node newNode = new Node(nodeId, latitude, longitude);

        if (!nodes.contains(newNode)) {
            nodes.add(newNode);
        }
    }

    private void addEdgeIfNotExistent(EdgeIterator neighborIterator) {
        final int edgeId = neighborIterator.getEdge();
        final int baseNode = neighborIterator.getBaseNode();
        final int adjNode = neighborIterator.getAdjNode();
        final Edge newEdge = new Edge(edgeId, baseNode, adjNode);

        if (!edges.contains(newEdge)) {
            edges.add(newEdge);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Node node : nodes) {
            sb.append(node + "\n");
        }

        for (Edge edge : edges) {
            sb.append(edge + "\n");
        }

        return sb.toString();
    }

    public void newHighlightEdge(final EdgeIterator edgeState) {
        final Edge edge = new Edge(edgeState.getEdge(), edgeState.getBaseNode(), edgeState.getAdjNode());
        this.gui.addEdgeToHighlight(edge);
    }

    public void visualize() {
        if (nodes.size() > 0) {
            gui.visualizeGraph();
        }
    }

    public void removeAllNodesAndCorrespondingEdges(final List<Integer> nodes) {
        for (Integer node : nodes) {
            removeNodeAndCorrespondingEdges(node);
        }
    }

    public void removeNodeAndCorrespondingEdges(final int node) {
        this.nodes.remove(new Node(node, -1, -1));
        for (int i = this.edges.size() - 1; i >= 0; i--) {
            final Edge edge = this.edges.get(i);
            if (edge.baseNode == node || edge.adjNode == node) {
                this.edges.remove(i);
            }
        }
    }

}
