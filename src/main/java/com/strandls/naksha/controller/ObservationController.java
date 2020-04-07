package com.strandls.naksha.controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.strandls.naksha.ApiConstants;

import io.swagger.annotations.Api;

@Api("ObservationService")
@Path(ApiConstants.OBSERVATION)
public interface ObservationController {

	@GET
	@Path("map")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_HTML)
	public Response fetchMap(@QueryParam("SpeciesId") String speciesId, @QueryParam("top") Double top,
			@QueryParam("left") Double left, @QueryParam("bottom") Double bottom, @QueryParam("right") Double right,
			@QueryParam("width") Double width, @QueryParam("height") Double height);

	@GET
	@Path("aggregation")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchESAggs(@QueryParam("index") String index, @QueryParam("type") String type,
			@QueryParam("geoField") String geoField, @QueryParam("precision") Integer precision,
			@QueryParam("top") Double top, @QueryParam("left") Double left, @QueryParam("bottom") Double bottom,
			@QueryParam("right") Double right, @QueryParam("speciesId") Long speciesId);

	@GET
	@Path("aggregation/map")
	@Produces(MediaType.TEXT_HTML)
	public Response fetchAggsMap(@QueryParam("precision") Integer precision, @QueryParam("top") Double top,
			@QueryParam("left") Double left, @QueryParam("bottom") Double bottom, @QueryParam("right") Double right,
			@QueryParam("width") Double width, @QueryParam("height") Double height);

}
