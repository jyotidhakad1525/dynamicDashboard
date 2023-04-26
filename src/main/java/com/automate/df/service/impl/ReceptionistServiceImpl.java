package com.automate.df.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.automate.df.dao.EmpRepo;
import com.automate.df.dao.LeadStageRefDao;
import com.automate.df.dao.SourceAndIddao;
import com.automate.df.dao.dashboard.DmsLeadDao;
import com.automate.df.dao.dashboard.DmsLeadDropDao;
import com.automate.df.dao.dashboard.DmsWfTaskDao;
import com.automate.df.dao.dashboard.SubSourceRepo;
import com.automate.df.dao.oh.DmsBranchDao;
import com.automate.df.dao.oh.EmpLocationMappingDao;
import com.automate.df.dao.oh.LocationNodeDataDao;
import com.automate.df.dao.oh.LocationNodeDefDao;
import com.automate.df.dao.salesgap.DmsEmployeeRepo;
import com.automate.df.dao.salesgap.TargetSettingRepo;
import com.automate.df.dao.salesgap.TargetUserRepo;
import com.automate.df.entity.LeadStageRefEntity;
import com.automate.df.entity.SubSourceId;
import com.automate.df.entity.dashboard.DmsLead;
import com.automate.df.entity.dashboard.DmsWFTask;
import com.automate.df.entity.oh.DmsBranch;
import com.automate.df.entity.oh.LocationNodeData;
import com.automate.df.entity.oh.LocationNodeDef;
import com.automate.df.entity.sales.employee.DMSEmployee;
import com.automate.df.entity.salesgap.DmsEmployee;
import com.automate.df.exception.DynamicFormsServiceException;
import com.automate.df.model.df.dashboard.ReceptionistDashBoardReq;
import com.automate.df.model.df.dashboard.ReceptionistLeadRes;
import com.automate.df.model.df.dashboard.SourceRes;
import com.automate.df.model.df.dashboard.VehicleModelRes;
import com.automate.df.model.oh.LocationNodeDataV2;
import com.automate.df.service.ReceptionistService;
import com.automate.df.util.DashBoardUtil;
import com.automate.df.util.ObjectMapperUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author jignesh
 *
 */
@Slf4j
@Service
public class ReceptionistServiceImpl implements ReceptionistService {

	@Autowired
	Environment env;

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
	SalesGapServiceImpl salesGapServiceImpl;

	@Autowired
	DmsEmployeeRepo dmsEmployeeRepo;

	@Autowired
	DashBoardUtil dashBoardUtil;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	DmsLeadDao dmsLeadDao;

	@Autowired
	DmsWfTaskDao dmsWfTaskDao;

	@Autowired
	DmsLeadDropDao dmsLeadDropDao;

	@Autowired
	LocationNodeDataDao locationNodeDataDao;

	@Autowired
	LeadStageRefDao leadStageRefDao;

	@Autowired
	SourceAndIddao repository;

	@Autowired
	DmsBranchDao dmsBranchDao;

	@Autowired
	LocationNodeDefDao locationNodeDefDao;

	public static final String ENQUIRY = "Enquiry";
	public static final String DROPPED = "DROPPED";
	public static final String TEST_DRIVE = "Test Drive";
	public static final String HOME_VISIT = "Home Visit";
	public static final String FINANCE = "Finance";
	public static final String INSURANCE = "Insurance";
	public static final String VIDEO_CONFERENCE = "Video Conference";
	public static final String PROCEED_TO_BOOKING = "Proceed to Booking";
	public static final String BOOKING_FOLLOWUP_DSE = "Booking Follow Up - DSE";
	public static final String BOOKING_FOLLOWUP_CRM = "Booking Follow Up - CRM";
	public static final String BOOKING_FOLLOWUP_ACCCESSORIES = "Booking Follow Up - Accessories";
	public static final String VERIFY_EXCHANGE_APPROVAL = "Verify Exchange Approval";

	public static final String READY_FOR_INVOICE = "Ready for invoice - Accounts";
	public static final String PROCEED_TO_INVOICE = "Proceed to Invoice";
	public static final String INVOICE_FOLLOWUP_DSE = "Invoice Follow Up - DSE";
	public static final String INVOICE = "INVOICE";

	public static final String PRE_BOOKING = "Pre Booking";
	public static final String DELIVERY = "Delivery";

	public static final String BOOKING = "Booking";
	public static final String EXCHANGE = "Exchange";
	public static final String ACCCESSORIES = "Accessories";
	public static final String EVENTS = "Events";

	public String getStartDate(String inputStartDate) {
		if (null == inputStartDate && null == inputStartDate) {
			return getFirstDayOfMonth();
		} else {
			return inputStartDate;
		}

	}

	public String getEndDate(String inputEndDate) {
		if (null == inputEndDate && null == inputEndDate) {
			return getLastDayOfMonth();
		} else {
			return inputEndDate;
		}
	}

	public String getFirstDayOfMonth() {
		return LocalDate.ofEpochDay(System.currentTimeMillis() / (24 * 60 * 60 * 1000)).withDayOfMonth(1).toString()
				+ " 00:00:00";
	}

	public String getLastDayOfMonth() {
		return LocalDate.ofEpochDay(System.currentTimeMillis() / (24 * 60 * 60 * 1000)).plusMonths(1).withDayOfMonth(1)
				.minusDays(1).toString() + " 23:59:59";
	}

	public Map getReceptionistLiveLeadData(ReceptionistDashBoardReq req) {

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		String loginEmpName = dmsEmployeeObj.getEmpName();

		Map map = new HashMap();
		String startDate = getStartDate(req.getStartDate()) + " 00:00:00";
		String endDate = getEndDate(req.getEndDate()) + " 23:59:59";
		int orgId = req.getOrgId();
		List<Integer> dealerCode = null;
		try {
			dealerCode = dmsBranchDao.getDealerCodes(req.getBranchList());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		List<String> dmsEmployeeList = new ArrayList<>();
		if (!dealerCode.isEmpty()) {
			dmsEmployeeList = dmsEmployeeRepo.findEmpNames(orgId, dealerCode);
		} else {
			dmsEmployeeList = dmsEmployeeRepo.findEmpNames(orgId);
		}

		List consultantList = new ArrayList();
		for (String dmsEmployee : dmsEmployeeList) {
			Map m = new HashMap();

			int count = dmsLeadDao.getLiveCount(orgId, dmsEmployee, loginEmpName);

			if (count > 0) {
				List<Integer> userId = dmsEmployeeRepo.findEmpIdsByNames(dmsEmployee, orgId);
				List<DMSEmployee> userData = empRepo.findEmpByNames(dmsEmployee, orgId);
				m.put("emp_id", userId.get(0));
				m.put("branch", userData.get(0).getDmsBranch().getName());
				m.put("emp_name", dmsEmployee);
				m.put("allocatedCount", count);
				m.put("roleName", userData.get(0).getDmsRole().getRoleName());
				m.put("droppedCount", dmsLeadDao.getLiveDropCount(dmsEmployee, orgId, loginEmpName));
				m.put("contactCount", dmsLeadDao.getPreEnqLiveCount(orgId, dmsEmployee, loginEmpName));
				m.put("enquiryCount", dmsLeadDao.getEnqLiveCount(orgId, dmsEmployee, loginEmpName));
				m.put("retailCount", dmsLeadDao.getReatilLiveCount(orgId, dmsEmployee, loginEmpName));
				m.put("bookingCount", dmsLeadDao.getBkgLiveCount(orgId, dmsEmployee, loginEmpName));
				consultantList.add(m);

			}
		}

		if (consultantList != null && consultantList.size() > 0) {
			boolean isRecordAvailable = false;
			for (int i = 0; i < consultantList.size(); i++) {
				if (((Map) consultantList.get(i)).get("emp_id") != null
						&& ((Map) consultantList.get(i)).get("emp_id").equals(req.getLoggedInEmpId())) {
					isRecordAvailable = true;
				}
			}

			if (!isRecordAvailable) {
				Map m = new HashMap();
				List<Integer> userId = dmsEmployeeRepo.findEmpIdsByNames(loginEmpName, orgId);
				List<DMSEmployee> userData = empRepo.findEmpByNames(loginEmpName, orgId);
				m.put("emp_id", userId.get(0));
				m.put("branch", userData.get(0).getDmsBranch().getName());
				m.put("emp_name", loginEmpName);
				m.put("roleName", userData.get(0).getDmsRole().getRoleName());
				m.put("allocatedCount", 0);
				m.put("droppedCount", 0);
				m.put("contactCount", 0);
				m.put("enquiryCount", 0);
				m.put("bookingCount", 0);
				m.put("retailCount", 0);
				consultantList.add(m);
			}

			map.put("consultantList", consultantList);
		} else {
			Map m = new HashMap();
			List<Integer> userId = dmsEmployeeRepo.findEmpIdsByNames(loginEmpName, orgId);
			List<DMSEmployee> userData = empRepo.findEmpByNames(loginEmpName, orgId);
			m.put("emp_id", userId.get(0));
			m.put("branch", userData.get(0).getDmsBranch().getName());
			m.put("emp_name", loginEmpName);
			m.put("roleName", userData.get(0).getDmsRole().getRoleName());
			m.put("allocatedCount", 0);
			m.put("droppedCount", 0);
			m.put("contactCount", 0);
			m.put("enquiryCount", 0);
			m.put("bookingCount", 0);
			m.put("retailCount", 0);
			consultantList.add(m);
			map.put("consultantList", consultantList);
		}

//		map.put("consultantList", consultantList);

		if (!dealerCode.isEmpty()) {
			map.put("totalDroppedCount", dmsLeadDao.getTotaLiveContactDrops(loginEmpName, dealerCode, orgId));
			map.put("totalAllocatedCount", dmsLeadDao.getAllocatedLiveLeadsCountW(orgId, dealerCode, loginEmpName));
			map.put("totalLostCount", dmsLeadDao.getDroppedLiveLeadsCountt(orgId, dealerCode, loginEmpName));

			map.put("contactsCount", dmsLeadDao.getTotalPreEnqLiveCountWithFilter(orgId, loginEmpName, dealerCode));
			map.put("enquirysCount", dmsLeadDao.getTotalEnqLiveCountWithFilter(orgId, loginEmpName, dealerCode));
			map.put("bookingsCount", dmsLeadDao.getTotalBkgLiveCountWithFilter(orgId, loginEmpName, dealerCode));
			map.put("RetailCount", dmsLeadDao.getTotalRetLiveCountWithFilter(orgId, loginEmpName, dealerCode));

		} else {
			map.put("totalDroppedCount", dmsLeadDao.getTotaLiveContactDrop(loginEmpName, orgId));
			map.put("totalAllocatedCount", dmsLeadDao.getAllocatedLiveLeadsCount(orgId, loginEmpName));
			map.put("totalLostCount", dmsLeadDao.getDroppedLiveLeadsCountt(orgId, loginEmpName));

			map.put("contactsCount", dmsLeadDao.getTotalPreEnqLiveCount(orgId, loginEmpName));
			map.put("enquirysCount", dmsLeadDao.getTotalEnqLiveCount(orgId, loginEmpName));
			map.put("bookingsCount", dmsLeadDao.getTotalBkgLiveCount(orgId, loginEmpName));
			map.put("RetailCount", dmsLeadDao.getTotalRetLiveCount(orgId, loginEmpName));
		}

		return map;
	}

	public List<String> getEmpsForLiveLeads(ReceptionistDashBoardReq req) {

		List<String> emps = new ArrayList<>();
		DMSEmployee dmsEmp = empRepo.findemp(req.getLoggedInEmpId());
		Set<Integer> branches = empLocationMappingDao.getBranches(dmsEmp.getEmpId());

//		if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("MD")
//				|| dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("GM")
//				|| dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("Manager")
//				|| dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("Sales Manager")
//				|| dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("branch Manager")) {
//
////			Set<Integer> branches = empLocationMappingDao.getBranches(dmsEmp.getEmpId());
//
//			if (req.getDashboardType() != null && req.getDashboardType().equalsIgnoreCase("reception")) {
//				if (req.getBranchList() == null) {
//					emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranches(branches));
//				} else {
//					Set<Integer> branchs = new HashSet<>();
//					branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
//
//					emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranches(branchs));
//				}
//
//			} else {
//				if (req.getBranchList() == null) {
//					emps.addAll(dmsEmployeeRepo.getEmsByBranches(branches));
//					emps.addAll(dmsEmployeeRepo.getCrmEmsByBranches(branches));
//				} else {
//					Set<Integer> branchs = new HashSet<>();
//					branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
//					emps.addAll(dmsEmployeeRepo.getEmsByBranches(branchs));
//					emps.addAll(dmsEmployeeRepo.getCrmEmsByBranches(branchs));
//				}
//
//			}
//		}

		if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("CRM")) {

			if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("CRM") && req.getDashboardType() != null
					&& req.getDashboardType().equalsIgnoreCase("reception")) {
				if (req.getBranchList()==null) {
					emps.addAll(dmsEmployeeRepo.findCrmReceptionReportees(dmsEmp.getEmpId()));

					emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
				} else {

					emps.addAll(dmsEmployeeRepo.findCrmReceptionReporteesWithFilter(dmsEmp.getEmpId(),
							dmsBranchDao.getDealerCodes(req.getBranchList())));

					Set<Integer> branchs = new HashSet<>();
					branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
					emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
				}
			} else {
				if (req.getBranchList()==null) {
					
					emps.addAll(dmsEmployeeRepo.getCrmEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
					emps.add(dmsEmp.getEmpName());
					
				} else {
					emps.add(dmsEmp.getEmpName());
					Set<Integer> branchs = new HashSet<>();
					branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
					emps.addAll(dmsEmployeeRepo.getCrmEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
				}
			}

		}

		return emps;
	}
	
	
	public List<String> getEmpsForLiveLeadsIsSelf(ReceptionistDashBoardReq req) {

		List<String> emps = new ArrayList<>();
		DMSEmployee dmsEmp = empRepo.findemp(req.getLoggedInEmpId());
//		Set<Integer> branches = empLocationMappingDao.getBranches(dmsEmp.getEmpId());


		if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("CRM")) {

			if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("CRM") && req.getDashboardType() != null
					&& req.getDashboardType().equalsIgnoreCase("reception")) {
				if (req.getBranchList()==null) {
					emps.addAll(dmsEmployeeRepo.findCrmReceptionReportees(dmsEmp.getEmpId()));

//					emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
				} else {

					emps.addAll(dmsEmployeeRepo.findCrmReceptionReporteesWithFilter(dmsEmp.getEmpId(),
							dmsBranchDao.getDealerCodes(req.getBranchList())));

//					Set<Integer> branchs = new HashSet<>();
//					branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
//					emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
				}
			} else {
				if (req.getBranchList()==null) {
					
//					emps.addAll(dmsEmployeeRepo.getCrmEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
					emps.add(dmsEmp.getEmpName());
					
				} else {
					emps.add(dmsEmp.getEmpName());
//					Set<Integer> branchs = new HashSet<>();
//					branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
//					emps.addAll(dmsEmployeeRepo.getCrmEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
				}
			}

		}

