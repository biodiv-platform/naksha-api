package com.strandls.naksha.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.dao.MetaLayerDao;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.OGR2OGR;
import com.strandls.naksha.service.AbstractService;
import com.strandls.naksha.service.GeoserverService;
import com.strandls.naksha.service.MetaLayerService;
import com.strandls.naksha.utils.MetaLayerUtil;
import com.sun.jersey.multipart.FormDataMultiPart;

import it.geosolutions.geoserver.rest.decoder.RESTLayer;

public class MetaLayerServiceImpl extends AbstractService<MetaLayer> implements MetaLayerService {

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private GeoserverService geoserverService;

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
	public List<MetaLayer> findAll(HttpServletRequest request, Integer limit, Integer offset) {
		List<MetaLayer> metaLayers;
		if (limit == -1 || offset == -1)
			metaLayers = findAll();
		else
			metaLayers = findAll(limit, offset);
		return metaLayers;
	}

	@Override
	public Map<String, Object> uploadLayer(HttpServletRequest request, FormDataMultiPart multiPart)
			throws IOException, ParseException, InvalidAttributesException, InterruptedException {
		Map<String, Object> result = new HashMap<String, Object>();
		
		String jsonString = MetaLayerUtil.getMetadataAsJson(multiPart).toJSONString();
		JSONObject jsonObject = new JSONObject(jsonString);
		JSONObject layerColumnDescription = (JSONObject) jsonObject.remove("$layerColumnDescription");
		JSONObject layerFileDescription = (JSONObject) jsonObject.remove("$layerFileDescription");

		String fileType = layerFileDescription.getString("fileType");
		Map<String, String> copiedFiles;
		String ogrInputFileLocation;
		String layerName;
		
		if("shp".equals(fileType)) {
			copiedFiles = MetaLayerUtil.copyFiles(multiPart);
			ogrInputFileLocation = copiedFiles.get("shp");
			layerName = multiPart.getField("shp").getContentDisposition().getFileName().split("\\.")[0]
					.toLowerCase();
		} else if("csv".equals(fileType)) {
			copiedFiles = MetaLayerUtil.copyCSVFile(multiPart, layerFileDescription);
			ogrInputFileLocation = copiedFiles.get("vrt");
			layerName = multiPart.getField("csv").getContentDisposition().getFileName().split("\\.")[0]
					.toLowerCase();
		} else {
			throw new IllegalArgumentException("Invalid file type");
		}
		
		String dirPath = copiedFiles.get("dirPath");
		result.put("Files copied to", dirPath);
				
		MetaLayer metaLayer = objectMapper.readValue(jsonObject.toString(), MetaLayer.class);
		metaLayer.setDirPath(dirPath);
		metaLayer = save(metaLayer);
		result.put("Meta layer table entry", metaLayer.getId());
		
		String layerTableName = "lyr_" + metaLayer.getId() + "_" + layerName;
		metaLayer.setLayerTableName(layerTableName);
		update(metaLayer);

		OGR2OGR ogr2ogr = new OGR2OGR(OGR2OGR.SHP_TO_POSTGRES, null, layerTableName, null, null,
				ogrInputFileLocation);
		
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

		List<String> keywords = new ArrayList<String>();
		keywords.add(layerTableName);
		boolean isPublished = geoserverService.publishLayer(WORKSPACE, DATASTORE, layerTableName, null, layerTableName,
				keywords);
		if (!isPublished) {
			throw new IOException("Geoserver publication of layer failed");
		}
		result.put("Uplaoded on geoserver", layerTableName);
		RESTLayer layer = geoserverService.getManager().getReader().getLayer(WORKSPACE, layerTableName);
		result.put("Geoserver layer url", layer.getResourceUrl());
		return result;
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
		OGR2OGR ogr2ogr = new OGR2OGR(OGR2OGR.POSTGRES_TO_SHP, null, layerName, null, query, shapeFileDirectoryPath);
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
}
