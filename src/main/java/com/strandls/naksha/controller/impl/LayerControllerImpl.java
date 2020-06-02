package com.strandls.naksha.controller.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.json.JSONObject;
import org.pac4j.core.profile.CommonProfile;

import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.controller.LayerController;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.response.LayerAttributes;
import com.strandls.naksha.pojo.response.ObservationLocationInfo;
import com.strandls.naksha.service.MetaLayerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Layer Service")
@Path(ApiConstants.LAYER)
public class LayerControllerImpl implements LayerController {

	private MetaLayerService metaLayerService;

	@Inject
	public LayerControllerImpl(MetaLayerService metaLayerService) {
		this.metaLayerService = metaLayerService;
	}

	@Override
	@Path("ping")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String ping() {
		return "pong";
	}

	@Override
	@Path("all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get meta data of all the layers", response = MetaLayer.class, responseContainer = "List")
	public Response findAll(@Context HttpServletRequest request, @DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		try {
			List<MetaLayer> metaLayers = metaLayerService.findAll(request, limit, offset);
			return Response.ok().entity(metaLayers).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	@Path("upload")
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Upload Layer", notes = "Returns succuess failure", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "file not present", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })
	//@ValidateUser
	public Response upload(@Context HttpServletRequest request, final FormDataMultiPart multiPart) {
		try {
			Map<String, Object> result = metaLayerService.uploadLayer(request, multiPart);
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
	//@ValidateUser
	public Response prepareDownload(@Context HttpServletRequest request, String jsonString) throws FileNotFoundException {

		CommonProfile profile = AuthUtil.getProfileFromRequest(request);
		System.out.println(profile);

		String uri = request.getRequestURI();
		String hashKey = UUID.randomUUID().toString();

		try {
			ExecutorService service = Executors.newFixedThreadPool(10);
			service.execute(new Runnable() {
				@Override
				public void run() {
					try {
						metaLayerService.prepareDownloadLayer(uri, hashKey, jsonString);
					} catch (InvalidAttributesException | InterruptedException | IOException e) {
						e.printStackTrace();
					}
				}
			});

			JSONObject jsonObject = new JSONObject(jsonString);
			String layerName = jsonObject.getString("layerName");

			Map<String, String> retValue = new HashMap<String, String>();
			retValue.put("url", uri + "/" + hashKey + "/" + layerName);
			retValue.put("success", "The layer download process has started. You will receive the mail shortly");
			return Response.ok().entity(retValue).build();
		} catch (Exception e) {
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
	public Response download(@PathParam("hashKey") String hashKey,  @PathParam("layerName") String layerName) throws FileNotFoundException {
		try {
			String fileLocation = metaLayerService.getFileLocation(hashKey, layerName);
			return Response.ok(new File(fileLocation))
					.header("Content-Disposition", "attachment; filename=\"" + layerName + "\"").build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
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
		// TODO Auto-generated method stub
		// Here we need to get info from four different layer and return the observation
		// info about it.
		// 1. Get tahsil from lyr_115_india_tahsils
		// 2. Get description from lyr_117_india_soils
		// 3. Get type description from lyr_118_india_foresttypes;
		// 4. Get rain range from lyr_119_india_rainfallzone.
		// 5. Get temperature from lyr_162_india_temperature.
		return null;
	}

	@Override
	@Path("/attributes")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<LayerAttributes> attributes(@QueryParam("layername") String layername) {
		// TODO Auto-generated method stub
		// Get the layer attribute from given layer. basically column name and
		// description.
		// return layerService.getLayerAttributes(layername);
		/*
		 * SELECT c.column_name, pgd.description\n" +
		 * "FROM pg_catalog.pg_statio_all_tables as st\n" +
		 * "INNER JOIN pg_catalog.pg_description pgd on (pgd.objoid=st.relid)\n" +
		 * "RIGHT OUTER JOIN information_schema.columns c on (pgd.objsubid=c.ordinal_position and  c.table_schema=st.schemaname and c.table_name=st.relname)\n"
		 * + "WHERE table_schema = 'public' and table_name = ?"
		 */
		return null;
	}

	@Override
	@Path("/tags")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> tags(@QueryParam("tag") String tag) {
		// TODO Auto-generated method stub
		// Get layer name with tags.
		// return layerService.getLayerNamesWithTag(tag);
		// "SELECT layer_tablename, tags FROM \"Meta_Layer\"";
		return null;
	}
}
