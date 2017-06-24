package com.ehealth.services;

import com.ehealth.entities.*;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;

public class GoogleMapsAddressValidator {
	private static final String API_KEY = "AIzaSyAfSGUAZLPORv9x8bHrMB14mu6qhhWr0co";
	private static final String OBLAST = "область";

	
	public static AddressValidationResult validate(FacilityStreetCoverage facilityStreetCoverage) {
		AddressValidationResult result = new AddressValidationResult();
		GeoApiContext context = new GeoApiContext().setApiKey(API_KEY);
		try {
			GeocodingResult[] results = GeocodingApi.geocode(context, generateAddress(facilityStreetCoverage)).custom("language","Uk").await();
			
			if (results.length != 0) {
				for(AddressComponent addressComponent : results[0].addressComponents) {
					if (addressComponent.types[0] == AddressComponentType.ROUTE) {
						result.setReturnedStreetName(addressComponent.longName);
						System.out.println("returned street -"+result.getReturnedStreetName());
						if ((facilityStreetCoverage.getStreetType()+" "+facilityStreetCoverage.getStreet()).equals(result.getReturnedStreetName()) || (facilityStreetCoverage.getStreet()+" "+facilityStreetCoverage.getStreetType()).equals(result.getReturnedStreetName())) result.setMatch(true);
						System.out.println("match: "+ result.isMatch());
						
					}
				}
				for(AddressType addressType : results[0].types) {
					System.out.println("address type: "+ addressType.name());
				}
			}
			//System.out.println(results[0].formattedAddress);
			//for (int i=0;i<results.length;i++) System.out.println("formatted address: "+results[i].formattedAddress);
			

		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		return result;
	}
	private static String generateAddress(FacilityStreetCoverage facilityStreetCoverage) {
		String result = facilityStreetCoverage.getOblast() +" "+ OBLAST+", "; 
		if (facilityStreetCoverage.getRayon()!=null) result+=facilityStreetCoverage.getRayon()+" ";
		result += facilityStreetCoverage.getLocality() + ", " + facilityStreetCoverage.getStreetType() +" "+facilityStreetCoverage.getStreet() + ", " + facilityStreetCoverage.getIndex();
		System.out.println(result);
		return result;
	}

}
