package com.automate.df.model;

import java.util.List;

import lombok.Data;
@Data
public class AttendanceReportRequest {
	String org;
	String fromDate;
	String toDate;
	List<Integer> dealerCodes;
	List<Integer> empIds;
}
