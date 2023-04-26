package com.automate.df.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.automate.df.dao.DmsSourceOfEnquiryDao;
import com.automate.df.dao.EmployeeAllocation;
import com.automate.df.dao.LeadStageRefDao;
import com.automate.df.dao.SourceAndIddao;
import com.automate.df.dao.TmpEmpHierarchyDao;
import com.automate.df.dao.dashboard.DashBoardV3Dao;
import com.automate.df.dao.dashboard.DmsEmpTargetRankingBranchDao;
import com.automate.df.dao.dashboard.DmsEmpTargetRankingOrgDao;
import com.automate.df.dao.dashboard.DmsLeadDao;
import com.automate.df.dao.dashboard.DmsLeadDropDao;
import com.automate.df.dao.dashboard.DmsTargetParamAllEmployeeSchedularDao;
import com.automate.df.dao.dashboard.DmsTargetParamEmployeeSchedularDao;
import com.automate.df.dao.dashboard.DmsWfTaskDao;
import com.automate.df.dao.dashboard.TargetAchivementEvent;
import com.automate.df.dao.dashboard.TargetAchivementModelandSource;
import com.automate.df.dao.salesgap.DmsEmployeeRepo;
import com.automate.df.dao.salesgap.TargetSettingRepo;
import com.automate.df.dao.salesgap.TargetUserRepo;
import com.automate.df.entity.LeadStageRefEntity;
import com.automate.df.entity.SourceAndId;
import com.automate.df.entity.dashboard.DmsLead;
import com.automate.df.entity.dashboard.DmsWFTask;
import com.automate.df.entity.sales.allocation.DmsEmployeeAllocation;
import com.automate.df.entity.salesgap.DmsEmployee;
import com.automate.df.exception.DynamicFormsServiceException;
import com.automate.df.model.EventDetail;
import com.automate.df.model.df.dashboard.DashBoardReqV2;
import com.automate.df.model.df.dashboard.EmployeeTargetAchievement;
import com.automate.df.model.df.dashboard.EmployeeTargetAchievementEvents;
import com.automate.df.model.df.dashboard.EmployeeTargetAchievementModelAndView;
import com.automate.df.model.df.dashboard.EventDataRes;
import com.automate.df.model.df.dashboard.LeadSourceRes;
import com.automate.df.model.df.dashboard.OverAllTargetAchivements;
import com.automate.df.model.df.dashboard.OverAllTargetAchivementsEvents;
import com.automate.df.model.df.dashboard.OverAllTargetAchivementsModelAndSource;
import com.automate.df.model.df.dashboard.TargetAchivement;
import com.automate.df.model.df.dashboard.VehicleModelRes;
import com.automate.df.model.salesgap.TargetDropDownV2;
import com.automate.df.model.salesgap.TargetRoleRes;
import com.automate.df.service.DashBoardServiceV4;
import com.automate.df.util.DashBoardUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;


/**
 * 
 * @author srujan
 *
 */

@Service
@Slf4j
public class DashBoardServiceImplV4 implements DashBoardServiceV4{
	
	
	public static final String ENQUIRY = "Enquiry";
	public static final String DROPPED = "DROPPED";
	public static final String TEST_DRIVE= "Test Drive";
	public static final String TEST_DRIVE_APPROVAL= "Test Drive Approval";
	
	public static final String HOME_VISIT= "Home Visit";
	public static final String HOME_VISIT_APPROVAL= "Home Visit Approval";
	public static final String FINANCE= "Finance";
	public static final String INSURANCE= "Insurance";
	public static final String VIDEO_CONFERENCE= "Video Conference";
	public static final String PROCEED_TO_BOOKING= "Proceed to Booking";
	public static final String BOOKING_FOLLOWUP_DSE= "Booking Follow Up - DSE";
	public static final String BOOKING_FOLLOWUP_CRM= "Booking Follow Up - CRM";
	public static final String BOOKING_FOLLOWUP_ACCCESSORIES= "Booking Follow Up - Accessories";
	public static final String VERIFY_EXCHANGE_APPROVAL = "Verify Exchange Approval";
	
	
	public static final String READY_FOR_INVOICE= "Ready for invoice - Accounts";
	public static final String PROCEED_TO_INVOICE= "Proceed to Invoice";
	public static final String INVOICE_FOLLOWUP_DSE = "Invoice Follow Up - DSE";
	public static final String INVOICE = "INVOICE";
	public static final String EXTENDED_WARRANTY = "EXTENDEDWARRANTY";
	
	public static final String PRE_BOOKING = "Pre Booking";
	public static final String DELIVERY = "Delivery";
	
	public static final String BOOKING = "Booking";
	public static final String EXCHANGE = "Exchange";
	public static final String ACCCESSORIES = "Accessories";
	public static final String EVENTS = "Events";
	
	
	
	public static final String enqCompStatus = "ENQUIRYCOMPLETED";
	public static final String preBookCompStatus = "PREBOOKINGCOMPLETED";
	public static final String bookCompStatus = "BOOKINGCOMPLETED";
	public static final String invCompStatus = "INVOICECOMPLETED";
	public static final String preDelCompStatus = "PREDELIVERYCOMPLETED";
	public static final String delCompStatus="DELIVERYCOMPLETED";

	private static final String RETAIL_TARGET = "RETAIL_TARGET";
	
	

	@Autowired
	DmsSourceOfEnquiryDao dmsSourceOfEnquiryDao;
	
	@Autowired
	SourceAndIddao repository; 
	
	@Autowired
	EmployeeAllocation employeeAllocation;
	
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
	OHServiceImpl ohServiceImpl;
	
	@Autowired
	RestTemplate restTemplate;
	
	
	
	@Autowired
	ObjectMapper om;
	
	@Autowired
	LeadStageRefDao leadStageRefDao;
	
	@Value("${lead.enquiry.url}")
	String leadSourceEnqUrl;
	
