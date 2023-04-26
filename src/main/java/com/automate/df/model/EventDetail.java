package com.automate.df.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeStringType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;

import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;



import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EventDetail implements Serializable {
    private static final long serialVersionUID = 1L;

   
    private int id;

    private String area;

    //@Column(name = "assessment")
    private String assessments;


    //@Column(name = "organization_id")
    private int organizationId;

    //@Column(name = "branch_id")
    private int branchId;

    //@Column(name = "budget")
    private String budget;


    //@Column(name = "status_remarks")
    private String statusRemarks;

    //@Column(name = "organiser_name")
    private String organiserName;

    //@Column(name = "event_emp_details")
    private String eventEmpDetails;

    //@Column(name = "event_vehicle_details")
    private String eventVehicleDetails;

    //@Column(name = "district")
    private String district;


    //@Column(name = "end_datetime")
    private Date endDatetime;

    //@Column(name = "event_id")
    private String eventId;

    @Lob
    //@Column(name = "event_photos")
    private String eventPhotos;
    
    //@Column(name = "latitude")
    private String latitude;

    //@Column(name = "leaf_lets_quntiry")
    private int leafLetsQuntiry;

    //@Column(name = "location")
    private String location;

    @Lob
    //@Column(name = "location_photos")
    private String locationPhotos;

    //@Column(name = "longitude")
    private String longitude;

    //@Column(name = "name")
    private String name;

    //@Column(name = "organiser_id")
    private Integer organiserId;
    
    //@Column(name = "pincode")
    private int pincode;

    //@Column(name = "start_datetime")
    private Date startDatetime;

    //@Column(name = "state")
    private String state;

    //@Column(name = "startdate")
    private Date startdate;
    
    //@Column(name = "enddate")
    private Date enddate;

    //@Column(name = "others")
    private String others;

    //@Column(name = "manager_id")
    private Integer managerId;


    //@Column(name = "area_type")
    private String areatype;

    //@Column(name = "population_strength")
    private BigInteger populationStrngth;

    //@Column(name = "available_cars")
    private BigInteger availableCars;


    //@Column(name = "total_budget_amount")
    private BigDecimal totalBudgetAmount;


    @Enumerated(EnumType.STRING)
    //@Column(name = "status")
    private Status status;
    
	/*
	 * @OneToMany(mappedBy = "eventDetail", cascade = CascadeType.ALL, fetch =
	 * FetchType.EAGER)
	 * 
	 * @JsonIgnore private List<EventApproval> eventApprovals;
	 */
    
    

    public enum Status {
        Planning_Pending,
        Approval_Pending,
        Approved,
        Rejected

    }

}