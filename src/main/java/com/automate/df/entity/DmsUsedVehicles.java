package com.automate.df.entity;


import com.automate.df.entity.oh.DmsBranch;
import com.automate.df.entity.sales.DmsOrganization;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "dms_used_vehicles")
public class DmsUsedVehicles implements Serializable {

    private static final long serialVersionUID = -2476163317401375224L;
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @JoinColumn(name = "org_id")
    private Integer org_id;

    @NotNull
    @Column(name = "created_by")
    private Integer created_by;

    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "created_date")
    private Date created_date;

    @Column(name = "modified_by")
    private Integer modified_by;

    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "modified_date")
    private Date modified_date;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;



    //bi-directional many-to-one association to DmsBranch

    @Column(name = "location")
    private String location;
    @JoinColumn(name = "dealer_code")
    private String dealer_code;

    @JoinColumn(name = "dealer_id")
    private Integer dealer_id;

    @JoinColumn(name = "make_id")
    private Integer make_id;
    @JoinColumn(name = "model_id")
    private Integer model_id;


    @Column(name = "variant")
    private String variant;
    @Column(name = "color")
    private String color;
    @Column(name = "fuel")
    private String fuel;
    @Column(name = "transmission")
    private String transmission;


    @Column(name = "making_month")
    private String making_month;
    @Column(name = "making_year")
    private Integer making_year;
    @Column(name = "rc_number")
    private String rc_number;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "registration_date")
    private String registration_date;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "registration_valid_upto")
    private String registration_valid_upto;

    @Column(name = "vin_number")
    private String vin_number;
    @Column(name = "engine_number")
    private String engine_number;
    @Column(name = "chassis_number")
    private String chassis_number;

    @Column(name = "no_of_owner")
    private String no_of_owner;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "vehicle_purchase_date")
    private String vehicle_purchase_date;

    @Column(name = "vehicle_purchase_price")
    private Double vehicle_purchase_price;
    @Column(name = "insurance_type")
    private String insurance_type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "insurance_valid_upto")
    private String insurance_valid_upto;

    @Column(name = "driven_kms")
    private Double driven_kms;
    @Column(name = "vehicle_selling_price")
    private Double vehicle_selling_price;
    @Column(name = "evaluator_name")
    private String evaluator_name;

    @Column(name = "doc_list")
    @ElementCollection
    public List<String> doc_list;


    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "dms_sold_vehicles_id")
    DmsSoldVehicles soldVehicle;

    @Column(name = "universal_id")
    private String universal_id;

    @JsonInclude()
    @Transient
    @JsonSerialize
    public String newMake;
    @JsonInclude()
    @Transient
    @JsonSerialize
    public String newModel;
    
    public enum Status {
        Sold,
        Unsold,
        EvalutionApprovedOnly
    }


}
