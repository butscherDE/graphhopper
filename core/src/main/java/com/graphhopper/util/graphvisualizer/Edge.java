package com.graphhopper.util.graphvisualizer;

public class Edge {
    public int id;
    public int baseNode;
    public int adjNode;

    public Edge(int id, int baseNode, int adjNode) {
        this.id = id;
        this.baseNode = baseNode;
        this.adjNode = adjNode;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Edge) {
            return this.id == ((Edge) other).id;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return id + ";" + baseNode + ";" + adjNode;
    }
}
