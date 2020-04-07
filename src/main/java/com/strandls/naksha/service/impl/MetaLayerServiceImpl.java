package com.strandls.naksha.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

public class MetaLayerServiceImpl extends AbstractService<MetaLayer> implements MetaLayerService {

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private GeoserverService geoserverService;
	
	public static final String DOWNLOAD_BASE_LOCATION = NakshaConfig.getString(MetaLayerUtil.TEMP_DIR_PATH) + File.separator + "temp_zip";
	
	@Inject
	public MetaLayerServiceImpl(MetaLayerDao dao) {
		super(dao);
	}

	@Override
	public Map<String, String> uploadLayer(HttpServletRequest request, FormDataMultiPart multiPart)
			throws IOException, ParseException, InvalidAttributesException, InterruptedException {
		Map<String, String> result = new HashMap<String, String>();

		Map<String, String> copiedFiles = MetaLayerUtil.copyFiles(multiPart);
		String dirPath = copiedFiles.get("dirPath");

		String jsonString = MetaLayerUtil.getMetadataAsJson(multiPart).toJSONString();
		JSONObject jsonObject = new JSONObject(jsonString);
		
		JSONObject layerColumnDescription = (JSONObject) jsonObject.remove("$layerColumnDescription");

		String layerName = multiPart.getField("shp").getContentDisposition().getFileName().split("\\.")[0]
				.toLowerCase();

		MetaLayer metaLayer = objectMapper.readValue(jsonObject.toString(), MetaLayer.class);
		metaLayer.setDirPath(dirPath);
		metaLayer = save(metaLayer);

		String layerTableName = "lyr_" + metaLayer.getId() + "_" + layerName;

		OGR2OGR ogr2ogr = new OGR2OGR(OGR2OGR.SHP_TO_POSTGRES, null, layerTableName, null, null, copiedFiles.get("shp"));
		int isUploaded = ogr2ogr.execute();
		if( isUploaded == -1) {
			throw new IOException("Layer upload on the postgis failed");
		}
		int isCommentAdded = ogr2ogr.addColumnDescription(layerTableName, layerColumnDescription);
		if( isCommentAdded == -1) {
			throw new IOException("Comment could not be added to table");
		}

		List<String> keywords = new ArrayList<String>();
		keywords.add(layerTableName);
		boolean isPublished = geoserverService.publishLayer(WORKSPACE, DATASTORE, layerTableName, null, layerTableName, keywords);
		if(!isPublished) {
			throw new IOException("Geoserver publication of layer failed");
		}

		result.put("success", "File upload succesful");
		return result;
	}
	
	@Override
	public void prepareDownloadLayer(String uri, String jsonString) throws InvalidAttributesException, InterruptedException, IOException {
		
		JSONObject jsonObject = new JSONObject(jsonString);
		
		String layerName = jsonObject.getString("layerName");
		
		List<String> attributeList = new ArrayList<String>();
		JSONArray attributeArray = jsonObject.getJSONArray("attributeList");
		attributeArray.forEach(jO -> {
			attributeList.add(jO.toString());
		});
		
		JSONArray filterArray = jsonObject.getJSONArray("filterArray");
		filterArray.forEach( fA -> {
			
		});
		
		
		File directory = new File(DOWNLOAD_BASE_LOCATION);
		if(!directory.exists()) {
			directory.mkdir();
		}
		
		String hashKey = UUID.randomUUID().toString();
		String shapeFileDirectoryPath = DOWNLOAD_BASE_LOCATION + File.separator + hashKey;
		File shapeFileDirectory = new File(shapeFileDirectoryPath);
		if(!shapeFileDirectory.exists()) {
			shapeFileDirectory.mkdir();
		}
		
		shapeFileDirectoryPath += File.separator + layerName;
		shapeFileDirectory = new File(shapeFileDirectoryPath);
		if(!shapeFileDirectory.exists()) {
			shapeFileDirectory.mkdir();
		}
		
		String attributeString = "";
		if(attributeList.size() > 0) {
			for(String attribute : attributeList) {
				attributeString += attribute + ", ";
			}
			attributeString += "wkb_geometry ";
		} else 
			attributeString = "*";
		String query = "select " + attributeString + " from " + layerName;
		
		shapeFileDirectoryPath = shapeFileDirectory.getAbsolutePath();
		OGR2OGR ogr2ogr = new OGR2OGR(OGR2OGR.POSTGRES_TO_SHP, null, layerName, null, query, shapeFileDirectoryPath);
		ogr2ogr.execute();
		
		String zipFileLocation = shapeFileDirectoryPath + ".zip";

		FileOutputStream fos = new FileOutputStream(zipFileLocation);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (File fileToZip : shapeFileDirectory.listFiles()) {
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
 
            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
		
		System.out.println(uri + "/" + hashKey + "/" + layerName);
		
		// TODO : send mail notification for download url
		// return directory.getAbsolutePath();
	}
	
	@Override
	public String getFileLocation(String hashKey, String layerName) {
		return DOWNLOAD_BASE_LOCATION + File.separator + hashKey + File.separator + layerName;
	}
	
	@Override
	public String removeLayer(String layerName) {
		// TODO : Remove the copied files from the file system. (Need to take a call on this)
		// TODO : Delete table from the database
		// TODO : Mark the entry in the metalayer as inactive.
		// TODO : remove-publish layer from the geoserver
		return "";
	}
}
