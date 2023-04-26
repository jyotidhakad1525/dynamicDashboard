package com.automate.df.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardReq {

    String startDate;
    String endDate;
    String dealerCode;
    List<Integer> branchList;
    String empName;
    Integer orgId;
    Integer loggedInEmpId;
    List<String> dealerCodes;
    List<Integer> empList;

    private String offset;
    private String limit;
    private String stageName;
    private String filterValue;
    private boolean forDropped;
    String dashboardType;
    private boolean self;
    List<Integer> leadIdList;
}