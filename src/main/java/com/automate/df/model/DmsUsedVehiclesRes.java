package com.automate.df.model;


import com.automate.df.entity.DmsSoldVehicles;
import com.automate.df.entity.DmsUsedVehicles;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.sun.istack.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class DmsUsedVehiclesRes implements Serializable {

   
    private Long id;
    private Integer org_id;
    private Integer created_by;
    private Date created_date;
    private Integer modified_by;
    private Date modified_date;
    private DmsUsedVehicles.Status status;
    private String location;
    private String dealer_code;
    private Integer dealer_id;
    private Integer make_id;
    private Integer model_id;
    private String variant;
    private String color;
    private String fuel;
    private String transmission;
    private String making_month;
    private Integer making_year;
    private String rc_number;
    private String registration_date;
    private String registration_valid_upto;
    private String vin_number;
    private String engine_number;
    private String chassis_number;
    private String no_of_owner;
    private String vehicle_purchase_date;
    private Double vehicle_purchase_price;
    private String insurance_type;
    private String insurance_valid_upto;
    private Double driven_kms;
    private Double vehicle_selling_price;
    private String evaluator_name;
    private String makeName;
    private String modelName;
    public List<String> doc_list;
    DmsSoldVehicles soldVehicle;
    private String universal_id;
}
