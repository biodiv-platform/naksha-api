package com.strandls.naksha.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// import jakarta.inject.Inject;
// import jakarta.naming.directory.InvalidAttributesException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.ws.rs.BadRequestException;
// import jakarta.ws.rs.core.HttpHeaders;

import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.bouncycastle.pqc.crypto.rainbow.Layer;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.naksha.ApiConstants;
import com.strandls.naksha.Headers;
import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.dao.LayerPortalDao;
import com.strandls.naksha.dao.MetaLayerDao;
import com.strandls.naksha.dao.PortalDao;
import com.strandls.naksha.pojo.MetaLayer;
import com.strandls.naksha.pojo.OGR2OGR;
import com.strandls.naksha.pojo.Portal;
import com.strandls.naksha.pojo.LayerPortalMapping;
import com.strandls.naksha.pojo.enumtype.DownloadAccess;
import com.strandls.naksha.pojo.enumtype.LayerStatus;
import com.strandls.naksha.pojo.enumtype.LayerType;
import com.strandls.naksha.pojo.request.LayerDownload;
import com.strandls.naksha.pojo.request.LayerFileDescription;
import com.strandls.naksha.pojo.request.MetaData;
import com.strandls.naksha.pojo.request.MetaLayerEdit;
import com.strandls.naksha.pojo.response.LocationInfo;
import com.strandls.naksha.pojo.response.ObservationLocationInfo;
import com.strandls.naksha.pojo.response.ObservationLocationInfoBBP;
import com.strandls.naksha.pojo.response.ObservationLocationInfoPA;
import com.strandls.naksha.pojo.response.TOCLayer;
import com.strandls.naksha.service.AbstractService;
import com.strandls.naksha.service.GeoserverService;
import com.strandls.naksha.service.GeoserverStyleService;
import com.strandls.naksha.service.MailService;
import com.strandls.naksha.service.MetaLayerService;
import com.strandls.naksha.utils.MetaLayerUtil;
import com.strandls.naksha.utils.Utils;
import com.strandls.user.ApiException;
import com.strandls.user.controller.UserServiceApi;
import com.strandls.user.pojo.DownloadLogData;
import com.strandls.user.pojo.UserIbp;
import com.strandls.naksha.utils.MessageDigestPasswordEncoder;

import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.HttpHeaders;
import net.minidev.json.JSONArray;

public class MetaLayerServiceImpl extends AbstractService<MetaLayer> implements MetaLayerService {

	private final Logger logger = LoggerFactory.getLogger(MetaLayerServiceImpl.class);

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
	private PortalDao portaldao;

	@Inject
	private LayerPortalDao layerPortalDao;

	@Inject
	private MailService mailService;
	@Inject
	private MessageDigestPasswordEncoder passwordEncoder;

	@Inject
	private Headers headers;

	public static final String DOWNLOAD_BASE_LOCATION = NakshaConfig.getString(MetaLayerUtil.TEMP_DIR_PATH)
			+ File.separator + "temp_zip";

	@Inject
	public MetaLayerServiceImpl(MetaLayerDao dao) {
		super(dao);
	}

	@Override
	public Long getLayerCount(HttpServletRequest request) {
		return metaLayerDao.getLayerCount();
	}

	@Override
	public MetaLayer findByLayerTableName(String layerName) {
		return metaLayerDao.findByLayerTableName(layerName);
	}

	@Override
	public String isTableAvailable(String layerName) {
		return metaLayerDao.isTableAvailable(layerName);
	}

	@Override
	public List<TOCLayer> getTOCList(HttpServletRequest request, Integer limit, Integer offset, boolean showOnlyPending)
			throws ApiException, com.vividsolutions.jts.io.ParseException, URISyntaxException, BadRequestException {

		String portalId = request.getHeader("Portal-Id");

		String apiKeyRecieved = request.getHeader("api-key");

		Portal portal = portaldao.findById(Long.valueOf(portalId));
		String apiKeyStored = portal.getApiKey();

		boolean apikeyIsValid = passwordEncoder.isPasswordValid(apiKeyStored, apiKeyRecieved, null);

		if (!apikeyIsValid) {
			throw new BadRequestException("api key is not valid");
		}

		List<MetaLayer> metaLayers = findAllByPortalId(request, limit, offset, Long.valueOf(portalId));
		List<TOCLayer> layerLists = new ArrayList<>();

		for (MetaLayer metaLayer : metaLayers) {

			Long authorId = metaLayer.getUploaderUserId();

			String downloadAccess = metaLayer.getDownloadAccess().name().toString();
			String uploaderPortalId = metaLayer.getPortalId().toString();

			List<List<Double>> bbox = geoserverService.getBBoxByLayerName(WORKSPACE, metaLayer.getLayerTableName());
			String thumbnail = getThumbnail(metaLayer, bbox);
			TOCLayer tocLayer = new TOCLayer(metaLayer, null, downloadAccess, bbox, thumbnail, authorId.toString(),
					uploaderPortalId);
			layerLists.add(tocLayer);
		}
		return layerLists;
	}

