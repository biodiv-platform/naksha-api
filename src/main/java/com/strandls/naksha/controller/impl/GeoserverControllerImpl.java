package com.strandls.naksha.controller.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import com.strandls.naksha.controller.GeoserverController;
import com.strandls.naksha.pojo.response.GeoserverLayerStyles;
import com.strandls.naksha.pojo.style.JsonStyle;
import com.strandls.naksha.service.GeoserverService;
import com.strandls.naksha.service.GeoserverStyleService;
import com.strandls.naksha.utils.Utils;

public class GeoserverControllerImpl implements GeoserverController {

	@Inject
	private GeoserverService geoserverService;
	
	@Inject
	private GeoserverStyleService geoserverStyleService;
	
	@Inject
	public GeoserverControllerImpl() {
	}

	@Override
	public Response fetchAllLayers(String workspace) {
		try {
			ArrayList<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("REQUEST", "GetCapabilities"));

			String url = workspace + "/wfs";
			String layers = new String(geoserverService.getRequest(url, params));
			return Response.status(Status.OK).entity(Utils.convertStringToDocument(layers)).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	public Response getCapabilities(String workspace) {
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
	public Response fetchAllStyles(String id) {
		try {
			String url = "wms";

			ArrayList<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("request", "GetStyles"));
			params.add(new BasicNameValuePair("layers", id));
			params.add(new BasicNameValuePair("service", "wms"));
			params.add(new BasicNameValuePair("version", "1.1.1"));

			String styleString = new String(geoserverService.getRequest(url, params));
			Document styleDocument = Utils.convertStringToDocument(styleString);
			List<GeoserverLayerStyles> styles = Utils.getLayerStyles(styleDocument);
			return Response.status(Status.OK).entity(styles).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	public Response fetchStyle(String id) {
		try {
			String url = "styles/" + id;
			String style = new String(geoserverService.getRequest(url, null));
			return Response.status(Status.OK).entity(style).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Override
	public Response fetchThumbnail(String id, String wspace, String para, String width, String height, String srs) {
		String url = "wms";

		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("request", "GetStyles"));
		params.add(new BasicNameValuePair("layers", id));
		params.add(new BasicNameValuePair("service", "wms"));
		params.add(new BasicNameValuePair("version", "1.1.1"));

		String styleString = new String(geoserverService.getRequest(url, params));
		Document styleDocument = Utils.convertStringToDocument(styleString);
		List<GeoserverLayerStyles> styles = Utils.getLayerStyles(styleDocument);
		return Response.ok(styles).build();
	}

	@Override
	public Response fetchLegend(String layer, String style) {
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
	public Response fetchTiles(String layer, String z, String x, String y) {
		String url = "gwc/service/tms/1.0.0/" + layer + "@EPSG%3A900913@pbf/" + z + "/" + x + "/" + y + ".pbf";
		byte[] file = geoserverService.getRequest(url, null);
		return Response.ok(new ByteArrayInputStream(file)).build();
	}

	@Override
	public Response fetchStyle1(String layerName, String columnName) {
		try {
			JsonStyle style = geoserverStyleService.generateJsonStyle(layerName, columnName);
			return Response.status(Status.OK).entity(style).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

}
