package com.strandls.naksha.controller;

import javax.ws.rs.core.Response;

public interface GeoserverController {

	public Response getCapabilities(String workspace);

	public Response fetchAllStyles(String id);

	public Response fetchStyle(String workspaces, String id);

	public Response fetchThumbnail(String id, String wspace, String para, String width, String height, String srs);

	public Response fetchLegend(String layer, String style);

	public Response fetchTiles(String layer, String z, String x, String y);
}
