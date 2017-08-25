package com.ehealth.services;

import com.ehealth.Runner;
import com.ehealth.entities.*;
import com.ehealth.ui.MainFrame;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.OverDailyLimitException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;

import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


@Component
public class GoogleMapsAddressValidator {
	
	
	@Value("${google.api.key}")
	private String googleApiKey;
	
    @Autowired
    private MainFrame frame;
	
	private static final String OBLAST = "область";
	
    private static Logger logger = LogManager.getLogger(GoogleMapsAddressValidator.class);
	
    private static GeoApiContext context;

    @PostConstruct
    private void initValues() throws IllegalStateException{
        logger.info("initValues");
        context = new GeoApiContext().setApiKey(googleApiKey);
        
    }
	public AddressValidationResult validate(FacilityStreetCoverage facilityStreetCoverage) {
		AddressValidationResult result = new AddressValidationResult();
		try {
			String addressToSearch = generateAddress(facilityStreetCoverage);
			logger.info(addressToSearch);
			GeocodingResult[] results = GeocodingApi.geocode(getContext(), addressToSearch).custom("language","Uk").await();
			//logger.info(results[0].addressComponents[0].longName);
			if (results != null && results.length != 0) {
				for(AddressComponent addressComponent : results[0].addressComponents) {
					if (addressComponent.types[0] !=null) {
						result.setReturnedStreetName(addressComponent.longName);
						result.setMatchType(examineStreetNameMatchType(facilityStreetCoverage,result.getReturnedStreetName()));
						break;
					}
				}
				for(AddressType addressType : results[0].types) {
					System.out.println("address type: "+ addressType.name());
				}
			}
			else {
				result.setMatchType(examineStreetNameMatchType(facilityStreetCoverage,null));
			}

		} catch (UnknownHostException e) {
			notify("No Interned connection");
		} catch (SocketTimeoutException e) {
			notify("No Interned connection");
		} catch (OverDailyLimitException e) {
			notify("Over daily limit");
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		return result;
	}
	private static String generateAddress(FacilityStreetCoverage facilityStreetCoverage) {
		String result = facilityStreetCoverage.getOblast() +" "+ OBLAST+", "; 
		if (facilityStreetCoverage.getRayon()!=null) result+=facilityStreetCoverage.getRayon()+" ";
		result += facilityStreetCoverage.getLocality() + ", " + facilityStreetCoverage.getStreetType() +" "+facilityStreetCoverage.getStreet() + ", " + facilityStreetCoverage.getIndex();
		return result;
	}
	private static StreetNameMatchType examineStreetNameMatchType(FacilityStreetCoverage coverage, String returnedStreetName) {
		logger.info("examineStreetNameMatchType start");
		if (returnedStreetName==null || returnedStreetName.isEmpty()) return StreetNameMatchType.MISSING;
		if ((coverage.getStreetType()+" "+coverage.getStreet()).trim().equals(returnedStreetName.trim()) || (coverage.getStreet()+" "+coverage.getStreetType()).trim().equals(returnedStreetName.trim())) return StreetNameMatchType.FULL;
		if (returnedStreetName.contains(coverage.getStreet().trim().replaceFirst(".*\\.", ""))) return StreetNameMatchType.PARTIAL; 
		return StreetNameMatchType.NOT_MATCHED;
	}
	
	private void writeLine(String line) {
		frame.addLine(line);
	}
	private void notify(String message) {
		frame.showDialog(message);
	}
	
	private static GeoApiContext getContext(){
		return context;
	}
	

}