	@Autowired
	DashBoardServiceImplV2 dashBoardServiceImplV2;
	
	
	@Override
	public OverAllTargetAchivements getTargetAchivementParamsWithEmps(DashBoardReqV2 req) throws DynamicFormsServiceException {
		log.info("Inside getTargetAchivementParamsWithEmps() of DashBoardServiceImplV{}");
		OverAllTargetAchivements overAllTargetAchivements = new OverAllTargetAchivements();
		List<EmployeeTargetAchievement> empTargetAchievements = new ArrayList<>();
		List<EmployeeTargetAchievement> finalEmpTargetAchievements = new ArrayList<>();
		List<TargetAchivement> resList = new ArrayList<>();
		try {
			List<List<TargetAchivement>> allTargets = new ArrayList<>();
			Integer empId = req.getLoggedInEmpId();
			log.debug("Getting Target Data, LoggedIn emp id "+empId );
			String startDate = null;
			String endDate = null;
			if (null == req.getStartDate() && null == req.getEndDate()) {
				startDate = dashBoardServiceImplV2.getFirstDayOfMonth();
				endDate = dashBoardServiceImplV2.getLastDayOfMonth();
			} else {
				startDate = req.getStartDate()+" 00:00:00";
				endDate = req.getEndDate()+" 23:59:59";
			}

			List<Integer> selectedEmpIdList = req.getEmpSelected();
			List<Integer> selectedNodeList = req.getLevelSelected();
			TargetRoleRes tRole = salesGapServiceImpl.getEmpRoleDataV2(empId);
			String orgId = tRole.getOrgId();
			String branchId = tRole.getBranchId();
			//orgId = req.getOrgId();
			log.debug("tRole getTargetAchivementParams "+tRole);
			if (null != selectedEmpIdList && !selectedEmpIdList.isEmpty()) {
				log.debug("Fetching empReportingIdList for selected employees,selectedEmpIdList" + selectedEmpIdList);
				for (Integer eId : selectedEmpIdList) {
					List<Integer> empReportingIdList = new ArrayList<>();
					empReportingIdList.add(eId);
					empReportingIdList.addAll(getImmediateReportingEmp(empId,orgId));
					log.debug("empReportingIdList for given selectedEmpIdList " + empReportingIdList);
					List<TargetAchivement> targetList = dashBoardServiceImplV2.getTargetAchivementParamsForMultipleEmpAndEmps(
							empReportingIdList, req, orgId, empTargetAchievements, startDate, endDate);
					
					allTargets.add(targetList);
				}

				resList = dashBoardServiceImplV2.buildFinalTargets(allTargets);
			}
			else if(null!=selectedNodeList && !selectedNodeList.isEmpty()) {
				log.debug("Fetching empReportingIdList for selected LEVEL NODES");
				
				for (Integer node : selectedNodeList) {
					List<Integer> empReportingIdList = new ArrayList<>();
					empReportingIdList.add(req.getLoggedInEmpId());
					List<Integer> nodeList = new ArrayList<>();
					nodeList.add(node);

					Map<String, Object> datamap = ohServiceImpl.getActiveDropdownsV2(nodeList,
							Integer.parseInt(tRole.getOrgId()), empId);
					datamap.forEach((k, v) -> {
						Map<String, Object> innerMap = (Map<String, Object>) v;
						innerMap.forEach((x, y) -> {
							List<TargetDropDownV2> dd = (List<TargetDropDownV2>) y;
							empReportingIdList.addAll(dd.stream().map(z -> z.getCode()).map(Integer::parseInt)
									.collect(Collectors.toList()));
						});
					});
					List<TargetAchivement> targetList = dashBoardServiceImplV2.getTargetAchivementParamsForMultipleEmpAndEmps(empReportingIdList,req,orgId,empTargetAchievements,startDate,endDate);
					allTargets.add(targetList);
					
				}
				resList = dashBoardServiceImplV2.buildFinalTargets(allTargets);
			}
			else {
				log.debug("Fetching empReportingIdList for logged in emp in else :"+req.getOrgId()+"sdsdfsdfsd"+orgId);
				List<Integer> empReportingIdList =  getImmediateReportingEmp(req.getSelectedEmpId(),orgId);
				log.debug("empReportingIdList::"+empReportingIdList);
				empReportingIdList.add(req.getSelectedEmpId());
				resList = dashBoardServiceImplV2.getTargetAchivementParamsForMultipleEmpAndEmps(empReportingIdList,req,orgId,empTargetAchievements,startDate,endDate);
			}
			final List<TargetAchivement> resListFinal = resList;
			final String startDt = startDate;
			final String endDt = endDate;
			int empId1 = req.getLoggedInEmpId();
			if(empTargetAchievements.size()>1) {
			List<List<EmployeeTargetAchievement>> targetAchiPartList = dashBoardServiceImplV2.partitionListEmpTarget(empTargetAchievements);
			ExecutorService executor = Executors.newFixedThreadPool(targetAchiPartList.size());
			
			List<CompletableFuture<List<EmployeeTargetAchievement>>> futureList  = targetAchiPartList.stream()
					.map(strings -> CompletableFuture.supplyAsync(() -> dashBoardServiceImplV2.processEmployeeTargetAchiveList(strings,resListFinal,startDt,endDt,empId1), executor))
					.collect(Collectors.toList());
			
			if (null != futureList) {
				CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
				Stream<List<EmployeeTargetAchievement>> dataStream = (Stream<List<EmployeeTargetAchievement>>) futureList.stream().map(CompletableFuture::join);
				dataStream.forEach(x -> {
					finalEmpTargetAchievements.addAll(x);
				});

			}
		
			
			}
			
			else if(empTargetAchievements.size()==1){
				empTargetAchievements.get(0).setTargetAchievements(resList);
				finalEmpTargetAchievements.addAll(empTargetAchievements);
			}
					
			
			/*if(empTargetAchievements.size()>1) {
			
			empTargetAchievements.stream().forEach(employeeTarget->{
				List<TargetAchivement> responseList = new ArrayList();
				employeeTarget.setTargetAchievements(getTaskAndBuildTargetAchievements(Arrays.asList(employeeTarget.getEmpId()), employeeTarget.getOrgId(), responseList, Arrays.asList(employeeTarget.getEmpName()), startDt,endDt, employeeTarget.getTargetAchievementsMap()));
			});
			}else if(empTargetAchievements.size()==1){
				empTargetAchievements.get(0).setTargetAchievements(resList);
			}
		*/

		}catch(Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		overAllTargetAchivements.setOverallTargetAchivements(resList);
		//overAllTargetAchivements.setEmployeeTargetAchievements(empTargetAchievements);
//		overAllTargetAchivements.setEmployeeTargetAchievements(finalEmpTargetAchievements);

		overAllTargetAchivements.setEmployeeTargetAchievements(getTargetUserChildCount(req,finalEmpTargetAchievements));
		return overAllTargetAchivements;
	}
	
	
	
	@Override
	public OverAllTargetAchivementsEvents getTargetAchivementParamsWithEmpsEvent(DashBoardReqV2 req) throws DynamicFormsServiceException {
		log.info("Inside getTargetAchivementParamsWithEmps() of DashBoardServiceImplV{}");
		OverAllTargetAchivementsEvents overAllTargetAchivements = new OverAllTargetAchivementsEvents();
		List<EmployeeTargetAchievementEvents> empTargetAchievements = new ArrayList<>();
		List<EmployeeTargetAchievementEvents> finalEmpTargetAchievements = new ArrayList<>();
		List<TargetAchivementEvent> resList = new ArrayList<>();
		  List<EventDataRes> eventData = null;
		try {
			List<List<TargetAchivementEvent>> allTargets = new ArrayList<>();
			Integer empId = req.getLoggedInEmpId();
			String sdate = req.getStartDate();
			String edate = req.getEndDate();
			List<Integer> selectedEmpIdList = req.getEmpSelected();
			List<Integer> selectedNodeList = req.getLevelSelected();
			TargetRoleRes tRole = salesGapServiceImpl.getEmpRoleDataV2(empId);
			String orgId = tRole.getOrgId();
			String branchId = tRole.getBranchId();
			log.debug("tRole getTargetAchivementParams "+tRole);
			if (null != selectedEmpIdList && !selectedEmpIdList.isEmpty()) {
				log.debug("Fetching empReportingIdList for selected employees,selectedEmpIdList" + selectedEmpIdList);
				for (Integer eId : selectedEmpIdList) {
					List<Integer> empReportingIdList = new ArrayList<>();
					empReportingIdList.add(eId);
					empReportingIdList.addAll(getImmediateReportingEmp(empId,orgId));
					log.debug("empReportingIdList for given selectedEmpIdList " + empReportingIdList);
					eventData = getEventSourceDataNew(empReportingIdList,req,orgId, branchId);
					List<TargetAchivementEvent> targetList = dashBoardServiceImplV2.getTargetAchivementParamsForMultipleEmpAndEmpsEventsV4(
							empReportingIdList, req, orgId, empTargetAchievements, sdate, edate,eventData);
					
					allTargets.add(targetList);
				}

				resList=allTargets.get(0);
			}
			else if(null!=selectedNodeList && !selectedNodeList.isEmpty()) {
				log.debug("Fetching empReportingIdList for selected LEVEL NODES");
				
				for (Integer node : selectedNodeList) {
					List<Integer> empReportingIdList = new ArrayList<>();
					empReportingIdList.add(req.getLoggedInEmpId());
					List<Integer> nodeList = new ArrayList<>();
					nodeList.add(node);

					Map<String, Object> datamap = ohServiceImpl.getActiveDropdownsV2(nodeList,
							Integer.parseInt(tRole.getOrgId()), empId);
					datamap.forEach((k, v) -> {
						Map<String, Object> innerMap = (Map<String, Object>) v;
						innerMap.forEach((x, y) -> {
							List<TargetDropDownV2> dd = (List<TargetDropDownV2>) y;
							empReportingIdList.addAll(dd.stream().map(z -> z.getCode()).map(Integer::parseInt)
									.collect(Collectors.toList()));
						});
					});
					
					eventData = getEventSourceDataNew(empReportingIdList,req,orgId, branchId);

					List<TargetAchivementEvent> targetList = dashBoardServiceImplV2.getTargetAchivementParamsForMultipleEmpAndEmpsEventsV4(
							empReportingIdList, req, orgId, empTargetAchievements, sdate, edate,eventData);
					allTargets.add(targetList);
					 
				}
				resList=allTargets.get(0);
			}
			else {
				log.debug("Fetching empReportingIdList for logged in emp in else :"+req.getLoggedInEmpId());
				List<Integer> empReportingIdList =  getImmediateReportingEmp(req.getSelectedEmpId(),orgId);
				log.debug("empReportingIdList::"+empReportingIdList);
				empReportingIdList.add(req.getSelectedEmpId());
				eventData = getEventSourceDataNew(empReportingIdList,req,orgId, branchId);
				
				resList = dashBoardServiceImplV2.getTargetAchivementParamsForMultipleEmpAndEmpsEventsV4(
						empReportingIdList, req, orgId, empTargetAchievements, sdate, edate,eventData);
			}
			final List<TargetAchivementEvent> resListFinal = resList;
			final String startDt = sdate;
			final String endDt = edate;
			 final List<EventDataRes> eventDataFinal = eventData;
			
			if(empTargetAchievements.size()>1) {
			List<List<EmployeeTargetAchievementEvents>> targetAchiPartList = dashBoardServiceImplV2.partitionListEmpTargetEvents(empTargetAchievements);
			ExecutorService executor = Executors.newFixedThreadPool(targetAchiPartList.size());
			int empId1=req.getLoggedInEmpId();
			
			List<CompletableFuture<List<EmployeeTargetAchievementEvents>>> futureList  = targetAchiPartList.stream()
					.map(strings -> CompletableFuture.supplyAsync(() -> dashBoardServiceImplV2.processEmployeeTargetAchiveListEvents(strings,resListFinal,startDt,endDt,eventDataFinal, empId1,sdate,edate), executor))
					.collect(Collectors.toList()); 
			
			if (null != futureList) {
				CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
				Stream<List<EmployeeTargetAchievementEvents>> dataStream = (Stream<List<EmployeeTargetAchievementEvents>>) futureList.stream().map(CompletableFuture::join);
				dataStream.forEach(x -> {
					finalEmpTargetAchievements.addAll(x);
				});
			}	
			}
			
			else if(empTargetAchievements.size()==1){
				empTargetAchievements.get(0).setTargetAchievements(resList);
				finalEmpTargetAchievements.addAll(empTargetAchievements);
			}
					
		}catch(Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		overAllTargetAchivements.setOverallTargetAchivementsEvent(resList);
		//overAllTargetAchivements.setEmployeeTargetAchievements(finalEmpTargetAchievements);
		overAllTargetAchivements.setEmployeeTargetAchievements(getTargetUserChildCountEvents(req,finalEmpTargetAchievements));
		return overAllTargetAchivements;
	}
	
	
	private Map<String,Integer> getEventTypes(String orgId,String startDate, String endDate){
		
		 List<EventDetail>  eventList = getEventsData(orgId,startDate,endDate);
			System.out.println("eventList::::::::"+eventList);
			Map<String,Integer> map = new LinkedHashMap<>();
			eventList.stream().forEach(res->
			{
				map.put(res.getName(), res.getId());
				
			});
			return map;
		}
	
	private List<EventDetail> getEventTypesData(String orgId,String startDate, String endDate){
		
		 List<EventDetail>  eventList = getEventsData(orgId,startDate,endDate);
			System.out.println("eventList::::::::"+eventList);
			Map<String,Integer> map = new LinkedHashMap<>();
			/*
			 * eventList.stream().forEach(res-> { map.put(res.getName(), res.getId());
			 * 
			 * });
			 */
			return eventList;
		}
	
	@Value("${dms.event.details.eventDetails}")
    private String getEventDetails;
	
	public  List<EventDetail> getEventsData(String orgId,String startDate, String endDate) {
		  
        // TODO : do not hardcode properties as below. Instead, read from external properties.
       
       HttpHeaders headers = new HttpHeaders();
       headers.setContentType(MediaType.APPLICATION_JSON);
       headers.add("orgid", "" + orgId);
       String servicePath = getEventDetails + "?startdate=" + startDate +"&enddate="+endDate;

       RestTemplate template = new RestTemplate();
       Gson gson = new Gson();
       HttpEntity<String> entity = new HttpEntity<String>(headers);
       try {
           //ResponseEntity<String> respEntity = template.exchange(servicePath, HttpMethod.GET, entity, String.class);
           //ResponseEntity<List<EventDetail>> responseEntity =
    	   ResponseEntity<?> responseEntity =
        		   template.exchange(
         		    servicePath,
         		    HttpMethod.GET,
							entity,
							//EventDetail.class/*
							
					new ParameterizedTypeReference<List<EventDetail>>() {}
									 
         		  );
           //UtilityMasterResponse resp = gson.fromJson(respEntity.getBody(), UtilityMasterResponse.class);
           List<EventDetail> eventDetail = (List<EventDetail>) responseEntity.getBody();
           //List<EventDetail> eventDetail = new Gson().fromJson(EventDetail, responseEntity.getBody());
           //EventDetail eventDetail = (EventDetail)responseEntity.getBody();
           //List<EventDetail> eventDetail1 = eventDetail;
           return eventDetail;
       } catch (HttpClientErrorException e) {
           throw e;

       }
   }
	
	private List<EventDataRes> getEventSourceDataNew(List<Integer> empIdsUnderReporting, DashBoardReqV2 req,
			String orgId,String branchId) {
		log.info("Inside getEventSourceData()");
		List<EventDataRes> resList = new ArrayList<>();
		List<String> empNamesList = dmsEmployeeRepo.findEmpNamesById(empIdsUnderReporting);
		log.info("empNamesList::" + empNamesList);
		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
		log.info("StartDate " + startDate + ", EndDate " + endDate);
		String sdate = req.getStartDate();
		String edate = req.getEndDate();
		//Date sdate = SimpleDateFormat.parse(startDate);
		//Date sdate = new SimpleDateFormat("yyyy-MM-dd").parse(startDate); 

		//List<DmsLead> dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBasedOnEnquiry(orgId,empNamesList, startDate, endDate, 4);
		//Set<String> eventCodeSet = dmsLeadList.stream().map(DmsLead::getEventCode).collect(Collectors.toSet());
		//log.debug("eventCodeSet :" + eventCodeSet);
		
		 //eventList.forEach(x -> );
		
		 getEventTypes(orgId,sdate,edate).forEach((k, v) -> {
		
			//log.debug("Generating data for EventSource " + v);
			 //getEventTypesData(orgId,sdate,edate).stream().filter(x -> x.getId()==v).map(x->x.getBudget()).collect(Collectors.toList())
			List<EventDetail> eventDetailData = getEventTypesData(orgId,sdate,edate).stream().filter(x -> x.getId()==v).collect(Collectors.toList());
			
			EventDataRes eventSource = new EventDataRes();
			//Long enqLeadCnt = 0L;
			//Long droppedCnt = 0L;
			
			log.info("Generating data for EventName " + k + " and Event id " + v);
			//List<Integer> dmsAllLeadList = dmsLeadDao.getAllEmployeeLeadsBasedOnEnquiry1(orgId, empNamesList, startDate, endDate, v);

			
			//New Code Starts
			
			List<LeadStageRefEntity> leadRefListPreEnq;
			
			log.info("Generating data for LeadSource " + v);
			
			//New Code
			List<DmsEmployeeAllocation> dmsEmployeeAllocations = employeeAllocation.findByEmployeeId(req.getLoggedInEmpId());
			
			List<Integer> dmsLeadListPreEnq = dmsLeadDao.getAllEmployeeLeadsBasedOnEnquiryEventPre(orgId,empNamesList,Integer.toString(v));

			
			//List<Integer> dmsLeadList = dmsLeadDao.getLeadIdsByEmpNamesWithOutDrop(empNamesList);
			//List<Integer> dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBasedOnEnquiry1(orgId,empNamesList,startDate, endDate, v,vehicleModelList);
			List<Integer> dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBasedOnEnquiryEvents(orgId,empNamesList,Integer.toString(v));
	        ////System.out.println("dmsLeadList Before Adding"+dmsLeadList.size());

	        dmsLeadList.addAll(dmsEmployeeAllocations.stream().filter(res -> !res.getDmsLead().getLeadStage().equalsIgnoreCase("DROPPED")
					&& empNamesList.equals(res.getDmsLead().getSalesConsultant()) && res.getDmsLead().getEventCode()==Integer.toString(v)).map(res -> res.getDmsLead().getId()).collect(Collectors.toList()));
			//dmsLeadList.addAll(dmsEmployeeAllocations.stream().filter(res -> !res.getDmsLead().getLeadStage().equalsIgnoreCase("DROPPED")).map(res -> res.getDmsLead().getId()).collect(Collectors.toList()));
			
			////System.out.println("dmsLeadList After Adding"+dmsLeadList.size());	
			
			List<Integer> dmsLeadListDropped = dmsLeadDao.getAllEmployeeLeadsBasedOnEnquiryEvents11(orgId,empNamesList,Integer.toString(v));
			

			List<DmsLead> leadRefListDropped1  =  dmsLeadDao.getLeadsByStageandDate(orgId,dmsLeadListDropped,startDate,endDate);
			////System.out.println("dmsLeadListDropped Before Adding"+dmsLeadListDropped.size());
			
			dmsLeadListDropped.addAll(dmsEmployeeAllocations.stream().filter(res -> res.getDmsLead().getLeadStage().equalsIgnoreCase("DROPPED")
					&& empNamesList.equals(res.getDmsLead().getSalesConsultant()) && res.getDmsLead().getEventCode()==Integer.toString(v)).map(res -> res.getDmsLead().getId()).collect(Collectors.toList()));
			
			////System.out.println("dmsLeadListDropped After Adding"+dmsLeadListDropped.size());
			
			dmsLeadListPreEnq.addAll(dmsEmployeeAllocations.stream().filter(res -> !res.getDmsLead().getLeadStage().equalsIgnoreCase("DROPPED") &&
	        		empNamesList.equals(res.getDmsLead().getCreatedBy()) && res.getDmsLead().getEventCode()==Integer.toString(v)).map(res -> res.getDmsLead().getId()).collect(Collectors.toList()));
					
			dmsLeadListPreEnq = dmsLeadListPreEnq.stream().distinct().collect(Collectors.toList());
			
			dmsLeadList = dmsLeadList.stream().distinct().collect(Collectors.toList());
			
			////System.out.println("dmsLeadList After Deleting Duplicates"+dmsLeadList.size());
			
			dmsLeadListDropped = dmsLeadListDropped.stream().distinct().collect(Collectors.toList());
			
			////System.out.println("dmsLeadListDropped After Deleting Duplicates"+dmsLeadListDropped.size());
			
			List<LeadStageRefEntity> leadRefList  =  leadStageRefDao.getLeadsByStageandDate(orgId,dmsLeadList,startDate,endDate);
			
			////System.out.println("leadRefList Before Duplicates"+leadRefList.size());
			
			leadRefList = leadRefList.stream().distinct().collect(Collectors.toList());
			
			leadRefListPreEnq = leadStageRefDao.getLeadsByStageandDate2EventPre(orgId,dmsLeadListPreEnq,startDate,endDate);
			leadRefListPreEnq = leadRefListPreEnq.stream().distinct().collect(Collectors.toList());
			
			////System.out.println("leadRefList After Duplicates"+leadRefList.size());
			
			List<LeadStageRefEntity> leadRefListDropped  =  leadStageRefDao.getLeadsByStageandDate(orgId,dmsLeadListDropped,startDate,endDate);
			Long preenqLeadCnt = 0L;
			Long enqLeadCnt = 0L;
			Long preBookCount = 0L;
			Long bookCount = 0L;
			Long invCount = 0L;
			Long preDeliveryCnt = 0L;
			Long delCnt = 0L;
			Long droppedCnt =0L;
			Long preEnqCarCnt =0L;
			Long enqPerCarCnt =0L;
			Long bkgPerCarCnt =0L;
			String bud = "";
		
			log.debug("dmsLeadList::"+dmsLeadList);
			
			if(null!=leadRefListPreEnq && !leadRefListPreEnq.isEmpty()) {
				preenqLeadCnt = leadRefListPreEnq.stream().filter(x->x.getStageName().equalsIgnoreCase("PREENQUIRY")).count();
			}
			//List<LeadStageRefEntity> leadRefList  =  leadStageRefDao.getLeadsByStageandDate(orgId,dmsLeadList,startDate,endDate);
			log.debug("leadRefList size "+leadRefList.size());
			if(null!=leadRefList && !leadRefList.isEmpty()) {
				
				log.debug("Total leads in LeadReF table is ::"+leadRefList.size());
				
				enqLeadCnt = leadRefList.stream().filter(x-> x.getStageName().equalsIgnoreCase("ENQUIRY")).distinct().count();
				preBookCount =leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("PREBOOKING")).count();
				bookCount = leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("BOOKING")).distinct().count();
			//	invCount = leadRefList.stream().filter(x->x.getLeadStatus()!=null && x.getLeadStatus().equalsIgnoreCase(invCompStatus)).distinct().count();
				invCount = leadRefList.stream().filter(x->x.getLeadStatus()!=null && x.getLeadStatus().equalsIgnoreCase("INVOICECOMPLETED") && x.getStageName().equalsIgnoreCase("INVOICE")).distinct().count();
				////System.out.println("Source Ids Invoice"+leadRefList.stream().filter(x->x.getLeadStatus()!=null && x.getLeadStatus().equalsIgnoreCase(invCompStatus) && x.getStageName().equalsIgnoreCase("INVOICE")).map(res->res.getLeadId()).collect(Collectors.toList()));
				preDeliveryCnt = leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("PREDELIVERY")).count();
				delCnt = leadRefList.stream().filter(x->x.getLeadStatus()!=null && x.getLeadStatus().equalsIgnoreCase(delCompStatus)).count();
			}
			bud = eventDetailData.stream().filter(x -> x.getId()==v && x.getTotalBudgetAmount()!=null).map(x->x.getTotalBudgetAmount().toString()).collect(Collectors.joining());
			String eventDateStr = eventDetailData.stream().filter(x -> x.getId()==v).map(x-> x.getStartdate().toString()).collect(Collectors.joining());
			eventSource.setId(Integer.toString(v));
			eventSource.setEventName(dashBoardUtil.getEventNameFromCode(k));
			eventSource.setBudget(new BigDecimal(bud));
			eventSource.setEventDate(eventDateStr.substring(0, 10));
			eventSource.setC(preenqLeadCnt);
			eventSource.setE(enqLeadCnt);
			eventSource.setB(bookCount);
			eventSource.setR(invCount);
			
			if(preenqLeadCnt > 0)	{	
				preEnqCarCnt = (new BigDecimal(bud).longValue()) / preenqLeadCnt;
			}
			eventSource.setContPerCar(preEnqCarCnt);
			
		if(enqLeadCnt > 0)	{
			enqPerCarCnt = (new BigDecimal(bud).longValue()) / enqLeadCnt;
		}
			eventSource.setEnqPerCar(enqPerCarCnt);
		if(bookCount > 0)	{	
			bkgPerCarCnt = (new BigDecimal(bud).longValue()) / bookCount;
		}
			eventSource.setBkgPerCar(bkgPerCarCnt);
			if(leadRefListDropped1!=null && leadRefListDropped1.size() > 0)
			{
			droppedCnt = leadRefListDropped1.stream().distinct().count();
			}
			/*
			 * if (null != dmsAllLeadList) { log.info("size of dmsLeadList " +
			 * dmsAllLeadList.size()); //enqLeadCnt = getEnqLeadCount(dmsLeadList);
			 * droppedCnt = getDroppedCount(dmsAllLeadList);
			 * ///leadSource.setR(getInvoiceCount(dmsLeadList)); //log.info("enqLeadCnt: " +
			 * enqLeadCnt + " ,droppedCnt : " + droppedCnt); }
			 */
			
			
			//eventSource.setLead(k);
			
			eventSource.setL(droppedCnt);
			
			List<String> leadUniversalIdList = leadRefList.stream().map(x->x.getUniversalId()).distinct().collect(Collectors.toList());


			/*
			 * List<String> leadUniversalIdList =
			 * dmsAllLeadList.stream().map(DmsLead::getCrmUniversalId)
			 * .collect(Collectors.toList());
			 */
			log.debug("leadUniversalIdList " + leadUniversalIdList);

			List<DmsWFTask> wfTaskList = dmsWfTaskDao.getWfTaskByAssigneeIdListByModel(empIdsUnderReporting,
					leadUniversalIdList, startDate, endDate);

			eventSource.setT(getTestDriveCount(wfTaskList));
			eventSource.setV(getHomeVisitCount(wfTaskList));
//			leadSource.setB(getBookingCount(wfTaskList));
			resList.add(eventSource);
			enqLeadCnt = 0L;
			bookCount = 0L;
			invCount = 0L;
			preEnqCarCnt = 0L;
			enqPerCarCnt = 0L;
			bkgPerCarCnt = 0L;
			//eventData = null;

		});
		return resList;
	}
	
	
	public List<EmployeeTargetAchievement>  getTargetUserChildCount(DashBoardReqV2 req,List<EmployeeTargetAchievement> finalEmpTargetAchievements) throws DynamicFormsServiceException {
		try {
			for (int i = 0; i < finalEmpTargetAchievements.size(); i++) {
				List<DmsEmployee> data = gmanagersCount(finalEmpTargetAchievements.get(i).getEmpId());
				Set<DmsEmployee> hSet = new HashSet<DmsEmployee>(data);
				finalEmpTargetAchievements.get(i).setChildCount(hSet.size());
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return finalEmpTargetAchievements;
	}
	
	public List<EmployeeTargetAchievementEvents>  getTargetUserChildCountEvents(DashBoardReqV2 req,List<EmployeeTargetAchievementEvents> finalEmpTargetAchievements) throws DynamicFormsServiceException {
		try {
			for (int i = 0; i < finalEmpTargetAchievements.size(); i++) {
				List<DmsEmployee> data = gmanagersCount(finalEmpTargetAchievements.get(i).getEmpId());
				Set<DmsEmployee> hSet = new HashSet<DmsEmployee>(data);
				finalEmpTargetAchievements.get(i).setChildCount(hSet.size());
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return finalEmpTargetAchievements;
	}


	private List<DmsEmployee> gmanagersCount(int userId) {

		List<DmsEmployee> emps = dmsEmployeeRepo.findReportee(userId);
		emps.addAll(emps);
		List<Integer> list = emps.stream().map(DmsEmployee::getEmp_id).collect(Collectors.toList());

		List<DmsEmployee> emp1 = dmsEmployeeRepo.findReporte(list);
		emps.addAll(emp1);
		List<Integer> list1 = emp1.stream().map(DmsEmployee::getEmp_id).collect(Collectors.toList());

		if (!emp1.isEmpty()) {

			List<DmsEmployee> emp2 = dmsEmployeeRepo.findReporte(list1);
			emps.addAll(emp2);
			List<Integer> list2 = emp2.stream().map(DmsEmployee::getEmp_id).collect(Collectors.toList());
			if (!emp2.isEmpty()) {
				List<DmsEmployee> emp3 = dmsEmployeeRepo.findReporte(list2);
				emps.addAll(emp3);
				List<Integer> list3 = emp3.stream().map(DmsEmployee::getEmp_id).collect(Collectors.toList());
				if (!emp3.isEmpty()) {
					List<DmsEmployee> emp4 = dmsEmployeeRepo.findReporte(list3);
					emps.addAll(emp4);
				}
			}
		}
		return emps;
	}

	@Override
	public List<TargetAchivement> getTargetAchivementParams(DashBoardReqV2 req) throws DynamicFormsServiceException {
		Integer selectedEmpId = req.getSelectedEmpId();
		log.debug("Inside getTargetAchivementParams():::"+selectedEmpId);
		List<TargetAchivement> resList = new ArrayList<>();
		String orgId  = req.getOrgId();
		//List<Integer> empReportingList = getImmediateReportingEmp(selectedEmpId,orgId);
		try {
			long startTime = System.currentTimeMillis();
			List<List<TargetAchivement>> allTargets = new ArrayList<>();
			Integer empId = req.getLoggedInEmpId();
			log.debug("Getting Target Data, LoggedIn emp id " + empId);

			List<Integer> selectedEmpIdList = req.getEmpSelected();
			List<Integer> selectedNodeList = req.getLevelSelected();
			TargetRoleRes tRole = salesGapServiceImpl.getEmpRoleDataV2(empId);
			
			log.debug("tRole getTargetAchivementParams " + tRole);
			if (null != selectedEmpIdList && !selectedEmpIdList.isEmpty()) {
				log.debug("Fetching empReportingIdList for selected employees,selectedEmpIdList" + selectedEmpIdList);
				
				
				for (Integer eId : selectedEmpIdList) {
					List<Integer> empReportingIdList = new ArrayList<>();
					empReportingIdList.add(eId);
					//empIdList = getEmployeeHiearachyData(orgId,req.getLoggedInEmpId());
					empReportingIdList.addAll(getImmediateReportingEmp(eId,orgId));
					log.debug("empReportingIdList for given selectedEmpIdList " + empReportingIdList);
					
					List<TargetAchivement> targetList = dashBoardServiceImplV2.getTargetAchivementParamsForMultipleEmp(empReportingIdList, req,
							orgId);
					log.debug("targetList::::::" + targetList);
					allTargets.add(targetList);
				}

				resList = dashBoardServiceImplV2.buildFinalTargets(allTargets);
			} else if (null != selectedNodeList && !selectedNodeList.isEmpty()) {
				log.debug("Fetching empReportingIdList for selected LEVEL NODES");

				for (Integer node : selectedNodeList) {
					List<Integer> empReportingIdList = new ArrayList<>();
					empReportingIdList.add(req.getLoggedInEmpId());
					List<Integer> nodeList = new ArrayList<>();
					nodeList.add(node);

					Map<String, Object> datamap = ohServiceImpl.getActiveDropdownsV2(nodeList,
							Integer.parseInt(tRole.getOrgId()), empId);
					datamap.forEach((k, v) -> {
						Map<String, Object> innerMap = (Map<String, Object>) v;
						innerMap.forEach((x, y) -> {
							List<TargetDropDownV2> dd = (List<TargetDropDownV2>) y;
							empReportingIdList.addAll(dd.stream().map(z -> z.getCode()).map(Integer::parseInt)
									.collect(Collectors.toList()));
						});
					});
					
					List<TargetAchivement> targetList = dashBoardServiceImplV2.getTargetAchivementParamsForMultipleEmp(empReportingIdList, req,
							orgId);
					allTargets.add(targetList);

				}
				resList = dashBoardServiceImplV2.buildFinalTargets(allTargets);
			} else {
				log.debug("Fetching empReportingIdList for logged in emp in else :" + req.getLoggedInEmpId());
				List<Integer> empReportingIdList = getImmediateReportingEmp(selectedEmpId,orgId);
				empReportingIdList.add(req.getLoggedInEmpId());
				
				
				log.debug("empReportingIdList for emp " + req.getLoggedInEmpId());
				log.debug("Calling getTargetAchivemetns in else" + empReportingIdList);
				
				resList = dashBoardServiceImplV2.getTargetAchivementParamsForMultipleEmp(empReportingIdList, req, orgId);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return resList;
	}


	//findEmpByDeptwithActive(orgId,empId);
	public List<Integer> getImmediateReportingEmp(Integer empId, String orgId) throws DynamicFormsServiceException {
		List<String> empReportingIdList = new ArrayList<>();
		log.debug("getImmediateReportingEmp(){} , Empid "+empId+", ORGID "+orgId);
		List<Integer> empReportingIdList_1 = new ArrayList<>();
		Optional<DmsEmployee> empOpt = dmsEmployeeRepo.findEmpById(empId);
		if(empOpt.isPresent()) {
			TargetRoleRes tRole = salesGapServiceImpl.getEmpRoleDataV3(empId);
			for(String orgMapBranchId : tRole.getOrgMapBranches()) {
				Map<String, Object> hMap = ohServiceImpl.getReportingHierarchyV3(empOpt.get(),Integer.parseInt(orgMapBranchId),Integer.parseInt(orgId));
				log.debug("Emp Hierarchy "+hMap);				
				if(null!=hMap) {
				for(Map.Entry<String, Object> mapentry : hMap.entrySet()) {
					Map<String, Object> map2 = (Map<String, Object>)mapentry.getValue();
					for(Map.Entry<String, Object> mapentry_1 :map2.entrySet()) {
						List<TargetDropDownV2> ddList = (List<TargetDropDownV2>)mapentry_1.getValue();
						empReportingIdList.addAll(ddList.stream().map(x->x.getCode()).collect(Collectors.toList()));
					}
				}
			}
			}
			Set<String> s = new HashSet<>();
			s.addAll(empReportingIdList);
			empReportingIdList = new ArrayList<>(s);
			
			
		}else {
			throw new DynamicFormsServiceException("Logged in emp is not valid employee,no record found in dms_employee", HttpStatus.BAD_REQUEST);
		}
		empReportingIdList_1 = empReportingIdList.stream().map(Integer::parseInt).collect(Collectors.toList());
		
		
		log.debug("empReportingIdList for org "+orgId+ " is "+empReportingIdList_1);
		List<Integer> salesempReportingIdList = new ArrayList<>();
		for(Integer id : empReportingIdList_1) {
			if(dmsEmployeeRepo.findEmpByDeptwithActive(orgId,id).isPresent()) {
				salesempReportingIdList.add(id);
			}
		}
		
		log.debug("salesempReportingIdList for org "+orgId+ " is "+salesempReportingIdList);
		return salesempReportingIdList;
	}
	
	
	

	@Override
	public OverAllTargetAchivementsModelAndSource getTargetAchivementParamsWithEmpsModelAndSource(DashBoardReqV2 req) throws DynamicFormsServiceException {
		log.info("Inside getTargetAchivementParamsWithEmps() of DashBoardServiceImplV{}");
		OverAllTargetAchivementsModelAndSource overAllTargetAchivements = new OverAllTargetAchivementsModelAndSource();
		List<EmployeeTargetAchievementModelAndView> empTargetAchievements = new ArrayList<>();
		List<EmployeeTargetAchievementModelAndView> finalEmpTargetAchievements = new ArrayList<>();
		List<TargetAchivementModelandSource> resList = new ArrayList<>();
	      List<VehicleModelRes> vehicleModelDataModelAndSource =null;
		  List<LeadSourceRes> leadSourceData = null;
		try {
			List<List<TargetAchivementModelandSource>> allTargets = new ArrayList<>();
			Integer empId = req.getLoggedInEmpId();
			log.debug("Getting Target Data, LoggedIn emp id "+empId );
			String startDate = null;
			String endDate = null;
			if (null == req.getStartDate() && null == req.getEndDate()) {
				startDate = dashBoardServiceImplV2.getFirstDayOfMonth();
				endDate = dashBoardServiceImplV2.getLastDayOfMonth();
			} else {
				startDate = req.getStartDate()+" 00:00:00";
				endDate = req.getEndDate()+" 23:59:59";
			}

			List<Integer> selectedEmpIdList = req.getEmpSelected();
			List<Integer> selectedNodeList = req.getLevelSelected();
			TargetRoleRes tRole = salesGapServiceImpl.getEmpRoleDataV2(empId);
			String orgId = tRole.getOrgId();
			////System.out.println("orginization id for the details --"+orgId);
			log.debug("tRole getTargetAchivementParams "+tRole);
			if (null != selectedEmpIdList && !selectedEmpIdList.isEmpty()) {
				log.debug("Fetching empReportingIdList for selected employees,selectedEmpIdList" + selectedEmpIdList);
				for (Integer eId : selectedEmpIdList) {
					List<Integer> empReportingIdList = new ArrayList<>();
					empReportingIdList.add(eId);
					empReportingIdList.addAll(getImmediateReportingEmp(empId,orgId));
					log.debug("empReportingIdList for given selectedEmpIdList " + empReportingIdList);
					 vehicleModelDataModelAndSource = getVehicleModelDataModelAndSource(req,empReportingIdList);
					 leadSourceData = getLeadSourceData(req,empReportingIdList);
			
					
					List<TargetAchivementModelandSource> targetList = dashBoardServiceImplV2.getTargetAchivementParamsForMultipleEmpAndEmpsModelAndSourceV4(
							empReportingIdList, req, orgId, empTargetAchievements, startDate, endDate,vehicleModelDataModelAndSource,leadSourceData);
					
					allTargets.add(targetList);
				}

				resList=allTargets.get(0);
			//	resList = dashBoardServiceImplV2.buildFinalTargets(allTargets);
			}
			else if(null!=selectedNodeList && !selectedNodeList.isEmpty()) {
				log.debug("Fetching empReportingIdList for selected LEVEL NODES");
				
				for (Integer node : selectedNodeList) {
					List<Integer> empReportingIdList = new ArrayList<>();
					empReportingIdList.add(req.getLoggedInEmpId());
					List<Integer> nodeList = new ArrayList<>();
					nodeList.add(node);

					Map<String, Object> datamap = ohServiceImpl.getActiveDropdownsV2(nodeList,
							Integer.parseInt(tRole.getOrgId()), empId);
					datamap.forEach((k, v) -> {
						Map<String, Object> innerMap = (Map<String, Object>) v;
						innerMap.forEach((x, y) -> {
							List<TargetDropDownV2> dd = (List<TargetDropDownV2>) y;
							empReportingIdList.addAll(dd.stream().map(z -> z.getCode()).map(Integer::parseInt)
									.collect(Collectors.toList()));
						});
					});
					 vehicleModelDataModelAndSource = getVehicleModelDataModelAndSource(req,empReportingIdList);
					 leadSourceData = getLeadSourceData(req,empReportingIdList);
					List<TargetAchivementModelandSource> targetList = dashBoardServiceImplV2.getTargetAchivementParamsForMultipleEmpAndEmpsModelAndSourceV4(empReportingIdList,req,orgId,empTargetAchievements,startDate,endDate,vehicleModelDataModelAndSource,leadSourceData);
					allTargets.add(targetList);
					 
				}
				//resList = dashBoardServiceImplV2.buildFinalTargets(allTargets);
				resList=allTargets.get(0);
			}
			else {
				log.debug("Fetching empReportingIdList for logged in emp in else :"+req.getLoggedInEmpId());
				List<Integer> empReportingIdList =  getImmediateReportingEmp(req.getSelectedEmpId(),orgId);
				log.debug("empReportingIdList::"+empReportingIdList);
				empReportingIdList.add(req.getSelectedEmpId());
				 vehicleModelDataModelAndSource = getVehicleModelDataModelAndSource(req,empReportingIdList);
			 leadSourceData = getLeadSourceData(req,empReportingIdList);
				resList = dashBoardServiceImplV2.getTargetAchivementParamsForMultipleEmpAndEmpsModelAndSourceV4(empReportingIdList,req,orgId,empTargetAchievements,startDate,endDate,vehicleModelDataModelAndSource,leadSourceData);
			}
			final List<TargetAchivementModelandSource> resListFinal = resList;
			final String startDt = startDate;
			final String endDt = endDate;
			
			final List<VehicleModelRes> vehicleModelDataModelAndSourceFinal =vehicleModelDataModelAndSource;
			 final List<LeadSourceRes> leadSourceDataFinal = leadSourceData;
			
			if(empTargetAchievements.size()>1) {
			List<List<EmployeeTargetAchievementModelAndView>> targetAchiPartList = dashBoardServiceImplV2.partitionListEmpTargetModelAndSource(empTargetAchievements);
			ExecutorService executor = Executors.newFixedThreadPool(targetAchiPartList.size());
			int empId1=req.getLoggedInEmpId();
			
			List<CompletableFuture<List<EmployeeTargetAchievementModelAndView>>> futureList  = targetAchiPartList.stream()
					.map(strings -> CompletableFuture.supplyAsync(() -> dashBoardServiceImplV2.processEmployeeTargetAchiveListModelAndSource(strings,resListFinal,startDt,endDt,vehicleModelDataModelAndSourceFinal,leadSourceDataFinal, empId1), executor))
					.collect(Collectors.toList()); 
			
			if (null != futureList) {
				CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
				Stream<List<EmployeeTargetAchievementModelAndView>> dataStream = (Stream<List<EmployeeTargetAchievementModelAndView>>) futureList.stream().map(CompletableFuture::join);
				dataStream.forEach(x -> {
					finalEmpTargetAchievements.addAll(x);
				});

			}
		
			
			}
			
			else if(empTargetAchievements.size()==1){
				empTargetAchievements.get(0).setTargetAchievements(resList);
				finalEmpTargetAchievements.addAll(empTargetAchievements);
			}
					
			
			/*if(empTargetAchievements.size()>1) {
			
			empTargetAchievements.stream().forEach(employeeTarget->{
				List<TargetAchivement> responseList = new ArrayList();
				employeeTarget.setTargetAchievements(getTaskAndBuildTargetAchievements(Arrays.asList(employeeTarget.getEmpId()), employeeTarget.getOrgId(), responseList, Arrays.asList(employeeTarget.getEmpName()), startDt,endDt, employeeTarget.getTargetAchievementsMap()));
			});
			}else if(empTargetAchievements.size()==1){
				empTargetAchievements.get(0).setTargetAchievements(resList);
			}
		*/

		}catch(Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		overAllTargetAchivements.setOverallTargetAchivements(resList);
		//overAllTargetAchivements.setEmployeeTargetAchievements(empTargetAchievements);
		overAllTargetAchivements.setEmployeeTargetAchievements(finalEmpTargetAchievements);
		return overAllTargetAchivements;
	}
	
	
	public List<VehicleModelRes> getVehicleModelDataModelAndSource(DashBoardReqV2 req,List<Integer> empReportingIdList) throws DynamicFormsServiceException {
		log.info("Inside getVehicleModelData(){}");
		List<VehicleModelRes> resList = new ArrayList<>();
		////System.out.println("model"+resList);
		try {

			Integer empId = req.getLoggedInEmpId();
			log.debug("Getting Target Data, LoggedIn emp id " + empId);
			
			List<Integer> selectedEmpIdList = req.getEmpSelected();
		
			TargetRoleRes tRole = salesGapServiceImpl.getEmpRoleDataV2(empId);

			String orgId = tRole.getOrgId();
			String branchId = tRole.getBranchId();

			Map<Integer, String> vehicleDataMap = dashBoardUtil.getVehilceDetails(orgId).get("main");
			List<String> vehicleModelList = new ArrayList<>();
			vehicleDataMap.forEach((k, v) -> {
				vehicleModelList.add(v);
			});
			List<Integer> selectedNodeList = req.getLevelSelected();
			resList = getVehicleModelDataModelAndSource(empReportingIdList, req, orgId, branchId, vehicleModelList);
			Double totalBookingCnt=0D;
			for(VehicleModelRes vr: resList) {
				totalBookingCnt= totalBookingCnt+vr.getB();
			}
			for(VehicleModelRes vr: resList) {
				Long tmp = vr.getB();
				
				Double perc = Double.valueOf(tmp)/totalBookingCnt;
				perc = perc*100;
				String t = String.format("%.1f", perc);
				t = t+"%";
				vr.setBookingPercentage(t);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return resList;
	}
	
	
	private List<VehicleModelRes> getVehicleModelDataModelAndSource(List<Integer> empIdsUnderReporting, DashBoardReqV2 req,String orgId, String branchId,
			List<String> vehicleModelList) {
		List<VehicleModelRes> resList = new ArrayList<>();

		List<String> empNamesList = dmsEmployeeRepo.findEmpNamesById(empIdsUnderReporting);
		log.info("empNamesList::" + empNamesList);
		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
		log.info("StartDate " + startDate + ", EndDate " + endDate);
		log.info("vehicleModelList ::" + vehicleModelList);
		for (String model : vehicleModelList) {
			if (null != model) {
				VehicleModelRes vehicleRes = new VehicleModelRes();
				log.info("Generating data for model " + model);
				
				//New Code
				List<DmsEmployeeAllocation> dmsEmployeeAllocations = employeeAllocation.findByEmployeeId(req.getLoggedInEmpId());
				
				
				
				//List<Integer> dmsLeadList = dmsLeadDao.getLeadIdsByEmpNamesWithOutDrop(empNamesList);
				List<Integer> dmsLeadList = dmsLeadDao.getAllEmployeeLeadsWithModel1(orgId,empNamesList,model);
		        ////System.out.println("dmsLeadList Before Adding"+dmsLeadList.size());

		        dmsLeadList.addAll(dmsEmployeeAllocations.stream().filter(res -> !res.getDmsLead().getLeadStage().equalsIgnoreCase("DROPPED")
						&& empNamesList.equals(res.getDmsLead().getSalesConsultant()) && res.getDmsLead().getModel().equalsIgnoreCase(model)).map(res -> res.getDmsLead().getId()).collect(Collectors.toList()));
				//dmsLeadList.addAll(dmsEmployeeAllocations.stream().filter(res -> !res.getDmsLead().getLeadStage().equalsIgnoreCase("DROPPED")).map(res -> res.getDmsLead().getId()).collect(Collectors.toList()));
				
				////System.out.println("dmsLeadList After Adding"+dmsLeadList.size());	
				
				List<Integer> dmsLeadListDropped = dmsLeadDao.getAllEmployeeLeadsWithModel1(orgId,empNamesList,model);
				
				////System.out.println("dmsLeadListDropped Before Adding"+dmsLeadListDropped.size());
				
				dmsLeadListDropped.addAll(dmsEmployeeAllocations.stream().filter(res -> res.getDmsLead().getLeadStage().equalsIgnoreCase("DROPPED")
						&& empNamesList.equals(res.getDmsLead().getSalesConsultant()) && res.getDmsLead().getModel().equalsIgnoreCase(model)).map(res -> res.getDmsLead().getId()).collect(Collectors.toList()));
				
				////System.out.println("dmsLeadListDropped After Adding"+dmsLeadListDropped.size());
				
				dmsLeadList = dmsLeadList.stream().distinct().collect(Collectors.toList());
				
				////System.out.println("dmsLeadList After Deleting Duplicates"+dmsLeadList.size());
				
				dmsLeadListDropped = dmsLeadListDropped.stream().distinct().collect(Collectors.toList());
				
				////System.out.println("dmsLeadListDropped After Deleting Duplicates"+dmsLeadListDropped.size());
				
				List<LeadStageRefEntity> leadRefList  =  leadStageRefDao.getLeadsByStageandDate(orgId,dmsLeadList,startDate,endDate);
				
				////System.out.println("leadRefList Before Duplicates"+leadRefList.size());
				
				leadRefList = leadRefList.stream().distinct().collect(Collectors.toList());
				
				////System.out.println("leadRefList After Duplicates"+leadRefList.size());
				
				List<LeadStageRefEntity> leadRefListDropped  =  leadStageRefDao.getLeadsByStageandDate(orgId,dmsLeadListDropped,startDate,endDate);
				
				//New Code Ends
				
				
				
				//List<DmsLead> dmsLeadList = dmsLeadDao.getAllEmployeeLeadsWithModel(orgId,empNamesList,startDate, endDate, model);
				
				
				Long enqLeadCnt = 0L;
				Long bookCount = 0L;
				Long invCount = 0L;
				
			
				/*
				 * List<Integer> dmsLeadIdList =
				 * dmsLeadList.stream().map(DmsLead::getId).collect(Collectors.toList());
				 * log.debug("dmsLeadList::"+dmsLeadList); List<LeadStageRefEntity> leadRefList
				 * =
				 * leadStageRefDao.getLeadsByStageandDate(orgId,dmsLeadIdList,startDate,endDate)
				 * ;
				 */
				if(null!=leadRefList && !leadRefList.isEmpty()) {
					log.debug("Total leads in LeadReF table is ::"+leadRefList.size());
//					////System.out.println("Total leads in LeadReF table is ------ ::"+leadRefList.size());
//					for(LeadStageRefEntity refentity : leadRefList) {
//						////System.out.println("-------------"+refentity.getStageName());
//					}
					enqLeadCnt = leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("ENQUIRY")).distinct().count();
					bookCount = leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("BOOKING")).distinct().count();
					invCount = leadRefList.stream().filter(x->x.getLeadStatus() !=null && x.getLeadStatus().equalsIgnoreCase("INVOICECOMPLETED")).distinct().count();

				}
				
				////System.out.println("@@@@@@@EnqMod List"+leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("ENQUIRY")).map(res -> res.getLeadId()).distinct().collect(Collectors.toList()));
				////System.out.println("@@@@@@@@@BookingMod List"+leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("BOOKING")).map(res -> res.getLeadId()).distinct().collect(Collectors.toList()));
				////System.out.println("@@@@@@@@@@InvMod List"+leadRefList.stream().filter(x->x.getLeadStatus()!=null && x.getLeadStatus().equalsIgnoreCase(invCompStatus)).map(res -> res.getLeadId()).distinct().collect(Collectors.toList()));
		
				Long droppedCnt = 0L;
				if (null != dmsLeadList) {
					log.info("size of dmsLeadList " + dmsLeadList.size());
					enqLeadCnt = enqLeadCnt;
					if(leadRefListDropped!=null && leadRefListDropped.size() > 0)
					{
					droppedCnt = leadRefListDropped.stream().distinct().count();
					}
					vehicleRes.setR(invCount);

					log.info("enqLeadCnt: " + enqLeadCnt + " ,droppedCnt : " + droppedCnt);
				}
				vehicleRes.setModel(model);
				vehicleRes.setE(enqLeadCnt);
				vehicleRes.setL(droppedCnt);

				List<String> leadUniversalIdList = leadRefList.stream().map(x->x.getUniversalId()).distinct().collect(Collectors.toList());
				/*
				 * dmsLeadList.stream().map(DmsLead::getCrmUniversalId)
				 * .collect(Collectors.toList());
				 */
				log.info("leadUniversalIdList " + leadUniversalIdList);

				List<DmsWFTask> wfTaskList = dmsWfTaskDao.getWfTaskByAssigneeIdListByModel(empIdsUnderReporting,
						leadUniversalIdList, startDate, endDate);

				vehicleRes.setT(getTestDriveCount(wfTaskList));
				vehicleRes.setV(getHomeVisitCount(wfTaskList));
			//	vehicleRes.setB(getBookingCount(wfTaskList));
				vehicleRes.setB(bookCount);
				resList.add(vehicleRes);
			}
		}
		return resList;
	}

	public String getStartDate(String inputStartDate) {
		if (null == inputStartDate && null == inputStartDate) {
			return getFirstDayOfMonth();
		} else {
			return inputStartDate + " 00:00:00";
		}

	}

	public String getEndDate(String inputEndDate) {
		if (null == inputEndDate && null == inputEndDate) {
			return getLastDayOfMonth();
		} else {
			return inputEndDate + " 23:59:59";
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
	
	private Long getEnqLeadCount(List<DmsLead> dmsLeadList) {
		return dmsLeadList.stream().filter(x->x.getLeadStage().equalsIgnoreCase(ENQUIRY)).count();
	}
	
	private Long getDroppedCount(List<DmsLead> dmsLeadList) {
		return dmsLeadList.stream().filter(x->x.getLeadStage().equalsIgnoreCase(DROPPED)).count();
	}
	
	private Long getInvoiceCount(List<DmsLead> dmsLeadList) {
		/*return wfTaskList.stream().
				filter(x->(x.getTaskName().equalsIgnoreCase(READY_FOR_INVOICE)
						|| x.getTaskName().equalsIgnoreCase(PROCEED_TO_INVOICE)
						|| x.getTaskName().equalsIgnoreCase(INVOICE_FOLLOWUP_DSE))).count();*/
		return dmsLeadList.stream().filter(x->x.getLeadStage().equalsIgnoreCase(INVOICE)).count();
	}
	
	public Long getTestDriveCount(List<DmsWFTask> wfTaskList) {
		//TEST_DRIVE_APPROVAL
		return wfTaskList.stream().filter(x->(x.getTaskName().equalsIgnoreCase(TEST_DRIVE) && x.getTaskStatus().equalsIgnoreCase("CLOSED")) ).count();
	}
	
	private Long getHomeVisitCount(List<DmsWFTask> wfTaskList) {
		return wfTaskList.stream().filter(x->x.getTaskName().equalsIgnoreCase(HOME_VISIT) && x.getTaskStatus().equalsIgnoreCase("CLOSED")).count();
	}
	
	
	public List<LeadSourceRes> getLeadSourceData(DashBoardReqV2 req,List<Integer> empReportingIdList) throws DynamicFormsServiceException {
		log.info("Inside getLeadSourceData(){}");
		List<LeadSourceRes> resList = new ArrayList<>();
		try {
			Integer empId = req.getLoggedInEmpId();
			log.debug("Getting Target Data, LoggedIn emp id " + empId);
			
			List<Integer> selectedEmpIdList = req.getEmpSelected();
			TargetRoleRes tRole = salesGapServiceImpl.getEmpRoleDataV2(empId);

			String orgId = tRole.getOrgId();
			String branchId = tRole.getBranchId();

			Map<Integer, String> vehicleDataMap = dashBoardUtil.getVehilceDetails(orgId).get("main");
			List<String> vehicleModelList = new ArrayList<>();
			vehicleDataMap.forEach((k, v) -> {
				vehicleModelList.add(v);
			});
			List<Integer> selectedNodeList = req.getLevelSelected();
			String selectedBranch = req.getBranchSelectionInEvents();		
			resList = getLeadSourceDataModelAndSource(empReportingIdList,req,orgId, branchId,vehicleModelList);
		}catch(Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return resList;
	}
	
	private List<LeadSourceRes> getLeadSourceDataModelAndSource(List<Integer> empIdsUnderReporting, DashBoardReqV2 req,String orgId,String branchId, List<String> vehicleModelList) {
		List<LeadSourceRes> resList = new ArrayList<>();

		List<String> empNamesList = dmsEmployeeRepo.findEmpNamesById(empIdsUnderReporting);
		log.info("empNamesList::" + empNamesList);

		String startDate = getStartDate(req.getStartDate());
		String endDate = getEndDate(req.getEndDate());
		log.info("StartDate " + startDate + ", EndDate " + endDate);

		getLeadTypes(orgId).forEach((k, v) -> {
			LeadSourceRes leadSource = new LeadSourceRes();
			log.debug("Generating data for Leadsource " + k + " and enq id " + v);
			//List<Integer> dmsAllLeadList = dmsLeadDao.getAllEmployeeLeadsBasedOnEnquiry1(orgId, empNamesList, startDate, endDate, v);

			
			//New Code Starts
			
			log.info("Generating data for LeadSource " + v);
			
			//New Code
			List<DmsEmployeeAllocation> dmsEmployeeAllocations = employeeAllocation.findByEmployeeId(req.getLoggedInEmpId());
			
			
			
			//List<Integer> dmsLeadList = dmsLeadDao.getLeadIdsByEmpNamesWithOutDrop(empNamesList);
			List<Integer> dmsLeadList = dmsLeadDao.getAllEmployeeLeadsBasedOnEnquiry1(orgId,empNamesList,v,vehicleModelList);
	        ////System.out.println("dmsLeadList Before Adding"+dmsLeadList.size());

	        dmsLeadList.addAll(dmsEmployeeAllocations.stream().filter(res -> !res.getDmsLead().getLeadStage().equalsIgnoreCase("DROPPED")
					&& empNamesList.equals(res.getDmsLead().getSalesConsultant()) && res.getDmsLead().getDmsSourceOfEnquiry().getId()==v).map(res -> res.getDmsLead().getId()).collect(Collectors.toList()));
			//dmsLeadList.addAll(dmsEmployeeAllocations.stream().filter(res -> !res.getDmsLead().getLeadStage().equalsIgnoreCase("DROPPED")).map(res -> res.getDmsLead().getId()).collect(Collectors.toList()));
			
			////System.out.println("dmsLeadList After Adding"+dmsLeadList.size());	
			
			List<Integer> dmsLeadListDropped = dmsLeadDao.getAllEmployeeLeadsBasedOnEnquiry1(orgId,empNamesList,v,vehicleModelList);
			
			////System.out.println("dmsLeadListDropped Before Adding"+dmsLeadListDropped.size());
			
			dmsLeadListDropped.addAll(dmsEmployeeAllocations.stream().filter(res -> res.getDmsLead().getLeadStage().equalsIgnoreCase("DROPPED")
					&& empNamesList.equals(res.getDmsLead().getSalesConsultant()) && res.getDmsLead().getDmsSourceOfEnquiry().getId()==v).map(res -> res.getDmsLead().getId()).collect(Collectors.toList()));
			
			////System.out.println("dmsLeadListDropped After Adding"+dmsLeadListDropped.size());
			
			dmsLeadList = dmsLeadList.stream().distinct().collect(Collectors.toList());
			
			////System.out.println("dmsLeadList After Deleting Duplicates"+dmsLeadList.size());
			
			dmsLeadListDropped = dmsLeadListDropped.stream().distinct().collect(Collectors.toList());
			
			////System.out.println("dmsLeadListDropped After Deleting Duplicates"+dmsLeadListDropped.size());
			
			List<LeadStageRefEntity> leadRefList  =  leadStageRefDao.getLeadsByStageandDate(orgId,dmsLeadList,startDate,endDate);
			
			////System.out.println("leadRefList Before Duplicates"+leadRefList.size());
			
			leadRefList = leadRefList.stream().distinct().collect(Collectors.toList());
			
			////System.out.println("leadRefList After Duplicates"+leadRefList.size());
			
			List<LeadStageRefEntity> leadRefListDropped  =  leadStageRefDao.getLeadsByStageandDate(orgId,dmsLeadListDropped,startDate,endDate);
			
			//New Code Ends
			
			//New Code Ends
			//List<DmsLead> dmsAllLeadList = dmsLeadDao.getAllEmployeeLeasForDate(empNamesList, startDate, endDate);
			//log.debug("Size of dmsAllLeadList "+dmsAllLeadList.size());
			Long enqLeadCnt = 0L;
			Long preBookCount = 0L;
			Long bookCount = 0L;
			Long invCount = 0L;
			Long preDeliveryCnt = 0L;
			Long delCnt = 0L;
			Long droppedCnt =0L;
		
			
		
			
			//List<Integer> dmsLeadList = dmsLeadDao.getLeadIdsByEmpNames(empNamesList);
			//List<Integer> dmsLeadList = dmsAllLeadList.stream().map(DmsLead::getId).collect(Collectors.toList());
			log.debug("dmsLeadList::"+dmsLeadList);
				
			//List<LeadStageRefEntity> leadRefList  =  leadStageRefDao.getLeadsByStageandDate(orgId,dmsLeadList,startDate,endDate);
			log.debug("leadRefList size "+leadRefList.size());
			if(null!=leadRefList && !leadRefList.isEmpty()) {
				
				log.debug("Total leads in LeadReF table is ::"+leadRefList.size());
/*				enqLeadCnt = leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("ENQUIRY")).count();
				preBookCount =leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("PREBOOKING")).count();
				bookCount = leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("BOOKING")).count();
				invCount = leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("INVOICE")).count();
				preDeliveryCnt = leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("PREDELIVERY")).count();
				delCnt = leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("DELIVERY")).count();*/
				
				enqLeadCnt = leadRefList.stream().filter(x-> x.getStageName().equalsIgnoreCase("ENQUIRY")).distinct().count();
				preBookCount =leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("PREBOOKING")).count();
				bookCount = leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("BOOKING")).distinct().count();
				invCount = leadRefList.stream().filter(x->x.getLeadStatus()!=null && x.getLeadStatus().equalsIgnoreCase(invCompStatus)).distinct().count();
				preDeliveryCnt = leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("PREDELIVERY")).count();
				delCnt = leadRefList.stream().filter(x->x.getLeadStatus()!=null && x.getLeadStatus().equalsIgnoreCase(delCompStatus)).count();
			}

			////System.out.println("@@@@@@@@@@EnqSource List"+leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("ENQUIRY")).map(res -> res.getLeadId()).distinct().collect(Collectors.toList()));
			////System.out.println("@@@@@@@@@@BookingSource List"+leadRefList.stream().filter(x->x.getStageName().equalsIgnoreCase("BOOKING")).map(res -> res.getLeadId()).distinct().collect(Collectors.toList()));
			////System.out.println("@@@@@@@@@@InvSource List"+leadRefList.stream().filter(x->x.getLeadStatus()!=null && x.getLeadStatus().equalsIgnoreCase(invCompStatus)).map(res -> res.getLeadId()).distinct().collect(Collectors.toList()));

			//log.debug("enqLeadCnt:"+enqLeadCnt);
			
			leadSource.setE(enqLeadCnt);
			leadSource.setR(invCount);
			leadSource.setB(bookCount);
			
			if(leadRefListDropped!=null && leadRefListDropped.size() > 0)
			{
			droppedCnt = leadRefListDropped.stream().distinct().count();
			}
			/*
			 * if (null != dmsAllLeadList) { log.info("size of dmsLeadList " +
			 * dmsAllLeadList.size()); //enqLeadCnt = getEnqLeadCount(dmsLeadList);
			 * droppedCnt = getDroppedCount(dmsAllLeadList);
			 * ///leadSource.setR(getInvoiceCount(dmsLeadList)); //log.info("enqLeadCnt: " +
			 * enqLeadCnt + " ,droppedCnt : " + droppedCnt); }
			 */
			
			
			leadSource.setLead(k);
			
			leadSource.setL(droppedCnt);
			
			List<String> leadUniversalIdList = leadRefList.stream().map(x->x.getUniversalId()).distinct().collect(Collectors.toList());


			/*
			 * List<String> leadUniversalIdList =
			 * dmsAllLeadList.stream().map(DmsLead::getCrmUniversalId)
			 * .collect(Collectors.toList());
			 */
			log.debug("leadUniversalIdList " + leadUniversalIdList);

			List<DmsWFTask> wfTaskList = dmsWfTaskDao.getWfTaskByAssigneeIdListByModel(empIdsUnderReporting,
					leadUniversalIdList, startDate, endDate);

			leadSource.setT(getTestDriveCount(wfTaskList));
			leadSource.setV(getHomeVisitCount(wfTaskList));
//			leadSource.setB(getBookingCount(wfTaskList));
			resList.add(leadSource);
		});
		return resList;

	}
private Map<String,Integer> getLeadTypes(String orgId){
		
		List<SourceAndId> reslist=repository.getSources(orgId);
		////System.out.println("reslist"+reslist);
		Map<String,Integer> map = new LinkedHashMap<>();
		reslist.stream().forEach(res->
		{
			map.put(res.getName(), res.getId());
			
		});
		return map;
	}

		
	


}
