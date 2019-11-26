package com.strandls.naksha.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.strandls.naksha.pojo.LayerAttributes;
import com.strandls.naksha.pojo.ObservationLocationInfo;

/**
 * Implementation of {@link LayerDAO}
 * 
 * @author mukund
 *
 */
public class LayerDAOJDBC implements LayerDAO {

	@Inject
	Connection connection;

	private static final String GET_ATTRIBUTES = "SELECT c.column_name, pgd.description\n"
			+ "FROM pg_catalog.pg_statio_all_tables as st\n"
			+ "INNER JOIN pg_catalog.pg_description pgd on (pgd.objoid=st.relid)\n"
			+ "RIGHT OUTER JOIN information_schema.columns c on (pgd.objsubid=c.ordinal_position and  c.table_schema=st.schemaname and c.table_name=st.relname)\n"
			+ "WHERE table_schema = 'public' and table_name = ?";

	private static final String GET_LAYERNAME_WITH_TAG = "SELECT layer_tablename, tags FROM \"Meta_Layer\"";

	@Override
	public List<LayerAttributes> getLayerAttributes(String layerName) {
		List<LayerAttributes> layerAttributes = new ArrayList<>();

		try (PreparedStatement statement = DAOUtil.prepareStatement(connection, GET_ATTRIBUTES, false, layerName);
				ResultSet resultSet = statement.executeQuery();) {
			while (resultSet.next()) {
				String name = resultSet.getString("column_name");
				String desc = resultSet.getString("description");
				layerAttributes.add(new LayerAttributes(name, desc));
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}

		return layerAttributes;
	}

	@Override
	public List<String> getLayerNamesWithTag(String tag) {
		List<String> layerNames = new ArrayList<>();

		try (PreparedStatement statement = connection.prepareStatement(GET_LAYERNAME_WITH_TAG);
				ResultSet resultSet = statement.executeQuery();) {
			while (resultSet.next()) {
				String tags = resultSet.getString("tags");
				if (tags != null)
					for (String t : tags.split(",")) {
						if (t.trim().toLowerCase().contains(tag.toLowerCase()))
							layerNames.add(resultSet.getString("layer_tablename"));
					}
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}

		return layerNames;
	}

	@Override
	public ObservationLocationInfo getLayersDetails(Double lat, Double lon) {

		String tahsilQuery = "SELECT tahsil from lyr_115_india_tahsils where st_contains"
				+ "(lyr_115_india_tahsils.__mlocate__topology, ST_GeomFromText('POINT(" + lon + " " + lat + ")',0))";
		String soilQuery = "SELECT descriptio from lyr_117_india_soils where st_contains"
				+ "(lyr_117_india_soils.__mlocate__topology, ST_GeomFromText('POINT(" + lon + " " + lat + ")',0))";
		String forestTypeQuery = "SELECT type_desc from lyr_118_india_foresttypes where st_contains"
				+ "(lyr_118_india_foresttypes.__mlocate__topology, ST_GeomFromText('POINT(" + lon + " " + lat
				+ ")',0))";
		String rainfallQuery = "SELECT rain_range from lyr_119_india_rainfallzone where st_contains"
				+ "(lyr_119_india_rainfallzone.__mlocate__topology, ST_GeomFromText('POINT(" + lon + " " + lat
				+ ")',0))";
		String tempQuery = "SELECT temp_c from lyr_162_india_temperature where st_contains"
				+ "(lyr_162_india_temperature.__mlocate__topology, ST_GeomFromText('POINT(" + lon + " " + lat
				+ ")',0));";

		String soil = null, rainfall = null, tahsil = null, temp = null, forestType = null;
		try {
			PreparedStatement tahsilStatement = connection.prepareStatement(tahsilQuery);
			ResultSet tahsilresult = tahsilStatement.executeQuery();
			while (tahsilresult.next()) {
				tahsil = tahsilresult.getString("tahsil");
			}

			PreparedStatement soilStatment = connection.prepareStatement(soilQuery);
			ResultSet soilResult = soilStatment.executeQuery();
			while (soilResult.next()) {
				soil = soilResult.getString("descriptio");
			}

			PreparedStatement typeDescStatment = connection.prepareStatement(forestTypeQuery);
			ResultSet typeDescResult = typeDescStatment.executeQuery();
			while (typeDescResult.next()) {
				forestType = typeDescResult.getString("type_desc");
			}

			PreparedStatement rainStatment = connection.prepareStatement(rainfallQuery);
			ResultSet rainResult = rainStatment.executeQuery();
			while (rainResult.next()) {
				rainfall = rainResult.getString("rain_range");
			}
			PreparedStatement tempStatment = connection.prepareStatement(tempQuery);
			ResultSet tempResult = tempStatment.executeQuery();
			while (tempResult.next()) {
				temp = tempResult.getString("temp_c");
			}
			return new ObservationLocationInfo(soil, temp, rainfall, tahsil, forestType);

		} catch (Exception e) {
			throw new DAOException(e);
		}
	}

}
