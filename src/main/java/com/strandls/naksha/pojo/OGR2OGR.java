package com.strandls.naksha.pojo;

import java.io.IOException;

import javax.naming.directory.InvalidAttributesException;

import org.json.simple.JSONObject;

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

	// Require to promote the data higher level.
	private String nlt;

	// To change the layer name to new name.
	private String nln;

	// Layer creation option
	private String lco;

	private String ogrCommand;
	
	private String query;
	
	private String shpFile;
	
	public OGR2OGR(String formatName, String nlt, String nln, String lco, String query, String shpFile) throws InvalidAttributesException {
		super();
		this.user = NakshaConfig.getString(GEOSERVER_DBUSER);
		this.dbName = NakshaConfig.getString(GEOSERVER_DBNAME);
		this.password = NakshaConfig.getString(GEOSERVER_PASS);
		this.port = NakshaConfig.getString(GEOSERVER_PORT);
		this.host = NakshaConfig.getString(GEOSERVER_HOST);

		this.formatName = formatName;
		this.nlt = nlt;
		if(nlt == null)
			nlt = "PROMOTE_TO_MULTI";
		this.nln = nln;
		this.lco = lco;
		
		this.query = query;
		this.shpFile = shpFile;
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
			if(nlt != null) 
				ogrCommand += " -nlt " + nlt;
			if(nln != null)
				ogrCommand += " -nln " + nln;
			if(lco != null)
				ogrCommand += " -lco " + lco;
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
			break;
		default:
			throw new InvalidAttributesException("Invalid format");
		}
	}

	public int execute() throws InterruptedException {
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("bash", "-c", ogrCommand);
		try {
			pb.start();
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int addColumnDescription(String layerName, JSONObject layerColumnDescription) {
		
		String comments = "";
		
		for(Object key : layerColumnDescription.keySet()) {
			String columnName = key.toString();
			String description = layerColumnDescription.get(key).toString();
			
			String comment = "";
			comment += "PGPASSWORD=" + password;
			comment += " psql ";
			comment += " -h " + host;
			comment += " -d " + dbName;
			comment += " -a -U " + user;
			String sqlComment = "COMMENT ON COLUMN public." + layerName + "." + columnName + " IS \'" + description + "\'";
			comment += " -c " + "\"" + sqlComment + "\"";
			comment +=";";
			comments += comment;
		}
		
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("bash", "-c", comments);
		
		//ProcessBuilder builder = new ProcessBuilder("bash", "-c", "PGPASSWORD=" + dbpassword
		//		+ " psql -h " + dbhost + " -d " + dbname + " -a -U " + dbuser + " -f " + sql_fl);
		return 0;
	}
}
