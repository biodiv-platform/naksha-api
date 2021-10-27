/**
 * 
 */
package com.strandls.naksha.pojo.response;

/**
 * @author Abhishek Rudra
 *
 */
public class ObservationLocationInfo {

	private String soil;
	private String temp;
	private String rainfall;
	private String tahsil;
	private String forestType;
	private String protectedAreaName;
	private String province;
	private String district;

	/**
	 * @param soil
	 * @param temp
	 * @param rainfall
	 * @param tahsil
	 * @param forestType
	 */
	public ObservationLocationInfo(String soil, String temp, String rainfall, String tahsil, String forestType,
			String protectedAreaName, String province, String district) {
		super();
		this.soil = soil;
		this.temp = temp;
		this.rainfall = rainfall;
		this.tahsil = tahsil;
		this.forestType = forestType;
		this.protectedAreaName = protectedAreaName;
		this.province = province;
		this.district = district;
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

	public String getProtectedAreaName() {
		return protectedAreaName;
	}

	public void setProtectedAreaName(String protectedAreaName) {
		this.protectedAreaName = protectedAreaName;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

}
