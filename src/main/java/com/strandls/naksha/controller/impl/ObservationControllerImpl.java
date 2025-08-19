package com.strandls.naksha.controller.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.strandls.esmodule.controllers.GeoServiceApi;
import com.strandls.esmodule.pojo.GeoAggregationData;
import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.controller.ObservationController;
import com.strandls.naksha.service.GeoserverService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Tag(name = "Observation Service")
@Path(ApiConstants.OBSERVATION)
public class ObservationControllerImpl implements ObservationController {

	@Inject
	private GeoServiceApi geoServiceApi;
	@Inject
	private GeoserverService geoserverService;

	@Inject
	public ObservationControllerImpl() {
		// Default constructor
	}

	@Override
	@GET
	@Path("map")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_HTML)
	@Operation(summary = "Fetch WMS HTML map for observations", description = "Returns an HTML OpenLayers WMS map for the given BBOX. Returns 'text/html' content, typically for display in a browser widget.", responses = {
			@ApiResponse(responseCode = "200", description = "HTML OpenLayers map", content = @Content(mediaType = MediaType.TEXT_HTML, schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response fetchMap(
			@Parameter(description = "Species ID (optional)") @QueryParam("SpeciesId") String speciesId,
			@Parameter(description = "Top coordinate of bounding box") @QueryParam("top") Double top,
			@Parameter(description = "Left coordinate of bounding box") @QueryParam("left") Double left,
			@Parameter(description = "Bottom coordinate of bounding box") @QueryParam("bottom") Double bottom,
			@Parameter(description = "Right coordinate of bounding box") @QueryParam("right") Double right,
			@Parameter(description = "Width in pixels") @QueryParam("width") Double width,
			@Parameter(description = "Height in pixels") @QueryParam("height") Double height) {
		try {
			String workspace = NakshaConfig.getString("observation.geoserver.workspace");
			String layer = NakshaConfig.getString("observation.geoserver.layer");
			String uri = workspace + "/wms";

			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("service", "WMS"));
			params.add(new BasicNameValuePair("version", "1.1.0"));
			params.add(new BasicNameValuePair("request", "GetMap"));
			params.add(new BasicNameValuePair("style", ""));
			params.add(new BasicNameValuePair("srs", "EPSG:4326"));
			params.add(new BasicNameValuePair("format", "application/openlayers"));

			params.add(new BasicNameValuePair("layers", workspace + ":" + layer));
			params.add(new BasicNameValuePair("bbox", top + "," + left + "," + bottom + "," + right));
			params.add(new BasicNameValuePair("width", "400"));
			params.add(new BasicNameValuePair("height", "650"));

			if (speciesId != null)
				params.add(new BasicNameValuePair("viewparams", "q:{\"term\":{\"species_id\":\"" + speciesId + "\"}}"));
			byte[] file = geoserverService.getRequest(uri, params);
			return Response.status(Status.OK).entity(new ByteArrayInputStream(file)).build();

		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@GET
	@Path("aggregation")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Fetch ES aggregation geohash grid for observations", description = "Returns ES geohash grid aggregation for selected index/type/field as a JSON object (bucketed counts).", responses = {
			@ApiResponse(responseCode = "200", description = "Geohash buckets and counts", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GeoAggregationData.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response fetchESAggs(@Parameter(description = "Elasticsearch index") @QueryParam("index") String index,
			@Parameter(description = "Elasticsearch doc type") @QueryParam("type") String type,
			@Parameter(description = "Field used for geo index") @QueryParam("geoField") String geoField,
			@Parameter(description = "Precision (max 12 for geohash)") @QueryParam("precision") Integer precision,
			@Parameter(description = "Top coordinate of bounding box") @QueryParam("top") Double top,
			@Parameter(description = "Left coordinate of bounding box") @QueryParam("left") Double left,
			@Parameter(description = "Bottom coordinate of bounding box") @QueryParam("bottom") Double bottom,
			@Parameter(description = "Right coordinate of bounding box") @QueryParam("right") Double right,
			@Parameter(description = "Species ID (optional)") @QueryParam("speciesId") Long speciesId) {
		try {
			index = index == null ? NakshaConfig.getString("observation.es.index") : index;
			type = type == null ? NakshaConfig.getString("observation.es.type") : type;
			geoField = geoField == null ? NakshaConfig.getString("observation.es.geoField") : geoField;

			Map<String, Long> geoHashToDocCount = geoServiceApi.getGeoAggregationFromIndexType(index, type, geoField,
					precision, top, left, bottom, right, speciesId).getData();
			return Response.ok().entity(geoHashToDocCount).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@GET
	@Path("aggregation/map")
	@Produces(MediaType.TEXT_HTML)
	@Operation(summary = "Return OpenLayers grid map from Geoserver geohash aggregation", description = "Returns an HTML map for the grid-based ES aggregation (text/html).", responses = {
			@ApiResponse(responseCode = "200", description = "HTML OpenLayers grid map", content = @Content(mediaType = MediaType.TEXT_HTML, schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response fetchAggsMap(
			@Parameter(description = "Geohash precision (default depends on your field)") @QueryParam("precision") Integer precision,
			@Parameter(description = "Top coordinate of bounding box") @QueryParam("top") Double top,
			@Parameter(description = "Left coordinate of bounding box") @QueryParam("left") Double left,
			@Parameter(description = "Bottom coordinate of bounding box") @QueryParam("bottom") Double bottom,
			@Parameter(description = "Right coordinate of bounding box") @QueryParam("right") Double right,
			@Parameter(description = "Width in pixels") @QueryParam("width") Double width,
			@Parameter(description = "Height in pixels") @QueryParam("height") Double height) {
		try {
			String workspace = NakshaConfig.getString("observation.geoserver.workspace");
			String layer = NakshaConfig.getString("observation.geoserver.layer");
			String geoField = NakshaConfig.getString("observation.es.geoField");
			String uri = workspace + "/wms";

			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("service", "WMS"));
			params.add(new BasicNameValuePair("version", "1.1.0"));
			params.add(new BasicNameValuePair("request", "GetMap"));
			params.add(new BasicNameValuePair("format", "application/openlayers"));
			params.add(new BasicNameValuePair("styles", "GeoHashGrid"));
			params.add(new BasicNameValuePair("srs", "EPSG:4326"));

			params.add(new BasicNameValuePair("layers", workspace + ":" + layer));
			params.add(new BasicNameValuePair("bbox", top + "," + left + "," + bottom + "," + right));
			params.add(new BasicNameValuePair("width", "400"));
			params.add(new BasicNameValuePair("height", "650"));
			params.add(new BasicNameValuePair("viewparams", "a:{\"large-grid\": {\"geohash_grid\": {\"field\": \""
					+ geoField + "\"\\, \"precision\": " + precision + "}}}"));

			byte[] file = geoserverService.getRequest(uri, params);
			return Response.status(Status.OK).entity(new ByteArrayInputStream(file)).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
}
