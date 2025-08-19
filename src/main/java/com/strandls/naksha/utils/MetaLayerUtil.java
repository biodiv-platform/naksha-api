package com.strandls.naksha.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.pojo.request.LayerFileDescription;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class MetaLayerUtil {

	private static final Logger logger = LoggerFactory.getLogger(MetaLayerUtil.class);

	private MetaLayerUtil() {
	}

	public static final String TEMP_DIR_PATH = "tmpDir.path";
	public static final String TEMP_DIR_GEOSERVER_PATH = "tmpDirGeoserverPath";
	private static final String DIR_PATH = "dirPath";
	private static final String FINAL = "final";

	private static final String[] COMPULSORY_EXTENSIONS = { "shp", "dbf", "shx" };
	private static final String[] RASTER_COMPULSORY_EXTENSIONS = { "tif" };
	private static final String[] RASTER_OPTNAL_EXTENSIONS = { "sld" };
	private static final String[] OPTIONAL_EXTENSIONS = { "prj", "sbn", "sbx", "fbn", "fbx", "ain", "aih", "ixs", "mxs",
			"atx", "shp.xml", "cpg", "qix" };

	private static String createVRTFileContent(String layerName, String csvFilePath,
			LayerFileDescription layerFileDescription) {

		String vrtFileContent = "<OGRVRTDataSource>" + "\n";
		vrtFileContent += "\t" + "<OGRVRTLayer name=\"" + layerName + "\">" + "\n";
		vrtFileContent += "\t" + "<SrcDataSource>" + csvFilePath + "</SrcDataSource>" + "\n";
		vrtFileContent += "\t" + "<GeometryType>" + layerFileDescription.getGeoColumnType() + "</GeometryType>" + "\n";
		vrtFileContent += "\t" + "<LayerSRS>" + layerFileDescription.getLayerSRS() + "</LayerSRS>" + "\n";

		String encoding = layerFileDescription.getEncoding();
		if ("PointFromColumns".equals(encoding)) {
			vrtFileContent += "\t" + "<GeometryField encoding=\"" + encoding + "\" x=\""
					+ layerFileDescription.getLonColumnName() + "\" y=\"" + layerFileDescription.getLatColumnName()
					+ "\"/>" + "\n";
		} else if ("WKT".equals(encoding) || "WKB".equals(encoding)) {
			vrtFileContent += "\t" + "<GeometryField encoding=\"" + encoding + "\" field=\""
					+ layerFileDescription.getField() + "\"/>" + "\n";
		}
		vrtFileContent += "\t" + "</OGRVRTLayer>" + "\n";
		vrtFileContent += "</OGRVRTDataSource>";

		return vrtFileContent;
	}

	/**
	 * This method is to copy only CSV file to server.
	 *
	 * @param multiPart
	 * @return location of the copied file
	 * @throws IOException
	 */
	public static Map<String, String> copyCSVFile(FormDataMultiPart multiPart,
			LayerFileDescription layerFileDescription) throws IOException {

		Map<String, String> result = new HashMap<>();
		String dataPath = NakshaConfig.getString(TEMP_DIR_PATH) + File.separator + System.currentTimeMillis();
		String tmpDirPath = dataPath + File.separator + FINAL;

		String location = copyFile(multiPart, "csv", tmpDirPath, false);

		String layerName = multiPart.getField("csv").getContentDisposition().getFileName().split("\\.")[0]
				.toLowerCase();

		result.put("csv", location);

		String vrtFileContent = createVRTFileContent(layerName, location, layerFileDescription);

		String vrtFilePath = createVRTFile(tmpDirPath, layerName, vrtFileContent);
		result.put("vrt", vrtFilePath);

		result.put(DIR_PATH, tmpDirPath);
		return result;
	}

	private static String createVRTFile(String tmpDirPath, String layerName, String vrtFileContent) throws IOException {
		File vrtFile = new File(tmpDirPath + File.separator + layerName + ".vrt");
		if (!vrtFile.exists() && !vrtFile.createNewFile()) {
			throw new IOException("Failed to create file");
		}
		try (FileOutputStream fos = new FileOutputStream(vrtFile)) {
			byte[] bytes = vrtFileContent.getBytes();
			fos.write(bytes);
		}
		return vrtFile.getAbsolutePath();
	}

	/**
	 * This method copy all the file from multipart to server.
	 *
	 * @param multiPart
	 * @return map of file extension to absolute location of the file
	 * @throws IOException
	 */
	public static Map<String, String> copyFiles(FormDataMultiPart multiPart) throws IOException {

		Map<String, String> result = new HashMap<>();
		String dataPath = NakshaConfig.getString(TEMP_DIR_PATH) + File.separator + System.currentTimeMillis();
		String tmpDirPath = dataPath + File.separator + FINAL;

		for (String type : COMPULSORY_EXTENSIONS) {
			String location = copyFile(multiPart, type, tmpDirPath, false);
			result.put(type, location);
		}

		for (String type : OPTIONAL_EXTENSIONS) {
			String location = copyFile(multiPart, type, tmpDirPath, true);
			result.put(type, location);
		}

		result.put(DIR_PATH, tmpDirPath);
		return result;
	}

	public static Map<String, String> copyRasterFiles(FormDataMultiPart multiPart) throws IOException {

		Map<String, String> result = new HashMap<>();
		String dataPath = NakshaConfig.getString(TEMP_DIR_PATH) + File.separator + System.currentTimeMillis();
		String tmpDirPath = dataPath + File.separator + FINAL;

		for (String type : RASTER_COMPULSORY_EXTENSIONS) {
			String location = copyFile(multiPart, type, tmpDirPath, false);
			result.put(type, location);
		}

		for (String type : RASTER_OPTNAL_EXTENSIONS) {
			String location = copyFile(multiPart, type, tmpDirPath, true);
			result.put(type, location);
		}

		result.put(DIR_PATH, tmpDirPath);
		return result;
	}

	public static Map<String, String> copyGeneralFile(FormDataMultiPart multiPart, String type, boolean optional)
			throws IOException {
		String dataPath = NakshaConfig.getString(TEMP_DIR_PATH) + File.separator + System.currentTimeMillis();
		String tmpDirPath = dataPath + File.separator + FINAL;
		String fileLocation = copyFile(multiPart, type, tmpDirPath, optional);

		Map<String, String> result = new HashMap<>();
		result.put(type, fileLocation);
		result.put(FINAL, tmpDirPath);
		return result;
	}

	/**
	 * This method save each file with given extension. If the extension is optional
	 * then file need not be there If the extension is required then it will throw
	 * the exception
	 *
	 * @param multiPart
	 * @param type       - extension type
	 * @param tmpDirPath - directory location to store
	 * @param optional   - extension type ( i.e optional or required.)
	 * @return return the absolute location of file if present otherwise it return
	 *         the null value.
	 * @throws IOException - exception will be thrown if file type is required and
	 *                     it is not present.
	 */
	private static String copyFile(FormDataMultiPart multiPart, String type, String tmpDirPath, boolean optional)
			throws IOException {

		FormDataBodyPart formdata = multiPart.getField(type);
		if (formdata == null) {
			if (optional)
				return null;
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
					.entity(type.toUpperCase() + " file not present").build());
		}
		InputStream inputStream = formdata.getValueAs(InputStream.class);
		String fileName = formdata.getContentDisposition().getFileName();
		fileName += ".";
		String layerName = fileName.split("\\.")[0].toLowerCase();

		layerName = refineLayerName(layerName);

		String filePath = tmpDirPath + File.separator + layerName + "." + type;

		File file = new File(filePath);
		FileUtils.copyInputStreamToFile(inputStream, file);
		return filePath;
	}

	public static String refineLayerName(String layerName) {
		// Remove all the space character
		layerName = layerName.trim().replaceAll("\\s+", "_");

		// Keep only the character and number in the string (along with the underscore)
		layerName = layerName.replaceAll("[^a-zA-Z0-9_]", "");

		return layerName;
	}

	public static void deleteFiles(String dirPath) {
		dirPath = dirPath.substring(0, dirPath.lastIndexOf("/"));
		try {
			FileUtils.deleteDirectory(new File(dirPath));
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * This return the metadata for layer from metadata.json file.
	 *
	 * @param multiPart
	 * @return - json object from the metadata json file.
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static JSONObject getMetadataAsJson(FormDataMultiPart multiPart) throws IOException, ParseException {
		FormDataBodyPart formdata = multiPart.getField("metadata");
		if (formdata == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST).entity("Metadata file not present").build());
		}
		InputStream metaDataInputStream = formdata.getValueAs(InputStream.class);
		InputStreamReader inputStreamReader = new InputStreamReader(metaDataInputStream, StandardCharsets.UTF_8);
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(inputStreamReader);
	}
}
