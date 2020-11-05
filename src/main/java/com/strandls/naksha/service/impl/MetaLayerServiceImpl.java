package com.strandls.naksha.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;
import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.pac4j.core.profile.CommonProfile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.dao.MetaLayerDao;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.OGR2OGR;
import com.strandls.naksha.pojo.enumtype.DownloadAccess;
import com.strandls.naksha.pojo.enumtype.LayerStatus;
import com.strandls.naksha.pojo.request.LayerFileDescription;
import com.strandls.naksha.pojo.request.MetaData;
import com.strandls.naksha.pojo.response.ObservationLocationInfo;
import com.strandls.naksha.pojo.response.TOCLayer;
import com.strandls.naksha.service.AbstractService;
import com.strandls.naksha.service.GeoserverService;
import com.strandls.naksha.service.GeoserverStyleService;
import com.strandls.naksha.service.MetaLayerService;
import com.strandls.naksha.utils.MetaLayerUtil;
import com.strandls.user.ApiException;
import com.strandls.user.controller.UserServiceApi;
import com.strandls.user.pojo.UserIbp;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

import it.geosolutions.geoserver.rest.decoder.RESTLayer;

public class MetaLayerServiceImpl extends AbstractService<MetaLayer> implements MetaLayerService {

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private GeoserverService geoserverService;

	@Inject
	private GeoserverStyleService geoserverStyleService;

	@Inject
	private UserServiceApi userServiceApi;

	@Inject
	private MetaLayerDao metaLayerDao;

	@Inject
	private GeometryFactory geoFactory;

	public static final String DOWNLOAD_BASE_LOCATION = NakshaConfig.getString(MetaLayerUtil.TEMP_DIR_PATH)
			+ File.separator + "temp_zip";

	@Inject
	public MetaLayerServiceImpl(MetaLayerDao dao) {
		super(dao);
	}

	@Override
	public MetaLayer findByLayerTableName(String layerName) {
		return findByPropertyWithCondtion("layerTableName", layerName, "=");
	}

	@Override
	public List<TOCLayer> getTOCList(HttpServletRequest request, Integer limit, Integer offset)
			throws ApiException, com.vividsolutions.jts.io.ParseException, URISyntaxException {

		CommonProfile userProfile = AuthUtil.getProfileFromRequest(request);

		List<MetaLayer> metaLayers = findAll(request, limit, offset);
		List<TOCLayer> layerLists = new ArrayList<TOCLayer>();
		for (MetaLayer metaLayer : metaLayers) {
			Long authorId = metaLayer.getUploaderUserId();

			UserIbp userIbp = userServiceApi.getUserIbp(authorId + "");

			Boolean isDownloadable = false;
			if (userIbp.getIsAdmin() || DownloadAccess.ALL.equals(metaLayer.getDownloadAccess())
					|| (userProfile != null && userProfile.getId().equals(authorId.toString()))
					|| checkDownLoadAccess(userProfile, metaLayer))
				isDownloadable = true;

			List<List<Double>> bbox = getBoundingBox(metaLayer);
			String thumbnail = getThumbnail(request, metaLayer, bbox);
			TOCLayer tocLayer = new TOCLayer(metaLayer, userIbp, isDownloadable, bbox, thumbnail);
			layerLists.add(tocLayer);
		}
		return layerLists;
	}

	private String getThumbnail(HttpServletRequest request, MetaLayer metaLayer, List<List<Double>> bbox)
			throws URISyntaxException {
		String bboxValue = bbox.get(0).get(0) + "," + bbox.get(0).get(1) + "," + bbox.get(1).get(0) + ","
				+ bbox.get(1).get(1);

		String uri = ApiConstants.GEOSERVER + ApiConstants.THUMBNAILS + "/" + MetaLayerService.WORKSPACE + "/"
				+ metaLayer.getLayerTableName();

		URIBuilder builder = new URIBuilder(uri);

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("layers", metaLayer.getLayerTableName()));
		params.add(new BasicNameValuePair("bbox", bboxValue));
		params.add(new BasicNameValuePair("request", "GetMap"));
		params.add(new BasicNameValuePair("service", "WMS"));
		params.add(new BasicNameValuePair("version", "1.1.0"));
		params.add(new BasicNameValuePair("format", "image/gif"));

