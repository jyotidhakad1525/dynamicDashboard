package com.automate.df.model.df.dashboard;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionistDashBoardReq {
	
	String startDate;
	String endDate;
	String dealerCode;
	List<Integer> branchList;
	String empName;
	Integer orgId; 
	Integer loggedInEmpId;
	List<String> dealerCodes;
	List<Integer> empList;
	String role;
	String dashboardType;
	private boolean self;
}
