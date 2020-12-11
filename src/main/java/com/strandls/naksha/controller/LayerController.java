package com.strandls.naksha.controller;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.strandls.naksha.pojo.request.LayerDownload;

/**
 * 
 * 
 */


public interface LayerController {

	public Response findAll(HttpServletRequest request, Integer limit, Integer offset, Boolean showOnlyPending);

	public Response upload(HttpServletRequest request, final FormDataMultiPart multiPart);

	public Response prepareDownload(HttpServletRequest request, LayerDownload layerDownload) throws FileNotFoundException;

	public Response download(String hashKey, String layerName) throws FileNotFoundException;

	public Response getLayerInfo(String lat, String lon);

	public Response getLayerInfoOnClick(String layer);

	public Response removeLayer(HttpServletRequest request, String layer);

	public Response makeLayerActive(HttpServletRequest request, String layer);

}