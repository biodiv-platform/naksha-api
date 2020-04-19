package com.strandls.naksha.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.parser.ParseException;

import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.pojo.MetaLayer;
import com.sun.jersey.multipart.FormDataMultiPart;

public interface MetaLayerService {

	public static final String WORKSPACE = NakshaConfig.getString("workspace");
	public static final String DATASTORE = NakshaConfig.getString("datastore");

	public List<MetaLayer> findAll(HttpServletRequest request, Integer limit, Integer offset);

	public Map<String, Object> uploadLayer(HttpServletRequest request, FormDataMultiPart multiPart)
			throws IOException, ParseException, InvalidAttributesException, InterruptedException;

	public void prepareDownloadLayer(String uri, String hashKey, String jsonString)
			throws InvalidAttributesException, InterruptedException, FileNotFoundException, IOException;

	public String removeLayer(String layerName);

	public String getFileLocation(String hashKey, String layerName);

}
