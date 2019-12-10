package com.strandls.naksha.controller;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.inject.Inject;
import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.geoserver.GeoServerIntegrationService;
import com.strandls.naksha.layers.LayerUploadService;
import com.strandls.naksha.pojo.ObservationLocationInfo;
import com.sun.jersey.multipart.FormDataBodyPart;
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
public class LayerController {

	@Inject
	LayerUploadService layerService;
	@Inject
	GeoServerIntegrationService service;

	@GET
	@Path("/ping")
	@Produces(MediaType.TEXT_PLAIN)
	public String ping() {
		return "pong";
	}

	@POST
	@Path(ApiConstants.UPLOADSHP)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Upload Layer", notes = "Returns succuess failure", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "file not present", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })
	public Response uploadFiles(final FormDataMultiPart multiPart) {

		try {
			FormDataBodyPart formdata = multiPart.getField("metadata");
			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("Metadata file not present").build());
			}
			InputStream metadataInputStream = formdata.getValueAs(InputStream.class);
			formdata = multiPart.getField("shp");
			if (formdata != null) {
				InputStream shpInputStream = formdata.getValueAs(InputStream.class);
				String shpFileName = formdata.getContentDisposition().getFileName();
				shpFileName += ".";
				String layerName = shpFileName.split("\\.")[0].toLowerCase();
				System.out.println(layerName);
				formdata = multiPart.getField("dbf");
				if (formdata == null) {
					throw new WebApplicationException(
							Response.status(Response.Status.BAD_REQUEST).entity("Metadata file not present").build());
				}
				InputStream dbfInputStream = formdata.getValueAs(InputStream.class);
				formdata = multiPart.getField("shx");
				if (formdata == null) {
					throw new WebApplicationException(
							Response.status(Response.Status.BAD_REQUEST).entity("Shx file not present").build());
				}
				InputStream shxInputStream = formdata.getValueAs(InputStream.class);
				int i = layerService.uploadShpLayer(shpInputStream, dbfInputStream, metadataInputStream, shxInputStream,
						layerName);
				TimeUnit.SECONDS.sleep(5);
				service.getRequest("/rest/reload", null, "POST");
				return Response.status(Response.Status.OK)
						.entity("{\"responseCode\":" + i + ", \"info\": \"1 = failure && 0 = Success\"}").build();
			} else {
				boolean isCsvFile;
				InputStream inputStream;
				FormDataBodyPart formData = multiPart.getField("csv");
				if (formData == null) {
					formData = multiPart.getField("xlsx");
					isCsvFile = false;
				} else {
					isCsvFile = true;
				}
				inputStream = formData.getValueAs(InputStream.class);
				String fileName = formData.getContentDisposition().getFileName();
				fileName += ".";
				String layerName = fileName.split("\\.")[0].toLowerCase();
				System.out.println(layerName);
				int k = layerService.uploadfilelayer(inputStream, metadataInputStream, layerName, isCsvFile);
				TimeUnit.SECONDS.sleep(5);
				service.getRequest("/rest/reload", null, "POST");
				return Response.status(Response.Status.OK)
						.entity("{\"responseCode\":" + k + ", \"info\": \"1 = failure && 0 = Success\"}").build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}

	}

	@GET
	@Path(ApiConstants.LAYERINFO)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Find layer info By Latitude and Longitude", notes = "Returns Layer Details", response = ObservationLocationInfo.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Layer info not found", response = String.class) })

	public Response getLayerInfo(@QueryParam("lat") String lat, @QueryParam("lon") String lon) {

		try {
			Double latitude = Double.parseDouble(lat);
			Double longitude = Double.parseDouble(lon);
			ObservationLocationInfo locationInfo = layerService.getLayerDetails(latitude, longitude);
			return Response.status(Status.OK).entity(locationInfo).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	// @GET
	// @Path("/attributes")
	// @Produces(MediaType.APPLICATION_JSON)
	// public List<LayerAttributes> attributes(@QueryParam("layername") String
	// layername) {
	// return layerService.getLayerAttributes(layername);
	// }
	//
	// @GET
	// @Path("/tags")
	// @Produces(MediaType.APPLICATION_JSON)
	// public List<String> tags(@QueryParam("tag") String tag) {
	// return layerService.getLayerNamesWithTag(tag);
	// }

}