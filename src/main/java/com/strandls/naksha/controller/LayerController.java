package com.strandls.naksha.controller;

import java.io.FileNotFoundException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.strandls.naksha.pojo.response.LayerAttributes;

/**
 * 
 * 
 */


public interface LayerController {

	public String ping();

	public Response findAll(HttpServletRequest request, Integer limit, Integer offset);

	public Response upload(HttpServletRequest request, final FormDataMultiPart multiPart);

	public Response prepareDownload(HttpServletRequest request, String jsonString) throws FileNotFoundException;

	public Response download(String hashKey, String layerName) throws FileNotFoundException;

	public Response getLayerInfo(String lat, String lon);

	public List<LayerAttributes> attributes(String layername);

	public List<String> tags(String tag);

}