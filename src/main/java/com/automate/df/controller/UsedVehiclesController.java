package com.automate.df.controller;


import com.automate.df.entity.DmsUsedVehicles;
import com.automate.df.entity.UsedVehicleFilterReq;
import com.automate.df.exception.DynamicFormsServiceException;
import com.automate.df.service.UsedVehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/used_vehicles")
public class UsedVehiclesController {

    @Autowired
    Environment env;

    @Autowired
    UsedVehicleService usedVehicleService;


    @CrossOrigin
    @PostMapping(value="/create/{userId}")
    public ResponseEntity<?> createUsedVehicle(@RequestBody DmsUsedVehicles dmsUsedVehicles,@PathVariable int userId)
            throws DynamicFormsServiceException {
        DmsUsedVehicles response = null;
        if (Optional.of(dmsUsedVehicles).isPresent()) {
            response=usedVehicleService.saveUsedVehicle(dmsUsedVehicles,userId);
        } else {
            throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping(value="/update/{userId}")
    public ResponseEntity<?> updateUsedVehicle(@RequestBody DmsUsedVehicles dmsUsedVehicles ,@PathVariable int userId)
            throws DynamicFormsServiceException {
        DmsUsedVehicles response = null;
        if (Optional.of(dmsUsedVehicles).isPresent()) {
            response=usedVehicleService.updateUsedVehicle(dmsUsedVehicles,userId);
            if(response==null) {
                return new ResponseEntity<>("This RC number vehicle already registered", HttpStatus.OK);
            }
        } else {
            throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value="/{orgId}")
    public ResponseEntity<?> getUsedVehicles(@RequestParam(name="pageNo") Integer pageNo,
                                                         @RequestParam(name="size") Integer size ,
                                                         @RequestParam(name="paginationRequired") boolean paginationRequired,
                                                         @RequestParam(name="status") String status ,
                                                         @PathVariable(name="orgId") Integer orgId)
            throws DynamicFormsServiceException {
        Map<String, Object> response = null;
        response = usedVehicleService.getUsedVehicleList(pageNo,size,orgId,paginationRequired,status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value="/record/{recordId}")
    public ResponseEntity<?> getUsedVehicles(@PathVariable(name="recordId") Long recordId)
            throws DynamicFormsServiceException {
        DmsUsedVehicles response = null;
        response = usedVehicleService.getUsedVehicleById(recordId);
        if(response != null){
            return new ResponseEntity<>(response, HttpStatus.OK);
        }else{
            throw new DynamicFormsServiceException("Record not found", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin
    @PostMapping(value="/filter")
    public ResponseEntity<?> getUsedVehiclesWithFilter(@RequestBody UsedVehicleFilterReq usedVehicleFilterReq)
            throws DynamicFormsServiceException {
        Map<String, Object> response = null;
        response = usedVehicleService.getUsedVehicleFilterList(usedVehicleFilterReq);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @CrossOrigin
    @PostMapping(value="/moveToUsedVehicle/{rcNumber}")
    public ResponseEntity<?> modelToUsedVehicle(@PathVariable String rcNumber)
            throws DynamicFormsServiceException {
        DmsUsedVehicles response = null;
        response=usedVehicleService.saveMoveToUsedVehicle(rcNumber);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @CrossOrigin
    @PostMapping(value="/getByRcNumber/{rcNumber}")
    public ResponseEntity<?> getByRcNumber(@PathVariable String rcNumber)
            throws DynamicFormsServiceException {
        DmsUsedVehicles response = null;
        response=usedVehicleService.getByRcNumber(rcNumber);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @CrossOrigin
    @PostMapping(value="/createForUsedVehicle/{userId}")
    public ResponseEntity<?> createForUsedVehicle(@RequestBody DmsUsedVehicles dmsUsedVehicles,@PathVariable int userId)
            throws DynamicFormsServiceException {
        DmsUsedVehicles response = null;
        if (Optional.of(dmsUsedVehicles).isPresent()) {
            response=usedVehicleService.saveUsedVehicle2(dmsUsedVehicles,userId);
        } else {
            throw new DynamicFormsServiceException(env.getProperty("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
