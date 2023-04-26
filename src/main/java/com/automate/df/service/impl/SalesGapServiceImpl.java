package com.automate.df.service.impl;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import com.automate.df.entity.salesgap.*;
import com.automate.df.model.df.dashboard.DashBoardReq;
import com.automate.df.model.salesgap.*;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.automate.df.constants.DynamicFormConstants;
import com.automate.df.constants.GsAppConstants;
import com.automate.df.dao.oh.DmsBranchDao;
import com.automate.df.dao.oh.DmsDesignationRepo;
import com.automate.df.dao.salesgap.DmsEmployeeRepo;
import com.automate.df.dao.salesgap.DmsRoleRepo;
import com.automate.df.dao.salesgap.TargetSettingRepo;
import com.automate.df.dao.salesgap.TargetUserRepo;
import com.automate.df.entity.oh.DmsBranch;
import com.automate.df.entity.oh.DmsDesignation;
import com.automate.df.exception.DynamicFormsServiceException;
import com.automate.df.service.SalesGapService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 
 * @author srujan
 *
 */
@Slf4j
@Service
public class SalesGapServiceImpl implements SalesGapService {

	@Autowired
	Environment env;
	
	@Autowired
	DmsRoleRepo dmsRoleRepo;

	@Autowired
	TargetSettingRepo targetSettingRepo;
	

	@Autowired
	TargetUserRepo targetUserRepo;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	Gson gson;

	@Autowired
	DashBoardServiceImplV2 dashBoardServiceImplV2;

	@Autowired
	private EntityManager entityManager;

	@Value("${targetsetting.params}")
	List<String> paramList;

	@Autowired
	DmsEmployeeRepo dmsEmployeeRepo;

	@Value("${dse.roles}")
	List<String> dseDesignationList;

	@Value("${tl.roles}")
	List<String> tlDesignationList;

	@Value("${manager.roles}")
	List<String> mgrDesignationList;

	@Value("${branchmgr.roles}")
	List<String> branchMgrDesignationList;

	@Value("${gm.roles}")
	List<String> GMDesignationList;

	public static final String RETAIL_TARGET = "retailTarget";
	public static final String INVOICE = "invoice";

	public static final String PERCENTAGE = "percentage";
	public static final String NUMBER = "number";

	final String getReportingEmp = "SELECT emp_id,emp_name FROM dms_employee where reporting_to=<ID>";
	final String getEmpUnderTLQuery = "SELECT emp_id FROM dms_employee where reporting_to=<ID>";

	final String roleMapQuery = " SELECT "
			+ " rolemap.organization_id, rolemap.branch_id, rolemap.emp_id, role.role_name, role.role_id, role.precedence "
			+ " FROM dms_role role " + " INNER JOIN dms_employee_role_mapping rolemap ON rolemap.role_id=role.role_id "
			+ " AND rolemap.emp_id=<EMP_ID> " + " ORDER BY role.precedence ";
	final String dmsEmpByidQuery = "SELECT * FROM dms_employee where emp_id = <EMP_ID>";
	final String roleMapQueryimmediate = "SELECT rolemap.organization_id, rolemap.branch_id, rolemap.emp_id, role.role_name, role.role_id, role.precedence FROM dms_role role INNER JOIN dms_employee_role_mapping rolemap ON rolemap.role_id=role.role_id AND rolemap.emp_id in EMP_ID ORDER BY role.precedence ";
	final String dmsEmpByidQueryimmediate = "SELECT * FROM dms_employee where emp_id in <EMP_ID>";
	final String dmsEmpimmediateByidQuery = "SELECT * FROM dms_employee where emp_id in <EMP_ID>";
	final String getSalForEmp = "select salary from dms_emp_sal_mapping where emp_id=<ID>";

	@Override
	public List<TargetSettingRes> getTargetSettingData(int pageNo, int size,int orgId) {
		log.debug("Inside getTargetSettingData()");
		List<TargetSettingRes> list = new ArrayList<>();
		try {
			Pageable pageable = PageRequest.of(pageNo, size);
			List<TargetEntity> dbList = targetSettingRepo.getTargetmappingDataOrg(orgId,pageable);
			// list = dbList.stream().map(x -> modelMapper.map(x,
			// TargetSettingRes.class)).collect(Collectors.toList());
			dbList = dbList.stream().filter(x -> x.getActive().equalsIgnoreCase(GsAppConstants.ACTIVE))
					.collect(Collectors.toList());
			for (TargetEntity te : dbList) {
				TargetSettingRes res = modelMapper.map(te, TargetSettingRes.class);
				String json = te.getTargets();
				if (null != json && !json.isEmpty()) {
					JsonParser parser = new JsonParser();
					JsonArray arr = parser.parse(json).getAsJsonArray();
					for (JsonElement je : arr) {
						JsonObject obj = je.getAsJsonObject();
						String paramName = obj.get("parameter").getAsString();
						res = convertJsonToStrV4(res, paramName, obj);
					}
				}
				// branch,location,department,designation,experience,salaryrange,branchmanager,manager,teamlead,employee
				res.setEmpName(te.getEmpName());

				res.setBranchName(getBranchName(te.getBranch()));
				// res.setLocationName(getLocationName(te.getLocation()));
				res.setDepartmentName(getDeptName(te.getDepartment()));
				res.setDesignationName(getDesignationName(te.getDesignation()));
				res.setExperience(te.getExperience());
				res.setSalrayRange(te.getSalrayRange());
				list.add(res);
			}

		} catch (Exception e) {
			log.error("getTargetSettingData() ", e);
		}
		return list;
	}

