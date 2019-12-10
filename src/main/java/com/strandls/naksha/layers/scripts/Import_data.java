/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strandls.naksha.layers.scripts;

import static com.strandls.naksha.layers.scripts.Import_layers.filemainfunc;
import static com.strandls.naksha.layers.scripts.Import_layers.main_func;
import static com.strandls.naksha.layers.scripts.gen_geoserver_cache_conf.generate_cache;
import static com.strandls.naksha.layers.scripts.generate_geoserver_layers.geoserver_func;
import static com.strandls.naksha.layers.scripts.generate_geoserver_styles.generate_styles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.strandls.naksha.NakshaConfig;

/**
 *
 * @author humeil
 */
public class Import_data {

	@Inject
	DBexec database;
	static Connection connection = null;
	static Statement stmt = null;
	static Statement stmt1 = null;
	String layer_type = "POINT";
	String dbname;
	String dbuser;
	String datapath;
	String dbpassword;
	static String PATH = NakshaConfig.getString("tmpDir.path");
	static String ROLE_TYPES[] = new String[] { "admin", "validator", "member" };
	static HashMap<String, String> Role_perm = new HashMap<>();
	static String dirname = NakshaConfig.getString("tmpDir.path") + "layersqls/";
	String sql_file_name = "sql_cmds";

	generate_geoserver_layers layer_obj = new generate_geoserver_layers();
	generate_geoserver_styles styles_obj = new generate_geoserver_styles();

	String tmp_dir_path = NakshaConfig.getString("tmpDir.path");

	public int main_data(String dbname, String dbuser, String dataPath, String dbpassword)
			throws ClassNotFoundException, SQLException, IOException, ScriptException, ParseException,
			InterruptedException {

		this.dbname = dbname;
		this.dbuser = dbuser;
		this.datapath = dataPath;
		this.dbpassword = dbpassword;

		System.getProperty("user.dir");
		System.setProperty("user.dir", tmp_dir_path);

		String sql_file_name = "sql_cmds";

		Import_layers imp = new Import_layers(dbname, dbuser, datapath, sql_file_name);
		int end_res = main_func(imp, database, dbname, dbpassword, dbuser);
		generate_styles(dbname, dbuser, dbpassword);
		geoserver_func(dbname, dbuser, dbpassword);
		generate_cache(dbname, dbuser, dbpassword);
		return end_res;
	}

	public int csvexcelmain_data(String dbname, String dbuser, String dbpassword, String dataPath, String FilePath,
			boolean isCsvFile) throws Exception {
		this.dbname = dbname;
		this.dbuser = dbuser;
		this.dbpassword = dbpassword;
		this.datapath = dataPath;
		String sql_file_name = "sql_cmds";
		Import_layers imp = new Import_layers(dbname, dbuser, dataPath, sql_file_name);
		int end_res = filemainfunc(imp, database, dbname, dbpassword, dbuser);
		System.out.println(end_res);
		String metadata = imp.getLayerInfo(imp.path);
		System.out.println(metadata);
		JSONParser jsonParser = new JSONParser();
		JSONArray json = (JSONArray) jsonParser.parse(metadata);
		System.out.println(metadata);
		String[] lines = metadata.split(System.getProperty("line.separator"));
		System.out.println("database import");
		for (Object o : json) {
			JSONObject layer = (JSONObject) o;
			System.out.println(layer.get("filename"));
			System.out.println(layer);
			JSONObject Meta_Layer = (JSONObject) layer.get("Meta_Layer");
			System.out.println(Meta_Layer);
			if (!Meta_Layer.containsKey("layer_tablename")) {
				System.out.println(layer.get("filename"));
			}
			String l = (String) Meta_Layer.get("layer_tablename");
			String layer_tablename = l.replace(".csv", "");
			System.out.println(layer_tablename);
			List<String> ret = Import_layers.parse_Meta_Layer(layer_tablename, layer_type, Meta_Layer);
			String Meta_Layer_sql = ret.get(0);
			System.out.println("Meta_Layer_sql");
			String layer_colcomments = ret.get(1);
			String sql1 = String.format(layer_colcomments);
			System.out.println(sql1);
			File newFile = new File(dirname + File.separator + layer_tablename.concat(".sql"));
			newFile.createNewFile();
			System.out.println(Meta_Layer_sql);
			allquries(layer_tablename, sql1, dbname, dbuser, dbpassword, isCsvFile, Meta_Layer_sql, FilePath);
		}
		return 0;
	}

