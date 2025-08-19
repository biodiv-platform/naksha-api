package com.strandls.naksha.controller.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.controller.GeoserverController;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.enumtype.LayerType;
import com.strandls.naksha.pojo.response.GeoserverLayerStyles;
import com.strandls.naksha.service.GeoserverService;
import com.strandls.naksha.service.GeoserverStyleService;
import com.strandls.naksha.service.MetaLayerService;
import com.strandls.naksha.utils.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Tag(name = "Geoserver Service")
@Path(ApiConstants.GEOSERVER)
public class GeoserverControllerImpl implements GeoserverController {

	@Inject
	private GeoserverService geoserverService;

	@Inject
	private GeoserverStyleService geoserverStyleService;

	@Inject
	private MetaLayerService metaLayerService;

	@Inject
	public GeoserverControllerImpl() {
		// Default constructor
	}

	@Override
	@GET
	@Path(ApiConstants.LAYERS + "/{workspace}" + ApiConstants.WMS)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Capabilities", description = "Returns Capabilities", responses = {
			@ApiResponse(responseCode = "200", description = "Capabilities returned", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Details not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))) })
	public Response getCapabilities(
			@Parameter(description = "Geoserver workspace") @PathParam("workspace") String workspace) {
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
	@Operation(summary = "Fetch All Styles", description = "Return All Styles", responses = {
			@ApiResponse(responseCode = "200", description = "List of styles", content = @Content(mediaType = MediaType.APPLICATION_JSON, array = @ArraySchema(schema = @Schema(implementation = GeoserverLayerStyles.class)))),
			@ApiResponse(responseCode = "400", description = "Details not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))) })
	public Response fetchAllStyles(@Parameter(description = "Layer Table Name") @PathParam("id") String id) {
		try {
			List<GeoserverLayerStyles> styles = geoserverStyleService.fetchAllStyles(id);
			return Response.status(Status.OK).entity(styles).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@PUT
	@ValidateUser
	@Path(ApiConstants.LAYERS + "/all" + ApiConstants.STYLES)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Reload All Styles for the layer", description = "This method will override the style if already exist", responses = {
			@ApiResponse(responseCode = "200", description = "Styles as list of string layer names", content = @Content(mediaType = MediaType.APPLICATION_JSON, array = @ArraySchema(schema = @Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "400", description = "Details not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))) })
	public Response publishAllStyles(@Context HttpServletRequest request, @QueryParam("workspace") String workspace) {
		try {
			if (!Utils.isAdmin(request)) {
				throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
						.entity("Only admin can update the styles").build());
			}
			List<String> styles = geoserverStyleService.publishAllStyles(workspace);
			return Response.status(Status.OK).entity(styles).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@PUT
	@ValidateUser
	@Path(ApiConstants.LAYERS + "/{layerTableName}" + ApiConstants.STYLES)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Reload All Styles for the layer", description = "This method will override the style if already exist", responses = {
			@ApiResponse(responseCode = "200", description = "Styles as list of string layer names", content = @Content(mediaType = MediaType.APPLICATION_JSON, array = @ArraySchema(schema = @Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "400", description = "Details not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))) })
	public Response publishAllStyles(@Context HttpServletRequest request,
			@PathParam("layerTableName") String layerTableName, @QueryParam("workspace") String workspace) {
		try {
			if (!Utils.isAdmin(request)) {
				throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
						.entity("Only admin can update the styles").build());
			}
			List<String> styles = geoserverStyleService.publishAllStyles(layerTableName, workspace);
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
	@Operation(summary = "Fetch Styles", description = "Returns Styles as raw string (XML or JSON)", responses = {
			@ApiResponse(responseCode = "200", description = "Style returned (string)", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Styles not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))) })
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
	@Operation(summary = "Fetch Thumbnails", description = "Return Thumbnails", responses = {
			@ApiResponse(responseCode = "200", description = "Thumbnail returned", content = @Content(mediaType = "image/gif", schema = @Schema(type = "string", format = "binary"))),
			@ApiResponse(responseCode = "400", description = "Thumbnail not found", content = @Content(mediaType = "image/gif", schema = @Schema(implementation = String.class))) })
	public Response fetchThumbnail(@Parameter(description = "Layer Table Name") @PathParam("id") String id,
			@DefaultValue("biodiv") @PathParam("workspace") String wspace, @QueryParam("bbox") String para,
			@DefaultValue("200") @QueryParam("width") String width,
			@DefaultValue("200") @QueryParam("height") String height,
			@DefaultValue("EPSG:4326") @QueryParam("srs") String srs) {
		try {
			MetaLayer metaLayer = metaLayerService.findByLayerTableName(id);
			String colorBy = metaLayer.getColorBy();

			String style = id + "_";
			if (colorBy == null || "".equals(colorBy) || "NA".equals(colorBy)) {
				String summaryColumns = metaLayer.getSummaryColumns();
				if (summaryColumns == null || "".equals(summaryColumns))
					style = "";
				else {
					String column = metaLayer.getSummaryColumns().split(",")[0];
					style += column;
				}
			} else
				style += colorBy;

			ArrayList<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("request", "GetMap"));
			params.add(new BasicNameValuePair("layers", id));
			params.add(new BasicNameValuePair("service", "WMS"));
			params.add(new BasicNameValuePair("version", "1.1.0"));
			params.add(new BasicNameValuePair("bbox", para));
			params.add(new BasicNameValuePair("width", width));
			params.add(new BasicNameValuePair("height", height));
			params.add(new BasicNameValuePair("srs", srs));
			params.add(new BasicNameValuePair("format", "image/gif"));
			if (metaLayer.getLayerType() != LayerType.RASTER) {
				params.add(new BasicNameValuePair("styles", style.toLowerCase()));
			}

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
	@Operation(summary = "Fetch Legend", description = "Return Legend image", responses = {
			@ApiResponse(responseCode = "200", description = "Legend image returned", content = @Content(mediaType = "image/png", schema = @Schema(type = "string", format = "binary"))),
			@ApiResponse(responseCode = "400", description = "Legend not found", content = @Content(mediaType = "image/png", schema = @Schema(implementation = String.class))) })
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
	@Operation(summary = "Fetch Tiles", description = "Return Tiles (protobuf format)", responses = {
			@ApiResponse(responseCode = "200", description = "Tiles returned (protobuf binary)", content = @Content(mediaType = "application/x-protobuf", schema = @Schema(type = "string", format = "binary"))),
			@ApiResponse(responseCode = "400", description = "Tiles not found", content = @Content(mediaType = "application/x-protobuf", schema = @Schema(implementation = String.class))) })
	public Response fetchTiles(@PathParam("layer") String layer, @PathParam("z") String z, @PathParam("x") String x,
			@PathParam("y") String y) {
		String url = "gwc/service/tms/1.0.0/" + layer + "@EPSG%3A900913@pbf/" + z + "/" + x + "/" + y + ".pbf";
		byte[] file = geoserverService.getRequest(url, null);
		return Response.ok(new ByteArrayInputStream(file)).build();
	}
}
