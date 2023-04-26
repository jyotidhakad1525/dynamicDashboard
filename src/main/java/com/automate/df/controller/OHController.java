package com.automate.df.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.automate.df.entity.oh.LocationNodeData;
import com.automate.df.entity.oh.LocationNodeDef;
import com.automate.df.entity.salesgap.DmsEmployee;
import com.automate.df.exception.DynamicFormsServiceException;
import com.automate.df.model.AttendanceReportRequest;
import com.automate.df.model.BranchResponce;
import com.automate.df.model.BulkUploadModel;
import com.automate.df.model.BulkUploadResponse;
import com.automate.df.model.oh.LocationDefNodeRes;
import com.automate.df.model.oh.OHEmpLevelMapping;
import com.automate.df.model.oh.OHEmpLevelMappingV2;
import com.automate.df.model.oh.OHEmpLevelUpdateMapReq;
import com.automate.df.model.oh.OHLeveDeleteReq;
import com.automate.df.model.oh.OHLevelReq;
import com.automate.df.model.oh.OHLevelUpdateReq;
import com.automate.df.model.oh.OHNodeUpdateReq;
import com.automate.df.model.oh.OHRes;
import com.automate.df.model.salesgap.SuperSetFilterDropDown;
import com.automate.df.model.salesgap.TargetDropDownV2;
import com.automate.df.model.salesgap.TargetDropDownV3;
import com.automate.df.model.salesgap.TeamAttendanceResponse;
import com.automate.df.service.OHService;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Api(value = "/Org Hierarchy", tags = "Org Hierarchy", description = "Org Hierarchy")
@RequestMapping(value="/oh")
public class OHController {

	@Autowired
	Environment env;
	
