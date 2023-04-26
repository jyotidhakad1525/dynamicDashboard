package com.automate.df.service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.automate.df.dao.LostSubLostRepository;
import com.automate.df.dao.SubLostResonsRepository;
import com.automate.df.entity.LostReasons;
import com.automate.df.entity.SubLostReasons;
import com.automate.df.model.BulkUploadResponse;

@Service
public class LostSubLostServices {
	@Autowired
	LostSubLostRepository lostsublostRepo;
	@Autowired
	SubLostResonsRepository subLostResonsRepository; 
	public List<LostReasons> getAllSubLostAllDetails(String orgId, String stageName) {
		return lostsublostRepo.getAllSubLost(orgId, stageName);
	}

	public BulkUploadResponse processBulkExcelForSubLostReasons(Integer empId, Integer orgId, Integer branchId,
			MultipartFile bulkExcel) throws Exception {
		Resource file = null;
		if (bulkExcel.isEmpty()) {
			BulkUploadResponse res = new BulkUploadResponse();
			List<String> FailedRecords = new ArrayList<>();
			String resonForFailure = "File not found";
			FailedRecords.add(resonForFailure);
			res.setFailedCount(0);
			res.setFailedRecords(FailedRecords);
			res.setSuccessCount(0);
			res.setTotalCount(0);
			return res;
		}
		Path tmpDir = Files.createTempDirectory("temp");
		Path tempFilePath = tmpDir.resolve(bulkExcel.getOriginalFilename());
		Files.write(tempFilePath, bulkExcel.getBytes());
		String fileName = bulkExcel.getOriginalFilename();
		fileName = fileName.substring(0, fileName.indexOf("."));
		return processBulkExcelSubLost(tempFilePath.toString(), empId, orgId, branchId);
	}

