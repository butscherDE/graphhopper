package com.graphhopper.routing.template.util;

import com.graphhopper.routing.QueryGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.DistanceCalc2D;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.ArrayList;
import java.util.List;

public class QueryGraphCreator {
    private final Graph graph;
    private final List<Integer> nodesToLookup;

    public QueryGraphCreator(final Graph graph, List<Integer> nodesToLookup) {
        this.graph = graph;
        this.nodesToLookup = nodesToLookup;
    }

    public QueryGraph createQueryGraph() {
        final List<QueryResult> queryResults = this.getQueryResults();
        final QueryGraph queryGraph = new QueryGraph(this.graph);

        queryGraph.lookup(queryResults);

        return queryGraph;
    }

    public List<QueryResult> getQueryResults() {
        final List<QueryResult> queryResults = new ArrayList<>(this.nodesToLookup.size());

        for (final int node : this.nodesToLookup) {
            final double latitude = this.graph.getNodeAccess().getLatitude(node);
            final double longitude = this.graph.getNodeAccess().getLongitude(node);

            QueryResult queryResult = createQueryReult(node, latitude, longitude);
            queryResults.add(queryResult);
        }
        return queryResults;
    }

    public QueryResult createQueryReult(int node, double latitude, double longitude) {
        QueryResult queryResult = new QueryResult(latitude, longitude);
        queryResult.setClosestNode(node);
        queryResult.setWayIndex(0);
        queryResult.setClosestEdge(findClosestEdge(node));
        queryResult.calcSnappedPoint(new DistanceCalc2D());
        return queryResult;
    }

    public EdgeIteratorState findClosestEdge(final int baseNode) {
        EdgeExplorer edgeExplorer = this.graph.createEdgeExplorer();
        EdgeIterator edgeIterator = edgeExplorer.setBaseNode(baseNode);
        edgeIterator.next();
        return edgeIterator;
    }
}