		return emps;
	}

	@Autowired
	private EmpLocationMappingDao empLocationMappingDao;

	public Map getManagerLiveLeadData(ReceptionistDashBoardReq req) {

		Map map = new HashMap();
		String startDate = getStartDate(req.getStartDate()) + " 00:00:00";
		String endDate = getEndDate(req.getEndDate()) + " 23:59:59";
		int orgId = req.getOrgId();
		String dealerCode = req.getDealerCode();
		List<String> emps;

		List<Integer> branchList = new ArrayList<>();
		try {
//			branchList = getActiveDropdownsV2(req.getBranchList(), req.getOrgId(), req.getLoggedInEmpId());
			branchList = dmsBranchDao.getDealerCodes(req.getBranchList());
		} catch (Exception e) {
			e.printStackTrace();
		}

		int totalPreInquiryCount = 0;

		String empName = dmsEmployeeRepo.getEmpName(Integer.toString(req.getLoggedInEmpId()));

		if (req.getEmpList() != null && req.getEmpList().size() > 0) {
			emps = dmsEmployeeRepo.findemps(req.getEmpList());
		} else {
//			if (branchList.isEmpty()) {
			emps = getEmpsForLiveLeads(req);

//			} else {
//				emps = dmsEmployeeRepo.findEmpsSalesManagerByBranch(req.getLoggedInEmpId(), branchList);
//			}
		}

		List<HashMap> consultantListReceptionist = new ArrayList();
		List<HashMap> consultantListCRM = new ArrayList();
		for (String one : emps) {
			HashMap receptionistMap = new HashMap();
			HashMap CRMMap = new HashMap();
			List<Integer> userId1 = dmsEmployeeRepo.findEmpIdsByNames(one, orgId);
			DMSEmployee dms1 = empRepo.findemp(one,req.getOrgId());

			if (!dms1.getDmsRole().getRoleName().equalsIgnoreCase("CRM")) {
				receptionistMap.put("emp_name", one);
				receptionistMap.put("emp_id", userId1.get(0));
				receptionistMap.put("branch", dms1.getDmsBranch().getName());
				receptionistMap.put("roleName", dms1.getDmsRole().getRoleName());

				List<HashMap> consultantList2 = new ArrayList();
				if (!branchList.isEmpty()) {
					req.setDealerCodes(null);
					req.setDealerCodes(dmsBranchDao.getDealerCodeName(branchList));
				}
				req.setLoggedInEmpId(userId1.get(0));
				receptionistMap.put("data", getReceptionistLiveLeadData(req));
				consultantListReceptionist.add(receptionistMap);

			} else if (dms1.getDmsRole().getRoleName().equalsIgnoreCase("CRM")) {
				CRMMap.put("emp_name", one);
				CRMMap.put("emp_id", userId1.get(0));
				CRMMap.put("branch", dms1.getDmsBranch().getName());
				CRMMap.put("roleName", dms1.getDmsRole().getRoleName());
				req.setLoggedInEmpId(userId1.get(0));
				CRMMap.put("data", getTeamCountLiveLeads(req));
				consultantListCRM.add(CRMMap);
			}
		}

//      Total calculation of data

		long retailCount = 0;
		long totalLostCount = 0;
		long bookingCount = 0;
		long totalEnquiryCount = 0;

		for (int i = 0; i < consultantListReceptionist.size(); i++) {
			HashMap data = (HashMap) consultantListReceptionist.get(i).get("data");
			retailCount += (Long) data.get("RetailCount");
			totalLostCount += (Long) data.get("totalLostCount");
			bookingCount += (Long) data.get("bookingsCount");
			totalEnquiryCount += (Long) data.get("enquirysCount");
			totalPreInquiryCount += (Long) data.get("contactsCount");
		}

		for (int i = 0; i < consultantListCRM.size(); i++) {
			HashMap data = (HashMap) consultantListCRM.get(i).get("data");
			retailCount += (Long) data.get("totalRetailCount");
			totalLostCount += (Long) data.get("totalLostCount");
			bookingCount += (Long) data.get("totalBookingCount");
			totalEnquiryCount += (Long) data.get("totalEnquiryCount");
			totalPreInquiryCount += (Long) data.get("totalPreInquiryCount");

		}

		map.put("totalRetailCount", retailCount);
		map.put("totalLostCount", totalLostCount);
		map.put("totalBookingCount", bookingCount);
		map.put("totalEnquiryCount", totalEnquiryCount);
		map.put("childUserCount", consultantListReceptionist.size() + consultantListCRM.size());

		Integer totalDroppedCount = 0;
		emps.add(empName);
		if (!branchList.isEmpty()) {
			totalDroppedCount = dmsLeadDao.getTotaContactDrops(emps, startDate, endDate, branchList,
					Integer.valueOf(orgId));
		} else {
			totalDroppedCount = dmsLeadDao.getTotaContactDrops(emps, startDate, endDate, Integer.valueOf(orgId));
		}
		map.put("totalDroppedCount", totalDroppedCount);
		map.put("totalPreInquiryCount", totalPreInquiryCount);

		map.put("CRE", consultantListReceptionist);
		map.put("CRM", consultantListCRM);

		return map;

	}

	public Map getTeamCountLiveLeads(ReceptionistDashBoardReq req) {

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());

		Map map = new HashMap();
		String startDate = getStartDate(req.getStartDate()) + " 00:00:00";
		String endDate = getEndDate(req.getEndDate()) + " 23:59:59";
		int orgId = req.getOrgId();
		String dealerCode = req.getDealerCode();
		List<String> emps;

		List<Integer> branchList = new ArrayList<>();
		try {
//			branchList = getActiveDropdownsV2(req.getBranchList(), req.getOrgId(), req.getLoggedInEmpId());
			branchList = dmsBranchDao.getDealerCodes(req.getBranchList());
		} catch (Exception e) {
			e.printStackTrace();
		}

		DMSEmployee dmsEmp = empRepo.findemp(dmsEmployeeObj.getEmpName(),req.getOrgId());
		String loginUserRole = dmsEmp.getDmsRole().getRoleName();

		if (req.getEmpList() != null && req.getEmpList().size() > 0) {
			emps = dmsEmployeeRepo.findemps(req.getEmpList());
		} else {
//			emps = getEmpList(req, branchList);
			emps = dmsEmployeeRepo.findCrmReportees(dmsEmp.getEmpId());
			emps.add(dmsEmployeeObj.getEmpName());
		}

		List<HashMap> consultantList = new ArrayList();
		

		for (String one : emps) {
			HashMap m = new HashMap();
			m.put("emp_name", one);

			List<Integer> userId = dmsEmployeeRepo.findEmpIdsByNames(one, orgId);
			m.put("emp_id", userId.get(0));

			DMSEmployee dms = empRepo.findemp(one,req.getOrgId());
			m.put("branch", dms.getDmsBranch().getName());
			m.put("roleName", dms.getDmsRole().getRoleName());
			// .collect(Collectors.toList()
			List<DmsLead> stageCounts = new ArrayList<>();
//			if (branchList != null && branchList.size() > 0) {
//				stageCounts = dmsLeadDao.getAllocatedLeadsCountData(startDate, endDate, orgId, one, branchList);
//			} else {
//			int	total = dmsLeadDao.getAllocatedLiveLeadsCount(orgId, one);
//			}

//			List<Integer> leadList = stageCounts.stream().map(DmsLead::getId).collect(Collectors.toList());
//			List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(startDate, endDate,
//					String.valueOf(orgId), leadList);
			m.put("enquiryCount", dmsLeadDao.getTotalEnqLiveCountM(orgId, one));

			m.put("droppedCount", dmsLeadDao.getTotaLiveContactDropM(one, orgId));

			m.put("bookingCount", dmsLeadDao.getTotalBkgLiveCountM(orgId, one));

			m.put("retailCount", dmsLeadDao.getTotalRetLiveCountM(orgId, one));
			m.put("contactCount", dmsLeadDao.getTotalPreEnqLiveCountM(orgId, one));
			consultantList.add(m);

//			preInquiryCount1 = dmsLeadDao.getTotalPreEnqLiveCountM(orgId, one);

			List<HashMap> consultantList1 = new ArrayList();
			if (!branchList.isEmpty()) {
				req.setDealerCodes(null);
				req.setDealerCodes(dmsBranchDao.getDealerCodeName(branchList));

			}
			req.setLoggedInEmpId(userId.get(0));
			ArrayList<Map> salesConsltantData = (ArrayList<Map>) getReceptionistLiveLeadData(req).get("consultantList");
			for (int i = 0; i < salesConsltantData.size(); i++) {
				HashMap m1 = new HashMap();
				m1.put("contactCount", salesConsltantData.get(i).get("contactCount"));
				m1.put("enquiryCount", salesConsltantData.get(i).get("enquiryCount"));
				m1.put("droppedCount", salesConsltantData.get(i).get("droppedCount"));
				m1.put("bookingCount", salesConsltantData.get(i).get("bookingCount"));
				m1.put("retailCount", salesConsltantData.get(i).get("retailCount"));
				m1.put("emp_name", salesConsltantData.get(i).get("emp_name"));
				m1.put("emp_id", salesConsltantData.get(i).get("emp_id"));
				m1.put("branch", salesConsltantData.get(i).get("branch"));
				m1.put("roleName", salesConsltantData.get(i).get("roleName"));
				consultantList1.add(m1);
			}
			m.put("salesconsultant", consultantList1);

		}
		map.put("manager", consultantList);

		long retailCount = 0;
		long totalLostCount = 0;
		long bookingCount = 0;
		long totalEnquiryCount = 0;
		long preInquiryCount1 = 0;

		for (int i = 0; i < consultantList.size(); i++) {
			retailCount += (Long) consultantList.get(i).get("retailCount");
			totalLostCount += (Long) consultantList.get(i).get("droppedCount");
			bookingCount += (Long) consultantList.get(i).get("bookingCount");
			totalEnquiryCount += (Long) consultantList.get(i).get("enquiryCount");
			preInquiryCount1 += (Long) consultantList.get(i).get("contactCount");
		}

		map.put("totalRetailCount", retailCount);
		map.put("totalLostCount", totalLostCount);
		map.put("totalBookingCount", bookingCount);
		map.put("totalEnquiryCount", totalEnquiryCount);
		map.put("childUserCount", consultantList.size());

		Integer totalDroppedCount = 0;
//		if (branchList != null && branchList.size() > 0) {
//			totalDroppedCount = dmsLeadDao.getTotaContactDrops(emps, startDate, endDate, branchList,
//					Integer.valueOf(orgId));
//		} else {
//			totalDroppedCount = dmsLeadDao.getTotaContactDrops(emps, startDate, endDate, Integer.valueOf(orgId));
//		}
		map.put("totalDroppedCount", totalDroppedCount);
		map.put("totalPreInquiryCount", preInquiryCount1);

		return map;
	}

	public List<SourceRes> getManagerSourceLiveLeadData(ReceptionistDashBoardReq req) {
		List<SourceRes> resList = new ArrayList<>();

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
//		String dealerCode = req.getDealerCode();

		List<Integer> branchList = new ArrayList<>();
		try {
//			branchList = getActiveDropdownsV2(req.getBranchList(), req.getOrgId(), req.getLoggedInEmpId());
			branchList = dmsBranchDao.getDealerCodes(req.getBranchList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<String> emps = new ArrayList<>();
		if (req.isSelf()) {

//		emps = getEmpsForLiveLeadsIsSelf(req);

			DMSEmployee dmsEmp = empRepo.findemp(req.getLoggedInEmpId());
//		Set<Integer> branches = empLocationMappingDao.getBranches(dmsEmp.getEmpId());

			if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("CRM")) {

				if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("CRM") && req.getDashboardType() != null
						&& req.getDashboardType().equalsIgnoreCase("reception")) {
					if (req.getBranchList() == null) {
						emps.addAll(dmsEmployeeRepo.findCrmReceptionReportees(dmsEmp.getEmpId()));

//					emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
					} else {

						emps.addAll(dmsEmployeeRepo.findCrmReceptionReporteesWithFilter(dmsEmp.getEmpId(),
								dmsBranchDao.getDealerCodes(req.getBranchList())));

//					Set<Integer> branchs = new HashSet<>();
//					branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
//					emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
					}
				} else {
					if (req.getBranchList() == null) {
						emps.addAll(dmsEmployeeRepo.findCrmReportees(dmsEmp.getEmpId()));

//					emps.addAll(dmsEmployeeRepo.getCrmEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
						emps.add(dmsEmp.getEmpName());

					} else {
						emps.add(dmsEmp.getEmpName());
						Set<Integer> branchs = new HashSet<>();
						branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
						emps.addAll(dmsEmployeeRepo.findCrmReporteesWithBranches(dmsEmp.getEmpId(), branchs));
					}
				}

			}

		} else {

//			 emps = getEmpsForLiveLeads(req);

			DMSEmployee dmsEmp = empRepo.findemp(req.getLoggedInEmpId());
			Set<Integer> branches = empLocationMappingDao.getBranches(dmsEmp.getEmpId());

			if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("CRM")) {

				if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("CRM") && req.getDashboardType() != null
						&& req.getDashboardType().equalsIgnoreCase("reception")) {
					if (req.getBranchList() == null) {
						emps.addAll(dmsEmployeeRepo.findCrmReceptionReportees(dmsEmp.getEmpId()));

						emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
					} else {

						emps.addAll(dmsEmployeeRepo.findCrmReceptionReporteesWithFilter(dmsEmp.getEmpId(),
								dmsBranchDao.getDealerCodes(req.getBranchList())));

						Set<Integer> branchs = new HashSet<>();
						branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
						emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
					}
				} else {
					if (req.getBranchList() == null) {
						emps.addAll(dmsEmployeeRepo.findCrmReportees(dmsEmp.getEmpId()));

						emps.addAll(dmsEmployeeRepo.getCrmEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
						emps.add(dmsEmp.getEmpName());

					} else {
						emps.add(dmsEmp.getEmpName());
						Set<Integer> branchs = new HashSet<>();
						branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
						emps.addAll(dmsEmployeeRepo.findCrmReporteesWithBranches(dmsEmp.getEmpId(), branches));
						emps.addAll(dmsEmployeeRepo.getCrmEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
					}
				}

			}

		}

//		List<DMSEmployee> dmsEmps = empRepo.findemps(emps);
//
//		List<DMSEmployee> dmsEmp = dmsEmps.stream().filter(x -> x.getDmsRole().getRoleName().equalsIgnoreCase("CRM"))
//				.collect(Collectors.toList());
//
//		if (!dmsEmp.isEmpty()) {
//
//			if (req.getDashboardType() != null && req.getDashboardType().equalsIgnoreCase("reception")) {
//				emps.addAll(dmsEmployeeRepo.findCrmReceptionReportees(
//						dmsEmp.stream().map(DMSEmployee::getEmpId).collect(Collectors.toList())));
//			} else {
//
//				emps.addAll(dmsEmployeeRepo
//						.findCrmReportees(dmsEmp.stream().map(DMSEmployee::getEmpId).collect(Collectors.toList())));
//
//				emps.addAll(dmsEmp.stream().map(DMSEmployee::getEmpName).collect(Collectors.toList()));
//			}
//		}

		String orgId = req.getOrgId().toString();
		List<SubSourceId> reslist = subSourceRepo.getSources(orgId);
		Map<String, Integer> map = new LinkedHashMap<>();
		if (!emps.isEmpty()) {

			for (SubSourceId res : reslist) {

				SourceRes leadSource = new SourceRes();
				List<Integer> dmsLeadList;
				if (!branchList.isEmpty()) {
					dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBySourceManLiveLeads(orgId, startDate, endDate,
							res.getSource().getId(), res.getSubSource(), emps, branchList);
				} else {
					dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBySourceManLiveLead(orgId, res.getSource().getId(),
							res.getSubSource(), emps);
				}
				if (!dmsLeadList.isEmpty()) {

					if (!branchList.isEmpty()) {
						
						leadSource.setContact(dmsLeadDao.getLivePreEnqLeadsBySourceWithFilter(orgId,
								res.getSource().getId(), res.getSubSource(), branchList, emps));
						
						leadSource.setE(dmsLeadDao.getLiveEnqLeadsBySourceWithFilter(orgId, startDate, endDate,
								res.getSource().getId(), res.getSubSource(), branchList, emps));

						leadSource.setB(dmsLeadDao.getLiveBkgLeadsBySourceWithFilter(orgId, startDate, endDate,
								res.getSource().getId(), res.getSubSource(), branchList, emps));

						leadSource.setR(dmsLeadDao.getLiveRtlLeadsBySourceWithFilter(orgId, startDate, endDate,
								res.getSource().getId(), res.getSubSource(), branchList, emps));

						leadSource.setL(dmsLeadDao.getAllEmployeeLeadsBySourceManLostLiveLeads(orgId, startDate,
								endDate, res.getSource().getId(), res.getSubSource(), emps, branchList));

						leadSource.setSource(res.getSource().getName());
						leadSource.setSubsource(res.getSubSource());
						resList.add(leadSource);
					} else {	
						
						leadSource.setContact(dmsLeadDao.getLivePreEnqLeadsBySource(orgId,
								res.getSource().getId(), res.getSubSource(), emps));

						leadSource.setE(dmsLeadDao.getLiveEnqLeadsBySource(orgId, res.getSource().getId(),
								res.getSubSource(), emps));

						leadSource.setB(dmsLeadDao.getLiveBkgLeadsBySource(orgId, res.getSource().getId(),
								res.getSubSource(), emps));

						leadSource.setR(dmsLeadDao.getLiveRtlLeadsBySource(orgId, res.getSource().getId(),
								res.getSubSource(), emps));

						leadSource.setL(dmsLeadDao.getAllEmployeeLeadsBySourceManLostLiveLead(orgId,
								res.getSource().getId(), res.getSubSource(), emps));

						leadSource.setSource(res.getSource().getName());
						leadSource.setSubsource(res.getSubSource());
						resList.add(leadSource);

					}
				}
			}
		}

		return resList;

	}

	public List<VehicleModelRes> getManagerModelLiveLeadData(ReceptionistDashBoardReq req) {
		List<VehicleModelRes> resList = new ArrayList<>();

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());

		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