	@Autowired
	OHService ohService;
	
	
	@CrossOrigin
	@GetMapping(value="/child-dropdown-data")
	public ResponseEntity<List<OHRes>> getOHDropdown(
			@RequestParam(name="orgId",required = true) Integer orgId,
			@RequestParam(name="empId",required = true) Integer empId,
			@RequestParam(name="parent_key_id",required = true) Integer id
			)
			throws DynamicFormsServiceException {
		List<OHRes> response = null;
		if (Optional.of(orgId).isPresent() && Optional.of(empId).isPresent()) {
			response = ohService.getOHDropdown(orgId,empId,id);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
	@CrossOrigin
	@GetMapping(value="/emp-level-data")
	public ResponseEntity<List<String>> getLevelData(
			@RequestParam(name="orgId",required = true) Integer orgId,
			@RequestParam(name="empId",required = true) Integer empId
			)
			throws DynamicFormsServiceException {
		List<String> response = null;
		if (Optional.of(orgId).isPresent() && Optional.of(empId).isPresent()) {
			response = ohService.getLevelData(orgId,empId);
		} else {
			throw new DynamicFormsServiceException("Bad Request", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
	
	@CrossOrigin
	@GetMapping(value="/emp-parent-dropdown")
	public ResponseEntity<List<OHRes>> getEmpParentDropdown(
			@RequestParam(name="orgId",required = true) Integer orgId,
			@RequestParam(name="empId",required = true) Integer empId
			)
			throws DynamicFormsServiceException {
		List<OHRes> response = null;
		if (Optional.of(orgId).isPresent() && Optional.of(empId).isPresent()) {
			response = ohService.getEmpParentDropdown(orgId,empId);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@GetMapping(value="/emp-branches")
	public ResponseEntity<List<OHRes>> getEmpBranches(
			@RequestParam(name="orgId",required = true) Integer orgId,
			@RequestParam(name="empId",required = true) Integer empId
			)
			throws DynamicFormsServiceException {
		List<OHRes> response = null;
		if (Optional.of(orgId).isPresent() && Optional.of(empId).isPresent()) {
			response = ohService.getEmpBranches(orgId,empId);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
/*	
	@CrossOrigin
	@PostMapping(value="/emp-level-data-mapping")
	public ResponseEntity<?> addOHMapping(@RequestBody LevelDataReq req)
			throws DynamicFormsServiceException {
		String response = null;
		if (Optional.of(req).isPresent()) {
			response = ohService.addOHMapping(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}*/

	@CrossOrigin
	@GetMapping(value="/org-levels")
	public ResponseEntity<List<LocationNodeDef>> getOrgLevels(@RequestParam(name="orgId",required = true) Integer orgId)
			throws DynamicFormsServiceException {
		List<LocationNodeDef> response = null;
		if (Optional.of(orgId).isPresent()) {
			response = ohService.getOrgLevels(orgId);
		} else {
			throw new DynamicFormsServiceException("Bad Request", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@GetMapping(value="/data-nodes")
	public ResponseEntity<List<LocationNodeData>> getLevelDataNodes(
			@RequestParam(name="orgId",required = true) Integer orgId,
			@RequestParam(name="levelCode",required = true) String levelCode
			)
			throws DynamicFormsServiceException {
		List<LocationNodeData> response = null;
		if (Optional.of(orgId).isPresent() &&Optional.of(levelCode).isPresent() ) {
			response = ohService.getLevelDataNodes(orgId,levelCode);
		} else {
			throw new DynamicFormsServiceException("Bad Request", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value="/level-data-creation")
	public ResponseEntity<?> createDataLevels(@RequestBody OHLevelReq req)
			throws DynamicFormsServiceException {
		List<?> response = null;
		if (Optional.of(req).isPresent()) {
			response = ohService.createLevels(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value="/org-level-removal")
	public ResponseEntity<?> removeDataLevels(@RequestBody OHLeveDeleteReq req)
			throws DynamicFormsServiceException {
		List<?> response = null;
		if (Optional.of(req).isPresent()) {
			response = ohService.removeDataLevels(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/*
	@CrossOrigin
	@PostMapping(value="/org-level-update")
	public ResponseEntity<?> updateOrgLevels(@RequestBody OHLeveUpdateReq req)
			throws DynamicFormsServiceException {
		List<?> response = null;
		if (Optional.of(req).isPresent()) {
			response = ohService.updateOrgLevels(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	*/
	@CrossOrigin
	@PostMapping(value="/emp-level-data-mapping")
	public ResponseEntity<?> setEmpLevelMapping(@RequestBody OHEmpLevelMapping req)
			throws DynamicFormsServiceException {
		List<?> response = null;
		if (Optional.of(req).isPresent()) {
			response = ohService.setEmpLevelMapping(req,"Y");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value="/emp-multilevel-data-mapping")
	public ResponseEntity<?> setEmpLevelMappingMultiple(@RequestBody OHEmpLevelMappingV2 req)
			throws DynamicFormsServiceException {
		List<?> response = null;
		if (Optional.of(req).isPresent()) {
			response = ohService.setEmpLevelMappingMultiple(req,"Y");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PutMapping(value="/update-emp-level-mapping")
	public ResponseEntity<?> updateEmpLevelMapping(@RequestBody OHEmpLevelUpdateMapReq req)
			throws DynamicFormsServiceException {
		String response = null;
		if (Optional.of(req).isPresent()) {
			response = ohService.updateEmpLevelMapping(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value="/active-emp-mappings")
	public ResponseEntity<?> getActiveEmpMappings(@RequestParam(name="orgId") Integer orgId,
			@RequestParam(name="empId") Integer empId)
			throws DynamicFormsServiceException {
		List<LocationNodeData> response = null;
		if (Optional.of(empId).isPresent() && Optional.of(orgId).isPresent()) {
			response = ohService.getActiveEmpMappings(orgId,empId);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value="/active-org-mappings-all")
	public ResponseEntity<?> getActiveEmpMappingsAll(@RequestParam(name="orgId") Integer orgId
			)
			throws DynamicFormsServiceException {
		List<LocationDefNodeRes> response = null;
		if (Optional.of(orgId).isPresent()) {
			
			response = ohService.getActiveEmpMappingsAll(orgId);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@GetMapping(value="/employees/{orgId}")
	public ResponseEntity<?> getEmployeesListWithMapping(@RequestParam(name="pageNo") Integer pageNo,
			@RequestParam(name="size") Integer size ,@PathVariable(name="orgId") Integer orgId)
			throws DynamicFormsServiceException {
		Map<String, Object> response = null;
		if (Optional.of(pageNo).isPresent() && Optional.of(size).isPresent()) {
			response = ohService.getEmployeesListWithMapping(pageNo,size,orgId);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value="/employees/create")
	public ResponseEntity<DmsEmployee> createEmployee(@RequestBody DmsEmployee dmsEmployee)
			throws DynamicFormsServiceException {
		DmsEmployee response = null;
		if (Optional.of(dmsEmployee).isPresent()) {
			response=ohService.saveEmployee(dmsEmployee);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value="/employees/update")
	public ResponseEntity<DmsEmployee> updateEmployee(@RequestBody DmsEmployee dmsEmployee)
			throws DynamicFormsServiceException {
		DmsEmployee response = null;
		if (Optional.of(dmsEmployee).isPresent()) {
			response=ohService.updateEmployee(dmsEmployee);
			if(response==null) { // emp doesnt exist
				throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
			}
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@GetMapping(value="/employees/{type}/{orgId}")
	public ResponseEntity<?> getMappedEmployees(@PathVariable(name="type") String type,
			@PathVariable(name="orgId") String orgId)
			throws DynamicFormsServiceException {
			return new ResponseEntity<>(ohService.getMappedEmployees(type,orgId), HttpStatus.OK);
	}
	
	@CrossOrigin
	@GetMapping(value="/active-mappings/{empId}")
	public ResponseEntity<?> getMappingByEmpId(@PathVariable(name="empId") Integer empId)
			throws DynamicFormsServiceException {
			return new ResponseEntity<>(ohService.getMappingByEmpId(empId), HttpStatus.OK);
	}
	
	@CrossOrigin
	@PutMapping(value="/org-level-update")
	public ResponseEntity<?> updateOrgLevels(@RequestBody OHLevelUpdateReq req)
			throws DynamicFormsServiceException {
		String response = null;
		if (Optional.of(req).isPresent()) {
			response = ohService.updateOrgLevels(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PutMapping(value="/update-node-displayname")
	public ResponseEntity<?> updateNodes(@RequestBody OHNodeUpdateReq req)
			throws DynamicFormsServiceException {
		String response = null;
		if (Optional.of(req).isPresent()) {
			response = ohService.updateNodes(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/*
	@CrossOrigin
	@GetMapping(value="/active-dropdowns/{orgId}/{empId}")
	public ResponseEntity<?> getActiveDropdowns(@PathVariable(name="empId") Integer empId,
			@PathVariable(name="orgId") Integer orgId)
			throws DynamicFormsServiceException {
			return new ResponseEntity<>(ohService.getActiveDropdowns(orgId,empId), HttpStatus.OK);
	}
	*/
	@CrossOrigin
	@GetMapping(value="/active-levels/{orgId}/{empId}")
	public ResponseEntity<?> getActiveLevels(@PathVariable(name="empId") Integer empId,
			@PathVariable(name="orgId") Integer orgId)
			throws DynamicFormsServiceException {
			return new ResponseEntity<>(ohService.getActiveLevels(orgId,empId), HttpStatus.OK);
	}
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    Set<Object> seen = ConcurrentHashMap.newKeySet();
	    return t -> seen.add(keyExtractor.apply(t));
	}

	@CrossOrigin
	@GetMapping(value="/active-branches/{orgId}/{empId}")
	public ResponseEntity<?> getActiveBranches(@PathVariable(name="empId") Integer empId,
			@PathVariable(name="orgId") Integer orgId)
			throws DynamicFormsServiceException {
			return new ResponseEntity<>(ohService.getActiveBranches(orgId,empId), HttpStatus.OK);
	}

	
	@CrossOrigin
	@PostMapping(value="/active-dropdowns/{orgId}/{empId}")
	public ResponseEntity<?> getActiveDropdowns(@RequestBody  List<Integer> levelList,@PathVariable(name="empId") Integer empId,
			@PathVariable(name="orgId") Integer orgId)
			throws DynamicFormsServiceException {
			Map<String, Object> dataMap = ohService.getActiveDropdownsV2(levelList,orgId,empId);
			Map<String, Object> formattedMap = new LinkedHashMap<>();
			dataMap.forEach((k,v)->{
				Map<String, Object> innerMap=(Map<String, Object>)v;
				innerMap.forEach((x,y)->{
					List<TargetDropDownV2> ddList = (List<TargetDropDownV2>)y;
					ddList=	ddList.stream().distinct().collect(Collectors.toList());
					if(formattedMap.containsKey(x)) {
						List<TargetDropDownV2> l = (List<TargetDropDownV2>)formattedMap.get(x);
						
						l.addAll(ddList);
						l=	l.stream().distinct().collect(Collectors.toList());
						formattedMap.put(x, l);
					}else {
						formattedMap.put(x, ddList);
					}
				});
			});


		formattedMap.get("Sales Manager");
		Map<String, Object> formattedMap1 = new LinkedHashMap<>();


//		for (int i = 0; i <formattedMap.size() ; i++) {

			if (formattedMap.get("MD") !=null) {
				formattedMap1.put("MD",formattedMap.get("MD"));
			}
			
			if (formattedMap.get("CEO,CTO,CXO") !=null) {
				formattedMap1.put("CEO,CTO,CXO",formattedMap.get("CEO,CTO,CXO"));
			}

			if (formattedMap.get("BUSINESS HEAD") !=null) {
				formattedMap1.put("BUSINESS HEAD",formattedMap.get("BUSINESS HEAD"));
			}


			if(formattedMap.get("General Manager") !=null){
				formattedMap1.put("General Manager",formattedMap.get("General Manager"));
			}
			if(formattedMap.get("GM") !=null){
				formattedMap1.put("GM",formattedMap.get("GM"));
			}
			if(formattedMap.get("Branch Manager") !=null){
				formattedMap1.put("Branch Manager",formattedMap.get("Branch Manager"));
			}
			if (formattedMap.get("Sales Manager") !=null) {
				formattedMap1.put("Sales Manager",formattedMap.get("Sales Manager"));
			}

			if (formattedMap.get("Assistant Manager") !=null) {
				formattedMap1.put("Assistant Manager",formattedMap.get("Assistant Manager"));
			}

			if (formattedMap.get("TL") !=null) {
				formattedMap1.put("TL",formattedMap.get("TL"));
			}
			if (formattedMap.get("Sales Consultant") !=null) {
				formattedMap1.put("Sales Consultant",formattedMap.get("Sales Consultant"));
			}

			System.out.println(formattedMap);
//		}


		return new ResponseEntity<>(formattedMap1, HttpStatus.OK);
	}
	
	
	@CrossOrigin
	@PostMapping(value="/team-attendance/{orgId}/{empId}/{startDate}/{endDate}")
	public ResponseEntity<?> getTeamDropdowns(@RequestBody  List<Integer> levelList,@PathVariable(name="empId") Integer empId,
			@PathVariable(name="orgId") Integer orgId,@PathVariable("startDate") String startDate,@PathVariable("endDate") String endDate)
			throws DynamicFormsServiceException {	
			String startDate3 = startDate.subSequence(0, 9)+" "+"00:00:00";
			String endDate3 = endDate.subSequence(0, 9) +" "+"23:59:59";
			startDate = startDate +" "+"00:00:00";
			endDate = endDate +" "+"23:59:59";
			System.out.println(startDate+"-----------------startDate");
			System.out.println(endDate+"-----------------enddate");
			Map<String, Object> dataMap = ohService.getTeamActiveDropdownsV3(levelList,orgId,empId,startDate,endDate);
			Map<String, Object> formattedMap = new LinkedHashMap<>();
			dataMap.forEach((k,v)->{
				Map<String, Object> innerMap=(Map<String, Object>)v;
				innerMap.forEach((x,y)->{
					List<TeamAttendanceResponse> ddList = (List<TeamAttendanceResponse>)y;
					ddList=	ddList.stream().distinct().collect(Collectors.toList());
					if(formattedMap.containsKey(x)) {
						List<TeamAttendanceResponse> l = (List<TeamAttendanceResponse>)formattedMap.get(x);
						
						l.addAll(ddList);
						l=	l.stream().distinct().collect(Collectors.toList());
						formattedMap.put(x, l);
					}else {
						formattedMap.put(x, ddList);
					}
				});
			});
			
			return new ResponseEntity<>(formattedMap, HttpStatus.OK);
	}
	
	@CrossOrigin
	@GetMapping(value="/levelForDealer/{orgId}/{empId}")
	public ResponseEntity<?> getlevelForDealer(@PathVariable(name="empId") Integer empId,
			@PathVariable(name="orgId") Integer orgId)
			throws DynamicFormsServiceException {
			return new ResponseEntity<>(ohService.getlevelForDealer(orgId,empId), HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/uploadBulkUploadForEmployee")
	public ResponseEntity<?> uploadBulkUploadForEmployee(@RequestPart("file") MultipartFile bulkExcel,
		    @RequestPart("bumodel") BulkUploadModel bUModel) {
		Integer empId=bUModel.getEmpId();
		Integer orgId=bUModel.getOrgid();
		Integer branchId=bUModel.getBranchId();
		BulkUploadResponse  response =null;
		try {	
			if(null != orgId && null != empId) {
				response = ohService.processBulkExcelForEmployee(empId,orgId,branchId,bulkExcel);
			}
		} catch (Exception e) {
			BulkUploadResponse res = new BulkUploadResponse();
			List<String> FailedRecords =new ArrayList<>();
			String resonForFailure = e.getMessage();
			FailedRecords.add(resonForFailure);
			res.setFailedCount(0);
			res.setFailedRecords(FailedRecords);
			res.setSuccessCount(0);
			res.setTotalCount(0);
			return new ResponseEntity<>(res, HttpStatus.OK);
		}
		return new ResponseEntity<>(response, HttpStatus.CREATED);
		
    }
	
	@CrossOrigin
	@PostMapping(value="/active-dropdownsForSuperSet/{orgId}/{empId}")
	public ResponseEntity<?> getActiveDropdownsForSuperSet(@RequestBody  List<Integer> levelList,@PathVariable(name="empId") Integer empId,
			@PathVariable(name="orgId") Integer orgId)
			throws DynamicFormsServiceException {
			Map<String, Object> dataMap = ohService.getActiveDropdownsV3(levelList,orgId,empId);
			Map<String, Object> formattedMap = new LinkedHashMap<>();
			SuperSetFilterDropDown response =new SuperSetFilterDropDown();
			response.setOrg_id(orgId);
			List<Integer> userIds =new ArrayList<>();
			dataMap.forEach((k,v)->{
				Map<String, Object> innerMap=(Map<String, Object>)v;
				innerMap.forEach((x,y)->{
					List<TargetDropDownV3> ddList = (List<TargetDropDownV3>)y;
					ddList=	ddList.stream().distinct().collect(Collectors.toList());
					if(formattedMap.containsKey(x)) {
						List<TargetDropDownV3> l = (List<TargetDropDownV3>)formattedMap.get(x);
						l.addAll(ddList);
						l=	l.stream().distinct().collect(Collectors.toList());
						formattedMap.put(x, l);
						l.stream().distinct().forEach(e->userIds.add(Integer.valueOf(e.getUserId())));
					}else {
						formattedMap.put(x, ddList);
						ddList.stream().distinct().forEach(e->userIds.add(Integer.valueOf(e.getUserId())));
					}
				});
			});
			response.setUser_ids(userIds);
			return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/forTeamAttandanceReport/{orgId}/{empId}")
	public ResponseEntity<?> forTeamAttandanceReport(@PathVariable(name = "empId") Integer empId,
			@PathVariable(name = "orgId") Integer orgId, @RequestBody AttendanceReportRequest attendanceRequest)
			throws DynamicFormsServiceException {
		List<Integer> inputEmployeeIds = attendanceRequest.getEmpIds();
		inputEmployeeIds.add(empId);
		Set<Integer> demoEmpList = new HashSet<>();
		for (Integer empIds : inputEmployeeIds) {
			ResponseEntity<?> activeDropdownsForSuperSet = getActiveDropdownsForSuperSet(
					attendanceRequest.getDealerCodes(), empIds, orgId);

			if (activeDropdownsForSuperSet != null) {
				@SuppressWarnings("unchecked")
				SuperSetFilterDropDown body = (SuperSetFilterDropDown) activeDropdownsForSuperSet.getBody();
				
				if (body!=null) {
					List<Integer> listedEmployee = body.getUser_ids();
					listedEmployee.stream().forEach(b -> demoEmpList.add(b));
				}
			}
		}
		List<Integer> finalEmployeeIds = new ArrayList<>(demoEmpList);
		return new ResponseEntity<>(finalEmployeeIds, HttpStatus.OK);
	}
	
	@CrossOrigin
	@GetMapping(value="/getLocationBasedBranches")
	public ResponseEntity<List<BranchResponce>> getBranches(
			@RequestParam(name="orgId",required = true) Integer orgId,
			@RequestParam(name="locationId",required = true) Integer locationId
			)
			throws DynamicFormsServiceException {
		List<BranchResponce> response = null;
		if (Optional.of(orgId).isPresent() &&Optional.of(locationId).isPresent() ) {
			response = ohService.getBranches(orgId,locationId);
		} else {
			throw new DynamicFormsServiceException("Bad Request", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}


	@CrossOrigin
	@GetMapping(value="/getcrmchild")
	public ResponseEntity<List<BranchResponce>> getCrmChild(
			@RequestParam(name="orgId",required = true) Integer orgId,
			@RequestParam(name="locationId",required = true) Integer locationId
	)
			throws DynamicFormsServiceException {
		List<BranchResponce> response = null;
		if (Optional.of(orgId).isPresent() &&Optional.of(locationId).isPresent() ) {
			response = ohService.getBranches(orgId,locationId);
		} else {
			throw new DynamicFormsServiceException("Bad Request", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}



	@CrossOrigin
	@PostMapping(value="/getcrmchild/{dashboard_type}/{orgId}/{empId}")
	public ResponseEntity<?> getCrmChild(@RequestBody  List<Integer> levelList,
										 @PathVariable(name="dashboard_type") String dashboard_type,
										 @PathVariable(name="empId") Integer empId,
										 @PathVariable(name="orgId") Integer orgId)
			throws DynamicFormsServiceException {

		Map<String, Object> dataMap = ohService.getCRMChildUser(levelList,orgId,empId,dashboard_type);
		return new ResponseEntity<>(dataMap, HttpStatus.OK);
	}
}
