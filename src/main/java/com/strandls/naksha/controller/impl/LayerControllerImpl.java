package com.strandls.naksha.controller.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.controller.LayerController;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.request.LayerDownload;
import com.strandls.naksha.pojo.request.MetaData;
import com.strandls.naksha.pojo.request.MetaLayerEdit;
import com.strandls.naksha.pojo.response.GeoserverLayerStyles;
import com.strandls.naksha.pojo.response.LayerInfoOnClick;
import com.strandls.naksha.pojo.response.LocationInfo;
import com.strandls.naksha.pojo.response.ObservationLocationInfo;
import com.strandls.naksha.pojo.response.TOCLayer;
import com.strandls.naksha.service.GeoserverStyleService;
import com.strandls.naksha.service.MetaLayerService;
import com.strandls.naksha.utils.Utils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Layer Service")
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
	@ApiOperation(value = "Get state,district and tahsil for lat lon", response = LocationInfo.class)
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
	@Path("all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get meta data of all the layers", response = TOCLayer.class, responseContainer = "List")
	public Response findAll(@Context HttpServletRequest request, @DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset,
			@DefaultValue("false") @QueryParam("showOnlyPending") Boolean showOnlyPending) {
		try {
			List<TOCLayer> layerList = metaLayerService.getTOCList(request, limit, offset, showOnlyPending);
			return Response.ok().entity(layerList).build();
		} catch (BadRequestException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@Path("count")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get meta data of all the layers", response = String.class)
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
	@Path("onClick/{layer}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get layer information for the layer on click", response = LayerInfoOnClick.class, responseContainer = "List")
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
	@Path("upload")
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Upload Layer", notes = "Returns succuess failure", response = MetaData.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "file not present", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })
	// @ValidateUser
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
	@Path("edit")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Upload Layer", notes = "Returns succuess failure", response = MetaData.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "file not present", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })
	@ValidateUser
	public Response updateMetaLayerData(@Context HttpServletRequest request,
			@ApiParam("mataLayerEdit") MetaLayerEdit metaLayerEdit) {
		try {
			MetaLayer result = metaLayerService.updateMataLayer(request, metaLayerEdit);
			return Response.ok().entity(result).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@Path("download")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "prepate shape file", notes = "Return the shape file location", response = Map.class)
	// @ValidateUser
	public Response prepareDownload(@Context HttpServletRequest request,
			@ApiParam("layerDownload") LayerDownload layerDownload) {
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
	@Path("download/{hashKey}/{layerName}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces("application/zip")
	@ApiOperation(value = "Download the shp file", notes = "Return the shp file", response = StreamingOutput.class)
	public Response download(@PathParam("hashKey") String hashKey, @PathParam("layerName") String layerName) {
		String fileLocation = metaLayerService.getFileLocation(hashKey, layerName);

		File file = new File(fileLocation);
		if (!file.exists()) {
			return javax.ws.rs.core.Response.status(404).build();
		} else {
			ContentDisposition contentDisposition = ContentDisposition.type("attachment").fileName(file.getName())
					.creationDate(new Date()).build();
			return javax.ws.rs.core.Response.ok((StreamingOutput) output -> {
				try {
					InputStream input = new FileInputStream(file);
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
	@Path(ApiConstants.LAYERINFO)
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Find layer info By Latitude and Longitude", notes = " Returns Layer Details", response = ObservationLocationInfo.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Layer info notfound", response = String.class) })
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
	@Path("active/{layer}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Make the layer active", response = MetaLayer.class)
	// @ValidateUser
	public Response makeLayerActive(@Context HttpServletRequest request, @PathParam("layer") String layer) {
		try {
//			if (!Utils.isAdmin(request)) {
//				throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
//						.entity("Only admin can make the layer active").build());
//			}
			MetaLayer metaLayer = metaLayerService.makeLayerActive(request, layer);
			return Response.ok().entity(metaLayer).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@Path("pending/{layer}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Make the layer pending", response = MetaLayer.class)
	// @ValidateUser
	public Response makeLayerPending(@Context HttpServletRequest request, @PathParam("layer") String layer) {
		try {
//			if (!Utils.isAdmin(request)) {
//				throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
//						.entity("Only admin can make the layer pending").build());
//			}
			MetaLayer metaLayer = metaLayerService.makeLayerPending(request, layer);
			return Response.ok().entity(metaLayer).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@Path("{layer}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get layer information for the layer on click", response = LayerInfoOnClick.class, responseContainer = "List")
	@ValidateUser
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
	@Path("deep/{layer}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Delete the layer completely with file and table as well", response = LayerInfoOnClick.class, responseContainer = "List")
	@ValidateUser
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
	@Path("cleanup")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Delete all the inactive layer completely with file and table as well", response = LayerInfoOnClick.class, responseContainer = "List")
	@ValidateUser
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
