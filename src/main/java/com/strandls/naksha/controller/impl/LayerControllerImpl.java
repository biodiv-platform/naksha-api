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

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.pac4j.core.profile.CommonProfile;

import com.google.inject.Inject;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.naksha.controller.LayerController;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.response.LayerAttributes;
import com.strandls.naksha.service.MetaLayerService;
import com.sun.jersey.multipart.FormDataMultiPart;

public class LayerControllerImpl implements LayerController {

	@Inject
	private MetaLayerService metaLayerService;

	@Override
	public String ping() {
		return "pong";
	}

	@Override
	public Response findAll(HttpServletRequest request, Integer limit, Integer offset) {
		try {
			List<MetaLayer> metaLayers = metaLayerService.findAll(request, limit, offset);
			return Response.ok().entity(metaLayers).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
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
	public Response prepareDownload(HttpServletRequest request, String jsonString) throws FileNotFoundException {

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
	public Response download(String hashKey, String layerName) throws FileNotFoundException {
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
	public Response getLayerInfo(String lat, String lon) {
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
	public List<LayerAttributes> attributes(String layername) {
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
	public List<String> tags(String tag) {
		// TODO Auto-generated method stub
		// Get layer name with tags.
		// return layerService.getLayerNamesWithTag(tag);
		// "SELECT layer_tablename, tags FROM \"Meta_Layer\"";
		return null;
	}
}
