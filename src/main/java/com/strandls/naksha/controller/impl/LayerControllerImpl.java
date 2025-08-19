package com.strandls.naksha.controller.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.controller.LayerController;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.request.LayerDownload;
import com.strandls.naksha.pojo.request.MetaLayerEdit;
import com.strandls.naksha.pojo.response.GeoserverLayerStyles;
import com.strandls.naksha.pojo.response.LayerInfoOnClick;
import com.strandls.naksha.pojo.response.LocationInfo;
import com.strandls.naksha.pojo.response.ObservationLocationInfo;
import com.strandls.naksha.pojo.response.TOCLayer;
import com.strandls.naksha.service.GeoserverStyleService;
import com.strandls.naksha.service.MetaLayerService;
import com.strandls.naksha.utils.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

@Tag(name = "Layer Service")
@Path(ApiConstants.LAYER)
public class LayerControllerImpl implements LayerController {

	@Inject
	private MetaLayerService metaLayerService;
	@Inject
	private GeoserverStyleService geoserverStyleService;

	@Override
	@GET
	@Path(ApiConstants.LOCATIONINFO)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get state, district and tahsil for lat lon", responses = {
			@ApiResponse(responseCode = "200", description = "Location info object", content = @Content(schema = @Schema(implementation = LocationInfo.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response fetchLocationInfo(@QueryParam("lat") String lat, @QueryParam("lon") String lon) {
		try {
			LocationInfo result = metaLayerService.getLocationInfo(lat, lon);
			return Response.ok().entity(result).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@GET
	@Path("all")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get meta data of all the layers", responses = {
			@ApiResponse(responseCode = "200", description = "List of layer metadata", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TOCLayer.class)))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response findAll(@Context HttpServletRequest request, @DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset,
			@DefaultValue("false") @QueryParam("showOnlyPending") Boolean showOnlyPending) {
		try {
			List<TOCLayer> layerList = metaLayerService.getTOCList(request, limit, offset, showOnlyPending);
			return Response.ok().entity(layerList).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@GET
	@Path("count")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get layer count", responses = {
			@ApiResponse(responseCode = "200", description = "Total layer count", content = @Content(schema = @Schema(implementation = Long.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response getLayerCount(@Context HttpServletRequest request) {
		try {
			Long layerCount = metaLayerService.getLayerCount(request);
			return Response.ok().entity(layerCount).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@GET
	@Path("onClick/{layer}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get layer information for the layer on click", responses = {
			@ApiResponse(responseCode = "200", description = "Layer info on click", content = @Content(schema = @Schema(implementation = LayerInfoOnClick.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response getLayerInfoOnClick(@PathParam("layer") String layer) {
		try {
			MetaLayer metaLayer = metaLayerService.findByLayerTableName(layer);
			String titleColumn = metaLayer.getTitleColumn();
			List<String> summaryColumn = new ArrayList<>();
			for (String column : metaLayer.getSummaryColumns().split(",")) {
				if (column == null || "".equals(column))
					continue;
				summaryColumn.add(column);
			}
			List<GeoserverLayerStyles> styles = geoserverStyleService.fetchAllStyles(layer);
			LayerInfoOnClick layerInfoOnClick = new LayerInfoOnClick(layer, titleColumn, summaryColumn, styles);
			return Response.ok().entity(layerInfoOnClick).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@POST
	@Path("upload")
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Upload Layer", description = "Returns success or failure", responses = {
			@ApiResponse(responseCode = "200", description = "Layer uploaded", content = @Content(schema = @Schema(implementation = Map.class))),
			@ApiResponse(responseCode = "400", description = "file not present", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response upload(@Context HttpServletRequest request, final FormDataMultiPart multiPart) {
		try {
			Map<String, Object> result = metaLayerService.uploadLayer(request, multiPart);
			return Response.ok().entity(result).build();
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@PUT
	@Path("edit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Edit Layer meta data", description = "Returns updated meta layer", responses = {
			@ApiResponse(responseCode = "200", description = "Meta layer updated", content = @Content(schema = @Schema(implementation = MetaLayer.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response updateMetaLayerData(@Context HttpServletRequest request, MetaLayerEdit metaLayerEdit) {
		try {
			MetaLayer result = metaLayerService.updateMataLayer(request, metaLayerEdit);
			return Response.ok().entity(result).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@POST
	@Path("download")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Prepare shape file", description = "Return the shape file location", responses = {
			@ApiResponse(responseCode = "200", description = "Shape file location", content = @Content(schema = @Schema(implementation = Map.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response prepareDownload(@Context HttpServletRequest request, LayerDownload layerDownload) {
		try {
			Map<String, String> retValue = metaLayerService.prepareDownloadLayer(request, layerDownload);
			return Response.ok().entity(retValue).build();
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@GET
	@Path("download/{hashKey}/{layerName}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces("application/zip")
	@Operation(summary = "Download the shape file", description = "Return the shape file", responses = {
			@ApiResponse(responseCode = "200", description = "ZIP file attachment", content = @Content(mediaType = "application/zip", schema = @Schema(type = "string", format = "binary"))),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response download(@PathParam("hashKey") String hashKey, @PathParam("layerName") String layerName) {
		String fileLocation = metaLayerService.getFileLocation(hashKey, layerName);
		File file = new File(fileLocation);
		if (!file.exists()) {
			return Response.status(404).build();
		} else {
			ContentDisposition contentDisposition = ContentDisposition.type("attachment").fileName(file.getName())
					.creationDate(new Date()).build();
			return Response.ok((StreamingOutput) output -> {
				try (InputStream input = new FileInputStream(file)) {
					IOUtils.copy(input, output);
					output.flush();
				} catch (Exception e) {
					throw new WebApplicationException(
							Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
				}
			}).header("Content-Disposition", contentDisposition).build();
		}
	}

	@Override
	@GET
	@Path(ApiConstants.LAYERINFO)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Find layer info By Latitude and Longitude", description = "Returns Layer Details", responses = {
			@ApiResponse(responseCode = "200", description = "Observation location info", content = @Content(schema = @Schema(implementation = ObservationLocationInfo.class))),
			@ApiResponse(responseCode = "400", description = "Layer info not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response getLayerInfo(@QueryParam("lat") String lat, @QueryParam("lon") String lon) {
		try {
			ObservationLocationInfo observationLocationInfo = metaLayerService.getLayerInfo(lon, lat);
			return Response.ok().entity(observationLocationInfo).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@PUT
	@Path("active/{layer}")
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Make the layer active", responses = {
			@ApiResponse(responseCode = "200", description = "Layer marked active", content = @Content(schema = @Schema(implementation = MetaLayer.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response makeLayerActive(@Context HttpServletRequest request, @PathParam("layer") String layer) {
		try {
			if (!Utils.isAdmin(request)) {
				throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
						.entity("Only admin can make the layer active").build());
			}
			MetaLayer metaLayer = metaLayerService.makeLayerActive(layer);
			return Response.ok().entity(metaLayer).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@PUT
	@Path("pending/{layer}")
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Make the layer pending", responses = {
			@ApiResponse(responseCode = "200", description = "Layer marked pending", content = @Content(schema = @Schema(implementation = MetaLayer.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response makeLayerPending(@Context HttpServletRequest request, @PathParam("layer") String layer) {
		try {
			if (!Utils.isAdmin(request)) {
				throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
						.entity("Only admin can make the layer pending").build());
			}
			MetaLayer metaLayer = metaLayerService.makeLayerPending(layer);
			return Response.ok().entity(metaLayer).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@DELETE
	@Path("{layer}")
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Remove the layer (soft delete)", responses = {
			@ApiResponse(responseCode = "200", description = "Layer soft-deleted (inactive)", content = @Content(schema = @Schema(implementation = MetaLayer.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response removeLayer(@Context HttpServletRequest request, @PathParam("layer") String layer) {
		try {
			if (!Utils.isAdmin(request)) {
				throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
						.entity("Only admin can delete the layer").build());
			}
			MetaLayer metaLayer = metaLayerService.removeLayer(layer);
			return Response.ok().entity(metaLayer).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@DELETE
	@Path("deep/{layer}")
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Delete the layer completely with file and table as well", responses = {
			@ApiResponse(responseCode = "200", description = "Layer fully deleted", content = @Content(schema = @Schema(implementation = MetaLayer.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response deleteLayer(@Context HttpServletRequest request, @PathParam("layer") String layer) {
		try {
			if (!Utils.isAdmin(request)) {
				throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
						.entity("Only admin can do deep clean up").build());
			}
			MetaLayer metaLayer = metaLayerService.deleteLayer(layer);
			return Response.ok().entity(metaLayer).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@DELETE
	@Path("cleanup")
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Delete all the inactive layers completely with file and table as well", responses = {
			@ApiResponse(responseCode = "200", description = "All inactive layers full deleted", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MetaLayer.class)))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response cleanupInactiveLayer(@Context HttpServletRequest request) {
		try {
			if (!Utils.isAdmin(request)) {
				throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
						.entity("Only admin can do complete clean up").build());
			}
			List<MetaLayer> metaLayer = metaLayerService.cleanupInactiveLayers();
			return Response.ok().entity(metaLayer).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
}
