package com.strandls.naksha.service;

import com.strandls.naksha.pojo.style.JsonStyle;

public interface GeoserverStyleService {

	public JsonStyle generateJsonStyle(String layerName, String columnName);
}