		if (params != null)
			builder.setParameters(params);
		return builder.build().toString();
	}

	private List<List<Double>> getBoundingBox(MetaLayer metaLayer) throws com.vividsolutions.jts.io.ParseException {
		String bbox = metaLayerDao.getBoundingBox(metaLayer.getLayerTableName());

		WKTReader reader = new WKTReader(geoFactory);
		Geometry topology = reader.read(bbox);

		Geometry envelop = topology.getEnvelope();

		Double top = envelop.getCoordinates()[0].x, left = envelop.getCoordinates()[0].y,
				bottom = envelop.getCoordinates()[2].x, right = envelop.getCoordinates()[2].y;

		List<List<Double>> boundingBox = new ArrayList<List<Double>>();

		List<Double> topLeft = new ArrayList<Double>();
		List<Double> bottomRight = new ArrayList<Double>();

		topLeft.add(top);
		topLeft.add(left);

		bottomRight.add(bottom);
		bottomRight.add(right);

		boundingBox.add(topLeft);
		boundingBox.add(bottomRight);

		return boundingBox;
	}

	private boolean checkDownLoadAccess(CommonProfile userProfile, MetaLayer metaLayer) {
		// TODO : Need to add permission with the download access
		return true;
	}

	@Override
	public List<MetaLayer> findAll(HttpServletRequest request, Integer limit, Integer offset) {
		List<MetaLayer> metaLayers;
		if (limit == -1 || offset == -1)
			metaLayers = findAll();
		else
			metaLayers = findAll(limit, offset);
		return metaLayers;
	}

	public void uploadGeoTiff(String geoLayerName, String inputGeoTiffFileLocation, Map<String, Object> result)
			throws UnsupportedEncodingException, IOException, ParseException {
		File inputGeoTiffFile = new File(inputGeoTiffFileLocation);
		boolean isPublished = geoserverService.publishGeoTiffLayer(WORKSPACE, geoLayerName, inputGeoTiffFile);

		if (!isPublished) {
			throw new IOException("Geoserver publication of layer failed");
		}
		result.put("Uplaoded on geoserver", geoLayerName);
		RESTLayer layer = geoserverService.getManager().getReader().getLayer(WORKSPACE, geoLayerName);
		result.put("Geoserver layer url", layer.getResourceUrl());
	}

	@Override
	public Map<String, Object> uploadLayer(HttpServletRequest request, FormDataMultiPart multiPart)
			throws IOException, ParseException, InvalidAttributesException, InterruptedException {
		Map<String, Object> result = new HashMap<String, Object>();

		String jsonString = MetaLayerUtil.getMetadataAsJson(multiPart).toJSONString();
		MetaData metaData = objectMapper.readValue(jsonString, MetaData.class);
		CommonProfile profile = AuthUtil.getProfileFromRequest(request);
		long uploaderUserId = Long.parseLong(profile.getId());
		Map<String, String> layerColumnDescription = metaData.getLayerColumnDescription();
		LayerFileDescription layerFileDescription = metaData.getLayerFileDescription();
		String fileType = layerFileDescription.getFileType();

		Map<String, String> copiedFiles;
		String ogrInputFileLocation;
		String layerName;

		if ("shp".equals(fileType)) {
			copiedFiles = MetaLayerUtil.copyFiles(multiPart);
			ogrInputFileLocation = copiedFiles.get("shp");
			layerName = multiPart.getField("shp").getContentDisposition().getFileName().split("\\.")[0].toLowerCase();
		} else if ("csv".equals(fileType)) {
			copiedFiles = MetaLayerUtil.copyCSVFile(multiPart, layerFileDescription);
			ogrInputFileLocation = copiedFiles.get("vrt");
			layerName = multiPart.getField("csv").getContentDisposition().getFileName().split("\\.")[0].toLowerCase();
		} else if ("tif".equals(fileType)) {
			copiedFiles = MetaLayerUtil.copyGeneralFile(multiPart, "tif", false);
			ogrInputFileLocation = copiedFiles.get("tif");
			layerName = multiPart.getField("tif").getContentDisposition().getFileName().split("\\.")[0].toLowerCase();
		} else {
			throw new IllegalArgumentException("Invalid file type");
		}

		String dirPath = copiedFiles.get("dirPath");
		result.put("Files copied to", dirPath);
		metaData.setDirPath(dirPath);

		MetaLayer metaLayer = new MetaLayer(metaData, uploaderUserId);
		metaLayer = save(metaLayer);
		result.put("Meta layer table entry", metaLayer.getId());

		String layerTableName = "lyr_" + metaLayer.getId() + "_" + layerName;
		metaLayer.setLayerTableName(layerTableName);
		update(metaLayer);

		if ("tif".equals(fileType)) {
			uploadGeoTiff(layerTableName, ogrInputFileLocation, result);
			return result;
		}

		createDBTable(layerTableName, ogrInputFileLocation, layerColumnDescription, layerFileDescription, result);

		List<String> keywords = new ArrayList<String>();
		keywords.add(layerTableName);

		List<String> styles = geoserverStyleService.publishAllStyles(layerTableName, WORKSPACE);
		boolean isPublished = geoserverService.publishLayer(WORKSPACE, DATASTORE, layerTableName, null, layerTableName,
				keywords, styles);
		if (!isPublished) {
			throw new IOException("Geoserver publication of layer failed");
		}

		metaLayer.setLayerStatus(LayerStatus.PENDING);
		update(metaLayer);

		result.put("Uplaoded on geoserver", layerTableName);
		RESTLayer layer = geoserverService.getManager().getReader().getLayer(WORKSPACE, layerTableName);
		result.put("Geoserver layer url", layer.getResourceUrl());
		return result;
	}

	private void createDBTable(String layerTableName, String ogrInputFileLocation,
			Map<String, String> layerColumnDescription, LayerFileDescription layerFileDescription,
			Map<String, Object> result) throws InvalidAttributesException, InterruptedException, IOException {

		String encoding = layerFileDescription.getEncoding();

		OGR2OGR ogr2ogr = new OGR2OGR(OGR2OGR.SHP_TO_POSTGRES, null, layerTableName, "precision=NO", null,
				ogrInputFileLocation, encoding);

		Process process = ogr2ogr.execute();
		if (process == null) {
			throw new IOException("Layer upload on the postgis failed");
		} else {
			process.waitFor();
			result.put("Table created for layer", layerTableName);
		}
		process = ogr2ogr.addColumnDescription(layerTableName, layerColumnDescription);
		if (process == null) {
			throw new IOException("Comment could not be added to table");
		} else {
			process.waitFor();
			result.put("Comments added", "success");
		}
	}

	@Override
	public void prepareDownloadLayer(String uri, String hashKey, String jsonString)
			throws InvalidAttributesException, InterruptedException, IOException {

		JSONObject jsonObject = new JSONObject(jsonString);

		String layerName = jsonObject.getString("layerName");

		List<String> attributeList = new ArrayList<String>();
		JSONArray attributeArray = jsonObject.getJSONArray("attributeList");
		attributeArray.forEach(jO -> {
			attributeList.add(jO.toString());
		});

		JSONArray filterArray = jsonObject.getJSONArray("filterArray");
		filterArray.forEach(fA -> {

		});

		File directory = new File(DOWNLOAD_BASE_LOCATION);
		if (!directory.exists()) {
			directory.mkdir();
		}

		String shapeFileDirectoryPath = DOWNLOAD_BASE_LOCATION + File.separator + hashKey;
		File shapeFileDirectory = new File(shapeFileDirectoryPath);
		if (!shapeFileDirectory.exists()) {
			shapeFileDirectory.mkdir();
		}

		shapeFileDirectoryPath += File.separator + layerName;
		shapeFileDirectory = new File(shapeFileDirectoryPath);
		if (!shapeFileDirectory.exists()) {
			shapeFileDirectory.mkdir();
		}

		String attributeString = "";
		if (attributeList.size() > 0) {
			for (String attribute : attributeList) {
				attributeString += attribute + ", ";
			}
			attributeString += "wkb_geometry ";
		} else
			attributeString = "*";
		String query = "select " + attributeString + " from " + layerName;

		shapeFileDirectoryPath = shapeFileDirectory.getAbsolutePath();
		OGR2OGR ogr2ogr = new OGR2OGR(OGR2OGR.POSTGRES_TO_SHP, null, layerName, null, query, shapeFileDirectoryPath,
				null);
		Process process = ogr2ogr.execute();
		if (process == null) {
			throw new IOException("Shape file creation failed");
		} else {
			process.waitFor();
		}

		String zipFileLocation = shapeFileDirectoryPath + ".zip";

		zipFolder(zipFileLocation, shapeFileDirectory);

		System.out.println(uri + "/" + hashKey + "/" + layerName);

		// TODO : send mail notification for download url
		// return directory.getAbsolutePath();
	}

	public void zipFolder(String zipFileLocation, File fileDirectory) throws IOException {
		FileOutputStream fos = new FileOutputStream(zipFileLocation);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		for (File fileToZip : fileDirectory.listFiles()) {
			FileInputStream fis = new FileInputStream(fileToZip);
			ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
			zipOut.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}
			fis.close();
		}
		zipOut.close();
		fos.close();
	}

	@Override
	public String getFileLocation(String hashKey, String layerName) {
		return DOWNLOAD_BASE_LOCATION + File.separator + hashKey + File.separator + layerName + ".zip";
	}

	@Override
	public String removeLayer(String layerName) {
		// TODO : Remove the copied files from the file system. (Need to take a call on
		// this)
		// TODO : Delete table from the database
		// TODO : Mark the entry in the metalayer as inactive.
		// TODO : remove-publish layer from the geoserver
		return "";
	}

	@Override
	public ObservationLocationInfo getLayerInfo(String lon, String lat) {

		String soil = getAttributeValueAtLatlon("descriptio", INDIA_SOIL, lon, lat);
		String temp = getAttributeValueAtLatlon("temp_c", INDIA_TEMPERATURE, lon, lat);
		String rainfall = getAttributeValueAtLatlon("rain_range", INDIA_RAINFALLZONE, lon, lat);
		String tahsil = getAttributeValueAtLatlon("tahsil", INDIA_TAHSIL, lon, lat);
		String forestType = getAttributeValueAtLatlon("type_desc", INDIA_FOREST_TYPE, lon, lat);

		return new ObservationLocationInfo(soil, temp, rainfall, tahsil, forestType);
	}

	private String getAttributeValueAtLatlon(String attribute, String layerName, String lon, String lat) {

		String queryStr = "SELECT " + attribute + " from " + layerName + " where st_contains" + "(" + layerName + "."
				+ MetaLayerService.GEOMETRY_COLUMN_NAME + ", ST_GeomFromText('POINT(" + lon + " " + lat + ")',0))";
		List<Object> result = metaLayerDao.executeQueryForSingleResult(queryStr);

		if (result.size() == 0)
			return null;

		return result.get(0).toString();
	}
}
