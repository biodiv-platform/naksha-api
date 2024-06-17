package com.strandls.naksha.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.http.NameValuePair;

import com.strandls.naksha.pojo.GeoServerResponse;

import it.geosolutions.geoserver.rest.GeoServerRESTManager;

public interface GeoserverService {

	public static final String GEOSERVER_URL = "geoserver.url";
	public static final String GEOSERVER_USER_NAME = "geoserver.web.username";
	public static final String GEOSERVER_PASSWORD = "geoserver.web.password";

	public GeoServerRESTManager getManager();

	public boolean publishLayer(String workspace, String datastore, String layerName, String srs, String layerTitle,
			List<String> keywords, List<String> styles);

	public boolean removeLayer(String workspace, String layerName);

	public boolean removeDataStore(String workspace, String layerName);

	public byte[] postRequest(String uri, String styleContent, String contentType, List<NameValuePair> params);

	public byte[] getRequest(String url, List<NameValuePair> params);

	public GeoServerResponse getRequestForTiles(String url, List<NameValuePair> params);

	boolean publishGeoTiffLayer(String workspace, String datastore, File geoTiffFile) throws FileNotFoundException;

	boolean publishGeoTiffStyleLayer(String workspace, String datastore, File sldStyleFile)
			throws FileNotFoundException;

	boolean publishGeoTiffLayerWithStyle(String workspace, String datastore, String srs, String styleName,
			File geoTiffFile) throws FileNotFoundException, IllegalArgumentException;

	public List<List<Double>> getBBoxByLayerName(String workspace, String layerName);

}
