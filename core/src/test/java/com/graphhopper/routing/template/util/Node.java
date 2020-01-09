package com.graphhopper.routing.template.util;

import com.graphhopper.routing.AbstractRoutingAlgorithmTester;
import com.graphhopper.storage.Graph;

class Node {
    final int id;
    final double latitude;
    final double longitude;

    Node(final int id, final double latitude, final double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    void updateDistance(final Graph graph) {
        AbstractRoutingAlgorithmTester.updateDistancesFor(graph, this.id, this.latitude, this.longitude);
    }
}
