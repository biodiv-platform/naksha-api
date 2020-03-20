package com.strandls.naksha.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.strandls.naksha.dao.MetaLayerDao;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.OGR2OGR;
import com.strandls.naksha.service.AbstractService;
import com.strandls.naksha.service.MetaLayerService;
import com.strandls.naksha.utils.MetaLayerUtil;
import com.sun.jersey.multipart.FormDataMultiPart;

public class MetaLayerServiceImpl extends AbstractService<MetaLayer> implements MetaLayerService {

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	public MetaLayerServiceImpl(MetaLayerDao dao) {
		super(dao);
	}

	@Override
	public Map<String, String> upload(HttpServletRequest request, FormDataMultiPart multiPart) throws IOException, ParseException, InvalidAttributesException, InterruptedException {

		Map<String, String> copiedFiles = MetaLayerUtil.copyFiles(multiPart);
		String dirPath = copiedFiles.get("dirPath");
		
		JSONObject jsonObject = MetaLayerUtil.getMetadataAsJson(multiPart);
		JSONObject layerColumnDescription = (JSONObject) jsonObject.remove("$layerColumnDescription");

		
		
		MetaLayer metaLayer = objectMapper.readValue(jsonObject.toJSONString(), MetaLayer.class);
		metaLayer.setDirPath(dirPath);
		save(metaLayer);

		String shpFile = copiedFiles.get("shp");
		String nlt = "PROMOTE_TO_MULTI";
		String nln = "";
		String lco = "";
		String query = null;
		OGR2OGR ogr2ogr = new OGR2OGR(OGR2OGR.SHP_TO_POSTGRES, nlt, nln, lco, query, shpFile);
		ogr2ogr.execute();
		
		
		//return command;

		//int i = layerService.uploadShpLayer(shpInputStream, dbfInputStream, metadataInputStream, shxInputStream,
		//		layerName);

		// Waiting for disk files to be created then reload layers
		//TimeUnit.SECONDS.sleep(5);
		//service.getRequest("/rest/reload", null, "POST");

		Map<String, String> result = new HashMap<String, String>();
		result.put("success", "File upload succesful");
		return result;
	}

}
