package com.strandls.naksha.service;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.strandls.naksha.style.json.JsonStyle;

public interface GeoserverStyleService {

	public JsonStyle generateJsonStyle(String layerName, String columnName);
	
	public List<Object[]> getColumnName(String tableName);

	public List<String> publishAllStyles(String layerTableName, String workspace) throws JsonGenerationException, JsonMappingException, IOException;

}
