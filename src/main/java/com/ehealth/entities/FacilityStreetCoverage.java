package com.ehealth.entities;

import java.util.List;

public class FacilityStreetCoverage {
	private String fullName;
	private String divisionName;
	private String oblast;
	private String rayon;
	private String locality;
	private String streetType;
	private String street;
	private List<String> buildings;
	private String index;
	private boolean allStreetsCoverage;
	
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getDivisionName() {
		return divisionName;
	}
	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}
	public String getLocality() {
		return locality;
	}
	public void setLocality(String locality) {
		this.locality = locality;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public List<String> getBuildings() {
		return buildings;
	}
	public void setBuildings(List<String> buildings) {
		this.buildings = buildings;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getStreetType() {
		return streetType;
	}
	public void setStreetType(String streetType) {
		this.streetType = streetType;
	}
	public boolean isAllStreetsCoverage() {
		return allStreetsCoverage;
	}
	public void setAllStreetsCoverage(boolean allStreetsCoverage) {
		this.allStreetsCoverage = allStreetsCoverage;
	}
	public String getRayon() {
		return rayon;
	}
	public void setRayon(String rayon) {
		this.rayon = rayon;
	}
	public String getOblast() {
		return oblast;
	}
	public void setOblast(String oblast) {
		this.oblast = oblast;
	}
}