	public static int allquries(String layer_tablename, String sql1, String dbname, String dbuser, String dbpassword,
			boolean isCsvFile, String Meta_Layer_sql, String FilePath) throws Exception {
		final String updatetable = "\nUPDATE " + layer_tablename
				+ " SET __mlocate__layer_id = (SELECT currval('\"Meta_Layer_layer_id_seq\"')), __mlocate__status = 1,__mlocate__nid = 0, __mlocate__created_by = 1, __mlocate__created_date = now(), __mlocate__modified_by = 1, __mlocate__modified_date = now(), __mlocate__validated_by = 1, __mlocate__validated_date = now();\n\n";
		final String geometryquery = " ALTER TABLE " + layer_tablename + " ADD COLUMN __mlocate__topology geometry ;\n"
				+ sql1 + ";\n" + " UPDATE " + layer_tablename
				+ " SET __mlocate__topology = ST_MakePoint(longitude, latitude);\n" + " CREATE INDEX on "
				+ layer_tablename + " USING gist ( __mlocate__topology );\n" + "COMMIT";
		String createQuery = "\ncreate table " + layer_tablename
				+ "(__mlocate__id bigint NOT NULL DEFAULT nextval('layer_template_id_seq'::regclass),__mlocate__layer_id BIGINT ,__mlocate__status BIGINT ,__mlocate__nid bigint,__mlocate__created_by SMALLINT ,__mlocate__created_date DATE,__mlocate__modified_by BIGINT ,__mlocate__modified_date DATE,__mlocate__validated_by BIGINT,__mlocate__validated_date DATE,";
		try {
			Class.forName("org.postgresql.Driver");
			String dbhost = NakshaConfig.getString("geoserver.dbhost");
			String dbport = NakshaConfig.getString("geoserver.dbport");
			connection = DriverManager.getConnection("jdbc:postgresql://" + dbhost + ":" + dbport + "/" + dbname,
					dbuser, dbpassword);
			stmt = connection.createStatement();
			if (isCsvFile) {
				csvdatabase(layer_tablename, Meta_Layer_sql, FilePath, createQuery, updatetable, geometryquery);
			} else {
				exceldatabase(layer_tablename, Meta_Layer_sql, FilePath, createQuery, updatetable, geometryquery);
			}
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}
		return 0;
	}

	private static int csvdatabase(String layer_tablename, String Meta_Layer_sql, String FilePath, String createQuery,
			String updatetable, String geometryquery) throws Exception

	{
		FileReader filereader = new FileReader(FilePath);
		CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
		CSVReader csvReader = new CSVReaderBuilder(filereader).withCSVParser(parser).build();
		String[] nextRecord;
		String[] columnname = null;
		String[] dataType = null;
		StringBuffer sb = new StringBuffer();
		while ((nextRecord = csvReader.readNext()) != null) {
			columnname = new String[nextRecord.length];
			for (int i = 0; i < nextRecord.length; i++) {
				sb.append(nextRecord[i]).append(",");
				columnname[i] = nextRecord[i];
			}
			stmt.executeUpdate(Meta_Layer_sql);
			System.out.println();
			break;
		}
		int countforLoop = 0;
		Pattern p = Pattern.compile("^\\d{4}[-]?\\d{1,2}[-]?\\d{1,2}[T]?\\d{1,2}:\\d{1,2}:\\d{1,2}$");
		while ((nextRecord = csvReader.readNext()) != null) {
			String insertQuery = "insert into " + layer_tablename + "(" + sb.substring(0, sb.length() - 1) + ")"
					+ " values (";
			dataType = new String[nextRecord.length];
			for (int j = 0; j < nextRecord.length; j++) {
				if (nextRecord[j].matches("-?\\d+(\\.\\d+)?")) {
					dataType[j] = "decimal";
					insertQuery = insertQuery + "" + nextRecord[j] + ",";
				} else {
					dataType[j] = "VARCHAR";
					insertQuery = insertQuery + "'" + nextRecord[j] + "',";
				}
			}
			insertQuery = insertQuery.substring(0, insertQuery.length() - 1);
			insertQuery += " )";
			if (countforLoop == 0) {
				for (int k = 0; k < columnname.length; k++) {
					createQuery = createQuery + columnname[k] + " " + dataType[k] + ",";
				}
				createQuery = createQuery.substring(0, createQuery.length() - 1);
				createQuery += " )";
				System.out.println(createQuery);
				stmt.executeUpdate(createQuery);
			}
			stmt.executeUpdate(insertQuery);
			stmt.executeUpdate(updatetable);
			countforLoop++;
		}
		stmt.executeUpdate(geometryquery);
		return 1;
	}

