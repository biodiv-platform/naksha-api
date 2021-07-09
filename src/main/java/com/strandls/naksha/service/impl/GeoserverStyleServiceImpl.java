package com.strandls.naksha.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.dao.GeoserverStyleDao;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.enumtype.LayerType;
import com.strandls.naksha.pojo.response.GeoserverLayerStyles;
import com.strandls.naksha.pojo.response.MBStyle;
import com.strandls.naksha.service.GeoserverService;
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
import com.strandls.naksha.utils.MetaLayerUtil;

import it.geosolutions.geoserver.rest.manager.GeoServerRESTStyleManager;

public class GeoserverStyleServiceImpl implements GeoserverStyleService {
	
	private final Logger logger = LoggerFactory.getLogger(GeoserverStyleServiceImpl.class);

	@Inject
	private GeoserverStyleDao geoserverStyleDao;

	@Inject
	private MetaLayerService metaLayerService;

	@Inject
	private GeoserverService geoserverService;

	@Inject
	private ObjectMapper objectMapper;

	private static final String[] COLOR_SCHEME = { "ffff7e", "f9d155", "f1a430", "a75118", "6c0000" };

	private static final Set<String> ALLOWED_TYPE;

	private static final double GOLDEN_RATIO_CONJUGATE = 0.618033988749895;

	private static final String CATEGORICAL = "categorical";
	private static final String INTERVAL = "interval";

	private static final String GEO_TYPE_FILL = "fill";
	private static final String GEO_TYPE_CIRCLE = "circle";
	private static final String GEO_TYPE_LINE = "line";

	private static final String CHARACTER = "character";

	private static final int VERSION = 8;
	
	private static final double OPACITY = 0.8;

	private static String geoserverDataDirectory = "";

	static {
		ALLOWED_TYPE = new HashSet<>();
		ALLOWED_TYPE.add("bigint");
		ALLOWED_TYPE.add("integer");
		ALLOWED_TYPE.add("smallint");
		ALLOWED_TYPE.add("double precision");
		ALLOWED_TYPE.add("real");
		ALLOWED_TYPE.add("text");
		geoserverDataDirectory = NakshaConfig.getString(MetaLayerUtil.TEMP_DIR_GEOSERVER_PATH);
	}

	@Inject
	public GeoserverStyleServiceImpl() {
		// Default constructor
	}

	@Override
	public List<GeoserverLayerStyles> fetchAllStyles(String id) {
		MetaLayer metaLayer = metaLayerService.findByLayerTableName(id);
		String colorByColumn = metaLayer.getColorBy();
		List<Object[]> columnNames = getColumnName(id);
		List<GeoserverLayerStyles> styles = new ArrayList<>();
		for (Object[] row : columnNames) {
			String styleName = row[0].toString();
			String styleTitle = row[1].toString();
			String styleType = row[2].toString();
			GeoserverLayerStyles geoserverLayerStyles = new GeoserverLayerStyles(styleName, styleTitle, styleType);
			if (styleName.equalsIgnoreCase(colorByColumn))
				styles.add(0, geoserverLayerStyles);
			else
				styles.add(geoserverLayerStyles);
		}
		return styles;
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

		String styleType = columnType.startsWith(CHARACTER) || columnType.equalsIgnoreCase("text") ? CATEGORICAL
				: INTERVAL;

		List<StyledLayer> layers = getStyledLayers(layerName, layerType, columnName, styleType, stops);

		return new JsonStyle(VERSION, sources, layers);
	}

	private Map<String, StyledSource> getSource(String layerName) {
		Map<String, StyledSource> sources = new HashMap<>();
		String type = "vector";
		String scheme = "tms";
		List<String> tiles = new ArrayList<>();
		tiles.add("/geoserver/gwc/service/tms/1.0.0/biodiv:" + layerName + "@EPSG%3A900913@pbf/{z}/{x}/{y}.pbf");
		StyledSource styledSource = new StyledSource(type, scheme, tiles);
		sources.put(layerName, styledSource);
		return sources;
	}

