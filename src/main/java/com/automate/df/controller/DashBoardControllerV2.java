package com.automate.df.controller;

import java.util.*;
import java.util.stream.Collectors;

import com.automate.df.model.DmsLeadDropInqResponse;
import com.automate.df.model.oh.OHRes;
import com.automate.df.model.oh.TodaysTaskRes2;
import com.automate.df.model.salesgap.TargetDropDownV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.automate.df.dao.DmsTargetParamSchedularRepository;
import com.automate.df.dao.dashboard.TargetAchivementEvent;
import com.automate.df.dao.dashboard.TargetAchivementModelandSource;
import com.automate.df.entity.DmsTargetParamSchedular;
import com.automate.df.exception.DynamicFormsServiceException;
import com.automate.df.model.MyTaskReq;
import com.automate.df.model.df.dashboard.DashBoardReqImmediateHierarchyV2;
import com.automate.df.model.df.dashboard.DashBoardReqV2;
import com.automate.df.model.df.dashboard.EventDataRes;
import com.automate.df.model.df.dashboard.LeadSourceRes;
import com.automate.df.model.df.dashboard.OverAllTargetAchivements;
import com.automate.df.model.df.dashboard.SalesDataRes;
import com.automate.df.model.df.dashboard.TargetAchivement;
import com.automate.df.model.df.dashboard.TargetRankingRes;
import com.automate.df.model.df.dashboard.VehicleModelRes;
import com.automate.df.service.DashBoardServiceV2;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/** 
 * 
 * @author srujan
 *
 */
@RestController
@Slf4j
@Api(value = "/dashboard", tags = "dashboard V2", description = "dashboard V2")
@RequestMapping(value="/dashboard")
public class DashBoardControllerV2 {
	
	@Autowired
	Environment env;
	

	@Autowired
	DashBoardServiceV2 dashBoardService;
	@Autowired
	DmsTargetParamSchedularRepository targetparamschedularRepo;
	
