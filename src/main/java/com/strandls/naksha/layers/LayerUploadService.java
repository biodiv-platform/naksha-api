package com.strandls.naksha.layers;

import static com.strandls.naksha.layers.scripts.gen_geoserver_cache_conf.generate_cache;
import static com.strandls.naksha.layers.scripts.generate_geoserver_layers.geoserver_func;
import static com.strandls.naksha.layers.scripts.generate_geoserver_styles.generate_styles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.script.ScriptException;

import org.apache.commons.io.FileUtils;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.dao.LayerDAO;
import com.strandls.naksha.layers.scripts.Import_data;
import com.strandls.naksha.pojo.ObservationLocationInfo;

public class LayerUploadService {

	@Inject
	private LayerDAO layerDao;

	private final Logger logger = LoggerFactory.getLogger(LayerUploadService.class);
	private static final String TEMP_DIR_PATH = "tmpDir.path";
	private static final String GEOSERVER_DBNAME = "geoserver.dbname";
	private static final String GEOSERVER_DBUSER = "geoserver.dbuser";
	private static final String GEOSERVER_PASS = "db.password";
	static String dataPath = NakshaConfig.getString(TEMP_DIR_PATH) + File.separator + System.currentTimeMillis();
	static String tmpDirPath = dataPath + File.separator + "final";
	static String FilePath;
	boolean isCsvFile;
	Import_data impo = new Import_data();
	String dbname = NakshaConfig.getString(GEOSERVER_DBNAME);
	String dbuser = NakshaConfig.getString(GEOSERVER_DBUSER);
	String dbpassword = NakshaConfig.getString(GEOSERVER_PASS);

	public int uploadShpLayer(InputStream shpInputStream, InputStream dbfInputStream, InputStream metadataInputStream,
			InputStream shxInputStream, String layerName) throws IOException, ClassNotFoundException, SQLException,
			ScriptException, ParseException, InterruptedException {

		String dataPath = NakshaConfig.getString(TEMP_DIR_PATH) + File.separator + System.currentTimeMillis();
		String tmpDirPath = dataPath + File.separator + "final";
		String shpFilePath = tmpDirPath + File.separator + layerName + ".shp";
		File shpFile = new File(shpFilePath);
		String dbfFilePath = tmpDirPath + File.separator + layerName + ".dbf";
		File dbfFile = new File(dbfFilePath);
		String metadataFilePath = tmpDirPath + File.separator + "metadata.txt";
		File metadataFile = new File(metadataFilePath);
		String shxFilePath = tmpDirPath + File.separator + layerName + ".shx";
		File shxFile = new File(shxFilePath);
//		Process p;
		logger.info("Trying to upload shp at {}", dataPath);
		try {
			FileUtils.copyInputStreamToFile(shpInputStream, shpFile);
			FileUtils.copyInputStreamToFile(dbfInputStream, dbfFile);
			FileUtils.copyInputStreamToFile(metadataInputStream, metadataFile);
			FileUtils.copyInputStreamToFile(shxInputStream, shxFile);
			int command = getCommand(dataPath);
			logger.info("Finished upload shp at {}", tmpDirPath);
			return command;

		} catch (IOException e) {
			logger.error("Error while creating data files.", e);
			throw e;
		}
	}

	public int uploadfilelayer(InputStream InputStream, InputStream metadataTInputStream, String layername,
			boolean isCsvFile) throws Exception {
		String metadataFilepath = tmpDirPath + File.separator + layername + "metadata.txt";
		File metadataFile = new File(metadataFilepath);
		FileUtils.copyInputStreamToFile(metadataTInputStream, metadataFile);
		String FilePath;
		int command = 0;
		if (isCsvFile) {
			FilePath = tmpDirPath + File.separator + layername + ".csv";
			File csvFile = new File(FilePath);
			FileUtils.copyInputStreamToFile(InputStream, csvFile);
			command = getcsvcommand(dataPath, FilePath);
		} else {
			FilePath = tmpDirPath + File.separator + layername + ".xlsx";
			File xlsxFile = new File(FilePath);
			FileUtils.copyInputStreamToFile(InputStream, xlsxFile);
			command = xlsxcommand(dataPath, FilePath);
		}
		logger.info("trying to upload csv file", dataPath);
		return command;

	}

//	private void logScriptOutput(Process p) throws IOException {
//		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
//		BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//		String s;
//		while ((s = stdInput.readLine()) != null) {
//			logger.info(s);
//		}
//		while ((s = stdError.readLine()) != null) {
//			logger.error(s);
//		}
//	}

	public void geoserver() throws Exception, SQLException, IOException {
		generate_styles(dbname, dbuser, dbpassword);
		geoserver_func(dbname, dbuser, dbpassword);
		generate_cache(dbname, dbuser, dbpassword);
	}

	private int getCommand(String dataPath) throws ClassNotFoundException, SQLException, IOException, ScriptException,
			ParseException, InterruptedException {
		String dbname = NakshaConfig.getString(GEOSERVER_DBNAME);
		String dbuser = NakshaConfig.getString(GEOSERVER_DBUSER);
		String dbpassword = NakshaConfig.getString(GEOSERVER_PASS);
		Import_data impo = new Import_data();
		int j = impo.main_data(dbname, dbuser, dataPath, dbpassword);
		return j;
	}

	private int getcsvcommand(String dataPath, String FilePath) throws Exception {
		isCsvFile = true;
		int s = impo.csvexcelmain_data(dbname, dbuser, dbpassword, dataPath, FilePath, isCsvFile);
		geoserver();
		return s;
	}

	private int xlsxcommand(String dataPath, String FilePath) throws Exception {
		isCsvFile = false;
		int s = impo.csvexcelmain_data(dbname, dbuser, dbpassword, dataPath, FilePath, isCsvFile);
		geoserver();
		return s;
	}

	public ObservationLocationInfo getLayerDetails(Double lat, Double lon) {
		ObservationLocationInfo locInfo = layerDao.getLayersDetails(lat, lon);
		return locInfo;
	}

//	public List<LayerAttributes> getLayerAttributes(String layerName) {
//		return layerDAO.getLayerAttributes(layerName);
//	}
//
//	public List<String> getLayerNamesWithTag(String tag) {
//		if (tag == null || tag.isEmpty())
//			return new ArrayList<>();
//
//		return layerDAO.getLayerNamesWithTag(tag);
//	}

}