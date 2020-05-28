package com.strandls.naksha.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.http.NameValuePair;

import it.geosolutions.geoserver.rest.GeoServerRESTManager;

public interface GeoserverService {

	public static final String GEOSERVER_URL       = "geoserver.url";
	public static final String GEOSERVER_USER_NAME = "geoserver.web.username";
	public static final String GEOSERVER_PASSWORD  = "geoserver.web.password";
	
	public GeoServerRESTManager getManager();
	
	public boolean publishLayer(String workspace, String datastore, String layerName, String srs, String layerTitle,
			List<String> keywords);
	
	public boolean removeLayer(String workspace, String layerName);

	public byte[] getRequest(String url, List<NameValuePair> params);

	boolean publishGeoTiffLayer(String workspace, String datastore, File geoTiffFile) throws FileNotFoundException;

}
