package com.ehealth;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ehealth.entities.AddressValidationResult;
import com.ehealth.entities.FacilityStreetCoverage;

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

public class ExcelReader {

	private static final String INPUT_FILE_NAME = "/Users/ypmacc/Downloads/test5.xlsx";
	private static final String COVERS_ALL_STREETS = "Структурний підрозділ обслуговує всі адреси цього населеного пункту";
	private static final String STREET_WASNT_FOUND ="вулицю не знайдено";
	private static final String RAYON = "район";
	private static final String R_N = "р-н";
	private static final int TOP_INDENT = 16;
	private static final int OBLAST_ROW = 6;

	public static void main(String[] args) {

		List<FacilityStreetCoverage> results = readXls(INPUT_FILE_NAME);
			
		/*for (FacilityStreetCoverage coverage: results) {
			if (!coverage.isAllStreetsCoverage()) GoogleMapsAddressValidator.validate(coverage);
		}*/

	}
	public static List<FacilityStreetCoverage> readXls(String filepath) {
		List<FacilityStreetCoverage> results = new ArrayList<FacilityStreetCoverage>();
		Workbook workbook = null;
		try {

			FileInputStream excelFile = new FileInputStream(new File(filepath));
			workbook = new XSSFWorkbook(excelFile);
			Sheet datatypeSheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = datatypeSheet.iterator();
			
			CellStyle redStyle = workbook.createCellStyle();
			redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
			//redStyle.setFillPattern(CellStyle.ALIGN_FILL);
			redStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			
			
			CellStyle greenStyle = workbook.createCellStyle();
			greenStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
			//greenStyle.setFillPattern(CellStyle.ALIGN_FILL);
			greenStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);


			String oblast=null;	
			while (iterator.hasNext() ) {

				Row currentRow = iterator.next();
				// check if table ended
				if (isRowEmpty(currentRow) && currentRow.getRowNum() >= TOP_INDENT) break;
				// get the oblast
				if (currentRow.getRowNum()==OBLAST_ROW) {
					oblast = currentRow.getCell(0).getStringCellValue();
				}
				//process the row
				if (currentRow.getRowNum() >= TOP_INDENT) {
					FacilityStreetCoverage coverage = new FacilityStreetCoverage();

					if(currentRow.getCell(0).getCellTypeEnum() == CellType.STRING) { 
						String fullName = currentRow.getCell(0).getStringCellValue();
						coverage.setFullName(fullName);
						System.out.println("full name : " + fullName); 
					} 
					if(currentRow.getCell(1).getCellTypeEnum() == CellType.STRING)  { 
						String divisionName = currentRow.getCell(1).getStringCellValue();
						coverage.setDivisionName(divisionName);
						System.out.println("division :" + divisionName);
					}
					if(currentRow.getCell(2).getCellTypeEnum() == CellType.STRING)  { 
						String rayon = currentRow.getCell(2).getStringCellValue();
						if (rayon.contains(RAYON) || rayon.contains(R_N)) coverage.setRayon(rayon);
						System.out.println("rayon :" + rayon);
					}
					if(currentRow.getCell(4).getCellTypeEnum() == CellType.STRING)  { 
						String locality = currentRow.getCell(4).getStringCellValue();
						coverage.setLocality(locality);
						System.out.println("locality :" + locality);
					}
					if(currentRow.getCell(5).getCellTypeEnum() == CellType.NUMERIC)  { 
						String index = Integer.toString((int)currentRow.getCell(5).getNumericCellValue());
						coverage.setIndex(index);
						System.out.println("index :" + index);
					}
					if(currentRow.getCell(6).getCellTypeEnum() == CellType.STRING)  { 
						String streetType = currentRow.getCell(6).getStringCellValue();
						coverage.setStreetType(streetType);
						if (streetType.equals(COVERS_ALL_STREETS)) coverage.setAllStreetsCoverage(true);
						System.out.println("street type:" + streetType + "; covers all streets:" + coverage.isAllStreetsCoverage());
					}

					if(currentRow.getCell(7).getCellTypeEnum() == CellType.STRING && !coverage.isAllStreetsCoverage())  { 
						String street = currentRow.getCell(7).getStringCellValue();
						coverage.setStreet(street);
						System.out.println("street :" + street);
					}
					
					// set the oblast
					if (oblast!=null) coverage.setOblast(oblast);
					
					if (!coverage.isAllStreetsCoverage()) {
						// validate  the address
						AddressValidationResult valResult = GoogleMapsAddressValidator.validate(coverage);
						// write match result
						Cell cellMatch = currentRow.getCell(10);
						if(cellMatch == null) cellMatch = currentRow.createCell(10);
						cellMatch.setCellValue(valResult.isMatch());
						if (valResult.isMatch()) cellMatch.setCellStyle(greenStyle); else cellMatch.setCellStyle(redStyle);
						
						Cell cellStreet = currentRow.getCell(11);
						if(cellStreet == null) cellStreet = currentRow.createCell(11);
						if (valResult.getReturnedStreetName()==null || valResult.getReturnedStreetName().isEmpty()) {
							cellStreet.setCellValue(STREET_WASNT_FOUND);
						}
						else cellStreet.setCellValue(valResult.getReturnedStreetName());
						if (valResult.isMatch()) cellStreet.setCellStyle(greenStyle); else cellStreet.setCellStyle(redStyle);
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
				FileOutputStream outFile = new FileOutputStream(getResultFileName(INPUT_FILE_NAME));
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
	
	public static boolean isRowEmpty(Row row) {
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
		//String filename = p.getFileName().toString().replaceFirst("[.][^.]+$", "") +"_RESULTS.xlsx";
		String path = (new File(INPUT_FILE_NAME)).getParent();
		return Paths.get(path,filename).toString();
	}

}