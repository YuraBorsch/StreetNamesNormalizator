package com.ehealth.services;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ehealth.entities.AddressValidationResult;
import com.ehealth.entities.FacilityStreetCoverage;
import com.ehealth.entities.StreetNameMatchType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;

@Component
public class ExcelReader {

	private static final String COVERS_ALL_STREETS = "Структурний підрозділ обслуговує всі адреси цього населеного пункту";
	private static final String STREET_WASNT_FOUND ="вулицю не знайдено";
	private static final String RAYON = "район";
	private static final String R_N = "р-н";
	
	
	@Value("${document.structure.topIndent}")
	private int topIndent;
	
	@Value("${document.structure.oblastRow}")
	private int oblastRow;
	
	
	@Value("${document.structure.fullNameCell}")
	private int fullNameCell;
	
	@Value("${document.structure.divisionNameCell}")
	private int divisionNameCell;
	
	@Value("${document.structure.rayonCell}")
	private int rayonCell;
	
	@Value("${document.structure.localityCell}")
	private int localityCell;
	
	@Value("${document.structure.indexCell}")
	private int indexCell;
	
	@Value("${document.structure.streetTypeCell}")
	private int streetTypeCell;

	@Value("${document.structure.streetCell}")
	private int streetCell;
	
	@Autowired
	GoogleMapsAddressValidator googleMapsAddressValidator;


	

	public List<FacilityStreetCoverage> readXls(String filepath) {
		List<FacilityStreetCoverage> results = new ArrayList<FacilityStreetCoverage>();
		Workbook workbook = null;
		try {

			FileInputStream excelFile = new FileInputStream(new File(filepath));
			workbook = new XSSFWorkbook(excelFile);
			Sheet datatypeSheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = datatypeSheet.iterator();
			
			// initialize styles
			CellStyle redStyle = createCellStyle(workbook,IndexedColors.RED);	
			CellStyle greenStyle = createCellStyle(workbook,IndexedColors.GREEN);	
			CellStyle yellowStyle = createCellStyle(workbook,IndexedColors.YELLOW);	
			

			String oblast=null;	
			while (iterator.hasNext() ) {

				Row currentRow = iterator.next();
				// check if table ended
				if (isRowEmpty(currentRow) && currentRow.getRowNum() >= topIndent) break;
				// get the oblast
				if (currentRow.getRowNum()==oblastRow ) {
					oblast = currentRow.getCell(0).getStringCellValue();
				}
				//process the row
				if (currentRow.getRowNum() >= topIndent) {

					FacilityStreetCoverage coverage = parseCoverageFromRow(currentRow);
					// set the oblast
					if (oblast!=null) coverage.setOblast(oblast);
					
					if (!coverage.isAllStreetsCoverage()) {
						// validate  the address
						AddressValidationResult valResult = googleMapsAddressValidator.validate(coverage);
						// write match result
						// initialize cells
						Cell cellMatch = currentRow.getCell(10);
						if(cellMatch == null) cellMatch = currentRow.createCell(10);
						Cell cellStreet = currentRow.getCell(11);
						if(cellStreet == null) cellStreet = currentRow.createCell(11);
						if (valResult.getMatchType()!=null) cellMatch.setCellValue(valResult.getMatchType().toString());
						if (valResult.getReturnedStreetName()!=null) cellStreet.setCellValue(valResult.getReturnedStreetName());
						System.out.println(valResult.getMatchType());
						switch (valResult.getMatchType()) {
					    	case FULL: cellMatch.setCellStyle(greenStyle);
					    	    break;
					    	case PARTIAL: cellMatch.setCellStyle(yellowStyle);
					        	break;
					    	case NOT_MATCHED: cellMatch.setCellStyle(redStyle);
				        		break;
					    	case MISSING: cellMatch.setCellStyle(redStyle);
			        			break;
						}
					}
					
					
					// add coverage to the result list
					results.add(coverage);
					
				}
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// write to the file
			try {
				FileOutputStream outFile = new FileOutputStream(getResultFileName(filepath));
		        if (workbook!=null) {
		        	workbook.write(outFile);
		        	workbook.close();
				}
		        outFile.close(); 
	        } catch (IOException e) {
				e.printStackTrace();
	        }
		}
		return results;
	}
	
	private static boolean isRowEmpty(Row row) {
		if (row == null) {
	        return true;
	    }
	    if (row.getLastCellNum() <= 0) {
	        return true;
	    }
	    for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
	        Cell cell = row.getCell(cellNum);
	        if (cell != null && cell.getCellTypeEnum() != CellType.BLANK ) {
	            return false;
	        }
	    }
	    return true;
	}
	
	private static String getResultFileName(String inputFileName) {
		String filename = Paths.get(inputFileName).getFileName().toString().replaceFirst("[.][^.]+$", "") +"_RESULTS.xlsx";;
		String path = (new File(inputFileName)).getParent();
		return Paths.get(path,filename).toString();
	}
	
	/*@PostConstruct
    protected void checkConfiguration() {
        
    }*/
	
	private static CellStyle createCellStyle(Workbook workbook, IndexedColors color) {
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(color.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		return style;
	}
	
	private FacilityStreetCoverage parseCoverageFromRow(Row currentRow) {
		FacilityStreetCoverage coverage = new FacilityStreetCoverage();

		if(currentRow.getCell(0).getCellTypeEnum() == CellType.STRING) { 
			String fullName = currentRow.getCell(fullNameCell).getStringCellValue();
			coverage.setFullName(fullName);
			System.out.println("full name : " + fullName); 
		} 
		if(currentRow.getCell(1).getCellTypeEnum() == CellType.STRING)  { 
			String divisionName = currentRow.getCell(divisionNameCell).getStringCellValue();
			coverage.setDivisionName(divisionName);
			System.out.println("division :" + divisionName);
		}
		if(currentRow.getCell(2).getCellTypeEnum() == CellType.STRING)  { 
			String rayon = currentRow.getCell(rayonCell).getStringCellValue();
			if (rayon.contains(RAYON) || rayon.contains(R_N)) coverage.setRayon(rayon);
			System.out.println("rayon :" + rayon);
		}
		if(currentRow.getCell(4).getCellTypeEnum() == CellType.STRING)  { 
			String locality = currentRow.getCell(localityCell).getStringCellValue();
			coverage.setLocality(locality);
			System.out.println("locality :" + locality);
		}
		if(currentRow.getCell(5).getCellTypeEnum() == CellType.NUMERIC)  { 
			String index = Integer.toString((int)currentRow.getCell(indexCell).getNumericCellValue());
			coverage.setIndex(index);
			System.out.println("index :" + index);
		}
		if(currentRow.getCell(6).getCellTypeEnum() == CellType.STRING)  { 
			String streetType = currentRow.getCell(streetTypeCell).getStringCellValue();
			coverage.setStreetType(streetType);
			if (streetType.equals(COVERS_ALL_STREETS)) coverage.setAllStreetsCoverage(true);
			System.out.println("street type:" + streetType + "; covers all streets:" + coverage.isAllStreetsCoverage());
		}

		if(currentRow.getCell(7).getCellTypeEnum() == CellType.STRING && !coverage.isAllStreetsCoverage())  { 
			String street = currentRow.getCell(streetCell).getStringCellValue();
			coverage.setStreet(street);
			System.out.println("street :" + street);
		}
		return coverage;
	}

}