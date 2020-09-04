package com.strandls.naksha.service;

import java.util.List;

import com.strandls.naksha.pojo.style.JsonStyle;

public interface GeoserverStyleService {

	public JsonStyle generateJsonStyle(String layerName, String columnName);
	
	public List<Object[]> getColumnName(String tableName);
}