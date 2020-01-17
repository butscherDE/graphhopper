package com.graphhopper.routing.template.polygonRoutingUtil;

import java.util.Objects;

public class NodeIdPair {
    final int a;
    final int b;

    public NodeIdPair(int a, int b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeIdPair that = (NodeIdPair) o;
        return a == that.a &&
               b == that.b;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }
}
