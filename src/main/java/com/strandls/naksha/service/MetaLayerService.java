package com.strandls.naksha.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.json.simple.parser.ParseException;

import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.response.ObservationLocationInfo;

public interface MetaLayerService {
	
	public static final String INDIA_TAHSIL = NakshaConfig.getString("layer.india.tahsil.name");
	public static final String INDIA_SOIL = NakshaConfig.getString("layer.india.soil.name");
	public static final String INDIA_FOREST_TYPE = NakshaConfig.getString("layer.india.foresttype.name");
	public static final String INDIA_RAINFALLZONE = NakshaConfig.getString("layer.india.rainfallzone.name");
	public static final String INDIA_TEMPERATURE = NakshaConfig.getString("layer.india.temperature.name");
	public static final String GEOMETRY_COLUMN_NAME = NakshaConfig.getString("layer.geometry.column.name");

	public static final String WORKSPACE = NakshaConfig.getString("workspace");
	public static final String DATASTORE = NakshaConfig.getString("datastore");

	public MetaLayer findByLayerTableName(String layerName);
	
	public List<MetaLayer> findAll(HttpServletRequest request, Integer limit, Integer offset);

	public Map<String, Object> uploadLayer(HttpServletRequest request, FormDataMultiPart multiPart)
			throws IOException, ParseException, InvalidAttributesException, InterruptedException;

	public void prepareDownloadLayer(String uri, String hashKey, String jsonString)
			throws InvalidAttributesException, InterruptedException, FileNotFoundException, IOException;

	public String removeLayer(String layerName);

	public String getFileLocation(String hashKey, String layerName);

	public ObservationLocationInfo getLayerInfo(String lon, String lat);

}
