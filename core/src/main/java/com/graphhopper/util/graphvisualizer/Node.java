package com.graphhopper.util.graphvisualizer;

import com.graphhopper.storage.GraphHopperStorage;

public class Node {
    public int id;
    public double latitude;
    public double longitude;
    public int level;

    public Node(int id, double latitude, double longitude, int level) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.level = level;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Node) {
            return this.id == ((Node) other).id;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return id + ";" + longitude + ";" + latitude + ";" + level;
    }

    static int getLevel(int nodeId, final GraphHopperStorage graph) {
        int level;
        try {
            level = graph.getCHGraph().getLevel(nodeId);
        } catch (Exception e) {
            level = -1;
        }
        return level;
    }
}