	private List<List<Object>> getStops(String layerName, String columnName, String columnType) {
		List<List<Object>> stops = new ArrayList<>();
		if (columnType.startsWith(CHARACTER) || columnType.equalsIgnoreCase("text")) {
			List<Object[]> values = geoserverStyleDao.getDistinctValues(layerName, columnName);
			for (Object object : values) {
				String color = getRandColor();
				List<Object> stop = new ArrayList<>();
				if(object == null)
					stop.add("No Data");
				else
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
				List<Object> stop = new ArrayList<>();
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
		int rndr = ThreadLocalRandom.current().nextInt(150, 400 + 1);//NOSONAR

		double r = Math.floor(rndr * GOLDEN_RATIO_CONJUGATE);

		int rndg = ThreadLocalRandom.current().nextInt(150, 400 + 1);//NOSONAR
		double g = Math.floor(rndg * GOLDEN_RATIO_CONJUGATE);

		int rndb = ThreadLocalRandom.current().nextInt(150, 400 + 1);//NOSONAR
		double b = Math.floor(rndb * GOLDEN_RATIO_CONJUGATE);

		return String.format("%x%x%x", (int) r, (int) g, (int) b);
	}

	private List<StyledLayer> getStyledLayers(String layerName, LayerType layerType, String columnName,
			String styleType, List<List<Object>> stops) {
		JsonStyle jsonStyle = new JsonStyle();
		List<StyledLayer> layers = new ArrayList<>();
		StyleColor styleColor = new StyleColor(columnName, styleType, stops);
		StylePaint paint;
		String geoType = "";

		switch (layerType) {
		case MULTIPOLYGON:
			String fillOutlineColor = "#aaaaaa";
			Double fillOpacity = OPACITY;
			paint = new FillLayerPaint(fillOutlineColor, fillOpacity, styleColor);
			geoType = GEO_TYPE_FILL;
			break;
		case POINT:
		case MULTIPOINT:
			Double circleRadius = 5.0;
			Double circleOpacity = OPACITY;
			paint = new CircleLayerPaint(circleRadius, circleOpacity, styleColor);
			geoType = GEO_TYPE_CIRCLE;
			break;
		case MULTILINESTRING:
			Double lineWidth = 1.0;
			paint = new LineLayerPaint(lineWidth, styleColor);
			geoType = GEO_TYPE_LINE;
			break;
		default:
			return new ArrayList<>();
		}

		StyledLayer styledLayer = new StyledLayer(layerName, geoType, layerName, layerName, paint);
		layers.add(styledLayer);
		jsonStyle.setLayers(layers);
		return layers;
	}

	@Override
	public void unpublishAllStyles(String layerName, String workspace) {
		List<GeoserverLayerStyles> styles = fetchAllStyles(layerName);
		GeoServerRESTStyleManager styleManager = geoserverService.getManager().getStyleManager();
		for(GeoserverLayerStyles geoserverLayerStyles : styles) {
			String styleName = geoserverLayerStyles.getStyleName();
			styleName = layerName + "_" + styleName;
			styleManager.removeStyleInWorkspace(workspace, styleName);
		}
	}
	
	@Override
	public List<String> publishAllStyles(String workspace) throws IOException {
		List<MetaLayer> metaLayers = metaLayerService.findAll(null, -1, -1);
		List<String> stylesUpdated = new ArrayList<>();
		for(MetaLayer metaLayer : metaLayers) {
			List<String> styles = publishAllStyles(metaLayer.getLayerTableName(), workspace);
			stylesUpdated.addAll(styles);
		}
		return stylesUpdated;
	}
	
	@Override
	public List<String> publishAllStyles(String layerName, String workspace) throws IOException {
		
		String layerTableName = metaLayerService.isTableAvailable(layerName);
		if(layerTableName == null)
			throw new IOException("Could not find the table name");
		
		List<Object[]> columnNameTypes = geoserverStyleDao.getColumnTypes(layerTableName);
		List<String> styles = new ArrayList<>();
		for (Object[] columnNameType : columnNameTypes) {
			String columnName = columnNameType[0].toString();
			String columnType = columnNameType[1].toString();

			if (columnName.startsWith("__mlocate") || columnName.equalsIgnoreCase("ogc_fid")
					|| (!columnType.startsWith(CHARACTER) && !ALLOWED_TYPE.contains(columnType)))
				continue;

			String styleName = layerTableName + "_" + columnName;

			JsonStyle jsonStyle = generateJsonStyle(layerTableName, columnName);
			publishStyleOnGeoserver(styleName, workspace);
			copyMBStyleToGeoserver(jsonStyle, styleName + ".json", workspace);

			styleName = workspace + ":" + styleName;
			styles.add(styleName);
		}
		return styles;
	}

	public byte[] publishStyleOnGeoserver(String styleName, String workspace) {
		String styleUri;

		if (workspace == null)
			styleUri = "rest/styles";
		else
			styleUri = "rest/workspaces/" + workspace + "/styles";

		byte[] bytes = null;
		try {
			MBStyle mbStyle = new MBStyle(styleName, styleName + ".json", "mbstyle");
			bytes = geoserverService.postRequest(styleUri, objectMapper.writeValueAsString(mbStyle), "application/json",
					null);

		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
		}
		return bytes;
	}

	private void copyMBStyleToGeoserver(JsonStyle jsonStyle, String styleName, String workspace) throws IOException {

		String geoserverStyleFilePath = geoserverDataDirectory + File.separator;

		if (workspace == null)
			geoserverStyleFilePath += "styles";
		else
			geoserverStyleFilePath += "workspaces" + File.separator + workspace + File.separator + "styles";

		geoserverStyleFilePath += File.separator + styleName;

		File file = new File(geoserverStyleFilePath);
		objectMapper.writeValue(file, jsonStyle);
	}

}
