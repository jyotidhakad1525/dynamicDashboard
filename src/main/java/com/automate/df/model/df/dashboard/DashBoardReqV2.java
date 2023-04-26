package com.automate.df.model.df.dashboard;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DashBoardReqV2 {
	
	String orgId;
	Integer loggedInEmpId;
	List<Integer> empSelected;
	List<Integer> levelSelected;
	
	/*
	List<String> employeeIds;
	List<String> branchIds;
	List<String> vpIds;
	List<String> generalMgrId;
	List<String> managerIds;
	List<String> teamLeadIds;
	*/
	
	
	String startDate;
	String endDate;
	
	String branchSelectionInEvents;
	int pageNo;
	int size;
	
	Integer selectedEmpId;

	String stageName;
	String filterValue;
	Boolean isSelf;
	
	public DashBoardReqV2 clone() {
		DashBoardReqV2 clone = new DashBoardReqV2();
		clone.orgId = this.orgId;
		clone.loggedInEmpId = this.loggedInEmpId;
		clone.startDate = this.startDate;
		clone.endDate = this.endDate;
		clone.branchSelectionInEvents = this.branchSelectionInEvents;
		clone.pageNo = this.pageNo;
		clone.size = this.size;
		clone.selectedEmpId = this.selectedEmpId;
		if (this.empSelected != null && this.empSelected.size() > 0) {
			clone.empSelected = new ArrayList<>(this.empSelected);
		}
		if (this.levelSelected != null && this.levelSelected.size() > 0) {
			clone.levelSelected = new ArrayList<>(this.levelSelected);
		}
		return clone;
	}
}
