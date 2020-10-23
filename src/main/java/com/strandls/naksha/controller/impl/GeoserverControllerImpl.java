package com.strandls.naksha.controller.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.controller.GeoserverController;
import com.strandls.naksha.pojo.response.GeoserverLayerStyles;
import com.strandls.naksha.service.GeoserverService;
import com.strandls.naksha.service.GeoserverStyleService;
import com.strandls.naksha.utils.Utils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Geoserver Service")
@Path(ApiConstants.GEOSERVER)
public class GeoserverControllerImpl implements GeoserverController {

	@Inject
	private GeoserverService geoserverService;

	@Inject
	private GeoserverStyleService geoserverStyleService;

	@Inject
	public GeoserverControllerImpl() {
	}

	@Override
	@GET
	@Path(ApiConstants.LAYERS + "/{workspace}" + ApiConstants.WMS)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Capabilities", notes = " Returns Capabilities", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Details not found", response = String.class) })
	public Response getCapabilities(@PathParam("workspace") String workspace) {
		try {
			ArrayList<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("REQUEST", "GetCapabilities"));
			String url = workspace + "/wms";
			String layers = new String(geoserverService.getRequest(url, params));
			String capabilities = Utils.jsonizeLayerString(layers);
			return Response.status(Status.OK).entity(capabilities).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@GET
	@Path(ApiConstants.LAYERS + "/{id}" + ApiConstants.STYLES)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Fetch All Styles", notes = "Return All Styles", response = GeoserverLayerStyles.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Details not found", response = String.class) })
	public Response fetchAllStyles(@PathParam("id") String id) {
		try {
			List<GeoserverLayerStyles> styles = geoserverStyleService.fetchAllStyles(id);
			return Response.status(Status.OK).entity(styles).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@GET
	@Path("/workspaces/{workspaces}" + ApiConstants.STYLES + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Fetch Styles", notes = "Retruns Styles", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Styles not found", response = String.class) })
	public Response fetchStyle(@PathParam("workspaces") String workspaces, @PathParam("id") String id) {
		try {
			String url = "rest/workspaces/" + workspaces + "/styles/" + id;
			String style = new String(geoserverService.getRequest(url, null));
			return Response.status(Status.OK).entity(style).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@GET
	@Path(ApiConstants.THUMBNAILS + "/{workspace}/{id}")
	@Produces("image/gif")
	@ApiOperation(value = "Fetch Thumbnails", notes = "Return Thumbnails", response = ByteArrayInputStream.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Thumbnail not found", response = String.class) })
	public Response fetchThumbnail(@PathParam("id") String id, @DefaultValue("biodiv") @PathParam("workspace") String wspace,
			@QueryParam("bbox") String para, @DefaultValue("200") @QueryParam("width") String width, @DefaultValue("200") @QueryParam("height") String height,
			 @DefaultValue("EPSG:4326") @QueryParam("srs") String srs) {
		try {
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("request", "GetMap"));
			params.add(new BasicNameValuePair("layers", id));
			params.add(new BasicNameValuePair("service", "WMS"));
			params.add(new BasicNameValuePair("version", "1.1.0"));
			params.add(new BasicNameValuePair("bbox", para));
			params.add(new BasicNameValuePair("width", width));
			params.add(new BasicNameValuePair("height", height));
			params.add(new BasicNameValuePair("srs", srs));
			params.add(new BasicNameValuePair("format", "image/gif"));

			byte[] file = geoserverService.getRequest(wspace + "/wms", params);

			return Response.status(Status.OK).entity(new ByteArrayInputStream(file)).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}

	}

	@Override
	@GET
	@Path(ApiConstants.LEGEND + "/{layer}/{style}")
	@Produces("image/png")
	@ApiOperation(value = "Fetch Legend", notes = "Return Legend", response = ByteArrayInputStream.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Legend not found", response = String.class) })
	public Response fetchLegend(@PathParam("layer") String layer, @PathParam("style") String style) {
		String url = "wms";

		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("REQUEST", "GetLegendGraphic"));
		params.add(new BasicNameValuePair("VERSION", "1.0.0"));
		params.add(new BasicNameValuePair("FORMAT", "image/png"));
		params.add(new BasicNameValuePair("transparent", "true"));
		params.add(new BasicNameValuePair("LAYER", layer));
		params.add(new BasicNameValuePair("style", style));

		byte[] file = geoserverService.getRequest(url, params);
		return Response.ok(new ByteArrayInputStream(file)).build();
	}

	@Override
	@GET
	@Path("/gwc/service/tms/1.0.0/{layer}/{z}/{x}/{y}")
	@Produces("application/x-protobuf")
	@ApiOperation(value = "Fetch Tiles", notes = "Return Tiles", response = ByteArrayInputStream.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Tiles not found", response = String.class) })
	public Response fetchTiles(@PathParam("layer") String layer, @PathParam("z") String z, @PathParam("x") String x,
			@PathParam("y") String y) {
		String url = "gwc/service/tms/1.0.0/" + layer + "@EPSG%3A900913@pbf/" + z + "/" + x + "/" + y + ".pbf";
		byte[] file = geoserverService.getRequest(url, null);
		return Response.ok(new ByteArrayInputStream(file)).build();
	}

}
