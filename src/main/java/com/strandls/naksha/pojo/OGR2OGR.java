package com.strandls.naksha.pojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.naming.directory.InvalidAttributesException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.naksha.NakshaConfig;

public class OGR2OGR {

	private static final String GEOSERVER_DBNAME = "geoserver.dbname";
	private static final String GEOSERVER_DBUSER = "geoserver.dbuser";
	private static final String GEOSERVER_PASS = "db.password";
	private static final String GEOSERVER_PORT = "geoserver.dbport";
	private static final String GEOSERVER_HOST = "geoserver.dbhost";

	public static final String SHP_TO_POSTGRES = "ESRI Shapefile";
	public static final String POSTGRES_TO_SHP = "PostgreSQL";

	private String formatName;
	private String host;
	private String user;
	private String dbName;
	private String password;
	private String port;
	
	private final Logger logger = LoggerFactory.getLogger(OGR2OGR.class);
	

	// Require to promote the data higher level.
	private String nlt;

	// To change the layer name to new name.
	private String nln;

	// Layer creation option
	private String lco;

	private String ogrCommand;

	private String query;

	private String shpFile;
	
	private String encoding;

	public OGR2OGR(String formatName, String nlt, String nln, String lco, String query, String shpFile, String encoding)
			throws InvalidAttributesException {
		super();
		this.user = NakshaConfig.getString(GEOSERVER_DBUSER);
		this.dbName = NakshaConfig.getString(GEOSERVER_DBNAME);
		this.password = NakshaConfig.getString(GEOSERVER_PASS);
		this.port = NakshaConfig.getString(GEOSERVER_PORT);
		this.host = NakshaConfig.getString(GEOSERVER_HOST);

		this.formatName = formatName;
		if (nlt == null)
			this.nlt = "PROMOTE_TO_MULTI";
		else
			this.nlt = nlt;
		this.nln = nln;
		this.lco = lco;

		this.query = query;
		this.shpFile = shpFile;
		this.encoding = encoding;
		init();
	}

	private void init() throws InvalidAttributesException {
		switch (formatName) {
		case SHP_TO_POSTGRES:
			ogrCommand = "ogr2ogr ";
			ogrCommand += "-f \"PostgreSQL\" ";
			String conn = "host=" + host + " user=" + user + " dbname=" + dbName + " port=" + port;
			if (password != null)
				conn += " password=" + password;
			ogrCommand += " PG:\"" + conn + "\" ";
			ogrCommand += shpFile;
			if (nlt != null)
				ogrCommand += " -nlt " + nlt;
			if (nln != null)
				ogrCommand += " -nln " + nln;
			if (lco != null)
				ogrCommand += " -lco " + lco;
			if(encoding != null)
				ogrCommand += " --config SHAPE_ENCODING " + this.encoding;
			break;
		case POSTGRES_TO_SHP:
			ogrCommand = "ogr2ogr ";
			ogrCommand += "-f \"ESRI Shapefile\" ";
			ogrCommand += shpFile;
			conn = "host=" + host + " user=" + user + " dbname=" + dbName + " port=" + port;
			if (password != null)
				conn += " password=" + password;
			ogrCommand += " PG:\"" + conn + "\"";
			ogrCommand += " -sql";
			ogrCommand += " \"" + query + "\"";
			ogrCommand += " -lco ENCODING=UTF-8";
			if (nln != null)
				ogrCommand += " -nln " + nln;
			break;
		default:
			throw new InvalidAttributesException("Invalid format");
		}
	}
	
	public Process execute(String command) {
		System.out.println("===========================================================");
		System.out.println("======================org2org started======================");
		System.out.println(command);
		System.out.println("===========================================================");
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("/bin/bash", "-c", command);
		pb.redirectErrorStream(true);
		try {
			return pb.start();
		} catch (IOException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	public Process execute() {
		return execute(ogrCommand);
	}

	public Process addColumnDescription(String layerName, Map<String, String> layerColumnDescription) {

		StringBuilder comments = new StringBuilder();

		for (Map.Entry<String, String> entry : layerColumnDescription.entrySet()) {
			String columnName = entry.getKey();
			String description = layerColumnDescription.get(columnName);
			
			String comment = "";
			comment += "PGPASSWORD=" + password;
			comment += " psql ";
			comment += " -h " + host;
			comment += " -d " + dbName;
			comment += " -a -U " + user;
			String sqlComment = "COMMENT ON COLUMN public." + layerName + "." + columnName + " IS \'" + description
					+ "\'";
			comment += " -c " + "\"" + sqlComment + "\"";
			comment += ";";
			comments.append(comment);
		}

		ProcessBuilder pb = new ProcessBuilder();
		pb.command("/bin/bash", "-c", comments.toString());

		try {
			return pb.start();
		} catch (IOException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
}
