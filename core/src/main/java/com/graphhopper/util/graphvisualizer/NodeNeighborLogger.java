package com.graphhopper.util.graphvisualizer;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Receives nodes and add the node and the neighbors to its data structure to enable to visualize the so built subgraph
 */
public class NodeNeighborLogger {
    private final EdgeExplorer edgeExplorer;
    private final NodeAccess nodeAccess;

    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    private SwingGraphGUI gui;

    public NodeNeighborLogger(Graph superGraph) {
        this.edgeExplorer = superGraph.createEdgeExplorer();
        this.nodeAccess = superGraph.getNodeAccess();
    }

    public void add(final int node) {
        addNodeWithCoordinates(node);
        addNeighborsNodesAndEdges(node);
    }

    private void addNodeWithCoordinates(int node) {
        final double latitude = nodeAccess.getLatitude(node);
        final double longitude = nodeAccess.getLongitude(node);
        final Node newNode = new Node(node, latitude, longitude);
        if (!nodes.contains(newNode)) {
            nodes.add(newNode);
        }
    }

    private void addNeighborsNodesAndEdges(int node) {
        final EdgeIterator neighborIterator = edgeExplorer.setBaseNode(node);

        while (neighborIterator.next()) {
            final int adjacentNode = neighborIterator.getAdjNode();
            final int edgeId = neighborIterator.getEdge();
            final Edge newEdge = new Edge(edgeId, node, adjacentNode);

            addNodeWithCoordinates(adjacentNode);
            if (!edges.contains(newEdge)) {
                this.edges.add(newEdge);
            }
        }
    }

    public void visualize() {
        gui = new SwingGraphGUI(nodes, edges);
        gui.visualizeGraph();
    }

    public void remove(final List<Integer> deleteNodes) {
        for (Integer node : deleteNodes) {
            remove(node);
        }
    }

    public void remove(final Integer deleteNode) {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (nodes.get(i).equals(new Node(deleteNode, -1, -1))) {
                nodes.remove(i);
            }
        }

        for (int i = edges.size() - 1; i >= 0; i--) {
            final Edge edge = edges.get(i);
            if (edge.baseNode == deleteNode || edge.adjNode == deleteNode) {
                edges.remove(i);
            }
        }
    }
}
