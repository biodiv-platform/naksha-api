package com.strandls.naksha.controller;

import javax.ws.rs.core.Response;

public interface GeoserverController {

	public Response fetchAllLayers(String workspace);

	public Response getCapabilities(String workspace);

	public Response fetchAllStyles(String id);

	public Response fetchStyle(String id);

	public Response fetchThumbnail(String id, String wspace, String para, String width, String height, String srs);

	public Response fetchLegend(String layer, String style);

	public Response fetchTiles(String layer, String z, String x, String y);

	public Response fetchStyle1(String layerName, String columnName);
}
