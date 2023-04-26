package com.automate.df.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.automate.df.exception.DynamicFormsServiceException;
import com.automate.df.model.df.dashboard.ReceptionistDashBoardReq;
import com.automate.df.model.df.dashboard.ReceptionistLeadRes;
import com.automate.df.model.df.dashboard.SourceRes;
import com.automate.df.model.df.dashboard.VehicleModelRes;
import com.automate.df.service.ReceptionistService;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Api(value = "/dashboard", tags = "dashboard", description = "dashboard")
@RequestMapping(value="/dashboard")
public class ReceptionistController {
	
	@Autowired
	Environment env;

	@Autowired
	ReceptionistService dashBoardService;
	
	
	@CrossOrigin
	@PostMapping(value = "/receptionist")
	public ResponseEntity<Map> getReceptionistData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
			Map response = dashBoardService.getReceptionistData(req, "Reception");
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/receptionistLiveLeads")
	public ResponseEntity<Map> getReceptionistLiveLeadData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
			Map response = dashBoardService.getReceptionistLiveLeadData(req);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/managerLiveLeads")
	public ResponseEntity<Map> getManagerLiveLeadData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
			Map response = dashBoardService.getManagerLiveLeadData(req);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/liveLeads/source")
	public ResponseEntity<List<SourceRes>> getLiveLeadsSourceData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<SourceRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getLiveLeadsSourceData(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/liveLeads/model")
	public ResponseEntity<List<VehicleModelRes>> getLiveLeadModelData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<VehicleModelRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getLiveLeadModelData(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/managerLiveLeads/source")
	public ResponseEntity<List<SourceRes>> getManagerSourceLiveLeadData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<SourceRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getManagerSourceLiveLeadData(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/managerLiveLeads/model")
	public ResponseEntity<List<VehicleModelRes>> getManagerModelLiveLeadData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<VehicleModelRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getManagerModelLiveLeadData(req);
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/receptionistTeam")
	public ResponseEntity<Map> getTeamCount(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
			Map response = dashBoardService.getTeamCount(req, "Reception");
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin
	@PostMapping(value = "/receptionistManagerTeam")
	public ResponseEntity<Map> getTeamCountReceptionist(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		Map response = dashBoardService.getTeamCountReceptionist(req, "crmuser");

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin
	@PostMapping(value = "/SalesManagerDigitalTeam")
	public ResponseEntity<Map> getSalesManagerDigitalTeam(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		Map response = dashBoardService.getSalesManagerDigitalTeam(req, "Reception");

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/receptionistManager")
	public ResponseEntity<Map> getReceptionistManagerData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
			Map response = dashBoardService.getReceptionistManagerData(req, "Reception");
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/teleCallerList")
	public ResponseEntity<Map> getTeleCallerData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
			Map response = dashBoardService.getReceptionistData(req, "Tele Caller");
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin
	@PostMapping(value = "/receptionist/model")
	public ResponseEntity<List<VehicleModelRes>> getReceptionistModelData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<VehicleModelRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getReceptionistModelData(req, "Reception");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/receptionistManager/model")
	public ResponseEntity<List<VehicleModelRes>> getReceptionistManagerModelData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<VehicleModelRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getReceptionistManagerModelData(req, "Reception");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/teleCaller/model")
	public ResponseEntity<List<VehicleModelRes>> getTeleCallerModelData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<VehicleModelRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getReceptionistModelData(req, "Tele Caller");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/receptionist/source")
	public ResponseEntity<List<SourceRes>> getReceptionistSourceData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<SourceRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getReceptionistSourceData(req, "Reception");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
	@CrossOrigin
	@PostMapping(value = "/receptionistManager/source")
	public ResponseEntity<List<SourceRes>> getReceptionistManagerSourceData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<SourceRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getReceptionistManagerSourceData(req, "Reception");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/teleCaller/source")
	public ResponseEntity<List<SourceRes>> getTelecallerSourceData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<SourceRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getReceptionistSourceData(req, "Tele Caller");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/receptionist/empLead")
	public ResponseEntity<List<ReceptionistLeadRes>> getReceptionistLeadData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<ReceptionistLeadRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getReceptionistLeadData(req, "Reception");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/teleCaller/empLead")
	public ResponseEntity<List<ReceptionistLeadRes>> getTelecallerLeadData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<ReceptionistLeadRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getReceptionistLeadData(req, "Tele Caller");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/receptionist/droppedLead")
	public ResponseEntity<List<ReceptionistLeadRes>> getReceptionistDropedLeadData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<ReceptionistLeadRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getReceptionistDroppedLeadData(req, "Reception");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping(value = "/teleCaller/droppedLead")
	public ResponseEntity<List<ReceptionistLeadRes>> getTeleCallerDroppedLeadData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<ReceptionistLeadRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getReceptionistDroppedLeadData(req, "Tele Caller");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}



	@CrossOrigin
	@PostMapping(value = "/xrole/source")
	public ResponseEntity<List<SourceRes>> getXRoleSourceData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<SourceRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getXRoleSourceData(req, "Reception");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}


	@CrossOrigin
	@PostMapping(value = "/xrole/model")
	public ResponseEntity<List<VehicleModelRes>> getXRoleModelData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		List<VehicleModelRes> response = null;
		if (Optional.of(req).isPresent()) {
			response = dashBoardService.getXRoleModelData(req, "Reception");
		} else {
			throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin
	@PostMapping(value = "/receptionist/filter")
	public ResponseEntity<Map> getReceptionistFilterData(@RequestBody ReceptionistDashBoardReq req)
			throws DynamicFormsServiceException {
		Map response = dashBoardService.getReceptionistFilterData(req, "Reception");

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	}
