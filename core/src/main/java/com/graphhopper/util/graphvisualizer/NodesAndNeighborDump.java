package com.graphhopper.util.graphvisualizer;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;

import java.util.ArrayList;
import java.util.List;

public class NodesAndNeighborDump {
    final GraphHopperStorage graph;

    final List<Node> nodes = new ArrayList<>();
    final List<Edge> edges = new ArrayList<>();

    public NodesAndNeighborDump(GraphHopperStorage graph, List<Integer> nodes) {
        this.graph = graph;

        for (Integer node : nodes) {
            addNode(node);
        }
    }

    private void addNode(Integer node) {
        final NodeAccess nodeAccess = graph.getNodeAccess();
        final int level = Node.getLevel(node, graph);
        final Node newNode = new Node(node, nodeAccess.getLat(node), nodeAccess.getLon(node), level);

        if (!nodes.contains(newNode)) {
            this.nodes.add(newNode);
        }
    }

    public void dump() {
        edges.clear();

        for (Node node : nodes) {
            final EdgeIterator neighbors = graph.createEdgeExplorer().setBaseNode(node.id);

            System.out.print("Neighbors of " + node + ": ");
            while (neighbors.next()) {
                final int edgeId = neighbors.getEdge();
                final int baseNode = neighbors.getBaseNode();
                final int adjNode = neighbors.getAdjNode();
                edges.add(new Edge(edgeId, baseNode, adjNode));
                System.out.print(adjNode + ", ");
            }
            System.out.println();
        }

        for (Edge edge : edges) {
            final int baseNode = edge.baseNode;
            final int adjNode = edge.adjNode;
            addNode(baseNode);
            addNode(adjNode);
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}
