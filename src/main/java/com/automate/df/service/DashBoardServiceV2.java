package com.automate.df.service;

import java.util.List;
import java.util.Map;

import com.automate.df.dao.dashboard.TargetAchivementEvent;
import com.automate.df.dao.dashboard.TargetAchivementModelandSource;
import com.automate.df.exception.DynamicFormsServiceException;
import com.automate.df.model.DmsLeadDropInqResponse;
import com.automate.df.model.MyTaskReq;
import com.automate.df.model.df.dashboard.DashBoardReq;
import com.automate.df.model.df.dashboard.DashBoardReqImmediateHierarchyV2;
import com.automate.df.model.df.dashboard.DashBoardReqV2;
import com.automate.df.model.df.dashboard.EventDataRes;
import com.automate.df.model.df.dashboard.LeadSourceRes;
import com.automate.df.model.df.dashboard.OverAllTargetAchivements;
import com.automate.df.model.df.dashboard.SalesDataRes;
import com.automate.df.model.df.dashboard.TargetAchivement;
import com.automate.df.model.df.dashboard.TargetRankingRes;
import com.automate.df.model.df.dashboard.VehicleModelRes;
import com.automate.df.model.oh.TodaysTaskRes2;

import org.springframework.http.ResponseEntity;

public interface DashBoardServiceV2 {

	List<TargetAchivement> getTargetAchivementParams(DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	OverAllTargetAchivements getTargetAchivementParamsWithEmps(DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	List<TargetAchivement> getTargetAchivementParamsForSingleEmp(DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	List<TargetAchivement> getTargetAchivementParamsForSingleEmpImmediateHierarchy(
			DashBoardReqImmediateHierarchyV2 req) throws DynamicFormsServiceException;

	List<VehicleModelRes> getVehicleModelData(DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	List<VehicleModelRes> getVehicleModelDataByBranch(DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	List<LeadSourceRes> getLeadSourceData(DashBoardReqV2 req) throws DynamicFormsServiceException;

	List<LeadSourceRes> getLeadSourceDataByBranch(DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	List<EventDataRes> getEventSourceData(DashBoardReqV2 req) throws DynamicFormsServiceException;

	List<EventDataRes> getEventSourceDataByBranch(DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	Map<String, Object> getLostDropData(DashBoardReqV2 req) throws DynamicFormsServiceException;

	Map<String, Object> getLostDropDataByBranch(DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	Map<String, Object> getTodaysPendingUpcomingData(DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	SalesDataRes getSalesData(DashBoardReqV2 req) throws DynamicFormsServiceException;

	List<Map<String, Long>> getSalesComparsionData(DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	Map<String, Object> getTodaysPendingUpcomingDataV2(MyTaskReq req)
			throws DynamicFormsServiceException;

	Map<String, Object> getTodaysPendingUpcomingDataV3(MyTaskReq req)
			throws DynamicFormsServiceException;

	List<TargetRankingRes> getEmployeeTargetRankingByOrg(Integer orgId, DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	List<TargetRankingRes> getEmployeeTargetRankingByOrgAndBranch(Integer orgId, Integer branchId,
			DashBoardReqV2 req) throws DynamicFormsServiceException;

	Map<String, Object> getTodaysPendingUpcomingDataDetailV3(MyTaskReq req)
			throws DynamicFormsServiceException;

	List<TargetAchivementModelandSource> getTargetAchivementParamsModelAndSource(DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	List<TargetAchivementEvent> getTargetAchivementParamsEvents(DashBoardReqV2 req)
			throws DynamicFormsServiceException;

	List<TargetAchivementModelandSource> getTargetAchivementParamsForSingleEmpModelAndSource3(
			DashBoardReqV2 req) throws DynamicFormsServiceException;

	Map<String, Object> getTodaysPendingUpcomingDataV2Filter(MyTaskReq req)
			throws DynamicFormsServiceException;

	List<TodaysTaskRes2> getFilteredTasksCount(MyTaskReq req) throws DynamicFormsServiceException;

	List<TargetRankingRes> getEmployeeTargetDealerRankingByOrg(Integer orgId, String branchName,
			DashBoardReqV2 req) throws DynamicFormsServiceException;

	List<TargetRankingRes> getEmployeeTargetBranchRankingByOrgAndBranch(Integer orgId,
			String branchName, DashBoardReqV2 req) throws DynamicFormsServiceException;

	ResponseEntity<?> getDropLeadsRedirection(DashBoardReqV2 req, String token)
			throws DynamicFormsServiceException;

}
