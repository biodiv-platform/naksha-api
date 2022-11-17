package com.strandls.naksha.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.service.GeoserverService;

import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.decoder.RESTBoundingBox;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;

public class GeoserverServiceImpl implements GeoserverService {

	private final Logger logger = LoggerFactory.getLogger(GeoserverServiceImpl.class);

	private String baseUrl;
	private String userName;
	private String password;

	private GeoServerRESTManager manager;
	private HttpClientContext context;

	@Inject
	public GeoserverServiceImpl() throws MalformedURLException {
		super();
		this.baseUrl = NakshaConfig.getString(GEOSERVER_URL);
		this.userName = NakshaConfig.getString(GEOSERVER_USER_NAME);
		this.password = NakshaConfig.getString(GEOSERVER_PASSWORD);

		context = HttpClientContext.create();
		context.setAttribute(HttpClientContext.COOKIE_STORE, new BasicCookieStore());

		this.manager = new GeoServerRESTManager(new URL(baseUrl), userName, password);
	}

	public GeoserverServiceImpl(String baseUrl, String userName, String password) throws MalformedURLException {
		super();
		this.baseUrl = baseUrl;
		this.userName = userName;
		this.password = password;

		context = HttpClientContext.create();
		context.setAttribute(HttpClientContext.COOKIE_STORE, new BasicCookieStore());

		this.manager = new GeoServerRESTManager(new URL(baseUrl), userName, password);
	}

	@Override
	public GeoServerRESTManager getManager() {
		return manager;
	}

	@Override
	public boolean publishLayer(String workspace, String datastore, String layerName, String srs, String layerTitle,
			List<String> keywords, List<String> styles) {

		GSFeatureTypeEncoder fte = new GSFeatureTypeEncoder();
		fte.setEnabled(true);
		fte.setName(layerName);
		srs = srs == null ? "EPSG:4326" : srs;
		fte.setSRS(srs);
		fte.setTitle(layerTitle);
		for (String keyword : keywords)
			fte.addKeyword(keyword);

		GSLayerEncoder layerEncoder = new GSLayerEncoder();
		for (String style : styles)
			layerEncoder.addStyle(style);
		layerEncoder.setEnabled(true);

		return manager.getPublisher().publishDBLayer(workspace, datastore, fte, layerEncoder);
	}

	@Override
	public boolean publishGeoTiffStyleLayer(String workspace, String styleName, File sldStyleFile)
			throws FileNotFoundException {
		try {

			return manager.getPublisher().publishStyleInWorkspace(workspace, sldStyleFile, styleName);
		} catch (Exception e) {
			throw new FileNotFoundException("Geoserver publication of layer failed");
		}
	}

	@Override
	public boolean publishGeoTiffLayer(String workspace, String datastore, File geoTiffFile)
			throws FileNotFoundException {

		return manager.getPublisher().publishGeoTIFF(workspace, datastore, geoTiffFile);
	}

	@Override
	public boolean publishGeoTiffLayerWithStyle(String workspace, String datastore, String srs, String styleName,
			File geoTiffFile) throws FileNotFoundException, IllegalArgumentException {
		srs = srs == null ? "EPSG:4326" : srs;
		return manager.getPublisher().publishGeoTIFF(workspace, datastore, datastore, geoTiffFile, srs,
				ProjectionPolicy.NONE, styleName, null);

	}

	@Override
	public boolean removeDataStore(String workspace, String layerName) {
		return manager.getPublisher().removeDatastore(workspace, layerName, true);
	}

	@Override
	public boolean removeLayer(String workspace, String layerName) {
		return manager.getPublisher().removeLayer(workspace, layerName);
	}

	/**
	 * Makes http get request to geoserver
	 * 
	 * @param uri    the uri to hit
	 * @param params the parameters with the url
	 * @return byte[] response
	 */
	@Override
	public byte[] getRequest(String uri, List<NameValuePair> params) {

		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = null;
		byte[] byteArrayResponse = null;

		try {

			URIBuilder builder = new URIBuilder(baseUrl + uri);
			if (params != null)
				builder.setParameters(params);
			HttpGet request = new HttpGet(builder.build());

			String userPass = userName + ":" + password;
			byte[] encoding = Base64.getEncoder().encode(userPass.getBytes(StandardCharsets.UTF_8.name()));
			request.setHeader("Authorization", "Basic " + new String(encoding)); // NOSONAR

			httpclient = HttpClients.createDefault();

			response = httpclient.execute(request, context);
			HttpEntity entity = response.getEntity();
			byteArrayResponse = EntityUtils.toByteArray(entity);
			EntityUtils.consume(entity);

		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.error("Error while trying to send request at URL {}", uri);
		} finally {
			if (byteArrayResponse != null)
				HttpClientUtils.closeQuietly(response);
			try {
				if (httpclient != null)
					httpclient.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}

		return byteArrayResponse != null ? byteArrayResponse : new byte[0];
	}

	@Override
	public byte[] postRequest(String uri, String styleContent, String contentType, List<NameValuePair> params) {
		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = null;
		byte[] byteArrayResponse = null;

		try {

			URIBuilder builder = new URIBuilder(baseUrl + uri);
			if (params != null)
				builder.setParameters(params);

			HttpPost request = new HttpPost(builder.build());

			request.setHeader("Content-type", contentType);

			if (styleContent != null && !styleContent.isEmpty()) {
				StringEntity stringEntity = new StringEntity(styleContent);
				request.setEntity(stringEntity);
			}

			String userPass = userName + ":" + password;
			byte[] encoding = Base64.getEncoder().encode(userPass.getBytes(StandardCharsets.UTF_8));
			request.setHeader("Authorization", "Basic " + new String(encoding)); // NOSONAR

			httpclient = HttpClients.createDefault();
			response = httpclient.execute(request, context);
			HttpEntity entity = response.getEntity();

			byteArrayResponse = EntityUtils.toByteArray(entity);
			EntityUtils.consume(entity);

		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.error("Error while trying to send request at URL {}", uri);
		} finally {
			if (byteArrayResponse != null)
				HttpClientUtils.closeQuietly(response);
			try {
				if (httpclient != null)
					httpclient.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}

		return byteArrayResponse != null ? byteArrayResponse : new byte[0];
	}

	@Override
	public List<List<Double>> getBBoxByLayerName(String workspace, String layerName) {
		List<List<Double>> boundingBox = new ArrayList<List<Double>>();
		try {
			RESTLayer layer = manager.getReader().getLayer(workspace, layerName);
			RESTBoundingBox bbox = null;
			if (layer == null)
				return boundingBox;
			if (layer.getType() == RESTLayer.Type.RASTER) {
				bbox = manager.getReader().getCoverage(layer).getNativeBoundingBox();
			} else {
				bbox = manager.getReader().getFeatureType(layer).getNativeBoundingBox();

			}

			List<Double> topLeft = new ArrayList<>();
			List<Double> bottomRight = new ArrayList<>();

			topLeft.add(bbox.getMinX());
			topLeft.add(bbox.getMinY());

			bottomRight.add(bbox.getMaxX());
			bottomRight.add(bbox.getMaxY());

			boundingBox.add(topLeft);
			boundingBox.add(bottomRight);

			return boundingBox;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return boundingBox;
	}
}
