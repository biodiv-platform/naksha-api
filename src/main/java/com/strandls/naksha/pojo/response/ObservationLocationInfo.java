/**
 * 
 */
package com.strandls.naksha.pojo.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * @author Abhishek Rudra
 *
 */
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = ObservationLocationInfoPA.class, name = "ObservationLocationInfoPA") })
public class ObservationLocationInfo {

	private String soil;
	private String temp;
	private String rainfall;
	private String tahsil;
	private String forestType;

	/**
	 * @param soil
	 * @param temp
	 * @param rainfall
	 * @param tahsil
	 * @param forestType
	 */
	public ObservationLocationInfo(String soil, String temp, String rainfall, String tahsil, String forestType) {
		super();
		this.soil = soil;
		this.temp = temp;
		this.rainfall = rainfall;
		this.tahsil = tahsil;
		this.forestType = forestType;
	}

	public String getSoil() {
		return soil;
	}

	public void setSoil(String soil) {
		this.soil = soil;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String getRainfall() {
		return rainfall;
	}

	public void setRainfall(String rainfall) {
		this.rainfall = rainfall;
	}

	public String getTahsil() {
		return tahsil;
	}

	public void setTahsil(String tahsil) {
		this.tahsil = tahsil;
	}

	public String getForestType() {
		return forestType;
	}

	public void setForestType(String forestType) {
		this.forestType = forestType;
	}

}
