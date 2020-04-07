package com.strandls.naksha.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.strandls.naksha.NakshaConfig;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

public class MetaLayerUtil {

	public static final String TEMP_DIR_PATH = "tmpDir.path";

	private static final String[] COMPULSORY_EXTENSIONS = { "shp", "dbf", "shx" };
	private static final String[] OPTIONAL_EXTENSIONS = { "prj", "sbn", "sbx", "fbn", "fbx", "ain", "aih", "ixs", "mxs",
			"atx", "shp.xml", "cpg", "qix" };

	/**
	 * This method copy all the file from multipart to server.
	 * @param multiPart
	 * @return map of file extension to absolute location of the file
	 * @throws IOException
	 */
	public static Map<String, String> copyFiles(FormDataMultiPart multiPart) throws IOException {

		Map<String, String> result = new HashMap<String, String>();
		String dataPath = NakshaConfig.getString(TEMP_DIR_PATH) + File.separator + System.currentTimeMillis();
		String tmpDirPath = dataPath + File.separator + "final";

		for(String type : COMPULSORY_EXTENSIONS) {
			String location = copyFile(multiPart, type, tmpDirPath, false);
			result.put(type, location);
		}
		
		for(String type : OPTIONAL_EXTENSIONS) { 
			String location = copyFile(multiPart, type, tmpDirPath, true);
			result.put(type, location);
		}

		result.put("dirPath", tmpDirPath);
		return result;
	}

	/**
	 * This method save each file with given extension.
	 * If the extension is optional then file need not be there
	 * If the extension is required then it will throw the exception
	 * @param multiPart
	 * @param type - extension type
	 * @param tmpDirPath - directory location to store
	 * @param optional - extension type ( i.e optional or required.)
	 * @return return the absolute location of file if present otherwise it return the null value.
	 * @throws IOException - exception will be thrown if file type is required and it is not present.
	 */
	private static String copyFile(FormDataMultiPart multiPart, String type, String tmpDirPath, boolean optional)
			throws IOException {

		FormDataBodyPart formdata = multiPart.getField(type);
		if (formdata == null) {
			if (optional == true)
				return null;
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
					.entity(type.toUpperCase() + " file not present").build());
		}
		InputStream inputStream = formdata.getValueAs(InputStream.class);
		String fileName = formdata.getContentDisposition().getFileName();
		fileName += ".";
		String layerName = fileName.split("\\.")[0].toLowerCase();

		String filePath = tmpDirPath + File.separator + layerName + "." + type;

		File file = new File(filePath);
		FileUtils.copyInputStreamToFile(inputStream, file);
		return filePath;
	}
	
	/**
	 * This return the metadata for layer from metadata.json file.
	 * @param multiPart
	 * @return - json object from the metadata json file.
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static JSONObject getMetadataAsJson(FormDataMultiPart multiPart) throws UnsupportedEncodingException, IOException, ParseException {
		FormDataBodyPart formdata = multiPart.getField("metadata");
		if (formdata == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST).entity("Metadata file not present").build());
		}
		InputStream metaDataInputStream = formdata.getValueAs(InputStream.class);
		InputStreamReader inputStreamReader = new InputStreamReader(metaDataInputStream, "UTF-8");
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(inputStreamReader);
		return jsonObject;
	}
}