	private TargetSettingRes convertJsonToStr(TargetSettingRes res, String paramName, JsonObject obj) {
		if (null != paramName && paramName.equalsIgnoreCase("retailTarget")) {
			if (obj.has("target"))
				res.setRetailTarget(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("enquiry")) {
			if (obj.has("target"))
				res.setEnquiry(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("testDrive")) {
			if (obj.has("target"))
				res.setTestDrive(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("homeVisit")) {
			if (obj.has("target"))
				res.setHomeVisit(obj.get("target").getAsString());
		}

		if (null != paramName && paramName.equalsIgnoreCase("videoConference")) {
			if (obj.has("target"))
				res.setVideoConference(obj.get("target").getAsString());
		}

		if (null != paramName && paramName.equalsIgnoreCase("booking")) {
			if (obj.has("target"))
				res.setBooking(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("exchange")) {
			if (obj.has("target"))
				res.setExchange(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("finance")) {
			if (obj.has("target"))
				res.setFinance(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("insurance")) {
			if (obj.has("target"))
				res.setInsurance(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("exWarranty")) {
			if (obj.has("target"))
				res.setExWarranty(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("accessories")) {
			if (obj.has("target"))
				res.setAccessories(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("events")) {
			if (obj.has("target"))
				res.setEvents(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("other")) {
			if (obj.has("target"))
				res.setOther(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("enquiry")) {
			if (obj.has("target"))
				res.setEnquiry(obj.get("target").getAsString());
		}

		if (null != paramName && paramName.equalsIgnoreCase(INVOICE)) {
			if (obj.has("target"))
				res.setInvoice(obj.get("target").getAsString());
		}

		if (null != paramName && paramName.equalsIgnoreCase("Ex-Warranty")) {
			if (obj.has("target"))
				res.setExWarranty(obj.get("target").getAsString());
		}

		return res;
	}

	private TargetSettingRes convertJsonToStrV3(TargetSettingRes res, String paramName, JsonObject obj) {
		if (null != paramName && paramName.equalsIgnoreCase("retailTarget")) {
			if (obj.has("target"))
				res.setRetailTarget("0");
		}
		if (null != paramName && paramName.equalsIgnoreCase("enquiry")) {
			if (obj.has("target"))
				res.setEnquiry("0");
		}
		if (null != paramName && paramName.equalsIgnoreCase("testDrive")) {
			if (obj.has("target"))
				res.setTestDrive("0");
		}
		if (null != paramName && paramName.equalsIgnoreCase("homeVisit")) {
			if (obj.has("target"))
				res.setHomeVisit("0");
		}

		if (null != paramName && paramName.equalsIgnoreCase("videoConference")) {
			if (obj.has("target"))
				res.setVideoConference("0");
		}

		if (null != paramName && paramName.equalsIgnoreCase("booking")) {
			if (obj.has("target"))
				res.setBooking("0");
		}
		if (null != paramName && paramName.equalsIgnoreCase("exchange")) {
			if (obj.has("target"))
				res.setExchange("0");
		}
		if (null != paramName && paramName.equalsIgnoreCase("finance")) {
			if (obj.has("target"))
				res.setFinance("0");
		}
		if (null != paramName && paramName.equalsIgnoreCase("insurance")) {
			if (obj.has("target"))
				res.setInsurance("0");
		}
		if (null != paramName && paramName.equalsIgnoreCase("exWarranty")) {
			if (obj.has("target"))
				res.setExWarranty("0");
		}
		if (null != paramName && paramName.equalsIgnoreCase("accessories")) {
			if (obj.has("target"))
				res.setAccessories("0");
		}
		if (null != paramName && paramName.equalsIgnoreCase("events")) {
			if (obj.has("target"))
				res.setEvents("0");
		}
		if (null != paramName && paramName.equalsIgnoreCase("other")) {
			if (obj.has("target"))
				res.setOther("0");
		}
		if (null != paramName && paramName.equalsIgnoreCase("enquiry")) {
			if (obj.has("target"))
				res.setEnquiry("0");
		}

		if (null != paramName && paramName.equalsIgnoreCase(INVOICE)) {
			if (obj.has("target"))
				res.setInvoice("0");
		}

		return res;
	}

	private TargetSettingRes convertJsonToStrV2(TargetSettingRes res, String paramName, JsonObject obj,
			String retailTarget) {
		if (null != paramName && paramName.equalsIgnoreCase("retailTarget")) {
			res.setRetailTarget(retailTarget);
		}
		if (null != paramName && paramName.equalsIgnoreCase("enquiry")) {
			if (obj.has("target"))
				res.setEnquiry(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("testDrive")) {
			if (obj.has("target"))
				res.setTestDrive(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("homeVisit")) {
			if (obj.has("target"))
				res.setHomeVisit(obj.get("target").getAsString());
		}

		if (null != paramName && paramName.equalsIgnoreCase("videoConference")) {
			if (obj.has("target"))
				res.setVideoConference(obj.get("target").getAsString());
		}

		if (null != paramName && paramName.equalsIgnoreCase("booking")) {
			if (obj.has("target"))
				res.setBooking(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("exchange")) {
			if (obj.has("target"))
				res.setExchange(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("finance")) {
			if (obj.has("target"))
				res.setFinance(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("insurance")) {
			if (obj.has("target"))
				res.setInsurance(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("exWarranty")) {
			if (obj.has("target"))
				res.setExWarranty(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("accessories")) {
			if (obj.has("target"))
				res.setAccessories(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("events")) {
			if (obj.has("target"))
				res.setEvents(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("other")) {
			if (obj.has("target"))
				res.setOther(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("enquiry")) {
			if (obj.has("target"))
				res.setEnquiry(obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase(INVOICE)) {
			if (obj.has("target"))
				res.setInvoice(obj.get("target").getAsString());
		}
		return res;
	}

	@Override
	public TargetSettingRes saveTargetSettingData(TargetSettingReq request) throws DynamicFormsServiceException {
		log.debug("Inside saveTargetSettingData()");
		TargetEntity dbRes = null;
		TargetSettingRes ts = null;
		try {
			String minSal = "";
			String maxSal = "";
			TargetEntity te = new TargetEntity();
			te.setBranch(request.getBranch());
			// te.setLocation(request.getLocation());
			te.setDepartment(request.getDepartment());
			te.setDesignation(request.getDesignation());
			String exp = request.getExperience();
			String salRange = request.getSalrayRange();
			te.setExperience(request.getExperience());
			if (null != salRange) {
				te.setSalrayRange(salRange);
				if (salRange.contains("-")) {
					String tmp[] = salRange.split("-");
					minSal = tmp[0];
					minSal = StringUtils.replaceIgnoreCase(minSal, "k", "").trim();
					maxSal = tmp[1];
					maxSal = StringUtils.replaceIgnoreCase(maxSal, "k", "").trim();

				} else {
					minSal = salRange;
					minSal = StringUtils.replaceIgnoreCase(minSal, "k", "").trim();
				}
			}
			te.setOrgId(request.getOrgId());

			te.setMaxSalary(maxSal);
			te.setMinSalary(minSal);
			List<Target> list = request.getTargets();
			// log.debug("Before Targets "+list);
			// list = updatedTargetValues(list);
			// log.debug("After updating Targets "+list);
			if (null != list) {
				te.setTargets(gson.toJson(list));
			}
			if (!validateTargetAdminData(te)) {
				log.debug("TARGET ADMIN DATA DOES NOT EXISTS IN DB");
				te.setActive(GsAppConstants.ACTIVE);
				dbRes = targetSettingRepo.save(te);
				String targets = dbRes.getTargets();

				ts = modelMapper.map(dbRes, TargetSettingRes.class);
				ts = convertTargetStrToObj(targets, ts);
				ts.setBranchName(getBranchName(te.getBranch()));
				// ts.setLocationName(getLocationName(te.getLocation()));
				ts.setDepartmentName(getDeptName(te.getDepartment()));
				ts.setDesignationName(getDesignationName(te.getDesignation()));
				ts.setExperience(te.getExperience());
				ts.setSalrayRange(te.getSalrayRange());

			} else {
				throw new DynamicFormsServiceException("TARGET ADMIN DATA  EXISTS IN DB",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} catch (DynamicFormsServiceException e) {
			log.error("saveTargetSettingData() ", e);
			e.printStackTrace();
			throw new DynamicFormsServiceException("TARGET ADMIN DATA  EXISTS IN DB", HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (Exception e) {
			log.error("saveTargetSettingData() ", e);
			e.printStackTrace();
			throw new DynamicFormsServiceException(env.getProperty("INTERNAL_SERVER_ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return ts;
	}

	private boolean validateTargetAdminData(TargetEntity te) {
		List<TargetEntity> dbList = new ArrayList<>();

		String salRange = te.getSalrayRange();
		String exp = te.getExperience();

		if (null != salRange && null != exp) {
			dbList = targetSettingRepo.getTargetmappingData(te.getOrgId(), te.getBranch(), te.getDepartment(),
					te.getExperience(), te.getSalrayRange(), te.getDesignation());
		}
		if (null != salRange && null == exp) {
			dbList = targetSettingRepo.getTargetmappingDataWithOutExp(te.getOrgId(), te.getBranch(), te.getDepartment(),
					te.getSalrayRange(), te.getDesignation());
		}
		if (null == salRange && null != exp) {
			dbList = targetSettingRepo.getTargetmappingDataWithOutSal(te.getOrgId(), te.getBranch(), te.getDepartment(),
					te.getExperience(), te.getDesignation());
		}
		if (null == salRange && null == exp) {
			dbList = targetSettingRepo.getTargetmappingDataWithOutExpSal(te.getOrgId(), te.getBranch(),
					te.getDepartment(), te.getDesignation());
		}

		if (null != dbList && !dbList.isEmpty()) {
			return true;
		}
		return false;
	}

	private List<Target> updatedTargetValues(List<Target> list) {
		Integer retailTarget = null;
		String unitType = null;
		for (Target target : list) {

			if (null != target && target.getParameter().equalsIgnoreCase(RETAIL_TARGET)) {
				unitType = target.getUnit();
				retailTarget = Integer.valueOf(target.getTarget());

			}
		}
		log.debug("retailTarget " + retailTarget + " unitType " + unitType);
		for (Target target : list) {
			if (null != target && !target.getParameter().equalsIgnoreCase(RETAIL_TARGET)
					&& unitType.equalsIgnoreCase(PERCENTAGE)) {
				Integer paramTarget = 0;
				if (null != target.getTarget()) {
					paramTarget = Integer.valueOf(target.getTarget());
				}

				log.debug("paramTarget::" + paramTarget);
				if (paramTarget != 0) {
					Integer updatedTarget = (retailTarget * paramTarget * 100) / 100;
					target.setTarget(String.valueOf(updatedTarget));
				}
			}
		}
		return list;
	}

	/*
	 * 
	 * @Override public String saveTargetMappingData(TargetMappingReq request) {
	 * log.debug("Inside saveTargetMappingData()"); String res = null; try {
	 * Optional<TargetEntity> opt = targetSettingRepo.findById(request.getId());
	 * if(opt.isPresent()) { TargetEntity dbRes = opt.get(); dbRes =
	 * updateTargetMappingData(request,dbRes); targetSettingRepo.save(dbRes); res =
	 * objectMapper.writeValueAsString(dbRes); }else { res = "NO Record found"; } }
	 * catch (Exception e) { log.error("saveTargetSettingData() ", e);
	 * 
	 * } return res; }
	 * 
	 */

	@Override
	public TargetMappingReq getTargetMappingData(Integer id) {
		log.debug("Inside searchTargetMappingData()");
		TargetMappingReq res = null;
		try {
			Optional<TargetEntity> opt = targetSettingRepo.findById(id);
			if (opt.isPresent()) {
				res = modelMapper.map(opt.get(), TargetMappingReq.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("searchTargetMappingData() ", e);

		}
		return res;
	}

	@Override
	public String updateTargetSettingData(TargetSettingRes request) {
		log.debug("Inside updateTargetSettingData()");
		String res = null;
		try {

			Optional<TargetEntity> opt = targetSettingRepo.findById(request.getId());
			if (opt.isPresent()) {
				TargetEntity dbRes = opt.get();
				dbRes = updateTargetMappingData(request, dbRes);
				targetSettingRepo.save(dbRes);
				res = objectMapper.writeValueAsString(dbRes);
			} else {
				res = "NO Record found";
			}
		} catch (Exception e) {
			log.error("saveTargetSettingData() ", e);

		}
		return res;
	}

	private TargetEntity updateTargetMappingData(TargetSettingRes req, TargetEntity dbRes) {

		try {
			dbRes.setBranch(req.getBranch());
			// dbRes.setLocation(req.getLocation());
			dbRes.setEmployeeId(req.getEmployeeId());
			dbRes.setEmpName(getEmpIdByName(req.getEmployeeId()));
			dbRes.setManagerId(req.getManagerId());
			dbRes.setTeamLeadId(req.getTeamLeadId());

			TargetParamReq[] params = objectMapper.readValue(dbRes.getTargets(), TargetParamReq[].class);
			String retailUnitType = null;
			for (TargetParamReq param : params) {
				if (null != param && param.getParameter().equalsIgnoreCase(RETAIL_TARGET)) {
					retailUnitType = param.getUnit();
				}
			}

			List<TargetParamReq> list = new ArrayList<>();
			Map<String, String> unitsMap = getUnitsFromDbIfExists(dbRes.getTargets());
			log.debug("paramList::" + paramList);

			/*
			 * [TargetParamReq(parameter=retailTarget, target=null, unit=null),
			 * TargetParamReq(parameter=enquiry, target=null, unit=percentage),
			 * TargetParamReq(parameter=testDrive, target=null, unit=percentage),
			 * TargetParamReq(parameter=homeVisit, target=null, unit=percentage),
			 * TargetParamReq(parameter=booking, target=null, unit=percentage),
			 * TargetParamReq(parameter=exchange, target=null, unit=percentage),
			 * TargetParamReq(parameter=finance, target=null, unit=percentage),
			 * TargetParamReq(parameter=insurance, target=null, unit=percentage),
			 * TargetParamReq(parameter=exWarranty, target=null, unit=percentage),
			 * TargetParamReq(parameter=events, target=null, unit=number),
			 * TargetParamReq(parameter=other, target=null, unit=null),
			 * TargetParamReq(parameter=accessories, target=null, unit=number)]
			 */

			list.add(new TargetParamReq("retailTarget", req.getRetailTarget(), unitsMap.get("retailTarget")));
			list.add(new TargetParamReq("enquiry", req.getEnquiry(), unitsMap.get("enquiry")));
			list.add(new TargetParamReq("testDrive", req.getTestDrive(), unitsMap.get("testDrive")));
			list.add(new TargetParamReq("homeVisit", req.getHomeVisit(), unitsMap.get("homeVisit")));
			list.add(new TargetParamReq("booking", req.getBooking(), unitsMap.get("booking")));
			list.add(new TargetParamReq("homeVisit", req.getHomeVisit(), unitsMap.get("homeVisit")));
			list.add(new TargetParamReq("booking", req.getBooking(), unitsMap.get("booking")));
			list.add(new TargetParamReq("exchange", req.getExchange(), unitsMap.get("exchange")));
			list.add(new TargetParamReq("finance", req.getFinance(), unitsMap.get("finance")));
			list.add(new TargetParamReq("insurance", req.getInsurance(), unitsMap.get("insurance")));
			list.add(new TargetParamReq("exWarranty", req.getExWarranty(), unitsMap.get("exWarranty")));
			list.add(new TargetParamReq("events", req.getEvents(), unitsMap.get("events")));
			list.add(new TargetParamReq("other", req.getOther(), unitsMap.get("other")));
			list.add(new TargetParamReq("accessories", req.getAccessories(), unitsMap.get("accessories")));

			/*
			 * for (String param : paramList) { String methodName = "get" +
			 * StringUtils.capitalize(param); Method getNameMethod =
			 * req.getClass().getMethod(methodName);
			 * log.debug("getNameMethod::"+getNameMethod+" method name "+methodName); String
			 * name = (String) getNameMethod.invoke(req); // explicit cast
			 * log.debug("method name "+name); list.add(new TargetParamReq(param, name,
			 * unitsMap.get(param)));
			 * 
			 * }
			 */

			log.debug("target param updated list " + list);
			dbRes.setTargets(new Gson().toJson(list));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dbRes;
	}

	private String getDropDownValueById(String id, String type) {
		log.info("getDropDownValueById ,id :" + id + " ,type " + type);
		List<TargetDropDown> list = getTargetDropdown(type);
		if (null != id && type != "experience") {
			Optional<TargetDropDown> opt = list.stream().filter(x -> x.getId() == id).findAny();
			if (opt.isPresent()) {
				return opt.get().getValue();
			}
		}
		return "";
	}

	@Override
	public List<TargetDropDown> getTargetDropdown(String type) {
		List<TargetDropDown> list = new ArrayList<>();
		/*
		 * if(type.equalsIgnoreCase("branch")) { TargetDropDown td1 = new
		 * TargetDropDown(1,"BharathHyundai"); TargetDropDown td2 = new
		 * TargetDropDown(2,"Renalut"); list.add(td1); list.add(td2); }
		 * if(type.equalsIgnoreCase("location")) { TargetDropDown td1 = new
		 * TargetDropDown(1,"Gachibowli"); TargetDropDown td2 = new
		 * TargetDropDown(2,"HitechCity"); list.add(td1); list.add(td2); }
		 * if(type.equalsIgnoreCase("department")) { TargetDropDown td1 = new
		 * TargetDropDown(1,"Sales"); TargetDropDown td2 = new
		 * TargetDropDown(2,"Finance"); list.add(td1); list.add(td2); }
		 * if(type.equalsIgnoreCase("designation")) { TargetDropDown td1 = new
		 * TargetDropDown(1,"Sales Executive"); TargetDropDown td2 = new
		 * TargetDropDown(2,"Team Lead"); list.add(td1); list.add(td2); }
		 * if(type.equalsIgnoreCase("experience")) { TargetDropDown td1 = new
		 * TargetDropDown(1,"5Y"); TargetDropDown td2 = new TargetDropDown(2,"3Y");
		 * list.add(td1); list.add(td2); } if(type.equalsIgnoreCase("salaryrange")) {
		 * TargetDropDown td1 = new TargetDropDown(1,"20K"); TargetDropDown td2 = new
		 * TargetDropDown(2,"30k"); list.add(td1); list.add(td2);
		 * }if(type.equalsIgnoreCase("branchmanager")) { TargetDropDown td1 = new
		 * TargetDropDown(1,"BranchManager1"); TargetDropDown td2 = new
		 * TargetDropDown(2,"BranchManager2"); list.add(td1); list.add(td2); }
		 * if(type.equalsIgnoreCase("manager")) { TargetDropDown td1 = new
		 * TargetDropDown(1,"Manager1"); TargetDropDown td2 = new
		 * TargetDropDown(2,"Manager2"); list.add(td1); list.add(td2); }
		 * if(type.equalsIgnoreCase("teamlead")) { TargetDropDown td1 = new
		 * TargetDropDown(1,"teamlead1"); TargetDropDown td2 = new
		 * TargetDropDown(2,"teamlead2"); list.add(td1); list.add(td2); }
		 * if(type.equalsIgnoreCase("employee")) { TargetDropDown td1 = new
		 * TargetDropDown(1,"employee1"); TargetDropDown td2 = new
		 * TargetDropDown(2,"employee2"); list.add(td1); list.add(td2); }
		 */
		return list;
	}

	@Override
	public Map<String, Object> getTargetDataWithRole(TargetRoleReq req) throws DynamicFormsServiceException {
		log.debug("calling getTargetDataWithRole ,given req " + req);
		List<TargetSettingRes> outputList = new ArrayList<>();
		TargetPlanningReq targetreq=new TargetPlanningReq();
		if(req.getStartDate()== null|| req.getEndDate()== null) {
			targetreq.setStartDate(getFirstDayOfQurter());
			targetreq.setEndDate(getLastDayOfQurter());
		}
		else {
		targetreq.setStartDate(req.getStartDate());
		targetreq.setEndDate(req.getEndDate());
		}
		Map<String, Object> map = new LinkedHashMap<>();
		int pageNo = req.getPageNo();
		int size = req.getSize();
		// List<TargetSettingRes> outputList = new ArrayList<>();
		try {
			int empId = req.getEmpId();

			List<Integer> empReportingIdList = new ArrayList<>();
			empReportingIdList.add(empId);
			empReportingIdList.addAll(dashBoardServiceImplV2.getReportingEmployes(empId));

			log.debug("empReportingIdList for emp " + empId);
			log.debug("" + empReportingIdList);

			

			for (Integer id : empReportingIdList) {

				String eId = String.valueOf(id);
				log.debug("Executing for EMP ID " + eId);

				String tmpQuery = roleMapQuery.replaceAll("<EMP_ID>", eId);
				List<Object[]> tlData = entityManager.createNativeQuery(tmpQuery).getResultList();
				List<TargetRoleRes> tlDataResList = new ArrayList<>();

				for (Object[] arr : tlData) {
					TargetRoleRes tlDataRole = new TargetRoleRes();
					tlDataRole.setOrgId(String.valueOf(arr[0]));
					tlDataRole.setBranchId(String.valueOf(arr[1]));
					tlDataRole.setEmpId(String.valueOf(arr[2]));
					tlDataRole.setRoleName(String.valueOf(arr[3]));
					tlDataRole.setRoleId(String.valueOf(arr[4]));
					tlDataResList.add(tlDataRole);

					String tEmpId = String.valueOf(arr[2]);
					Integer emp = Integer.parseInt(tEmpId);
					// tlDataResList.add(getEmpRoleDataV3(emp));
				}
				if (null != tlDataResList) {
					log.debug("Size of tlDataResList " + tlDataResList.size() + " tlDataResList " + tlDataResList);

					for (TargetRoleRes tr : tlDataResList) {
						log.debug("Inside tr loop " + tr);
						Optional<DmsEmployee> empOpt = dmsEmployeeRepo.findById(Integer.valueOf(tr.getEmpId()));
						if (empOpt.isPresent()) {
							DmsEmployee emp = empOpt.get();
							outputList.addAll(
									getTSDataForRoleV2(buildTargetRoleRes(tr, emp), String.valueOf(empId), "", "", "",targetreq));

						}
					}
				}
			}
			outputList = outputList.stream()
                    .collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(TargetSettingRes::getId))),
                                               ArrayList::new));
			
			System.out.println(outputList +"output$$$$$$$$$$$$$$$");
			
			
			//outputList = outputList.stream().distinct().collect(Collectors.toList());
			
			log.debug("outputList::"+outputList);
			int totalCnt = outputList.size();
			int fromIndex = size * (pageNo - 1);
			int toIndex = size * pageNo;

			if (toIndex > totalCnt) {
				toIndex = totalCnt;
			}
			if (fromIndex > toIndex) {
				fromIndex = toIndex;
			}
			String targetType = req.getTargetType();
			String startDateval=req.getStartDate();
			String enddateval=req.getEndDate();
			log.debug("targetType in get all api " + targetType);

			if (null != targetType && targetType.equalsIgnoreCase(DynamicFormConstants.TARGET_MONTHLY_TYPE)) {
				outputList = outputList.stream()
						.filter(x -> x.getTargetType().equalsIgnoreCase(DynamicFormConstants.TARGET_MONTHLY_TYPE))
						.collect(Collectors.toList());
			} else if (null != targetType && targetType.equalsIgnoreCase(DynamicFormConstants.TARGET_SPEICAL_TYPE)) {
				outputList = outputList.stream()
						.filter(x -> x.getTargetType().equalsIgnoreCase(DynamicFormConstants.TARGET_SPEICAL_TYPE))
						.collect(Collectors.toList());
			}

			log.debug("outputList ::" + outputList.size());

			if (outputList != null && !outputList.isEmpty() && outputList.size() > toIndex) {
				outputList = outputList.subList(fromIndex, toIndex);
			}
			List<TargetSettingRes> outputList1 = new ArrayList<>();
			for(int i =0; i<outputList.size();i++) {
			if(targetreq.getStartDate().equals(outputList.get(i).getStartDate()) && targetreq.getEndDate().equals(outputList.get(i).getEndDate())) {
				outputList1.add(outputList.get(i));
			}
			}
			outputList1.sort((o1,o2) -> o2.getEndDate().compareTo(o1.getEndDate()));
			for(int i =0 ; i<outputList1.size();i++) {
				outputList1.get(i).setIsAccess(getAccess(String.valueOf(req.getEmpId()),outputList1.get(i).getEmployeeId(),String.valueOf(outputList1.get(i).getId())));
				System.out.println("outputList1 &&&&&&&&"+outputList1.get(i).toString());	
			}
			map.put("totalCnt", outputList1.size());
			map.put("pageNo", pageNo);
			map.put("size", size);
			map.put("data", outputList1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public String getAccess(String loggedInEmpId, String employeeId, String recordId) {
		try {
			List<TargetEntityUser> targetEntityUserList = new ArrayList<>();

			Optional<TargetEntityUser> targetEntityUser = targetUserRepo.findByEmpIdWithRecordId(recordId, employeeId);
			targetEntityUserList.add(targetEntityUser.get());

			for (TargetEntityUser te : targetEntityUserList) {

				List<Integer> listOfParentUser = new ArrayList<>();
				int userId = Integer.parseInt(loggedInEmpId);
				int reportingId = dmsEmployeeRepo.getReportingPersonId(userId);

				if (userId != reportingId) {
					listOfParentUser.add(reportingId);

					while (userId != reportingId) {
						userId = reportingId;
						reportingId = dmsEmployeeRepo.getReportingPersonId(userId);
						listOfParentUser.add(reportingId);
					}
				}
				if (listOfParentUser.contains(te.getUpdatedById())) {
					return "false";
				}

			}
			return "true";
		} catch (Exception e) {
			e.printStackTrace();
			return "false";
		}

	}

	@Override
	public Map<String, Object> getTargetMappingDataSearchByEmpId(TargetPlanningReq req) throws DynamicFormsServiceException {
		log.debug("calling getTargetDataWithRole ,given req " + req);
		List<TargetSettingRes> outputList = new ArrayList<>();
		Map<String, Object> map = new LinkedHashMap<>();
		int pageNo = req.getPageNo();
		int size = req.getSize();
		try {
			int empId = req.getLoggedInEmpId();

			List<Integer> empReportingIdList = new ArrayList<>(req.getChildEmpId());

			for (Integer id : empReportingIdList) {

				String eId = String.valueOf(id);
				log.debug("Executing for EMP ID " + eId);

				String tmpQuery = roleMapQuery.replaceAll("<EMP_ID>", eId);
				List<Object[]> tlData = entityManager.createNativeQuery(tmpQuery).getResultList();
				List<TargetRoleRes> tlDataResList = new ArrayList<>();

				for (Object[] arr : tlData) {
					TargetRoleRes tlDataRole = new TargetRoleRes();
					tlDataRole.setOrgId(String.valueOf(arr[0]));
					tlDataRole.setBranchId(String.valueOf(arr[1]));
					tlDataRole.setEmpId(String.valueOf(arr[2]));
					tlDataRole.setRoleName(String.valueOf(arr[3]));
					tlDataRole.setRoleId(String.valueOf(arr[4]));
					tlDataResList.add(tlDataRole);

					String tEmpId = String.valueOf(arr[2]);
					Integer emp = Integer.parseInt(tEmpId);
					// tlDataResList.add(getEmpRoleDataV3(emp));
				}
				if (null != tlDataResList) {
					log.debug("Size of tlDataResList " + tlDataResList.size() + " tlDataResList " + tlDataResList);

					for (TargetRoleRes tr : tlDataResList) {
						log.debug("Inside tr loop " + tr);
						Optional<DmsEmployee> empOpt = dmsEmployeeRepo.findById(Integer.valueOf(tr.getEmpId()));
						if (empOpt.isPresent()) {
							DmsEmployee emp = empOpt.get();
							outputList.addAll(
									getTSDataForRoleV2(buildTargetRoleRes(tr, emp), String.valueOf(empId), "", "", "",req));

						}
					}
				}
			}
			outputList = outputList.stream()
					.collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(TargetSettingRes::getId))),
							ArrayList::new));


			List<TargetSettingRes> outputList1 = new ArrayList<>();
			if(req.getStartDate()!=null && req.getEndDate()!=null && req.getBranchNumber()!=null && req.getBranchNumber().size()>0 ){
				for (int i = 0; i <outputList.size() ; i++) {
					for (int j = 0; j <req.getBranchNumber().size() ; j++) {
						if(outputList.get(i).getBranch().equals(String.valueOf(req.getBranchNumber().get(j)))){
							if(isDateBetweenRange(req,outputList.get(i).getStartDate()) && isDateBetweenRange(req,outputList.get(i).getEndDate()) ){
								outputList1.add(outputList.get(i));
							}
						}
					}
				}
			}else if(  (req.getStartDate()==null || req.getEndDate()==null) &&  req.getBranchNumber()!=null && req.getBranchNumber().size()>0 ){
				for (int i = 0; i <outputList.size() ; i++) {
					for (int j = 0; j <req.getBranchNumber().size() ; j++) {
						if(outputList.get(i).getBranch().equals(String.valueOf(req.getBranchNumber().get(j)))){
							outputList1.add(outputList.get(i));
						}
					}
				}
			}else if(req.getStartDate()!=null && req.getEndDate()!=null && req.getBranchNumber()==null){
				for (int i = 0; i <outputList.size() ; i++) {
					if(isDateBetweenRange(req,outputList.get(i).getStartDate()) && isDateBetweenRange(req,outputList.get(i).getEndDate()) ){
						outputList1.add(outputList.get(i));
					}
				}
			}else{
				outputList1 = outputList;
			}




			log.debug("outputList::"+outputList1);
			int totalCnt = outputList1.size();
			int fromIndex = size * (pageNo - 1);
			int toIndex = size * pageNo;

			if (toIndex > totalCnt) {
				toIndex = totalCnt;
			}
			if (fromIndex > toIndex) {
				fromIndex = toIndex;
			}
			String targetType = req.getTargetType();
			log.debug("targetType in get all api " + targetType);

			if (null != targetType && targetType.equalsIgnoreCase(DynamicFormConstants.TARGET_MONTHLY_TYPE)) {
				outputList1 = outputList1.stream()
						.filter(x -> x.getTargetType().equalsIgnoreCase(DynamicFormConstants.TARGET_MONTHLY_TYPE))
						.collect(Collectors.toList());
			} else if (null != targetType && targetType.equalsIgnoreCase(DynamicFormConstants.TARGET_SPEICAL_TYPE)) {
				outputList1 = outputList1.stream()
						.filter(x -> x.getTargetType().equalsIgnoreCase(DynamicFormConstants.TARGET_SPEICAL_TYPE))
						.collect(Collectors.toList());
			}

			log.debug("outputList1 ::" + outputList1.size());

			if (outputList1 != null && !outputList1.isEmpty() && outputList1.size() > toIndex) {
				outputList1 = outputList1.subList(fromIndex, toIndex);
			}
			outputList1.sort((o1,o2) -> o2.getEndDate().compareTo(o1.getEndDate()));


			List<Integer> listOfParentUser = new ArrayList<>();

			int userId =req.getLoggedInEmpId();
			int reportingId =dmsEmployeeRepo.getReportingPersonId(userId);

			if(userId != reportingId){
				listOfParentUser.add(reportingId);

				while(userId != reportingId){
					userId = reportingId;
					reportingId = dmsEmployeeRepo.getReportingPersonId(userId);
					listOfParentUser.add(reportingId);
				}

				for (int i = 0; i <outputList1.size() ; i++) {
					if(listOfParentUser.contains(outputList1.get(i).getUpdated_by_user_id())){
						outputList1.get(i).setRecordEditable(false);
						outputList1.get(i).setIsAccess("false");
					}else{
						outputList1.get(i).setRecordEditable(true);
						outputList1.get(i).setIsAccess("true");
					}
				}
			}else{
				for (int i = 0; i <outputList1.size() ; i++) {
						outputList1.get(i).setRecordEditable(true);
						outputList1.get(i).setIsAccess("true");
				}
			}



			System.out.println(listOfParentUser);

			List<Integer> availableDataId = new ArrayList<>();
			for (int i = 0; i <outputList1.size() ; i++) {
				List<TargetSettingRecord> targetSettingRecords = new ArrayList<>();

				availableDataId.add(Integer.parseInt(outputList1.get(i).getEmployeeId()));

				TargetSettingRecord ts1 = new TargetSettingRecord();
				ts1.setParamName("Retail");
				ts1.setTarget(outputList1.get(i).getRetailTarget());
				targetSettingRecords.add(ts1);

				TargetSettingRecord ts2 = new TargetSettingRecord();
				ts2.setParamName("Enquiry");
				ts2.setTarget(outputList1.get(i).getEnquiry());
				targetSettingRecords.add(ts2);

				TargetSettingRecord ts3 = new TargetSettingRecord();
				ts3.setParamName("Test Drive");
				ts3.setTarget(outputList1.get(i).getTestDrive());
				targetSettingRecords.add(ts3);

				TargetSettingRecord ts4 = new TargetSettingRecord();
				ts4.setParamName("Visit");
				ts4.setTarget(outputList1.get(i).getHomeVisit());
				targetSettingRecords.add(ts4);

				TargetSettingRecord ts5 = new TargetSettingRecord();
				ts5.setParamName("Booking");
				ts5.setTarget(outputList1.get(i).getBooking());
				targetSettingRecords.add(ts5);

				TargetSettingRecord ts6 = new TargetSettingRecord();
				ts6.setParamName("Exchange");
				ts6.setTarget(outputList1.get(i).getExchange());
				targetSettingRecords.add(ts6);

				TargetSettingRecord ts7 = new TargetSettingRecord();
				ts7.setParamName("Finance");
				ts7.setTarget(outputList1.get(i).getFinance());
				targetSettingRecords.add(ts7);

				TargetSettingRecord ts8 = new TargetSettingRecord();
				ts8.setParamName("Insurance");
				ts8.setTarget(outputList1.get(i).getInsurance());
				targetSettingRecords.add(ts8);

				TargetSettingRecord ts9 = new TargetSettingRecord();
				ts9.setParamName("Exwarranty");
				ts9.setTarget(outputList1.get(i).getExWarranty());
				targetSettingRecords.add(ts9);

				TargetSettingRecord ts10 = new TargetSettingRecord();
				ts10.setParamName("Accessories");
				ts10.setTarget(outputList1.get(i).getAccessories());
				targetSettingRecords.add(ts10);

				outputList1.get(i).setTarget(targetSettingRecords);
			}


			for (Integer userIdData :req.getChildEmpId()) {
				if(!availableDataId.contains(userIdData)){
					TargetSettingRes ts = new TargetSettingRes();
					ts.setEmployeeId(String.valueOf(userIdData));

					Optional<DmsEmployee> dmsEmployee = dmsEmployeeRepo.findEmpById(userIdData);
					ts.setEmployeeId(String.valueOf(userIdData));
					if(dmsEmployee.isPresent() && dmsEmployee.get()!=null){
						if(dmsEmployee.get().getDeptId()!=null)
						ts.setDepartment(dmsEmployee.get().getDeptId());

						if(dmsEmployee.get().getDesignationId()!=null)
						ts.setDesignation(dmsEmployee.get().getDesignationId());
					}

					outputList1.add(ts);
				}
			}


			map.put("totalCnt", totalCnt);
			map.put("pageNo", pageNo);
			map.put("size", size);
			map.put("data", outputList1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}


	private boolean isDateBetweenRange(TargetPlanningReq req, String dataStartDate1) throws ParseException {
		Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(req.getStartDate());
		Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(req.getEndDate());
		Date dataStartDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataStartDate1);
		return startDate.compareTo(dataStartDate) * dataStartDate.compareTo(endDate) >= 0;
	}





	public String getFirstDayOfMonth() {
		return LocalDate.ofEpochDay(System.currentTimeMillis() / (24 * 60 * 60 * 1000) ).withDayOfMonth(1).toString();
	}
	public String getLastDayOfMonth() {
		return LocalDate.ofEpochDay(System.currentTimeMillis() / (24 * 60 * 60 * 1000) ).plusMonths(1).withDayOfMonth(1).minusDays(1).toString();
	}

	@Override
	public List<TargetPlanningCountRes> getTargetPlanningParamsCount(TargetPlanningReq req) throws DynamicFormsServiceException {
		List<TargetPlanningCountRes> targetPlanningCountResList = new ArrayList<>();

		for(Integer childEmpId : req.getChildEmpId()){
			TargetPlanningCountRes targetPlanningCountRes = getAllSelectedUserTargetPlanningCount(req,req.getStartDate(),req.getEndDate(),childEmpId);
			if(targetPlanningCountRes.getEmployeeId() !=null){

				List<TargetSettingRecord> targetSettingRecords = new ArrayList<>();

				TargetSettingRecord ts1 = new TargetSettingRecord();
				ts1.setParamName("Retail");
				ts1.setTarget(targetPlanningCountRes.getRetailTarget());
				targetSettingRecords.add(ts1);

				TargetSettingRecord ts2 = new TargetSettingRecord();
				ts2.setParamName("Enquiry");
				ts2.setTarget(targetPlanningCountRes.getEnquiry());
				targetSettingRecords.add(ts2);

				TargetSettingRecord ts3 = new TargetSettingRecord();
				ts3.setParamName("Test Drive");
				ts3.setTarget(targetPlanningCountRes.getTestDrive());
				targetSettingRecords.add(ts3);

				TargetSettingRecord ts4 = new TargetSettingRecord();
				ts4.setParamName("Visit");
				ts4.setTarget(targetPlanningCountRes.getHomeVisit());
				targetSettingRecords.add(ts4);

				TargetSettingRecord ts5 = new TargetSettingRecord();
				ts5.setParamName("Booking");
				ts5.setTarget(targetPlanningCountRes.getBooking());
				targetSettingRecords.add(ts5);

				TargetSettingRecord ts6 = new TargetSettingRecord();
				ts6.setParamName("Exchange");
				ts6.setTarget(targetPlanningCountRes.getExchange());
				targetSettingRecords.add(ts6);

				TargetSettingRecord ts7 = new TargetSettingRecord();
				ts7.setParamName("Finance");
				ts7.setTarget(targetPlanningCountRes.getFinance());
				targetSettingRecords.add(ts7);

				TargetSettingRecord ts8 = new TargetSettingRecord();
				ts8.setParamName("Insurance");
				ts8.setTarget(targetPlanningCountRes.getInsurance());
				targetSettingRecords.add(ts8);

				TargetSettingRecord ts9 = new TargetSettingRecord();
				ts9.setParamName("Exwarranty");
				ts9.setTarget(targetPlanningCountRes.getExWarranty());
				targetSettingRecords.add(ts9);

				TargetSettingRecord ts10 = new TargetSettingRecord();
				ts10.setParamName("Accessories");
				ts10.setTarget(targetPlanningCountRes.getAccessories());
				targetSettingRecords.add(ts10);

				targetPlanningCountRes.setTarget(targetSettingRecords);
				targetPlanningCountResList.add(targetPlanningCountRes);
			}
		}

		return targetPlanningCountResList;
	}



	private TargetPlanningCountRes getAllSelectedUserTargetPlanningCount(TargetPlanningReq req,String reqStartDate, String reqEndDate,int loggedInEmpId){
		int retail =0, enquiry=0, testDrive=0, visit=0, booking=0, exchange=0, finance=0, insurance=0, exwarranty=0,accessories=0;
		String startDate = null;
		String endDate = null;
		TargetPlanningCountRes targetPlanningCountRes = new TargetPlanningCountRes();
		if (null == reqStartDate && null == reqEndDate) {
			startDate = getFirstDayOfMonth();
			endDate = getLastDayOfMonth();
		} else {
			startDate = reqStartDate;
			endDate = reqEndDate;
		}

		List<TargetSettingRes> outputList = new ArrayList<>();
		Map<String, Object> map = new LinkedHashMap<>();

		try {
			int empId = loggedInEmpId;

			List<Integer> empReportingIdList = new ArrayList<>();
			empReportingIdList.add(empId);
			empReportingIdList.addAll(dashBoardServiceImplV2.getReportingEmployes(empId));

			log.debug("empReportingIdList for emp " + empId);
			log.debug("" + empReportingIdList);

			for (Integer id : empReportingIdList) {

				String eId = String.valueOf(id);
				log.debug("Executing for EMP ID " + eId);

				String tmpQuery = roleMapQuery.replaceAll("<EMP_ID>", eId);
				List<Object[]> tlData = entityManager.createNativeQuery(tmpQuery).getResultList();
				List<TargetRoleRes> tlDataResList = new ArrayList<>();

				for (Object[] arr : tlData) {
					TargetRoleRes tlDataRole = new TargetRoleRes();
					tlDataRole.setOrgId(String.valueOf(arr[0]));
					tlDataRole.setBranchId(String.valueOf(arr[1]));
					tlDataRole.setEmpId(String.valueOf(arr[2]));
					tlDataRole.setRoleName(String.valueOf(arr[3]));
					tlDataRole.setRoleId(String.valueOf(arr[4]));
					tlDataResList.add(tlDataRole);

					String tEmpId = String.valueOf(arr[2]);
					Integer emp = Integer.parseInt(tEmpId);
					// tlDataResList.add(getEmpRoleDataV3(emp));
				}
				if (null != tlDataResList) {
					log.debug("Size of tlDataResList " + tlDataResList.size() + " tlDataResList " + tlDataResList);

					for (TargetRoleRes tr : tlDataResList) {
						log.debug("Inside tr loop " + tr);
						Optional<DmsEmployee> empOpt = dmsEmployeeRepo.findById(Integer.valueOf(tr.getEmpId()));
						if (empOpt.isPresent()) {
							DmsEmployee emp = empOpt.get();
							outputList.addAll(
									getTPDataBasedOnDate(buildTargetRoleRes(tr, emp), String.valueOf(empId), "", "", "",startDate,endDate,req));

						}
					}
				}
			}
			outputList = outputList.stream()
					.collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(TargetSettingRes::getId))),
							ArrayList::new));


//			outputList.sort((o1,o2) -> o2.getEndDate().compareTo(o1.getEndDate()));





			List<TargetSettingRes> outputList1 = new ArrayList<>();
			if(req.getStartDate()!=null && req.getEndDate()!=null && req.getBranchNumber()!=null && req.getBranchNumber().size()>0 ){
				for (int i = 0; i <outputList.size() ; i++) {
					for (int j = 0; j <req.getBranchNumber().size() ; j++) {
						if(outputList.get(i).getBranch().equals(String.valueOf(req.getBranchNumber().get(j)))){
							if(isDateBetweenRange(req,outputList.get(i).getStartDate()) && isDateBetweenRange(req,outputList.get(i).getEndDate()) ){
								outputList1.add(outputList.get(i));
							}
						}
					}
				}
			}else if(  (req.getStartDate()==null || req.getEndDate()==null) &&  req.getBranchNumber()!=null && req.getBranchNumber().size()>0 ){
				for (int i = 0; i <outputList.size() ; i++) {
					for (int j = 0; j <req.getBranchNumber().size() ; j++) {
						if(outputList.get(i).getBranch().equals(String.valueOf(req.getBranchNumber().get(j)))){
								outputList1.add(outputList.get(i));
						}
					}
				}
			}else if(req.getStartDate()!=null && req.getEndDate()!=null && req.getBranchNumber()==null){
				for (int i = 0; i <outputList.size() ; i++) {
					if(isDateBetweenRange(req,outputList.get(i).getStartDate()) && isDateBetweenRange(req,outputList.get(i).getEndDate()) ){
						outputList1.add(outputList.get(i));
					}
				}
			}else{
				outputList1 = outputList;
			}


			String targetType = req.getTargetType();
			log.debug("targetType in get all api " + targetType);

			if (null != targetType && targetType.equalsIgnoreCase(DynamicFormConstants.TARGET_MONTHLY_TYPE)) {
				outputList1 = outputList1.stream()
						.filter(x -> x.getTargetType().equalsIgnoreCase(DynamicFormConstants.TARGET_MONTHLY_TYPE))
						.collect(Collectors.toList());
			} else if (null != targetType && targetType.equalsIgnoreCase(DynamicFormConstants.TARGET_SPEICAL_TYPE)) {
				outputList1 = outputList1.stream()
						.filter(x -> x.getTargetType().equalsIgnoreCase(DynamicFormConstants.TARGET_SPEICAL_TYPE))
						.collect(Collectors.toList());
			}

			for (int i = 0; i <outputList1.size() ; i++) {
				if(outputList1.get(i)!=null) {
					if(outputList1.get(i).getRetailTarget()!=null)
						retail += Integer.parseInt(outputList1.get(i).getRetailTarget());

					if(outputList1.get(i).getEnquiry()!=null)
						enquiry += Integer.parseInt(outputList1.get(i).getEnquiry());

					if(outputList1.get(i).getTestDrive()!=null)
						testDrive += Integer.parseInt(outputList1.get(i).getTestDrive());

					if(outputList1.get(i).getHomeVisit()!=null)
						visit += Integer.parseInt(outputList1.get(i).getHomeVisit());

					if(outputList1.get(i).getBooking()!=null)
						booking += Integer.parseInt(outputList1.get(i).getBooking());

					if(outputList1.get(i).getExchange()!=null)
						exchange += Integer.parseInt(outputList1.get(i).getExchange());

					if(outputList1.get(i).getFinance()!=null)
						finance += Integer.parseInt(outputList1.get(i).getFinance());

					if(outputList1.get(i).getInsurance()!=null)
						insurance += Integer.parseInt(outputList1.get(i).getInsurance());

					if(outputList1.get(i).getExWarranty()!=null)
						exwarranty += Integer.parseInt(outputList1.get(i).getExWarranty());

					if(outputList1.get(i).getAccessories()!=null)
						accessories += Integer.parseInt(outputList1.get(i).getAccessories());
				}

			}

			targetPlanningCountRes.setRetailTarget(String.valueOf(retail));
			targetPlanningCountRes.setEnquiry(String.valueOf(enquiry));
			targetPlanningCountRes.setTestDrive(String.valueOf(testDrive));
			targetPlanningCountRes.setHomeVisit(String.valueOf(visit));
			targetPlanningCountRes.setBooking(String.valueOf(booking));
			targetPlanningCountRes.setExchange(String.valueOf(exchange));
			targetPlanningCountRes.setFinance(String.valueOf(finance));
			targetPlanningCountRes.setInsurance(String.valueOf(insurance));
			targetPlanningCountRes.setExWarranty(String.valueOf(exwarranty));
			targetPlanningCountRes.setAccessories(String.valueOf(accessories));
			targetPlanningCountRes.setStartDate(reqStartDate);
			targetPlanningCountRes.setEndDate(reqEndDate);
			targetPlanningCountRes.setEmployeeId(String.valueOf(loggedInEmpId));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return targetPlanningCountRes;
	}


	public List<TargetSettingRes> getTPDataBasedOnDate(TargetRoleRes tRole, String teamLeadId, String managerId,
													 String branchMgrId, String generalMgrId,String startDate, String endDate,TargetPlanningReq req) {
		log.debug("Inside getTSDataForRoleV2(),TROLE " + tRole);
		List<TargetSettingRes> list = new ArrayList<>();
		try {

			for (TargetEntity te : getTargetSettingMasterDataForGivenRole(tRole)) {
				// List<TargetEntityUser> tesUserList =
				// targetUserRepo.findAllEmpIds(tRole.getEmpId());
				List<TargetEntityUser> tesUserList = targetUserRepo.findAllEmpIdsWithNoDefault(tRole.getEmpId(),req.getStartDate(),req.getEndDate());
				List<TargetEntityUser> teUserDefaultList = targetUserRepo.findAllEmpIdsWithDefault(tRole.getEmpId(),req.getStartDate(),req.getEndDate());
				log.debug("teUserDefaultList is not empty " + teUserDefaultList.size());
				log.debug("tesUserList is not empty " + tesUserList.size());
				if (null != tesUserList && !tesUserList.isEmpty()) {

					for (TargetEntityUser teUser : tesUserList) {
						modelMapper.getConfiguration().setAmbiguityIgnored(true);
						TargetSettingRes tsRes = modelMapper.map(teUser, TargetSettingRes.class);
						tsRes = convertTargetStrToObj(teUser.getTargets(), tsRes);
						tsRes.setEmpName(getEmpName(tRole.getEmpId()));
						tsRes.setEmployeeId(tRole.getEmpId());
						tsRes.setId(teUser.getGeneratedId());
						tsRes.setTargetName(teUser.getTargetName());
						tsRes.setTargetType(teUser.getTargetType());
						if (null != teamLeadId) {
							tsRes.setTeamLead(getTeamLeadName(teamLeadId));
							tsRes.setTeamLeadId(teamLeadId);
						}
						if (null != managerId) {
							tsRes.setManager(getEmpName(managerId));
							tsRes.setManagerId(managerId);
						}
						if (null != branchMgrId) {
							tsRes.setBranchManagerId(branchMgrId);
							tsRes.setBranchmanger(getEmpName(branchMgrId));
						}
						if (null != generalMgrId) {
							tsRes.setGeneralManagerId(generalMgrId);
							tsRes.setGeneralManager(getEmpName(generalMgrId));
						}

						if (null != tRole.getLocationId()) {
							tsRes.setLocationName(getLocationName(tRole.getLocationId()));
						}
						if (null != tRole.getBranchId()) {
							tsRes.setBranchName(getBranchName(tRole.getBranchId()));
						}
						if (null != tRole.getDeptId()) {
							tsRes.setDepartmentName(getDeptName(tRole.getDeptId()));
						}
						if (null != tRole.getDesignationId()) {
							tsRes.setDesignationName(getDesignationName(tRole.getDesignationId()));
						}
						list.add(tsRes);
					}

				}

				if(null!=teUserDefaultList && teUserDefaultList.isEmpty()){
					log.debug("tesUserList is  empty ");
					modelMapper.getConfiguration().setAmbiguityIgnored(true);

					TargetSettingRes res = modelMapper.map(te, TargetSettingRes.class);
					res.setStartDate(getFirstDayOfQurter());
					res.setEndDate(getLastDayOfQurter());

					TargetEntityUser teUserToSave = modelMapper.map(res, TargetEntityUser.class);
					teUserToSave.setEmployeeId(tRole.getEmpId());

					teUserToSave.setTargets(te.getTargets());
				//	teUserToSave.setType("default");
					teUserToSave.setActive(GsAppConstants.ACTIVE);
					teUserToSave.setExperience(tRole.getExperience());
					teUserToSave.setTargetName("DEFAULT");
					teUserToSave.setTargetType(DynamicFormConstants.TARGET_MONTHLY_TYPE);
					Optional<TargetEntityUser> defaultTeUserOpt = targetUserRepo
							.checkDefaultDataInTargetUser(tRole.getEmpId());

					log.debug("defaultTeUserOpt:::"+defaultTeUserOpt);
					if (!defaultTeUserOpt.isPresent()) {
						log.debug("Default data is empty for " + tRole.getEmpId());
						TargetEntityUser dbRes = targetUserRepo.save(teUserToSave);
						res.setId(dbRes.getGeneratedId());
					}

					res = convertTargetStrToObjV3(te.getTargets(), res);
					res.setEmpName(getEmpName(tRole.getEmpId()));
					res.setEmployeeId(tRole.getEmpId());
					res.setTargetName("DEFAULT");
					res.setTargetType(DynamicFormConstants.TARGET_MONTHLY_TYPE);
					res.setExperience(tRole.getExperience() != null ? tRole.getExperience() : "");
					if (null != teamLeadId) {
						res.setTeamLead(getTeamLeadName(teamLeadId));
						res.setTeamLeadId(teamLeadId);
					}
					if (null != managerId) {
						res.setManager(getEmpName(managerId));
						res.setManagerId(managerId);
					}
					if (null != branchMgrId) {
						res.setBranchManagerId(branchMgrId);
						res.setBranchmanger(getEmpName(branchMgrId));
					}
					if (null != generalMgrId) {
						res.setGeneralManagerId(generalMgrId);
						res.setGeneralManager(getEmpName(generalMgrId));
					}

					if (null != tRole.getLocationId()) {
						res.setLocationName(getLocationName(tRole.getLocationId()));
					}
					if (null != tRole.getBranchId()) {
						res.setBranchName(getBranchName(tRole.getBranchId()));
					}
					if (null != tRole.getDeptId()) {
						res.setDepartmentName(getDeptName(tRole.getDeptId()));
					}
					if (null != tRole.getDesignationId()) {
						res.setDesignationName(getDesignationName(tRole.getDesignationId()));
					}
					log.debug("res::::::::::::"+res);
					list.add(res);

				}
				if(null!=teUserDefaultList && !teUserDefaultList.isEmpty()){
					log.debug("teUserDefaultList is not empty");
					for (TargetEntityUser teUser : teUserDefaultList) {
						modelMapper.getConfiguration().setAmbiguityIgnored(true);
						TargetSettingRes tsRes = modelMapper.map(teUser, TargetSettingRes.class);
						tsRes = convertTargetStrToObj(teUser.getTargets(), tsRes);
						tsRes.setEmpName(getEmpName(tRole.getEmpId()));
						tsRes.setEmployeeId(tRole.getEmpId());
						tsRes.setId(teUser.getGeneratedId());
						tsRes.setTargetName(teUser.getTargetName());
						tsRes.setTargetType(teUser.getTargetType());
						if (null != teamLeadId) {
							tsRes.setTeamLead(getTeamLeadName(teamLeadId));
							tsRes.setTeamLeadId(teamLeadId);
						}
						if (null != managerId) {
							tsRes.setManager(getEmpName(managerId));
							tsRes.setManagerId(managerId);
						}
						if (null != branchMgrId) {
							tsRes.setBranchManagerId(branchMgrId);
							tsRes.setBranchmanger(getEmpName(branchMgrId));
						}
						if (null != generalMgrId) {
							tsRes.setGeneralManagerId(generalMgrId);
							tsRes.setGeneralManager(getEmpName(generalMgrId));
						}

						if (null != tRole.getLocationId()) {
							tsRes.setLocationName(getLocationName(tRole.getLocationId()));
						}
						if (null != tRole.getBranchId()) {
							tsRes.setBranchName(getBranchName(tRole.getBranchId()));
						}
						if (null != tRole.getDeptId()) {
							tsRes.setDepartmentName(getDeptName(tRole.getDeptId()));
						}
						if (null != tRole.getDesignationId()) {
							tsRes.setDesignationName(getDesignationName(tRole.getDesignationId()));
						}
						list.add(tsRes);
					}

				}
			}
			log.debug("list in getTSDataForRoleV2  " + list);

		} catch (Exception e) {
			log.error("getTargetSettingData() ", e);
		}

		List<TargetSettingRes> outPutData = new ArrayList<>();
		for (int i = 0; i <list.size() ; i++) {
			try{
				Date startD=new SimpleDateFormat("yyyy-MM-dd").parse(list.get(i).getStartDate());
				Date endD=new SimpleDateFormat("yyyy-MM-dd").parse(list.get(i).getEndDate());

				Date reqStartDate=new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
				Date reqEndDate=new SimpleDateFormat("yyyy-MM-dd").parse(endDate);


				if((startD.after(reqStartDate) || (startD.equals(reqStartDate)))&&
								(endD.before(reqEndDate) || (endD.equals(reqEndDate)))){
					outPutData.add(list.get(i));
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		return outPutData;
	}







	/*
	 * @Override public Map<String, Object> getTargetDataWithRole(TargetRoleReq req)
	 * throws DynamicFormsServiceException {
	 * log.debug("calling getTargetDataWithRole ,given req "+req);
	 * List<TargetSettingRes> outputList = new ArrayList<>(); Map<String, Object>
	 * map = new LinkedHashMap<>(); int pageNo = req.getPageNo(); int size =
	 * req.getSize(); try { int empId = req.getEmpId();
	 * 
	 * 
	 * TargetRoleRes trRoot = getEmpRoleData(empId);
	 * log.debug("Givne Emp Designation "+trRoot.getDesignationName());
	 * 
	 * if (validateDSE(trRoot.getDesignationName())) {
	 * log.info("Generating Data for DSE");
	 * outputList.addAll(getTSDataForRoleV2(trRoot, null, null, null, null)); }
	 * 
	 * else if (validateTL(trRoot.getDesignationName())) {
	 * log.info("Generating Data for TL of ID " + empId); outputList =
	 * getTLData(String.valueOf(empId), outputList, null, null, null); }
	 * 
	 * else if (validateMgr(trRoot.getDesignationName())) {
	 * log.info("Generating Data for MANAGER of ID " + empId); outputList =
	 * getManagerData(String.valueOf(empId), outputList, null, null); } else if
	 * (validateBranchMgr(trRoot.getDesignationName())) {
	 * log.info("Generating Data for Branch Mgr of ID " + empId); outputList =
	 * getBranchMgrData(String.valueOf(empId), outputList, null); } else if
	 * (validateGeneralMgr(trRoot.getDesignationName())) {
	 * log.info("Generating Data for General Mgr of ID " + empId); outputList =
	 * getGeneralMgrData(String.valueOf(empId), outputList); } outputList =
	 * outputList.stream().distinct().collect(Collectors.toList()); int totalCnt =
	 * outputList.size(); int fromIndex = size * (pageNo - 1); int toIndex = size *
	 * pageNo;
	 * 
	 * if (toIndex > totalCnt) { toIndex = totalCnt; } if (fromIndex > toIndex) {
	 * fromIndex = toIndex; } log.debug("outputList ::"+outputList.size());
	 * 
	 * if(outputList!=null && !outputList.isEmpty() && outputList.size()>toIndex) {
	 * outputList = outputList.subList(fromIndex, toIndex); } map.put("totalCnt",
	 * totalCnt); map.put("pageNo", pageNo); map.put("size", size); map.put("data",
	 * outputList); } catch (Exception e) { e.printStackTrace(); } return map; }
	 * 
	 */

	/**
	 * @param empId
	 * @return
	 */

	public TargetRoleRes getEmpRoleData(int empId) throws DynamicFormsServiceException {

		String tmpQuery = dmsEmpByidQuery.replaceAll("<EMP_ID>", String.valueOf(empId));

		tmpQuery = roleMapQuery.replaceAll("<EMP_ID>", String.valueOf(empId));
		List<Object[]> data = entityManager.createNativeQuery(tmpQuery).getResultList();
		TargetRoleRes trRoot = new TargetRoleRes();

		for (Object[] arr : data) {
			trRoot.setOrgId(String.valueOf(arr[0]));
			trRoot.setBranchId(String.valueOf(arr[1]));
			trRoot.setEmpId(String.valueOf(arr[2]));
			trRoot.setRoleName(String.valueOf(arr[3]));
			trRoot.setRoleId(String.valueOf(arr[4]));
			trRoot.setPrecedence(Integer.parseInt(arr[5].toString()));
			String empid = trRoot.getEmpId();
			if (null != empid) {
				TargetRoleRes tm = getEmpRoleDataV3(Integer.parseInt(empid));
				List<String> l = tm.getOrgMapBranches();
				trRoot.setOrgMapBranches(l);
				;
				if (l != null) {
					trRoot.setBranchId(l.get(0));

				}
			}
		}

		Optional<DmsEmployee> empOpt = dmsEmployeeRepo.findById(empId);
		DmsEmployee emp = null;
		if (empOpt.isPresent()) {
			emp = empOpt.get();
			trRoot = buildTargetRoleRes(trRoot, emp);
		} else {
			throw new DynamicFormsServiceException("No Empoloyee with given empId in DB", HttpStatus.BAD_REQUEST);

		}
		log.debug("trRoot " + trRoot);

		return trRoot;
	}

	public List<TargetRoleRes> getEmpRoles(int empId) {

		List<TargetRoleRes> empRoles = new ArrayList<>();
		String tmpQuery = roleMapQuery.replaceAll("<EMP_ID>", String.valueOf(empId));
		List<Object[]> data = entityManager.createNativeQuery(tmpQuery).getResultList();

		for (final Object[] arr : data) {
			final TargetRoleRes trRoot = new TargetRoleRes();
			trRoot.setOrgId(String.valueOf(arr[0]));
			trRoot.setBranchId(String.valueOf(arr[1]));
			trRoot.setEmpId(String.valueOf(arr[2]));
			trRoot.setRoleName(String.valueOf(arr[3]));
			trRoot.setRoleId(String.valueOf(arr[4]));
			trRoot.setPrecedence(Integer.valueOf(arr[5].toString()));
			empRoles.add(trRoot);
		}
		return empRoles;
	}

	private List<TargetSettingRes> getGeneralMgrData(String empId, List<TargetSettingRes> outputList,TargetPlanningReq req) {
		List<Object> branchMgrReportiesEmpIds = entityManager
				.createNativeQuery(getEmpUnderTLQuery.replaceAll("<ID>", empId)).getResultList();

		for (Object id : branchMgrReportiesEmpIds) {
			String eId = String.valueOf(id);
			log.info("Executing for General MANAGER ID " + eId);

			outputList = getBranchMgrData(eId, outputList, empId,req);
		}

		return outputList;
	}

	private List<TargetSettingRes> getBranchMgrData(String empId, List<TargetSettingRes> outputList,
			String generalMgrId,TargetPlanningReq req) {

		List<Object> branchMgrReportiesEmpIds = entityManager
				.createNativeQuery(getEmpUnderTLQuery.replaceAll("<ID>", empId)).getResultList();

		for (Object id : branchMgrReportiesEmpIds) {
			String eId = String.valueOf(id);
			log.info("Executing for Branch MANAGER ID " + eId);

			outputList = getManagerData(eId, outputList, empId, generalMgrId,req);
		}

		return outputList;
	}

	private List<TargetSettingRes> getManagerData(String empId, List<TargetSettingRes> outputList, String branchMgrId,
			String generalMgrId,TargetPlanningReq req) {

		List<Object> managerTLEmpIds = entityManager.createNativeQuery(getEmpUnderTLQuery.replaceAll("<ID>", empId))
				.getResultList();
		for (Object id : managerTLEmpIds) {
			String eId = String.valueOf(id);
			log.info("Executing for TL ID " + eId);

			outputList = getTLData(eId, outputList, empId, branchMgrId, generalMgrId,req);
		}
		return outputList;
	}

	private List<TargetSettingRes> getTLData(String empId, List<TargetSettingRes> outputList, String managerId,
			String branchMgrId, String generalMgrId,TargetPlanningReq req) {
		log.debug("getTLData(){},empId " + empId);
		try {
			List<Object> tlEmpIDData = entityManager
					.createNativeQuery(getEmpUnderTLQuery.replaceAll("<ID>", String.valueOf(empId))).getResultList();
			log.debug("tlEmpIDData size " + tlEmpIDData.size());
			if (!tlEmpIDData.isEmpty()) {
				for (Object id : tlEmpIDData) {
					String eId = String.valueOf(id);
					log.debug("Executing for EMP ID " + eId);

					String tmpQuery = roleMapQuery.replaceAll("<EMP_ID>", eId);
					List<Object[]> tlData = entityManager.createNativeQuery(tmpQuery).getResultList();
					List<TargetRoleRes> tlDataResList = new ArrayList<>();

					for (Object[] arr : tlData) {
						TargetRoleRes tlDataRole = new TargetRoleRes();
						tlDataRole.setOrgId(String.valueOf(arr[0]));
						tlDataRole.setBranchId(String.valueOf(arr[1]));
						tlDataRole.setEmpId(String.valueOf(arr[2]));
						tlDataRole.setRoleName(String.valueOf(arr[3]));
						tlDataRole.setRoleId(String.valueOf(arr[4]));
						tlDataResList.add(tlDataRole);

						String tEmpId = String.valueOf(arr[2]);
						Integer emp = Integer.parseInt(tEmpId);
						tlDataResList.add(getEmpRoleDataV3(emp));
					}
					if (null != tlDataResList) {
						log.info("Size of tlDataResList " + tlDataResList.size() + " tlDataResList " + tlDataResList);

						for (TargetRoleRes tr : tlDataResList) {
							Optional<DmsEmployee> empOpt = dmsEmployeeRepo.findById(Integer.valueOf(tr.getEmpId()));
							if (empOpt.isPresent()) {
								DmsEmployee emp = empOpt.get();
								// outputList.add(getTSDataForRole(buildTargetRoleRes(tr,
								// emp),String.valueOf(empId),managerId,branchMgrId,generalMgrId));
								outputList.addAll(getTSDataForRoleV2(buildTargetRoleRes(tr, emp), String.valueOf(empId),
										managerId, branchMgrId, generalMgrId,req));

							}
						}
					}
				}
			} else {
				log.debug("tlEmpIDData is empty ");

				String eId = String.valueOf(empId);
				log.debug("Executing for EMP ID " + eId);

				String tmpQuery = roleMapQuery.replaceAll("<EMP_ID>", eId);
				List<Object[]> tlData = entityManager.createNativeQuery(tmpQuery).getResultList();
				List<TargetRoleRes> tlDataResList = new ArrayList<>();

				for (Object[] arr : tlData) {
					TargetRoleRes tlDataRole = new TargetRoleRes();
					tlDataRole.setOrgId(String.valueOf(arr[0]));
					tlDataRole.setBranchId(String.valueOf(arr[1]));
					tlDataRole.setEmpId(String.valueOf(arr[2]));
					tlDataRole.setRoleName(String.valueOf(arr[3]));
					tlDataRole.setRoleId(String.valueOf(arr[4]));
					tlDataResList.add(tlDataRole);

					String tEmpId = String.valueOf(arr[2]);
					Integer emp = Integer.parseInt(tEmpId);
					tlDataResList.add(getEmpRoleDataV3(emp));
				}
				if (null != tlDataResList) {
					log.info("Size of tlDataResList " + tlDataResList.size() + " tlDataResList " + tlDataResList);

					for (TargetRoleRes tr : tlDataResList) {
						Optional<DmsEmployee> empOpt = dmsEmployeeRepo.findById(Integer.valueOf(tr.getEmpId()));
						if (empOpt.isPresent()) {
							DmsEmployee emp = empOpt.get();
							// outputList.add(getTSDataForRole(buildTargetRoleRes(tr,
							// emp),String.valueOf(empId),managerId,branchMgrId,generalMgrId));
							outputList.addAll(getTSDataForRoleV2(buildTargetRoleRes(tr, emp), String.valueOf(empId),
									managerId, branchMgrId, generalMgrId,req));

						}
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputList;
	}

	private TargetRoleRes buildTargetRoleRes(TargetRoleRes trRoot, DmsEmployee emp) {
		try {
			trRoot.setSalary(getEmpSal(emp.getEmp_id()));

			// trRoot.setSalary(emp.getBasicSal());
			trRoot.setLocationId(emp.getLocationId());
			trRoot.setDesignationId(emp.getDesignationId());
			trRoot.setDesignationName(getDesignationName(emp.getDesignationId())); // trRoot.setDesignationName(getDesignationName(emp.getDesignationId()));
			trRoot.setExperience(calcualteExperience(emp.getPrevExperience(), emp.getJoiningDate()));
			trRoot.setDeptId(emp.getDeptId());
			trRoot.setDeptName(getDeptName(emp.getDeptId()));
			trRoot.setBranchId(emp.getBranch());
			// trRoot.setBranchId(trRoot.getOrgMapBranches());
			List<String> l = trRoot.getOrgMapBranches();
			log.debug("ORG MAP BRANCHES in buildTargetRoleRes " + trRoot.getOrgMapBranches());
			if (null != l && !l.isEmpty()) {
				trRoot.setBranchId(l.get(0));
			}

			trRoot.setHrmsRole(emp.getHrmsRole());
			// trRoot.setLevel(getEmpLevel(emp.getEmp_id()));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return trRoot;
	}

	
	///immidiate hirr
	private TargetRoleRes buildTargetRoleResemps(TargetRoleRes trRoot, List<DmsEmployee> emp) {
		try {
			for(DmsEmployee singleEmp : emp) {
			trRoot.setSalary(getEmpSal(singleEmp.getEmp_id()));

			// trRoot.setSalary(emp.getBasicSal());
			trRoot.setLocationId(singleEmp.getLocationId());
			trRoot.setDesignationId(singleEmp.getDesignationId());
			trRoot.setDesignationName(getDesignationName(singleEmp.getDesignationId())); // trRoot.setDesignationName(getDesignationName(emp.getDesignationId()));
			trRoot.setExperience(calcualteExperience(singleEmp.getPrevExperience(), singleEmp.getJoiningDate()));
			trRoot.setDeptId(singleEmp.getDeptId());
			trRoot.setDeptName(getDeptName(singleEmp.getDeptId()));
			trRoot.setBranchId(singleEmp.getBranch());
			// trRoot.setBranchId(trRoot.getOrgMapBranches());
			List<String> l = trRoot.getOrgMapBranches();
			log.debug("ORG MAP BRANCHES in buildTargetRoleRes " + trRoot.getOrgMapBranches());
			if (null != l && !l.isEmpty()) {
				trRoot.setBranchId(l.get(0));
			}

			trRoot.setHrmsRole(singleEmp.getHrmsRole());
			// trRoot.setLevel(getEmpLevel(emp.getEmp_id()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trRoot;
	}

	@Autowired
	DmsDesignationRepo dmsDesignationRepo;

	private Integer getEmpLevel(Integer empId) throws DynamicFormsServiceException {
		Integer empLevel = 0;
		Optional<DmsDesignation> desOpt = dmsDesignationRepo.findById(empId);
		if (desOpt.isPresent()) {
			empLevel = desOpt.get().getLevel();
		} else {
			throw new DynamicFormsServiceException("Given emp does not have valid designation in dms_designation",
					HttpStatus.BAD_REQUEST);
		}

		log.debug("Given emp level is " + empLevel);
		return empLevel;
	}

	public TargetSettingRes getTSDataForRole(TargetRoleRes tRole, String teamLeadId, String managerId,
			String branchMgrId, String generalMgrId) {
		log.debug("Inside getTSDataForRole()");
		TargetSettingRes res = new TargetSettingRes();
		try {
			List<TargetEntity> dbList = targetSettingRepo.getTargetmappingDataWithRole(tRole.getOrgId(),
					tRole.getBranchId(), tRole.getDeptId(), tRole.getExperience(), tRole.getSalary());
			for (TargetEntity te : dbList) {
				res = modelMapper.map(te, TargetSettingRes.class);

				Optional<TargetEntityUser> tesOpt = targetUserRepo.findByEmpId(tRole.getEmpId());
				String target = null;
				if (tesOpt.isPresent()) {
					TargetEntityUser tes = tesOpt.get();
					target = tesOpt.get().getTargets();
					res.setStartDate(tes.getStartDate());
					res.setEndDate(tes.getEndDate());
				} else {
					target = te.getTargets();
				}
				////System.out.println("target::" + target);
				res = convertTargetStrToObj(target, res);
				res.setEmpName(getEmpName(tRole.getEmpId()));
				res.setEmployeeId(tRole.getEmpId());
				if (null != teamLeadId) {
					res.setTeamLead(getTeamLeadName(teamLeadId));
					res.setTeamLeadId(teamLeadId);
				}
				if (null != managerId) {
					res.setManager(getEmpName(managerId));
					res.setManagerId(managerId);
				}
				if (null != branchMgrId) {
					res.setBranchManagerId(branchMgrId);
					res.setBranchmanger(getEmpName(branchMgrId));
				}
				if (null != generalMgrId) {
					res.setGeneralManagerId(generalMgrId);
					res.setGeneralManager(getEmpName(generalMgrId));
				}
				// res.setManager(getDropDownValueById(te.getManagerId(),"manager"));
				// res.setBranchmanger(getDropDownValueById(te.getBranchmangerId(),"branchmanager"));
				// res.setBranch(getDropDownValueById(te.getBranch(),"branch"));
				// res.setLocation(getDropDownValueById(te.getLocation(), "location"));
				res.setDepartment(getDropDownValueById(te.getDepartment(), "department"));
				res.setDesignation(getDropDownValueById(te.getDesignation(), "designation"));
				res.setExperience(getDropDownValueById(te.getExperience(), "experience"));
				res.setSalrayRange(getDropDownValueById(te.getSalrayRange(), "salaryrange"));

			}

		} catch (Exception e) {
			log.error("getTargetSettingData() ", e);
		}
		return res;
	}

	public List<TargetSettingRes> getTSDataForRoleV2(TargetRoleRes tRole, String teamLeadId, String managerId,
			String branchMgrId, String generalMgrId,TargetPlanningReq req) {
		log.debug("Inside getTSDataForRoleV2(),TROLE " + tRole);
		List<TargetSettingRes> list = new ArrayList<>();
		try {

			for (TargetEntity te : getTargetSettingMasterDataForGivenRole(tRole)) {
				// List<TargetEntityUser> tesUserList =
				// targetUserRepo.findAllEmpIds(tRole.getEmpId());
				List<TargetEntityUser> tesUserList = targetUserRepo.findAllEmpIdsWithNoDefault(tRole.getEmpId(),req.getStartDate(),req.getEndDate());
				List<TargetEntityUser> teUserDefaultList = targetUserRepo.findAllEmpIdsWithDefault(tRole.getEmpId(),req.getStartDate(),req.getEndDate());
				log.debug("teUserDefaultList is not empty " + teUserDefaultList.size());
				log.debug("tesUserList is not empty " + tesUserList.size());
				if (null != tesUserList && !tesUserList.isEmpty()) {
					
					for (TargetEntityUser teUser : tesUserList) {
						modelMapper.getConfiguration().setAmbiguityIgnored(true);
						TargetSettingRes tsRes = modelMapper.map(teUser, TargetSettingRes.class);
						tsRes = convertTargetStrToObj(teUser.getTargets(), tsRes);
						tsRes.setEmpName(getEmpName(tRole.getEmpId()));
						if(teUser.getUpdatedById()!=null) {
							tsRes.setUpdated_by_user_id(teUser.getUpdatedById());
							tsRes.setUpdatedUserName(dmsEmployeeRepo.getEmpName(String.valueOf(teUser.getUpdatedById())));
						}else {
							tsRes.setUpdated_by_user_id(0);
						}
						tsRes.setEmployeeId(tRole.getEmpId());
						tsRes.setId(teUser.getGeneratedId());
						tsRes.setTargetName(teUser.getTargetName());
						tsRes.setTargetType(teUser.getTargetType());
						if (null != teamLeadId) {
							tsRes.setTeamLead(getTeamLeadName(teamLeadId));
							tsRes.setTeamLeadId(teamLeadId);
						}
						if (null != managerId) {
							tsRes.setManager(getEmpName(managerId));
							tsRes.setManagerId(managerId);
						}
						if (null != branchMgrId) {
							tsRes.setBranchManagerId(branchMgrId);
							tsRes.setBranchmanger(getEmpName(branchMgrId));
						}
						if (null != generalMgrId) {
							tsRes.setGeneralManagerId(generalMgrId);
							tsRes.setGeneralManager(getEmpName(generalMgrId));
						}

						if (null != tRole.getLocationId()) {
							tsRes.setLocationName(getLocationName(tRole.getLocationId()));
						}
						if (null != tRole.getBranchId()) {
							tsRes.setBranchName(getBranchName(tRole.getBranchId()));
						}
						if (null != tRole.getDeptId()) {
							tsRes.setDepartmentName(getDeptName(tRole.getDeptId()));
						}
						if (null != tRole.getDesignationId()) {
							tsRes.setDesignationName(getDesignationName(tRole.getDesignationId()));
						}
						list.add(tsRes);
					}

				}

				if(null!=teUserDefaultList && teUserDefaultList.isEmpty()){
					log.debug("tesUserList is  empty ");
					modelMapper.getConfiguration().setAmbiguityIgnored(true);

					TargetSettingRes res = modelMapper.map(te, TargetSettingRes.class);
//					res.setStartDate(getFirstDayOfQurter());
//					res.setEndDate(getLastDayOfQurter());
					res.setStartDate(req.getStartDate());
					res.setEndDate(req.getEndDate());

					TargetEntityUser teUserToSave = modelMapper.map(res, TargetEntityUser.class);
					teUserToSave.setEmployeeId(tRole.getEmpId());

					teUserToSave.setTargets(te.getTargets());
				//	teUserToSave.setType("default");
					teUserToSave.setActive(GsAppConstants.ACTIVE);
					teUserToSave.setExperience(tRole.getExperience());
					teUserToSave.setTargetName("DEFAULT");
					teUserToSave.setTargetType(DynamicFormConstants.TARGET_MONTHLY_TYPE);
					Optional<TargetEntityUser> defaultTeUserOpt = targetUserRepo
							.checkDefaultDataInTargetUserwithDate(tRole.getEmpId(),req.getStartDate(),req.getEndDate());

					log.debug("defaultTeUserOpt:::"+defaultTeUserOpt);
					if (!defaultTeUserOpt.isPresent()) {
						log.debug("Default data is empty for " + tRole.getEmpId());
						TargetEntityUser dbRes = targetUserRepo.save(teUserToSave);
						res.setId(dbRes.getGeneratedId());
					}

					res = convertTargetStrToObjV3(te.getTargets(), res);
					res.setEmpName(getEmpName(tRole.getEmpId()));
					res.setEmployeeId(tRole.getEmpId());
					res.setTargetName("DEFAULT");
					res.setTargetType(DynamicFormConstants.TARGET_MONTHLY_TYPE);
					res.setExperience(tRole.getExperience() != null ? tRole.getExperience() : "");
					if (null != teamLeadId) {
						res.setTeamLead(getTeamLeadName(teamLeadId));
						res.setTeamLeadId(teamLeadId);
					}
					if (null != managerId) {
						res.setManager(getEmpName(managerId));
						res.setManagerId(managerId);
					}
					if (null != branchMgrId) {
						res.setBranchManagerId(branchMgrId);
						res.setBranchmanger(getEmpName(branchMgrId));
					}
					if (null != generalMgrId) {
						res.setGeneralManagerId(generalMgrId);
						res.setGeneralManager(getEmpName(generalMgrId));
					}

					if (null != tRole.getLocationId()) {
						res.setLocationName(getLocationName(tRole.getLocationId()));
					}
					if (null != tRole.getBranchId()) {
						res.setBranchName(getBranchName(tRole.getBranchId()));
					}
					if (null != tRole.getDeptId()) {
						res.setDepartmentName(getDeptName(tRole.getDeptId()));
					}
					if (null != tRole.getDesignationId()) {
						res.setDesignationName(getDesignationName(tRole.getDesignationId()));
					}
					log.debug("res::::::::::::"+res);
					list.add(res);

				}
				if(null!=teUserDefaultList && !teUserDefaultList.isEmpty()){
					log.debug("teUserDefaultList is not empty");
					for (TargetEntityUser teUser : teUserDefaultList) {
						modelMapper.getConfiguration().setAmbiguityIgnored(true);
						TargetSettingRes tsRes = modelMapper.map(teUser, TargetSettingRes.class);
						tsRes = convertTargetStrToObj(teUser.getTargets(), tsRes);
						tsRes.setEmpName(getEmpName(tRole.getEmpId()));
						if(teUser.getUpdatedById()!=null) {
							tsRes.setUpdated_by_user_id(teUser.getUpdatedById());
							tsRes.setUpdatedUserName(dmsEmployeeRepo.getEmpName(String.valueOf(teUser.getUpdatedById())));
						}else {
							tsRes.setUpdated_by_user_id(0);
						}
						tsRes.setEmployeeId(tRole.getEmpId());
						tsRes.setId(teUser.getGeneratedId());
						tsRes.setTargetName(teUser.getTargetName());
						tsRes.setTargetType(teUser.getTargetType());
						if (null != teamLeadId) {
							tsRes.setTeamLead(getTeamLeadName(teamLeadId));
							tsRes.setTeamLeadId(teamLeadId);
						}
						if (null != managerId) {
							tsRes.setManager(getEmpName(managerId));
							tsRes.setManagerId(managerId);
						}
						if (null != branchMgrId) {
							tsRes.setBranchManagerId(branchMgrId);
							tsRes.setBranchmanger(getEmpName(branchMgrId));
						}
						if (null != generalMgrId) {
							tsRes.setGeneralManagerId(generalMgrId);
							tsRes.setGeneralManager(getEmpName(generalMgrId));
						}

						if (null != tRole.getLocationId()) {
							tsRes.setLocationName(getLocationName(tRole.getLocationId()));
						}
						if (null != tRole.getBranchId()) {
							tsRes.setBranchName(getBranchName(tRole.getBranchId()));
						}
						if (null != tRole.getDeptId()) {
							tsRes.setDepartmentName(getDeptName(tRole.getDeptId()));
						}
						if (null != tRole.getDesignationId()) {
							tsRes.setDesignationName(getDesignationName(tRole.getDesignationId()));
						}
						list.add(tsRes);
					}

				}
			}
			log.debug("list in getTSDataForRoleV2  " + list);

		} catch (Exception e) {
			log.error("getTargetSettingData() ", e);
		}
		return list;
	}

	private List<TargetEntity> getTargetSettingMasterDataForGivenRole(TargetRoleRes tRole)
			throws DynamicFormsServiceException {
		log.debug("Inside getTargetSettingMasterDataForGivenRole,for tRole");
		log.debug("tRole:::" + tRole);
		List<TargetEntity> finalList = new ArrayList<>();
		////System.out.println("tRole.getDesignationId() " + tRole.getDesignationId());
		////System.out.println("tRole.getOrgId()::" + tRole.getOrgId());
		////System.out.println("tRole.getBranchId()::" + tRole.getBranchId());
		////System.out.println("tRole.getLocationId()::" + tRole.getLocationId());
		////System.out.println("tRole.getDeptId()::" + tRole.getDeptId());
		////System.out.println("tRole.getDesignationId()::" + tRole.getDesignationId());

		// List<TargetEntityUser> userTargetList =
		// targetUserRepo.getUserTargetData(tRole.getOrgId(),tRole.getDeptId(),tRole.getDesignationId(),tRole.getBranchId());
		// List<TargetEntity> dbList =
		// targetSettingRepo.getTargetmappingDataWithOutExpSalV2(tRole.getOrgId(),
		// tRole.getBranchId(), tRole.getLocationId(), tRole.getDeptId(),
		// tRole.getDesignationId());
		List<TargetEntity> dbList = targetSettingRepo.getTargetmappingDataWithOutExpSalV2(tRole.getOrgId(),
				tRole.getDeptId(), tRole.getDesignationId(), tRole.getBranchId());
		// tRole.getBranchId(), tRole.getLocationId(), tRole.getDeptId(),
		// tRole.getDesignationId());

		log.debug("dbList size::::::: :" + dbList.size());
		log.debug("dbList " + dbList);
		String salRange = tRole.getSalary();
		log.debug("salRange:::" + salRange);

		if (null != salRange) {
			// throw new DynamicFormsServiceException("Salary Details of Employees are
			// missing", HttpStatus.INTERNAL_SERVER_ERROR);
			salRange = StringUtils.replaceIgnoreCase(salRange, "k", "");
			salRange = salRange.trim();
			Integer sal = Integer.valueOf(salRange);
			log.debug("Sal range of emp " + tRole.getEmpId() + " is " + salRange);

			for (TargetEntity te : dbList) {
				if (null != te.getSalrayRange() && te.getSalrayRange().length() > 0 && null != te.getExperience()
						&& te.getExperience().length() > 0 && null != te.getMinSalary()
						&& te.getMinSalary().length() > 0 && null != te.getMaxSalary()
						&& te.getMaxSalary().length() > 0) {
					Integer minSal = Integer.valueOf(te.getMinSalary());
					Integer maxSal = Integer.valueOf(te.getMaxSalary());
					log.debug("minSal::" + minSal + " maxSal " + maxSal);
					if ((minSal <= sal) && (sal <= maxSal)) {
						finalList.add(te);
					}
				}
			}
		}
		if (finalList.isEmpty()) {

			log.debug("FinalList is empty,Fetching adming config from NO Sal & Exp");
			for (TargetEntity te : dbList) {
				if (null == te.getSalrayRange() || null == te.getExperience()) {
					finalList.add(te);
				}
			}
		}
		if (finalList.size() > 1) {
			finalList = finalList.subList(0, 1);
		}
		log.debug("finalList " + finalList);
		log.debug("finalList size " + finalList.size());
		return finalList;
	}

	public TargetSettingRes getTSDataForRoleWithTarget(TargetRoleRes tRole, String teamLeadId, String managerId,
			String branchMgrId, String generalMgrId, String retailTarget) {
		log.debug("Inside getTSDataForRole()");
		TargetSettingRes res = new TargetSettingRes();
		try {
			List<TargetEntity> dbList = targetSettingRepo.getTargetmappingDataWithRole(tRole.getOrgId(),
					tRole.getBranchId(), tRole.getDeptId(), tRole.getExperience(), tRole.getSalary());
			for (TargetEntity te : dbList) {
				res = modelMapper.map(te, TargetSettingRes.class);
				String target = te.getTargets();
				////System.out.println("target::" + target);
				res = convertTargetStrToObjV2(target, res, retailTarget);
				res.setEmpName(getEmpName(tRole.getEmpId()));
				res.setEmployeeId(tRole.getEmpId());
				if (null != teamLeadId) {
					res.setTeamLead(getTeamLeadName(teamLeadId));
					res.setTeamLeadId(teamLeadId);
				}
				if (null != managerId) {
					res.setManager(getEmpName(managerId));
					res.setManagerId(managerId);
				}
				if (null != branchMgrId) {
					res.setBranchManagerId(branchMgrId);
					res.setBranchmanger(getEmpName(branchMgrId));
				}
				if (null != generalMgrId) {
					res.setGeneralManagerId(generalMgrId);
					res.setGeneralManager(getEmpName(generalMgrId));
				}
				// res.setManager(getDropDownValueById(te.getManagerId(),"manager"));
				// res.setBranchmanger(getDropDownValueById(te.getBranchmangerId(),"branchmanager"));
				// res.setBranch(getDropDownValueById(te.getBranch(),"branch"));
				// res.setLocation(getDropDownValueById(te.getLocation(), "location"));
				res.setDepartment(getDropDownValueById(te.getDepartment(), "department"));
				res.setDesignation(getDropDownValueById(te.getDesignation(), "designation"));
				res.setExperience(getDropDownValueById(te.getExperience(), "experience"));
				res.setSalrayRange(getDropDownValueById(te.getSalrayRange(), "salaryrange"));

			}

		} catch (Exception e) {
			log.error("getTargetSettingData() ", e);
		}
		return res;
	}

	private TargetSettingRes convertTargetStrToObjV3(String json, TargetSettingRes res) {
		if (null != json && !json.isEmpty()) {
			JsonParser parser = new JsonParser();
			JsonArray arr = parser.parse(json).getAsJsonArray();
			for (JsonElement je : arr) {
				JsonObject obj = je.getAsJsonObject();
				String paramName = obj.get("parameter").getAsString();
				res = convertJsonToStrV3(res, paramName, obj);
			}
		}
		return res;
	}

	private TargetSettingRes convertTargetStrToObj(String json, TargetSettingRes res) {
		if (null != json && !json.isEmpty()) {
			JsonParser parser = new JsonParser();
			JsonArray arr = parser.parse(json).getAsJsonArray();
			for (JsonElement je : arr) {
				JsonObject obj = je.getAsJsonObject();
				String paramName = obj.get("parameter").getAsString();
				res = convertJsonToStr(res, paramName, obj);
			}
		}
		return res;
	}

	private TargetSettingRes convertTargetStrToObjV2(String json, TargetSettingRes res, String retailTarget) {
		if (null != json && !json.isEmpty()) {
			JsonParser parser = new JsonParser();
			JsonArray arr = parser.parse(json).getAsJsonArray();
			for (JsonElement je : arr) {
				JsonObject obj = je.getAsJsonObject();
				String paramName = obj.get("parameter").getAsString();
				res = convertJsonToStrV2(res, paramName, obj, retailTarget);
			}
		}
		return res;
	}

	public String getTeamLeadName(String id) {
		String res = null;
		log.info("TeamLead ID " + id);
		String empNameQuery = "SELECT emp_name FROM dms_employee where emp_id=<ID>;";
		try {
			Object obj = entityManager.createNativeQuery(empNameQuery.replaceAll("<ID>", id)).getSingleResult();
			res = (String) obj;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public String getEmpName(String id) {
		String res = null;
		String empNameQuery = "SELECT emp_name FROM dms_employee where emp_id=<ID>;";
		try {
			if (id != null && id.length() > 0) {
				// if (null != id || !id.equalsIgnoreCase("string") || id.length()>0){
				Object obj = entityManager.createNativeQuery(empNameQuery.replaceAll("<ID>", id)).getSingleResult();
				res = (String) obj;
			} else {
				res = "";
			}
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private String getBranchName(String branchId) {
		log.info("Inside getBranchName,Given Branch ID : " + branchId);
		String res = null;
		String deptQuery = "SELECT name FROM dms_branch where branch_id=<ID>;";
		try {
			if (null != branchId) {
				Object obj = entityManager.createNativeQuery(deptQuery.replaceAll("<ID>", branchId)).getSingleResult();
				res = (String) obj;
			} else {
				res = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private String getEmpSal(Integer eId) {
		log.info("Inside getEmpSal,Given Branch ID : " + eId);
		String res = null;
		try {
			if (null != eId) {
				final String dmsEmpByidQueryForDmsSalary = "SELECT * FROM dms_emp_sal_mapping where emp_id=<EMP_ID>";
				String tmpQuery = dmsEmpByidQueryForDmsSalary.replaceAll("<EMP_ID>", String.valueOf(eId));
				List<Object[]> data = entityManager.createNativeQuery(tmpQuery).getResultList();
				if(data.size()>0) {
				Object obj = entityManager.createNativeQuery(getSalForEmp.replaceAll("<ID>", String.valueOf(eId)))
						.getSingleResult();
				res = (String) obj;
				}else {
					res = "0";
				}
			} else {
				res = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private String getLocationName(String locationId) {
		String res = null;
		String deptQuery = "SELECT location_name FROM dms_location where id=<ID>;";
		try {
			Object obj = entityManager.createNativeQuery(deptQuery.replaceAll("<ID>", locationId)).getSingleResult();
			res = (String) obj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private String getDeptName(String deptId) {

		String res = null;
		String deptQuery = "SELECT hrms_department_id FROM dms_department where dms_department_id=<ID>;";
		try {
			Object obj = entityManager.createNativeQuery(deptQuery.replaceAll("<ID>", deptId)).getSingleResult();
			res = (String) obj;
			////System.out.println("Dept ID " + deptId + " is : " + res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private String calcualteExperience(String prevExperience, String joiningDate)
			throws ParseException, DynamicFormsServiceException {
		log.debug("Inside calcualteExperience ,prevExperience :" + prevExperience + " and joiningDate " + joiningDate);

		// if(prevExperience==null && joiningDate ==null) {
		// throw new DynamicFormsServiceException("Joining Date and Previous experience
		// is NULL for the given employee ", HttpStatus.INTERNAL_SERVER_ERROR);
		// }

		if (!StringUtils.isEmpty(prevExperience) && !StringUtils.isEmpty(joiningDate)) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			Date joining = dateFormat.parse(joiningDate);
			log.debug("todaysDate::" + date + " joining " + joining);
			int diff = getDiffYears(joining, date);
			log.info("Diff date" + diff);
			prevExperience = prevExperience.replaceAll("Y", "").trim();
			Integer totalExp = diff + Integer.valueOf(prevExperience);
			return totalExp + "Y";
		} else {
			return null;
		}
	}

	public static int getDiffYears(Date first, Date last) {
		Calendar a = getCalendar(first);
		Calendar b = getCalendar(last);
		////System.out.println("b.get(Calendar.YEAR) " + b.get(Calendar.YEAR) + ":::, " + a.get(Calendar.YEAR));
		int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
		if (a.get(Calendar.DAY_OF_YEAR) > b.get(Calendar.DAY_OF_YEAR)) {
			diff--;
		}

		return diff;
	}

	public static Calendar getCalendar(Date date) {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(date);
		return cal;
	}

	public String getDesignationName(String designationId) {
		String res = null;
		String designationQuery = "SELECT designation_name FROM dms_designation where dms_designation_id=<ID>";
		try {
			Object obj = entityManager.createNativeQuery(designationQuery.replaceAll("<ID>", designationId))
					.getSingleResult();
			res = (String) obj;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public TargetSettingRes updateTargetDataWithRole(TargetSettingRes req) {
		log.info("Inside updateTargetDataWithRole()");
		TargetSettingRes res = null;
		try {
			Optional<TargetEntity> teOpt = targetSettingRepo.findById(req.getId());
			if (teOpt.isPresent()) {
				TargetEntity te = teOpt.get();
				Map<String, String> unitsMap = getUnitsFromDbIfExists(te.getTargets());
				log.info("unitsMap::" + unitsMap);
				List<TargetParamReq> list = new ArrayList<>();
				for (String param : paramList) {
					String methodName = "get" + StringUtils.capitalize(param);
					Method getNameMethod = req.getClass().getMethod(methodName);
					String name = (String) getNameMethod.invoke(req); // explicit cast
					list.add(new TargetParamReq(param, name, unitsMap.get(param)));
				}
				TargetEntityUser teUser = modelMapper.map(te, TargetEntityUser.class);
				teUser.setTargets(new Gson().toJson(list));
				teUser.setEmployeeId(req.getEmployeeId());

				log.debug("Emp ID " + req.getEmployeeId() + " StartDate " + req.getStartDate() + " endData: "
						+ req.getEndDate());
				List<TargetEntityUser> targetList = targetUserRepo.findByEmpIdWithDate(req.getEmployeeId(),
						req.getStartDate(), req.getEndDate(), req.getTargetType(), req.getTargetName());
				if (!targetList.isEmpty()) {
					log.debug("Record present in user ts table");
					TargetEntityUser tes = targetList.get(0);
					teUser.setGeneratedId(tes.getGeneratedId());
				}

				modelMapper.getConfiguration().setAmbiguityIgnored(true);
				teUser.setTeamLeadId(req.getTeamLead());
				teUser.setManagerId(req.getManager());
				teUser.setBranch(req.getBranch());
				teUser.setBranchmangerId(req.getBranchmanger());
				teUser.setGeneralManager(req.getGeneralManagerId());
				teUser.setStartDate(req.getStartDate());
				teUser.setEndDate(req.getEndDate());

				res = modelMapper.map(targetUserRepo.save(teUser), TargetSettingRes.class);
				res.setId(teUser.getGeneratedId());
				res = convertTargetStrToObj(teUser.getTargets(), res);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	@Autowired
	DmsBranchDao dmsBranchDao;

	@Override
	public TargetSettingRes addTargetDataWithRole(TargetMappingAddReq req) throws DynamicFormsServiceException {
		log.info("Inside addTargetDataWithRole()");
		log.debug("Inside addTargetDataWithRole()");

		String targetType = req.getTargetType();
		String targetName = req.getTargetName();
		TargetSettingRes res = null;

		if (targetType.equalsIgnoreCase(DynamicFormConstants.TARGET_MONTHLY_TYPE)
				|| targetType.equalsIgnoreCase(DynamicFormConstants.TARGET_SPEICAL_TYPE)) {

			if (targetType.equalsIgnoreCase(DynamicFormConstants.TARGET_MONTHLY_TYPE)) {
				res = addTargetDataWithRoleToDB(req);
			} else if (targetType.equalsIgnoreCase(DynamicFormConstants.TARGET_SPEICAL_TYPE)) {
				res = addTargetDataWithRoleToDBSpecial(req);
			}

		} else {
			throw new DynamicFormsServiceException("Invalid Target Type", HttpStatus.BAD_REQUEST);
		}

		return res;

	}

	private TargetSettingRes addTargetDataWithRoleToDBSpecial(TargetMappingAddReq req)
			throws DynamicFormsServiceException {

		log.info("Inside addTargetDataWithRoleToDBSpecial()");
		TargetSettingRes res = null;

		try {
			String empId = req.getEmployeeId();
			log.debug("empId::" + empId);
			Integer retailTarget = parseRetailTarget(req);

			String finalEmpId = null;
			String managerId = req.getManagerId();
			String teamLeadId = req.getTeamLeadId();
			String generalManagerId = req.getGeneralManagerId();
			String branchManagerId = req.getBranchmangerId();

			if (empId != null && Optional.of(empId).isPresent()) {
				finalEmpId = empId;
			}

			else if (teamLeadId != null && Optional.of(teamLeadId).isPresent()) {
				finalEmpId = teamLeadId;
			} else if (managerId != null && Optional.of(managerId).isPresent()) {
				finalEmpId = managerId;

			} else if (generalManagerId != null && Optional.of(generalManagerId).isPresent()) {

				finalEmpId = generalManagerId;
			} else if (branchManagerId != null && Optional.of(branchManagerId).isPresent()) {

				finalEmpId = branchManagerId;
			}
			log.debug("finalEmpId:::" + finalEmpId);

			String tmpQuery = dmsEmpByidQuery.replaceAll("<EMP_ID>", String.valueOf(finalEmpId));
			tmpQuery = roleMapQuery.replaceAll("<EMP_ID>", String.valueOf(finalEmpId));
			log.debug("tmpQuery  " + tmpQuery);
			List<Object[]> data = entityManager.createNativeQuery(tmpQuery).getResultList();
			TargetRoleRes trRoot = new TargetRoleRes();
			for (Object[] arr : data) {
				trRoot.setOrgId(String.valueOf(arr[0]));
				trRoot.setBranchId(String.valueOf(arr[1]));
				trRoot.setEmpId(String.valueOf(arr[2]));
				trRoot.setRoleName(String.valueOf(arr[3]));
				trRoot.setRoleId(String.valueOf(arr[4]));
			}

			TargetSettingRes userDefaultTsRes = null;
			log.debug("addTargetDataWithRole::::" + finalEmpId);
			Optional<DmsEmployee> empOpt = dmsEmployeeRepo.findById(Integer.valueOf(finalEmpId));
			TargetRoleRes tRole = null;

			if (empOpt.isPresent()) {
				userDefaultTsRes = new TargetSettingRes();
				DmsEmployee emp = empOpt.get();
				tRole = buildTargetRoleRes(trRoot, emp);

			}
			Map<String, Object> adminTargetMap = getAdminTargetString(Integer.parseInt(finalEmpId));
			String adminTargets = (String) adminTargetMap.get("VAL");
			Integer adminId = (Integer) adminTargetMap.get("ID");
			log.debug("adminTargets :" + adminTargets);
			String calculatedTargetString = calculateTargets(adminTargets, retailTarget);
			log.debug("calculatedTargetString in add " + calculatedTargetString);

			if (null != calculatedTargetString) {
				TargetEntityUser teUser = new TargetEntityUser();
				teUser.setTargets(calculatedTargetString);
				teUser.setTargetAdminId(adminId);
				teUser.setOrgId(trRoot.getOrgId());

				teUser.setStartDate(req.getStartDate());
				teUser.setEndDate(req.getEndDate());

				teUser.setEmployeeId(finalEmpId);

				modelMapper.getConfiguration().setAmbiguityIgnored(true);
				teUser.setTeamLeadId(req.getTeamLeadId());
				teUser.setManagerId(req.getManagerId());

				String branchId = req.getBranch();
				log.debug("Input branch ID ,orgmapid " + branchId);
				if (null != branchId) {
					// DmsBranch branch =
					// dmsBranchDao.getBranchByOrgMpId(Integer.parseInt(branchId));
					DmsBranch branch = dmsBranchDao.findById(Integer.parseInt(branchId)).get();
					log.debug("branch:::" + branch);
					if (branch != null) {
						branchId = String.valueOf(branch.getBranchId());
					} else {

						throw new DynamicFormsServiceException("NO VALID BRANCH EXISTS IN DB",
								HttpStatus.INTERNAL_SERVER_ERROR);
					}
					log.debug("branchId::::" + branchId);
					teUser.setBranch(branchId);
				} else {
					throw new DynamicFormsServiceException("NO VALID BRANCH EXISTS IN DB",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

				teUser.setLocation(tRole.getLocationId());
				teUser.setDesignation(tRole.getDesignationId());
				teUser.setDepartment(tRole.getDeptId());
				teUser.setExperience(tRole.getExperience());
				teUser.setBranchmangerId(req.getBranchmangerId());
				teUser.setGeneralManager(getEmpName(req.getGeneralManagerId()));
				teUser.setStartDate(req.getStartDate());
				teUser.setEndDate(req.getEndDate());
				teUser.setRetailTarget(retailTarget);
				teUser.setType("");
				teUser.setTargetName(req.getTargetName());
				teUser.setTargetType(req.getTargetType());
				teUser = setTargetSettingUserHierarchy(teUser, req);
				List<TargetEntityUser> list = targetUserRepo.findAllQ3(finalEmpId);
				log.debug("Data list for emp id " + list.size());
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

				Date inputStartDate = dateFormat.parse(req.getStartDate());
				Date inputEndDate = dateFormat.parse(req.getEndDate());
				if (inputEndDate.before(inputStartDate)) {
					throw new DynamicFormsServiceException(
							"Date Validation Fails, please verify start date and end date",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

				log.debug("TARGET ROLE DATA DOESNOT EXISTS IN DB");
				teUser.setActive(GsAppConstants.ACTIVE);
				res = modelMapper.map(targetUserRepo.save(teUser), TargetSettingRes.class);

				res.setEmpName(getEmpName(res.getEmployeeId()));
				res.setTeamLead(getEmpName(res.getTeamLeadId()));
				res.setManager(getEmpName(res.getManagerId()));
				res.setBranchmanger(getEmpName(req.getBranchmangerId()));
				res.setGeneralManager(getEmpName(req.getGeneralManagerId()));
				res.setBranchManagerId(req.getBranchmangerId());
				// res.setLocation(tRole.getLocationId());
				res.setBranchName(getBranchName(res.getBranch()));
				res.setLocationName(getLocationName(trRoot.getLocationId()));
				res.setDepartmentName(getDeptName(res.getDepartment()));
				res.setDesignationName(getDesignationName(res.getDesignation()));
				res.setExperience(res.getExperience());
				res.setSalrayRange(res.getSalrayRange());
				res = convertTargetStrToObj(teUser.getTargets(), res);

			}

		} catch (DynamicFormsServiceException e) {
			log.error("saveTargetSettingData() ", e);
			throw e;

		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return res;

	}

	public TargetSettingRes addTargetDataWithRoleToDB(TargetMappingAddReq req) throws DynamicFormsServiceException {
		log.info("Inside addTargetDataWithRoleToDB()");
		TargetSettingRes res = null;

		try {
			String empId = req.getEmployeeId();
			log.debug("empId::" + empId);
			Integer retailTarget = parseRetailTarget(req);

			String finalEmpId = null;
			String managerId = req.getManagerId();
			String teamLeadId = req.getTeamLeadId();
			String generalManagerId = req.getGeneralManagerId();
			String branchManagerId = req.getBranchmangerId();

			if (empId != null && Optional.of(empId).isPresent()) {
				finalEmpId = empId;
			}

			else if (teamLeadId != null && Optional.of(teamLeadId).isPresent()) {
				finalEmpId = teamLeadId;
			} else if (managerId != null && Optional.of(managerId).isPresent()) {
				finalEmpId = managerId;

			} else if (generalManagerId != null && Optional.of(generalManagerId).isPresent()) {

				finalEmpId = generalManagerId;
			} else if (branchManagerId != null && Optional.of(branchManagerId).isPresent()) {

				finalEmpId = branchManagerId;
			}
			log.debug("finalEmpId:::" + finalEmpId);

			String tmpQuery = dmsEmpByidQuery.replaceAll("<EMP_ID>", String.valueOf(finalEmpId));
			tmpQuery = roleMapQuery.replaceAll("<EMP_ID>", String.valueOf(finalEmpId));
			log.debug("tmpQuery  " + tmpQuery);
			List<Object[]> data = entityManager.createNativeQuery(tmpQuery).getResultList();
			TargetRoleRes trRoot = new TargetRoleRes();
			for (Object[] arr : data) {
				trRoot.setOrgId(String.valueOf(arr[0]));
				trRoot.setBranchId(String.valueOf(arr[1]));
				trRoot.setEmpId(String.valueOf(arr[2]));
				trRoot.setRoleName(String.valueOf(arr[3]));
				trRoot.setRoleId(String.valueOf(arr[4]));
			}

			TargetSettingRes userDefaultTsRes = null;
			log.debug("addTargetDataWithRole::::" + finalEmpId);
			Optional<DmsEmployee> empOpt = dmsEmployeeRepo.findById(Integer.valueOf(finalEmpId));
			TargetRoleRes tRole = null;

			if (empOpt.isPresent()) {
				userDefaultTsRes = new TargetSettingRes();
				DmsEmployee emp = empOpt.get();
				tRole = buildTargetRoleRes(trRoot, emp);

			}
			Map<String, Object> adminTargetMap = getAdminTargetString(Integer.parseInt(finalEmpId));
			String adminTargets = (String) adminTargetMap.get("VAL");
			Integer adminId = (Integer) adminTargetMap.get("ID");
			log.debug("adminTargets :" + adminTargets);
			String calculatedTargetString = calculateTargets(adminTargets, retailTarget);
			log.debug("calculatedTargetString in add " + calculatedTargetString);

			if (null != calculatedTargetString) {
				TargetEntityUser teUser = new TargetEntityUser();
				teUser.setTargets(calculatedTargetString);
				teUser.setOrgId(trRoot.getOrgId());
				teUser.setTargetAdminId(adminId);
				teUser.setStartDate(req.getStartDate());
				teUser.setEndDate(req.getEndDate());

				teUser.setEmployeeId(finalEmpId);

				modelMapper.getConfiguration().setAmbiguityIgnored(true);
				teUser.setTeamLeadId(req.getTeamLeadId());
				teUser.setManagerId(req.getManagerId());

				String branchId = req.getBranch();
				log.debug("Input branch ID ,orgmapid " + branchId);
				if (null != branchId) {
					// DmsBranch branch =
					// dmsBranchDao.getBranchByOrgMpId(Integer.parseInt(branchId));
					DmsBranch branch = dmsBranchDao.findById(Integer.parseInt(branchId)).get();
					log.debug("branch:::" + branch);
					if (branch != null) {
						branchId = String.valueOf(branch.getBranchId());
					} else {

						throw new DynamicFormsServiceException("NO VALID BRANCH EXISTS IN DB",
								HttpStatus.INTERNAL_SERVER_ERROR);
					}
					log.debug("branchId::::" + branchId);
					teUser.setBranch(branchId);
				} else {
					throw new DynamicFormsServiceException("NO VALID BRANCH EXISTS IN DB",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

				teUser.setLocation(tRole.getLocationId());
				teUser.setDesignation(tRole.getDesignationId());
				teUser.setDepartment(tRole.getDeptId());
				teUser.setExperience(tRole.getExperience());
				teUser.setBranchmangerId(req.getBranchmangerId());
				teUser.setGeneralManager(getEmpName(req.getGeneralManagerId()));
				teUser.setStartDate(req.getStartDate());
				teUser.setEndDate(req.getEndDate());
				teUser.setRetailTarget(retailTarget);
				teUser.setType("");
				teUser.setTargetName(req.getTargetName());
				teUser.setTargetType(req.getTargetType());

				teUser = setTargetSettingUserHierarchy(teUser, req);
				List<TargetEntityUser> list = targetUserRepo.findAllQ3(finalEmpId);
				log.debug("Data list for emp id " + list.size());
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

				Date inputStartDate = dateFormat.parse(req.getStartDate());
				Date inputEndDate = dateFormat.parse(req.getEndDate());
				if (inputEndDate.before(inputStartDate)) {
					throw new DynamicFormsServiceException(
							"Date Validation Fails, please verify start date and end date",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
				boolean startFlag = false;
				boolean endFlag = false;
				log.debug("Inputstartdate " + inputStartDate + ",inputenddate:" + inputEndDate);
				boolean insertFlag = false;
				for (TargetEntityUser te : list) {

					Date dbStartDate = dateFormat.parse(te.getStartDate());
					Date dbEndDate = dateFormat.parse(te.getEndDate());
					log.debug("dbStartDate::" + dbStartDate + " ,dbEndDate " + dbEndDate);

					startFlag = dateoverlapvalidation(inputStartDate, dbStartDate, dbEndDate);
					endFlag = dateoverlapvalidation(inputEndDate, dbStartDate, dbEndDate);

					if (startFlag && endFlag) {
						insertFlag = true;
						break;
					}

					// log.debug("startFlag "+startFlag+", endFlag:"+endFlag);

				}

				log.debug("startFlag:: " + startFlag + ", endFlag:" + endFlag);
				log.debug("insertFlag ::" + insertFlag);
				// if (!validateTargetMappingRole(teUser)) {
				if (!insertFlag) {
					log.debug("TARGET ROLE DATA DOESNOT EXISTS IN DB");
					teUser.setActive(GsAppConstants.ACTIVE);
					res = modelMapper.map(targetUserRepo.save(teUser), TargetSettingRes.class);

					res.setEmpName(getEmpName(res.getEmployeeId()));
					res.setTeamLead(getEmpName(res.getTeamLeadId()));
					res.setManager(getEmpName(res.getManagerId()));
					res.setBranchmanger(getEmpName(req.getBranchmangerId()));
					res.setGeneralManager(getEmpName(req.getGeneralManagerId()));
					res.setBranchManagerId(req.getBranchmangerId());
					// res.setLocation(tRole.getLocationId());
					res.setBranchName(getBranchName(res.getBranch()));
					res.setLocationName(getLocationName(trRoot.getLocationId()));
					res.setDepartmentName(getDeptName(res.getDepartment()));
					res.setDesignationName(getDesignationName(res.getDesignation()));
					res.setExperience(res.getExperience());
					res.setSalrayRange(res.getSalrayRange());
					res = convertTargetStrToObj(teUser.getTargets(), res);
				}

				else {
					throw new DynamicFormsServiceException("TARGET ROLE DATA  EXISTS IN DB",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}

		} catch (DynamicFormsServiceException e) {
			log.error("saveTargetSettingData() ", e);
			throw e;

		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return res;
	}

	private TargetEntityUser setTargetSettingUserHierarchy(TargetEntityUser teUser, TargetMappingAddReq req) {
		Map<String, String> userHiearchyMap = new HashMap<>();
		userHiearchyMap.put("Level1", req.getGeneralManagerId() != null ? req.getGeneralManagerId() : "");
		userHiearchyMap.put("Level2", req.getBranchmangerId() != null ? req.getBranchmangerId() : "");
		userHiearchyMap.put("Level3", req.getManagerId() != null ? req.getManagerId() : "");
		userHiearchyMap.put("Level4", req.getTeamLeadId() != null ? req.getTeamLeadId() : "");
		userHiearchyMap.put("Level5", req.getEmployeeId() != null ? req.getEmployeeId() : "");

		String userHiearchyStr = new Gson().toJson(userHiearchyMap);
		log.debug("userHiearchyStr::" + userHiearchyStr);
		teUser.setUserHierarchy(userHiearchyStr);
		return teUser;
	}

	public static boolean dateoverlapvalidation(Date date, Date dateStart, Date dateEnd) {
		if (date != null && dateStart != null && dateEnd != null) {
			if ((date.after(dateStart) && date.before(dateEnd)) || date.equals(dateStart) || date.equals(dateEnd)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	private boolean validateTargetMappingRole(TargetEntityUser teUser) {

		List<TargetEntityUser> list = targetUserRepo.findAllQ1(teUser.getEmployeeId(), teUser.getStartDate(),
				teUser.getEndDate());
		if (list != null && !list.isEmpty()) {
			return true;
		}
		return false;
	}

	private Map<String, String> getUnitsFromDbIfExists(String targets) {
		Map<String, String> map = new HashMap<>();
		JsonParser parser = new JsonParser();
		JsonArray arr = parser.parse(targets).getAsJsonArray();
		for (JsonElement je : arr) {
			JsonObject obj = je.getAsJsonObject();
			String paramName = obj.get("parameter").getAsString();
			for (String param : paramList) {
				if (null != paramName && paramName.equalsIgnoreCase(param)) {
					if (obj.has("unit")) {
						map.put(param, obj.get("unit").getAsString());
					}
				}
			}
		}
		return map;
	}

	@Override
	public List<TargetDropDown> getTargetDropdownV2(String orgId, String branchId, String parent, String child,
			String parentId) {
		List<TargetDropDown> list = new ArrayList<>();
		if ((null != parent && parent.equalsIgnoreCase("location"))
				&& (null != child && child.equalsIgnoreCase("branchmanager")) && (null != parentId)) {

			String branchMgrStr = StringUtils.join(branchMgrDesignationList, "\", \"");// Join with ", "
			branchMgrStr = StringUtils.wrap(branchMgrStr, "\"");// Wrap step1 with "

			log.debug("branchMgrStr ::" + branchMgrStr);
			String query = "SELECT emp_id,emp_name FROM dms_employee where location = " + parentId
					+ " and emp_id in \r\n"
					+ "(select rolemap.emp_id FROM dms_employee_role_mapping rolemap where rolemap.role_id in (\r\n"
					+ "SELECT role_id FROM dms_role where role_name in (" + branchMgrStr + ") and org_id=" + orgId
					+ " and branch_id=" + branchId + "));";

			list = buildDropDown(query);
		} else if ((null != parent && parent.equalsIgnoreCase("organization"))
				&& (null != child && child.equalsIgnoreCase("branch")) && (null != parentId)) {
			list = buildDropDown("SELECT branch_id,name FROM dms_branch where organization_id=" + orgId + ";");
		}

		/*
		 * else if ((null != parent && parent.equalsIgnoreCase("branch")) && (null !=
		 * child && child.equalsIgnoreCase("location")) && (null != parentId)) { list =
		 * buildDropDown("SELECT id,location_name FROM dms_location where branch_id=" +
		 * parentId + ";"); }
		 */

		else if ((null != parent && parent.equalsIgnoreCase("branch"))
				&& (null != child && child.equalsIgnoreCase("department")) && (null != parentId)) {
			list = buildDropDown("select dms_department_id,department_name from dms_department where branch_id = "
					+ parentId + " and org_id=" + orgId);

		} else if ((null != parent && parent.equalsIgnoreCase("department"))
				&& (null != child && child.equalsIgnoreCase("designation")) && (null != parentId)) {
			list = buildDropDown(
					"select dms_designation_id,designation_name from dms_designation where dms_designation_id in \r\n"
							+ "(select designation_id from dms_dept_designation_mapping where dept_id = " + parentId
							+ ");");
		} else if ((null != parent && parent.equalsIgnoreCase("designation"))
				&& (null != child && child.equalsIgnoreCase("experience")) && (null != parentId)) {
			list = buildDropDown(
					"select dms_designation_id,designation_name from dms_designation where dms_designation_id in \r\n"
							+ "(select designation_id from dms_dept_designation_mapping where dept_id = " + parentId
							+ ");");
		} else {
			list = buildDropDown(getReportingEmp.replaceAll("<ID>", parentId));

		}
		return list;
	}

	private List<TargetDropDown> buildDropDown(String query) {
		List<TargetDropDown> list = new ArrayList<>();
		List<Object[]> data = entityManager.createNativeQuery(query).getResultList();

		for (Object[] arr : data) {
			TargetDropDown trRoot = new TargetDropDown();
			trRoot.setId(String.valueOf(arr[0]));
			trRoot.setValue(String.valueOf(arr[1]));
			list.add(trRoot);
		}
		return list;
	}

	@Override
	public String deleteTSData(String recordId, String empId) {
		String res = null;
		Optional<TargetEntityUser> opt = targetUserRepo.findByEmpIdWithRecordId(recordId, empId);
		if (opt.isPresent()) {
			TargetEntityUser te = opt.get();
			te.setActive(GsAppConstants.INACTIVE);
			targetUserRepo.save(te);
			res = "{\"SUCCESS\": \"Deleted Succesfully\"}";
		} else {
			res = "{\"INVALID_REQUEST\": \"No data found in DB\"}";
		}
		return res;
	}

	public String getFirstDayOfQurter() {
		// LocalDate localDate = LocalDate.now();
		// LocalDate firstDayOfQuarter =
		// localDate.with(localDate.getMonth().firstMonthOfQuarter()).with(TemporalAdjusters.firstDayOfMonth());
		return LocalDate.ofEpochDay(System.currentTimeMillis() / (24 * 60 * 60 * 1000)).withDayOfMonth(1).toString();
	}

	public String getLastDayOfQurter() {
		/*
		 * LocalDate localDate = LocalDate.now(); LocalDate firstDayOfQuarter =
		 * localDate.with(localDate.getMonth().firstMonthOfQuarter())
		 * .with(TemporalAdjusters.firstDayOfMonth());
		 * 
		 * LocalDate lastDayOfQuarter = firstDayOfQuarter.plusMonths(2)
		 * .with(TemporalAdjusters.lastDayOfMonth());
		 */
		return LocalDate.ofEpochDay(System.currentTimeMillis() / (24 * 60 * 60 * 1000)).plusMonths(1).withDayOfMonth(1)
				.minusDays(1).toString();
	}

	@Override
	public TargetSettingRes editTargetDataWithRoleV2(TargetMappingAddReq req) throws DynamicFormsServiceException {
		log.info("Inside editTargetDataWithRoleV2()");
		TargetSettingRes res = null;
		try {
			Integer retailTarget = parseRetailTarget(req);
			String finalEmpId = null;

			String empId = req.getEmployeeId();
			String managerId = req.getManagerId();
			String teamLeadId = req.getTeamLeadId();
			String generalManagerId = req.getGeneralManagerId();

			if (empId != null && Optional.of(empId).isPresent()) {
				finalEmpId = empId;
			}

			else if (teamLeadId != null && Optional.of(teamLeadId).isPresent()) {
				finalEmpId = teamLeadId;
			} else if (managerId != null && Optional.of(managerId).isPresent()) {
				finalEmpId = managerId;
			} else if (generalManagerId != null && Optional.of(generalManagerId).isPresent()) {
				finalEmpId = generalManagerId;
			}
			Optional<TargetEntityUser> targetEntityUser = targetUserRepo.findByEmpIdWithRecordId(req.getRecordId(),empId);
			List<TargetEntityUser> targetEntityUserList = new ArrayList<>();
			if(targetEntityUser.isPresent() && targetEntityUser.get()!=null){
				targetEntityUserList.add(targetEntityUser.get());
			}
			/*
			 * else if(Optional.of(empId).isEmpty() && Optional.of(teamLeadId).isEmpty() &&
			 * Optional.of(managerId).isPresent()) { finalEmpId=managerId; }
			 */

			log.debug("targetName:::: "+req.getTargetName());
			log.debug("finalEmpId::" + finalEmpId+", startDate:"+req.getStartDate()+" End Dte "+req.getEndDate()+" ,TargetType "+req.getTargetType());
			log.debug("Targe name "+req.getTargetName());
	//		List<TargetEntityUser> targetEntityUserList = targetUserRepo.findByEmpIdWithDate(finalEmpId,
	//				req.getStartDate(), req.getEndDate(), req.getTargetType(), req.getTargetName());

//			Optional<TargetEntityUser> targetEntityUser = targetUserRepo.findByEmpIdWithRecordId(req.getRecordId(),empId);

	//		List<TargetEntityUser> targetEntityUserList = new ArrayList<>();
			if(targetEntityUser.isPresent() && targetEntityUser.get()!=null){
				targetEntityUserList.add(targetEntityUser.get());
			}
			log.debug("targetEntityUserList:::::"+targetEntityUserList);
			Map<String, Object> adminTargetMap = getAdminTargetString(Integer.parseInt(finalEmpId));
			String adminTargets = (String) adminTargetMap.get("VAL");
			Integer adminId = (Integer) adminTargetMap.get("ID");
			log.debug("adminTargets :" + adminTargets);
			if (!targetEntityUserList.isEmpty()) {
				for (TargetEntityUser te : targetEntityUserList) {
					try {
						String target = calculateTargets(adminTargets, retailTarget);
						te.setTargets(target);

						if(req.getLoggedInEmpId()!=null){
							te.setUpdatedById(Integer.parseInt(req.getLoggedInEmpId()));
						}
						te.setTargetAdminId(adminId);
						te.setActive("Y");
//						if(te.getType().equalsIgnoreCase("default")) {
//							te.setType("default");
//						}else {
//						te.setType("");
//						}
						log.debug("Setting type :::::::;");
						te.setTargetName(req.getTargetName());
						te.setTargetType(req.getTargetType());
						modelMapper.getConfiguration().setAmbiguityIgnored(true);
						te.setTeamLeadId(req.getTeamLeadId());
						if (req.getManagerId() != null) {
							te.setManagerId(req.getManagerId());
						}
						te.setBranch(req.getBranch());
						if (req.getBranchmangerId() != null) {
							te.setBranchmangerId(req.getBranchmangerId());
						}
						if (req.getGeneralManagerId() != null) {
							te.setGeneralManager(getEmpName(req.getGeneralManagerId()));
						}
						te.setStartDate(req.getStartDate());
						te.setEndDate(req.getEndDate());
						te.setRetailTarget(retailTarget);

						TargetRoleRes role = getEmpRoleDataV3(Integer.parseInt(finalEmpId));
						te.setOrgId(role.getOrgId());

						log.debug("TE before sving "+te);
						res = modelMapper.map(targetUserRepo.save(te), TargetSettingRes.class);

						res.setEmpName(getEmpName(res.getEmployeeId()));
						res.setTeamLead(getEmpName(res.getTeamLeadId()));
						res.setManager(getEmpName(res.getManagerId()));
						res.setBranchmanger(getEmpName(req.getBranchmangerId()));
						res.setGeneralManager(getEmpName(req.getGeneralManagerId()));

						if(req.getLoggedInEmpId()!=null){
							res.setUpdated_by_user_id(Integer.parseInt(req.getLoggedInEmpId()));
						}

						res.setBranchManagerId(req.getBranchmangerId());
						// res.setLocation(req.getLocation());
						res = convertTargetStrToObj(te.getTargets(), res);
					} catch (Exception e) {
						e.printStackTrace();
						log.error("exception ", e);
					}
				}
			}

		} catch (DynamicFormsServiceException e) {
			log.error("saveTargetSettingData() ", e);
			throw e;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private Integer parseRetailTarget(TargetMappingAddReq req) throws DynamicFormsServiceException {
		Integer retailTarget = 0;
		if (null != req.getRetailTarget()) {
			retailTarget = Integer.parseInt(req.getRetailTarget());
		} else {
			throw new DynamicFormsServiceException("Retail Target is missing", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.debug("retailTarget to EDIT Is :" + retailTarget);
		return retailTarget;
	}

	private Map<String, Object> getAdminTargetString(Integer empId)
			throws ParseException, DynamicFormsServiceException {
		Map<String, Object> map = new HashMap<>();
		String adminTargets = null;
		log.debug(dmsEmpByidQuery);
		// TargetRoleRes trRoot = getEmpRoleData(empId);
		TargetRoleRes trRoot = getEmpRoleDataV3(empId);
		TargetRoleRes tRole = new TargetRoleRes();

		log.debug("TARGET ROLE " + trRoot);
		List<String> orgMapBranchList = trRoot.getOrgMapBranches();
		Optional<DmsEmployee> empOpt = dmsEmployeeRepo.findById(empId);
		if (empOpt.isPresent()) {
			DmsEmployee emp = empOpt.get();
			tRole = buildTargetRoleRes(trRoot, emp);

		}
		if (orgMapBranchList != null) {
			tRole.setBranchId(orgMapBranchList.get(0));
		}

		log.debug("tRole in getAdminTargets:::" + tRole);

		for (TargetEntity te : getTargetSettingMasterDataForGivenRole(tRole)) {
			map.put("ID", te.getId());
			map.put("VAL", te.getTargets());
		}
		return map;
	}

	private String calculateTargets(String adminTargets, Integer retailTarget)
			throws JsonMappingException, JsonProcessingException, DynamicFormsServiceException {
		TargetParamReq[] paramArr = null;
		log.debug("adminTargets::" + adminTargets);

		if (null != adminTargets) {
			paramArr = objectMapper.readValue(adminTargets, TargetParamReq[].class);
			String enquiry = null;
			for (TargetParamReq param : paramArr) {

				if (param.getParameter().equalsIgnoreCase("enquiry")) {
					enquiry = calculateEnquiry(retailTarget, param.getTarget(), param.getUnit());
					param.setTarget(enquiry);
				}

			}
			log.debug("paramArr::" + paramArr);
			for (TargetParamReq param : paramArr) {
				log.debug("param:::" + param.getParameter());
				if (param.getParameter().equalsIgnoreCase("testDrive")
						|| param.getParameter().equalsIgnoreCase("Test drive")) {
					param.setTarget(calculateBooking(enquiry, param.getTarget(), param.getUnit()));
				}
				if (param.getParameter().equalsIgnoreCase("homeVisit")
						|| param.getParameter().equalsIgnoreCase("Home Visit")) {
					param.setTarget(calculateBooking(enquiry, param.getTarget(), param.getUnit()));
				}
				if (param.getParameter().equalsIgnoreCase("booking")) {
					param.setTarget(calculateBooking(enquiry, param.getTarget(), param.getUnit()));
				}
				if (param.getParameter().equalsIgnoreCase("exchange")) {
					param.setTarget(calculateEnquiry(retailTarget, param.getTarget(), param.getUnit()));
				}
				if (param.getParameter().equalsIgnoreCase("finance")) {
					param.setTarget(calculateEnquiry(retailTarget, param.getTarget(), param.getUnit()));
				}
				if (param.getParameter().equalsIgnoreCase("insurance")) {
					param.setTarget(calculateEnquiry(retailTarget, param.getTarget(), param.getUnit()));
				}
				if (param.getParameter().equalsIgnoreCase("exWarranty")) {
					param.setTarget(calculateEnquiry(retailTarget, param.getTarget(), param.getUnit()));
				}
				if (param.getParameter().equalsIgnoreCase("accessories")) {
					param.setTarget(calculateAccessories(retailTarget, param.getTarget(), param.getUnit()));
				}
				if (param.getParameter().equalsIgnoreCase("events")) {
					param.setTarget(calculateEnquiry(retailTarget, param.getTarget(), param.getUnit()));
				}
			}
		} else {
			throw new DynamicFormsServiceException("Target Admin data with for Given user does not exists in DB",
					HttpStatus.BAD_REQUEST);
		}
		return new Gson().toJson(paramArr);
	}

	private String calculateHomeVisit(String enquiry, String target, String unit) {
		if (null != enquiry && unit.equalsIgnoreCase(PERCENTAGE)) {
			Integer t = Integer.parseInt(target);
			Integer enq = Integer.parseInt(enquiry);
			Double perc = 0D;
			if (t > 0) {
				perc = (double) enq * (t / 100);
			}
			log.debug("Calculated TestDrive for ENQUIRY " + enquiry + " and HOMEVISIT is  " + perc);
			return String.format("%.1f", perc);
		} else {
			return target;
		}
	}

	private String calculateAccessories(Integer retailTarget, String target, String unit) {
		if (null != retailTarget) {
			Integer t = Integer.parseInt(target);
			Integer perc = t * retailTarget;
			return String.valueOf(perc);
		} else {
			return target;
		}
	}

	private String calculateTestDrive(String enquiry, String target, String unit) {
		if (null != enquiry && unit.equalsIgnoreCase(PERCENTAGE)) {
			Integer t = Integer.parseInt(target);
			Integer enq = Integer.parseInt(enquiry);
			Integer perc = 0;
			if (t > 0) {
				perc = enq * (t / 100);
			}
			log.debug("Calculated TestDrive for ENQUIRY " + enquiry + " and Testdrive is  " + perc);
			return String.format("%.0f", perc);
		} else {
			return target;
		}
	}

	private String calculateEnquiry(Integer retailTarget, String target, String unit) {
		if (unit.equalsIgnoreCase(PERCENTAGE)) {
			Double t = Double.valueOf(target);
			Double perc = 0D;
			////System.out.println("t " + t + " retailTarget " + retailTarget);
			if (t > 0) {
				Double p = t / 100;
				////System.out.println(" P :::" + p);
				perc = (t / 100) * retailTarget;
			}
			log.debug("Calculated Enquiry for target " + target + " and retailTarget " + retailTarget + " is " + perc);
			return String.format("%.0f", perc);
		} else {
			return target;
		}
	}

	private String calculateBooking(String enqTarget, String bookingTarget, String unit) {
		log.debug("callingg calculateBooking");
		;
		if (unit.equalsIgnoreCase(PERCENTAGE) && bookingTarget != null && enqTarget != null) {
			Double t = Double.valueOf(bookingTarget);
			Double e = Double.valueOf(enqTarget);
			Double perc = 0D;
			// ////System.out.println("t " + t + " retailTarget " + retailTarget);
			if (t > 0) {
				Double p = t / 100;
				////System.out.println(" P :::" + p);
				perc = (t / 100) * e;
			}
			log.debug("Calculated Enquiry for target " + bookingTarget + " and enqTarget " + enqTarget + " is " + perc);
			return String.format("%.0f", perc);
		} else {
			return bookingTarget;
		}
	}

	@Override
	public List<TargetSettingRes> searchTargetMappingData(TargetSearch request) {
		log.debug("Inside searchTargetMappingData()");
		List<TargetSettingRes> list = new ArrayList<>();
		try {
			String empId = null;
			if (null != request.getEmpId()) {
				empId = request.getEmpId();
			} else if (null == request.getEmpId() && null != request.getEmpName()) {
				empId = getEmpIdByName(request.getEmpName());
			}

			String startDate = request.getStartDate();
			String endDate = request.getEndDate();

			log.info("empId " + empId + " starDate " + startDate + " endDate " + endDate);
			List<TargetEntityUser> teUserDbList = new ArrayList<>();
			if (null != empId && null != startDate && null != endDate) {
				teUserDbList = targetUserRepo.findAllQ1(empId, startDate, endDate);
			}
			if (null != empId && null != startDate && null == endDate) {
				teUserDbList = targetUserRepo.findAllQ2(empId, startDate);
			}
			if (null != empId && null == startDate && null == endDate) {
				teUserDbList = targetUserRepo.findAllQ3(empId);
			}

			for (TargetEntityUser te : teUserDbList) {
				modelMapper.getConfiguration().setAmbiguityIgnored(true);
				TargetSettingRes tsres = modelMapper.map(te, TargetSettingRes.class);
				tsres.setId(te.getGeneratedId());
				tsres = convertTargetStrToObj(te.getTargets(), tsres);

				if (null != request.getTeamLeadId()) {
					tsres.setTeamLead(getTeamLeadName(request.getTeamLeadId()));
					tsres.setTeamLeadId(request.getTeamLeadId());
				}
				if (null != request.getManagerId()) {
					tsres.setManager(getEmpName(request.getManagerId()));
					tsres.setManagerId(request.getManagerId());
				}
				if (null != request.getBranchmangerId()) {
					tsres.setBranchManagerId(request.getBranchmangerId());
					tsres.setBranchmanger(getEmpName(request.getBranchmangerId()));
				}
				if (null != request.getGeneralManagerId()) {
					tsres.setGeneralManagerId(request.getGeneralManagerId());
					tsres.setGeneralManager(getEmpName(request.getGeneralManagerId()));
				}
				list.add(tsres);
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error("searchTargetMappingData() ", e);

		}
		return list;
	}

	private String getEmpIdByName(String id) {
		String res = null;
		String empNameQuery = "SELECT emp_id FROM dms_employee where emp_name=<ID>;";
		try {
			if (null != id && !id.equalsIgnoreCase("string")) {
				Object obj = entityManager.createNativeQuery(empNameQuery.replaceAll("<ID>", id)).getSingleResult();
				res = (String) obj;
			} else {
				res = "";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public boolean validateTL(String roleName) {
		boolean flag = false;
		if (tlDesignationList.contains(roleName)) {
			flag = true;
		}
		return flag;
	}

	public boolean validateDSE(String roleName) {
		boolean flag = false;
		log.debug("dseDesignationList " + dseDesignationList);
		if (dseDesignationList.contains(roleName) || roleName.contains("Sales Consultant")
				|| roleName.contains("sales consultant")) {
			flag = true;
		}
		return flag;
	}

	public boolean validateMgr(String roleName) {
		boolean flag = false;
		if (mgrDesignationList.contains(roleName)) {
			flag = true;
		}
		return flag;
	}

	public boolean validateBranchMgr(String roleName) {
		boolean flag = false;
		if (branchMgrDesignationList.contains(roleName)) {
			flag = true;
		}
		return flag;
	}

	public boolean validateGeneralMgr(String roleName) {
		boolean flag = false;
		if (GMDesignationList.contains(roleName)) {
			flag = true;
		}
		return flag;
	}

	@Autowired
	OHServiceImpl ohServiceImpl;

	@Override
	public Map<String, String> getEmployeeRole(Integer empId) throws DynamicFormsServiceException {
		// TargetRoleRes res = getEmpRoleData(empId);
		Map<String, String> map = new HashMap<>();

		Optional<DmsEmployee> empOpt = dmsEmployeeRepo.findEmpById(empId);
		if (empOpt.isPresent()) {
			DmsEmployee emp = empOpt.get();
			Integer empDesId = Integer.parseInt(emp.getDesignationId());
			log.debug("empDesigntaion:::" + empDesId);
			Optional<DmsDesignation> desOpt = dmsDesignationRepo.findById(empDesId);
			Integer empLevel = 0;
			if (desOpt.isPresent()) {
				empLevel = desOpt.get().getLevel();
			}

			log.debug("Given emp level is " + empLevel);
			map.put("role", ohServiceImpl.getLevelName(empLevel));

		} else {
			map.put("role", "");
		}

		return map;
	}

	public String getEmployeeRoleV2(Integer empId) {
		TargetRoleRes res = null;
		try {
			res = getEmpRoleData(empId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getFormattedRole(res.getRoleName());
	}

	private String getFormattedRole(String roleName) {
		log.debug("input role " + roleName);
		if (validateDSE(roleName))
			return "DSE";
		else if (validateTL(roleName))
			return "TL";
		else if (validateMgr(roleName))
			return "Manager";
		else if (validateBranchMgr(roleName))
			return "Dept Heads";
		else if (validateGeneralMgr(roleName))
			return "President , Vice President";
		else
			return roleName;

	}

	@Override
	public String deleteAdminTargetMapping(Integer recordId) {
		String res = null;
		Optional<TargetEntity> opt = targetSettingRepo.findById(recordId);
		if (opt.isPresent()) {
			TargetEntity te = opt.get();
			te.setActive(GsAppConstants.INACTIVE);
			targetSettingRepo.save(te);
			res = "{\"SUCCESS\": \"Deleted Succesfully\"}";
		} else {
			res = "{\"INVALID_REQUEST\": \"No data found in DB\"}";
		}
		return res;
	}

	@Override
	public TargetSettingRes updateTargetSettingDataV2(TSAdminUpdateReq request) throws DynamicFormsServiceException {

		log.debug("Inside updateTargetSettingDataV2()");
		TargetEntity dbRes = null;
		TargetSettingRes ts = null;
		try {
			TargetEntity te = new TargetEntity();
			te.setBranch(request.getBranch());
			// te.setLocation(request.getLocation());
			te.setDepartment(request.getDepartment());
			te.setDesignation(request.getDesignation());
			te.setExperience(request.getExperience());
			te.setSalrayRange(request.getSalrayRange());
			te.setOrgId(request.getOrgId());
			String salRange = request.getSalrayRange();

			String minSal = "";
			String maxSal = "";
			if (null != salRange) {
				if (salRange.contains("-")) {
					String tmp[] = salRange.split("-");
					minSal = tmp[0];
					minSal = StringUtils.replaceIgnoreCase(minSal, "k", "").trim();
					maxSal = tmp[1];
					maxSal = StringUtils.replaceIgnoreCase(maxSal, "k", "").trim();

				} else {
					minSal = salRange;
					minSal = StringUtils.replaceIgnoreCase(minSal, "k", "").trim();
				}
			}
			te.setMaxSalary(maxSal);
			te.setMinSalary(minSal);
			te.setActive("Y");

			List<Target> list = request.getTargets();
			log.debug("Before Targets " + list);
			// list = updatedTargetValues(list);
			// log.debug("After updating Targets "+list);
			if (null != list) {
				te.setTargets(gson.toJson(list));
			}
			if (validateTargetAdminData(te)) {
				log.debug("TARGET ADMIN DATA EXISTS IN DB");

				int recordId = request.getId();
				Optional<TargetEntity> dbRecordOpt = targetSettingRepo.findById(recordId);

				if (dbRecordOpt.isPresent()) {

					TargetEntity dbRecord = dbRecordOpt.get();
					te.setId(dbRecord.getId());
					dbRes = targetSettingRepo.save(te);

					if (dbRes != null) {
						String targets = dbRes.getTargets();

						ts = modelMapper.map(dbRes, TargetSettingRes.class);
						ts = convertTargetStrToObj(targets, ts);
						ts.setBranchName(getBranchName(te.getBranch()));
						// ts.setLocationName(getLocationName(te.getLocation()));
						ts.setDepartmentName(getDeptName(te.getDepartment()));
						ts.setDesignationName(getDesignationName(te.getDesignation()));
						ts.setExperience(te.getExperience());
						ts.setSalrayRange(te.getSalrayRange());

						// Update target setting user data

						updateTargetSettingUserData(dbRes);
					}

				}

				else {
					throw new DynamicFormsServiceException("Target Admin data with Given ID does not exists in DB",
							HttpStatus.BAD_REQUEST);
				}

			} else {
				throw new DynamicFormsServiceException("TARGET ADMIN DOES NOT DATA  EXISTS IN DB",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} catch (DynamicFormsServiceException e) {
			log.error("saveTargetSettingData() ", e);
			throw e;

		} catch (Exception e) {
			log.error("saveTargetSettingData() ", e);
			throw new DynamicFormsServiceException(env.getProperty("INTERNAL_SERVER_ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return ts;

	}

	private void updateTargetSettingUserData(TargetEntity dbRes) {
		try {
			int adminId = dbRes.getId();
			String adminTargets = dbRes.getTargets();
			log.debug("updateTargetSettingUserData ,target admin id " + adminId + ",adminTargets ::" + adminTargets);
			List<TargetEntityUser> targetEntityUserList = targetUserRepo.findAllByTargetAdminId(adminId);
			if (null != targetEntityUserList && !targetEntityUserList.isEmpty()) {
				if (!targetEntityUserList.isEmpty()) {
					for (TargetEntityUser te : targetEntityUserList) {
						Integer retailTarget = te.getRetailTarget() != null ? te.getRetailTarget() : 1;
						te.setTargets(calculateTargets(adminTargets, retailTarget));
						targetUserRepo.save(te);
						log.debug("Updated Target setting data for user " + te.getEmployeeId()
								+ " on updation of Target Admin data with Id " + adminId);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception in updateTargetSettingUserData ", e);
		}

	}

	@Override
	public TSAdminUpdateReq getTargetSettingAdminById(int id) throws DynamicFormsServiceException {
		TargetEntity dbRecord = null;
		TSAdminUpdateReq res = null;
		try {

			Optional<TargetEntity> dbRecordOpt = targetSettingRepo.findById(id);

			if (dbRecordOpt.isPresent()) {
				dbRecord = dbRecordOpt.get();
				res = modelMapper.map(dbRecord, TSAdminUpdateReq.class);
				TargetParamReq[] params = objectMapper.readValue(dbRecord.getTargets(), TargetParamReq[].class);
				List list = Arrays.asList(params);
				res.setTargets(list);
			} else {
				throw new DynamicFormsServiceException("Target Admin data with Given data does not exists in DB",
						HttpStatus.BAD_REQUEST);
			}
		} catch (DynamicFormsServiceException e) {
			log.error("getTargetSettingAdminById() ", e);
			throw e;

		} catch (Exception e) {
			log.error("getTargetSettingAdminById() ", e);
			throw new DynamicFormsServiceException(env.getProperty("INTERNAL_SERVER_ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return res;
	}

	public String getOrgName(String id) {
		String res = null;
		String designationQuery = "select name from dms_organization where org_id=<ID>";
		try {
			if (null != id) {
				Object obj = entityManager.createNativeQuery(designationQuery.replaceAll("<ID>", id)).getSingleResult();
				res = (String) obj;
			} else {
				res = "";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public TargetRoleRes getEmpRoleDataV2(int empId) throws DynamicFormsServiceException {

		String tmpQuery = dmsEmpByidQuery.replaceAll("<EMP_ID>", String.valueOf(empId));

		tmpQuery = roleMapQuery.replaceAll("<EMP_ID>", String.valueOf(empId));
		List<Object[]> data = entityManager.createNativeQuery(tmpQuery).getResultList();
		TargetRoleRes trRoot = new TargetRoleRes();
		for (Object[] arr : data) {
			trRoot.setOrgId(String.valueOf(arr[0]));
			trRoot.setRoleName(String.valueOf(arr[3]));
			trRoot.setRoleId(String.valueOf(arr[4]));
			trRoot.setBranchId(String.valueOf(arr[1]));
		}

		return trRoot;
	}
	////Immediate Hierarchy
	public TargetRoleRes getEmpRoleDataV2ImmediateHirarchy(List<Integer> empId) throws DynamicFormsServiceException {
		
		//List<Integer> empId1 = new ArrayList<Integer>();
		String empId1 = empId.toString();
		
		String tmpQuery1 = dmsEmpByidQueryimmediate.replaceAll("<EMP_ID>", String.valueOf(empId1));

		tmpQuery1 = roleMapQueryimmediate.replaceAll("EMP_ID", String.valueOf(empId1));
		String ss =tmpQuery1.replaceAll("\\[", "(").replaceAll("\\]", ")");
		
		//tmpQuery = dmsEmployeeRepo.dmsEmpimmediateByidQuery(empId);
		//tmpQuery = dmsEmployeeRoleMappingRepo.roleMapQueryimmediate(empId);
		//String tmpQuery1 = tmpQuery.toString();
		List<Object[]> data = entityManager.createNativeQuery(ss).getResultList();
		TargetRoleRes trRoot = new TargetRoleRes();
		for (Object[] arr : data) {
			trRoot.setOrgId(String.valueOf(arr[0]));
			trRoot.setRoleName(String.valueOf(arr[3]));
			trRoot.setRoleId(String.valueOf(arr[4]));
			trRoot.setBranchId(String.valueOf(arr[1]));
		}

		return trRoot;
	}

	private List<TargetEntity> getUserTargetData(TargetRoleRes tRole) throws DynamicFormsServiceException {
		log.debug("Inside getTargetSettingMasterDataForGivenRole,for tRole");
		log.debug("tRole:::" + tRole);
		List<TargetEntity> finalList = new ArrayList<>();
		////System.out.println("tRole.getDesignationId() " + tRole.getDesignationId());
		////System.out.println("tRole.getOrgId()::" + tRole.getOrgId());
		////System.out.println("tRole.getBranchId()::" + tRole.getBranchId());
		////System.out.println("tRole.getLocationId()::" + tRole.getLocationId());
		////System.out.println("tRole.getDeptId()::" + tRole.getDeptId());
		////System.out.println("tRole.getDesignationId()::" + tRole.getDesignationId());

		List<TargetEntityUser> userTargetList = targetUserRepo.getUserTargetData(tRole.getOrgId(), tRole.getDeptId(),
				tRole.getDesignationId(), tRole.getBranchId());
		// List<TargetEntity> dbList =
		// targetSettingRepo.getTargetmappingDataWithOutExpSalV2(tRole.getOrgId(),
		// tRole.getBranchId(), tRole.getLocationId(), tRole.getDeptId(),
		// tRole.getDesignationId());
		List<TargetEntity> dbList = targetSettingRepo.getTargetmappingDataWithOutExpSalV2(tRole.getOrgId(),
				tRole.getDeptId(), tRole.getDesignationId(), tRole.getBranchId());
		// tRole.getBranchId(), tRole.getLocationId(), tRole.getDeptId(),
		// tRole.getDesignationId());

		log.debug("dbList size::::::: :" + dbList.size());
		log.debug("dbList " + dbList);
		String salRange = tRole.getSalary();
		if (null != salRange) {
			// throw new DynamicFormsServiceException("Salary Details of Employees are
			// missing", HttpStatus.INTERNAL_SERVER_ERROR);
			salRange = StringUtils.replaceIgnoreCase(salRange, "k", "");
			salRange = salRange.trim();
			Integer sal = Integer.valueOf(salRange);
			log.info("Sal range of emp " + tRole.getEmpId() + " is " + salRange);

			for (TargetEntity te : dbList) {
				if (null != te.getSalrayRange() && null != te.getExperience() && null != te.getMinSalary()
						&& null != te.getMaxSalary()) {
					Integer minSal = Integer.valueOf(te.getMinSalary());
					Integer maxSal = Integer.valueOf(te.getMaxSalary());
					log.info("minSal::" + minSal + " maxSal " + maxSal);
					if ((minSal <= sal) && (sal <= maxSal)) {
						finalList.add(te);
					}
				}
			}
		}
		if (finalList.isEmpty()) {

			log.debug("FinalList is empty,Fetching adming config from NO Sal & Exp");
			for (TargetEntity te : dbList) {
				if (null == te.getSalrayRange() || null == te.getExperience()) {
					finalList.add(te);
				}
			}
		}
		if (finalList.size() > 1) {
			finalList = finalList.subList(0, 1);
		}
		log.debug("finalList " + finalList);
		log.debug("finalList size " + finalList.size());
		return finalList;
	}

	public List<TargetSettingRes> getTSDataForRoleV3(Integer empId) throws DynamicFormsServiceException {
		log.debug("Inside getTSDataForRoleV3()");
		List<TargetSettingRes> list = new ArrayList<>();
		TargetRoleRes tRole = getEmpRoleDataV3(empId);
		try {
			log.debug("tRole.getOrgMapBranches()::" + tRole.getOrgMapBranches());

			for (String orgMapBranchID : tRole.getOrgMapBranches()) {
				log.debug("orgMapBranchID::" + orgMapBranchID);

				log.debug("tRole.getDesignationId() " + tRole.getDesignationId());
				log.debug("tRole.getOrgId()::" + tRole.getOrgId());
				log.debug("tRole.getBranchId()::" + tRole.getBranchId());
				log.debug("tRole.getLocationId()::" + tRole.getLocationId());

				List<TargetEntityUser> userTargetList = targetUserRepo.getUserTargetDataV2(tRole.getOrgId(),
						tRole.getDeptId(), tRole.getDesignationId(), orgMapBranchID, String.valueOf(empId));
				tRole.setBranchId(orgMapBranchID);
				tRole.setLocationId(orgMapBranchID);
				log.info("userTargetListis not empty " + userTargetList.size());
				for (TargetEntityUser teUser : userTargetList) {
					log.debug("TargetEntityUser:::" + teUser);
					log.debug("user targets " + teUser.getTargets());
					modelMapper.getConfiguration().setAmbiguityIgnored(true);
					TargetSettingRes tsRes = modelMapper.map(teUser, TargetSettingRes.class);
					log.debug("tsRes:::" + tsRes);
					tsRes = convertTargetStrToObj(teUser.getTargets(), tsRes);
					tsRes.setEmpName(getEmpName(tRole.getEmpId()));
					tsRes.setEmployeeId(tRole.getEmpId());
					tsRes.setId(teUser.getGeneratedId());

					if (null != tRole.getLocationId()) {
						tsRes.setLocationName(getLocationName(tRole.getLocationId()));
					}
					if (null != tRole.getBranchId()) {
						tsRes.setBranchName(getBranchName(tRole.getBranchId()));
					}
					if (null != tRole.getDeptId()) {
						tsRes.setDepartmentName(getDeptName(tRole.getDeptId()));
					}
					if (null != tRole.getDesignationId()) {
						tsRes.setDesignationName(getDesignationName(tRole.getDesignationId()));
					}
					list.add(tsRes);
				}

			}

		} catch (Exception e) {
			log.error("getTargetSettingData() ", e);
		}
		return list;
	}
	
	///immidiate hirar
	
	public List<TargetSettingRes> getTSDataForRoleForEmps(List<Integer> empId) throws DynamicFormsServiceException {
		log.debug("Inside getTSDataForRoleV3()");
		List<TargetSettingRes> list = new ArrayList<>();
		TargetRoleRes tRole = getEmpRoleDataV3Emps(empId);
		try {
			log.debug("tRole.getOrgMapBranches()::" + tRole.getOrgMapBranches());

			for (String orgMapBranchID : tRole.getOrgMapBranches()) {
				log.debug("orgMapBranchID::" + orgMapBranchID);

				log.debug("tRole.getDesignationId() " + tRole.getDesignationId());
				log.debug("tRole.getOrgId()::" + tRole.getOrgId());
				log.debug("tRole.getBranchId()::" + tRole.getBranchId());
				log.debug("tRole.getLocationId()::" + tRole.getLocationId());

				List<TargetEntityUser> userTargetList = targetUserRepo.getUserTargetDataV2(tRole.getOrgId(),
						tRole.getDeptId(), tRole.getDesignationId(), orgMapBranchID, String.valueOf(empId));
				tRole.setBranchId(orgMapBranchID);
				tRole.setLocationId(orgMapBranchID);
				log.info("userTargetListis not empty " + userTargetList.size());
				for (TargetEntityUser teUser : userTargetList) {
					log.debug("TargetEntityUser:::" + teUser);
					log.debug("user targets " + teUser.getTargets());
					modelMapper.getConfiguration().setAmbiguityIgnored(true);
					TargetSettingRes tsRes = modelMapper.map(teUser, TargetSettingRes.class);
					log.debug("tsRes:::" + tsRes);
					tsRes = convertTargetStrToObj(teUser.getTargets(), tsRes);
					tsRes.setEmpName(getEmpName(tRole.getEmpId()));
					tsRes.setEmployeeId(tRole.getEmpId());
					tsRes.setId(teUser.getGeneratedId());

					if (null != tRole.getLocationId()) {
						tsRes.setLocationName(getLocationName(tRole.getLocationId()));
					}
					if (null != tRole.getBranchId()) {
						tsRes.setBranchName(getBranchName(tRole.getBranchId()));
					}
					if (null != tRole.getDeptId()) {
						tsRes.setDepartmentName(getDeptName(tRole.getDeptId()));
					}
					if (null != tRole.getDesignationId()) {
						tsRes.setDesignationName(getDesignationName(tRole.getDesignationId()));
					}
					list.add(tsRes);
				}

			}

		} catch (Exception e) {
			log.error("getTargetSettingData() ", e);
		}
		return list;
	}
	/*
	 * public List<TargetSettingRes> getTSDataForRoleV3(Integer empId) throws
	 * DynamicFormsServiceException { log.debug("Inside getTSDataForRoleV2()");
	 * TargetRoleRes tRole = getEmpRoleDataV3(empId); List<TargetSettingRes> list =
	 * new ArrayList<>(); try {
	 * 
	 * for(String orgMapBranchID : tRole.getOrgMapBranches()) {
	 * tRole.setBranchId(orgMapBranchID); tRole.setLocationId(orgMapBranchID);
	 * log.debug("trole :::"+tRole.toString());
	 * list.addAll(getTSDataForRoleV2(tRole,null,null,null,null)); /* for
	 * (TargetEntity te : getTargetSettingMasterDataForGivenRole(tRole)) {
	 * List<TargetEntityUser> tesUserList =
	 * targetUserRepo.findAllEmpIds(tRole.getEmpId()); if (null != tesUserList &&
	 * !tesUserList.isEmpty()) { log.info("tesUserList is not empty " +
	 * tesUserList.size()); for (TargetEntityUser teUser : tesUserList) {
	 * modelMapper.getConfiguration().setAmbiguityIgnored(true); TargetSettingRes
	 * tsRes = modelMapper.map(teUser, TargetSettingRes.class); tsRes =
	 * convertTargetStrToObj(teUser.getTargets(), tsRes);
	 * tsRes.setEmpName(getEmpName(String.valueOf(empId)));
	 * tsRes.setEmployeeId(String.valueOf(empId));
	 * tsRes.setId(teUser.getGeneratedId());
	 * 
	 * list.add(tsRes); }
	 * 
	 * }
	 * 
	 * } }
	 * 
	 * ////System.out.println("list in getTSDataForRoleV2  " + list);
	 * 
	 * } catch (Exception e) { log.error("getTargetSettingData() ", e); } return
	 * list;
	 * 
	 * }
	 */

	public TargetRoleRes getEmpRoleDataV3(int empId) throws DynamicFormsServiceException {

		String tmpQuery = dmsEmpByidQuery.replaceAll("<EMP_ID>", String.valueOf(empId));

		tmpQuery = roleMapQuery.replaceAll("<EMP_ID>", String.valueOf(empId));
		List<Object[]> data = entityManager.createNativeQuery(tmpQuery).getResultList();
		TargetRoleRes trRoot = new TargetRoleRes();
		for (Object[] arr : data) {
			trRoot.setOrgId(String.valueOf(arr[0]));
			trRoot.setBranchId(String.valueOf(arr[1]));
			trRoot.setEmpId(String.valueOf(arr[2]));
			trRoot.setRoleName(String.valueOf(arr[3]));
			trRoot.setRoleId(String.valueOf(arr[4]));
			trRoot.setPrecedence(Integer.parseInt(arr[5].toString()));
		}

		String branchQuery = "select branch_id,name from dms_branch where org_map_id in  (select location_node_data_id from emp_location_mapping where emp_id="
				+ empId + ");";

		// String branchQuery = "select location_node_data_id,org_id from
		// emp_location_mapping where emp_id="+empId;
		List<Object[]> branchdata = entityManager.createNativeQuery(branchQuery).getResultList();
		List<String> orgMapBranchIds = new ArrayList<>();

		for (Object[] arr : branchdata) {

			orgMapBranchIds.add(String.valueOf(arr[0]));
		}
		log.debug("orgMapBranchIds::" + orgMapBranchIds);
		trRoot.setOrgMapBranches(orgMapBranchIds);

		Optional<DmsEmployee> empOpt = dmsEmployeeRepo.findById(empId);
		DmsEmployee emp = null;
		if (empOpt.isPresent()) {
			emp = empOpt.get();
			trRoot = buildTargetRoleRes(trRoot, emp);
		} else {
			throw new DynamicFormsServiceException("No Empoloyee with given empId in DB", HttpStatus.BAD_REQUEST);

		}
		log.debug("trRoot " + trRoot);

		return trRoot;
	}
	
	
///immidiate hirrar	
	public TargetRoleRes getEmpRoleDataV3Emps(List<Integer> empId) throws DynamicFormsServiceException {

		String tmpQuery = dmsEmpimmediateByidQuery.replaceAll("<EMP_ID>", String.valueOf(empId));

		tmpQuery = roleMapQueryimmediate.replaceAll("EMP_ID", String.valueOf(empId));
		String ss =tmpQuery.replaceAll("\\[", "(").replaceAll("\\]", ")");
		List<Object[]> data = entityManager.createNativeQuery(ss).getResultList();
		TargetRoleRes trRoot = new TargetRoleRes();
		for (Object[] arr : data) {
			trRoot.setOrgId(String.valueOf(arr[0]));
			trRoot.setBranchId(String.valueOf(arr[1]));
			trRoot.setEmpId(String.valueOf(arr[2]));
			trRoot.setRoleName(String.valueOf(arr[3]));
			trRoot.setRoleId(String.valueOf(arr[4]));
			trRoot.setPrecedence(Integer.parseInt(arr[5].toString()));
		}

		String branchQuery = "select branch_id,name from dms_branch where org_map_id in  (select location_node_data_id from emp_location_mapping where emp_id in "
				+ empId + ");";
		String sss = branchQuery.replaceAll("\\[", "(").replaceAll("\\]", ")");
		// String branchQuery = "select location_node_data_id,org_id from
		// emp_location_mapping where emp_id="+empId;
		List<Object[]> branchdata = entityManager.createNativeQuery(sss).getResultList();
		List<String> orgMapBranchIds = new ArrayList<>();

		for (Object[] arr : branchdata) {

			orgMapBranchIds.add(String.valueOf(arr[0]));
		}
		log.debug("orgMapBranchIds::" + orgMapBranchIds);
		trRoot.setOrgMapBranches(orgMapBranchIds);

		List<DmsEmployee> empOpt = dmsEmployeeRepo.findByImmediateId(empId);
		List<DmsEmployee> emp = null;
		if (!empOpt.isEmpty()) {
			trRoot = buildTargetRoleResemps(trRoot, emp);
		} else {
			throw new DynamicFormsServiceException("No Empoloyee with given empId in DB", HttpStatus.BAD_REQUEST);

		}
		log.debug("trRoot " + trRoot);

		return trRoot;
	}

	private TargetSettingRes convertJsonToStrV4(TargetSettingRes res, String paramName, JsonObject obj) {
		if (null != paramName && paramName.equalsIgnoreCase("retailTarget")) {
			if (obj.has("target"))
				res.setRetailTarget((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
			;
		}
		if (null != paramName && paramName.equalsIgnoreCase("enquiry")) {
			if (obj.has("target"))
				res.setEnquiry((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("testDrive")) {
			if (obj.has("target"))
				res.setTestDrive((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("homeVisit")) {
			if (obj.has("target"))
				res.setHomeVisit((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}

		if (null != paramName && paramName.equalsIgnoreCase("videoConference")) {
			if (obj.has("target"))
				res.setVideoConference((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}

		if (null != paramName && paramName.equalsIgnoreCase("booking")) {
			if (obj.has("target"))
				res.setBooking((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("exchange")) {
			if (obj.has("target"))
				res.setExchange((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("finance")) {
			if (obj.has("target"))
				res.setFinance((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("insurance")) {
			if (obj.has("target"))
				res.setInsurance((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("exWarranty")) {
			if (obj.has("target"))
				res.setExWarranty((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("accessories")) {
			if (obj.has("target"))
				res.setAccessories((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("events")) {
			if (obj.has("target"))
				res.setEvents((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("other")) {
			if (obj.has("target"))
				res.setOther((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}
		if (null != paramName && paramName.equalsIgnoreCase("enquiry")) {
			if (obj.has("target"))
				res.setEnquiry((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}

		if (null != paramName && paramName.equalsIgnoreCase(INVOICE)) {
			if (obj.has("target"))
				res.setInvoice((obj.has("unit") && obj.get("unit").getAsString().equalsIgnoreCase("percentage"))
						? obj.get("target").getAsString() + "%"
						: obj.get("target").getAsString());
		}

		return res;
	}

	@Override
	public Map<String, String> verifyTargetSettingData(TargetSettingReq request) throws DynamicFormsServiceException {

		log.debug("Inside verifyTargetSettingData()");
		TargetSettingRes ts = null;
		Map<String, String> map = new HashMap<>();
		try {

			TargetEntity te = new TargetEntity();
			te.setBranch(request.getBranch());
			te.setDepartment(request.getDepartment());
			te.setDesignation(request.getDesignation());
			te.setOrgId(request.getOrgId());
			te.setExperience(request.getExperience());
			List<Target> list = request.getTargets();
			te.setSalrayRange(null);
			te.setExperience(null);
			if (null != list) {
				te.setTargets(gson.toJson(list));
			}
			if (validateTargetAdminData(te)) {
				log.debug("TARGET ADMIN DATA  EXISTS IN DB");
				map.put("msg", "TARGET ADMIN DATA  EXISTS IN DB");
				throw new DynamicFormsServiceException("TARGET ADMIN DATA  EXISTS IN DB",
						HttpStatus.INTERNAL_SERVER_ERROR);

			} else {
				map.put("msg", "TARGET ADMIN DATA DOES NOT EXISTS IN DB");
			}

		} catch (DynamicFormsServiceException e) {
			log.error("verifyTargetSettingData() ", e);
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (Exception e) {
			log.error("verifyTargetSettingData() ", e);
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return map;

	}

}
