package com.ehealth.entities;

import com.google.maps.model.GeocodingResult;

public class AddressValidationResult {
	private FacilityStreetCoverage facilityStreetCoverage;
	private GeocodingResult[] mapsAPIResults;
	private String returnedStreetName;
	private boolean match;
	private String error;
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public FacilityStreetCoverage getFacilityStreetCoverage() {
		return facilityStreetCoverage;
	}
	public void setFacilityStreetCoverage(FacilityStreetCoverage facilityStreetCoverage) {
		this.facilityStreetCoverage = facilityStreetCoverage;
	}
	public GeocodingResult[] getMapsAPIResults() {
		return mapsAPIResults;
	}
	public void setMapsAPIResults(GeocodingResult[] mapsAPIResults) {
		this.mapsAPIResults = mapsAPIResults;
	}
	public String getReturnedStreetName() {
		return returnedStreetName;
	}
	public void setReturnedStreetName(String returnedStreetName) {
		this.returnedStreetName = returnedStreetName;
	}
	public boolean isMatch() {
		return match;
	}
	public void setMatch(boolean match) {
		this.match = match;
	}

	
}
