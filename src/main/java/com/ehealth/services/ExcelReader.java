package com.ehealth.services;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ehealth.entities.AddressValidationResult;
import com.ehealth.entities.FacilityStreetCoverage;
import com.ehealth.entities.StreetNameMatchType;
import com.ehealth.ui.MainFrame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
	
    @Autowired
    private MainFrame frame;

	private String blockSeparator = String.join("", Collections.nCopies(40, "-"));
	

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
					// write coverage info to app UI
					writeCoverageInfo(coverage);
					
					if (!coverage.isAllStreetsCoverage()) {
						// validate  the address
						AddressValidationResult valResult = googleMapsAddressValidator.validate(coverage);
						// write validation result to app UI
						writeValidationResult(valResult);
						
						// update row with match results
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
			notify(e.getMessage());
		} catch (NotOfficeXmlFileException e) {
			notify(e.getMessage());
		} catch (IOException e) {
			notify(e.getMessage());
		} finally {
			// write to the file
			try {
		        if (workbook!=null) {
		        	FileOutputStream outFile = new FileOutputStream(getResultFileName(filepath));
		        	workbook.write(outFile);
		        	workbook.close();
		        	outFile.close();
				}		         
	        } catch (IOException e) {
	        	notify("Помилка запису файлу");
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
	
	private static CellStyle createCellStyle(Workbook workbook, IndexedColors color) {
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(color.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		return style;
	}
	
	private FacilityStreetCoverage parseCoverageFromRow(Row currentRow) {
		FacilityStreetCoverage coverage = new FacilityStreetCoverage();
		
		if(currentRow.getCell(fullNameCell).getCellTypeEnum() == CellType.STRING) { 
			String fullName = currentRow.getCell(fullNameCell).getStringCellValue();
			coverage.setFullName(fullName);
		} 
		if(currentRow.getCell(divisionNameCell).getCellTypeEnum() == CellType.STRING)  { 
			String divisionName = currentRow.getCell(divisionNameCell).getStringCellValue();
			coverage.setDivisionName(divisionName);
		}
		if(currentRow.getCell(rayonCell).getCellTypeEnum() == CellType.STRING)  { 
			String rayon = currentRow.getCell(rayonCell).getStringCellValue();
			if (rayon.contains(RAYON) || rayon.contains(R_N)) coverage.setRayon(rayon);
		}
		if(currentRow.getCell(localityCell).getCellTypeEnum() == CellType.STRING)  { 
			String locality = currentRow.getCell(localityCell).getStringCellValue();
			coverage.setLocality(locality);
		}
		if(currentRow.getCell(indexCell).getCellTypeEnum() == CellType.NUMERIC)  { 
			String index = Integer.toString((int)currentRow.getCell(indexCell).getNumericCellValue());
			coverage.setIndex(index);
		} else if(currentRow.getCell(indexCell).getCellTypeEnum() == CellType.STRING)  { 
			String index = currentRow.getCell(indexCell).getStringCellValue();
			coverage.setIndex(index);
		}
		if(currentRow.getCell(streetTypeCell).getCellTypeEnum() == CellType.STRING)  { 
			String streetType = currentRow.getCell(streetTypeCell).getStringCellValue();
			coverage.setStreetType(streetType);
			if (streetType.equals(COVERS_ALL_STREETS)) coverage.setAllStreetsCoverage(true);
		}

		if(currentRow.getCell(streetCell).getCellTypeEnum() == CellType.STRING && !coverage.isAllStreetsCoverage())  { 
			String street = currentRow.getCell(streetCell).getStringCellValue();
			coverage.setStreet(street);
		}
		return coverage;
	}
	
	private void writeLine(String line) {
		frame.addLine(line);
	}
	private void notify(String message) {
		frame.showDialog(message);
	}
	
	private void writeCoverageInfo(FacilityStreetCoverage coverage) {
		
		writeLine(blockSeparator);
	    writeLine("Заклад : " + coverage.getFullName()+"; Підрозділ :" + coverage.getDivisionName()); 
		writeLine("Район :" + coverage.getRayon()+"; Населений пункт :" + coverage.getLocality()+"; Індекс :" + coverage.getIndex());

		writeLine("Тип вулиці:" + coverage.getStreetType() + "; Обслуговує усі вулиці:" + coverage.isAllStreetsCoverage());
		if (coverage.getStreet()!=null) writeLine("Назва :" + coverage.getStreet());
	}
	
	private void writeValidationResult(AddressValidationResult valResult) {
		writeLine("Повернуто Google: "+valResult.getReturnedStreetName());
		writeLine("Результат: "+valResult.getMatchType());
	}

}