	public BulkUploadResponse processBulkExcelSubLost(String inputFilePath, Integer empId, Integer orgId,
			Integer branchId) throws Exception {

		Workbook workbook = null;
		Sheet sheet = null;
		List<SubLostReasons> subLostReasons = new ArrayList<>();
		workbook = getWorkBook(new File(inputFilePath));
		sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();
		List<String> FailedRecords = new ArrayList<>();
		int TotalCount = -1;
		int SuccessCount = 0;
		int FailedCount = 0;
		int emptyCheck = 0;
		BulkUploadResponse res = new BulkUploadResponse();
		while (rowIterator.hasNext()) {
			TotalCount++;
			Row row = rowIterator.next();
			try {
				if (row.getRowNum() != 0) {
					emptyCheck++;
					SubLostReasons subLostReasonsObj = new SubLostReasons();
					if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 0))) {
						subLostReasonsObj.setStageName(getCellValueBasedOnCellType(row, 0));
					}else {
						throw new Exception("Provide valid Stage");
					}
					if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 1))) {
						subLostReasonsObj.setLostReason(getCellValueBasedOnCellType(row, 1));
						List<LostReasons> lostReasonIdByName = lostsublostRepo.getLostReasonOrgIdNameStage(String.valueOf(orgId),
								getCellValueBasedOnCellType(row, 1),getCellValueBasedOnCellType(row, 0));
						if(lostReasonIdByName.size()==0) {
							throw new Exception("Provide valid LostReason,Stage");
						}else {
							subLostReasonsObj.setLostreasonId(lostReasonIdByName.get(0).getId());
						}
					}
					if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 2))) {
						subLostReasonsObj.setSubReason(getCellValueBasedOnCellType(row, 2));
					}
					if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 3))) {
						subLostReasonsObj.setStatus(getCellValueBasedOnCellType(row, 3));
					}else {
						throw new Exception("Status filed is Mandatory");
					}
					subLostReasonsObj.setOrgId(String.valueOf(orgId));
					subLostReasonsObj.setCreatedBy(String.valueOf(empId));
					subLostReasonsObj.setUpdatedBy(String.valueOf(empId));
					subLostReasonsObj.setCreatedAt(String.valueOf(new Timestamp(System.currentTimeMillis())));
					subLostReasonsObj.setUpdatedAt(String.valueOf(new Timestamp(System.currentTimeMillis())));
					subLostReasons.add(subLostReasonsObj);
				}
			} catch (Exception e) {
				String resonForFailure = e.getMessage();
				System.out.println(resonForFailure);
				FailedRecords.add(resonForFailure);
				continue;
			}
		}
		if (emptyCheck == 0) {
			String resonForFailure = "DATA NOT FOUND";
			System.out.println(resonForFailure);
			FailedRecords.add(resonForFailure);
		}

		int j = 0;
		for (SubLostReasons reasons : subLostReasons) {
			try {
				j++;
				if(reasons.getStageName().equalsIgnoreCase("Contacts")) {
					reasons.setStageName("Pre Enquiry");
				}
				if(reasons.getStageName().equalsIgnoreCase("Booking Approval")) {
					reasons.setStageName("Pre Booking");
				}
				subLostResonsRepository.save(reasons);
				SuccessCount++;
			} catch (DataAccessException e) {
				String resonForFailure = "DUPLICATE ENTRY IN " + j + " ROW FOUND";
				System.out.println(resonForFailure);
				FailedRecords.add(resonForFailure);
				continue;
			} catch (Exception e) {
				String resonForFailure = "ERROR IN SAVEING DATA FOR " + j + " ROW " + e.getMessage();
				System.out.println(resonForFailure);
				FailedRecords.add(resonForFailure);
				continue;
			}
		}
		FailedCount = TotalCount - SuccessCount;
		res.setFailedCount(FailedCount);
		res.setFailedRecords(FailedRecords);
		res.setSuccessCount(SuccessCount);
		res.setTotalCount(TotalCount);
		return res;
	}

	private Workbook getWorkBook(File fileName) {
		Workbook workbook = null;
		try {
			String myFileName = fileName.getName();
			String extension = myFileName.substring(myFileName.lastIndexOf("."));
			if (extension.equalsIgnoreCase(".xls")) {
				workbook = new HSSFWorkbook(new FileInputStream(fileName));
			} else if (extension.equalsIgnoreCase(".xlsx")) {
				workbook = new XSSFWorkbook(new FileInputStream(fileName));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return workbook;
	}

	private String getCellValueBasedOnCellType(Row rowData, int columnPosition) {
		String cellValue = null;
		Cell cell = rowData.getCell(columnPosition);
		if (cell != null) {
			if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				String inputCellValue = cell.getStringCellValue();
				if (inputCellValue.endsWith(".0")) {
					inputCellValue = inputCellValue.substring(0, inputCellValue.length() - 2);
				}
				cellValue = inputCellValue;
			} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				if (DateUtil.isCellDateFormatted(cell)) {

					DateFormat df = new SimpleDateFormat("yyyy-mm-dd");

					Date today = cell.getDateCellValue();

					cellValue = df.format(today);

				} else {
					Integer doubleVal = (int) cell.getNumericCellValue();
					cellValue = Integer.toString(doubleVal);
				}
			}

		}
		return cellValue;
	}
	
	public BulkUploadResponse processBulkExcelForLostReasons(Integer empId, Integer orgId, Integer branchId,
			MultipartFile bulkExcel) throws Exception {
		Resource file = null;
		if (bulkExcel.isEmpty()) {
			BulkUploadResponse res = new BulkUploadResponse();
			List<String> FailedRecords = new ArrayList<>();
			String resonForFailure = "File not found";
			FailedRecords.add(resonForFailure);
			res.setFailedCount(0);
			res.setFailedRecords(FailedRecords);
			res.setSuccessCount(0);
			res.setTotalCount(0);
			return res;
		}
		Path tmpDir = Files.createTempDirectory("temp");
		Path tempFilePath = tmpDir.resolve(bulkExcel.getOriginalFilename());
		Files.write(tempFilePath, bulkExcel.getBytes());
		String fileName = bulkExcel.getOriginalFilename();
		fileName = fileName.substring(0, fileName.indexOf("."));
		return processBulkExcelLostReason(tempFilePath.toString(), empId, orgId, branchId);
	}
	
	public BulkUploadResponse processBulkExcelLostReason(String inputFilePath, Integer empId, Integer orgId,
			Integer branchId) throws Exception {
		Workbook workbook = null;
		Sheet sheet = null;
		List<LostReasons> lostReasons = new ArrayList<>();
		workbook = getWorkBook(new File(inputFilePath));
		sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();
		List<String> FailedRecords = new ArrayList<>();
		int TotalCount = -1;
		int SuccessCount = 0;
		int FailedCount = 0;
		int emptyCheck = 0;
		BulkUploadResponse res = new BulkUploadResponse();
		while (rowIterator.hasNext()) {
			TotalCount++;
			Row row = rowIterator.next();
			try {
				if (row.getRowNum() != 0) {
					emptyCheck++;
					LostReasons lostReasonsObj = new LostReasons();
					if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 0))) {
						lostReasonsObj.setStageName(getCellValueBasedOnCellType(row, 0));
					}else {
						throw new Exception("Stage Name  is Mandatory");
					}
					if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 1))) {
						lostReasonsObj.setLostReason(getCellValueBasedOnCellType(row, 1));
					}else {
						throw new Exception("Lost Reason filed is Mandatory");
					}
					if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 2))) {
						lostReasonsObj.setStatus(getCellValueBasedOnCellType(row, 2));
					}else {
						throw new Exception("Status filed is Mandatory");
					}
					lostReasonsObj.setCreatedBy(String.valueOf(empId));
					lostReasonsObj.setOrgId(String.valueOf(orgId));
					lostReasonsObj.setUpdatedBy(String.valueOf(empId));
					lostReasonsObj.setCreatedAt(String.valueOf(new Timestamp(System.currentTimeMillis())));
					lostReasonsObj.setUpdatedAt(String.valueOf(new Timestamp(System.currentTimeMillis())));
					lostReasons.add(lostReasonsObj);
				}
			} catch (Exception e) {
				String resonForFailure = e.getMessage();
				System.out.println(resonForFailure);
				FailedRecords.add(resonForFailure);
				continue;
			}
		}
		if (emptyCheck == 0) {
			String resonForFailure = "DATA NOT FOUND";
			System.out.println(resonForFailure);
			FailedRecords.add(resonForFailure);
		}

		int j = 0;
		for (LostReasons reasons : lostReasons) {
			try {
				j++;
				if(reasons.getStageName().equalsIgnoreCase("Contacts")) {
					reasons.setStageName("Pre Enquiry");
				}
				if(reasons.getStageName().equalsIgnoreCase("Booking Approval")) {
					reasons.setStageName("Pre Booking");
				}
				lostsublostRepo.save(reasons);
				SuccessCount++;
			} catch (DataAccessException e) {
				String resonForFailure = "DUPLICATE ENTRY IN " + j + " ROW FOUND";
				System.out.println(resonForFailure);
				FailedRecords.add(resonForFailure);
				continue;
			} catch (Exception e) {
				String resonForFailure = "ERROR IN SAVEING DATA FOR " + j + " ROW " + e.getMessage();
				System.out.println(resonForFailure);
				FailedRecords.add(resonForFailure);
				continue;
			}
		}
		FailedCount = TotalCount - SuccessCount;
		res.setFailedCount(FailedCount);
		res.setFailedRecords(FailedRecords);
		res.setSuccessCount(SuccessCount);
		res.setTotalCount(TotalCount);
		return res;
	}

}