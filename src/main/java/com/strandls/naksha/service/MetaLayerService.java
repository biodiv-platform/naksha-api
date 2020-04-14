package com.strandls.naksha.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.parser.ParseException;

import com.sun.jersey.multipart.FormDataMultiPart;

public interface MetaLayerService {

	public static final String WORKSPACE = "biodiv";
	public static final String DATASTORE = "ibp";

	public Map<String, String> uploadLayer(HttpServletRequest request, FormDataMultiPart multiPart)
			throws IOException, ParseException, InvalidAttributesException, InterruptedException;

	public void prepareDownloadLayer(String uri, String jsonString)
			throws InvalidAttributesException, InterruptedException, FileNotFoundException, IOException;

	public String removeLayer(String layerName);

	public String getFileLocation(String hashKey, String layerName);

}
