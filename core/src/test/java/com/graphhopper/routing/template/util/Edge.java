package com.graphhopper.routing.template.util;

class Edge {
    final int baseNode;
    final int adjNode;
    final double distance;
    final boolean bothDirections;

    public Edge(int baseNode, int adjNode, double distance, boolean bothDirections) {
        this.baseNode = baseNode;
        this.adjNode = adjNode;
        this.distance = distance;
        this.bothDirections = bothDirections;
    }
}
