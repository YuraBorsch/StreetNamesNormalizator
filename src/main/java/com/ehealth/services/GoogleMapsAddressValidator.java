package com.ehealth.services;

import com.ehealth.entities.*;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Component
public class GoogleMapsAddressValidator {
	
	
	@Value("${google.api.key}")
	private String googleApiKey;
	
	private static final String OBLAST = "область";

	
	public AddressValidationResult validate(FacilityStreetCoverage facilityStreetCoverage) {
		AddressValidationResult result = new AddressValidationResult();
		GeoApiContext context = new GeoApiContext().setApiKey(googleApiKey);
		try {
			GeocodingResult[] results = GeocodingApi.geocode(context, generateAddress(facilityStreetCoverage)).custom("language","Uk").await();
			
			if (results.length != 0) {
				for(AddressComponent addressComponent : results[0].addressComponents) {
					if (addressComponent.types[0] == AddressComponentType.ROUTE) {
						result.setReturnedStreetName(addressComponent.longName);
						System.out.println("returned street -"+result.getReturnedStreetName());
						if (streetNameMatch(facilityStreetCoverage,result.getReturnedStreetName())) result.setMatch(true);
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
	private static boolean streetNameMatch(FacilityStreetCoverage coverage, String returnedStreetName) {
		if ((coverage.getStreetType()+" "+coverage.getStreet()).trim().equals(returnedStreetName.trim()) || (coverage.getStreet()+" "+coverage.getStreetType()).trim().equals(returnedStreetName.trim())) return true;
		else return false;
	}

}