//		String dealerCode = req.getDealerCode()

		List<Integer> branchList = new ArrayList<>();
		try {
//			branchList = getActiveDropdownsV2(req.getBranchList(), req.getOrgId(), req.getLoggedInEmpId());
			branchList = dmsBranchDao.getDealerCodes(req.getBranchList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<String> emps = new ArrayList<>();
		if (req.isSelf()) {

//			emps = getEmpsForLiveLeadsIsSelf(req);

				DMSEmployee dmsEmp = empRepo.findemp(req.getLoggedInEmpId());
//			Set<Integer> branches = empLocationMappingDao.getBranches(dmsEmp.getEmpId());

				if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("CRM")) {

					if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("CRM") && req.getDashboardType() != null
							&& req.getDashboardType().equalsIgnoreCase("reception")) {
						if (req.getBranchList() == null) {
							emps.addAll(dmsEmployeeRepo.findCrmReceptionReportees(dmsEmp.getEmpId()));

//						emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
						} else {

							emps.addAll(dmsEmployeeRepo.findCrmReceptionReporteesWithFilter(dmsEmp.getEmpId(),
									dmsBranchDao.getDealerCodes(req.getBranchList())));

//						Set<Integer> branchs = new HashSet<>();
//						branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
//						emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
						}
					} else {
						if (req.getBranchList() == null) {
							emps.addAll(dmsEmployeeRepo.findCrmReportees(dmsEmp.getEmpId()));

//						emps.addAll(dmsEmployeeRepo.getCrmEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
							emps.add(dmsEmp.getEmpName());

						} else {
							emps.add(dmsEmp.getEmpName());
							Set<Integer> branchs = new HashSet<>();
							branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
							emps.addAll(dmsEmployeeRepo.findCrmReporteesWithBranches(dmsEmp.getEmpId(), branchs));
						}
					}

				}

			} else {

//				 emps = getEmpsForLiveLeads(req);

				DMSEmployee dmsEmp = empRepo.findemp(req.getLoggedInEmpId());
				Set<Integer> branches = empLocationMappingDao.getBranches(dmsEmp.getEmpId());

				if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("CRM")) {

					if (dmsEmp.getDmsRole().getRoleName().equalsIgnoreCase("CRM") && req.getDashboardType() != null
							&& req.getDashboardType().equalsIgnoreCase("reception")) {
						if (req.getBranchList() == null) {
							emps.addAll(dmsEmployeeRepo.findCrmReceptionReportees(dmsEmp.getEmpId()));

							emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
						} else {

							emps.addAll(dmsEmployeeRepo.findCrmReceptionReporteesWithFilter(dmsEmp.getEmpId(),
									dmsBranchDao.getDealerCodes(req.getBranchList())));

							Set<Integer> branchs = new HashSet<>();
							branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
							emps.addAll(dmsEmployeeRepo.getReceptionEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
						}
					} else {
						if (req.getBranchList() == null) {
							emps.addAll(dmsEmployeeRepo.findCrmReportees(dmsEmp.getEmpId()));

							emps.addAll(dmsEmployeeRepo.getCrmEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
							emps.add(dmsEmp.getEmpName());

						} else {
							emps.add(dmsEmp.getEmpName());
							Set<Integer> branchs = new HashSet<>();
							branchs.addAll(dmsBranchDao.getDealerCodes(req.getBranchList()));
							emps.addAll(dmsEmployeeRepo.findCrmReporteesWithBranches(dmsEmp.getEmpId(), branches));
							emps.addAll(dmsEmployeeRepo.getCrmEmsByBranchesNotReportees(branches, dmsEmp.getEmpId()));
						}
					}

				}

			}

//		List<String> emps = getEmpsForLiveLeads(req);
//
//		List<DMSEmployee> dmsEmps = empRepo.findemps(emps);
//
//		List<DMSEmployee> dmsEmp = dmsEmps.stream().filter(x -> x.getDmsRole().getRoleName().equalsIgnoreCase("CRM"))
//				.collect(Collectors.toList());
//
//		if (!dmsEmp.isEmpty()) {
//
//			if (req.getDashboardType() != null && req.getDashboardType().equalsIgnoreCase("reception")) {
//				emps.addAll(dmsEmployeeRepo.findCrmReceptionReportees(
//						dmsEmp.stream().map(DMSEmployee::getEmpId).collect(Collectors.toList())));
//			} else {
//
//				emps.addAll(dmsEmployeeRepo
//						.findCrmReportees(dmsEmp.stream().map(DMSEmployee::getEmpId).collect(Collectors.toList())));
//
//				emps.addAll(dmsEmp.stream().map(DMSEmployee::getEmpName).collect(Collectors.toList()));
//			}
//		}

		String orgId = req.getOrgId().toString();
		Map<String, Integer> vehicalList = dashBoardUtil.getVehilceDetailsByOrgId(orgId);
		for (String model : vehicalList.keySet()) {

			if (null != model) {
				if (!emps.isEmpty()) {
					VehicleModelRes vehicleRes = new VehicleModelRes();
					log.info("Generating data for model " + model);
					List<DmsLead> dmsLeadList;
					if (!branchList.isEmpty()) {
//						dmsLeadList = dmsLeadDao.getAllEmployeeLeadsByModelMan(orgId, startDate, endDate, model, emps,
//								branchList);
						dmsLeadList = dmsLeadDao.getLiveLeadsModelWithFilterM(orgId, model, branchList, emps);
					} else {
//						dmsLeadList = dmsLeadDao.getAllEmployeeLeadsByModelMan(orgId, startDate, endDate, model, emps);

						dmsLeadList = dmsLeadDao.getLiveLeadsModelM(orgId, model, emps);

					}

					if (!dmsLeadList.isEmpty()) {

						if (branchList.isEmpty()) {
							
							vehicleRes.setContact(dmsLeadDao.getLiveLeadsPreEnqModelM(orgId, model, emps));

							vehicleRes.setE(dmsLeadDao.getLiveLeadsEnqModelM(orgId, model, emps));

							vehicleRes.setB(dmsLeadDao.getLiveLeadsBkgModelM(orgId, model, emps));

							vehicleRes.setR(dmsLeadDao.getLiveLeadsRetailModelM(orgId, model, emps));

							vehicleRes.setL(dmsLeadDao.getDroppedLeadsCountM(Integer.valueOf(orgId), emps, model));

							vehicleRes.setModel(model);

							resList.add(vehicleRes);

						} else {
							
							vehicleRes.setContact(dmsLeadDao.getLiveLeadsPreEnqModelWithFilterM(orgId, model, branchList, emps));

							vehicleRes.setE(dmsLeadDao.getLiveLeadsEnqModelWithFilterM(orgId, model, branchList, emps));

							vehicleRes.setB(dmsLeadDao.getLiveLeadsBkgModelWithFilterM(orgId, model, branchList, emps));

							vehicleRes.setR(
									dmsLeadDao.getLiveLeadsRetailModelWithFilterM(orgId, model, branchList, emps));

							vehicleRes.setL(dmsLeadDao.getDroppedLeadsCountLiveleadFilter(Integer.valueOf(orgId), emps,
									branchList, model));

							vehicleRes.setModel(model);

							resList.add(vehicleRes);

						}
					}
				}
			}
		}
		return resList;

	}

	public Map getReceptionistData(ReceptionistDashBoardReq req, String roleName) {

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		String loginEmpName = dmsEmployeeObj.getEmpName();

		Map map = new HashMap();
		String startDate = getStartDate(req.getStartDate()) + " 00:00:00";
		String endDate = getEndDate(req.getEndDate()) + " 23:59:59";
		int orgId = req.getOrgId();
//		List<Integer> dealerCode = req.getDealerCodes();
		List<Integer> dealerCode = null;
		try {
			dealerCode = dmsBranchDao.getDealerCode(req.getDealerCodes());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		List<String> dmsEmployeeList = new ArrayList<>();
		if (req.getEmpList() != null && req.getEmpList().size() > 0) {
			dmsEmployeeList = dmsEmployeeRepo.findEmpNamesById(orgId, req.getEmpList());
		} else if (req.getDealerCodes() != null && req.getDealerCodes().size() > 0) {
			dmsEmployeeList = dmsEmployeeRepo.findEmpNames(orgId, dealerCode);
		} else {
			dmsEmployeeList = dmsEmployeeRepo.findEmpNames(orgId);
		}

		List<Integer> leadList;
		if (req.getEmpList() != null && req.getEmpList().size() > 0) {
			leadList = dmsLeadDao.getTotalCountEmpByEmp(orgId, startDate, endDate, loginEmpName, req.getEmpList());
		} else if (req.getDealerCodes() != null && req.getDealerCodes().size() > 0) {
			leadList = dmsLeadDao.getTotalCountEmp(orgId, startDate, endDate, loginEmpName, dealerCode);
		} else {
			leadList = dmsLeadDao.getTotalCountEmp(orgId, startDate, endDate, loginEmpName);
		}

//		List<Integer> leadList = dmsLeadDao.getTotalCountEmp(orgId,startDate,endDate,loginEmpName);
		List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(startDate, endDate, String.valueOf(orgId),
				leadList);
		Long enqLeadCnt = 0L;
		Long bookCount = 0L;
		Long contactCnt = 0L;
		Long invoiceCnt = 0L;

		List consultantList = new ArrayList();
		for (String dmsEmployee : dmsEmployeeList) {
			Map m = new HashMap();
			// m.put("emp_id", dmsEmployee.getEmp_id());

			List<Integer> empLeadCount = new ArrayList<>();
			if (dealerCode != null && dealerCode.size() > 0) {
				empLeadCount = dmsLeadDao.getLeadCount(orgId, startDate, endDate, dmsEmployee, loginEmpName,
						dealerCode);
			} else {
				empLeadCount = dmsLeadDao.getLeadCount(orgId, startDate, endDate, dmsEmployee, loginEmpName);
			}

			List<Integer> empLeadCount1 = dmsLeadDao.getLeadDropCount(orgId, startDate, endDate, dmsEmployee,
					loginEmpName);
			List<LeadStageRefEntity> empEBRcount = leadStageRefDao.getLeadsByIds(startDate, endDate,
					String.valueOf(orgId), empLeadCount);

			int count = getAllocatedLeadsCountByEmp(dmsEmployee, startDate, endDate, orgId, dealerCode, loginEmpName);
			if (!empLeadCount1.isEmpty()) {
				List<Integer> userId = dmsEmployeeRepo.findEmpIdsByNames(dmsEmployee, orgId);
				List<DMSEmployee> userData = empRepo.findEmpByNames(dmsEmployee, orgId);
				m.put("emp_id", userId.get(0));
				m.put("branch", userData.get(0).getDmsBranch().getName());
				m.put("emp_name", dmsEmployee);
				m.put("roleName", userData.get(0).getDmsRole().getRoleName());
				m.put("allocatedCount", count);
				m.put("droppedCount",
						getDropeedLeadsCountByEmp(dmsEmployee, startDate, endDate, orgId, dealerCode, loginEmpName));
				m.put("contactCount",
						empEBRcount.stream().filter(x -> x.getStageName().equalsIgnoreCase("PREENQUIRY")).count());
				m.put("enquiryCount",
						empEBRcount.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY")).count());
				m.put("bookingCount",
						empEBRcount.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING")).count());
				m.put("retailCount",
						leadStageRefDao.getRetailCount(startDate, endDate, Integer.valueOf(orgId), empLeadCount));
				consultantList.add(m);

			}
		}

		if (consultantList != null && consultantList.size() > 0) {
			boolean isRecordAvailable = false;
			for (int i = 0; i < consultantList.size(); i++) {
				if (((Map) consultantList.get(i)).get("emp_id") != null
						&& ((Map) consultantList.get(i)).get("emp_id").equals(req.getLoggedInEmpId())) {
					isRecordAvailable = true;
				}
			}

			if (!isRecordAvailable) {
				Map m = new HashMap();
				List<Integer> userId = dmsEmployeeRepo.findEmpIdsByNames(loginEmpName, orgId);
				List<DMSEmployee> userData = empRepo.findEmpByNames(loginEmpName, orgId);
				m.put("emp_id", userId.get(0));
				m.put("branch", userData.get(0).getDmsBranch().getName());
				m.put("emp_name", loginEmpName);
				m.put("roleName", userData.get(0).getDmsRole().getRoleName());
				m.put("allocatedCount", 0);
				m.put("droppedCount", 0);
				m.put("contactCount", 0);
				m.put("enquiryCount", 0);
				m.put("bookingCount", 0);
				m.put("retailCount", 0);
				consultantList.add(m);
			}

			map.put("consultantList", consultantList);
		} else {
			Map m = new HashMap();
			List<Integer> userId = dmsEmployeeRepo.findEmpIdsByNames(loginEmpName, orgId);
			List<DMSEmployee> userData = empRepo.findEmpByNames(loginEmpName, orgId);
			m.put("emp_id", userId.get(0));
			m.put("branch", userData.get(0).getDmsBranch().getName());
			m.put("emp_name", loginEmpName);
			m.put("roleName", userData.get(0).getDmsRole().getRoleName());
			m.put("allocatedCount", 0);
			m.put("droppedCount", 0);
			m.put("contactCount", 0);
			m.put("enquiryCount", 0);
			m.put("bookingCount", 0);
			m.put("retailCount", 0);
			consultantList.add(m);
			map.put("consultantList", consultantList);
		}

		map.put("totalAllocatedCount", getAllocatedLeadsCount(startDate, endDate, orgId, dealerCode, loginEmpName));

		if (req.getDealerCodes() != null && req.getDealerCodes().size() > 0) {
			map.put("totalDroppedCount",
					dmsLeadDao.getTotaContactDrops(loginEmpName, startDate, endDate, dealerCode, orgId));
		} else {
			map.put("totalDroppedCount", dmsLeadDao.getTotaContactDrops(loginEmpName, startDate, endDate, orgId));
		}

		map.put("totalLostCount", getDroppedLeadsCount(startDate, endDate, orgId, dealerCode, loginEmpName));
		contactCnt = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("PREENQUIRY")).count();
		enqLeadCnt = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY")).count();
		bookCount = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING")).count();
		invoiceCnt = leadStageRefDao.getRetailCount(startDate, endDate, Integer.valueOf(orgId), leadList);
//		map.put("bookingCount", getAllLeadsCount(startDate, endDate, "BOOKING", orgId, dealerCode, loginEmpName, roleName) ); 
//		map.put("RetailCount", getAllLeadsCount(startDate, endDate, "INVOICE", orgId, dealerCode, loginEmpName, roleName))  ;
		map.put("contactsCount", contactCnt);
		map.put("enquirysCount", enqLeadCnt);
		map.put("bookingsCount", bookCount);
		map.put("RetailCount", invoiceCnt);
		return map;
	}

	Integer getAllocatedLeadsCountByEmp(String empName, String startDate, String endDate, int orgId,
			List<Integer> dealerCode, String loginEmpName) {
		if (dealerCode == null || dealerCode.size() == 0)
			return dmsLeadDao.getAllocatedLeadsCountByEmp(empName, startDate, endDate, orgId, loginEmpName);
		else
			return dmsLeadDao.getAllocatedLeadsCountByEmp(empName, startDate, endDate, orgId, dealerCode, loginEmpName);
	}

	Integer getDropeedLeadsCountByEmp(String empName, String startDate, String endDate, int orgId,
			List<Integer> dealerCode, String loginEmpName) {
		if (dealerCode == null || dealerCode.size() == 0)
			return dmsLeadDao.getDropeedLeadsCountByEm(empName, startDate, endDate, orgId, loginEmpName);
		else
			return dmsLeadDao.getDropeedLeadsCountByEm(empName, startDate, endDate, orgId, dealerCode, loginEmpName);
	}

	Integer getAllocatedLeadsCount(String startDate, String endDate, int orgId, List<Integer> dealerCode,
			String loginEmpName) {
		if (dealerCode == null || dealerCode.size() == 0)
			return dmsLeadDao.getAllocatedLeadsCount(startDate, endDate, orgId, loginEmpName);
		else
			return dmsLeadDao.getAllocatedLeadsCount(startDate, endDate, orgId, dealerCode, loginEmpName);
	}

	Integer getDroppedLeadsCount(String startDate, String endDate, int orgId, List<Integer> dealerCode,
			String loginEmpName) {
		if (dealerCode == null || dealerCode.size() == 0)
			return dmsLeadDao.getDroppedLeadsCountt(startDate, endDate, orgId, loginEmpName);
		else
			return dmsLeadDao.getDroppedLeadsCountt(startDate, endDate, orgId, dealerCode, loginEmpName);
	}

	Integer getDroppedLeadsCount(String startDate, String endDate, int orgId, String dealerCode, String loginEmpName,
			String roleName) {
		if (StringUtils.isEmpty(dealerCode))
			return dmsLeadDao.getDroppedLeadsCountt(startDate, endDate, orgId, loginEmpName);
		else
			return dmsLeadDao.getDroppedLeadsCount(startDate, endDate, orgId, dealerCode, loginEmpName, roleName);
	}

	Integer getAllLeadsCount(String startDate, String endDate, String leadType, int orgId, String dealerCode,
			String loginEmpName, String roleName) {
		if (StringUtils.isEmpty(dealerCode))
			return dmsLeadDao.getAllLeadsCount(startDate, endDate, leadType, orgId, loginEmpName, roleName);
		else
			return dmsLeadDao.getAllLeadsCount(startDate, endDate, leadType, orgId, dealerCode, loginEmpName, roleName);
	}

	public List<VehicleModelRes> getReceptionistModelData(ReceptionistDashBoardReq req, String roleName) {
		List<VehicleModelRes> resList = new ArrayList<>();

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		String loginEmpName = dmsEmployeeObj.getEmpName();
		String startDate = getStartDate(req.getStartDate()) + " 00:00:00";
		String endDate = getEndDate(req.getEndDate()) + " 23:59:59";
//		String dealerCode = req.getDealerCode();

		List<Integer> dealerCode = null;
		try {
			dealerCode = dmsBranchDao.getDealerCode(req.getDealerCodes());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		String orgId = req.getOrgId().toString();
		Map<String, Integer> vehicalList = dashBoardUtil.getVehilceDetailsByOrgId(orgId);
		for (String model : vehicalList.keySet()) {

			if (null != model) {
				VehicleModelRes vehicleRes = new VehicleModelRes();
				log.info("Generating data for model " + model);
				List<DmsLead> dmsLeadList;

				if (req.getEmpList() != null && req.getEmpList().size() > 0) {
					List<String> empNames = new ArrayList<>();
					for (int i = 0; i < req.getEmpList().size(); i++) {
						DmsEmployee empObj = dmsEmployeeRepo.getById(req.getEmpList().get(i));
						empNames.add(empObj.getEmpName());
					}
					if (dealerCode == null || dealerCode.size() == 0)
						dmsLeadList = dmsLeadDao.getAllEmployeeLeadsByModelSalesConsultant(orgId, startDate, endDate,
								model, loginEmpName, empNames);
					else
						dmsLeadList = dmsLeadDao.getAllEmployeeLeadsByModelSalesConsultant(orgId, startDate, endDate,
								model, dealerCode, loginEmpName, empNames);
				} else {
					if (dealerCode == null || dealerCode.size() == 0)
						dmsLeadList = dmsLeadDao.getAllEmployeeLeadsByModel(orgId, startDate, endDate, model,
								loginEmpName);
					else
						dmsLeadList = dmsLeadDao.getAllEmployeeLeadsByModel(orgId, startDate, endDate, model,
								dealerCode, loginEmpName);
				}

				Long enqLeadCnt = 0L;
				Long bookCount = 0L;
				Long invoiceCount = 0L;
				if (!dmsLeadList.isEmpty()) {
					List<Integer> leadIdList = dmsLeadList.stream()
							.filter(x -> !x.getLeadStage().equalsIgnoreCase("DROPPED")).map(x -> x.getId()).distinct()
							.collect(Collectors.toList());
					log.debug("dmsLeadList::" + dmsLeadList);
					List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(startDate, endDate, orgId,
							leadIdList);
					if (null != leadRefList && !leadRefList.isEmpty()) {
						log.debug("Total leads in LeadReF table is ::" + leadRefList.size());
						enqLeadCnt = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY"))
								.count();
						bookCount = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING"))
								.count();
						invoiceCount = leadStageRefDao.getRetailCount(startDate, endDate, Integer.valueOf(orgId),
								leadIdList);
					}

					vehicleRes.setR(invoiceCount);
					vehicleRes.setModel(model);
					vehicleRes.setE(enqLeadCnt);
					vehicleRes.setL(
							dmsLeadList.stream().filter(x -> x.getLeadStage().equalsIgnoreCase("DROPPED")).count());

					vehicleRes.setB(bookCount);
					resList.add(vehicleRes);
				}
			}
		}
		return resList;

	}

	public List<VehicleModelRes> getLiveLeadModelData(ReceptionistDashBoardReq req) {
		List<VehicleModelRes> resList = new ArrayList<>();

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		String loginEmpName = dmsEmployeeObj.getEmpName();
		String startDate = getStartDate(req.getStartDate()) + " 00:00:00";
		String endDate = getEndDate(req.getEndDate()) + " 23:59:59";
//		String dealerCode = req.getDealerCode();

		List<Integer> dealerCode = null;
		try {
//			dealerCode = dmsBranchDao.getDealerCode(req.getDealerCodes());
			dealerCode = dmsBranchDao.getDealerCodes(req.getBranchList());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		String orgId = req.getOrgId().toString();
		Map<String, Integer> vehicalList = dashBoardUtil.getVehilceDetailsByOrgId(orgId);
		for (String model : vehicalList.keySet()) {

			if (null != model) {
				VehicleModelRes vehicleRes = new VehicleModelRes();
				log.info("Generating data for model " + model);
				List<DmsLead> dmsLeadList;
				if (dealerCode.isEmpty()) {
					dmsLeadList = dmsLeadDao.getLiveLeadsModel(orgId, model, loginEmpName);
					if (!dmsLeadList.isEmpty()) {
						vehicleRes.setR(dmsLeadDao.getLiveLeadsRetailModel(orgId, model, loginEmpName));
						vehicleRes.setModel(model);
						vehicleRes.setE(dmsLeadDao.getLiveLeadsEnqModel(orgId, model, loginEmpName));
						vehicleRes.setContact(dmsLeadDao.getLiveLeadsPreEnqModel(orgId, model, loginEmpName));
						vehicleRes.setL(dmsLeadDao.getLiveLeadsLostModel(orgId, model, loginEmpName));

						vehicleRes.setB(dmsLeadDao.getLiveLeadsBkgModel(orgId, model, loginEmpName));
						resList.add(vehicleRes);
					}

				} else {
					dmsLeadList = dmsLeadDao.getAllEmployeeLeadsByModel(orgId, model, dealerCode, loginEmpName);
					if (!dmsLeadList.isEmpty()) {
						vehicleRes.setR(
								dmsLeadDao.getLiveLeadsRetailModelWithFilter(orgId, model, dealerCode, loginEmpName));
						vehicleRes.setModel(model);
						vehicleRes.setE(
								dmsLeadDao.getLiveLeadsEnqModelWithFilter(orgId, model, dealerCode, loginEmpName));
						vehicleRes.setContact(
								dmsLeadDao.getLiveLeadsPreEnqModelWithFilter(orgId, model, dealerCode, loginEmpName));
						vehicleRes.setL(
								dmsLeadDao.getLiveLeadsLostModelWithFilter(orgId, model, dealerCode, loginEmpName));

						vehicleRes.setB(
								dmsLeadDao.getLiveLeadsBkgModelWithFilter(orgId, model, dealerCode, loginEmpName));
						resList.add(vehicleRes);
					}
				}
			}
		}
		return resList;

	}

	private Long getDroppedCount(List<DmsLead> dmsLeadList) {
		return dmsLeadList.stream().filter(x -> x.getLeadStage().equalsIgnoreCase(DROPPED)).count();
	}

	private Long getEnqLeadCount(List<DmsLead> dmsLeadList) {
		return dmsLeadList.stream().filter(
				x -> x.getLeadStage().equalsIgnoreCase(ENQUIRY) || x.getLeadStage().equalsIgnoreCase("PREBOOKING"))
				.count();
	}

	private Long getInvoiceCount(List<DmsLead> dmsLeadList) {
		return dmsLeadList.stream().filter(x -> x.getLeadStage().equalsIgnoreCase(INVOICE)).count();
	}

	private Long getHomeVisitCount(List<DmsWFTask> wfTaskList) {
		return wfTaskList.stream().filter(
				x -> x.getTaskName().equalsIgnoreCase(HOME_VISIT) && x.getTaskStatus().equalsIgnoreCase("CLOSED"))
				.count();
	}

	public Long getTestDriveCount(List<DmsWFTask> wfTaskList) {
		// TEST_DRIVE_APPROVAL
		return wfTaskList.stream().filter(
				x -> (x.getTaskName().equalsIgnoreCase(TEST_DRIVE) && x.getTaskStatus().equalsIgnoreCase("CLOSED")))
				.count();
	}

	public List<SourceRes> getReceptionistSourceData(ReceptionistDashBoardReq req, String roleName) {
		List<SourceRes> resList = new ArrayList<>();

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		String loginEmpName = dmsEmployeeObj.getEmpName();
		String startDate = getStartDate(req.getStartDate()) + " 00:00:00";
		String endDate = getEndDate(req.getEndDate()) + " 23:59:59";
//		String dealerCode = req.getDealerCode();

		List<Integer> dealerCode = dmsBranchDao.getDealerCode(req.getDealerCodes());

		String orgId = req.getOrgId().toString();
		List<SubSourceId> reslist = subSourceRepo.getSources(orgId);
		Map<String, Integer> map = new LinkedHashMap<>();
		reslist.stream().forEach(res -> {
			SourceRes leadSource = new SourceRes();
			List<DmsLead> dmsLeadList;

			if (req.getEmpList() != null && req.getEmpList().size() > 0) {
				List<String> empNames = new ArrayList<>();
				for (int i = 0; i < req.getEmpList().size(); i++) {
					DmsEmployee empObj = dmsEmployeeRepo.getById(req.getEmpList().get(i));
					empNames.add(empObj.getEmpName());
				}
				if (dealerCode == null || dealerCode.size() == 0)
					dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBySourceSalesConsultant(orgId, startDate, endDate,
							res.getSource().getId(), res.getSubSource(), loginEmpName, empNames);
				else
					dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBySourceSalesConsultant(orgId, startDate, endDate,
							res.getSource().getId(), res.getSubSource(), dealerCode, loginEmpName, empNames);
			} else {
				if (dealerCode == null || dealerCode.size() == 0)
					dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBySource(orgId, startDate, endDate,
							res.getSource().getId(), res.getSubSource(), loginEmpName);
				else
					dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBySource(orgId, startDate, endDate,
							res.getSource().getId(), res.getSubSource(), dealerCode, loginEmpName);
			}

			Long enqLeadCnt = 0L;
			Long bookCount = 0L;
			Long invoiceCount = 0L;

			if (!dmsLeadList.isEmpty()) {
				List<Integer> leadIdList = dmsLeadList.stream()
						.filter(x -> !x.getLeadStage().equalsIgnoreCase("DROPPED")).map(x -> x.getId()).distinct()
						.collect(Collectors.toList());
				List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(startDate, endDate, orgId,
						leadIdList);
				if (null != leadRefList && !leadRefList.isEmpty()) {
					enqLeadCnt = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY")).count();
					bookCount = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING")).count();
					invoiceCount = leadStageRefDao.getRetailCount(startDate, endDate, Integer.valueOf(orgId),
							leadIdList);
				}

				leadSource.setR(invoiceCount);
				leadSource.setSubsource(res.getSubSource());
				leadSource.setSource(res.getSource().getName());
				leadSource.setE(enqLeadCnt);
				leadSource.setL(dmsLeadList.stream().filter(x -> x.getLeadStage().equalsIgnoreCase("DROPPED")).count());

				leadSource.setB(bookCount);
				resList.add(leadSource);
			}

		});

		return resList;

	}

	public List<SourceRes> getLiveLeadsSourceData(ReceptionistDashBoardReq req) {
		List<SourceRes> resList = new ArrayList<>();

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		String loginEmpName = dmsEmployeeObj.getEmpName();
		String startDate = getStartDate(req.getStartDate()) + " 00:00:00";
		String endDate = getEndDate(req.getEndDate()) + " 23:59:59";
//		String dealerCode = req.getDealerCode();

//		List<Integer> dealerCode = dmsBranchDao.getDealerCode(req.getDealerCodes());
		List<Integer> dealerCode = dmsBranchDao.getDealerCodes(req.getBranchList());

		String orgId = req.getOrgId().toString();
		List<SubSourceId> reslist = subSourceRepo.getSources(orgId);
		Map<String, Integer> map = new LinkedHashMap<>();
		reslist.stream().forEach(res -> {
			SourceRes leadSource = new SourceRes();
			List<DmsLead> dmsLeadList;
			if (dealerCode.isEmpty()) {
				dmsLeadList = dmsLeadDao.getLiveLeadsBySource(orgId, res.getSource().getId(), res.getSubSource(),
						loginEmpName);

				if (!dmsLeadList.isEmpty()) {
					leadSource.setR(dmsLeadDao.getLiveRtlLeadsBySource(orgId, res.getSource().getId(),
							res.getSubSource(), loginEmpName));
					leadSource.setSubsource(res.getSubSource());
					leadSource.setSource(res.getSource().getName());
					leadSource.setE(dmsLeadDao.getLiveEnqLeadsBySource(orgId, res.getSource().getId(),
							res.getSubSource(), loginEmpName));
					leadSource.setContact(dmsLeadDao.getLivePreEnqLeadsBySource(orgId, res.getSource().getId(),
							res.getSubSource(), loginEmpName));
					leadSource.setL(dmsLeadDao.getLiveLostLeadsBySource(orgId, res.getSource().getId(),
							res.getSubSource(), loginEmpName));

					leadSource.setB(dmsLeadDao.getLiveBkgLeadsBySource(orgId, res.getSource().getId(),
							res.getSubSource(), loginEmpName));
					resList.add(leadSource);
				}

			} else {
				dmsLeadList = dmsLeadDao.getLiveLeadsBySourceWithFilter(orgId, res.getSource().getId(),
						res.getSubSource(), dealerCode, loginEmpName);
				if (!dmsLeadList.isEmpty()) {
					leadSource.setR(dmsLeadDao.getLiveRtlLeadsBySourceWithFilter(orgId, res.getSource().getId(),
							res.getSubSource(), dealerCode, loginEmpName));
					leadSource.setSubsource(res.getSubSource());
					leadSource.setSource(res.getSource().getName());
					leadSource.setE(dmsLeadDao.getLiveEnqLeadsBySourceWithFilter(orgId, res.getSource().getId(),
							res.getSubSource(), dealerCode, loginEmpName));
					leadSource.setContact(dmsLeadDao.getLivePreEnqLeadsBySourceWithFilter(orgId, res.getSource().getId(),
							res.getSubSource(), dealerCode, loginEmpName));
					leadSource.setL(dmsLeadDao.getLiveLostLeadsBySourcewithFilter(orgId, res.getSource().getId(),
							res.getSubSource(), dealerCode, loginEmpName));

					leadSource.setB(dmsLeadDao.getLiveBkgLeadsBySourceWithFilter(orgId, res.getSource().getId(),
							res.getSubSource(), dealerCode, loginEmpName));
					resList.add(leadSource);
				}

			}

		});

		return resList;

	}

	public List<ReceptionistLeadRes> getReceptionistLeadData(ReceptionistDashBoardReq req, String roleName) {

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		String loginEmpName = dmsEmployeeObj.getEmpName();
		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
		String dealerCode = req.getDealerCode();

		int orgId = req.getOrgId();

		if (StringUtils.isEmpty(req.getEmpName())) {
			return null;
		}
		String empName = req.getEmpName();

		List<ReceptionistLeadRes> result = new ArrayList();
		List<Object[]> resultList;
		if (StringUtils.isEmpty(dealerCode))
			resultList = dmsLeadDao.getAllocatedLeadsByEmp(empName, startDate, endDate, orgId, loginEmpName, roleName);
		else
			resultList = dmsLeadDao.getAllocatedLeadsByEmp(empName, startDate, endDate, orgId, dealerCode, loginEmpName,
					roleName);

		for (Object[] record : resultList) {
			result.add(new ReceptionistLeadRes((String) record[0], (String) record[1], (Date) record[2],
					(String) record[3], (String) record[4], (String) record[5], (String) record[6],
					(String) record[7]));
		}
		return result;
	}

	public List<ReceptionistLeadRes> getReceptionistDroppedLeadData(ReceptionistDashBoardReq req, String roleName) {

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		String loginEmpName = dmsEmployeeObj.getEmpName();
		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
		String dealerCode = req.getDealerCode();

		int orgId = req.getOrgId();

		if (StringUtils.isEmpty(req.getEmpName())) {
			return null;
		}
		String empName = req.getEmpName();

		List<ReceptionistLeadRes> result = new ArrayList();
		List<Object[]> resultList;
		if (StringUtils.isEmpty(dealerCode))
			resultList = dmsLeadDao.getDroppedLeadsByEmp(empName, startDate, endDate, orgId, loginEmpName, roleName);
		else
			resultList = dmsLeadDao.getDroppedLeadsByEmp(empName, startDate, endDate, orgId, dealerCode, loginEmpName,
					roleName);

		for (Object[] record : resultList) {
			result.add(new ReceptionistLeadRes((String) record[0], (String) record[1], (Date) record[2],
					(String) record[3], (String) record[4], (String) record[5], (String) record[6], (String) record[7],
					(Date) record[8]));
		}
		return result;
	}

	public Map getReceptionistManagerData(ReceptionistDashBoardReq req, String roleName) {
		Map map = new HashMap();
		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
		int orgId = req.getOrgId();
		String dealerCode = req.getDealerCode();
		List<String> emps;
		List<Integer> branchlist = req.getBranchList();
		if (branchlist == null) {
			emps = dmsEmployeeRepo.findemps(req.getLoggedInEmpId());
			if (req.getLoggedInEmpId() != null) {
				emps.add(dmsEmployeeRepo.getEmpName(Integer.toString(req.getLoggedInEmpId())));
			}
		} else {
			emps = dmsEmployeeRepo.findempsByBranch(req.getBranchList());
			if (req.getLoggedInEmpId() != null) {
				emps.add(dmsEmployeeRepo.getEmpName(Integer.toString(req.getLoggedInEmpId())));
			}
		}
		List<String> dmsEmployeeList = dmsEmployeeRepo.findEmpNames(orgId);
		List<Integer> leadList = dmsLeadDao.getTotalCount(orgId, startDate, endDate, emps);
		List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(startDate, endDate, String.valueOf(orgId),
				leadList);
		Long enqLeadCnt = 0L;
		Long bookCount = 0L;
		Long contactCnt = 0L;
		Long invoiceCnt = 0L;
		List consultantList = new ArrayList();
		if (!emps.isEmpty()) {
			for (String dmsEmployee : dmsEmployeeList) {
				Map m = new HashMap();
				// m.put("emp_id", dmsEmployee.getEmp_id());
				List<Integer> empLeadCount = dmsLeadDao.getEmpLeadCount(orgId, startDate, endDate, dmsEmployee, emps);
				List<Integer> empLeadCount1 = dmsLeadDao.getEmpLeadCountWithDrop(orgId, startDate, endDate, dmsEmployee,
						emps);
				List<LeadStageRefEntity> empEBRcount = leadStageRefDao.getLeadsByIds(startDate, endDate,
						String.valueOf(orgId), empLeadCount);

				int count = getAllocatedLeadsCountByEmpMan(dmsEmployee, startDate, endDate, orgId, dealerCode, emps,
						roleName);
				if (!empLeadCount1.isEmpty()) {
					m.put("emp_name", dmsEmployee);
					m.put("allocatedCount", count);
					m.put("droppedCount", getDropeedLeadsCountByEmpMan(dmsEmployee, startDate, endDate, orgId,
							dealerCode, emps, roleName));

					m.put("contactCount",
							empEBRcount.stream().filter(x -> x.getStageName().equalsIgnoreCase("PREENQUIRY")).count());
					m.put("enquiryCount",
							empEBRcount.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY")).count());
					m.put("bookingCount",
							empEBRcount.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING")).count());
					m.put("retailCount",
							leadStageRefDao.getRetailCount(startDate, endDate, Integer.valueOf(orgId), empLeadCount));
					consultantList.add(m);
				}
			}
			contactCnt = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("PREENQUIRY")).count();
			enqLeadCnt = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY")).count();
			bookCount = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING")).count();
			invoiceCnt = leadStageRefDao.getRetailCount(startDate, endDate, Integer.valueOf(orgId), leadList);
			map.put("consultantList", consultantList);
//		map.put("totalAllocatedCount",
//				getAllocatedLeadsCountMan(startDate, endDate, orgId, dealerCode, emps, roleName));
			map.put("totalAllocatedCount", enqLeadCnt);
			map.put("totalDroppedCount", dmsLeadDao.getTotaContactDrops(emps, startDate, endDate));
			map.put("totalLostCount", getDroppedLeadsCountMan(startDate, endDate, orgId, dealerCode, emps, roleName));

//		map.put("bookingCount",
//				getAllLeadsCountMan(startDate, endDate, "BOOKING", orgId, dealerCode, emps, roleName));
			map.put("contactsCount", contactCnt);
			map.put("enquirysCount", enqLeadCnt);
			map.put("bookingsCount", bookCount);
			map.put("RetailCount", invoiceCnt);
//		map.put("RetailCount",
//				getAllLeadsCountMan(startDate, endDate, "INVOICE", orgId, dealerCode, emps, roleName));
		}
		return map;
	}

	Integer getAllocatedLeadsCountByEmpMan(String empName, String startDate, String endDate, int orgId,
			String dealerCode, List<String> loginEmpName, String roleName) {
		if (StringUtils.isEmpty(dealerCode))
			return dmsLeadDao.getAllocatedLeadsCountByEm(empName, startDate, endDate, orgId, loginEmpName);
		else
			return dmsLeadDao.getAllocatedLeadsCountByEmpMan(empName, startDate, endDate, orgId, dealerCode,
					loginEmpName, roleName);
	}

	Integer getDropeedLeadsCountByEmpMan(String empName, String startDate, String endDate, int orgId, String dealerCode,
			List<String> loginEmpName, String roleName) {
		if (StringUtils.isEmpty(dealerCode))
			return dmsLeadDao.getDropeedLeadsCountByEmpMa(empName, startDate, endDate, orgId, loginEmpName);
		else
			return dmsLeadDao.getDropeedLeadsCountByEmpMan(empName, startDate, endDate, orgId, dealerCode, loginEmpName,
					roleName);
	}

	Integer getAllocatedLeadsCountMan(String startDate, String endDate, int orgId, String dealerCode,
			List<String> loginEmpName, String roleName) {
		if (StringUtils.isEmpty(dealerCode))
			return dmsLeadDao.getAllocatedLeadsCountMan(startDate, endDate, orgId, loginEmpName, roleName);
		else
			return dmsLeadDao.getAllocatedLeadsCountMan(startDate, endDate, orgId, dealerCode, loginEmpName, roleName);
	}

	Integer getDroppedLeadsCountMan(String startDate, String endDate, int orgId, String dealerCode,
			List<String> loginEmpName, String roleName) {
		if (StringUtils.isEmpty(dealerCode))
			return dmsLeadDao.getDroppedLeadsCountMa(startDate, endDate, orgId, loginEmpName);
		else
			return dmsLeadDao.getDroppedLeadsCountMan(startDate, endDate, orgId, dealerCode, loginEmpName, roleName);
	}

	Integer getAllLeadsCountMan(String startDate, String endDate, String leadType, int orgId, String dealerCode,
			List<String> loginEmpName, String roleName) {
		if (StringUtils.isEmpty(dealerCode))
			return dmsLeadDao.getAllLeadsCountMan(startDate, endDate, leadType, orgId, loginEmpName, roleName);
		else
			return dmsLeadDao.getAllLeadsCountMan(startDate, endDate, leadType, orgId, dealerCode, loginEmpName,
					roleName);
	}

	public List<VehicleModelRes> getReceptionistManagerModelData(ReceptionistDashBoardReq req, String roleName) {
		List<VehicleModelRes> resList = new ArrayList<>();

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());

		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
//		String dealerCode = req.getDealerCode()

		List<Integer> branchList = new ArrayList<>();
		try {
			branchList = getActiveDropdownsV2(req.getBranchList(), req.getOrgId(), req.getLoggedInEmpId());
		} catch (Exception e) {
			e.printStackTrace();
		}


		List<String> emps = new ArrayList<>();
		List<String> managerEmps = getEmpList(req, branchList);
		for (int i = 0; i <managerEmps.size() ; i++) {
			DMSEmployee dmsEmployee = empRepo.findemp(managerEmps.get(i),req.getOrgId());
			if(dmsEmployee.getReportingManager().getEmpId().equals(req.getLoggedInEmpId())){
				emps.add(dmsEmployee.getEmpName());
			}else{
				if(dmsEmployee.getEmpId().equals(req.getLoggedInEmpId())){
					emps.add(dmsEmployee.getEmpName());
				}else{
					if(!req.isSelf()){
						emps.add(dmsEmployee.getEmpName());
					}
				}
			}
		}



		String orgId = req.getOrgId().toString();
		Map<String, Integer> vehicalList = dashBoardUtil.getVehilceDetailsByOrgId(orgId);
		for (String model : vehicalList.keySet()) {

			if (null != model) {
				if (!emps.isEmpty()) {
					VehicleModelRes vehicleRes = new VehicleModelRes();
					log.info("Generating data for model " + model);
					List<Integer> dmsLeadList;
					if (branchList != null && branchList.size() > 0)
						dmsLeadList = dmsLeadDao.getAllEmployeeLeadsByModelMan(orgId, startDate, endDate, model, emps,
								branchList);
					else
						dmsLeadList = dmsLeadDao.getAllEmployeeLeadsByModelMan(orgId, startDate, endDate, model, emps);

					List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(startDate, endDate,
							String.valueOf(orgId), dmsLeadList);
					Long enqLeadCnt = 0L;
					Long bookCount = 0L;
					Long invoice = 0L;
					if (!dmsLeadList.isEmpty()) {
//				List<Integer> leadIdList = dmsLeadList.stream().filter(x->!x.getLeadStage().equalsIgnoreCase("DROPPED")).map(x->x.getId()).distinct().collect(Collectors.toList());
						log.debug("dmsLeadList::" + dmsLeadList);
//				List<LeadStageRefEntity> leadRefList  =  leadStageRefDao.getLeadsByIds(orgId,leadIdList)
						if (null != leadRefList && !leadRefList.isEmpty()) {
							log.debug("Total leads in LeadReF table is ::" + leadRefList.size());
							enqLeadCnt = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY"))
									.count();
							bookCount = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING"))
									.count();
							invoice = leadStageRefDao.getRetailCount(startDate, endDate, Integer.valueOf(orgId),
									dmsLeadList);
						}

						vehicleRes.setR(invoice);
						vehicleRes.setModel(model);
						vehicleRes.setE(enqLeadCnt);
						vehicleRes.setL(getDroppedLeadsCount1(startDate, endDate, Integer.parseInt(orgId), emps,
								branchList, model));
//						vehicleRes.setL(dmsLeadList.stream().filter(x->x.getLeadStage().equalsIgnoreCase("DROPPED")).count());

						vehicleRes.setB(bookCount);
						resList.add(vehicleRes);
					} else {
						Long count = getDroppedLeadsCount1(startDate, endDate, Integer.parseInt(orgId), emps,
								branchList, model);
						if (count != null && count > 0) {
							vehicleRes.setModel(model);
							vehicleRes.setL(getDroppedLeadsCount1(startDate, endDate, Integer.parseInt(orgId), emps,
									branchList, model));
							resList.add(vehicleRes);
						}
					}
				}
			}
		}
		return resList;

	}

	@Autowired
	private SubSourceRepo subSourceRepo;

	public List<SourceRes> getReceptionistManagerSourceData(ReceptionistDashBoardReq req, String roleName) {
		List<SourceRes> resList = new ArrayList<>();

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
//		String dealerCode = req.getDealerCode();

		List<Integer> branchList = new ArrayList<>();
		try {
			branchList = getActiveDropdownsV2(req.getBranchList(), req.getOrgId(), req.getLoggedInEmpId());
		} catch (Exception e) {
			e.printStackTrace();
		}

//		List<String> emps = getEmpList(req, branchList);

		List<String> emps = new ArrayList<>();
		List<String> managerEmps = getEmpList(req, branchList);
		for (int i = 0; i <managerEmps.size() ; i++) {
			DMSEmployee dmsEmployee = empRepo.findemp(managerEmps.get(i),req.getOrgId());
			if(dmsEmployee.getReportingManager().getEmpId().equals(req.getLoggedInEmpId())){
				emps.add(dmsEmployee.getEmpName());
			}else{
				if(dmsEmployee.getEmpId().equals(req.getLoggedInEmpId())){
					emps.add(dmsEmployee.getEmpName());
				}else {
					if(!req.isSelf()){
						emps.add(dmsEmployee.getEmpName());
					}
				}
			}
		}

		String orgId = req.getOrgId().toString();
		List<SubSourceId> reslist = subSourceRepo.getSources(orgId);
		Map<String, Integer> map = new LinkedHashMap<>();
		if (!emps.isEmpty()) {

			for (SubSourceId res : reslist) {
//
//			}
//				reslist.stream().forEach(res->
//		{
				SourceRes leadSource = new SourceRes();
				List<Integer> dmsLeadList;
				if (branchList != null && branchList.size() > 0)
					dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBySourceMan(orgId, startDate, endDate,
							res.getSource().getId(), res.getSubSource(), emps, branchList);
				else
					dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBySourceMan(orgId, startDate, endDate,
							res.getSource().getId(), res.getSubSource(), emps);

				List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(startDate, endDate, orgId,
						dmsLeadList);

				Long enqLeadCnt = 0L;
				Long bookCount = 0L;
				Long invoice = 0L;
				if (!dmsLeadList.isEmpty()) {

//				List<Integer> leadIdList = dmsLeadList.stream()
//						.filter(x -> !x.getLeadStage().equalsIgnoreCase("DROPPED")).map(x -> x.getId()).distinct()
//						.collect(Collectors.toList());
					log.debug("dmsLeadList::" + dmsLeadList);
//				List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(orgId, leadIdList);
					if (null != leadRefList && !leadRefList.isEmpty()) {
						log.debug("Total leads in LeadReF table is ::" + leadRefList.size());

						enqLeadCnt = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY"))
								.count();
						bookCount = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING"))
								.count();
						invoice = leadStageRefDao.getRetailCount(startDate, endDate, Integer.valueOf(orgId),
								dmsLeadList);

					}

					leadSource.setR(invoice);

					leadSource.setSubsource(res.getSubSource());
					leadSource.setSource(res.getSource().getName());
					leadSource.setE(enqLeadCnt);
					if (branchList != null && branchList.size() > 0)
						leadSource.setL(dmsLeadDao.getAllEmployeeLeadsBySourceManLost(orgId, startDate, endDate,
								res.getSource().getId(), res.getSubSource(), emps, branchList));
					else
						leadSource.setL(dmsLeadDao.getAllEmployeeLeadsBySourceManLost(orgId, startDate, endDate,
								res.getSource().getId(), res.getSubSource(), emps));

//					leadSource.setL(dmsLeadList.stream().filter(x -> x.getLeadStage().equalsIgnoreCase("DROPPED")).count());
					leadSource.setB(bookCount);
					resList.add(leadSource);
				} else {

					leadSource.setSubsource(res.getSubSource());
					leadSource.setSource(res.getSource().getName());
					if (branchList != null && branchList.size() > 0)
						leadSource.setL(dmsLeadDao.getAllEmployeeLeadsBySourceManLost(orgId, startDate, endDate,
								res.getSource().getId(), res.getSubSource(), emps, branchList));
					else
						leadSource.setL(dmsLeadDao.getAllEmployeeLeadsBySourceManLost(orgId, startDate, endDate,
								res.getSource().getId(), res.getSubSource(), emps));

					if (leadSource.getL() != null && leadSource.getL() > 0) {
						resList.add(leadSource);
					}
				}
			}
		}

		return resList;

	}

	@Autowired
	private EmpRepo empRepo;

	public Map getTeamCount(ReceptionistDashBoardReq req, String roleName) {

		Map map = new HashMap();
		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
		int orgId = req.getOrgId();
		String dealerCode = req.getDealerCode();
		List<String> emps;

		List<Integer> branchlist = req.getBranchList();
		if (branchlist == null) {
			emps = dmsEmployeeRepo.findemps(req.getLoggedInEmpId());
			if (req.getLoggedInEmpId() != null) {
				emps.add(dmsEmployeeRepo.getEmpName(Integer.toString(req.getLoggedInEmpId())));
			}
		} else {
			emps = dmsEmployeeRepo.findempsByBranch1(req.getBranchList());
			if (req.getLoggedInEmpId() != null) {
				emps.add(dmsEmployeeRepo.getEmpName(Integer.toString(req.getLoggedInEmpId())));
			}
		}

//		List<String> dmsEmployeeList = dmsEmployeeRepo.findEmpNames(orgId);

		List consultantList = new ArrayList();

		for (String one : emps) {
			Map m = new HashMap();
			m.put("empName", one);
			DMSEmployee dms = empRepo.findemp(one,req.getOrgId());
			m.put("branch", dms.getDmsBranch().getName());
			m.put("roleName", dms.getDmsRole().getRoleName());
			// .collect(Collectors.toList()
			List<DmsLead> stageCounts = dmsLeadDao.getAllocatedLeadsCountData(startDate, endDate, orgId, one);
			List<Integer> leadList = stageCounts.stream().map(DmsLead::getId).collect(Collectors.toList());
			List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(startDate, endDate,
					String.valueOf(orgId), leadList);
//		m.put("totalAllocatedCount" , getAllocatedLeadsCount(startDate, endDate, orgId, dealerCode, one, roleName));
			m.put("totalAllocatedCount",
					leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY")).count());

			m.put("totalDroppedCount", getDroppedLeadsCount(startDate, endDate, orgId, dealerCode, one, roleName));

			m.put("bookingCount",
					leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING")).count());

			m.put("RetailCount", leadStageRefDao.getRetailCount(startDate, endDate, orgId, leadList));
			consultantList.add(m);
		}
		map.put("empName", consultantList);
		return map;
	}

	public Map getTeamCountReceptionist(ReceptionistDashBoardReq req, String roleName) {

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());

		Map map = new HashMap();
		String startDate = getStartDate(req.getStartDate()) + " 00:00:00";
		String endDate = getEndDate(req.getEndDate()) + " 23:59:59";
		int orgId = req.getOrgId();
		String dealerCode = req.getDealerCode();
		List<String> emps;

		List<Integer> branchList = new ArrayList<>();
		try {
			branchList = getActiveDropdownsV2(req.getBranchList(), req.getOrgId(), req.getLoggedInEmpId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		DMSEmployee dmsEmp = empRepo.findemp(dmsEmployeeObj.getEmpName(),req.getOrgId());
		String loginUserRole = dmsEmp.getDmsRole().getRoleName();

		if (req.getEmpList() != null && req.getEmpList().size() > 0) {
			emps = dmsEmployeeRepo.findemps(req.getEmpList());
		} else {
			emps = getEmpList(req, branchList);
		}

		List<String> managerEmps = new ArrayList<>();
		List<String> creEmps = new ArrayList<>();
		for (int i = 0; i <emps.size() ; i++) {
			DMSEmployee dmsEmployee = empRepo.findemp(emps.get(i),req.getOrgId());
			if(dmsEmployee.getReportingManager().getEmpId().equals(req.getLoggedInEmpId())){
				managerEmps.add(dmsEmployee.getEmpName());
			}else{
				if(dmsEmployee.getEmpId().equals(req.getLoggedInEmpId())){
					managerEmps.add(dmsEmployee.getEmpName());
				}else {
					creEmps.add(dmsEmployee.getEmpName());
				}
			}
		}



		List<HashMap> consultantList = new ArrayList();
		int preInquiryCount = 0;
		int managerPreInquiryCount = 0;

		for (String one : managerEmps) {
			HashMap m = new HashMap();
			m.put("emp_name", one);

			List<Integer> userId = dmsEmployeeRepo.findEmpIdsByNames(one, orgId);
			m.put("emp_id", userId.get(0));

			DMSEmployee dms = empRepo.findemp(one,req.getOrgId());
			m.put("branch", dms.getDmsBranch().getName());
			m.put("roleName", dms.getDmsRole().getRoleName());
			// .collect(Collectors.toList()
			List<DmsLead> stageCounts = new ArrayList<>();
			if (branchList != null && branchList.size() > 0) {
				stageCounts = dmsLeadDao.getAllocatedLeadsCountData(startDate, endDate, orgId, one, branchList);
			} else {
				stageCounts = dmsLeadDao.getAllocatedLeadsCountData(startDate, endDate, orgId, one);
			}

			List<Integer> leadList = stageCounts.stream().map(DmsLead::getId).collect(Collectors.toList());
			List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(startDate, endDate,
					String.valueOf(orgId), leadList);
			m.put("enquiryCount",
					leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY")).count());

			m.put("droppedCount", getDroppedLeadsCount1(startDate, endDate, orgId, branchList, one, roleName));

			m.put("bookingCount",
					leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING")).count());

			m.put("retailCount", leadStageRefDao.getRetailCount(startDate, endDate, orgId, leadList));
			consultantList.add(m);


			List<DmsLead> stageCounts1 = new ArrayList<>();
			if (branchList != null && branchList.size() > 0) {
				stageCounts1 = dmsLeadDao.getAllocatedLeadsCountDataContact(startDate, endDate, orgId, one, branchList);
			} else {
				stageCounts1 = dmsLeadDao.getAllocatedLeadsCountDataContact(startDate, endDate, orgId, one);
			}
			List<Integer> leadList1 = stageCounts1.stream().map(DmsLead::getId).collect(Collectors.toList());
			List<LeadStageRefEntity> leadRefList1 = leadStageRefDao.getLeadsByIds(startDate, endDate,
					String.valueOf(orgId), leadList1);
			preInquiryCount += leadRefList1.stream().filter(x -> x.getStageName().equalsIgnoreCase("PREENQUIRY"))
					.count();

			managerPreInquiryCount += leadRefList1.stream().filter(x -> x.getStageName().equalsIgnoreCase("PREENQUIRY"))
					.count();

			List<HashMap> consultantList1 = new ArrayList();
			if (branchList != null && branchList.size() > 0) {
				req.setDealerCodes(null);
				req.setDealerCodes(dmsBranchDao.getDealerCodeName(branchList));
				;
			}
			req.setLoggedInEmpId(userId.get(0));
			ArrayList<Map> salesConsltantData = (ArrayList<Map>) getReceptionistData(req, "").get("consultantList");
			for (int i = 0; i < salesConsltantData.size(); i++) {
				HashMap m1 = new HashMap();
				m1.put("enquiryCount", salesConsltantData.get(i).get("enquiryCount"));
				m1.put("droppedCount", salesConsltantData.get(i).get("droppedCount"));
				m1.put("bookingCount", salesConsltantData.get(i).get("bookingCount"));
				m1.put("retailCount", salesConsltantData.get(i).get("retailCount"));
				m1.put("emp_name", salesConsltantData.get(i).get("emp_name"));
				m1.put("emp_id", salesConsltantData.get(i).get("emp_id"));
				m1.put("branch", salesConsltantData.get(i).get("branch"));
				m1.put("roleName", salesConsltantData.get(i).get("roleName"));
				consultantList1.add(m1);
			}
			m.put("salesconsultant", consultantList1);

		}
		map.put("manager", consultantList);

		List<HashMap> consultantList2 = new ArrayList();
		if(roleName.equalsIgnoreCase("crmuser")) {
			for (String one : creEmps) {
				HashMap m = new HashMap();
				m.put("emp_name", one);

				List<Integer> userId = dmsEmployeeRepo.findEmpIdsByNames(one, orgId);
				m.put("emp_id", userId.get(0));

				DMSEmployee dms = empRepo.findemp(one,req.getOrgId());
				m.put("branch", dms.getDmsBranch().getName());
				m.put("roleName", dms.getDmsRole().getRoleName());
				// .collect(Collectors.toList()
				List<DmsLead> stageCounts = new ArrayList<>();
				if (branchList != null && branchList.size() > 0) {
					stageCounts = dmsLeadDao.getAllocatedLeadsCountData(startDate, endDate, orgId, one, branchList);
				} else {
					stageCounts = dmsLeadDao.getAllocatedLeadsCountData(startDate, endDate, orgId, one);
				}

				List<Integer> leadList = stageCounts.stream().map(DmsLead::getId).collect(Collectors.toList());
				List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(startDate, endDate,
						String.valueOf(orgId), leadList);
				m.put("enquiryCount",
						leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY")).count());

				m.put("droppedCount", getDroppedLeadsCount1(startDate, endDate, orgId, branchList, one, roleName));

				m.put("bookingCount",
						leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING")).count());

				m.put("retailCount", leadStageRefDao.getRetailCount(startDate, endDate, orgId, leadList));
				consultantList2.add(m);


				List<DmsLead> stageCounts1 = new ArrayList<>();
				if (branchList != null && branchList.size() > 0) {
					stageCounts1 = dmsLeadDao.getAllocatedLeadsCountDataContact(startDate, endDate, orgId, one, branchList);
				} else {
					stageCounts1 = dmsLeadDao.getAllocatedLeadsCountDataContact(startDate, endDate, orgId, one);
				}
				List<Integer> leadList1 = stageCounts1.stream().map(DmsLead::getId).collect(Collectors.toList());
				List<LeadStageRefEntity> leadRefList1 = leadStageRefDao.getLeadsByIds(startDate, endDate,
						String.valueOf(orgId), leadList1);
				preInquiryCount += leadRefList1.stream().filter(x -> x.getStageName().equalsIgnoreCase("PREENQUIRY"))
						.count();

				List<HashMap> consultantList1 = new ArrayList();
				if (branchList != null && branchList.size() > 0) {
					req.setDealerCodes(null);
					req.setDealerCodes(dmsBranchDao.getDealerCodeName(branchList));
					;
				}
				req.setLoggedInEmpId(userId.get(0));
				ArrayList<Map> salesConsltantData = (ArrayList<Map>) getReceptionistData(req, "").get("consultantList");
				for (int i = 0; i < salesConsltantData.size(); i++) {
					HashMap m1 = new HashMap();
					m1.put("enquiryCount", salesConsltantData.get(i).get("enquiryCount"));
					m1.put("droppedCount", salesConsltantData.get(i).get("droppedCount"));
					m1.put("bookingCount", salesConsltantData.get(i).get("bookingCount"));
					m1.put("retailCount", salesConsltantData.get(i).get("retailCount"));
					m1.put("emp_name", salesConsltantData.get(i).get("emp_name"));
					m1.put("emp_id", salesConsltantData.get(i).get("emp_id"));
					m1.put("branch", salesConsltantData.get(i).get("branch"));
					m1.put("roleName", salesConsltantData.get(i).get("roleName"));
					consultantList1.add(m1);
				}
				m.put("salesconsultant", consultantList1);

			}
			map.put("CRE", consultantList2);
		}

		long retailCount = 0;
		Integer totalLostCount = 0;
		long bookingCount = 0;
		long totalEnquiryCount = 0;

		long managerRetailCount = 0;
		Integer managerLostCount = 0;
		long managerBookingCount = 0;
		long managerEnquiryCount = 0;

		for (int i = 0; i < consultantList.size(); i++) {
			retailCount += (Long) consultantList.get(i).get("retailCount");
			totalLostCount += (Integer) consultantList.get(i).get("droppedCount");
			bookingCount += (Long) consultantList.get(i).get("bookingCount");
			totalEnquiryCount += (Long) consultantList.get(i).get("enquiryCount");

			managerRetailCount += (Long) consultantList.get(i).get("retailCount");
			managerLostCount += (Integer) consultantList.get(i).get("droppedCount");
			managerBookingCount += (Long) consultantList.get(i).get("bookingCount");
			managerEnquiryCount += (Long) consultantList.get(i).get("enquiryCount");
		}

		if(roleName.equalsIgnoreCase("crmuser")) {
			for (int i = 0; i < consultantList2.size(); i++) {
				retailCount += (Long) consultantList2.get(i).get("retailCount");
				totalLostCount += (Integer) consultantList2.get(i).get("droppedCount");
				bookingCount += (Long) consultantList2.get(i).get("bookingCount");
				totalEnquiryCount += (Long) consultantList2.get(i).get("enquiryCount");
			}
		}

		map.put("totalRetailCount", retailCount);
		map.put("totalLostCount", totalLostCount);
		map.put("totalBookingCount", bookingCount);
		map.put("totalEnquiryCount", totalEnquiryCount);


		if(roleName.equalsIgnoreCase("crmuser")) {
			map.put("managerRetailCount", managerRetailCount);
			map.put("managerLostCount", managerLostCount);
			map.put("managerBookingCount", managerBookingCount);
			map.put("managerEnquiryCount", managerEnquiryCount);
		}

		map.put("childUserCount", consultantList.size());

		Integer totalDroppedCount = 0;
		if (branchList != null && branchList.size() > 0) {
			totalDroppedCount = dmsLeadDao.getTotaContactDrops(emps, startDate, endDate, branchList,
					Integer.valueOf(orgId));
		} else {
			totalDroppedCount = dmsLeadDao.getTotaContactDrops(emps, startDate, endDate, Integer.valueOf(orgId));
		}
		map.put("totalDroppedCount", totalDroppedCount);
		map.put("totalPreInquiryCount", preInquiryCount);
		map.put("managerPreInquiryCount", managerPreInquiryCount);


		Integer managerDroppedCount = 0;
		if (branchList != null && branchList.size() > 0) {
			managerDroppedCount = dmsLeadDao.getTotaContactDrops(managerEmps, startDate, endDate, branchList,
					Integer.valueOf(orgId));
		} else {
			managerDroppedCount = dmsLeadDao.getTotaContactDrops(managerEmps, startDate, endDate, Integer.valueOf(orgId));
		}
		map.put("managerDroppedCount", managerDroppedCount);

		return map;
	}

	Integer getDroppedLeadsCount1(String startDate, String endDate, int orgId, List<Integer> dealerCode,
			String loginEmpName, String roleName) {
		if (dealerCode != null && dealerCode.size() > 0)
			return dmsLeadDao.getDroppedLeadsCountt1(startDate, endDate, orgId, loginEmpName, dealerCode);
		else
			return dmsLeadDao.getDroppedLeadsCountt1(startDate, endDate, orgId, loginEmpName);
	}

	Long getDroppedLeadsCount1(String startDate, String endDate, int orgId, List<String> empIds,
			List<Integer> dealerCode, String model) {
		if (dealerCode != null && dealerCode.size() > 0)
			return dmsLeadDao.getDroppedLeadsCountt1(startDate, endDate, orgId, empIds, dealerCode, model);
		else
			return dmsLeadDao.getDroppedLeadsCountt1(startDate, endDate, orgId, empIds, model);
	}

	public List<Integer> getActiveDropdownsV2(List<Integer> levelList, Integer orgId, Integer empId)
			throws DynamicFormsServiceException {
		List<LocationNodeData> list = new ArrayList<>();
		List<Integer> branchIdList = new ArrayList<>();
		List<Integer> reqNodeIds = new ArrayList<>();
		Map<String, Object> resMap = new LinkedHashMap<>();
		try {
			for (Integer nodeId : levelList) {
				log.debug("nodeId::::" + nodeId);
				String levelName = locationNodeDataDao.getLevelname(nodeId);
				log.debug("Given node level " + levelName + " and nodeId " + nodeId);
				List<String> levels = getOrgLevels(orgId).stream().map(x -> x.getLocationNodeDefType())
						.collect(Collectors.toList());
				log.debug("levels before " + levels);
				log.debug("levelName::" + levelName);
				levels = levels.subList(levels.indexOf(levelName), levels.size());
				levels = levels.stream().sorted().collect(Collectors.toList());
				log.debug("levels after " + levels);
				Map<String, List<Integer>> levelIdmap = new LinkedHashMap<>();
				for (int i = 0; i < levels.size(); i++) {
					String level = levels.get(i);
					if (i == 0) {
						List<Integer> tmp = new ArrayList<>();
						tmp.add(nodeId);
						levelIdmap.put(level, tmp);
					} else {
						if (!levelIdmap.isEmpty()) {
							String previousLevel = getPreviousLevel(level);
							log.debug("level " + level + ",previousLevel:" + previousLevel);
							if (levelIdmap.containsKey(previousLevel)) {
								log.debug("LevelIdMap contains previous level " + level);
								List<LocationNodeData> nodeData = locationNodeDataDao.getNodeDataByParentId(orgId,
										level, levelIdmap.get(previousLevel));
								List<Integer> idLists = nodeData.stream().map(x -> x.getId())
										.collect(Collectors.toList());
								levelIdmap.put(level, idLists);
								list.addAll(nodeData);
							}
						}

					}
				}
				log.debug("levelIdmap ::" + levelIdmap);

				levelIdmap.forEach((k, v) -> {
					reqNodeIds.addAll(v);
				});
			}

			log.debug("reqNodeIds:::" + reqNodeIds);
			List<LocationNodeDataV2> activeParentNodeList = ObjectMapperUtils
					.mapAll(locationNodeDataDao.getActiveLevelsForEmpWithOutOrg(empId), LocationNodeDataV2.class);
			List<String> levels = activeParentNodeList.stream().map(x -> x.getType()).collect(Collectors.toList());
			levels = levels.stream().sorted().collect(Collectors.toList());
			log.debug("USER LEVELS " + levels + " and size:" + levels.size());
			String leafLevel = null;
			if (null != levels && levels.size() > 0) {
				leafLevel = levels.get(levels.size() - 1);
				log.debug("leafLevel:::" + leafLevel);
			}
			List<LocationNodeData> leafNodeList = locationNodeDataDao.getNodeDataByLevel(orgId, leafLevel);

			List<Integer> leafNodeIdList = leafNodeList.stream().map(x -> x.getId()).collect(Collectors.toList());
			List<Integer> finalleafNodeIdList1 = new ArrayList<>();
			for (Integer i : reqNodeIds) {
				if (leafNodeIdList.contains(i)) {
					finalleafNodeIdList1.add(i);
				}
			}
			List<Integer> finalleafNodeIdList2 = new ArrayList<>();
			for (Integer i : finalleafNodeIdList1) {
				if (levelList.contains(i)) {
					finalleafNodeIdList2.add(i);
				}
			}
			List<Integer> finalleafNodeIdList = new ArrayList<>(new HashSet<Integer>(finalleafNodeIdList2));
			log.debug("finalleafNodeIdList::" + finalleafNodeIdList);

			for (Integer nodeId : finalleafNodeIdList) {
				log.debug("Getting data for nodeId::" + nodeId);
				DmsBranch branch = dmsBranchDao.getBranchByOrgMpId(nodeId);
				int branchId = branch.getBranchId();
				branchIdList.add(branch.getBranchId());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return branchIdList;
	}

	public List<LocationNodeDef> getOrgLevels(Integer orgId) throws DynamicFormsServiceException {
		List<LocationNodeDef> list = new ArrayList<>();
		try {
			list = locationNodeDefDao.getLevelByOrgID(orgId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return list;
	}

	private String getPreviousLevel(String level) {

		if (level.equalsIgnoreCase("Level2")) {
			return "Level1";
		}

		if (level.equalsIgnoreCase("Level3")) {
			return "Level2";
		}

		if (level.equalsIgnoreCase("Level4")) {
			return "Level3";
		}

		if (level.equalsIgnoreCase("Level5")) {
			return "Level4";
		}
		if (level.equalsIgnoreCase("Level6")) {
			return "Level5";
		}
		if (level.equalsIgnoreCase("Level7")) {
			return "Level6";
		}
		if (level.equalsIgnoreCase("Level8")) {
			return "Level7";
		}
		if (level.equalsIgnoreCase("Level9")) {
			return "Level8";
		}
		return null;
	}

	public Map getSalesManagerDigitalTeam(ReceptionistDashBoardReq req, String roleName) {

		Map map = new HashMap();
		String startDate = getStartDate(req.getStartDate()) + " 00:00:00";
		String endDate = getEndDate(req.getEndDate()) + " 23:59:59";
		int orgId = req.getOrgId();

		List<String> emps;

		List<Integer> branchList = new ArrayList<>();
		try {
			branchList = getActiveDropdownsV2(req.getBranchList(), req.getOrgId(), req.getLoggedInEmpId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		int totalPreInquiryCount = 0;

// -------------------------------------------- For the child------------------------------------------------------

		if (req.getEmpList() != null && req.getEmpList().size() > 0) {
			emps = dmsEmployeeRepo.findemps(req.getEmpList());
		} else {
			emps = getEmpListSalesManager(req, branchList);
		}

		List<String> CRMUsers = new ArrayList<>();
		List<String> CREUsers = new ArrayList<>();

		if(req.getDashboardType() != null && req.getDashboardType().equalsIgnoreCase("reception")){
			for (int i = 0; i <emps.size() ; i++) {
				DMSEmployee dms1 = empRepo.findemp(emps.get(i),req.getOrgId());
				if(dms1.getDmsRole().getRoleName().equalsIgnoreCase("receptionist Manager") ||
						dms1.getDmsRole().getRoleName().equalsIgnoreCase("reception Manager")){
					CRMUsers.add(emps.get(i));
				}

			}
		}else {
			for (int i = 0; i <emps.size() ; i++) {
				DMSEmployee dms1 = empRepo.findemp(emps.get(i),req.getOrgId());
				if(dms1.getDmsRole().getRoleName().equalsIgnoreCase("CRM") ||
						dms1.getDmsRole().getRoleName().equalsIgnoreCase("Tele caller Manager")||
						dms1.getDmsRole().getRoleName().equalsIgnoreCase("Telecaller Manager")){
					CRMUsers.add(emps.get(i));
				}

			}
		}


		for (int i = 0; i <emps.size() ; i++) {
			DMSEmployee dms1 = empRepo.findemp(emps.get(i),req.getOrgId());
			if(!CRMUsers.contains(dms1.getReportingManager().getEmpName()) && !CRMUsers.contains(dms1.getEmpName())){
				CREUsers.add(emps.get(i));
			}
		}


		List<HashMap> consultantListReceptionist = new ArrayList();
		List<HashMap> consultantListCRM = new ArrayList();
		for (String one : CRMUsers) {
			HashMap receptionistMap = new HashMap();
			HashMap CRMMap = new HashMap();
			List<Integer> userId1 = dmsEmployeeRepo.findEmpIdsByNames(one, orgId);
			DMSEmployee dms1 = empRepo.findemp(one,req.getOrgId());


				CRMMap.put("emp_name", one);
				CRMMap.put("emp_id", userId1.get(0));
				CRMMap.put("branch", dms1.getDmsBranch().getName());
				CRMMap.put("roleName", dms1.getDmsRole().getRoleName());
				req.setLoggedInEmpId(userId1.get(0));
				CRMMap.put("data", getTeamCountReceptionist(req, ""));
				consultantListCRM.add(CRMMap);

		}


		for (String one : CREUsers) {
			HashMap receptionistMap = new HashMap();
			HashMap CRMMap = new HashMap();
			List<Integer> userId1 = dmsEmployeeRepo.findEmpIdsByNames(one, orgId);
			DMSEmployee dms1 = empRepo.findemp(one,req.getOrgId());

			receptionistMap.put("emp_name", one);
			receptionistMap.put("emp_id", userId1.get(0));
			receptionistMap.put("branch", dms1.getDmsBranch().getName());
			receptionistMap.put("roleName", dms1.getDmsRole().getRoleName());

			List<HashMap> consultantList2 = new ArrayList();
			if (branchList != null && branchList.size() > 0) {
				req.setDealerCodes(null);
				req.setDealerCodes(dmsBranchDao.getDealerCodeName(branchList));
			}
			req.setLoggedInEmpId(userId1.get(0));
			receptionistMap.put("data", getReceptionistData(req, ""));
			consultantListReceptionist.add(receptionistMap);

		}

//      Total calculation of data

		long retailCount = 0;
		Integer totalLostCount = 0;
		long bookingCount = 0;
		long totalEnquiryCount = 0;

		for (int i = 0; i < consultantListReceptionist.size(); i++) {
			HashMap data = (HashMap) consultantListReceptionist.get(i).get("data");
			retailCount += (Long) data.get("RetailCount");
			totalLostCount += (Integer) data.get("totalLostCount");
			bookingCount += (Long) data.get("bookingsCount");
			totalEnquiryCount += (Long) data.get("enquirysCount");
			totalPreInquiryCount += (Long) data.get("contactsCount");
		}

		for (int i = 0; i < consultantListCRM.size(); i++) {
			HashMap data = (HashMap) consultantListCRM.get(i).get("data");
			retailCount += (Long) data.get("totalRetailCount");
			totalLostCount += (Integer) data.get("totalLostCount");
			bookingCount += (Long) data.get("totalBookingCount");
			totalEnquiryCount += (Long) data.get("totalEnquiryCount");
			totalPreInquiryCount += Long.valueOf((Integer) data.get("totalPreInquiryCount"));

		}


		map.put("totalRetailCount", retailCount);
		map.put("totalLostCount", totalLostCount);
		map.put("totalBookingCount", bookingCount);
		map.put("totalEnquiryCount", totalEnquiryCount);
		map.put("childUserCount", consultantListReceptionist.size() + consultantListCRM.size());

		Integer totalDroppedCount = 0;
//		emps.add(empName);
		if (branchList != null && branchList.size() > 0) {
			totalDroppedCount = dmsLeadDao.getTotaContactDrops(emps, startDate, endDate, branchList,
					Integer.valueOf(orgId));
		} else {
			totalDroppedCount = dmsLeadDao.getTotaContactDrops(emps, startDate, endDate, Integer.valueOf(orgId));
		}
		map.put("totalDroppedCount", totalDroppedCount);
		map.put("totalPreInquiryCount", totalPreInquiryCount);

		map.put("CRE", consultantListReceptionist);
		map.put("CRM", consultantListCRM);

		return map;

	}

	private List<String> getEmpList(ReceptionistDashBoardReq req,List<Integer> branchList){
		if(branchList == null || branchList.size() == 0){
			branchList = locationNodeDataDao.getBranchList(req.getOrgId(),req.getLoggedInEmpId());
		}
		List<String> emps = new ArrayList<>();
		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		DMSEmployee dmsEmp = empRepo.findemp(dmsEmployeeObj.getEmpName(),req.getOrgId());
		String loginUserRole = dmsEmp.getDmsRole().getRoleName();
		if (req.getDashboardType() != null && req.getDashboardType().equalsIgnoreCase("reception")) {
			emps = dmsEmployeeRepo.findReceptionByBranch(req.getOrgId(),branchList);
			if(loginUserRole.equalsIgnoreCase("receptionist Manager") ||
					loginUserRole.equalsIgnoreCase("reception Manager") ){
				emps.add(dmsEmployeeRepo.getEmpName(Integer.toString(req.getLoggedInEmpId())));
			}
		}else {
			emps = dmsEmployeeRepo.findCreAndTellecallerByBranch(req.getOrgId(),branchList);
			if(loginUserRole.equalsIgnoreCase("CRM") ||
					loginUserRole.equalsIgnoreCase("Tele caller Manager") ||
					loginUserRole.equalsIgnoreCase("Telecaller Manager")){
				emps.add(dmsEmployeeRepo.getEmpName(Integer.toString(req.getLoggedInEmpId())));
			}
		}
		return emps;
	}


	private List<String> getEmpListSalesManager(ReceptionistDashBoardReq req, List<Integer> branchList) {
		List<String> emps = new ArrayList<>();
		if (branchList == null || branchList.size() == 0) {
			branchList = locationNodeDataDao.getBranchList(req.getOrgId(), req.getLoggedInEmpId());
		}
		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		DMSEmployee dmsEmp = empRepo.findemp(dmsEmployeeObj.getEmpName(),req.getOrgId());
		if (req.getDashboardType() != null && req.getDashboardType().equalsIgnoreCase("reception")) {
			emps = dmsEmployeeRepo.findReceptionByBranchSales(req.getOrgId(), branchList);
		} else {
			emps = dmsEmployeeRepo.findCreAndTellecallerByBranchSales(req.getOrgId(), branchList);
		}
		return emps;
	}

	@Override
	public List<SourceRes> getXRoleSourceData(ReceptionistDashBoardReq req, String roleName) {
		List<SourceRes> resList = new ArrayList<>();

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());
		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
//		String dealerCode = req.getDealerCode();

		List<Integer> branchList = new ArrayList<>();
		try {
			branchList = getActiveDropdownsV2(req.getBranchList(), req.getOrgId(), req.getLoggedInEmpId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<String> emps = getEmpListSalesManager(req, branchList);

		String orgId = req.getOrgId().toString();
		List<SubSourceId> reslist = subSourceRepo.getSources(orgId);
		Map<String, Integer> map = new LinkedHashMap<>();
		if (!emps.isEmpty()) {

			for (SubSourceId res : reslist) {
				SourceRes leadSource = new SourceRes();
				List<Integer> dmsLeadList;
				if (branchList != null && branchList.size() > 0)
					dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBySourceMan(orgId, startDate, endDate,
							res.getSource().getId(), res.getSubSource(), emps, branchList);
				else
					dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBySourceMan(orgId, startDate, endDate,
							res.getSource().getId(), res.getSubSource(), emps);

				List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(startDate, endDate, orgId,
						dmsLeadList);

				Long enqLeadCnt = 0L;
				Long bookCount = 0L;
				Long invoice = 0L;
				if (!dmsLeadList.isEmpty()) {

//				List<Integer> leadIdList = dmsLeadList.stream()
//						.filter(x -> !x.getLeadStage().equalsIgnoreCase("DROPPED")).map(x -> x.getId()).distinct()
//						.collect(Collectors.toList());
					log.debug("dmsLeadList::" + dmsLeadList);
//				List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(orgId, leadIdList);
					if (null != leadRefList && !leadRefList.isEmpty()) {
						log.debug("Total leads in LeadReF table is ::" + leadRefList.size());

						enqLeadCnt = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY"))
								.count();
						bookCount = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING"))
								.count();
						invoice = leadStageRefDao.getRetailCount(startDate, endDate, Integer.valueOf(orgId),
								dmsLeadList);

					}

					leadSource.setR(invoice);

					leadSource.setSubsource(res.getSubSource());
					leadSource.setSource(res.getSource().getName());
					leadSource.setE(enqLeadCnt);
					if (branchList != null && branchList.size() > 0)
						leadSource.setL(dmsLeadDao.getAllEmployeeLeadsBySourceManLost(orgId, startDate, endDate,
								res.getSource().getId(), res.getSubSource(), emps, branchList));
					else
						leadSource.setL(dmsLeadDao.getAllEmployeeLeadsBySourceManLost(orgId, startDate, endDate,
								res.getSource().getId(), res.getSubSource(), emps));

//					leadSource.setL(dmsLeadList.stream().filter(x -> x.getLeadStage().equalsIgnoreCase("DROPPED")).count());
					leadSource.setB(bookCount);
					resList.add(leadSource);
				} else {

					leadSource.setSubsource(res.getSubSource());
					leadSource.setSource(res.getSource().getName());
					if (branchList != null && branchList.size() > 0)
						leadSource.setL(dmsLeadDao.getAllEmployeeLeadsBySourceManLost(orgId, startDate, endDate,
								res.getSource().getId(), res.getSubSource(), emps, branchList));
					else
						leadSource.setL(dmsLeadDao.getAllEmployeeLeadsBySourceManLost(orgId, startDate, endDate,
								res.getSource().getId(), res.getSubSource(), emps));

					if (leadSource.getL() != null && leadSource.getL() > 0) {
						resList.add(leadSource);
					}
				}
			}
		}

		return resList;

	}

	@Override
	public List<VehicleModelRes> getXRoleModelData(ReceptionistDashBoardReq req, String roleName) {
		List<VehicleModelRes> resList = new ArrayList<>();

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());

		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());

		List<Integer> branchList = new ArrayList<>();
		try {
			branchList = getActiveDropdownsV2(req.getBranchList(), req.getOrgId(), req.getLoggedInEmpId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<String> emps = getEmpListSalesManager(req, branchList);

		String orgId = req.getOrgId().toString();
		Map<String, Integer> vehicalList = dashBoardUtil.getVehilceDetailsByOrgId(orgId);
		for (String model : vehicalList.keySet()) {

			if (null != model) {
				if (!emps.isEmpty()) {
					VehicleModelRes vehicleRes = new VehicleModelRes();
					log.info("Generating data for model " + model);
					List<Integer> dmsLeadList;
					if (branchList != null && branchList.size() > 0)
						dmsLeadList = dmsLeadDao.getAllEmployeeLeadsByModelMan(orgId, startDate, endDate, model, emps,
								branchList);
					else
						dmsLeadList = dmsLeadDao.getAllEmployeeLeadsByModelMan(orgId, startDate, endDate, model, emps);

					List<LeadStageRefEntity> leadRefList = leadStageRefDao.getLeadsByIds(startDate, endDate,
							String.valueOf(orgId), dmsLeadList);
					Long enqLeadCnt = 0L;
					Long bookCount = 0L;
					Long invoice = 0L;
					if (!dmsLeadList.isEmpty()) {
//				List<Integer> leadIdList = dmsLeadList.stream().filter(x->!x.getLeadStage().equalsIgnoreCase("DROPPED")).map(x->x.getId()).distinct().collect(Collectors.toList());
						log.debug("dmsLeadList::" + dmsLeadList);
//				List<LeadStageRefEntity> leadRefList  =  leadStageRefDao.getLeadsByIds(orgId,leadIdList)
						if (null != leadRefList && !leadRefList.isEmpty()) {
							log.debug("Total leads in LeadReF table is ::" + leadRefList.size());
							enqLeadCnt = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("ENQUIRY"))
									.count();
							bookCount = leadRefList.stream().filter(x -> x.getStageName().equalsIgnoreCase("BOOKING"))
									.count();
							invoice = leadStageRefDao.getRetailCount(startDate, endDate, Integer.valueOf(orgId),
									dmsLeadList);
						}

						vehicleRes.setR(invoice);
						vehicleRes.setModel(model);
						vehicleRes.setE(enqLeadCnt);
						vehicleRes.setL(getDroppedLeadsCount1(startDate, endDate, Integer.parseInt(orgId), emps,
								branchList, model));
//						vehicleRes.setL(dmsLeadList.stream().filter(x->x.getLeadStage().equalsIgnoreCase("DROPPED")).count());

						vehicleRes.setB(bookCount);
						resList.add(vehicleRes);
					} else {
						Long count = getDroppedLeadsCount1(startDate, endDate, Integer.parseInt(orgId), emps,
								branchList, model);
						if (count != null && count > 0) {
							vehicleRes.setModel(model);
							vehicleRes.setL(getDroppedLeadsCount1(startDate, endDate, Integer.parseInt(orgId), emps,
									branchList, model));
							resList.add(vehicleRes);
						}
					}
				}
			}
		}
		return resList;

	}

	@Override
	public Map getReceptionistFilterData(ReceptionistDashBoardReq req, String roleName) {

		DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(req.getLoggedInEmpId());

		DMSEmployee dmsEmp = empRepo.findemp(dmsEmployeeObj.getEmpName(),req.getOrgId());
		String loginUserRole = dmsEmp.getDmsRole().getRoleName();
		Map map = new HashMap();

		List<Integer> branchList = new ArrayList<>();
		try {
			branchList = getActiveDropdownsV2(req.getBranchList(), req.getOrgId(), req.getLoggedInEmpId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
		int orgId = req.getOrgId();
		List<String> emps;

		if (req.getEmpList() != null && req.getEmpList().size() > 0) {
			emps = dmsEmployeeRepo.findemps(req.getEmpList());
		} else {
			emps = getEmpList(req, branchList);
		}

		List<String> managerEmps = new ArrayList<>();
		List<String> creEmps = new ArrayList<>();
		for (int i = 0; i <emps.size() ; i++) {
			DMSEmployee dmsEmployee = empRepo.findemp(emps.get(i),req.getOrgId());
			if(dmsEmployee.getReportingManager().getEmpId().equals(req.getLoggedInEmpId())){
				managerEmps.add(dmsEmployee.getEmpName());
			}else{
				if(dmsEmployee.getEmpId().equals(req.getLoggedInEmpId())){
					managerEmps.add(dmsEmployee.getEmpName());
				}else {
					creEmps.add(dmsEmployee.getEmpName());
				}
			}
		}

		if(loginUserRole.equalsIgnoreCase("receptionist Manager") ||
				loginUserRole.equalsIgnoreCase("reception Manager") ||
				loginUserRole.equalsIgnoreCase("CRM") ||
				loginUserRole.equalsIgnoreCase("Tele caller Manager") ||
				loginUserRole.equalsIgnoreCase("Telecaller Manager") ){

			Map CRMData = (Map) getTeamCountReceptionist(req, "crmuser");
			map.put("totalLostCount",(CRMData.get("managerLostCount") == null) ? 0 : CRMData.get("managerLostCount"));
			map.put("totalRetailCount",(CRMData.get("managerRetailCount") == null) ? 0 : CRMData.get("managerRetailCount"));
			map.put("totalBookingsCount",(CRMData.get("managerBookingCount") == null) ? 0 : CRMData.get("managerBookingCount"));
			map.put("totalEnquirysCount",(CRMData.get("managerEnquiryCount") == null) ? 0 : CRMData.get("managerEnquiryCount"));
			map.put("totalPreInquiryCount", (CRMData.get("managerPreInquiryCount") == null) ? 0 : CRMData.get("managerPreInquiryCount"));
			map.put("emp_name",dmsEmp.getEmpName());
			map.put("roleName",dmsEmp.getDmsRole().getRoleName());
			map.put("branch",dmsEmp.getDmsBranch().getName());
			map.put("emp_id",dmsEmp.getEmpId());

			Integer totalDroppedCount = 0;
			if (branchList != null && branchList.size() > 0) {
				totalDroppedCount = dmsLeadDao.getTotaContactDrops(managerEmps, startDate, endDate, branchList,
						Integer.valueOf(orgId));
			} else {
				totalDroppedCount = dmsLeadDao.getTotaContactDrops(managerEmps, startDate, endDate, Integer.valueOf(orgId));
			}
			map.put("totalDroppedCount", totalDroppedCount);

		}else{

			Map CREData = (Map) getReceptionistData(req, "");
			map.put("totalLostCount",(CREData.get("totalLostCount") == null) ? 0 : CREData.get("totalLostCount"));
			map.put("totalRetailCount",(CREData.get("RetailCount") == null) ? 0 : CREData.get("RetailCount"));
			map.put("totalBookingsCount",(CREData.get("bookingsCount") == null) ? 0 : CREData.get("bookingsCount"));
			map.put("totalEnquirysCount",(CREData.get("enquirysCount") == null) ? 0 : CREData.get("enquirysCount"));
			map.put("totalPreInquiryCount",(CREData.get("contactsCount") == null) ? 0 : CREData.get("contactsCount"));
			map.put("emp_name",dmsEmp.getEmpName());
			map.put("roleName",dmsEmp.getDmsRole().getRoleName());
			map.put("branch",dmsEmp.getDmsBranch().getName());
			map.put("emp_id",dmsEmp.getEmpId());
			
			String totalDroppedCount = (CREData.get("totalDroppedCount") == null) ? "0" :  (String) CREData.get("totalDroppedCount");
			map.put("totalDroppedCount",Integer.parseInt(totalDroppedCount));
		}
		return map;
	}
}
