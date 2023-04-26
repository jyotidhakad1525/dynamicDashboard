package com.automate.df.service;

import com.automate.df.entity.DmsUsedVehicles;
import com.automate.df.entity.UsedVehicleFilterReq;
import com.automate.df.entity.salesgap.DmsEmployee;
import com.automate.df.exception.DynamicFormsServiceException;

import java.util.Map;

public interface UsedVehicleService {

    DmsUsedVehicles saveUsedVehicle(DmsUsedVehicles dmsUsedVehicles,int userID) throws DynamicFormsServiceException;

    DmsUsedVehicles updateUsedVehicle(DmsUsedVehicles dmsUsedVehicles,int userID) throws DynamicFormsServiceException;

    Map<String, Object> getUsedVehicleList(Integer pageNo, Integer size, Integer orgId, boolean paginationRequired, String status ) throws DynamicFormsServiceException;

    Map<String, Object> getUsedVehicleFilterList(UsedVehicleFilterReq usedVehicleFilterReq) throws DynamicFormsServiceException;

    DmsUsedVehicles getUsedVehicleById(Long recordId) throws DynamicFormsServiceException;
    
    DmsUsedVehicles saveMoveToUsedVehicle(String rcNumber) throws DynamicFormsServiceException;
    
    DmsUsedVehicles getByRcNumber(String rcNumber) throws DynamicFormsServiceException;
    
    DmsUsedVehicles saveUsedVehicle2(DmsUsedVehicles dmsUsedVehicles,int userID) throws DynamicFormsServiceException;
}