	private static int exceldatabase(String layer_tablename, String Meta_Layer_sql, String FilePath, String createQuery,
			String updatetable, String geometryquery) {
		try {
			String[] columnname = null;
			String[] dataType = null;
			StringBuffer sb = new StringBuffer();
			List<List<XSSFCell>> sheetData = new ArrayList<List<XSSFCell>>();
			Workbook wb = WorkbookFactory.create(new FileInputStream(FilePath));
			Sheet sheet = wb.getSheetAt(0);
			int maxNumOfCells = sheet.getRow(0).getLastCellNum();
			Iterator<Row> rows = sheet.rowIterator();
			while (rows.hasNext()) {
				XSSFRow row = (XSSFRow) rows.next();
				Iterator<Cell> cells = row.cellIterator();
				List<?> data = new ArrayList<Object>();
				int countforloop = 0;
				for (int cellCounter = 0; cellCounter < maxNumOfCells; cellCounter++) {
					if (countforloop == 0) {
						columnname = new String[maxNumOfCells];
						dataType = new String[columnname.length];
					}
					XSSFCell cell;
					if (row.getCell(cellCounter) == null) {
						cell = row.createCell(cellCounter);
					} else {
						cell = row.getCell(cellCounter);
						columnname[cellCounter] = cell.getStringCellValue();
					}
					sb.append(cell).append(" ,");
					countforloop++;
				}
				break;
			}
			while (rows.hasNext()) {
				XSSFRow row = (XSSFRow) rows.next();
				Iterator<Cell> cells = row.cellIterator();
				List<XSSFCell> data = new ArrayList<XSSFCell>();
				for (int cellCounter = 0; cellCounter < maxNumOfCells; cellCounter++) {
					XSSFCell cell;
					if (row.getCell(cellCounter) == null) {
						cell = row.createCell(cellCounter);
					} else {
						cell = row.getCell(cellCounter);
					}
					data.add(cell);
				}
				sheetData.add(data);
			}
			int countforLoop = 0;
			for (int i = 0; i < sheetData.size(); i++) {
				String insertQuery = "insert into " + layer_tablename + "(" + sb.substring(0, sb.length() - 1) + ")"
						+ " values (";
				List<?> list = (List<?>) sheetData.get(i);
				for (int j = 0; j < list.size(); j++) {
					Cell cell = (Cell) list.get(j);
					if (cell.getCellType() == CellType.NUMERIC) {
						dataType[j] = "Numeric";
						insertQuery = insertQuery + "'" + cell + "',";
					} else if (cell.getCellType() == CellType.STRING) {
						dataType[j] = "VARCHAR";
						insertQuery = insertQuery + "'" + cell + "',";
					} else if (cell.getCellType() == CellType.BOOLEAN) {
						dataType[j] = "BOOLEAN";
						insertQuery = insertQuery + "'" + cell + "',";
					} else if (cell.getCellType() == CellType.BLANK) {
						dataType[j] = "VARCHAR";
						insertQuery = insertQuery + "'" + cell + "',";
					}
				}
				if (countforLoop == 0) {
					for (int k = 0; k < columnname.length; k++) {
						createQuery = createQuery + " " + columnname[k] + " " + dataType[k] + ",";
					}
					createQuery = createQuery.substring(0, createQuery.length() - 1);
					createQuery += " )";
					System.out.println(createQuery);
					stmt.executeUpdate(createQuery);
					stmt.executeUpdate(Meta_Layer_sql);
				}
				insertQuery = insertQuery.substring(0, insertQuery.length() - 1);
				insertQuery += " )";
				System.out.println("");
				stmt.executeUpdate(insertQuery);
				stmt.executeUpdate(updatetable);
				System.out.println("");
				countforLoop++;
			}
			stmt.executeUpdate(geometryquery);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 1;
	}

	private static String getOutput(Process process) throws IOException {

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String s = null;
		while ((s = stdInput.readLine()) != null) {
			System.out.println(s);
		}
		return s;
	}

	public static List<String> getAllFiles(File curDir) {

		List<String> layers = new ArrayList<>();
		File[] filesList = curDir.listFiles();
		for (File f : filesList) {
			if (f.isDirectory())
				getAllFiles(f);
			if (f.isFile()) {
				layers.add(f.getName().replace(".sql", ""));
			}
		}
		return layers;
	}

}