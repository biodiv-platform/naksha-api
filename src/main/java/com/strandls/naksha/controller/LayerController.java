package com.strandls.naksha.controller;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

/**
 * 
 * 
 */


public interface LayerController {

	public Response findAll(HttpServletRequest request, Integer limit, Integer offset);

	public Response upload(HttpServletRequest request, final FormDataMultiPart multiPart);

	public Response prepareDownload(HttpServletRequest request, String jsonString) throws FileNotFoundException;

	public Response download(String hashKey, String layerName) throws FileNotFoundException;

	public Response getLayerInfo(String lat, String lon);

	public Response getLayerInfoOnClick(String layer);

}