package com.strandls.naksha.dao;

import java.util.List;

import com.strandls.naksha.pojo.ObservationLocationInfo;

/**
 * DAO for map layers that are stored in post gis
 * 
 * @author mukund
 *
 */
public interface LayerDAO {

	/**
	 * Get all the attributes present in the layer
	 * 
	 * @param layerName
	 * @return
	 */

	/**
	 * Get layer names associated with a tag
	 * 
	 * @return
	 */
	List<String> getLayerNamesWithTag(String tag);

	/**
	 * 
	 * @param lat
	 * @param lon
	 * @return {@link ObservationLocationInfo}
	 */
	public ObservationLocationInfo getLayersDetails(Double lat, Double lon);

}
