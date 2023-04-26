package com.automate.df.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.automate.df.dao.OtherMakerModelRepository;
import com.automate.df.dao.OtherMakerRepository;
import com.automate.df.dao.UsedVehiclesRepository;
import com.automate.df.dao.oh.DmsBranchDao;
import com.automate.df.entity.DmsUsedVehicles;
import com.automate.df.entity.DmsUsedVehicles.Status;
import com.automate.df.entity.OtherMaker;
import com.automate.df.entity.OtherModel;
import com.automate.df.entity.UsedVehicleFilterReq;
import com.automate.df.entity.oh.DmsBranch;
import com.automate.df.exception.DynamicFormsServiceException;
import com.automate.df.model.DmsUsedVehiclesRes;
import com.automate.df.service.UsedVehicleService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UsedVehicleServiceImpl implements UsedVehicleService {

    @Autowired
    UsedVehiclesRepository usedVehiclesRepository;

    @Autowired
    OtherMakerModelRepository otherModelRepository;

    @Autowired
    OtherMakerRepository otherMakerRepository;

    @Autowired
    DmsBranchDao dmsBranchDao;

    @Autowired
    private EntityManager entityManager;

    @Override
    public DmsUsedVehicles saveUsedVehicle(DmsUsedVehicles dmsUsedVehicles,int userID) throws DynamicFormsServiceException {
        DmsUsedVehicles dmsUsedVehicles1 = usedVehiclesRepository.usedVehiclesByRCNo(dmsUsedVehicles.getRc_number().trim());
        if(dmsUsedVehicles1 == null){
            DmsBranch dmsBranch = dmsBranchDao.findByName(dmsUsedVehicles.getDealer_code());
            if(dmsBranch!=null){
                dmsUsedVehicles.setDealer_id(dmsBranch.getBranchId());
            }
            if(dmsUsedVehicles.getMake_id()==208) {
            	OtherMaker obj=new OtherMaker();
            	obj.setOtherMaker(dmsUsedVehicles.getNewMake());
            	obj.setStatus("Active");
            	obj.setOrgId(String.valueOf(dmsUsedVehicles.getOrg_id()));
            	obj.setCreatedBy(String.valueOf(userID));
            	obj.setCreatedAt(new Date().toString());
            	OtherMaker saveMake = otherMakerRepository.save(obj);
            	OtherModel obj2=new OtherModel();
            	obj2.setOtherMaker(dmsUsedVehicles.getNewMake());
            	obj2.setOtherModel(dmsUsedVehicles.getNewModel());
            	obj2.setOthermakerId(saveMake.getId());
            	obj2.setStatus("Active");
            	obj2.setOrgId(String.valueOf(dmsUsedVehicles.getOrg_id()));
            	obj2.setCreatedBy(String.valueOf(userID));
            	obj2.setCreatedAt(new Date());
            	OtherModel saveModel = otherModelRepository.save(obj2);
            	dmsUsedVehicles.setMake_id(saveMake.getId());
            	dmsUsedVehicles.setModel_id(saveModel.getId());
            }
            dmsUsedVehicles.setCreated_date(new Date());
            dmsUsedVehicles.setCreated_by(userID);
            dmsUsedVehicles.setStatus(Status.EvalutionApprovedOnly);
            return usedVehiclesRepository.save(dmsUsedVehicles);
        }else {
        	 DmsBranch dmsBranch = dmsBranchDao.findByName(dmsUsedVehicles.getDealer_code());
            if(dmsBranch!=null){
            	dmsUsedVehicles1.setDealer_id(dmsBranch.getBranchId());
            }
            dmsUsedVehicles1.setModified_by(userID);
            dmsUsedVehicles1.setModified_date(new Date());
            dmsUsedVehicles1.setOrg_id(dmsUsedVehicles.getOrg_id());
            dmsUsedVehicles1.setLocation(dmsUsedVehicles.getLocation());
            dmsUsedVehicles1.setDealer_code(dmsUsedVehicles.getDealer_code());
            dmsUsedVehicles1.setDealer_id(dmsUsedVehicles.getDealer_id());
            if(dmsUsedVehicles.getMake_id()==208) {
            	OtherMaker obj=new OtherMaker();
            	obj.setOtherMaker(dmsUsedVehicles.getNewMake());
            	obj.setStatus("Active");
            	obj.setOrgId(String.valueOf(dmsUsedVehicles.getOrg_id()));
            	obj.setCreatedBy(String.valueOf(userID));
            	obj.setCreatedAt(new Date().toString());
            	OtherMaker saveMake = otherMakerRepository.save(obj);
            	OtherModel obj2=new OtherModel();
            	obj2.setOtherMaker(dmsUsedVehicles.getNewMake());
            	obj2.setOtherModel(dmsUsedVehicles.getNewModel());
            	obj2.setOthermakerId(saveMake.getId());
            	obj2.setStatus("Active");
            	obj2.setOrgId(String.valueOf(dmsUsedVehicles.getOrg_id()));
            	obj2.setCreatedBy(String.valueOf(userID));
            	obj2.setCreatedAt(new Date());
            	OtherModel saveModel = otherModelRepository.save(obj2);
            	dmsUsedVehicles1.setMake_id(saveMake.getId());
            	dmsUsedVehicles1.setModel_id(saveModel.getId());
            }else {
            	 dmsUsedVehicles1.setMake_id(dmsUsedVehicles.getMake_id());
                 dmsUsedVehicles1.setModel_id(dmsUsedVehicles.getModel_id());
            }
            dmsUsedVehicles1.setVariant(dmsUsedVehicles.getVariant());
            dmsUsedVehicles1.setColor(dmsUsedVehicles.getColor());
            dmsUsedVehicles1.setFuel(dmsUsedVehicles.getFuel());
            dmsUsedVehicles1.setTransmission(dmsUsedVehicles.getTransmission());
            dmsUsedVehicles1.setMaking_month(dmsUsedVehicles.getMaking_month());
            dmsUsedVehicles1.setMaking_year(dmsUsedVehicles.getMaking_year());
            dmsUsedVehicles1.setRc_number(dmsUsedVehicles.getRc_number());
            dmsUsedVehicles1.setRegistration_date(dmsUsedVehicles.getRegistration_date());
            dmsUsedVehicles1.setRegistration_valid_upto(dmsUsedVehicles.getRegistration_valid_upto());
            dmsUsedVehicles1.setVin_number(dmsUsedVehicles.getVin_number());
            dmsUsedVehicles1.setEngine_number(dmsUsedVehicles.getEngine_number());
            dmsUsedVehicles1.setChassis_number(dmsUsedVehicles.getChassis_number());
            dmsUsedVehicles1.setNo_of_owner(dmsUsedVehicles.getNo_of_owner());
            dmsUsedVehicles1.setVehicle_purchase_date(dmsUsedVehicles.getVehicle_purchase_date());
            dmsUsedVehicles1.setVehicle_purchase_price(dmsUsedVehicles.getVehicle_purchase_price());
            dmsUsedVehicles1.setInsurance_type(dmsUsedVehicles.getInsurance_type());
            dmsUsedVehicles1.setInsurance_valid_upto(dmsUsedVehicles.getInsurance_valid_upto());
            dmsUsedVehicles1.setDriven_kms(dmsUsedVehicles.getDriven_kms());
            dmsUsedVehicles1.setVehicle_selling_price(dmsUsedVehicles.getVehicle_selling_price());
            dmsUsedVehicles1.setEvaluator_name(dmsUsedVehicles.getEvaluator_name());
            dmsUsedVehicles1.setDoc_list(dmsUsedVehicles.getDoc_list());
            dmsUsedVehicles1.setSoldVehicle(dmsUsedVehicles.getSoldVehicle());
            dmsUsedVehicles1.setUniversal_id(dmsUsedVehicles.getUniversal_id());
            dmsUsedVehicles.setCreated_date(new Date());
            dmsUsedVehicles.setCreated_by(userID);
            dmsUsedVehicles.setStatus(Status.EvalutionApprovedOnly);
            return usedVehiclesRepository.save(dmsUsedVehicles);
          //  throw new DynamicFormsServiceException("This RC number vehicle already registered", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public DmsUsedVehicles updateUsedVehicle(DmsUsedVehicles dmsUsedVehicles,int userID) throws DynamicFormsServiceException {

        Optional<DmsUsedVehicles> optionalDmsUsedVehicles = usedVehiclesRepository.findById(dmsUsedVehicles.getId());

        if(optionalDmsUsedVehicles.isPresent()) {

            if(!optionalDmsUsedVehicles.get().getRc_number().equalsIgnoreCase(dmsUsedVehicles.getRc_number())){
                DmsUsedVehicles dmsUsedVehiclesRCNumber = usedVehiclesRepository.usedVehiclesByRCNo(dmsUsedVehicles.getRc_number().trim());
                if(dmsUsedVehiclesRCNumber!=null){
                    throw new DynamicFormsServiceException("This RC number vehicle already registered", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            DmsUsedVehicles dmsPersistentEmployee = optionalDmsUsedVehicles.get();
            dmsPersistentEmployee.setId(dmsPersistentEmployee.getId());
            dmsPersistentEmployee.setCreated_by(optionalDmsUsedVehicles.get().getCreated_by());
            dmsPersistentEmployee.setCreated_date(optionalDmsUsedVehicles.get().getCreated_date());
            dmsPersistentEmployee.setModified_date(new Date());
            dmsPersistentEmployee.setUniversal_id(optionalDmsUsedVehicles.get().getUniversal_id());

            dmsPersistentEmployee.setOrg_id(dmsUsedVehicles.getOrg_id());
            dmsPersistentEmployee.setModified_by(userID);
            dmsPersistentEmployee.setStatus(dmsUsedVehicles.getStatus());
            dmsPersistentEmployee.setLocation(dmsUsedVehicles.getLocation());

            DmsBranch dmsBranch = dmsBranchDao.findByName(dmsUsedVehicles.getDealer_code());
            if(dmsBranch!=null){
                dmsPersistentEmployee.setDealer_id(dmsBranch.getBranchId());
            }

            dmsPersistentEmployee.setDealer_code(dmsUsedVehicles.getDealer_code());
            if(dmsUsedVehicles.getMake_id()==208) {
            	OtherMaker obj=new OtherMaker();
            	obj.setOtherMaker(dmsUsedVehicles.getNewMake());
            	obj.setStatus("Active");
            	obj.setOrgId(String.valueOf(dmsUsedVehicles.getOrg_id()));
            	obj.setCreatedBy(String.valueOf(userID));
            	obj.setCreatedAt(new Date().toString());
            	OtherMaker saveMake = otherMakerRepository.save(obj);
            	OtherModel obj2=new OtherModel();
            	obj2.setOtherMaker(dmsUsedVehicles.getNewMake());
            	obj2.setOtherModel(dmsUsedVehicles.getNewModel());
            	obj2.setOthermakerId(saveMake.getId());
            	obj2.setStatus("Active");
            	obj2.setOrgId(String.valueOf(dmsUsedVehicles.getOrg_id()));
            	obj2.setCreatedBy(String.valueOf(userID));
            	obj2.setCreatedAt(new Date());
            	OtherModel saveModel = otherModelRepository.save(obj2);
            	dmsPersistentEmployee.setMake_id(saveMake.getId());
            	dmsPersistentEmployee.setModel_id(saveModel.getId());
            }else {
            	dmsPersistentEmployee.setMake_id(dmsUsedVehicles.getMake_id());
                dmsPersistentEmployee.setModel_id(dmsUsedVehicles.getModel_id());
            }
            dmsPersistentEmployee.setVariant(dmsUsedVehicles.getVariant());
            dmsPersistentEmployee.setColor(dmsUsedVehicles.getColor());
            dmsPersistentEmployee.setFuel(dmsUsedVehicles.getFuel());
            dmsPersistentEmployee.setTransmission(dmsUsedVehicles.getTransmission());
            dmsPersistentEmployee.setMaking_month(dmsUsedVehicles.getMaking_month());
            dmsPersistentEmployee.setMaking_year(dmsUsedVehicles.getMaking_year());
            dmsPersistentEmployee.setRc_number(dmsUsedVehicles.getRc_number());
            dmsPersistentEmployee.setRegistration_date(dmsUsedVehicles.getRegistration_date());
            dmsPersistentEmployee.setRegistration_valid_upto(dmsUsedVehicles.getRegistration_valid_upto());
            dmsPersistentEmployee.setVin_number(dmsUsedVehicles.getVin_number());
            dmsPersistentEmployee.setEngine_number(dmsUsedVehicles.getEngine_number());
            dmsPersistentEmployee.setChassis_number(dmsUsedVehicles.getChassis_number());
            dmsPersistentEmployee.setNo_of_owner(dmsUsedVehicles.getNo_of_owner());
            dmsPersistentEmployee.setVehicle_purchase_date(dmsUsedVehicles.getVehicle_purchase_date());
            dmsPersistentEmployee.setVehicle_purchase_price(dmsUsedVehicles.getVehicle_purchase_price());
            dmsPersistentEmployee.setInsurance_type(dmsUsedVehicles.getInsurance_type());
            dmsPersistentEmployee.setInsurance_valid_upto(dmsUsedVehicles.getInsurance_valid_upto());
            dmsPersistentEmployee.setDriven_kms(dmsUsedVehicles.getDriven_kms());
            dmsPersistentEmployee.setVehicle_selling_price(dmsUsedVehicles.getVehicle_selling_price());
            dmsPersistentEmployee.setEvaluator_name(dmsUsedVehicles.getEvaluator_name());
            dmsPersistentEmployee.setDoc_list(dmsUsedVehicles.getDoc_list());
            dmsPersistentEmployee.setSoldVehicle(dmsUsedVehicles.getSoldVehicle());
            dmsUsedVehicles.setStatus(Status.EvalutionApprovedOnly);

            return usedVehiclesRepository.save(dmsPersistentEmployee);
        }else{
            throw new DynamicFormsServiceException("Record not found", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Map<String, Object> getUsedVehicleList(Integer pageNo, Integer size, Integer orgId, boolean paginationRequired,  String status) throws DynamicFormsServiceException {
        List<DmsUsedVehiclesRes> dmsUsedVehiclesResList = new ArrayList<>();
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            List<DmsUsedVehicles> dmsUsedVehiclesList =usedVehiclesRepository.usedVehiclesFindByOrgId(orgId,status);
            for (DmsUsedVehicles dmsUsedVehicles : dmsUsedVehiclesList) {
                DmsUsedVehiclesRes dmsUsedVehiclesRes = new DmsUsedVehiclesRes();
                dmsUsedVehiclesRes.setId(dmsUsedVehicles.getId());
                dmsUsedVehiclesRes.setCreated_by(dmsUsedVehicles.getCreated_by());
                dmsUsedVehiclesRes.setCreated_date(dmsUsedVehicles.getCreated_date());
                dmsUsedVehiclesRes.setModified_date(dmsUsedVehicles.getModified_date());
                dmsUsedVehiclesRes.setOrg_id(dmsUsedVehicles.getOrg_id());
                dmsUsedVehiclesRes.setModified_by(dmsUsedVehicles.getModified_by());
                dmsUsedVehiclesRes.setStatus(dmsUsedVehicles.getStatus());
                dmsUsedVehiclesRes.setLocation(dmsUsedVehicles.getLocation());

                DmsBranch dmsBranch = dmsBranchDao.findByName(dmsUsedVehicles.getDealer_code());
                if(dmsBranch!=null){
                    dmsUsedVehiclesRes.setDealer_id(dmsBranch.getBranchId());
                }

                dmsUsedVehiclesRes.setDealer_code(dmsUsedVehicles.getDealer_code());
                dmsUsedVehiclesRes.setMake_id(dmsUsedVehicles.getMake_id());
                dmsUsedVehiclesRes.setModel_id(dmsUsedVehicles.getModel_id());
                dmsUsedVehiclesRes.setVariant(dmsUsedVehicles.getVariant());
                dmsUsedVehiclesRes.setColor(dmsUsedVehicles.getColor());
                dmsUsedVehiclesRes.setFuel(dmsUsedVehicles.getFuel());
                dmsUsedVehiclesRes.setTransmission(dmsUsedVehicles.getTransmission());
                dmsUsedVehiclesRes.setMaking_month(dmsUsedVehicles.getMaking_month());
                dmsUsedVehiclesRes.setMaking_year(dmsUsedVehicles.getMaking_year());
                dmsUsedVehiclesRes.setRc_number(dmsUsedVehicles.getRc_number());
                dmsUsedVehiclesRes.setRegistration_date(dmsUsedVehicles.getRegistration_date());
                dmsUsedVehiclesRes.setRegistration_valid_upto(dmsUsedVehicles.getRegistration_valid_upto());
                dmsUsedVehiclesRes.setVin_number(dmsUsedVehicles.getVin_number());
                dmsUsedVehiclesRes.setEngine_number(dmsUsedVehicles.getEngine_number());
                dmsUsedVehiclesRes.setChassis_number(dmsUsedVehicles.getChassis_number());
                dmsUsedVehiclesRes.setNo_of_owner(dmsUsedVehicles.getNo_of_owner());
                dmsUsedVehiclesRes.setVehicle_purchase_date(dmsUsedVehicles.getVehicle_purchase_date());
                dmsUsedVehiclesRes.setVehicle_purchase_price(dmsUsedVehicles.getVehicle_purchase_price());
                dmsUsedVehiclesRes.setInsurance_type(dmsUsedVehicles.getInsurance_type());
                dmsUsedVehiclesRes.setInsurance_valid_upto(dmsUsedVehicles.getInsurance_valid_upto());
                dmsUsedVehiclesRes.setDriven_kms(dmsUsedVehicles.getDriven_kms());
                dmsUsedVehiclesRes.setVehicle_selling_price(dmsUsedVehicles.getVehicle_selling_price());
                dmsUsedVehiclesRes.setEvaluator_name(dmsUsedVehicles.getEvaluator_name());
                dmsUsedVehiclesRes.setDoc_list(dmsUsedVehicles.getDoc_list());
                dmsUsedVehiclesRes.setUniversal_id(dmsUsedVehicles.getUniversal_id());
                dmsUsedVehiclesRes.setSoldVehicle(dmsUsedVehicles.getSoldVehicle());
                dmsUsedVehiclesRes.setMakeName(otherMakerRepository.findById(dmsUsedVehicles.getMake_id()).get().getOtherMaker());
                dmsUsedVehiclesRes.setModelName(otherModelRepository.findById(dmsUsedVehicles.getModel_id()).get().getOtherModel());


                dmsUsedVehiclesResList.add(dmsUsedVehiclesRes);
            }
            Comparator<DmsUsedVehiclesRes> comparator = (c1, c2) -> {
				return c2.getCreated_date().compareTo(c1.getCreated_date());
			};
			Collections.sort(dmsUsedVehiclesResList, comparator);
            if(paginationRequired) {
//                pageNo = pageNo + 1;
                int totalCnt = dmsUsedVehiclesResList.size();
                int fromIndex = size * (pageNo - 1);
                int toIndex = size * pageNo;

                if (toIndex > totalCnt) {
                    toIndex = totalCnt;
                }
                if (fromIndex > toIndex) {
                    fromIndex = toIndex;
                }
                dmsUsedVehiclesResList = dmsUsedVehiclesResList.subList(fromIndex, toIndex);
                map.put("totalCnt", totalCnt);
                map.put("pageNo", pageNo);
                map.put("size", size);
                map.put("data", dmsUsedVehiclesResList);
            }else {
                map.put("totalCnt", dmsUsedVehiclesResList.size());
                map.put("pageNo", 0);
                map.put("size", dmsUsedVehiclesResList.size());
                map.put("data", dmsUsedVehiclesResList);
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return map;
    }





    @Override
    public Map<String, Object> getUsedVehicleFilterList(UsedVehicleFilterReq usedVehicleFilterReq) throws DynamicFormsServiceException {
        List<DmsUsedVehiclesRes> dmsUsedVehiclesResList = new ArrayList<>();
        Integer pageNo = usedVehicleFilterReq.getPageNo();
        Integer size = usedVehicleFilterReq.getSize();
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            StringBuilder customQuery = new StringBuilder();
            customQuery.append("SELECT * FROM dms_used_vehicles where org_id = ");
            customQuery.append(usedVehicleFilterReq.getOrgId());

            if(usedVehicleFilterReq.getDealer_code()!=null){
                customQuery.append(" and status = '");
                customQuery.append(usedVehicleFilterReq.getStatus());
                customQuery.append("'");
            }
            if(usedVehicleFilterReq.getLocation()!=null && !usedVehicleFilterReq.getLocation().equals("")){
                customQuery.append(" and location in (:Location)");
            }
            if(usedVehicleFilterReq.getDealer_code()!=null && !usedVehicleFilterReq.getDealer_code().equals("")){
                customQuery.append(" and dealer_code in (:Dealer_code)");
            }
            if(usedVehicleFilterReq.getMake_id()!=0){
                customQuery.append(" and make_id = ");
                customQuery.append(usedVehicleFilterReq.getMake_id());
            }
            if(usedVehicleFilterReq.getModel_id()!=0){
                customQuery.append(" and model_id = ");
                customQuery.append(usedVehicleFilterReq.getModel_id());
            }
            if(usedVehicleFilterReq.getRc_number()!=null && !usedVehicleFilterReq.getRc_number().equals("")){
                customQuery.append(" and rc_number = '");
                customQuery.append(usedVehicleFilterReq.getRc_number());
                customQuery.append("'");
            }
            if(usedVehicleFilterReq.getPriceFrom()!=null){
                customQuery.append(" and vehicle_purchase_price >= ");
                customQuery.append(usedVehicleFilterReq.getPriceFrom());
            }
            if(usedVehicleFilterReq.getPriceTo()!=null){
                customQuery.append(" and vehicle_purchase_price <= ");
                customQuery.append(usedVehicleFilterReq.getPriceTo());
            }
            Query q = entityManager.createNativeQuery(customQuery.toString(),DmsUsedVehicles.class);
            q.setParameter("Location", usedVehicleFilterReq.getLocation());
            q.setParameter("Dealer_code",usedVehicleFilterReq.getDealer_code());
            List<DmsUsedVehicles> dmsUsedVehiclesList = q.getResultList();

            System.out.println(customQuery);

            for (DmsUsedVehicles dmsUsedVehicles : dmsUsedVehiclesList) {
                DmsUsedVehiclesRes dmsUsedVehiclesRes = new DmsUsedVehiclesRes();
                dmsUsedVehiclesRes.setId(dmsUsedVehicles.getId());
                dmsUsedVehiclesRes.setCreated_by(dmsUsedVehicles.getCreated_by());
                dmsUsedVehiclesRes.setCreated_date(dmsUsedVehicles.getCreated_date());
                dmsUsedVehiclesRes.setModified_date(dmsUsedVehicles.getModified_date());
                dmsUsedVehiclesRes.setOrg_id(dmsUsedVehicles.getOrg_id());
                dmsUsedVehiclesRes.setModified_by(dmsUsedVehicles.getModified_by());
                dmsUsedVehiclesRes.setStatus(dmsUsedVehicles.getStatus());
                dmsUsedVehiclesRes.setLocation(dmsUsedVehicles.getLocation());

                DmsBranch dmsBranch = dmsBranchDao.findByName(dmsUsedVehicles.getDealer_code());
                if(dmsBranch!=null){
                    dmsUsedVehiclesRes.setDealer_id(dmsBranch.getBranchId());
                }

                dmsUsedVehiclesRes.setDealer_code(dmsUsedVehicles.getDealer_code());
                dmsUsedVehiclesRes.setMake_id(dmsUsedVehicles.getMake_id());
                dmsUsedVehiclesRes.setModel_id(dmsUsedVehicles.getModel_id());
                dmsUsedVehiclesRes.setVariant(dmsUsedVehicles.getVariant());
                dmsUsedVehiclesRes.setColor(dmsUsedVehicles.getColor());
                dmsUsedVehiclesRes.setFuel(dmsUsedVehicles.getFuel());
                dmsUsedVehiclesRes.setTransmission(dmsUsedVehicles.getTransmission());
                dmsUsedVehiclesRes.setMaking_month(dmsUsedVehicles.getMaking_month());
                dmsUsedVehiclesRes.setMaking_year(dmsUsedVehicles.getMaking_year());
                dmsUsedVehiclesRes.setRc_number(dmsUsedVehicles.getRc_number());
                dmsUsedVehiclesRes.setRegistration_date(dmsUsedVehicles.getRegistration_date());
                dmsUsedVehiclesRes.setRegistration_valid_upto(dmsUsedVehicles.getRegistration_valid_upto());
                dmsUsedVehiclesRes.setVin_number(dmsUsedVehicles.getVin_number());
                dmsUsedVehiclesRes.setEngine_number(dmsUsedVehicles.getEngine_number());
                dmsUsedVehiclesRes.setChassis_number(dmsUsedVehicles.getChassis_number());
                dmsUsedVehiclesRes.setNo_of_owner(dmsUsedVehicles.getNo_of_owner());
                dmsUsedVehiclesRes.setVehicle_purchase_date(dmsUsedVehicles.getVehicle_purchase_date());
                dmsUsedVehiclesRes.setVehicle_purchase_price(dmsUsedVehicles.getVehicle_purchase_price());
                dmsUsedVehiclesRes.setInsurance_type(dmsUsedVehicles.getInsurance_type());
                dmsUsedVehiclesRes.setInsurance_valid_upto(dmsUsedVehicles.getInsurance_valid_upto());
                dmsUsedVehiclesRes.setDriven_kms(dmsUsedVehicles.getDriven_kms());
                dmsUsedVehiclesRes.setVehicle_selling_price(dmsUsedVehicles.getVehicle_selling_price());
                dmsUsedVehiclesRes.setEvaluator_name(dmsUsedVehicles.getEvaluator_name());
                dmsUsedVehiclesRes.setDoc_list(dmsUsedVehicles.getDoc_list());
                dmsUsedVehiclesRes.setSoldVehicle(dmsUsedVehicles.getSoldVehicle());
                dmsUsedVehiclesRes.setUniversal_id(dmsUsedVehicles.getUniversal_id());
                dmsUsedVehiclesRes.setMakeName(otherMakerRepository.findById(dmsUsedVehicles.getMake_id()).get().getOtherMaker());
                dmsUsedVehiclesRes.setModelName(otherModelRepository.findById(dmsUsedVehicles.getModel_id()).get().getOtherModel());


                dmsUsedVehiclesResList.add(dmsUsedVehiclesRes);
            }

            if(usedVehicleFilterReq.getPaginationRequired()) {
//                pageNo = pageNo + 1;
                int totalCnt = dmsUsedVehiclesResList.size();
                int fromIndex = size * (pageNo - 1);
                int toIndex = size * pageNo;

                if (toIndex > totalCnt) {
                    toIndex = totalCnt;
                }
                if (fromIndex > toIndex) {
                    fromIndex = toIndex;
                }
                dmsUsedVehiclesResList = dmsUsedVehiclesResList.subList(fromIndex, toIndex);
                map.put("totalCnt", totalCnt);
                map.put("pageNo", pageNo);
                map.put("size", size);
                map.put("data", dmsUsedVehiclesResList);
            }else {
                map.put("totalCnt", dmsUsedVehiclesResList.size());
                map.put("pageNo", 0);
                map.put("size", dmsUsedVehiclesResList.size());
                map.put("data", dmsUsedVehiclesResList);
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return map;
    }

    @Override
    public DmsUsedVehicles getUsedVehicleById(Long recordId) throws DynamicFormsServiceException {
        Optional<DmsUsedVehicles> dmsUsedVehicles = usedVehiclesRepository.findById(recordId);
        if(dmsUsedVehicles.isPresent() && dmsUsedVehicles.get()!=null) {
            return dmsUsedVehicles.get();
        }else{
            return null;
        }
    }
    
    @Override
    public DmsUsedVehicles saveMoveToUsedVehicle(String rcNumber) throws DynamicFormsServiceException {
        DmsUsedVehicles dmsUsedVehicles1 = usedVehiclesRepository.usedVehiclesByRCNo(rcNumber.trim());
        if(dmsUsedVehicles1!= null){
        	dmsUsedVehicles1.setStatus(Status.Unsold);
            return usedVehiclesRepository.save(dmsUsedVehicles1);
        }else {
        	throw new DynamicFormsServiceException("Provide Used Vehicle details First", HttpStatus.ACCEPTED);
        }
    }
    
    @Override
    public DmsUsedVehicles getByRcNumber(String rcNumber) throws DynamicFormsServiceException {
        DmsUsedVehicles dmsUsedVehicles1 = usedVehiclesRepository.usedVehiclesByRCNo(rcNumber.trim());
        if(dmsUsedVehicles1!= null){
            return dmsUsedVehicles1;
        }else {
        	throw new DynamicFormsServiceException("UsedVehicle not present with respect to rc id", HttpStatus.ACCEPTED);
        }
    }
    
    @Override
    public DmsUsedVehicles saveUsedVehicle2(DmsUsedVehicles dmsUsedVehicles,int userID) throws DynamicFormsServiceException {
        DmsUsedVehicles dmsUsedVehicles1 = usedVehiclesRepository.usedVehiclesByRCNo(dmsUsedVehicles.getRc_number().trim());
        if(dmsUsedVehicles1 == null){
            DmsBranch dmsBranch = dmsBranchDao.findByName(dmsUsedVehicles.getDealer_code());
            if(dmsBranch!=null){
                dmsUsedVehicles.setDealer_id(dmsBranch.getBranchId());
            }
            if(dmsUsedVehicles.getMake_id()==208) {
            	OtherMaker obj=new OtherMaker();
            	obj.setOtherMaker(dmsUsedVehicles.getNewMake());
            	obj.setStatus("Active");
            	obj.setOrgId(String.valueOf(dmsUsedVehicles.getOrg_id()));
            	obj.setCreatedBy(String.valueOf(userID));
            	obj.setCreatedAt(new Date().toString());
            	OtherMaker saveMake = otherMakerRepository.save(obj);
            	OtherModel obj2=new OtherModel();
            	obj2.setOtherMaker(dmsUsedVehicles.getNewMake());
            	obj2.setOtherModel(dmsUsedVehicles.getNewModel());
            	obj2.setOthermakerId(saveMake.getId());
            	obj2.setStatus("Active");
            	obj2.setOrgId(String.valueOf(dmsUsedVehicles.getOrg_id()));
            	obj2.setCreatedBy(String.valueOf(userID));
            	obj2.setCreatedAt(new Date());
            	OtherModel saveModel = otherModelRepository.save(obj2);
            	dmsUsedVehicles.setMake_id(saveMake.getId());
            	dmsUsedVehicles.setModel_id(saveModel.getId());
            }
            dmsUsedVehicles.setCreated_date(new Date());
            dmsUsedVehicles.setCreated_by(userID);
            return usedVehiclesRepository.save(dmsUsedVehicles);
        }else {
           throw new DynamicFormsServiceException("This RC number vehicle already registered", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
