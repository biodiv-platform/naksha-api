package com.strandls.naksha.controller;

import java.io.ByteArrayInputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.pojo.response.GeoserverLayerStyles;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Geoserver Service")
@Path(ApiConstants.GEOSERVER)
public interface GeoserverController {

	@GET
	@Path(ApiConstants.LAYERS + "/{workspace}" +  ApiConstants.WFS)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Fetch all Layer", notes = "Returns all Layers Details", response = String.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Details not found", response = String.class) })
	public Response fetchAllLayers(@PathParam("workspace") String workspace);

	@GET
	@Path(ApiConstants.LAYERS + "/{workspace}" + ApiConstants.WMS)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Capabilities", notes = " Returns Capabilities", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Details not found", response = String.class) })
	public Response getCapabilities(@PathParam("workspace") String workspace);

	@GET
	@Path(ApiConstants.LAYERS + "/{id}" + ApiConstants.STYLES)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Fetch All Styles", notes = "Return All Styles", response = GeoserverLayerStyles.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Details not found", response = String.class) })
	public Response fetchAllStyles(@PathParam("id") String id);

	@GET
	@Path(ApiConstants.STYLES + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Fetch Styles", notes = "Retruns Styles", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Styles not found", response = String.class) })
	public Response fetchStyle(@PathParam("id") String id);

	@GET
	@Path(ApiConstants.THUMBNAILS + "/{workspace}/{id}")
	@Produces("image/gif")
	@ApiOperation(value = "Fetch Thumbnails", notes = "Return Thumbnails", response = ByteArrayInputStream.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Thumbnail not found", response = String.class) })
	public Response fetchThumbnail(@PathParam("id") String id, @PathParam("workspace") String wspace,
			@QueryParam("bbox") String para, @QueryParam("width") String width, @QueryParam("height") String height,
			@QueryParam("srs") String srs);

	@GET
	@Path(ApiConstants.LEGEND + "/{layer}/{style}")
	@Produces("image/png")
	@ApiOperation(value = "Fetch Legend", notes = "Return Legend", response = ByteArrayInputStream.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Legend not found", response = String.class) })
	public Response fetchLegend(@PathParam("layer") String layer, @PathParam("style") String style);

	@GET
	@Path("/gwc/service/tms/1.0.0/{layer}/{z}/{x}/{y}")
	@Produces("application/x-protobuf")
	@ApiOperation(value = "Fetch Tiles", notes = "Return Tiles", response = ByteArrayInputStream.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Tiles not found", response = String.class) })
	public Response fetchTiles(@PathParam("layer") String layer, @PathParam("z") String z, @PathParam("x") String x,
			@PathParam("y") String y);
}
