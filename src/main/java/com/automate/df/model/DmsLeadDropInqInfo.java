package com.automate.df.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DmsLeadDropInqInfo {

    private String additionalRemarks;

    private BigInteger branchId;

    private String brandName;

    private Date createdDatetime;

    private String dealerName;

    private Integer leadId;

    private String lostReason;

    private BigInteger organizationId;

    private String otherReason;

    private String stage;

    private String firstName;

    private String lastName;

    private String mobileNumber;

    private String status;

    private Date updatedDatetime;

    private String enquiryCategory;

    private String droppedby;

    private Date droppedDate;

    private String approver;

    private Integer leadDropId;

    private String crmUniversalId;

    private Integer count;

    private String leadStatus;

    private String salesConsultant;

    private String createdBy;

}