	@CrossOrigin
	@PostMapping(value = "v2/get_target_params")
	public ResponseEntity<List<TargetAchivement>> getTargetAchivementParams(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<TargetAchivement> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getTargetAchivementParams(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
	@CrossOrigin
	@PostMapping(value = "v2/get_target_params_events")
	public ResponseEntity<List<TargetAchivementEvent>> getTargetAchivementParamsEvents(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<TargetAchivementEvent> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getTargetAchivementParamsEvents(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "v2/get_target_params_for_all_emps")
	public ResponseEntity<OverAllTargetAchivements> getTargetAchivementParamsByEmps(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		OverAllTargetAchivements response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getTargetAchivementParamsWithEmps(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "v2/get_target_param_headings")
	public ResponseEntity<List<String>> getTargetAchivementParamsByEmps()
			throws DynamicFormsServiceException {
		List<String> list = new ArrayList<>();
		
		list.add("Enq");
		list.add("Tdr");
		list.add("Fin");
		list.add("Ins");
		list.add("Acc");
		list.add("Bkg");
		list.add("Hvt");
		list.add("Exg");
		list.add("Ret");
		list.add("Exw");
		
		return new ResponseEntity<>(list, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "v2/get_target_params_for_emp")
	public ResponseEntity<List<TargetAchivement>> getTargetAchivementParamsForEmp(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<TargetAchivement> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getTargetAchivementParamsForSingleEmp(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	/////Immediate Hierarchy
	@CrossOrigin
	@PostMapping(value = "v2/get_target_params_immediate_hierarchy")
	public ResponseEntity<List<TargetAchivement>> getTargetAchivementParamsForEmpImmediateHirarchy(@RequestBody DashBoardReqImmediateHierarchyV2 req)
			throws DynamicFormsServiceException {
		List<TargetAchivement> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getTargetAchivementParamsForSingleEmpImmediateHierarchy(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "v2/get_emp_target_ranking/org/{orgId}")
	public ResponseEntity<List<TargetRankingRes>> getEmployeeTargetRankingsByOrg(@PathVariable(name="orgId") Integer orgId,@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<TargetRankingRes> response = null;
		if (Optional.of(orgId).isPresent() && Optional.of(req).isPresent()) {
			response = dashBoardService.getEmployeeTargetRankingByOrg(orgId,req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "v2/get_emp_target_ranking/org/{orgId}/branch/{branchId}")
	public ResponseEntity<List<TargetRankingRes>> getEmployeeTargetRankingsByOrgAndBranch(@PathVariable(name="orgId") Integer orgId,@PathVariable(name="branchId") Integer branchId,@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<TargetRankingRes> response = null;
		if (Optional.of(orgId).isPresent() && Optional.of(branchId).isPresent() && Optional.of(req).isPresent()) {
			response = dashBoardService.getEmployeeTargetRankingByOrgAndBranch(orgId, branchId,req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
//Branch Ranking	
	@CrossOrigin
	@PostMapping(value = "v2/get_emp_target_Branch_ranking/org/{orgId}/branchName/{branchName}")
	public ResponseEntity<List<TargetRankingRes>> getEmployeeTargetBranchRankingsByOrgAndBranch(@PathVariable(name="orgId") Integer orgId,@PathVariable(name="branchName") String branchName,@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<TargetRankingRes> response = null;
		if (Optional.of(orgId).isPresent() && Optional.of(branchName).isPresent() && Optional.of(req).isPresent()) {
			response = dashBoardService.getEmployeeTargetBranchRankingByOrgAndBranch(orgId, branchName,req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
//Dealer Ranking
	@CrossOrigin
	@PostMapping(value = "v2/get_emp_target_Dealer_ranking/org/{orgId}/branchName/{branchName}")
	public ResponseEntity<List<TargetRankingRes>> getEmployeeTargetDealerRankingsByOrg(@PathVariable(name="orgId") Integer orgId,@PathVariable(name="branchName") String branchName,@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<TargetRankingRes> response = null;
		if (Optional.of(orgId).isPresent() && Optional.of(req).isPresent()) {
			response = dashBoardService.getEmployeeTargetDealerRankingByOrg(orgId,branchName,req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "v2/get_vehicle_model_data")
	public ResponseEntity<List<VehicleModelRes>> getVehicleModelData(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<VehicleModelRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getVehicleModelData(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
	
	
	@CrossOrigin
	@PostMapping(value = "v2/get_vehicle_model_data_branch")
	public ResponseEntity<List<VehicleModelRes>> getVehicleModelDataByBranch(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<VehicleModelRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getVehicleModelData(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
	@CrossOrigin
	@PostMapping(value = "v2/get_leadsource_data")
	public ResponseEntity<List<LeadSourceRes>> getLeadSourceData(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<LeadSourceRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getLeadSourceData(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin
	@PostMapping(value = "v2/get_leadsource_data_branch")
	public ResponseEntity<List<LeadSourceRes>> getLeadSourceDataByBranch(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<LeadSourceRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getLeadSourceData(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "v2/get_events_data")
	public ResponseEntity<List<EventDataRes>> getEventsData(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<EventDataRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getEventSourceData(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin
	@PostMapping(value = "v2/get_events_data_branch")
	public ResponseEntity<List<EventDataRes>> getEventsDataByBranch(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<EventDataRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getEventSourceData(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	

	@CrossOrigin
	@PostMapping(value = "v2/get_lostdrop_data")
	public ResponseEntity<Map<String,Object>> getLostDropData(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		Map<String,Object> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getLostDropData(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	

	@CrossOrigin
	@PostMapping(value = "v2/get_todays_data")
	public ResponseEntity<Map<String,Object>> getTodaysData(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		Map<String, Object> response = null;
		if (req.getLoggedInEmpId() != null && req.getPageNo() >= 0 && req.getSize() > 0) {
			response = dashBoardService.getTodaysPendingUpcomingData(req);
		} else {
			throw new DynamicFormsServiceException("LoggedInEmpId,PageNo and Size are mandatory params",
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "v2/get_sales_data")
	public ResponseEntity<?> getSalesData(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		SalesDataRes response = null;
		if (req.getLoggedInEmpId() != null) {
			response = dashBoardService.getSalesData(req);
		} else {
			throw new DynamicFormsServiceException("LoggedInEmpId is mandatory params",
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "v2/get_sales_comparsion_data")
	public ResponseEntity<?> getSalesComparsionData(@RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		List<Map<String, Long>> response = null;
		if (req.getLoggedInEmpId() != null) {
			response = dashBoardService.getSalesComparsionData(req);
		} else {
			throw new DynamicFormsServiceException("LoggedInEmpId is mandatory params",
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "v2/get_todays_datav2")
	public ResponseEntity<Map<String,Object>> getTodaysDataV2(@RequestBody MyTaskReq req)
			throws DynamicFormsServiceException {
		Map<String, Object> response = null;
		if (req.getLoggedInEmpId() != null) {
			response = dashBoardService.getTodaysPendingUpcomingDataV2(req);
		} else {
			throw new DynamicFormsServiceException("LoggedInEmpId,PageNo and Size are mandatory params",
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}




	@CrossOrigin
	@PostMapping(value = "v2/get_todays_datav2/filter")
	public ResponseEntity<Map<String,Object>> getTodaysDataV2Filter(@RequestBody MyTaskReq req)
			throws DynamicFormsServiceException {
		Map<String, Object> response = null;
		if (req.getLoggedInEmpId() != null) {
			response = dashBoardService.getTodaysPendingUpcomingDataV2Filter(req);
		} else {
			throw new DynamicFormsServiceException("LoggedInEmpId,PageNo and Size are mandatory params",
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "v2/get_todays_datav2/filter/counts")
	public ResponseEntity<List<TodaysTaskRes2>> getTodaysDataV2FilterCount(@RequestBody MyTaskReq req)
			throws DynamicFormsServiceException {
		List<TodaysTaskRes2> response = null;
		if (req.getLoggedInEmpId() != null) {
			response = dashBoardService.getFilteredTasksCount(req);
		} else {
			throw new DynamicFormsServiceException("LoggedInEmpId,PageNo and Size are mandatory params",
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "v2/get_todays_datav3")
	public ResponseEntity<Map<String,Object>> getTodaysDataV3(@RequestBody MyTaskReq req)
			throws DynamicFormsServiceException {
		Map<String, Object> response = null;
		if (req.getLoggedInEmpId() != null) {
			response = dashBoardService.getTodaysPendingUpcomingDataV3(req);
		} else {
			throw new DynamicFormsServiceException("LoggedInEmpId,PageNo and Size are mandatory params",
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	 @GetMapping("/get_target_param/{orgId}/{empId}")
	    public List<DmsTargetParamSchedular> getById(@PathVariable("orgId") String orgId,@PathVariable("empId") String empId) {

	        return targetparamschedularRepo.getbyOrgIdAndEmpId(orgId,empId);
	    }
	 
	    @CrossOrigin
		@PostMapping(value = "v2/get_target_params_model_source")
		public ResponseEntity<List<TargetAchivementModelandSource>> getTargetAchivementParamsModelSource(@RequestBody DashBoardReqV2 req)
				throws DynamicFormsServiceException {
			List<TargetAchivementModelandSource> response = null;
			if (Optional.of(req).isPresent()) {
				response = dashBoardService.getTargetAchivementParamsModelAndSource(req);
			} else {
				throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	    
	    @CrossOrigin
		@PostMapping(value = "v2/get_target_params_model_source_events")
		public ResponseEntity<List<TargetAchivementModelandSource>> getTargetAchivementParamsModelSourceEvents(@RequestBody DashBoardReqV2 req)
				throws DynamicFormsServiceException {
			List<TargetAchivementModelandSource> response = null;
			if (Optional.of(req).isPresent()) {
				response = dashBoardService.getTargetAchivementParamsModelAndSource(req);
			} else {
				throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	    
	    @CrossOrigin
		@PostMapping(value = "v2/get_target_params_for_emp_model_source")
		public ResponseEntity<List<TargetAchivementModelandSource>> getTargetAchivementParamsForEmpModelAndSource3(@RequestBody DashBoardReqV2 req)
				throws DynamicFormsServiceException {
			List<TargetAchivementModelandSource> response = null;
			if (Optional.of(req).isPresent()) {
				response = dashBoardService.getTargetAchivementParamsForSingleEmpModelAndSource3(req);
			} else {
				throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<>(response, HttpStatus.OK);
		}


	@CrossOrigin
	@PostMapping(value = "leads/redirection")
	public ResponseEntity<?> getLeadsRedirection(HttpServletRequest request, @RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		ResponseEntity<?> response = null;

		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getDropLeadsRedirection(req, request.getHeader("Authorization"));
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response.getBody(), HttpStatus.OK);

	}

	@CrossOrigin
	@PostMapping(value = "ebrleads/redirection")
	public ResponseEntity<?> getEBRRedirection(HttpServletRequest request, @RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		ResponseEntity<?> response = null;

		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getDropLeadsRedirection(req, request.getHeader("Authorization"));
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response.getBody(), HttpStatus.OK);

	}

	@CrossOrigin
	@PostMapping(value = "tvtasks/redirection")
	public ResponseEntity<?> getTVTasksRedirection(HttpServletRequest request, @RequestBody DashBoardReqV2 req)
			throws DynamicFormsServiceException {
		ResponseEntity<?> response = null;

		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getDropLeadsRedirection(req, request.getHeader("Authorization"));
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);

	}
}
