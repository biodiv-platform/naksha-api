package com.strandls.naksha.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.json.simple.parser.ParseException;

import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.request.LayerDownload;
import com.strandls.naksha.pojo.request.MetaLayerEdit;
import com.strandls.naksha.pojo.response.LocationInfo;
import com.strandls.naksha.pojo.response.ObservationLocationInfo;
import com.strandls.naksha.pojo.response.TOCLayer;
import com.strandls.user.ApiException;

public interface MetaLayerService {

	public static final String INDIA_TAHSIL = NakshaConfig.getString("layer.india.tahsil.name");
	public static final String INDIA_SOIL = NakshaConfig.getString("layer.india.soil.name");
	public static final String INDIA_FOREST_TYPE = NakshaConfig.getString("layer.india.foresttype.name");
	public static final String INDIA_RAINFALLZONE = NakshaConfig.getString("layer.india.rainfallzone.name");
	public static final String INDIA_TEMPERATURE = NakshaConfig.getString("layer.india.temperature.name");
	public static final String GEOMETRY_COLUMN_NAME = NakshaConfig.getString("layer.geometry.column.name");
	public static final String LAYER_MADAGASCAR = NakshaConfig.getString("layer.madagascar.location.info");
	public static final String LAYER_BBP = NakshaConfig.getString("layer.bbp.location.info");

	public static final String WORKSPACE = NakshaConfig.getString("workspace");
	public static final String DATASTORE = NakshaConfig.getString("datastore");
	public static final String TAHSIL_COLUMN_NAME = NakshaConfig.getString("layer.india.tahsil.tahsil.column.name");
	public static final String DISTRICT_COLUMN_NAME = NakshaConfig.getString("layer.india.tahsil.district.column.name");
	public static final String STATE_COLUMN_NAME = NakshaConfig.getString("layer.india.tahsil.state.column.name");

	public MetaLayer findByLayerTableName(String layerName);

	public Long getLayerCount(HttpServletRequest request);

	public List<MetaLayer> findAll(HttpServletRequest request, Integer limit, Integer offset);

	public Map<String, Object> uploadLayer(HttpServletRequest request, FormDataMultiPart multiPart)
			throws Exception;

	public Map<String, String> prepareDownloadLayer(HttpServletRequest request, LayerDownload layerDownload)
			throws InvalidAttributesException, InterruptedException, IOException;

	public MetaLayer removeLayer(String layerName);

	public String getFileLocation(String hashKey, String layerName);

	public ObservationLocationInfo getLayerInfo(String lon, String lat);

	public List<TOCLayer> getTOCList(HttpServletRequest request, Integer limit, Integer offset, boolean showOnlyPending)
			throws ApiException, com.vividsolutions.jts.io.ParseException, URISyntaxException;

	public MetaLayer makeLayerActive(String layer);

	public MetaLayer makeLayerPending(String layerName);

	public MetaLayer deleteLayer(String layerName);

	public List<MetaLayer> cleanupInactiveLayers();

	public MetaLayer updateMataLayer(HttpServletRequest request, MetaLayerEdit metaLayerEdit) throws IOException;

	public LocationInfo getLocationInfo(String lat, String lon);

	public String isTableAvailable(String layerName);

}
