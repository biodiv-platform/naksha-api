package com.strandls.naksha.controller;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Inject;

import com.strandls.esmodule.controllers.GeoServiceApi;
import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.geoserver.GeoServerIntegrationService;

import io.swagger.annotations.Api;

/**
 * Controller for Observation related quries related queries
 *
 * @author Vilay
 *
 */

@Api("ObservationService")
@Path(ApiConstants.OBSERVATION)
public class ObservationController {

	@Inject
	private GeoServiceApi geoServiceApi;

	@Inject
	GeoServerIntegrationService service;

	@GET
	@Path("map")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_HTML)
	public Response fetchMap(@QueryParam("SpeciesId") String speciesId, @QueryParam("top") Double top,
			@QueryParam("left") Double left, @QueryParam("bottom") Double bottom, @QueryParam("right") Double right,
			@QueryParam("width") Double width, @QueryParam("height") Double height) {

		String workspace = NakshaConfig.getString("observation.geoserver.workspace");
		String layer = NakshaConfig.getString("observation.geoserver.layer");
		String uri = workspace + "/wms";

		List<NameValuePair> params = new ArrayList<NameValuePair>();
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
		byte[] file = service.getRequest(uri, params);
		return Response.status(Status.OK).entity(new ByteArrayInputStream(file)).build();
	}

	@GET
	@Path("aggregation")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchESAggs(@QueryParam("index") String index, @QueryParam("type") String type,
			@QueryParam("geoField") String geoField, @QueryParam("precision") Integer precision,
			@QueryParam("top") Double top, @QueryParam("left") Double left, @QueryParam("bottom") Double bottom,
			@QueryParam("right") Double right, @QueryParam("speciesId") Long speciesId) {
		try {

			index = index == null ? NakshaConfig.getString("observation.es.index") : index;
			type = type == null ? NakshaConfig.getString("observation.es.type") : type;
			geoField = geoField == null ? NakshaConfig.getString("observation.es.geoField") : geoField;

			Map<String, Object> geoHashToDocCount = geoServiceApi.getGeoAggregation(index, type, geoField, precision,
					top, left, bottom, right, speciesId);
			return Response.ok().entity(geoHashToDocCount).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path("aggregation/map")
	@Produces(MediaType.TEXT_HTML)
	public Response fetchAggsMap( @QueryParam("precision") Integer precision, @QueryParam("top") Double top,
			@QueryParam("left") Double left, @QueryParam("bottom") Double bottom, @QueryParam("right") Double right,
			@QueryParam("width") Double width, @QueryParam("height") Double height) {

		String workspace = NakshaConfig.getString("observation.geoserver.workspace");
		String layer = NakshaConfig.getString("observation.geoserver.layer");
		String geoField = NakshaConfig.getString("observation.es.geoField");
		String uri = workspace + "/wms";

		List<NameValuePair> params = new ArrayList<NameValuePair>();
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
		params.add(new BasicNameValuePair("viewparams",
				"a:{\"large-grid\": {\"geohash_grid\": {\"field\": \""+geoField+"\"\\, \"precision\": "+precision+"}}}"));

		byte[] file = service.getRequest(uri, params);
		return Response.status(Status.OK).entity(new ByteArrayInputStream(file)).build();
	}
}
