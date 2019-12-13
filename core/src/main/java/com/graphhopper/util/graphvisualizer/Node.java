package com.graphhopper.util.graphvisualizer;

public class Node {
    public int id;
    public double latitude;
    public double longitude;

    public Node(int id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
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
        return id + ";" + longitude + ";" + latitude;
    }
}
