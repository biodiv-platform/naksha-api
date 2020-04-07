package com.strandls.naksha.controller;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.pojo.response.LayerAttributes;
import com.strandls.naksha.pojo.response.ObservationLocationInfo;
import com.sun.jersey.multipart.FormDataMultiPart;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * 
 */

@Api("Layer Service")
@Path(ApiConstants.LAYER)
public interface LayerController {

	@GET
	@Path("ping")
	@Produces(MediaType.TEXT_PLAIN)
	public String ping();

	@POST
	@Path("upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Upload Layer", notes = "Returns succuess failure", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "file not present", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })
	public Response upload(@Context HttpServletRequest request, final FormDataMultiPart multiPart);

	@POST
	@Path("download")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "prepate shape file", notes = "Return the shape file location", response = Map.class)
	public Response prepareDownload(@Context HttpServletRequest request, String jsonString) throws FileNotFoundException;

	@GET
	@Path("download/{hashKey}/{layerName}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces("application/zip")
	@ApiOperation(value = "Download the shp file", notes = "Return the shp file", response = StreamingOutput.class)
	public Response download(@PathParam("hashKey") String hashKey, @PathParam("layerName") String layerName) throws FileNotFoundException;
	
	@GET
	@Path(ApiConstants.LAYERINFO)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Find layer info By Latitude and Longitude", notes = " Returns Layer Details", response = ObservationLocationInfo.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Layer info notfound", response = String.class) })
	public Response getLayerInfo(@QueryParam("lat") String lat, @QueryParam("lon") String lon);

	@GET
	@Path("/attributes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<LayerAttributes> attributes(@QueryParam("layername") String layername);
	
	
	@GET
	@Path("/tags")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> tags(@QueryParam("tag") String tag);

}