	private String getThumbnail(MetaLayer metaLayer, List<List<Double>> bbox) throws URISyntaxException {
		String bboxValue = bbox.size() > 0
				? bbox.get(0).get(0) + "," + bbox.get(0).get(1) + "," + bbox.get(1).get(0) + "," + bbox.get(1).get(1)
				: "";

		String uri = ApiConstants.GEOSERVER + ApiConstants.THUMBNAILS + "/" + MetaLayerService.WORKSPACE + "/"
				+ metaLayer.getLayerTableName();

		URIBuilder builder = new URIBuilder(uri);

		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("layers", metaLayer.getLayerTableName()));
		params.add(new BasicNameValuePair("bbox", bboxValue));
		params.add(new BasicNameValuePair("request", "GetMap"));
		params.add(new BasicNameValuePair("service", "WMS"));
		params.add(new BasicNameValuePair("version", "1.1.0"));
		params.add(new BasicNameValuePair("format", "image/gif"));

		builder.setParameters(params);
		return builder.build().toString();
	}

	@Override
	public List<MetaLayer> findAll(HttpServletRequest request, Integer limit, Integer offset) {
		return metaLayerDao.findAll(limit, offset);
	}

	public List<MetaLayer> findAllByPortalId(HttpServletRequest request, Integer limit, Integer offset, Long portalId) {
		return metaLayerDao.findAll(limit, offset, portalId);
	}

	public void uploadGeoTiff(String geoLayerName, String inputGeoTiffFileLocation, String inputGeoTiffStyleLocation,
			String styleName, Map<String, Object> result) throws IOException {
		File inputGeoTiffFile = new File(inputGeoTiffFileLocation);

		boolean isPublished = styleName != null
				? geoserverService.publishGeoTiffLayerWithStyle(WORKSPACE, geoLayerName, null, styleName,
						inputGeoTiffFile)
				: geoserverService.publishGeoTiffLayer(WORKSPACE, geoLayerName, inputGeoTiffFile);

		if (!isPublished) {
			throw new IOException("Geoserver publication of layer failed");
		}
		result.put("Uplaoded on geoserver", geoLayerName);
		RESTLayer layer = geoserverService.getManager().getReader().getLayer(WORKSPACE, geoLayerName);
		result.put("Geoserver layer url", layer.getResourceUrl());
	}

	public String uploadSLDStyle(String styleName, String inputStyleFile, Map<String, Object> result)
			throws IOException {
		File inputStyle = new File(inputStyleFile);
		String resultantStyleName = null;
		boolean isPublished = geoserverService.publishGeoTiffStyleLayer(WORKSPACE, styleName, inputStyle);

		if (!isPublished) {
			throw new IOException("Geoserver publication of layer failed");
		}
		resultantStyleName = WORKSPACE + ":" + styleName;
		result.put("Uplaoded SLD on geoserver", styleName);
		result.put("Geoserver layer file name", resultantStyleName);
		return resultantStyleName;
	}

	@Override
	public Map<String, Object> uploadLayer(HttpServletRequest request, FormDataMultiPart multiPart) throws Exception {
		logger.info("🎬 Starting uploadLayer process");
		Map<String, Object> result = new HashMap<>();

		String portalId = request.getHeader("Portal-Id");
		String apiKeyRecieved = request.getHeader("api-key");
		logger.debug("🔑 Auth check for Portal-Id: {}", portalId);

		Portal portal = portaldao.findById(Long.valueOf(portalId));
		String apiKeyStored = portal.getApiKey();

		boolean apikeyIsValid = passwordEncoder.isPasswordValid(apiKeyStored, apiKeyRecieved, null);

		if (!apikeyIsValid) {
			logger.error("❌ API Key validation failed for portal: {}", portalId);
			throw new BadRequestException("api key is not valid");
		}

		logger.info("📄 Parsing metadata and uploader info");
		String jsonString = MetaLayerUtil.getMetadataAsJson(multiPart).toJSONString();
		MetaData metaData = objectMapper.readValue(jsonString, MetaData.class);
		FormDataBodyPart formdata = multiPart.getField("uploaderUserId");
		long uploaderUserId = Long.valueOf(formdata.getValue());

		Map<String, String> layerColumnDescription = metaData.getLayerColumnDescription();
		LayerFileDescription layerFileDescription = metaData.getLayerFileDescription();
		String fileType = layerFileDescription.getFileType();

		Map<String, String> copiedFiles;
		String ogrInputFileLocation;
		String ogrInputStyleFileLocation = null;
		String layerName;

		// --- FILE COPY SECTION ---
		logger.info("📂 Processing file type: {}", fileType);
		if ("shp".equals(fileType)) {
			copiedFiles = MetaLayerUtil.copyFiles(multiPart);
			ogrInputFileLocation = copiedFiles.get("shp");
			layerName = multiPart.getField("shp").getContentDisposition().getFileName().split("\\.")[0].toLowerCase();
		} else if ("csv".equals(fileType)) {
			copiedFiles = MetaLayerUtil.copyCSVFile(multiPart, layerFileDescription);
			ogrInputFileLocation = copiedFiles.get("vrt");
			layerName = multiPart.getField("csv").getContentDisposition().getFileName().split("\\.")[0].toLowerCase();
		} else if ("tif".equals(fileType)) {
			copiedFiles = MetaLayerUtil.copyRasterFiles(multiPart);
			ogrInputFileLocation = copiedFiles.get("tif");
			ogrInputStyleFileLocation = copiedFiles.get("sld");
			layerName = multiPart.getField("tif").getContentDisposition().getFileName().split("\\.")[0].toLowerCase();
		} else {
			logger.error("❌ Unsupported file type: {}", fileType);
			throw new IllegalArgumentException("Invalid file type");
		}

		String dirPath = copiedFiles.get("dirPath");
		logger.info("💾 Files copied to directory: {}", dirPath);
		result.put("Files copied to", dirPath);

		// --- DATABASE RECORD SECTION ---
		logger.info("🗄️ Saving MetaLayer record to database");
		MetaLayer metaLayer = new MetaLayer(metaData, uploaderUserId, dirPath);
		metaLayer.setPortalId(Long.valueOf(portalId));

		metaLayer = save(metaLayer);
		result.put("Meta layer table entry", metaLayer.getId());

		String layerTableName = "lyr_" + metaLayer.getId() + "_" + MetaLayerUtil.refineLayerName(layerName);
		metaLayer.setLayerTableName(layerTableName);
		update(metaLayer);
		logger.info("📝 Table name generated: {}", layerTableName);

		LayerPortalMapping layerPortalMapping = new LayerPortalMapping(metaLayer.getId(), Long.valueOf(portalId));
		layerPortalDao.save(layerPortalMapping);

		// --- RASTER PROCESSING ---
		if ("tif".equals(fileType)) {
			logger.info("🛰️ Starting Raster (TIF) upload to GeoServer");
			try {
				String styleName = ogrInputStyleFileLocation != null
						? uploadSLDStyle(layerTableName, ogrInputStyleFileLocation, result)
						: null;
				logger.debug("🎨 Style name: {}", styleName);
				uploadGeoTiff(layerTableName, ogrInputFileLocation, ogrInputFileLocation, styleName, result);
				logger.info("✅ Raster upload successful");
				return result;
			} catch (Exception e) {
				logger.error("💥 Raster processing failed: {}", e.getMessage(), e);
				MetaLayerUtil.deleteFiles(dirPath);
				metaLayerDao.delete(metaLayer);
				throw new IOException("Table creation failed");
			}
		}

		// --- VECTOR PROCESSING (SHP/CSV) ---
		try {
			logger.info("🏗️ Starting createDBTable (ogr2ogr) for: {}", layerTableName);
			createDBTable(layerTableName, ogrInputFileLocation, layerColumnDescription, layerFileDescription, result);
			logger.info("✅ Database table created successfully");
		} catch (Exception e) {
			logger.error("💥 createDBTable failed for {}: {}", layerTableName, e.getMessage(), e);
			MetaLayerUtil.deleteFiles(dirPath);
			metaLayerDao.delete(metaLayer);
			throw new IOException("Table creation failed");
		}

		// --- GEOSERVER PUBLISHING SECTION ---
		List<String> keywords = new ArrayList<>();
		keywords.add(layerTableName);

		boolean isPublished = false;
		try {
			logger.info("🌍 Finding SRID and publishing styles for {}", layerTableName);
			String srs = metaLayerDao.findSRID(layerTableName);
			logger.debug("🌐 SRID identified: {}", srs);

			List<String> styles = geoserverStyleService.publishAllStyles(layerTableName, WORKSPACE);

			logger.info("📡 Calling GeoServer publishLayer...");
			isPublished = geoserverService.publishLayer(WORKSPACE, DATASTORE, layerTableName, srs, layerTableName,
					keywords, styles);
		} catch (Exception e) {
			logger.error("💥 Geoserver publication exception for {}: {}", layerTableName, e.getMessage(), e);
			isPublished = false;
		}

		if (!isPublished) {
			logger.error("❌ Geoserver publication failed for {}. Rolling back DB and files.", layerTableName);
			MetaLayerUtil.deleteFiles(dirPath);
			metaLayerDao.delete(metaLayer);
			metaLayerDao.dropTable(layerTableName);
			throw new IOException("Geoserver publication of layer failed");
		}

		metaLayer.setLayerStatus(LayerStatus.PENDING);
		update(metaLayer);

		logger.info("🏁 Upload process complete for {}. Fetching GeoServer URL.", layerTableName);
		result.put("Uplaoded on geoserver", layerTableName);

		try {
			RESTLayer layer = geoserverService.getManager().getReader().getLayer(WORKSPACE, layerTableName);
			result.put("Geoserver layer url", layer.getResourceUrl());
			logger.info("🔗 Layer URL: {}", layer.getResourceUrl());
		} catch (Exception e) {
			logger.warn("⚠️ Could not fetch Resource URL from GeoServer, but layer was published.");
		}

		return result;
	}

	private void createDBTable(String layerTableName, String ogrInputFileLocation,
			Map<String, String> layerColumnDescription, LayerFileDescription layerFileDescription,
			Map<String, Object> result) throws IllegalArgumentException, InterruptedException, IOException {

		logger.info("🎬 [createDBTable] Starting DB table generation pipeline for table: {}", layerTableName);
		logger.info("📁 [createDBTable] Target OGR input file location: {}", ogrInputFileLocation);

		String encoding = layerFileDescription.getEncoding();
		logger.info("🔤 [createDBTable] Extracted file character encoding schema: {}", encoding);

		OGR2OGR ogr2ogr = new OGR2OGR(OGR2OGR.SHP_TO_POSTGRES, null, layerTableName, "precision=NO", null,
				ogrInputFileLocation, encoding);

		logger.info("🚀 [createDBTable] Attempting to execute ogr2ogr system process...");
		Process process = ogr2ogr.execute();

		if (process == null) {
			logger.error(
					"❌ [createDBTable] Process initialization returned NULL. ProcessBuilder failed to start command.");
			throw new IOException("Layer upload on the postgis failed");
		} else {
			logger.info(
					"⏳ [createDBTable] Step 1: Entering process.waitFor() for ogr2ogr execution. If logs freeze here, you are deadlocked or hitting a network proxy timeout.");

			// Capture exit code to diagnose if it crashes immediately or hangs
			int exitCode = process.waitFor();

			logger.info("✅ [createDBTable] Step 1 Finished. ogr2ogr process completed with Exit Code: {}", exitCode);

			if (exitCode != 0) {
				logger.error(
						"❌ [createDBTable] ogr2ogr failed with non-zero exit status. Check database permissions or file health.");
				throw new IOException("Layer upload on the postgis failed with exit code: " + exitCode);
			}

			result.put("Table created for layer", layerTableName);
		}

		logger.info(
				"💬 [createDBTable] Step 2: Preparing column definitions map insertion payload. Total keys to process: {}",
				layerColumnDescription != null ? layerColumnDescription.size() : 0);

		process = ogr2ogr.addColumnDescription(layerTableName, layerColumnDescription);

		if (process == null) {
			logger.error("❌ [createDBTable] Step 2 Failed: addColumnDescription returned a NULL process handle.");
			throw new IOException("Comment could not be added to table");
		} else {
			logger.info(
					"⏳ [createDBTable] Step 2: Entering process.waitFor() for psql column descriptions update statement execution...");

			int commentExitCode = process.waitFor();

			logger.info("✅ [createDBTable] Step 2 Finished. psql comments process completed with Exit Code: {}",
					commentExitCode);

			if (commentExitCode != 0) {
				logger.warn("⚠️ [createDBTable] psql comments process completed with warning error status code: {}",
						commentExitCode);
			}

			result.put("Comments added", "success");
		}

		logger.info("🎉 [createDBTable] Complete execution pipeline finished cleanly for table context: {}",
				layerTableName);
	}

	@Override
	public Map<String, String> prepareDownloadLayer(HttpServletRequest request, LayerDownload layerDownload)
			throws IllegalArgumentException, InterruptedException, IOException {

		Map<String, String> retValue = new HashMap<>();

		String portalId = request.getHeader("Portal-Id");
		String apiKeyRecieved = request.getHeader("api-key");

		Portal portal = portaldao.findById(Long.valueOf(portalId));
		String apiKeyStored = portal.getApiKey();

		boolean apikeyIsValid = passwordEncoder.isPasswordValid(apiKeyStored, apiKeyRecieved, null);

		if (!apikeyIsValid) {
			throw new BadRequestException("api key is not valid");
		}

		MetaLayer metaLayer = (layerDownload.getLayerName() != null)
				? findByLayerTableName(layerDownload.getLayerName())
				: null;
		if (metaLayer == null) {
			throw new IllegalArgumentException("Layer not found");
		}

		String uri = request.getRequestURI();
		String hashKey = UUID.randomUUID().toString();

		ExecutorService service = Executors.newFixedThreadPool(10);
		service.execute(() -> {
			try {
				runDownloadLayer(null, uri, hashKey, null, layerDownload, metaLayer);
			} catch (IllegalArgumentException | IOException e) {
				logger.error("Download input or IO error: {}", e.getMessage());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // preserve interrupt status
				logger.warn("Download interrupted: {}", e.getMessage());
			} catch (Exception e) {
				logger.error("Unexpected error in download task", e);
			}
		});

		String layerName = layerDownload.getLayerName();

		retValue.put("filePath", "/" + hashKey + "/" + layerName);
		retValue.put("success", "The layer download process has started. You will receive the mail shortly");

		service.shutdown();
		return retValue;
	}

	private boolean checkDownLoadAccess(CommonProfile profile, LayerDownload layerDownload) {
		MetaLayer metaLayer = findByLayerTableName(layerDownload.getLayerName());
		return checkDownLoadAccess(profile, metaLayer);
	}

	private boolean checkDownLoadAccess(CommonProfile profile, MetaLayer metaLayer) {
		if (profile == null)
			return false;

		JSONArray roles = (JSONArray) profile.getAttribute("roles");
		if (roles.contains("ROLE_ADMIN"))
			return true;

		if (metaLayer == null)
			return false;

		return metaLayer.getDownloadAccess().equals(DownloadAccess.ALL)
				|| metaLayer.getUploaderUserId().equals(Long.parseLong(profile.getId()));
	}

	public void runDownloadLayer(String authorId, String uri, String hashKey, String requestToken,
			LayerDownload layerDownload, MetaLayer metaLayer)
			throws IllegalArgumentException, InterruptedException, IOException {

		String layerName = layerDownload.getLayerName();
		List<String> attributeList = layerDownload.getAttributeList();
		List<String> filterArray = layerDownload.getFilterArray();

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

		StringBuilder attributeString = new StringBuilder();
		if (!attributeList.isEmpty()) {
			for (String attribute : attributeList) {
				attributeString.append(attribute + ", ");
			}
			attributeString.append("wkb_geometry ");
		} else
			attributeString.append("*");

		for (String filter : filterArray) {
			logger.debug(filter);
		}

		shapeFileDirectoryPath = shapeFileDirectory.getAbsolutePath();

		if (metaLayer.getLayerType() == LayerType.RASTER) {
			FileUtils.copyDirectory(new File(metaLayer.getDirPath()), shapeFileDirectory);

		} else {

			String query = "select " + attributeString + " from " + layerName;

			OGR2OGR ogr2ogr = new OGR2OGR(OGR2OGR.POSTGRES_TO_SHP, null, layerName, null, query, shapeFileDirectoryPath,
					null);
			Process process = ogr2ogr.execute();
			if (process == null) {
				throw new IOException("Shape file creation failed");
			} else {
				process.waitFor();
			}
		}

		String zipFileLocation = shapeFileDirectoryPath + ".zip";
		zipFolder(zipFileLocation, shapeFileDirectory);

		logger.debug("{} / {} / {}", uri, hashKey, layerName);

//		String url = uri + "/" + hashKey + "/" + layerName;
//
//		mailService.sendMail(authorId, url, "naksha");
//		userServiceApi = headers.addUserHeaders(userServiceApi, requestToken);
//		DownloadLogData data = new DownloadLogData();
//		data.setFilePath(url);
//		data.setFileType(metaLayer.getLayerType() == LayerType.RASTER ? LayerType.RASTER.toString() : "SHP");
//		data.setFilterUrl(uri);
//		data.setStatus("success");
//		data.setSourcetype("Map");
//		data.setNotes(layerDownload.getLayerTitle());
//		try {
//			userServiceApi.logDocumentDownload(data);
//		} catch (ApiException e) {
//			logger.error(e.getMessage());
//		}
		// TODO : send mail notification for download url
		// return directory.getAbsolutePath();
	}

	public void zipFolder(String zipFileLocation, File fileDirectory) throws IOException {
		FileOutputStream fos = new FileOutputStream(zipFileLocation);
		try (ZipOutputStream zipOut = new ZipOutputStream(fos)) {
			for (File fileToZip : fileDirectory.listFiles()) {
				try (FileInputStream fis = new FileInputStream(fileToZip)) {
					ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
					zipOut.putNextEntry(zipEntry);

					byte[] bytes = new byte[1024];
					int length;
					while ((length = fis.read(bytes)) >= 0) {
						zipOut.write(bytes, 0, length);
					}
				}
			}
		}
	}

	@Override
	public String getFileLocation(String hashKey, String layerName) {
		return DOWNLOAD_BASE_LOCATION + File.separator + hashKey + File.separator + layerName + ".zip";
	}

	@Override
	public MetaLayer updateMataLayer(HttpServletRequest request, MetaLayerEdit metaLayerEdit) throws IOException {
		MetaLayer metaLayer = findById(metaLayerEdit.getId());
		if (Utils.isAdmin(request) || Utils.isOwner(metaLayer.getUploaderUserId(), request)) {
			metaLayer = metaLayerEdit.update(metaLayerEdit, metaLayer);
			return update(metaLayer);
		} else {
			throw new IOException("User is unauthorized to edit the layer");
		}
	}

	@Override
	public MetaLayer makeLayerActive(HttpServletRequest request, String layerName) {
		String portalId = request.getHeader("Portal-Id");
		String apiKeyRecieved = request.getHeader("api-key");

		Portal portal = portaldao.findById(Long.valueOf(portalId));
		String apiKeyStored = portal.getApiKey();

		boolean apikeyIsValid = passwordEncoder.isPasswordValid(apiKeyStored, apiKeyRecieved, null);

		if (!apikeyIsValid) {
			throw new BadRequestException("api key is not valid");
		}
		MetaLayer metaLayer = findByLayerTableName(layerName);
		metaLayer.setLayerStatus(LayerStatus.ACTIVE);
		return update(metaLayer);
	}

	@Override
	public MetaLayer makeLayerPending(HttpServletRequest request, String layerName) {

		String portalId = request.getHeader("Portal-Id");
		String apiKeyRecieved = request.getHeader("api-key");

		Portal portal = portaldao.findById(Long.valueOf(portalId));
		String apiKeyStored = portal.getApiKey();

		boolean apikeyIsValid = passwordEncoder.isPasswordValid(apiKeyStored, apiKeyRecieved, null);

		if (!apikeyIsValid) {
			throw new BadRequestException("api key is not valid");
		}

		MetaLayer metaLayer = findByLayerTableName(layerName);
		metaLayer.setLayerStatus(LayerStatus.PENDING);
		return update(metaLayer);
	}

	@Override
	public MetaLayer removeLayer(String layerName) {
		MetaLayer metaLayer = findByLayerTableName(layerName);
		metaLayer.setLayerStatus(LayerStatus.INACTIVE);
		update(metaLayer);
		return metaLayer;
	}

	@Override
	public MetaLayer deleteLayer(String layerName) {
		MetaLayer metaLayer = findByLayerTableName(layerName);
		return deleteLayer(metaLayer);
	}

	@Override
	public List<MetaLayer> cleanupInactiveLayers() {
		List<MetaLayer> layers = metaLayerDao.getAllInactiveLayer();
		List<MetaLayer> deletedLayers = new ArrayList<>();
		for (MetaLayer metaLayer : layers) {
			deletedLayers.add(deleteLayer(metaLayer));
		}
		return deletedLayers;
	}

	public MetaLayer deleteLayer(MetaLayer metaLayer) {
		String layerName = metaLayer.getLayerTableName();

		// Remove the copied files from the file system. (Need to take a call on this)
		String dirPath = metaLayer.getDirPath();
		MetaLayerUtil.deleteFiles(dirPath);

		if (metaLayer.getLayerType() == LayerType.RASTER) {
			geoserverService.removeDataStore(WORKSPACE, layerName);
		}
		// remove-published layer from the geoserver
		geoserverService.removeLayer(WORKSPACE, layerName);

		// remove the style from the geoserver
		geoserverStyleService.unpublishAllStyles(layerName, WORKSPACE);

		// Drop table from the database
		metaLayerDao.dropTable(layerName);

		// Delete the entry in the .
		return metaLayerDao.delete(metaLayer);
	}

	@Override
	public ObservationLocationInfo getLayerInfo(String lon, String lat) {
		try {
			String soil = getAttributeValueAtLatlon("descriptio", INDIA_SOIL, lon, lat);
			String temp = getAttributeValueAtLatlon("temp_c", INDIA_TEMPERATURE, lon, lat);
			String rainfall = getAttributeValueAtLatlon("rain_range", INDIA_RAINFALLZONE, lon, lat);
			String tahsil = getAttributeValueAtLatlon(TAHSIL_COLUMN_NAME, INDIA_TAHSIL, lon, lat);
			String forestType = getAttributeValueAtLatlon("type_desc", INDIA_FOREST_TYPE, lon, lat);
			// pa-fields
			String protectedAreaName = getAttributeValueAtLatlon("nom", LAYER_MADAGASCAR, lon, lat);
			String province = getAttributeValueAtLatlon("province", LAYER_MADAGASCAR, lon, lat);
			String district = getAttributeValueAtLatlon("district", LAYER_MADAGASCAR, lon, lat);

			// bbp fields
			String dzongkhag = getAttributeValueAtLatlon("dzongkhag", LAYER_BBP, lon, lat);
			String geog = getAttributeValueAtLatlon("geog", LAYER_BBP, lon, lat);

			if (soil == null && temp == null && rainfall == null && tahsil == null && forestType == null)
				return null;

			if (metaLayerDao.isTableAvailable(LAYER_MADAGASCAR) != null) {
				return new ObservationLocationInfoPA(soil, temp, rainfall, tahsil, forestType, protectedAreaName,
						province, district);
			}

			if (metaLayerDao.isTableAvailable(LAYER_BBP) != null) {
				return new ObservationLocationInfoBBP(soil, temp, rainfall, tahsil, forestType, dzongkhag, geog);
			}

			return new ObservationLocationInfo(soil, temp, rainfall, tahsil, forestType);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	private String getAttributeValueAtLatlon(String attribute, String layerName, String lon, String lat) {
		try {
			layerName = metaLayerDao.isTableAvailable(layerName);
			if (layerName == null)
				return "";
			List<Object> result = metaLayerDao.executeQueryForSingleResult(attribute, layerName, lon, lat);
			if (result.isEmpty())
				return null;

			return result.get(0).toString();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public LocationInfo getLocationInfo(String lat, String lon) {
		List<Object[]> result = metaLayerDao.executeQueryForLocationInfo(lat, lon);
		LocationInfo locationResponse = new LocationInfo();

		if (!result.isEmpty()) {
			Object[] values = result.get(0);
			locationResponse.setState(values[0].toString());
			locationResponse.setDistrict(values[1].toString());
			locationResponse.setTahsil(values[2].toString());
		}

		return locationResponse;
	}

	@Override
	public MetaLayer getMetaLayerInfo(HttpServletRequest request, String layerName) {
		MetaLayer layer = findByLayerTableName(layerName);
		return layer;
	}
}
