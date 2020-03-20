package com.strandls.naksha.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.service.MetaLayerService;
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

	//@Inject
	//private GeoServerIntegrationService service;
	
	@Inject
	private MetaLayerService metaLayerService;

	@GET
	@Path("ping")
	@Produces(MediaType.TEXT_PLAIN)
	public String ping() {
		return "pong";
	}

	@POST
	@Path("upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Upload Layer", notes = "Returns succuess failure", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "file not present", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })
	public Response upload(@Context HttpServletRequest request, final FormDataMultiPart multiPart) {
		try {
			Map<String, String> result = metaLayerService.upload(request, multiPart);
			return Response.ok().entity(result).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
/**
	@POST
	@Path(ApiConstants.UPLOADSHP)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Upload Layer", notes = "Returns succuess failure", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "file not present", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })
	public Response uploadFiles(final FormDataMultiPart multiPart) {

		try {
			FormDataBodyPart formdata = multiPart.getField("shp");

			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("SHP file not present").build());

			}

			InputStream shpInputStream = formdata.getValueAs(InputStream.class);
			String shpFileName = formdata.getContentDisposition().getFileName();
			shpFileName += ".";
			String layerName = shpFileName.split("\\.")[0].toLowerCase();
			System.out.println(layerName);

			formdata = multiPart.getField("dbf");

			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("DBF file not present").build());
			}
			InputStream dbfInputStream = formdata.getValueAs(InputStream.class);

			formdata = multiPart.getField("metadata");

			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("Metadata file not present").build());
			}
			InputStream metadataInputStream = formdata.getValueAs(InputStream.class);

			formdata = multiPart.getField("shx");
			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("Shx file not present").build());
			}
			InputStream shxInputStream = formdata.getValueAs(InputStream.class);

			int i = layerService.uploadShpLayer(shpInputStream, dbfInputStream, metadataInputStream, shxInputStream,
					layerName);

			// Waiting for disk files to be created then reload layers
			TimeUnit.SECONDS.sleep(5);
			service.getRequest("/rest/reload", null, "POST");

			return Response.status(Response.Status.OK)
					.entity("{\"responseCode\":" + i + ", \"info\": \"1 = failure && 0 = Success\"}").build();

		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());

		}

	}
*/
	@POST
	@Path(ApiConstants.DOWNLOADSHP)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Download the shp file", notes = "Return the shp file", response = String.class)
	public Response downloadShp(@QueryParam("outputFile") String outputFile, @QueryParam("host") String host,
			@QueryParam("user") String user, @QueryParam("password") String password,
			@QueryParam("dbName") String dbName, @QueryParam("query") String query, String jsonString) {

		JSONObject jsonObject = new JSONObject(jsonString);
		outputFile = jsonObject.getString("outputFile");
		host = jsonObject.getString("host");
		user = jsonObject.getString("user");
		password = jsonObject.getString("password");
		dbName = jsonObject.getString("dbName");
		String layer = jsonObject.getString("layerName");
		JSONArray attributeList = jsonObject.getJSONArray("attributeList");

		String selectAttribute = " * ";
		if (attributeList != null) {
			selectAttribute = "";
			attributeList.forEach(attribute -> {
				JSONObject attributeObject = (JSONObject) attribute;
				attributeObject.getString("Name");
			});
		}

		String ogrCommand = "ogr2ogr ";
		ogrCommand += "-f \"ESRI Shapefile\" ";
		ogrCommand += outputFile;
		String conn = "host=" + host + " user=" + user + " dbname=" + dbName;
		if (password != null)
			conn += " password=" + password;
		ogrCommand += " PG:\"" + conn + "\"";
		ogrCommand += " -sql";
		ogrCommand += " \"" + query + "\"";

		ProcessBuilder pb = new ProcessBuilder();
		pb.command("bash", "-c", ogrCommand);
		try {
			Process process1 = pb.start();
			process1.isAlive();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Response.ok().entity(ogrCommand).build();
	}

	/**
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
	*/
//	@GET
//	@Path("/attributes")
//	@Produces(MediaType.APPLICATION_JSON)
//	public List<LayerAttributes> attributes(@QueryParam("layername") String layername) {
//		return layerService.getLayerAttributes(layername);
//	}
//	
//	@GET
//	@Path("/tags")
//	@Produces(MediaType.APPLICATION_JSON)
//	public List<String> tags(@QueryParam("tag") String tag) {
//		return layerService.getLayerNamesWithTag(tag);
//	}

}