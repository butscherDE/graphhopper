/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.resources;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.MultiException;
import com.graphhopper.http.WebHelper;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.util.Constants;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.gpx.GpxFromInstructions;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.graphhopper.util.Parameters.Details.PATH_DETAILS;
import static com.graphhopper.util.Parameters.Routing.*;

/**
 * Resource to use GraphHopper in a remote client application like mobile or browser. Note: If type
 * is json it returns the points in GeoJson array format [longitude,latitude] unlike the format "lat,lon"
 * used for the request. See the full API response format in docs/web/api-doc.md
 *
 * @author Peter Karich
 */
@Path("route")
public class RouteResource {

    private static final Logger logger = LoggerFactory.getLogger(RouteResource.class);

    private final GraphHopperAPI graphHopper;
    private final Boolean hasElevation;

    @Inject
    public RouteResource(GraphHopperAPI graphHopper, @Named("hasElevation") Boolean hasElevation) {
        this.graphHopper = graphHopper;
        this.hasElevation = hasElevation;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/gpx+xml"})
    public Response doGet(
            @Context HttpServletRequest httpReq,
            @Context UriInfo uriInfo,
            @Context ContainerRequestContext rc,
            @QueryParam(WAY_POINT_MAX_DISTANCE) @DefaultValue("1") double minPathPrecision,
            @QueryParam("point") List<GHPoint> requestPoints,
            @QueryParam("polygon") List<GHPoint> polygonPoints,
            @QueryParam("polygonThrough") @DefaultValue("true") boolean polygonThrough,
            @QueryParam("type") @DefaultValue("json") String type,
            @QueryParam(INSTRUCTIONS) @DefaultValue("true") boolean instructions,
            @QueryParam(CALC_POINTS) @DefaultValue("true") boolean calcPoints,
            @QueryParam("elevation") @DefaultValue("false") boolean enableElevation,
            @QueryParam("points_encoded") @DefaultValue("true") boolean pointsEncoded,
            @QueryParam("vehicle") @DefaultValue("car") String vehicleStr,
            @QueryParam("weighting") @DefaultValue("fastest") String weighting,
            @QueryParam("algorithm") @DefaultValue("") String algoStr,
            @QueryParam("locale") @DefaultValue("en") String localeStr,
            @QueryParam(POINT_HINT) List<String> pointHints,
            @QueryParam(SNAP_PREVENTION) List<String> snapPreventions,
            @QueryParam(PATH_DETAILS) List<String> pathDetails,
            @QueryParam("heading") List<Double> favoredHeadings,
            @QueryParam("gpx.route") @DefaultValue("true") boolean withRoute /* default to false for the route part in next API version, see #437 */,
            @QueryParam("gpx.track") @DefaultValue("true") boolean withTrack,
            @QueryParam("gpx.waypoints") @DefaultValue("false") boolean withWayPoints,
            @QueryParam("gpx.trackname") @DefaultValue("GraphHopper Track") String trackName,
            @QueryParam("gpx.millis") String timeString) {

        boolean writeGPX = "gpx".equalsIgnoreCase(type);
        instructions = writeGPX || instructions;

        StopWatch sw = new StopWatch().start();

        getRequestErrorHandling(requestPoints, enableElevation, pointHints, favoredHeadings);

        Polygon polygon = Polygon.createPolygonFromGHPoints(polygonPoints);

        GHRequest request = buildRequest(requestPoints, favoredHeadings);
        initHints(request.getHints(), uriInfo.getQueryParameters());
        setRequestParams(minPathPrecision, polygon, polygonThrough, instructions, calcPoints, vehicleStr, weighting, algoStr, localeStr, pointHints, snapPreventions, pathDetails,
                         request);

        GHResponse ghResponse = graphHopper.route(request);

        // TODO: Request logging and timing should perhaps be done somewhere outside
        float took = sw.stop().getSeconds();
        String infoStr = buildInfoString(httpReq);
        String logStr = buildLogString(httpReq, requestPoints, vehicleStr, weighting, algoStr, took, infoStr);

        if (ghResponse.hasErrors()) {
            return handleErroneousResponse(ghResponse, logStr);
        } else {
            addResponserelatedLogEntry(ghResponse, logStr);

            Response response;

            if (writeGPX) {
                response = buildGpxResponse(enableElevation, withRoute, withTrack, withWayPoints, trackName, timeString, ghResponse, took);
            } else {
                response = buildOkResponse(instructions, calcPoints, enableElevation, pointsEncoded, ghResponse, took);
            }

            return response;
        }
    }

    private Response buildOkResponse(@DefaultValue("true") @QueryParam(INSTRUCTIONS) boolean instructions, @DefaultValue("true") @QueryParam(CALC_POINTS) boolean calcPoints,
                                     @DefaultValue("false") @QueryParam("elevation") boolean enableElevation,
                                     @DefaultValue("true") @QueryParam("points_encoded") boolean pointsEncoded, GHResponse ghResponse, float took) {
        return Response.ok(WebHelper.jsonObject(ghResponse, instructions, calcPoints, enableElevation, pointsEncoded, took)).
                header("X-GH-Took", "" + Math.round(took * 1000)).
                build();
    }

    private Response buildGpxResponse(@DefaultValue("false") @QueryParam("elevation") boolean enableElevation, @DefaultValue("true") @QueryParam("gpx.route") boolean withRoute,
                                      @DefaultValue("true") @QueryParam("gpx.track") boolean withTrack, @DefaultValue("false") @QueryParam("gpx.waypoints") boolean withWayPoints,
                                      @DefaultValue("GraphHopper Track") @QueryParam("gpx.trackname") String trackName, @QueryParam("gpx.millis") String timeString,
                                      GHResponse ghResponse, float took) {
        return gpxSuccessResponseBuilder(ghResponse, timeString, trackName, enableElevation, withRoute, withTrack, withWayPoints, Constants.VERSION).
                header("X-GH-Took", "" + Math.round(took * 1000)).
                build();
    }

    private void addResponserelatedLogEntry(GHResponse ghResponse, String logStr) {
        logger.info(logStr + ", alternatives: " + ghResponse.getAll().size()
                    + ", distance0: " + ghResponse.getBest().getDistance()
                    + ", weight0: " + ghResponse.getBest().getRouteWeight()
                    + ", time0: " + Math.round(ghResponse.getBest().getTime() / 60000f) + "min"
                    + ", points0: " + ghResponse.getBest().getPoints().getSize()
                    + ", debugInfo: " + ghResponse.getDebugInfo());
    }

    private Response handleErroneousResponse(GHResponse ghResponse, String logStr) {
        logger.error(logStr + ", errors:" + ghResponse.getErrors());
        throw new MultiException(ghResponse.getErrors());
    }

    private String buildLogString(@Context HttpServletRequest httpReq, @QueryParam("point") List<GHPoint> requestPoints,
                                  @DefaultValue("car") @QueryParam("vehicle") String vehicleStr, @DefaultValue("fastest") @QueryParam("weighting") String weighting,
                                  @DefaultValue("") @QueryParam("algorithm") String algoStr, float took, String infoStr) {
        return httpReq.getQueryString() + " " + infoStr + " " + requestPoints + ", took:"
               + took + ", " + algoStr + ", " + weighting + ", " + vehicleStr;
    }

    private String buildInfoString(@Context HttpServletRequest httpReq) {
        return httpReq.getRemoteAddr() + " " + httpReq.getLocale() + " " + httpReq.getHeader("User-Agent");
    }

    private void setRequestParams(@DefaultValue("1") @QueryParam(WAY_POINT_MAX_DISTANCE) double minPathPrecision,
                                  @QueryParam("polygon") Polygon polygon,
                                  @DefaultValue("true") @QueryParam("polygonThrough") boolean polygonThrough,
                                  @DefaultValue("true") @QueryParam(INSTRUCTIONS) boolean instructions,
                                  @DefaultValue("true") @QueryParam(CALC_POINTS) boolean calcPoints,
                                  @DefaultValue("car") @QueryParam("vehicle") String vehicleStr,
                                  @DefaultValue("fastest") @QueryParam("weighting") String weighting,
                                  @DefaultValue("") @QueryParam("algorithm") String algoStr,
                                  @DefaultValue("en") @QueryParam("locale") String localeStr,
                                  @QueryParam(POINT_HINT) List<String> pointHints,
                                  @QueryParam(SNAP_PREVENTION) List<String> snapPreventions,
                                  @QueryParam(PATH_DETAILS) List<String> pathDetails, GHRequest request) {
        request.setVehicle(vehicleStr).
                setWeighting(weighting).
                setAlgorithm(algoStr).
                setLocale(localeStr).
                setPointHints(pointHints).
                setSnapPreventions(snapPreventions).
                setPathDetails(pathDetails).
                setPolygon(polygon).
                setPolygonThrough(polygonThrough).
                getHints().
                put(CALC_POINTS, calcPoints).
                put(INSTRUCTIONS, instructions).
                put(WAY_POINT_MAX_DISTANCE, minPathPrecision);
    }

    private GHRequest buildRequest(@QueryParam("point") List<GHPoint> requestPoints, @QueryParam("heading") List<Double> favoredHeadings) {
        GHRequest request;
        if (favoredHeadings.size() > 0) {
            request = buildRequestIfFavoredHeadingsNonEmpty(requestPoints, favoredHeadings);
        } else {
            request = new GHRequest(requestPoints);
        }
        return request;
    }

    private GHRequest buildRequestIfFavoredHeadingsNonEmpty(@QueryParam("point") List<GHPoint> requestPoints, @QueryParam("heading") List<Double> favoredHeadings) {
        GHRequest request;// if only one favored heading is specified take as start heading
        if (favoredHeadings.size() == 1) {
            request = buildRequestIfFavoredHeadingsSizeIsOne(requestPoints, favoredHeadings);
        } else {
            request = new GHRequest(requestPoints, favoredHeadings);
        }
        return request;
    }

    private GHRequest buildRequestIfFavoredHeadingsSizeIsOne(@QueryParam("point") List<GHPoint> requestPoints, @QueryParam("heading") List<Double> favoredHeadings) {
        GHRequest request;
        List<Double> paddedHeadings = new ArrayList<>(Collections.nCopies(requestPoints.size(), Double.NaN));
        paddedHeadings.set(0, favoredHeadings.get(0));
        request = new GHRequest(requestPoints, paddedHeadings);
        return request;
    }

    private void getRequestErrorHandling(@QueryParam("point") List<GHPoint> requestPoints, @DefaultValue("false") @QueryParam("elevation") boolean enableElevation,
                                         @QueryParam(POINT_HINT) List<String> pointHints, @QueryParam("heading") List<Double> favoredHeadings) {
        exceptionOnNoPointsRequested(requestPoints);
        exceptionOnElevationEnabledButNotPresent(enableElevation);
        exceptionOnWrongNumberOfHeadings(requestPoints, favoredHeadings);
        exceptionOnWrongNumberOfPointHints(requestPoints, pointHints);
    }

    private void exceptionOnWrongNumberOfPointHints(@QueryParam("point") List<GHPoint> requestPoints, @QueryParam(POINT_HINT) List<String> pointHints) {
        if (pointHints.size() > 0 && pointHints.size() != requestPoints.size()) {
            throw new IllegalArgumentException("If you pass " + POINT_HINT + ", you need to pass a hint for every point, empty hints will be ignored");
        }
    }

    private void exceptionOnWrongNumberOfHeadings(@QueryParam("point") List<GHPoint> requestPoints, @QueryParam("heading") List<Double> favoredHeadings) {
        if (favoredHeadings.size() > 1 && favoredHeadings.size() != requestPoints.size()) {
            throw new IllegalArgumentException("The number of 'heading' parameters must be <= 1 "
                                               + "or equal to the number of points (" + requestPoints.size() + ")");
        }
    }

    private void exceptionOnElevationEnabledButNotPresent(@QueryParam("elevation") @DefaultValue("false") boolean enableElevation) {
        if (enableElevation && !hasElevation) {
            throw new IllegalArgumentException("Elevation not supported!");
        }
    }

    private void exceptionOnNoPointsRequested(@QueryParam("point") List<GHPoint> requestPoints) {
        if (requestPoints.isEmpty()) {
            throw new IllegalArgumentException("You have to pass at least one point");
        }
    }

    private static Response.ResponseBuilder gpxSuccessResponseBuilder(GHResponse ghRsp, String timeString, String
            trackName, boolean enableElevation, boolean withRoute, boolean withTrack, boolean withWayPoints, String version) {
        if (ghRsp.getAll().size() > 1) {
            throw new IllegalArgumentException("Alternatives are currently not yet supported for GPX");
        }

        long time = timeString != null ? Long.parseLong(timeString) : System.currentTimeMillis();
        InstructionList instructions = ghRsp.getBest().getInstructions();
        return Response.ok(GpxFromInstructions.createGPX(instructions, trackName, time, enableElevation, withRoute, withTrack, withWayPoints, version, instructions.getTr()),
                           "application/gpx+xml").
                header("Content-Disposition", "attachment;filename=" + "GraphHopper.gpx");
    }

    static void initHints(HintsMap m, MultivaluedMap<String, String> parameterMap) {
        // TODO Output parameters are bad practice, especially if the return type is void
        for (Map.Entry<String, List<String>> e : parameterMap.entrySet()) {
            if (e.getValue().size() == 1) {
                m.put(e.getKey(), e.getValue().get(0));
            } else {
                // Do nothing.
                // TODO: this is dangerous: I can only silently swallow
                // the forbidden multiparameter. If I comment-in the line below,
                // I get an exception, because "point" regularly occurs
                // multiple times.
                // I think either unknown parameters (hints) should be allowed
                // to be multiparameters, too, or we shouldn't use them for
                // known parameters either, _or_ known parameters
                // must be filtered before they come to this code point,
                // _or_ we stop passing unknown parameters alltogether..
                //
                // throw new WebApplicationException(String.format("This query parameter (hint) is not allowed to occur multiple times: %s", e.getKey()));
            }
        }
    }

}
