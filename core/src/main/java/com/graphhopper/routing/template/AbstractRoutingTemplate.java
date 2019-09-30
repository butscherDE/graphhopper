package com.graphhopper.routing.template;

import com.graphhopper.GHRequest;
import com.graphhopper.routing.Path;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.PointList;

import java.util.List;

/**
 * @author Peter Karich
 */
class AbstractRoutingTemplate {
    // result from lookup
    List<QueryResult> queryResults;

    PointList getWaypoints() {
        PointList pointList = new PointList(queryResults.size(), true);
        for (QueryResult qr : queryResults) {
            pointList.add(qr.getSnappedPoint());
        }
        return pointList;
    }

    void failOnNumPathsInvalid(final GHRequest ghrequest, final List<Path> paths) {
        if (ghrequest.getPoints().size() - 1 != paths.size())
            throw new RuntimeException("There should be exactly one more points than paths. points:" + ghrequest.getPoints().size() + ", paths:" + paths.size());
    }
}
