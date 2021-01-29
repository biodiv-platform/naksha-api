package com.strandls.naksha.service;

import java.io.IOException;
import java.util.List;

import com.strandls.naksha.pojo.response.GeoserverLayerStyles;
import com.strandls.naksha.style.json.JsonStyle;

public interface GeoserverStyleService {

	public JsonStyle generateJsonStyle(String layerName, String columnName);
	
	public List<Object[]> getColumnName(String tableName);

	public List<String> publishAllStyles(String layerTableName, String workspace) throws IOException;

	public void unpublishAllStyles(String layerName, String workspace);
	
	List<GeoserverLayerStyles> fetchAllStyles(String id);

}
