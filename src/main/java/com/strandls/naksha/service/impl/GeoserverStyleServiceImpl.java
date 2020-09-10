package com.strandls.naksha.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;

import com.strandls.naksha.dao.GeoserverStyleDao;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.enumtype.LayerType;
import com.strandls.naksha.service.GeoserverStyleService;
import com.strandls.naksha.service.MetaLayerService;
import com.strandls.naksha.style.json.CircleLayerPaint;
import com.strandls.naksha.style.json.FillLayerPaint;
import com.strandls.naksha.style.json.JsonStyle;
import com.strandls.naksha.style.json.LineLayerPaint;
import com.strandls.naksha.style.json.StyleColor;
import com.strandls.naksha.style.json.StylePaint;
import com.strandls.naksha.style.json.StyledLayer;
import com.strandls.naksha.style.json.StyledSource;

public class GeoserverStyleServiceImpl implements GeoserverStyleService {

	@Inject
	private GeoserverStyleDao geoserverStyleDao;

	@Inject
	private MetaLayerService metaLayerService;

	private static final String[] COLOR_SCHEME = { "ffff7e", "f9d155", "f1a430", "a75118", "6c0000" };
	
	private static final double golden_ratio_conjugate = 0.618033988749895;
	
	private static final String CATEGORICAL = "categorical";
	private static final String INTERVAL    = "interval";
	
	private static final String GEO_TYPE_FILL   = "fill";
	private static final String GEO_TYPE_CIRCLE = "circle";
	private static final String GEO_TYPE_LINE   = "line";
	
	private static final int VERSION = 8;

	@Inject
	public GeoserverStyleServiceImpl() {
	}
	
	@Override
	public List<Object[]> getColumnName(String tableName) {
		return geoserverStyleDao.getColumnNames(tableName);
	}
	
	@Override
	public JsonStyle generateJsonStyle(String layerName, String columnName) {

		Map<String, StyledSource> sources = getSource(layerName);

		String columnType = geoserverStyleDao.getColumnType(layerName, columnName);
		List<List<Object>> stops = getStops(layerName, columnName, columnType);

		MetaLayer metaLayer = metaLayerService.findByLayerTableName(layerName);

		LayerType layerType = metaLayer.getLayerType();
		
		String styleType = columnType.startsWith("character") ? CATEGORICAL : INTERVAL;			

		List<StyledLayer> layers = getStyledLayers(layerName, layerType, columnName, styleType, stops);
		
		return new JsonStyle(VERSION, sources, layers);
	}
	
	private Map<String, StyledSource>  getSource(String layerName) {
		Map<String, StyledSource> sources = new HashMap<String, StyledSource>();
		String type = "vector";
		String scheme = "tms";
		List<String> tiles = new ArrayList<String>();
		tiles.add("/geoserver/gwc/service/tms/1.0.0/biodiv:" + layerName + "@EPSG%3A900913@pbf/{z}/{x}/{y}.pbf");
		StyledSource styledSource = new StyledSource(type, scheme, tiles);
		sources.put(layerName, styledSource);
		return sources;
	}
	
	private List<List<Object>> getStops(String layerName, String columnName, String columnType) {
		List<List<Object>> stops = new ArrayList<List<Object>>();
		if (columnType.startsWith("character")) {
			List<Object[]> values = geoserverStyleDao.getDistinctValues(layerName, columnName);
			for (Object object : values) {
				String color = getRandColor();
				List<Object> stop = new ArrayList<Object>();
				stop.add(object);
				stop.add("#" + color);
				stops.add(stop);
			}
		} else {
			List<Object[]> minMax = geoserverStyleDao.getMinMaxValues(layerName, columnName);
			Double min = Double.parseDouble(minMax.get(0)[0].toString());
			Double max = Double.parseDouble(minMax.get(0)[1].toString());
			int binSize = COLOR_SCHEME.length;
			Double size = (max - min) / binSize;
			for (int i = 0; i < binSize; i++) {
				String color = COLOR_SCHEME[i];
				List<Object> stop = new ArrayList<Object>();
				if (i == binSize - 1)
					stop.add(Math.ceil(max));
				else
					stop.add(Math.floor(min + size * i));
				stop.add("#" + color);
				stops.add(stop);
			}
		}
		return stops;
	}
	
	private String getRandColor() {
		int rndr = ThreadLocalRandom.current().nextInt(150, 400 + 1);

		double r = Math.floor(rndr * golden_ratio_conjugate);

		int rndg = ThreadLocalRandom.current().nextInt(150, 400 + 1);
		double g = Math.floor(rndg * golden_ratio_conjugate);

		int rndb = ThreadLocalRandom.current().nextInt(150, 400 + 1);
		double b = Math.floor(rndb * golden_ratio_conjugate);

		return String.format("%x%x%x", (int) r, (int) g, (int) b);
	}
	
	private List<StyledLayer> getStyledLayers(String layerName, LayerType layerType, String columnName, String styleType, List<List<Object>> stops) {
		JsonStyle jsonStyle = new JsonStyle();
		List<StyledLayer> layers = new ArrayList<StyledLayer>();
		StyleColor styleColor = new StyleColor(columnName, styleType, stops);
		StylePaint paint;
		String geoType = "";
		
		switch (layerType) {
		case MULTIPOLYGON:
			String fillOutlineColor = "#aaaaaa";
			Double fillOpacity = 0.5;
			paint = new FillLayerPaint(fillOutlineColor, fillOpacity, styleColor);
			geoType = GEO_TYPE_FILL;
			break;
		case POINT:
		case MULTIPOINT:
			Double circleRadius = 5.0;
			Double circleOpacity = 0.5;
			paint = new CircleLayerPaint(circleRadius, circleOpacity, styleColor);
			geoType = GEO_TYPE_CIRCLE;
			break;
		case MULTILINESTRING:
			Double lineWidth = 1.0;
			paint = new LineLayerPaint(lineWidth, styleColor);
			geoType = GEO_TYPE_LINE;
			break;
		default:
			return null;
		}
		
		StyledLayer styledLayer = new StyledLayer(layerName, geoType, layerName, layerName, paint);
		layers.add(styledLayer);
		jsonStyle.setLayers(layers);
		return layers;
	}
}
