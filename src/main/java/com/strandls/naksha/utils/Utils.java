package com.strandls.naksha.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.CacheControl;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.json.XML;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.naksha.pojo.response.GeoserverLayerStyles;

import net.minidev.json.JSONArray;

/**
 * Common utility methods
 * 
 * @author mukund
 *
 */
public class Utils {

	private static final char CSV_SEPARATOR = ',';

	private static final Logger logger = LoggerFactory.getLogger(Utils.class);

	private Utils() {
	}

	public static Document convertStringToDocument(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); 
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xmlStr)));
		} catch (Exception e) {
			logger.error("Error while reading xml source string- {}", e.getMessage());
		}
		return null;
	}

	private static String followCVSformat(Object value) {
		if (value == null)
			return "";

		String result = value.toString();
		result = result.replace("\n", "").replace("\r", "");
		// https://tools.ietf.org/html/rfc4180
		if (result.contains("\"")) {
			result = result.replace("\"", "\"\"");
		}
		if (result.contains(",")) {
			result = "\"" + result + "\"";
		}
		return result;
	}

	public static String getCsvString(Collection<Object> values) {
		boolean first = true;

		StringBuilder sb = new StringBuilder();
		for (Object value : values) {
			if (!first)
				sb.append(CSV_SEPARATOR);

			sb.append(followCVSformat(value));
			first = false;
		}

		sb.append("\n");
		return sb.toString();
	}

	public static byte[] getCsvBytes(Collection<Object> values) {
		return getCsvString(values).getBytes();
	}

	public static void writeCsvLine(Writer w, Collection<Object> values) throws IOException {
		w.write(getCsvString(values));
	}

	public static List<GeoserverLayerStyles> getLayerStyles(Document doc) {
		List<GeoserverLayerStyles> styles = new ArrayList<>();
		if (doc == null)
			return styles;

		NodeList nList = doc.getDocumentElement().getElementsByTagName("sld:UserStyle");
		if (nList == null)
			return styles;

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String styleName = eElement.getElementsByTagName("sld:Name").item(0).getTextContent();
				String styleTitle = eElement.getElementsByTagName("sld:Title").item(0).getTextContent();

				styles.add(new GeoserverLayerStyles(styleName, styleTitle));
			}
		}

		return styles;
	}

	public static String jsonizeLayerString(String layerStr) {
		JSONObject jsonObject = XML.toJSONObject(layerStr);
		JSONObject wmsJson = jsonObject.getJSONObject("WMS_Capabilities");
		wmsJson.remove("Service");
		JSONObject capabilityJson = wmsJson.getJSONObject("Capability");
		capabilityJson.remove("Request");
		capabilityJson.remove("Exception");
		capabilityJson.getJSONObject("Layer").remove("CRS");

		return jsonObject.toString();
	}

	public static CacheControl getCacheControl() {
		CacheControl cache = new CacheControl();
		cache.setMaxAge(365 * 24 * 60 * 60);
		return cache;
	}

	public static boolean isAdmin(HttpServletRequest request) {
		if(request == null) return false;
		
		CommonProfile profile = AuthUtil.getProfileFromRequest(request);
		if(profile == null) return false;
		
		JSONArray roles = (JSONArray) profile.getAttribute("roles");
		if (roles.contains("ROLE_ADMIN") )
			return true;
		
		return false;
	}
}
