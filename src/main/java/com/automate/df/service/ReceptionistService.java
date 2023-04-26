package com.automate.df.service;

import java.util.List;
import java.util.Map;

import com.automate.df.model.df.dashboard.ReceptionistDashBoardReq;
import com.automate.df.model.df.dashboard.ReceptionistLeadRes;
import com.automate.df.model.df.dashboard.SourceRes;
import com.automate.df.model.df.dashboard.VehicleModelRes;

public interface ReceptionistService {
	
	Map getReceptionistData(ReceptionistDashBoardReq req, String roleName);
	
	Map getReceptionistLiveLeadData(ReceptionistDashBoardReq req);
	
	Map getManagerLiveLeadData(ReceptionistDashBoardReq req);
	
	List<SourceRes> getManagerSourceLiveLeadData(ReceptionistDashBoardReq req);
	
	List<VehicleModelRes> getManagerModelLiveLeadData(ReceptionistDashBoardReq req);
	
	Map getReceptionistManagerData(ReceptionistDashBoardReq req, String roleName);

	List<VehicleModelRes> getReceptionistModelData(ReceptionistDashBoardReq req, String roleName);
	
	List<VehicleModelRes> getLiveLeadModelData(ReceptionistDashBoardReq req);
	
	List<VehicleModelRes> getReceptionistManagerModelData(ReceptionistDashBoardReq req, String roleName);
	
	List<SourceRes> getReceptionistSourceData(ReceptionistDashBoardReq req, String roleName);
	
	List<SourceRes> getLiveLeadsSourceData(ReceptionistDashBoardReq req);
	
	List<SourceRes> getReceptionistManagerSourceData(ReceptionistDashBoardReq req, String roleName);

	List<ReceptionistLeadRes> getReceptionistLeadData(ReceptionistDashBoardReq req, String roleName);
	
	Map getTeamCount(ReceptionistDashBoardReq req, String roleName);

	Map getTeamCountReceptionist(ReceptionistDashBoardReq req, String roleName);

	Map getSalesManagerDigitalTeam(ReceptionistDashBoardReq req, String roleName);

	public List<ReceptionistLeadRes> getReceptionistDroppedLeadData(ReceptionistDashBoardReq req, String roleName);

	List<SourceRes> getXRoleSourceData(ReceptionistDashBoardReq req, String roleName);

	List<VehicleModelRes> getXRoleModelData(ReceptionistDashBoardReq req, String roleName);

	Map getReceptionistFilterData(ReceptionistDashBoardReq req, String roleName);
}
