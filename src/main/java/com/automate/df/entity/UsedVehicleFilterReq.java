package com.automate.df.entity;

import java.util.List;

import lombok.Data;

@Data
public class UsedVehicleFilterReq {
    Integer pageNo;
    Integer size;
    Boolean paginationRequired;
    Integer orgId;
    List<String> dealer_code;
    int make_id;
    int model_id;
    String  rc_number;
    Long priceFrom;
    Long priceTo;
    String status;
    List<String> location;
}
