package com.automate.df.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import com.automate.df.dao.EmpRepo;
import com.automate.df.entity.sales.employee.DMSEmployee;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.automate.df.dao.dashboard.DmsEmployeeAttachmentsDao;
import com.automate.df.dao.oh.DmsAddressDao;
import com.automate.df.dao.oh.DmsBranchDao;
import com.automate.df.dao.oh.DmsDepartmentRepo;
import com.automate.df.dao.oh.DmsDeptDesignationMappingRepo;
import com.automate.df.dao.oh.DmsDesignationRepo;
import com.automate.df.dao.oh.DmsEmployeeAttendancedao;
import com.automate.df.dao.oh.DmsGradeDao;
import com.automate.df.dao.oh.DmsHolidayListDao;
import com.automate.df.dao.oh.EmpLocationMappingDao;
import com.automate.df.dao.oh.LocationNodeDataDao;
import com.automate.df.dao.oh.LocationNodeDefDao;
import com.automate.df.dao.salesgap.DmsEmployeeRepo;
import com.automate.df.dao.salesgap.DmsRoleRepo;
import com.automate.df.dao.salesgap.EmployeeAddress;
import com.automate.df.entity.oh.DmsAddress;
import com.automate.df.entity.oh.DmsBranch;
import com.automate.df.entity.oh.DmsDesignation;
import com.automate.df.entity.oh.DmsGrade;
import com.automate.df.entity.oh.EmpLocationMapping;
import com.automate.df.entity.oh.LocationNodeData;
import com.automate.df.entity.oh.LocationNodeDef;
import com.automate.df.entity.sales.DmsEmployeeAttendanceEntity;
import com.automate.df.entity.salesgap.DmsEmployee;
import com.automate.df.exception.DynamicFormsServiceException;
import com.automate.df.model.AcitveMappingOrgChartRes;
import com.automate.df.model.AttendanceCount;
import com.automate.df.model.BranchResponce;
import com.automate.df.model.BulkUploadResponse;
import com.automate.df.model.oh.EmployeeRoot;
import com.automate.df.model.oh.EmployeeRootV2;
import com.automate.df.model.oh.LevelDDData;
import com.automate.df.model.oh.LevelDataReq;
import com.automate.df.model.oh.LevelDropDownData;
import com.automate.df.model.oh.LevelMapping;
import com.automate.df.model.oh.LevelReq;
import com.automate.df.model.oh.LocationDefNodeRes;
import com.automate.df.model.oh.LocationNodeDataV2;
import com.automate.df.model.oh.OHEmpLevelMapping;
import com.automate.df.model.oh.OHEmpLevelMappingV2;
import com.automate.df.model.oh.OHEmpLevelUpdateMapReq;
import com.automate.df.model.oh.OHLeveDeleteReq;
import com.automate.df.model.oh.OHLeveUpdateReq;
import com.automate.df.model.oh.OHLevelReq;
import com.automate.df.model.oh.OHLevelUpdateReq;
import com.automate.df.model.oh.OHNodeUpdateReq;
import com.automate.df.model.oh.OHRes;
import com.automate.df.model.oh.PostOffice;
import com.automate.df.model.oh.PostOfficeRoot;
import com.automate.df.model.salesgap.TargetDropDownV2;
import com.automate.df.model.salesgap.TargetDropDownV3;
import com.automate.df.model.salesgap.TargetRoleRes;
import com.automate.df.model.salesgap.TeamAttendanceResponse;
import com.automate.df.model.salesgap.TeamAttendanceResponse3;
import com.automate.df.service.OHService;
import com.automate.df.util.ObjectMapperUtils;
import com.automate.df.util.PasswordEncryptor;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author sruja
 *
 */

@Service
@Slf4j
public class OHServiceImpl implements OHService {

	@Autowired
	Environment env;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	LocationNodeDataDao locationNodeDataDao;

	@Autowired
	EmpLocationMappingDao empLocationMappingDao;

	@Autowired
	LocationNodeDefDao locationNodeDefDao;

	@Autowired
	DmsAddressDao dmsAddressDao;

	@Autowired
	DmsBranchDao dmsBranchDao;

	@Autowired
	DmsDesignationRepo dmsDesignationRepo;
	
	@Autowired
	DmsDepartmentRepo dmsDepartmentRepo;
	@Autowired
	DmsDeptDesignationMappingRepo dmsDeptDesignationMappingRepo;
	@Autowired
	DmsRoleRepo dmsRoleRepo;

	@Autowired
	private EmpRepo empRepo;

	@Override
	public List<OHRes> getOHDropdown(Integer orgId, Integer empId, Integer id) throws DynamicFormsServiceException {
		List<OHRes> list = new ArrayList<>();
		try {
			List<LocationNodeData> nodeList = locationNodeDataDao.getLocationNodeData(id,
					empLocationMappingDao.getLeads(orgId, empId));
			nodeList.forEach(x -> {
				OHRes ohRes = new OHRes();
				ohRes.setKey(String.valueOf(x.getId()));
				ohRes.setValue(x.getCode());
				list.add(ohRes);
			});
			log.debug("nodeList " + nodeList);
		}
		/*
		 * catch (DynamicFormsServiceException e) {
		 * log.error("saveTargetSettingData() ", e); throw e;
		 * 
		 * }
		 */
		catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(env.getProperty("INTERNAL_SERVER_ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return list;
	}

	@Override
	public List<String> getLevelData(Integer orgId, Integer empId) throws DynamicFormsServiceException {
		List<String> list = new ArrayList<>();
		try {
			list = locationNodeDataDao.getEmpLevelData(empLocationMappingDao.getLeads(orgId, empId));
			log.debug("getLevelData ,outputlist :: " + list);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(env.getProperty("INTERNAL_SERVER_ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return list;
	}

	@Override
	public List<OHRes> getEmpParentDropdown(Integer orgId, Integer empId) throws DynamicFormsServiceException {
		List<OHRes> list = new ArrayList<>();
		try {
			List<Integer> leadIdList = empLocationMappingDao.getLeads(orgId, empId);
			List<LocationNodeDef> opList = locationNodeDefDao.getNodeDefData(orgId,
					locationNodeDataDao.getEmpLevelData(leadIdList));
			String topLevel = null;
			////System.out.println("opList " + opList);
			if (null != opList && !opList.isEmpty()) {
				topLevel = opList.get(0).getLocationNodeDefType();
			} else {
				throw new DynamicFormsServiceException("No Level Data found for the given empId and OrgId",
						HttpStatus.UNPROCESSABLE_ENTITY);
			}
			log.debug("orgID:" + orgId + ",empId:" + empId + ",topLevel:" + topLevel);
			List<LocationNodeData> nodeList = locationNodeDataDao.getParentEmpDropdown(topLevel, leadIdList);

			nodeList.forEach(x -> {
				OHRes ohRes = new OHRes();
				ohRes.setKey(String.valueOf(x.getId()));
				ohRes.setValue(x.getCode());
				list.add(ohRes);
			});
			log.debug("nodeList " + nodeList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(env.getProperty("INTERNAL_SERVER_ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return list;
	}

	@Override
	public String addOHMapping(LevelDataReq req) throws DynamicFormsServiceException {
		try {
			boolean isRoot = req.isRootLevel();
			log.debug("isRoot Level " + isRoot);
			Integer orgId = req.getOrgId();
			Integer empId = req.getEmpId();
			log.debug("orgId ::" + orgId + ", empId " + empId);
			if (isRoot) {

				for (LevelDropDownData data : req.getData()) {

					LocationNodeData nodeData = new LocationNodeData();
					// nodeData.set

				}

			} else {

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(env.getProperty("INTERNAL_SERVER_ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return null;
	}

	@Override
	public List<OHRes> getEmpBranches(Integer orgId, Integer empId) throws DynamicFormsServiceException {
		List<OHRes> list = new ArrayList<>();
		try {
			List<Integer> leadIdList = empLocationMappingDao.getLeads(orgId, empId);
			List<LocationNodeDef> opList = locationNodeDefDao.getNodeDefData(orgId,
					locationNodeDataDao.getEmpLevelData(leadIdList));
			String leastLevelMinusOne = null;
			if (null != opList && !opList.isEmpty()) {
				////System.out.println("opList " + opList);
				leastLevelMinusOne = opList.get(opList.size() - 2).getLocationNodeDefType();
			} else {
				throw new DynamicFormsServiceException("No Level Data found for the given empId and OrgId",
						HttpStatus.UNPROCESSABLE_ENTITY);
			}
			log.debug("orgID:" + orgId + ",empId:" + empId + ",leastLevelMinusOne:" + leastLevelMinusOne);
			List<LocationNodeData> nodeList = locationNodeDataDao.getParentEmpDropdown(leastLevelMinusOne, leadIdList);

			nodeList.forEach(x -> {
				OHRes ohRes = new OHRes();
				ohRes.setKey(String.valueOf(x.getId()));
				ohRes.setValue(x.getCode());
				list.add(ohRes);
			});
			log.debug("nodeList " + nodeList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(env.getProperty("INTERNAL_SERVER_ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return list;
	}

	public String getLevelType(Integer order) {

		Map<Integer, String> map = new HashMap<>();
		map.put(1, "Level1");
		map.put(2, "Level2");
		map.put(3, "Level3");
		map.put(4, "Level4");
		map.put(5, "Level5");
		map.put(6, "Level6");
		map.put(7, "Level7");
		map.put(8, "Level8");
		map.put(9, "Level9");
		map.put(10, "Level10");
		return map.get(order);

	}

	@Override
	public List<LocationNodeData> createLevels(OHLevelReq req) throws DynamicFormsServiceException {
		List<LocationNodeData> resList = new ArrayList<>();
		List<LocationNodeDef> defList = new ArrayList<>();
		try {
			int orgId = req.getOrgId();
			int empId = req.getEmpId();
			for (LevelReq lr : req.getLevelList()) {

				LocationNodeDef locationNodeDef = new LocationNodeDef();
				locationNodeDef.setOrgId(orgId);
				locationNodeDef.setCreatedBy(empId);
				locationNodeDef.setCreatedOn(getCurrentTmeStamp());
				locationNodeDef.setModifiedBy(empId);
				locationNodeDef.setModifiedOn(getCurrentTmeStamp());
				locationNodeDef.setLocationNodeDefName(lr.getLevelDefName());
				locationNodeDef.setLocationNodeDefType(getLevelType(lr.getLevelOrder()));
				locationNodeDef.setParentId(lr.getLevelOrder());
				locationNodeDef.setDisplayName(lr.getLevelDefName());
				locationNodeDef.setActive("Y");
				Optional<LocationNodeDef> nodeDefOpt = locationNodeDefDao.verifyNodeDef(orgId, getLevelType(lr.getLevelOrder()));
				if (!nodeDefOpt.isPresent()) {
					locationNodeDefDao.save(locationNodeDef);
				}
			}
			defList = getOrgLevels(req.getOrgId());
			for (LevelReq lr : req.getLevelList()) {
				List<LevelDDData> levelMappings = lr.getLevelData();

				int levelOrder = lr.getLevelOrder();
				log.debug("levelOrder::" + levelOrder);
				if (levelOrder == 1) {
					for (LevelDDData levelDDData : levelMappings) {
						LocationNodeData lData = new LocationNodeData();
						lData.setCananicalName(levelDDData.getCode());
						lData.setCode(levelDDData.getCode());
						lData.setName(levelDDData.getName());
						lData.setCreatedOn(getCurrentTmeStamp());
						lData.setCreatedBy(req.getEmpId());
						lData.setModifiedOn(getCurrentTmeStamp());
						lData.setModifiedBy(req.getEmpId());
						lData.setParentId("0");
						lData.setRefParentId("0");
						lData.setLeafNode("NO");
						String levelType = getLevelType(levelOrder);
						lData.setType(levelType);
						lData.setOrgId(orgId);
						lData.setActive("Y");
						Integer levelDefId = defList.stream()
								.filter(x -> (x.getLocationNodeDefType().equalsIgnoreCase(levelType)
										&& x.getOrgId() == req.getOrgId()))
								.map(x -> x.getId()).findFirst().get();
						lData.setLocationNodeDefId(levelDefId);
						Optional<LocationNodeData> dbNodeParentlevelData = locationNodeDataDao
								.verifyLevelDataRecord(orgId, lr.getLevelDefType(), levelDDData.getCode());
						if (!dbNodeParentlevelData.isPresent()) {
							log.debug("Level 1 is not there, so inserting into DB");
							resList.add(locationNodeDataDao.save(lData));
						}

					}
					log.debug("Level Data Mapping insertion is done for level1");
				} else {

					Integer locatioNodeId = 0;
					String parentLevel = getParentLevel(levelOrder);
					log.debug("Level Data Mapping insertion started for level: " + levelOrder + ",parentLevel: "
							+ parentLevel);
					if (null != levelMappings) {
						for (LevelDDData levelDDData : levelMappings) {

							String parentMappingCode = levelDDData.getParentMappingCode();
							log.debug("parentMappingCode " + parentMappingCode);
							Optional<LocationNodeData> dbNodeParetDataOpt = locationNodeDataDao
									.verifyLevelDataRecord(orgId, parentLevel, levelDDData.getParentMappingCode());
							log.debug("dbNodeParetDataOpt " + dbNodeParetDataOpt.isPresent());
							if (dbNodeParetDataOpt.isPresent()) {
								LocationNodeData parentData = dbNodeParetDataOpt.get();
								log.debug("dbNodeParentlevelData is presne");
								int parentMappingId = parentData.getId();

								LocationNodeData lData = new LocationNodeData();

								String levelCode = levelDDData.getCode();
								String levelName = levelDDData.getName();
								log.debug("levelcode " + levelCode + " levelName " + levelName);
								boolean postalFlag = isValidPinCode(levelName);
								List<PostOffice> postOfficeObj = new ArrayList<>();
								if (postalFlag) {
									log.debug("Givne data contains valid pincode");
									lData.setCananicalName(parentData.getCananicalName() + "/" + levelName);
									lData.setLeafNode("YES");
									levelName = levelName.replaceAll(" ", "");
									ResponseEntity<String> resEntity = restTemplate.getForEntity(
											"https://api.postalpincode.in/pincode/" + levelName, String.class);
									////System.out.println("root " + resEntity.getBody());
									if (resEntity.getStatusCodeValue() == 200) {
										ObjectMapper om = new ObjectMapper();
										PostOfficeRoot[] proot = om.readValue(resEntity.getBody(),
												PostOfficeRoot[].class);
										List<PostOfficeRoot> pList = Arrays.asList(proot);
										////System.out.println("plist " + pList);
										if (null != pList && !pList.isEmpty()) {
											postOfficeObj = proot[0].getPostOffice();
											if (null != postOfficeObj && !postOfficeObj.isEmpty()) {
												PostOffice po = postOfficeObj.get(0);
												lData.setName(po.getName());

											}
										}

									}
								} else {
									lData.setCananicalName(parentData.getCananicalName() + "/" + levelCode);
									lData.setName(levelName);
									lData.setLeafNode("NO");
								}
								lData.setCode(levelDDData.getCode());

								lData.setCreatedOn(getCurrentTmeStamp());
								lData.setCreatedBy(req.getEmpId());
								lData.setModifiedOn(getCurrentTmeStamp());
								lData.setModifiedBy(req.getEmpId());
								lData.setParentId(String.valueOf(parentMappingId));
								lData.setRefParentId(String.valueOf(parentMappingId));
								String levelType = getLevelType(levelOrder);
								lData.setType(levelType);
								lData.setOrgId(orgId);
								lData.setActive("Y");
								////System.out.println("defList ::" + defList);
								Integer levelDefId = defList.stream()
										.filter(x -> (x.getLocationNodeDefType().equalsIgnoreCase(levelType)
												&& x.getOrgId() == req.getOrgId()))
										.map(x -> x.getId()).findFirst().get();
								lData.setLocationNodeDefId(levelDefId);

								Optional<LocationNodeData> dbNodeParentlevelData = locationNodeDataDao
										.verifyLevelDataRecord(orgId, lr.getLevelDefType(), levelDDData.getCode());
								if (!dbNodeParentlevelData.isPresent()) {
									log.debug("Data is not there, so inserting into DB");
									LocationNodeData dbData = locationNodeDataDao.save(lData);
									locatioNodeId = dbData.getId();
									resList.add(dbData);
								}

								if (postalFlag && null != postOfficeObj && !postOfficeObj.isEmpty()) {
									PostOffice po = postOfficeObj.get(0);
									Integer addressId = createAddress(po);
									createBranch(po, addressId, req.getOrgId(), locatioNodeId);
									
								}

							}
						}
						log.debug("Level Data Mapping insertion ended for level: " + levelOrder + ",parentLevel: "
								+ parentLevel);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(env.getProperty("INTERNAL_SERVER_ERROR"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return resList;
	}

	private void createBranch(PostOffice po, Integer addressId, Integer orgId, Integer locatioNodeId)
			throws DynamicFormsServiceException {
		// TODO Auto-generated method stub
		try {
			DmsBranch obj = new DmsBranch();
			obj.setActive("Y");
			obj.setAdress(addressId);
			obj.setAdress(addressId);
			obj.setName(po.getName());
			obj.setOrgMapId(locatioNodeId);
			dmsBranchDao.save(obj);
			log.debug("Branch creted ::: " + dmsBranchDao);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private Integer createAddress(PostOffice po) throws DynamicFormsServiceException {
		Integer id = 0;
		try {
			DmsAddress obj = new DmsAddress();
			obj.setCity(po.getRegion());
			obj.setCountry(po.getCountry());
			obj.setState(po.getState());
			obj.setDistrict(po.getDistrict());
			obj.setPincode(po.getPincode());
			obj.setActive("Y");
			id = dmsAddressDao.save(obj).getId();
			log.debug("Address creted with ID " + id);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return id;
	}

	private String getParentLevel(int levelOrder) {
		levelOrder = levelOrder - 1;
		return "Level" + levelOrder;
	}

	public Timestamp getCurrentTmeStamp() {
		return new Timestamp(System.currentTimeMillis());
	}

	@Override
	public List<?> removeDataLevels(OHLeveDeleteReq req) throws DynamicFormsServiceException {
		log.debug("removeDataLevels(){}");
		List<LocationNodeData> list = new ArrayList<>();
		try {
			List<String> levels = getOrgLevels(req.getOrgId()).stream().map(x -> x.getLocationNodeDefType())
					.collect(Collectors.toList());
			log.debug("levels before in removeDataLevels" + levels);
			levels = levels.subList(levels.indexOf(req.getLevelCodeToRemove()), levels.size());
			log.debug("levels afterin removeDataLevels" + levels);
			String leafLevel = levels.get(levels.size() - 1);
			log.debug("Leaf level in remove mappings " + leafLevel);
			List<LocationNodeData> leafData = getLevelDataNodes(req.getOrgId(), leafLevel);

			for (String level : levels) {
				locationNodeDefDao.removeLevel(req.getOrgId(), level);
				locationNodeDataDao.removeLevel(req.getOrgId(), level);
			}

			empLocationMappingDao.removeActiveMappings(req.getOrgId(), levels);

			////System.out.println("leafData" + leafData);
			for (LocationNodeData data : leafData) {

				String cName = data.getCananicalName();
				////System.out.println("cName " + cName);
				if (null != cName) {

					String[] tmp = cName.split("/");
					cName = tmp[tmp.length - 1];
					log.debug("Pincode " + cName);

					if (isValidPinCode(cName)) {
						DmsBranch branch = dmsBranchDao.getBranchByOrgMpId(data.getId());
						branch.setActive("N");

						dmsBranchDao.save(branch);
						DmsAddress addr = dmsAddressDao.getById(branch.getAdress());
						addr.setActive("N");
						dmsAddressDao.save(addr);
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return list;

	}

	@Override
	public List<LocationNodeData> setEmpLevelMapping(OHEmpLevelMapping req, String active)
			throws DynamicFormsServiceException {
		List<LocationNodeData> list = new ArrayList<>();
		try {
			List<String> levels = getOrgLevels(req.getOrgId()).stream().map(x -> x.getLocationNodeDefType())
					.collect(Collectors.toList());
			log.debug("levels before " + levels);
			levels = levels.subList(levels.indexOf(req.getLevelCode()), levels.size());
			levels = levels.stream().sorted().collect(Collectors.toList());
			log.debug("levels after " + levels);
			Map<String, List<Integer>> levelIdmap = new LinkedHashMap<>();
			for (int i = 0; i < levels.size(); i++) {
				String level = levels.get(i);
				if (i == 0) {
					levelIdmap.put(level, req.getNodesIds());
				} else {
					if (!levelIdmap.isEmpty()) {
						String previousLevel = getPreviousLevel(level);
						log.debug("level " + level + ",previousLevel:" + previousLevel);
						if (levelIdmap.containsKey(previousLevel)) {
							log.debug("LevelIdMap contains previous level " + level);
							List<LocationNodeData> nodeData = locationNodeDataDao.getNodeDataByParentId(req.getOrgId(),
									level, levelIdmap.get(previousLevel));
							List<Integer> idLists = nodeData.stream().map(x -> x.getId()).collect(Collectors.toList());
							////System.out.println("idLists  " + idLists);
							levelIdmap.put(level, idLists);
							list.addAll(nodeData);
						}
					}

				}

			}
			log.debug("levelIdmap ::" + levelIdmap);
			List<Integer> reqNodeIds = new ArrayList<>();
			levelIdmap.forEach((k, v) -> {
				reqNodeIds.addAll(v);
			});
			log.debug("reqNodeIds:::" + reqNodeIds);
			if (active.equalsIgnoreCase("Y")) {
				for (Integer empId : req.getEmpId()) {

					List<EmpLocationMapping> empLocationMapList = new ArrayList<>();
					for (Integer nodeId : reqNodeIds) {

						EmpLocationMapping emp = new EmpLocationMapping();
						emp.setActive(active);
						emp.setEmpId(String.valueOf(empId));
						emp.setLocationNodeDataId(String.valueOf(nodeId));
						emp.setOrgId(String.valueOf(req.getOrgId()));
						empLocationMapList.add(emp);
					}
					empLocationMappingDao.saveAll(empLocationMapList);
				}
			} else {
				for (Integer empId : req.getEmpId()) {

					List<EmpLocationMapping> empLocationMapList = empLocationMappingDao
							.getSelectedMappingsForEmp(req.getOrgId(), empId, reqNodeIds);
					for (EmpLocationMapping map : empLocationMapList) {
						map.setActive(active);
					}
					empLocationMappingDao.saveAll(empLocationMapList);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return list;

	}


	public String setEmpLevelMappingV2(OHEmpLevelMapping req, String active)
			throws DynamicFormsServiceException {
		List<LocationNodeData> list = new ArrayList<>();
		String res=null;
		try {
			List<String> levels = getOrgLevels(req.getOrgId()).stream().map(x -> x.getLocationNodeDefType())
					.collect(Collectors.toList());
			log.debug("levels before " + levels);
			log.debug("req.getLevelCode():::"+req.getLevelCode());
			levels = levels.subList(levels.indexOf(req.getLevelCode()), levels.size());
			levels = levels.stream().sorted().collect(Collectors.toList());
			log.debug("levels after " + levels);
			Map<String, List<Integer>> levelIdmap = new LinkedHashMap<>();
			for (int i = 0; i < levels.size(); i++) {
				String level = levels.get(i);
				if (i == 0) {
					levelIdmap.put(level, req.getNodesIds());
				} else {
					if (!levelIdmap.isEmpty()) {
						String previousLevel = getPreviousLevel(level);
						log.debug("level " + level + ",previousLevel:" + previousLevel);
						if (levelIdmap.containsKey(previousLevel)) {
							log.debug("LevelIdMap contains previous level " + level);
							List<LocationNodeData> nodeData = locationNodeDataDao.getNodeDataByParentId(req.getOrgId(),
									level, levelIdmap.get(previousLevel));
							List<Integer> idLists = nodeData.stream().map(x -> x.getId()).collect(Collectors.toList());
							////System.out.println("idLists  " + idLists);
							levelIdmap.put(level, idLists);
							list.addAll(nodeData);
						}
					}

				}

			}
			log.debug("levelIdmap ::" + levelIdmap);
			List<Integer> reqNodeIds = new ArrayList<>();
			levelIdmap.forEach((k, v) -> {
				reqNodeIds.addAll(v);
			});
			log.debug("reqNodeIds:::" + reqNodeIds);
			
			List<Integer> leafNodes  = new ArrayList<>();
			reqNodeIds.forEach(x->{
				String leafNode = locationNodeDataDao.verifyLeafNode(x);
				if(leafNode.equalsIgnoreCase("yes")) {
					leafNodes.add(x);
				}
			});
			
			Map<Integer,Boolean> taskMap = new HashMap<>();
			for(Integer node: leafNodes) {
				taskMap.put(node, validateOpenTasks(node,req.getEmpId(),req.getOrgId()));
			};
			
			log.debug("taskMap:"+taskMap);
			boolean taskFlag = false;
			for(Map.Entry<Integer,Boolean> e : taskMap.entrySet()) {
				if(e.getValue().equals(true)) {
					taskFlag = true;
					break;
				}
			}
			log.debug("taskFlag:: "+taskFlag);
			
			if (!taskFlag) {
				log.debug("NO Open tasks for the given emp " + req.getEmpId());
				if (active.equalsIgnoreCase("Y")) {
					for (Integer empId : req.getEmpId()) {

						List<EmpLocationMapping> empLocationMapList = new ArrayList<>();
						for (Integer nodeId : reqNodeIds) {

							EmpLocationMapping emp = new EmpLocationMapping();
							emp.setActive(active);
							emp.setEmpId(String.valueOf(empId));
							emp.setLocationNodeDataId(String.valueOf(nodeId));
							emp.setOrgId(String.valueOf(req.getOrgId()));
							empLocationMapList.add(emp);
						}
						empLocationMappingDao.saveAll(empLocationMapList);
					}
				} else {
					for (Integer empId : req.getEmpId()) {

						List<EmpLocationMapping> empLocationMapList = empLocationMappingDao
								.getSelectedMappingsForEmp(req.getOrgId(), empId, reqNodeIds);
						for (EmpLocationMapping map : empLocationMapList) {
							map.setActive(active);

						}
						empLocationMappingDao.saveAll(empLocationMapList);
					}

				}	
				res = "Successfully removed all active mappings for selected Node";
			} else {
				log.debug("Open tasks are there for the given emp " + req.getEmpId());
				String removed="";
				String notRemoved="";
				for (Integer empId : req.getEmpId()) {
					for (Map.Entry<Integer, Boolean> e : taskMap.entrySet()) {
						if (e.getValue().equals(false)) {
							List<Integer> nodeIdsToRemove = new ArrayList<>();
							nodeIdsToRemove.add(e.getKey());
							List<EmpLocationMapping> empLocationMapList = empLocationMappingDao
									.getSelectedMappingsForEmp(req.getOrgId(), empId, nodeIdsToRemove);
							for (EmpLocationMapping map : empLocationMapList) {
								map.setActive(active);
							}
							empLocationMappingDao.saveAll(empLocationMapList);
							String tmp = " Removed Mapping for Node "+e.getKey() ;
							removed = removed+tmp+System.lineSeparator();
						}else {
							String tmp = " Open tasks are available for branch "+e.getKey() +" for the given employee" ;
							notRemoved = notRemoved+tmp+System.lineSeparator();
						}
					}

				}
				res = removed +notRemoved;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return res;

	}

	
	private boolean validateOpenTasks(Integer nodeId, List<Integer> empList, Integer orgId) throws DynamicFormsServiceException {

		/*
		 * SELECT * FROM salesDataSetup.dms_workflow_task where task_status='ASSIGNED'
		 * AND universal_id in (SELECT crm_universal_id FROM salesDataSetup.dms_lead
		 * where organization_id=11 and sales_consultant='emp1' and branch_id=21 and
		 * lead_stage!='DROPPED');
		 * 
		 * sample query
		 */
		boolean flag =false;
		try {
			for (Integer empId : empList) {
				DmsBranch branch = dmsBranchDao.getBranchByOrgMpId(nodeId);
				if (null != branch) {
					Integer branchId = branch.getBranchId();
					Optional<DmsEmployee> opt = dmsEmployeeRepo.findById(empId);
					if(opt.isPresent()) {
						log.debug("Getting open tasks for branch :" + branchId);
						String empName = opt.get().getEmpName();
						String query = "SELECT * FROM dms_workflow_task where task_status='ASSIGNED' AND universal_id\r\n"
								+ " in (SELECT crm_universal_id FROM dms_lead where organization_id=" + orgId + " \r\n"
								+ " and sales_consultant='"+empName+"' and branch_id=" + branchId + " and lead_stage!='DROPPED')";
						log.debug("QUERY TO CHECK OPEN TASKS "+query);
						List<Object[]> data = entityManager.createNativeQuery(query).getResultList();
						if(null!=data && !data.isEmpty()) {
							flag =true;
						}
					}else {
						throw new DynamicFormsServiceException("No Employee found in Dms Employee table for employee Id: "+empId, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return flag;
	}

	private String getPreviousLevel(String level) {

		if (level.equalsIgnoreCase("Level2")) {
			return "Level1";
		}

		if (level.equalsIgnoreCase("Level3")) {
			return "Level2";
		}

		if (level.equalsIgnoreCase("Level4")) {
			return "Level3";
		}

		if (level.equalsIgnoreCase("Level5")) {
			return "Level4";
		}
		if (level.equalsIgnoreCase("Level6")) {
			return "Level5";
		}
		if (level.equalsIgnoreCase("Level7")) {
			return "Level6";
		}
		if (level.equalsIgnoreCase("Level8")) {
			return "Level7";
		}
		if (level.equalsIgnoreCase("Level9")) {
			return "Level8";
		}
		return null;
	}

	@Override
	public List<LocationNodeDef> getOrgLevels(Integer orgId) throws DynamicFormsServiceException {
		List<LocationNodeDef> list = new ArrayList<>();
		try {
			list = locationNodeDefDao.getLevelByOrgID(orgId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return list;
	}

	@Override
	public List<LocationNodeData> getLevelDataNodes(Integer orgId, String levelCode)
			throws DynamicFormsServiceException {
		List<LocationNodeData> list = new ArrayList<>();
		try {
			list = locationNodeDataDao.getNodeDataByLevel(orgId, levelCode);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return list;
	}

	@Override
	public List<LocationNodeData> updateOrgLevels(OHLeveUpdateReq req) throws DynamicFormsServiceException {
		List<LocationNodeData> resList = new ArrayList<>();
		try {
			int orgId = req.getOrgId();
			int empId = req.getEmpId();
			List<String> levels = getOrgLevels(req.getOrgId()).stream().map(x -> x.getLocationNodeDefType())
					.collect(Collectors.toList());
			log.debug("levels for orgId " + orgId + " is " + levels);

			if (levels.isEmpty()) {
				throw new DynamicFormsServiceException(
						"No levels exists for this Organization, please use /level-data-creation api to create levels",
						HttpStatus.INTERNAL_SERVER_ERROR);
			} else {

				for (LevelReq lr : req.getLevelList()) {
					LocationNodeDef locationNodeDef = new LocationNodeDef();
					locationNodeDef.setOrgId(orgId);
					locationNodeDef.setCreatedBy(empId);
					locationNodeDef.setCreatedOn(getCurrentTmeStamp());
					locationNodeDef.setModifiedBy(empId);
					locationNodeDef.setModifiedOn(getCurrentTmeStamp());
					locationNodeDef.setLocationNodeDefName(lr.getLevelDefName());
					locationNodeDef.setLocationNodeDefType(lr.getLevelDefType());
					locationNodeDef.setParentId(lr.getLevelOrder());
					locationNodeDef.setActive("Y");
					Optional<LocationNodeDef> nodeDefOpt = locationNodeDefDao.verifyNodeDef(orgId,
							lr.getLevelDefType());
					if (!nodeDefOpt.isPresent()) {
						locationNodeDefDao.save(locationNodeDef);
					}
				}
				for (LevelReq lr : req.getLevelList()) {
					List<LevelDDData> levelMappings = lr.getLevelData();
					int levelOrder = lr.getLevelOrder();
					log.debug("levelOrder::" + levelOrder);
					if (levelOrder == 1) {
						for (LevelDDData levelDDData : levelMappings) {
							LocationNodeData lData = new LocationNodeData();
							lData.setCananicalName(levelDDData.getCode());
							lData.setCode(levelDDData.getCode());
							lData.setName(levelDDData.getName());
							lData.setCreatedOn(getCurrentTmeStamp());
							lData.setCreatedBy(req.getEmpId());
							lData.setModifiedOn(getCurrentTmeStamp());
							lData.setModifiedBy(req.getEmpId());
							lData.setParentId("0");
							lData.setRefParentId("0");
							lData.setType(lr.getLevelDefType());
							lData.setOrgId(orgId);
							lData.setActive("Y");
							////System.out.println("saving node data");
							Optional<LocationNodeData> dbNodeParentlevelData = locationNodeDataDao
									.verifyLevelDataRecord(orgId, lr.getLevelDefType(), levelDDData.getCode());
							if (!dbNodeParentlevelData.isPresent()) {
								log.debug("Level 1 is not there, so inserting into DB");
								resList.add(locationNodeDataDao.save(lData));
							}

						}
						log.debug("Level Data Mapping insertion is done for level1");
					} else {
						String parentLevel = getParentLevel(levelOrder);
						log.debug("Level Data Mapping insertion started for level: " + levelOrder + ",parentLevel: "
								+ parentLevel);
						for (LevelDDData levelDDData : levelMappings) {

							String parentMappingCode = levelDDData.getParentMappingCode();
							log.debug("parentMappingCode " + parentMappingCode);
							////System.out.println("levelDDData.getParentMappingCode() " + levelDDData.getParentMappingCode());
							Optional<LocationNodeData> dbNodeParetDataOpt = locationNodeDataDao
									.verifyLevelDataRecord(orgId, parentLevel, levelDDData.getParentMappingCode());
							log.debug("dbNodeParetDataOpt " + dbNodeParetDataOpt.isPresent());
							if (dbNodeParetDataOpt.isPresent()) {
								LocationNodeData parentData = dbNodeParetDataOpt.get();
								log.debug("dbNodeParentlevelData is presne");
								int parentMappingId = parentData.getId();

								LocationNodeData lData = new LocationNodeData();
								lData.setCananicalName(parentData.getCananicalName() + "/" + levelDDData.getCode());
								lData.setCode(levelDDData.getCode());
								lData.setName(levelDDData.getName());
								lData.setCreatedOn(getCurrentTmeStamp());
								lData.setCreatedBy(req.getEmpId());
								lData.setModifiedOn(getCurrentTmeStamp());
								lData.setModifiedBy(req.getEmpId());
								lData.setParentId(String.valueOf(parentMappingId));
								lData.setRefParentId(String.valueOf(parentMappingId));
								lData.setType(lr.getLevelDefType());
								lData.setOrgId(orgId);
								lData.setActive("Y");
								// LocationNodeData nodeData = locationNodeDataDao.save(lData);

								Optional<LocationNodeData> dbNodeParentlevelData = locationNodeDataDao
										.verifyLevelDataRecord(orgId, lr.getLevelDefType(), levelDDData.getCode());
								if (!dbNodeParentlevelData.isPresent()) {
									log.debug("Data is not there, so inserting into DB");
									resList.add(locationNodeDataDao.save(lData));
								}
							}
						}
						log.debug("Level Data Mapping insertion ended for level: " + levelOrder + ",parentLevel: "
								+ parentLevel);

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return resList;

	}

	@Override
	public String updateEmpLevelMapping(OHEmpLevelUpdateMapReq req) throws DynamicFormsServiceException {
		String res = null;
		try {
			Integer orgId = req.getOrgId();
			boolean removeFlag = req.isRemoveCurrentActiveLevel();
			log.debug("Remove Current Flag is ::" + removeFlag);

			if (removeFlag) {
				try {
					for (Integer empId : req.getEmpId()) {
						log.debug("removing active level for emp id " + empId);
						empLocationMappingDao.updateActiveEmpMapStatus(orgId, empId);
						res = "Successfully removed active mappings for given employee list " + req.getEmpId();
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new DynamicFormsServiceException(
							"Error ocurred while removing the active mappings for given employees",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
				res = "removed successfully";
			}
			List<Integer> removeNodeIdsList = req.getRemoveActiveNodeIds();
			if (null != removeNodeIdsList && !removeNodeIdsList.isEmpty()) {
				log.debug("removeNodeIdsList is :" + removeNodeIdsList);
				try {
					for (Integer empId : req.getEmpId()) {
						for (Integer nodeIdToRemove : removeNodeIdsList) {
							List<LocationNodeData> activeParentNodeList = getActiveEmpMappings(orgId, empId);
							List<String> levels = activeParentNodeList.stream().map(x -> x.getType())
									.collect(Collectors.toList());
							levels = levels.stream().sorted().collect(Collectors.toList());
							log.debug("Levels list " + levels);
							OHEmpLevelMapping rq = new OHEmpLevelMapping();
							List<Integer> eList = new ArrayList<>();
							eList.add(empId);
							rq.setEmpId(eList);
							rq.setOrgId(orgId);
							rq.setLevelCode(locationNodeDataDao.getLevelname(nodeIdToRemove));
							rq.setNodesIds(removeNodeIdsList);
							res = setEmpLevelMappingV2(rq, "N");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new DynamicFormsServiceException(
							"Error ocurred while removing the selected nodes for given employees",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			
			}
			List<Integer> updatesNodesInCurrentLevel = req.getUpdateNodesInCurrentLevel();
			if (null != updatesNodesInCurrentLevel && !updatesNodesInCurrentLevel.isEmpty()) {
				log.debug("updatesNodesInCurrentLevel is :" + removeNodeIdsList);
				try {
					for (Integer empId : req.getEmpId()) {
						List<LocationNodeData> activeParentNodeList = getActiveEmpMappings(orgId, empId);
						List<String> levels = activeParentNodeList.stream().map(x -> x.getType())
								.collect(Collectors.toList());
						levels = levels.stream().sorted().collect(Collectors.toList());
						log.debug("Levels list " + levels);
						OHEmpLevelMapping rq = new OHEmpLevelMapping();
						List<Integer> eList = new ArrayList<>();
						eList.add(empId);
						rq.setEmpId(eList);
						rq.setOrgId(orgId);
						rq.setLevelCode(levels.get(0));
						rq.setNodesIds(updatesNodesInCurrentLevel);

						setEmpLevelMapping(rq, "Y");

					}

				} catch (Exception e) {
					e.printStackTrace();
					throw new DynamicFormsServiceException(
							"Error ocurred while adding the active mappings for given employees",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
				res = "Successfully added selected nodes for given employees";
			}

			List<Integer> newlyaddedNodesList = req.getNewlyAddedNodeIds();
			if (null != newlyaddedNodesList && !newlyaddedNodesList.isEmpty()) {
				try {
					if (null != req.getNewLevelAdded() && validateNewLevelAdded(req.getNewLevelAdded())) {
						log.debug("newlyaddedNodesList is :" + newlyaddedNodesList + " and level :"
								+ req.getNewLevelAdded());
						OHEmpLevelMapping rq = new OHEmpLevelMapping();
						rq.setEmpId(req.getEmpId());
						rq.setOrgId(orgId);
						rq.setLevelCode(req.getNewLevelAdded());
						rq.setNodesIds(newlyaddedNodesList);
						setEmpLevelMapping(rq, "Y");
					} else {
						throw new DynamicFormsServiceException(
								"Please check the level entered,given level is not valid one", HttpStatus.BAD_REQUEST);
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new DynamicFormsServiceException(
							"Error ocurred while adding the active mappings for given employees",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
				res = "Successfully added given level with mappings for given employees";
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return res;
	}

	private boolean validateNewLevelAdded(String newLevelAdded) {

		if (newLevelAdded.startsWith("Level")) {
			return true;
		}
		return false;
	}

	@Override
	public List<LocationNodeData> getActiveEmpMappings(Integer orgId, Integer empId)
			throws DynamicFormsServiceException {
		List<LocationNodeData> list = new ArrayList<>();
		try {
			list = locationNodeDataDao.getActiveLevelsForEmp(orgId, empId);
			List<String> levels = list.stream().map(x -> x.getType()).sorted().collect(Collectors.toList());
			log.debug("Levels list " + levels);
			if (null != levels && !levels.isEmpty()) {
				list = list.stream().filter(x -> x.getType().equalsIgnoreCase(levels.get(0)))
						.collect(Collectors.toList());
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return list;
	}

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	DmsEmployeeRepo dmsEmployeeRepo;

	@Autowired
	SalesGapServiceImpl salesGapServiceImpl;

	@Override
	public Map<String, Object> getEmployeesListWithMapping(Integer pageNo, Integer size,Integer orgId)
			throws DynamicFormsServiceException {
		List<EmployeeRoot> empRootList = new ArrayList<>();
		Map<String, Object> map = new LinkedHashMap<>();
		try {
	

			List<DmsEmployee> empList =dmsEmployeeRepo.getEmployeesByOrg(orgId);
			for (DmsEmployee dmsEmp : empList) {
				EmployeeRoot emp = new EmployeeRoot();

				emp.setEmpId(dmsEmp.getEmp_id());
				emp.setOrgId(dmsEmp.getOrg());
				emp.setBranchId(dmsEmp.getBranch());
				emp.setCognitoName(dmsEmp.getCogintoName());
				emp.setDesignationName(salesGapServiceImpl.getDesignationName(dmsEmp.getDesignationId()));
				emp.setDesignation(dmsEmp.getDesignationId());
			;
				emp.setReportingTo(salesGapServiceImpl.getEmpName(dmsEmp.getReportingTo()));
				emp.setEmpName(dmsEmp.getEmpName());

				if (null != dmsEmp.getOrg()) {
					List<LocationNodeData> activeParentNodeList = getActiveEmpMappings(
							Integer.parseInt(dmsEmp.getOrg()), dmsEmp.getEmp_id());

					List<String> levels = activeParentNodeList.stream().map(x -> x.getType())
							.collect(Collectors.toList());
					levels = levels.stream().sorted().collect(Collectors.toList());

					if (null != levels && !levels.isEmpty()) {
						emp.setMappedLevel(levels.get(0));
					}
				}
				emp.setLevelDisplayName(locationNodeDefDao.getLevelnameByType(emp.getMappedLevel(), orgId));
				
				
				Map<String, Object> mappings = getMappingByEmpId(dmsEmp.getEmp_id() );
				List<String> mapList = new ArrayList<>();
				mappings.forEach((k, v) -> {
					List<LocationNodeData> dataList = (List<LocationNodeData>) v;
					if (null != dataList) {

						dataList.forEach(x -> {

							LocationNodeData data = (LocationNodeData) x;
							mapList.add(data.getCananicalName());
						});
					}

				});;
				TargetRoleRes roleres = salesGapServiceImpl.getEmpRoleDataV2(dmsEmp.getEmp_id());
				if (roleres != null) {
					emp.setHrmsRole(roleres.getRoleName());
					emp.setHrmsRoleId(roleres.getRoleId());
				}
				emp.setMappings(mapList);
				emp.setOrgName(salesGapServiceImpl.getOrgName(emp.getOrgId()));

				empRootList.add(emp);
			}
			pageNo = pageNo + 1;
			int totalCnt = empRootList.size();
			int fromIndex = size * (pageNo - 1);
			int toIndex = size * pageNo;

			if (toIndex > totalCnt) {
				toIndex = totalCnt;
			}
			if (fromIndex > toIndex) {
				fromIndex = toIndex;
			}
			empRootList = empRootList.subList(fromIndex, toIndex);
			map.put("totalCnt", totalCnt);
			map.put("pageNo", pageNo);
			map.put("size", size);
			map.put("data", empRootList);

		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return map;
	}

	private void addBranchMappings(Integer orgId) throws DynamicFormsServiceException {
		try {
			List<LocationNodeDef> defList = getOrgLevels(orgId);

			if (null != defList && !defList.isEmpty()) {
				List<String> levels = defList.stream().map(x -> x.getLocationNodeDefType()).sorted()
						.collect(Collectors.toList());
				String leaflevel = levels.get(levels.size() - 1);
				log.debug("leaflevel for org " + orgId + " is " + leaflevel);

				List<LocationNodeData> dataList = locationNodeDataDao.getNodeDataByLevel(orgId, leaflevel);

				if (null != dataList && !dataList.isEmpty()) {

				} else {

					throw new DynamicFormsServiceException("No Data nodes present for the given org and level",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

			} else {
				throw new DynamicFormsServiceException("No Levels present for the given org",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	boolean isValidPinCode(String pinCode) {

		String regex = "^[1-9]{1}[0-9]{2}\\s{0,1}[0-9]{3}$";
		Pattern p = Pattern.compile(regex);
		if (pinCode == null) {
			return false;
		}
		java.util.regex.Matcher m = p.matcher(pinCode);

		return m.matches();
	}

	@Override
	public List<LocationDefNodeRes> getActiveEmpMappingsAll(Integer orgId) throws DynamicFormsServiceException {
		List<LocationNodeData> list = new ArrayList<>();
		List<LocationDefNodeRes> defList = new ArrayList<>();
		AcitveMappingOrgChartRes res = new AcitveMappingOrgChartRes();

		try {
			defList = ObjectMapperUtils.mapAll(locationNodeDefDao.getLevelByOrgID(orgId), LocationDefNodeRes.class);
			list = locationNodeDataDao.getActiveLevelsForOrg(orgId);
			List<String> levels = list.stream().map(x -> x.getType()).sorted().collect(Collectors.toList());
			log.debug("Levels list " + levels);
			Map<String, List<LocationNodeData>> map = new LinkedHashMap<>();
			if (null != levels && !levels.isEmpty()) {
				for (String level : levels) {
					map.put(level, list.stream().filter(x -> x.getType().equalsIgnoreCase(level))
							.collect(Collectors.toList()));
				}
			}
			res.setMap(map);
			for (LocationDefNodeRes def : defList) {
				List<LocationNodeData> list_2 = map.get(def.getLocationNodeDefType());
				if (null != list_2 && !list_2.isEmpty()) {
					String s = list_2.stream().map(x -> x.getLeafNode()).findAny().get();
					if (s.equalsIgnoreCase("YES")) {
						def.setLeafLevel(true);
					} else {
						def.setLeafLevel(false);

					}
				} else {
					def.setLeafLevel(false);
				}
				def.setNodes(map.get(def.getLocationNodeDefType()));
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return defList;
	}

	@Override
	public List<EmployeeRootV2> getMappedEmployees(String type,String orgId) throws DynamicFormsServiceException {

		List<EmployeeRoot> empRootList = new ArrayList<>();
		Map<String, Object> map = new LinkedHashMap<>();
		List<EmployeeRootV2> v2List = new ArrayList<>();
		try {
			List<DmsEmployee> empList = dmsEmployeeRepo.getEmployeesByOrg(Integer.parseInt(orgId));
			for (DmsEmployee dmsEmp : empList) {
			
				EmployeeRoot emp = new EmployeeRoot();
				emp.setEmpId(dmsEmp.getEmp_id());
				emp.setOrgId(dmsEmp.getOrg());
				emp.setEmpName(dmsEmp.getEmpName());
				if (null != dmsEmp.getOrg()) {
					List<LocationNodeData> activeParentNodeList = getActiveEmpMappings(
							Integer.parseInt(dmsEmp.getOrg()), dmsEmp.getEmp_id());
					List<String> levels = activeParentNodeList.stream().map(x -> x.getType())
							.collect(Collectors.toList());
					levels = levels.stream().sorted().collect(Collectors.toList());
					if (null != levels && !levels.isEmpty()) {
						emp.setMappedLevel(levels.get(0));
					}
				}
				empRootList.add(emp);
				
			}
			v2List = ObjectMapperUtils.mapAll(empRootList, EmployeeRootV2.class);
			if (type.equalsIgnoreCase("mapped")) {
				v2List = v2List.stream().filter(x -> x.getMappedLevel() != null).collect(Collectors.toList());

				for (EmployeeRootV2 empv2 : v2List) {

					empv2.setNodes(locationNodeDataDao.getActiveLevelsForEmpWithOutOrg(empv2.getEmpId()));
				}

			}
			if (type.equalsIgnoreCase("unmapped")) {
				v2List = v2List.stream().filter(x -> x.getMappedLevel() == null).collect(Collectors.toList());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return v2List;

	}

	@Override
	public String updateOrgLevels(OHLevelUpdateReq req) throws DynamicFormsServiceException {

		log.debug("updateOrgLevels(){}");
		List<LocationNodeData> list = new ArrayList<>();
		try {
			Integer orgId = req.getOrgId();
			String levelCode = req.getLevelCode();
			String displayName = req.getUpdateDisplayName();

			if (null != displayName && displayName.length() > 0) {
				log.debug("Updating displayName for the given level " + levelCode + " of org " + orgId);
				locationNodeDefDao.updateDisplayName(orgId, req.getLevelCode(), displayName);
			}

			List<Integer> datanodes = req.getRemoveDataNodes();
			////System.out.println("datanodes " + datanodes);
			if (null != datanodes && !datanodes.isEmpty()) {
				List<String> levels = getOrgLevels(orgId).stream().map(x -> x.getLocationNodeDefType())
						.collect(Collectors.toList());
				log.debug("levels before " + levels);
				levels = levels.subList(levels.indexOf(levelCode), levels.size());
				levels = levels.stream().sorted().collect(Collectors.toList());
				log.debug("levels after " + levels);
				Map<String, List<Integer>> levelIdmap = new LinkedHashMap<>();

				for (int i = 0; i < levels.size(); i++) {
					String level = levels.get(i);
					if (i == 0) {
						levelIdmap.put(level, datanodes);
					} else {
						if (!levelIdmap.isEmpty()) {
							String previousLevel = getPreviousLevel(level);
							log.debug("level " + level + ",previousLevel:" + previousLevel);
							if (levelIdmap.containsKey(previousLevel)) {
								log.debug("LevelIdMap contains previous level " + level);
								List<LocationNodeData> nodeData = locationNodeDataDao
										.getNodeDataByParentId(req.getOrgId(), level, levelIdmap.get(previousLevel));
								List<Integer> idLists = nodeData.stream().map(x -> x.getId())
										.collect(Collectors.toList());
								levelIdmap.put(level, idLists);
								list.addAll(nodeData);
							}
						}

					}

				}
				log.debug("levelIdmap ::" + levelIdmap);
				List<Integer> reqNodeIds = new ArrayList<>();
				levelIdmap.forEach((k, v) -> {
					reqNodeIds.addAll(v);
				});
				log.debug("reqNodeIds to delete:::" + reqNodeIds);

				locationNodeDataDao.removeOrgLevelNodes(orgId, reqNodeIds);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return "Data Updated Successfully";

	}

	@Override
	public String updateNodes(OHNodeUpdateReq req) throws DynamicFormsServiceException {

		try {
			List<LocationNodeDataV2> activeParentNodeList = ObjectMapperUtils
					.mapAll(locationNodeDataDao.getActiveLevelsForOrg(req.getOrgId()), LocationNodeDataV2.class);
			List<String> levels = activeParentNodeList.stream().map(x -> x.getType()).collect(Collectors.toList());
			levels = levels.stream().sorted().collect(Collectors.toList());
			log.debug("USER LEVELS " + levels + " and size:" + levels.size());
			String orgLeafLevel = null;
			if (null != levels && levels.size() > 0) {
				orgLeafLevel = levels.get(levels.size() - 1);
				log.debug("orgLeafLevel:::" + orgLeafLevel);
			}
			String inputLeafLevel = locationNodeDataDao.getLevelname(req.getDataNodeId());
			log.debug("inputLeafLevel:::"+inputLeafLevel);
			
			if(inputLeafLevel.equalsIgnoreCase(orgLeafLevel)) {
				log.debug("Updating for leaf level");
				locationNodeDataDao.updateNodeDisplayName(req.getOrgId(), req.getDataNodeId(), req.getUpdateNodeNameTo());
				dmsBranchDao.updateBranchName(req.getDataNodeId(),req.getUpdateNodeNameTo());				
			}else {
				log.debug("Updating for non leaf level");
				locationNodeDataDao.updateNodeDisplayName(req.getOrgId(), req.getDataNodeId(), req.getUpdateNodeNameTo());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return "Updated node name Successfully";
	}

	@Override
	public Map<String, Object> getMappingByEmpId(Integer empId) throws DynamicFormsServiceException {
		Map<String, Object> map = new LinkedHashMap<>();
		try {
			List<LocationNodeData> activeParentNodeList = locationNodeDataDao.getActiveLevelsForEmpWithOutOrg(empId);
			List<String> levels = activeParentNodeList.stream().map(x -> x.getType()).collect(Collectors.toList());
			levels = levels.stream().sorted().collect(Collectors.toList());
			if (null != levels && !levels.isEmpty()) {
				for (String level : levels) {
					map.put(level, activeParentNodeList.stream().filter(x -> x.getType().equalsIgnoreCase(level))
							.collect(Collectors.toList()));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return map;
	}


	private List<TargetDropDownV2> buildDropDown(Integer id, Integer branch, Integer orgId) {
		//String query = "SELECT emp_id,emp_name FROM dms_employee where reporting_to=<ID> and branch=<BRANCH> and org=<ORGID>";
		String query = "\r\n"
				+ "select emp_id,emp_name from dms_employee where org = <ORGID> "
				+ "and reporting_to=<ID> and emp_id in (select emp_id from emp_location_mapping where org_id=<ORGID>"
				+ " and location_node_data_id ='<BRANCH>')";
		query = query.replaceAll("<ID>", String.valueOf(id));
		query = query.replaceAll("<BRANCH>", String.valueOf(branch));
		query = query.replaceAll("<ORGID>", String.valueOf(orgId));
		log.debug("buildDropDown query "+query);
		List<TargetDropDownV2> list = new ArrayList<>();
		List<Object[]> data = entityManager.createNativeQuery(query).getResultList();

		for (Object[] arr : data) {
			TargetDropDownV2 trRoot = new TargetDropDownV2();
			trRoot.setCode(String.valueOf(arr[0]));
			trRoot.setName(String.valueOf(arr[1]));
			
			list.add(trRoot);
		}
		list = list.stream().distinct().collect(Collectors.toList());
		

		return list;
	}
	
	@Cacheable (value = "empDataCache",key="#branchId_1")
	private List<TargetDropDownV2> buildDropDownV2(Integer id, Integer branch, Integer orgId) {
		//String query = "SELECT emp_id,emp_name FROM dms_employee where reporting_to=<ID> and branch=<BRANCH> and org=<ORGID>";
		String query = "\r\n"
				+ "select emp_id,emp_name from dms_employee where org = <ORGID> "
				+ "and reporting_to=<ID> and status = 'Active' and emp_id in (select emp_id from emp_location_mapping where org_id=<ORGID>"
				+ " and location_node_data_id in (select org_map_id from dms_branch where branch_id=<BRANCH>))";
		query = query.replaceAll("<ID>", String.valueOf(id));
		query = query.replaceAll("<BRANCH>", String.valueOf(branch));
		query = query.replaceAll("<ORGID>", String.valueOf(orgId));
		log.debug("buildDropDown query "+query);
		List<TargetDropDownV2> list = new ArrayList<>();
		List<Object[]> data = entityManager.createNativeQuery(query).getResultList();

		for (Object[] arr : data) {
			TargetDropDownV2 trRoot = new TargetDropDownV2();
			trRoot.setCode(String.valueOf(arr[0]));
			trRoot.setName(String.valueOf(arr[1]));
			
			list.add(trRoot);
		}
		list = list.stream().distinct().collect(Collectors.toList());
		

		return list;
	}



	@Override
	public Map<String, Object> getActiveDropdownsV2(List<Integer> levelList, Integer orgId, Integer empId)
			throws DynamicFormsServiceException {
		List<LocationNodeData> list = new ArrayList<>();
		List<Integer> reqNodeIds = new ArrayList<>();
		Map<String, Object> resMap = new LinkedHashMap<>();
		try {
			for (Integer nodeId : levelList) {
				log.debug("nodeId::::"+nodeId);
				String levelName = locationNodeDataDao.getLevelname(nodeId);
				log.debug("Given node level " + levelName + " and nodeId " + nodeId);
				List<String> levels = getOrgLevels(orgId).stream().map(x -> x.getLocationNodeDefType()).collect(Collectors.toList());
				log.debug("levels before " + levels);
				log.debug("levelName::"+levelName);
				levels = levels.subList(levels.indexOf(levelName), levels.size());
				levels = levels.stream().sorted().collect(Collectors.toList());
				log.debug("levels after " + levels);
				Map<String, List<Integer>> levelIdmap = new LinkedHashMap<>();
				for (int i = 0; i < levels.size(); i++) {
					String level = levels.get(i);
					if (i == 0) {
						List<Integer> tmp = new ArrayList<>();
						tmp.add(nodeId);
						levelIdmap.put(level, tmp);
					} else {
						if (!levelIdmap.isEmpty()) {
							String previousLevel = getPreviousLevel(level);
							log.debug("level " + level + ",previousLevel:" + previousLevel);
							if (levelIdmap.containsKey(previousLevel)) {
								log.debug("LevelIdMap contains previous level " + level);
								List<LocationNodeData> nodeData = locationNodeDataDao.getNodeDataByParentId(orgId,
										level, levelIdmap.get(previousLevel));
								List<Integer> idLists = nodeData.stream().map(x -> x.getId())
										.collect(Collectors.toList());
								levelIdmap.put(level, idLists);
								list.addAll(nodeData);
							}
						}

					}
				}
				log.debug("levelIdmap ::" + levelIdmap);

				levelIdmap.forEach((k, v) -> {
					reqNodeIds.addAll(v);
				});
			}

			log.debug("reqNodeIds:::" + reqNodeIds);
			List<LocationNodeDataV2> activeParentNodeList = ObjectMapperUtils
					.mapAll(locationNodeDataDao.getActiveLevelsForEmpWithOutOrg(empId), LocationNodeDataV2.class);
			List<String> levels = activeParentNodeList.stream().map(x -> x.getType()).collect(Collectors.toList());
			levels = levels.stream().sorted().collect(Collectors.toList());
			log.debug("USER LEVELS " + levels + " and size:" + levels.size());
			String leafLevel = null;
			if (null != levels && levels.size() > 0) {
				leafLevel = levels.get(levels.size() - 1);
				log.debug("leafLevel:::" + leafLevel);
			}
			List<LocationNodeData> leafNodeList = locationNodeDataDao.getNodeDataByLevel(orgId, leafLevel);

			List<Integer> leafNodeIdList = leafNodeList.stream().map(x -> x.getId()).collect(Collectors.toList());
			List<Integer> finalleafNodeIdList1 = new ArrayList<>();
			for (Integer i : reqNodeIds) {
				if (leafNodeIdList.contains(i)) {
					finalleafNodeIdList1.add(i);
				}
			}
			List<Integer> finalleafNodeIdList2 = new ArrayList<>();
			for (Integer i : finalleafNodeIdList1) {
				if (levelList.contains(i)) {
					finalleafNodeIdList2.add(i);
				}
			}
			List<Integer> finalleafNodeIdList = new ArrayList<>(new HashSet<Integer>(finalleafNodeIdList2));
			log.debug("finalleafNodeIdList::" + finalleafNodeIdList);

			for (Integer nodeId : finalleafNodeIdList) {
				log.debug("Getting data for nodeId::"+nodeId);
				DmsBranch branch = dmsBranchDao.getBranchByOrgMpId(nodeId);
				int branchId = branch.getBranchId();
				log.debug("branchId::" + branchId);
				List<DmsEmployee> branchEmpList = dmsEmployeeRepo.getEmployeesTeamByOrg(orgId);
				Optional<DmsEmployee> empOpt = branchEmpList.stream().filter(x -> x.getEmp_id() == empId).findAny();
				////System.out.println("empOpt ::" + empOpt.isPresent());
				if (empOpt.isPresent()) {
					DmsEmployee emp = empOpt.get();
					//Map<String, Object> reportingHierarchyMap = getReportingHierarchy(emp, branchId, orgId);
					Map<String, Object> reportingHierarchyMap = getReportingHierarchy2(emp, nodeId, orgId,branchId);
					reportingHierarchyMap = formatReportingHierarchy2(reportingHierarchyMap);
					resMap.put(String.valueOf(branchId), reportingHierarchyMap);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return resMap;
	}
	
	//teamattendance
	@Override
	public Map<String, Object> getTeamActiveDropdownsV2(List<Integer> levelList, Integer orgId, Integer empId)
			throws DynamicFormsServiceException {
		List<LocationNodeData> list = new ArrayList<>();
		List<Integer> reqNodeIds = new ArrayList<>();
		Map<String, Object> resMap = new LinkedHashMap<>();
		try {
			for (Integer nodeId : levelList) {
				log.debug("nodeId::::"+nodeId);
				String levelName = locationNodeDataDao.getLevelTeamname(nodeId);
				log.debug("Given node level " + levelName + " and nodeId " + nodeId);
				List<String> levels = getOrgLevels(orgId).stream().map(x -> x.getLocationNodeDefType()).collect(Collectors.toList());
				log.debug("levels before " + levels);
				log.debug("levelName::"+levelName);
				levels = levels.subList(levels.indexOf(levelName), levels.size());
				levels = levels.stream().sorted().collect(Collectors.toList());
				log.debug("levels after " + levels);
				Map<String, List<Integer>> levelIdmap = new LinkedHashMap<>();
				for (int i = 0; i < levels.size(); i++) {
					String level = levels.get(i);
					if (i == 0) {
						List<Integer> tmp = new ArrayList<>();
						tmp.add(nodeId);
						levelIdmap.put(level, tmp);
					} else {
						if (!levelIdmap.isEmpty()) {
							String previousLevel = getPreviousLevel(level);
							log.debug("level " + level + ",previousLevel:" + previousLevel);
							if (levelIdmap.containsKey(previousLevel)) {
								log.debug("LevelIdMap contains previous level " + level);
								List<LocationNodeData> nodeData = locationNodeDataDao.getNodeDataByTeamParentId(orgId,
										level, levelIdmap.get(previousLevel));
								List<Integer> idLists = nodeData.stream().map(x -> x.getId())
										.collect(Collectors.toList());
								levelIdmap.put(level, idLists);
								list.addAll(nodeData);
							}
						}

					}
				}
				log.debug("levelIdmap ::" + levelIdmap);

				levelIdmap.forEach((k, v) -> {
					reqNodeIds.addAll(v);
				});
			}

			log.debug("reqNodeIds:::" + reqNodeIds);
			List<LocationNodeDataV2> activeParentNodeList = ObjectMapperUtils
					.mapAll(locationNodeDataDao.getActiveLevelsForTeamEmpWithOutOrg(empId), LocationNodeDataV2.class);
			List<String> levels = activeParentNodeList.stream().map(x -> x.getType()).collect(Collectors.toList());
			levels = levels.stream().sorted().collect(Collectors.toList());
			log.debug("USER LEVELS " + levels + " and size:" + levels.size());
			String leafLevel = null;
			if (null != levels && levels.size() > 0) {
				leafLevel = levels.get(levels.size() - 1);
				log.debug("leafLevel:::" + leafLevel);
			}
			List<LocationNodeData> leafNodeList = locationNodeDataDao.getNodeDataTeamByLevel(orgId, leafLevel);

			List<Integer> leafNodeIdList = leafNodeList.stream().map(x -> x.getId()).collect(Collectors.toList());
			List<Integer> finalleafNodeIdList1 = new ArrayList<>();
			for (Integer i : reqNodeIds) {
				if (leafNodeIdList.contains(i)) {
					finalleafNodeIdList1.add(i);
				}
			}
			List<Integer> finalleafNodeIdList2 = new ArrayList<>();
			for (Integer i : finalleafNodeIdList1) {
				if (levelList.contains(i)) {
					finalleafNodeIdList2.add(i);
				}
			}
			List<Integer> finalleafNodeIdList = new ArrayList<>(new HashSet<Integer>(finalleafNodeIdList2));
			log.debug("finalleafNodeIdList::" + finalleafNodeIdList);

			for (Integer nodeId : finalleafNodeIdList) {
				log.debug("Getting data for nodeId::"+nodeId);
				DmsBranch branch = dmsBranchDao.getBranchTeamByOrgMpId(nodeId);
				int branchId = branch.getBranchId();
				log.debug("branchId::" + branchId);
				List<DmsEmployee> branchEmpList = dmsEmployeeRepo.getEmployeesTeamByOrg(orgId);
				Optional<DmsEmployee> empOpt = branchEmpList.stream().filter(x -> x.getEmp_id() == empId).findAny();
				////System.out.println("empOpt ::" + empOpt.isPresent());
				if (empOpt.isPresent()) {
					DmsEmployee emp = empOpt.get();
					//Map<String, Object> reportingHierarchyMap = getReportingHierarchy(emp, branchId, orgId);
					Map<String, Object> reportingHierarchyMap = getReportingTeamHierarchy2(emp, nodeId, orgId,branchId);
					reportingHierarchyMap = formatTeamReportingHierarchy2(reportingHierarchyMap);
					resMap.put(String.valueOf(branchId), reportingHierarchyMap);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return resMap;
	}

	@Override
	public Map<String, Object> getActiveDropdowns(Integer orgId, Integer empId) throws DynamicFormsServiceException {
		Map<String, Object> map = new LinkedHashMap<>();
		try {
			List<LocationNodeDataV2> activeParentNodeList = ObjectMapperUtils
					.mapAll(locationNodeDataDao.getActiveLevelsForEmpWithOutOrg(empId), LocationNodeDataV2.class);
			List<String> levels = activeParentNodeList.stream().map(x -> x.getType()).collect(Collectors.toList());
			levels = levels.stream().sorted().collect(Collectors.toList());
			log.debug("USER LEVELS " + levels + " and size:" + levels.size());
			if (null != levels && levels.size() > 0) {
				String leafLevel = levels.get(levels.size() - 1);
				log.debug("leafLevel:::" + leafLevel);
				Integer maxLevel = getMaxLevel();
				log.debug("maxLevel:::" + maxLevel);
				if (null != levels && !levels.isEmpty()) {
					for (String level : levels) {
						if (level.equalsIgnoreCase(leafLevel)) {
							List<LocationNodeDataV2> leafNodes = activeParentNodeList.stream()
									.filter(x -> x.getType().equalsIgnoreCase(level)).collect(Collectors.toList());

							for (LocationNodeDataV2 ld : leafNodes) {
								int nodeId = ld.getId();
								log.debug("Node Id " + nodeId);
								DmsBranch branch = dmsBranchDao.getBranchByOrgMpId(nodeId);
								int branchId = branch.getBranchId();
								log.debug("branchId::" + branchId);
								Integer roleId = dmsEmployeeRepo.getEmpHrmsRole(empId);
								List<DmsEmployee> branchEmpList = dmsEmployeeRepo.getEmployeesByOrgBranch(orgId,
										branchId,roleId);
								log.debug("branchEmpList size ::" + branchEmpList.size());
								Optional<DmsEmployee> empOpt = branchEmpList.stream()
										.filter(x -> x.getEmp_id() == empId).findAny();
								////System.out.println("empOpt ::" + empOpt.isPresent());
								if (empOpt.isPresent()) {
									DmsEmployee emp = empOpt.get();
									Map<String, Object> reportingHierarchyMap = getReportingHierarchy(emp, branchId,
											orgId);
									reportingHierarchyMap = formatReportingHierarchy(reportingHierarchyMap);
									ld.setChilds(reportingHierarchyMap);
								}
							}

							map.put(level, leafNodes);
						} else {

							map.put(level, activeParentNodeList.stream()
									.filter(x -> x.getType().equalsIgnoreCase(level)).collect(Collectors.toList()));
						}
					}
				}
			} else {
				throw new DynamicFormsServiceException("No Org Hierarchy Data is present for the given empId",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return map;
	}

	private Map<String, Object> formatReportingHierarchy(Map<String, Object> hMap) {
		if (null != hMap) {
			AtomicInteger a = new AtomicInteger(0);
			for (Map.Entry<String, Object> mapentry : hMap.entrySet()) {
				Map<String, Object> map2 = (Map<String, Object>) mapentry.getValue();
				a.incrementAndGet();
				for (Map.Entry<String, Object> mapentry_1 : map2.entrySet()) {
					List<TargetDropDownV2> ddList = (List<TargetDropDownV2>) mapentry_1.getValue();
					ddList=	ddList.stream().distinct().collect(Collectors.toList());
					ddList.forEach(x -> {
						x.setParentId(mapentry_1.getKey());
						x.setOrder(a.intValue());
					});

				}
			}

			for (Map.Entry<String, Object> mapentry : hMap.entrySet()) {
				Map<String, Object> map2 = (Map<String, Object>) mapentry.getValue();
				List<TargetDropDownV2> list = new ArrayList<>();

				for (Map.Entry<String, Object> mapentry_1 : map2.entrySet()) {
					List<TargetDropDownV2> ddList = (List<TargetDropDownV2>) mapentry_1.getValue();
					ddList=	ddList.stream().distinct().collect(Collectors.toList());
					list.addAll(ddList);
				}
				list=	list.stream().distinct().collect(Collectors.toList());
				hMap.put(mapentry.getKey(), list);
			}
		}
		return hMap;
	}

	public Map<String, Object> getReportingHierarchy(DmsEmployee emp, int branchId, Integer orgId) {
		log.debug("Inside getReportingHierarchy,branchId: "+branchId+",orgId:"+orgId);
		Map<String, Object> empDtaMap = new LinkedHashMap<>();
		try {
			Integer maxLevel = getMaxLevel();
			log.debug("maxLevel:::" + maxLevel);
			Integer empLevel = 0;
			if (null != emp.getDesignationId()) {
				Integer empDesId = Integer.parseInt(emp.getDesignationId());
				log.debug("empDesigntaion:::" + empDesId);
				Optional<DmsDesignation> desOpt = dmsDesignationRepo.findById(empDesId);
				if (desOpt.isPresent()) {
					empLevel = desOpt.get().getLevel();
				} else {
					throw new DynamicFormsServiceException(
							"Given emp does not have valid designation in dms_designation", HttpStatus.BAD_REQUEST);
				}

				log.debug("Given emp level is " + empLevel);

				List<TargetDropDownV2> empList = buildDropDown(emp.getEmp_id(), branchId, orgId);
				if (!empList.isEmpty() && maxLevel >= (empLevel + 1)) {
					Map<String, Object> map = new LinkedHashMap<>();
					map.put(String.valueOf(emp.getEmp_id()), empList);
					empDtaMap.put(getLevelName(empLevel + 1), map);
				}
				Map<String, Object> map1 = new LinkedHashMap<>();
				Map<String, Object> map2 = new LinkedHashMap<>();
				Map<String, Object> map3 = new LinkedHashMap<>();
				Map<String, Object> map4 = new LinkedHashMap<>();
				Map<String, Object> map5 = new LinkedHashMap<>();
				for (TargetDropDownV2 td : empList) {
					List<TargetDropDownV2> empList1 = buildDropDown(Integer.parseInt(td.getCode()), branchId, orgId);
					map1.put(td.getCode(), empList1);

					for (TargetDropDownV2 td1 : empList1) {
						List<TargetDropDownV2> empList2 = buildDropDown(Integer.parseInt(td1.getCode()), branchId,
								orgId);
						map2.put(td1.getCode(), empList2);

						for (TargetDropDownV2 td2 : empList2) {
							List<TargetDropDownV2> empList3 = buildDropDown(Integer.parseInt(td2.getCode()), branchId,
									orgId);
							map3.put(td2.getCode(), empList3);

							for (TargetDropDownV2 td3 : empList3) {
								List<TargetDropDownV2> empList4 = buildDropDown(Integer.parseInt(td3.getCode()),
										branchId, orgId);
								map4.put(td3.getCode(), empList4);

								for (TargetDropDownV2 td4 : empList4) {
									List<TargetDropDownV2> empList5 = buildDropDown(Integer.parseInt(td4.getCode()),
											branchId, orgId);
									map5.put(td4.getCode(), empList5);
								}
							}
						}
					}
				}
				if (maxLevel >= (empLevel + 2) && !validateMap(map1)) {
					empDtaMap.put(getLevelName(empLevel + 2), map1);
				}
				if (maxLevel >= (empLevel + 3) && !validateMap(map2)) {
					empDtaMap.put(getLevelName(empLevel + 3), map2);
				}
				if (maxLevel >= (empLevel + 4) && !validateMap(map3)) {
					empDtaMap.put(getLevelName(empLevel + 4), map3);
				}
				if (maxLevel >= (empLevel + 5) && !validateMap(map4)) {
					empDtaMap.put(getLevelName(empLevel + 5), map4);
				}

			}
			log.debug("empDtaMap in getReportingHierarchy:::"+empDtaMap);		
		} catch (Exception e) {
			e.printStackTrace();
		}

		return empDtaMap;
	}

	
	

	public boolean validateMap(Map<String, Object> map) {
		// TODO Auto-generated method stub
		boolean flag = false;

		for (Map.Entry<String, Object> entry : map.entrySet()) {
			List<TargetDropDownV2> tmp = (List<TargetDropDownV2>) entry.getValue();
			if (!tmp.isEmpty()) {
				flag = false;
				break;
			} else {
				flag = true;
			}
		}
		////System.out.println("flag:::" + flag);
		return flag;

	}

	@Autowired
	DmsGradeDao dmsGradeDao;

	private Integer getMaxLevel() {

		List<DmsGrade> list = dmsGradeDao.findAll();
		return list.size();

	}

	public String getLevelName(int level) throws DynamicFormsServiceException {

		List<DmsGrade> list = dmsGradeDao.findAll();
		String levelName = null;
		Optional<DmsGrade> opt = list.stream().filter(x -> x.getLevel() == level).findFirst();
		if (opt.isPresent()) {
			levelName = opt.get().getLevelName();
			levelName.replaceAll(",", "/");
		} else {
			throw new DynamicFormsServiceException(
					"There is no valid levelname for given level " + level + " in dms_grade",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return levelName;
	}
	
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    Set<Object> seen = ConcurrentHashMap.newKeySet();
	    return t -> seen.add(keyExtractor.apply(t));
	}

	

	@Override
	public Map<String, Object> getActiveLevels(Integer orgId, Integer empId) throws DynamicFormsServiceException {
		
		Map<String, Object> finalMap = new LinkedHashMap<>();
		Map<String, Object> map = new LinkedHashMap<>();
		try {
			List<LocationNodeDataV2> activeParentNodeList = ObjectMapperUtils.mapAll(locationNodeDataDao.getActiveLevelsForEmpWithOutOrg(empId), LocationNodeDataV2.class);
			List<LocationNodeDef> list = locationNodeDefDao.getLevelByOrgID(orgId);
			List<String> orgLevels = list.stream().map(x->x.getLocationNodeDefType()).collect(Collectors.toList());
			List<String> activeLevels = activeParentNodeList.stream().map(x->x.getType()).distinct().collect(Collectors.toList());
			List<String> disabledLevels = new ArrayList<>(orgLevels);
			disabledLevels.removeAll(activeLevels);
			log.debug("disabledLevels "+disabledLevels);
			log.debug("activeLevels "+activeLevels);
			for(LocationNodeDataV2 d : activeParentNodeList) {
				d.setDisabled("N");
			}
			
			if(null!=disabledLevels && !disabledLevels.isEmpty()) {
				for(String disabledLevel: disabledLevels) {
					List<LocationNodeData> levelDataList = locationNodeDataDao.getNodeDataByLevel(orgId, disabledLevel);
					List<LocationNodeDataV2> levelDataListv2 = ObjectMapperUtils.mapAll(levelDataList, LocationNodeDataV2.class);
					for(LocationNodeDataV2 d : levelDataListv2) {
						d.setDisabled("Y");
					}
					activeParentNodeList.addAll(levelDataListv2);
				}
			}
			List<String> levels = activeParentNodeList.stream().map(x -> x.getType()).collect(Collectors.toList());
			levels = levels.stream().sorted().distinct().collect(Collectors.toList());
			log.debug("USER LEVELS " + levels + " and size:" + levels.size());
			if (null != levels && levels.size() > 0) {
				String leafLevel = levels.get(levels.size() - 1);
				log.debug("leafLevel:::" + leafLevel);
				Integer maxLevel = getMaxLevel();
				log.debug("maxLevel:::" + maxLevel);
			
					for (String level : levels) {
						if (level.equalsIgnoreCase(leafLevel)) {
							List<LocationNodeDataV2> leafNodes = activeParentNodeList.stream().filter(x -> x.getType().equalsIgnoreCase(level)).collect(Collectors.toList());

							for (LocationNodeDataV2 ld : leafNodes) {
								Map<String, Object> empDtaMap = new LinkedHashMap<>();
								int nodeId = ld.getId();
								log.debug("Node Id " + nodeId);
								DmsBranch branch = dmsBranchDao.getBranchByOrgMpId(nodeId);
								int branchId = branch.getBranchId();
								log.debug("branchId::" + branchId);
								Integer roleId = dmsEmployeeRepo.getEmpHrmsRole(empId);
								List<DmsEmployee> branchEmpList = dmsEmployeeRepo.getEmployeesByOrgBranch(orgId,
										branchId,roleId);
								log.debug("branchEmpList size ::" + branchEmpList.size());
						
							}

							map.put(level, leafNodes);
							List<LocationNodeData> levelDataList = locationNodeDataDao.getNodeDataByLevel(orgId, level);
							List<LocationNodeDataV2> levelDataListv2 = ObjectMapperUtils.mapAll(levelDataList, LocationNodeDataV2.class);
							List<LocationNodeDataV2> mapList = (List<LocationNodeDataV2>)map.get(level);
							
							levelDataListv2.forEach(x->{
							
							
								if(!mapList.contains(x)) {
									x.setDisabled("Y");
									mapList.add(x);
								}
							});
								map.put(level, mapList);
								
						} else {

							map.put(level, activeParentNodeList.stream().filter(x -> x.getType().equalsIgnoreCase(level)).collect(Collectors.toList()));
							
							List<LocationNodeData> levelDataList = locationNodeDataDao.getNodeDataByLevel(orgId, level);
							List<LocationNodeDataV2> levelDataListv2 = ObjectMapperUtils.mapAll(levelDataList, LocationNodeDataV2.class);
							List<LocationNodeDataV2> mapList = (List<LocationNodeDataV2>)map.get(level);
							
							levelDataListv2.forEach(x->{
							
							
								if(!mapList.contains(x)) {
									x.setDisabled("Y");
									mapList.add(x);
								}
							});
							map.put(level, mapList);
						//	map.put(getLevelName(level,orgId), mapList);
							
						}
					}
				
			} else {
				throw new DynamicFormsServiceException("No Org Hierarchy Data is present for the given empId",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			AtomicInteger atomicInt = new AtomicInteger(0);
			map.forEach((k,v)->{
				atomicInt.getAndIncrement();
				List<LocationNodeDataV2> l = (List<LocationNodeDataV2>)v;
				l.forEach(z->{
					z.setOrder(atomicInt.intValue());
				
				});
			});
			
			map.forEach((k,v)->{
				List<LocationNodeDataV2> l = (List<LocationNodeDataV2>)v;
				Map<String,List<LocationNodeDataV2>> innerMap = new LinkedHashMap<>();
				innerMap.put("sublevels",l);
				map.put(k, innerMap);
				finalMap.put(getLevelName(k, orgId), innerMap);
			});
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return finalMap;

	}
	
	
	private String getLevelName(String level, Integer orgId) {
		////System.out.println("level:"+level+",orgId:"+orgId);
		return locationNodeDefDao.getLevelnameByType(level,orgId);
	}

	@Override
	public List<LocationNodeData> setEmpLevelMappingMultiple(OHEmpLevelMappingV2 req, String active)
			throws DynamicFormsServiceException {
		List<LocationNodeData> list = new ArrayList<>();
		try {
			List<LevelMapping> levelMappings = req.getLevels();
			log.debug("levelMappings:::" + levelMappings);
			for (LevelMapping levelMap : levelMappings) {
				log.debug("Started Mapping for " + levelMap.toString());
				List<String> levels = getOrgLevels(req.getOrgId()).stream().map(x -> x.getLocationNodeDefType())
						.collect(Collectors.toList());
				log.debug("levels before " + levels);
				levels = levels.subList(levels.indexOf(levelMap.getLevelCode()), levels.size());
				levels = levels.stream().sorted().collect(Collectors.toList());
				log.debug("levels after " + levels);
				Map<String, List<Integer>> levelIdmap = new LinkedHashMap<>();
				for (int i = 0; i < levels.size(); i++) {
					String level = levels.get(i);
					if (i == 0) {
						levelIdmap.put(level, levelMap.getNodesIds());
					} else {
						if (!levelIdmap.isEmpty()) {
							String previousLevel = getPreviousLevel(level);
							log.debug("level " + level + ",previousLevel:" + previousLevel);
							if (levelIdmap.containsKey(previousLevel)) {
								log.debug("LevelIdMap contains previous level " + level);
								List<LocationNodeData> nodeData = locationNodeDataDao
										.getNodeDataByParentId(req.getOrgId(), level, levelIdmap.get(previousLevel));
								List<Integer> idLists = nodeData.stream().map(x -> x.getId())
										.collect(Collectors.toList());
								////System.out.println("idLists  " + idLists);
								levelIdmap.put(level, idLists);
								list.addAll(nodeData);
							}
						}

					}

				}
				log.debug("levelIdmap ::" + levelIdmap);
				List<Integer> reqNodeIds = new ArrayList<>();
				levelIdmap.forEach((k, v) -> {
					reqNodeIds.addAll(v);
				});
				log.debug("reqNodeIds:::" + reqNodeIds);
				if (active.equalsIgnoreCase("Y")) {
					List<EmpLocationMapping> empLocationMapList = new ArrayList<>();
					for (Integer nodeId : reqNodeIds) {
						EmpLocationMapping emp = new EmpLocationMapping();
						emp.setActive(active);
						emp.setEmpId(String.valueOf(levelMap.getEmpId()));
						emp.setLocationNodeDataId(String.valueOf(nodeId));
						emp.setBranchId(dmsAddressDao.getBranchId(nodeId));
						emp.setOrgId(String.valueOf(req.getOrgId()));
						List<Integer> tmpList = new ArrayList<>();
						tmpList.add(nodeId);
						List<EmpLocationMapping> dbList = empLocationMappingDao.getSelectedMappingsForEmp(req.getOrgId(), levelMap.getEmpId(), tmpList);
						if (null != dbList && dbList.isEmpty()) {
							empLocationMapList.add(emp);
						}
					}
					empLocationMappingDao.saveAll(empLocationMapList);
				} else {
					List<EmpLocationMapping> empLocationMapList = empLocationMappingDao.getSelectedMappingsForEmp(req.getOrgId(), levelMap.getEmpId(), reqNodeIds);
					for (EmpLocationMapping map : empLocationMapList) {
						map.setActive(active);
					}
					empLocationMappingDao.saveAll(empLocationMapList);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return list;

	}

	@Override
	public  List<LocationNodeDataV2>  getActiveBranches(Integer orgId, Integer empId) throws DynamicFormsServiceException {
		List<LocationNodeDataV2> leafNodes = new ArrayList<>();
		try {
			List<LocationNodeDataV2> activeParentNodeList = ObjectMapperUtils.mapAll(locationNodeDataDao.getActiveLevelsForEmpWithOutOrg(empId), LocationNodeDataV2.class);
			List<LocationNodeDef> list = locationNodeDefDao.getLevelByOrgID(orgId);
			List<String> orgLevels = list.stream().map(x->x.getLocationNodeDefType()).collect(Collectors.toList());
			List<String> activeLevels = activeParentNodeList.stream().map(x->x.getType()).distinct().collect(Collectors.toList());
			
			List<String> disabledLevels = new ArrayList<>(orgLevels);
			disabledLevels.removeAll(activeLevels);
			
			log.debug("disabledLevels "+disabledLevels);
			log.debug("activeLevels "+activeLevels);
			for(LocationNodeDataV2 d : activeParentNodeList) {
				d.setDisabled("N");
			}
			
			
			

			List<String> levels = activeParentNodeList.stream().map(x -> x.getType()).collect(Collectors.toList());
			levels = levels.stream().sorted().distinct().collect(Collectors.toList());
			log.debug("USER LEVELS " + levels + " and size:" + levels.size());
			if (null != levels && levels.size() > 0) {
				String leafLevel = levels.get(levels.size() - 1);
				log.debug("leafLevel:::" + leafLevel);
				Integer maxLevel = getMaxLevel();
				log.debug("maxLevel:::" + maxLevel);
			
					for (String level : levels) {
						if (level.equalsIgnoreCase(leafLevel)) {
							leafNodes = activeParentNodeList.stream().filter(x -> x.getType().equalsIgnoreCase(level)).collect(Collectors.toList());
						} 
					}
				
			} else {
				throw new DynamicFormsServiceException("No Org Hierarchy Data is present for the given empId",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			for(LocationNodeDataV2 l: leafNodes) {
				log.debug("Node Def ID: "+l.getId());
				l.setBranch(dmsBranchDao.getBranchByOrgMpId(l.getId()).getBranchId());
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return leafNodes;

	
	}
	
	//@Cacheable (value = "empDataCache",key="#branchId")
	public Map<String, Object> getReportingHierarchyV2(DmsEmployee emp, int branchId, Integer orgId) {
		log.debug("Inside getReportingHierarchyV2,branchId: "+branchId+",orgId:"+orgId);
		Map<String, Object> empDtaMap = new LinkedHashMap<>();
		try {
			Integer maxLevel = getMaxLevel();
			log.debug("maxLevel:::" + maxLevel);
			Integer empLevel = 0;
			if (null != emp.getDesignationId()) {
				Integer empDesId = Integer.parseInt(emp.getDesignationId());
				log.debug("empDesigntaion:::" + empDesId);
				Optional<DmsDesignation> desOpt = dmsDesignationRepo.findById(empDesId);
				if (desOpt.isPresent()) {
					empLevel = desOpt.get().getLevel();
				} else {
					throw new DynamicFormsServiceException(
							"Given emp does not have valid designation in dms_designation", HttpStatus.BAD_REQUEST);
				}

				log.debug("Given emp level is " + empLevel);

				List<TargetDropDownV2> empList = buildDropDownV2(emp.getEmp_id(), branchId, orgId);
				if (!empList.isEmpty() && maxLevel >= (empLevel + 1)) {
					Map<String, Object> map = new LinkedHashMap<>();
					map.put(String.valueOf(emp.getEmp_id()), empList);
					empDtaMap.put(getLevelName(empLevel + 1), map);
				}
				Map<String, Object> map1 = new LinkedHashMap<>();
				Map<String, Object> map2 = new LinkedHashMap<>();
				Map<String, Object> map3 = new LinkedHashMap<>();
				Map<String, Object> map4 = new LinkedHashMap<>();
				Map<String, Object> map5 = new LinkedHashMap<>();
				for (TargetDropDownV2 td : empList) {
					List<TargetDropDownV2> empList1 = buildDropDownV2(Integer.parseInt(td.getCode()), branchId, orgId);
					map1.put(td.getCode(), empList1);

					for (TargetDropDownV2 td1 : empList1) {
						List<TargetDropDownV2> empList2 = buildDropDownV2(Integer.parseInt(td1.getCode()), branchId,
								orgId);
						map2.put(td1.getCode(), empList2);

						for (TargetDropDownV2 td2 : empList2) {
							List<TargetDropDownV2> empList3 = buildDropDownV2(Integer.parseInt(td2.getCode()), branchId,
									orgId);
							map3.put(td2.getCode(), empList3);

							for (TargetDropDownV2 td3 : empList3) {
								List<TargetDropDownV2> empList4 = buildDropDownV2(Integer.parseInt(td3.getCode()),
										branchId, orgId);
								map4.put(td3.getCode(), empList4);

								for (TargetDropDownV2 td4 : empList4) {
									List<TargetDropDownV2> empList5 = buildDropDownV2(Integer.parseInt(td4.getCode()),
											branchId, orgId);
									map5.put(td4.getCode(), empList5);
								}
							}
						}
					}
				}
				if (maxLevel >= (empLevel + 2) && !validateMap(map1)) {
					empDtaMap.put(getLevelName(empLevel + 2), map1);
				}
				if (maxLevel >= (empLevel + 3) && !validateMap(map2)) {
					empDtaMap.put(getLevelName(empLevel + 3), map2);
				}
				if (maxLevel >= (empLevel + 4) && !validateMap(map3)) {
					empDtaMap.put(getLevelName(empLevel + 4), map3);
				}
				if (maxLevel >= (empLevel + 5) && !validateMap(map4)) {
					empDtaMap.put(getLevelName(empLevel + 5), map4);
				}

			}
			log.debug("empDtaMap in getReportingHierarchy:::"+empDtaMap);		
		} catch (Exception e) {
			e.printStackTrace();
		}

		return empDtaMap;
	}
	
	@Override
	public DmsEmployee saveEmployee(DmsEmployee dmsEmployee){
		return dmsEmployeeRepo.save(dmsEmployee);
	}
	
	@Override
	public DmsEmployee updateEmployee(DmsEmployee dmsEmployee){
		Optional<DmsEmployee> optionalPersistDmsEmployee = dmsEmployeeRepo.findEmpById(dmsEmployee.getEmp_id());
	if(optionalPersistDmsEmployee.isPresent()) {
		DmsEmployee dmsPersistentEmployee = optionalPersistDmsEmployee.get();
		dmsPersistentEmployee.setHrmsEmpId(dmsEmployee.getEmp_id()+"");
		dmsPersistentEmployee.setHrmsRole(dmsEmployee.getHrmsRole());
		dmsPersistentEmployee.setAddress(dmsEmployee.getAddress());
		dmsPersistentEmployee.setDeptId(dmsEmployee.getDeptId());
		dmsPersistentEmployee.setDesignationId(dmsEmployee.getDesignationId());
		dmsPersistentEmployee.setGradeId(dmsEmployee.getGradeId());
		dmsPersistentEmployee.setApprover(dmsEmployee.getApprover());
		dmsPersistentEmployee.setReportingTo(dmsEmployee.getReportingTo());
		dmsPersistentEmployee.setEmpName(dmsEmployee.getEmpName());
		dmsPersistentEmployee.setEmail(dmsEmployee.getEmail());
		dmsPersistentEmployee.setMobile(dmsEmployee.getMobile());
		dmsPersistentEmployee.setBasicSal(dmsEmployee.getBasicSal());
		dmsPersistentEmployee.setPrevExperience(dmsEmployee.getPrevExperience());
		return dmsEmployeeRepo.save(dmsPersistentEmployee);
	}
	return null;
	}
	
	
	
	
	public Map<String, Object> getReportingHierarchyV3(DmsEmployee emp, int branchId, Integer orgId) {
		log.debug("Inside getReportingHierarchyV3,branchId: "+branchId+",orgId:"+orgId);
		Map<String, Object> empDtaMap = new LinkedHashMap<>();
		try {
			Integer maxLevel = getMaxLevel();
			log.debug("maxLevel:::" + maxLevel);
			Integer empLevel = 0;
			if (null != emp.getDesignationId()) {
				Integer empDesId = Integer.parseInt(emp.getDesignationId());
				log.debug("empDesigntaion:::" + empDesId);
				Optional<DmsDesignation> desOpt = dmsDesignationRepo.findById(empDesId);
				if (desOpt.isPresent()) {
					empLevel = desOpt.get().getLevel();
				} else {
					throw new DynamicFormsServiceException(
							"Given emp does not have valid designation in dms_designation", HttpStatus.BAD_REQUEST);
				}

				log.debug("Given emp level is " + empLevel);

				List<TargetDropDownV2> empList = buildDropDownV2(emp.getEmp_id(), branchId, orgId);
				log.debug("empList::"+empList);	
				if (!empList.isEmpty() && maxLevel >= (empLevel + 1)) {
					Map<String, Object> map = new LinkedHashMap<>();
					map.put(String.valueOf(emp.getEmp_id()), empList);
					empDtaMap.put(getLevelName(empLevel + 1), map);
				}
			

			}
			log.debug("empDtaMap in getReportingHierarchyV3:::"+empDtaMap);		
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception ",e);;
		}

		return empDtaMap;
	}
	
	public Map<String, Object> getReportingHierarchy2(DmsEmployee emp, int nodeId, Integer orgId,int branchId) {
		long startTime = System.currentTimeMillis();
		log.debug("Inside getReportingHierarchy,branchId: " + branchId + ",orgId:" + orgId);
		Map<String, Object> empDtaMap = new LinkedHashMap<>();

		try {
			// get all the required static data first
			List<DmsDesignation> desOpt = dmsDesignationRepo.findByOrg_id(orgId);
			List<DmsGrade> grades = dmsGradeDao.findAll();
			List<DmsEmployee> branchEmpList = dmsEmployeeRepo.getEmployeesByOrg(orgId);
			// process current employee which recursively calls all child employees
			processEmployee(empToTD(emp), emp, nodeId, orgId, empDtaMap, desOpt, grades, branchEmpList,empToTD(emp),branchId);
			log.debug("empDtaMap in getReportingHierarchy:::" + empDtaMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("Total Time = %s ms", (System.currentTimeMillis() - startTime));

		return empDtaMap;
	}
	
	public Map<String, Object> getReportingTeamHierarchy2(DmsEmployee emp, int nodeId, Integer orgId,int branchId) {
		long startTime = System.currentTimeMillis();
		log.debug("Inside getReportingHierarchy,branchId: " + branchId + ",orgId:" + orgId);
		Map<String, Object> empDtaMap = new LinkedHashMap<>();

		try {
			// get all the required static data first
			List<DmsDesignation> desOpt = dmsDesignationRepo.findByTeamOrg_id(orgId);
			List<DmsGrade> grades = dmsGradeDao.findAll();
			List<DmsEmployee> branchEmpList = dmsEmployeeRepo.getEmployeesTeamByOrg(orgId);
			// process current employee which recursively calls all child employees
			processEmployeeLevel(empTooTD(emp), emp, nodeId, orgId, empDtaMap, desOpt, grades, branchEmpList,empTooTD(emp),branchId);
			log.debug("empDtaMap in getReportingHierarchy:::" + empDtaMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("Total Time = %s ms", (System.currentTimeMillis() - startTime));

		return empDtaMap;
	}
	
	private TargetDropDownV2 empToTD(DmsEmployee emp) {
		TargetDropDownV2 td = new TargetDropDownV2();
		td.setCode(String.valueOf(emp.getEmp_id()));
		td.setName(emp.getEmpName());
		return td;
	}
	//team attendance
	private TeamAttendanceResponse empTooTD(DmsEmployee emp) {
		TeamAttendanceResponse td = new TeamAttendanceResponse();
		td.setCode(String.valueOf(emp.getEmp_id()));
		td.setName(emp.getEmpName());
		return td;
	}
	
	/**
	 * Process each employee and call recursively for all the children of the
	 * current employee. For the current employee, put the employee
	 * {@link TargetDropDownV2} in the map for given current level
	 * 
	 * @param td
	 * @param emp
	 * @param branchId
	 * @param orgId
	 * @param empLevelMap
	 * @param desOpt
	 * @param grades
	 * @param branchEmpList 
	 */
	private void processEmployee(TargetDropDownV2 td, DmsEmployee emp, int nodeId, Integer orgId, Map<String, Object> empLevelMap, List<DmsDesignation> desOpt, List<DmsGrade> grades, List<DmsEmployee> branchEmpList,TargetDropDownV2 topTd,int branchId) {
		//find the current employee's designation
		Optional<DmsDesignation> designation = desOpt.stream().filter(a -> a.getDmsDesignationId() == Integer.parseInt(emp.getDesignationId())).findFirst();
		int delpId = dmsDepartmentRepo.findDepartmentForSales(orgId,branchId).get(0);
		Set<Integer> preDesig=dmsDeptDesignationMappingRepo.findDesignationIdByDeptId(delpId);
		if(preDesig.contains(designation.get().getDmsDesignationId()) && !topTd.equals(td)) {
			Integer currentLevel = null;
			String levelName = null;
			String designationName = null;
			if(designation.isPresent()) {
				 DmsDesignation dmsDesignation = designation.get();
				 currentLevel = dmsDesignation.getLevel();
				 designationName = dmsDesignation.getDesignationName();
				try {
					//find the current employee's levelName 
					levelName = getLevelName2(grades, currentLevel);
				} catch (DynamicFormsServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			Integer pme = Integer.valueOf(dmsEmployeeRepo.findById(Integer.valueOf(td.getCode())).get().getBranch());
			
			Boolean branchCheck = false;
			List<Integer> pmes = dmsEmployeeRepo.bracnhListByUserId(Integer.valueOf(td.getCode()));
			if (pmes != null && pmes.size() > 0) {
				for (int i = 0; i < pmes.size(); i++) {
					if (pmes.get(i) != null) {
						if (pmes.get(i) == branchId) {
							branchCheck = true;
							break;
						}
					}
				}
			} else {
				Integer pme = Integer
						.valueOf(dmsEmployeeRepo.findById(Integer.valueOf(td.getCode())).get().getBranch());
				if (pme == branchId) {
					branchCheck = true;
				}
			}
			
//			if(pme==branchId) {
//				branchCheck=true;
//			}
			//if map already contains items for the current employee's level, then add otherwise create new list and add to map
			if (designationName != null && empLevelMap.containsKey(designationName) && branchCheck) {
				List<TargetDropDownV2> currentList =(List<TargetDropDownV2>) empLevelMap.get(designationName);// buildDropDown(Integer.parseInt(td.getCode()),branchId, orgId);//
				currentList.add(td);
			} else if(branchCheck) {
				List<TargetDropDownV2> currentList = new ArrayList<>();
				currentList.add(td);
				empLevelMap.put(designationName, currentList);
			}
		}
		//now process all the children
		List<TargetDropDownV2> empList = buildDropDown2(emp.getEmp_id(), nodeId, orgId);
		for (TargetDropDownV2 currentTd : empList) {
			DmsEmployee childEmp = getEmployee(branchEmpList, Integer.parseInt(currentTd.getCode()));
			if(childEmp!=null) {
				if(childEmp.getEmp_id() != emp.getEmp_id()) {
					processEmployee(currentTd, childEmp, nodeId, orgId, empLevelMap, desOpt, grades, branchEmpList,topTd,branchId);
				}	
			}
		}
		
	}
	
	//team attendance
	@Autowired
	DmsEmployeeAttachmentsDao dmsEmployeeAttachmentsDao;
	private void processEmployeeLevel(TeamAttendanceResponse td, DmsEmployee emp, int nodeId, Integer orgId, Map<String, Object> empLevelMap, List<DmsDesignation> desOpt, List<DmsGrade> grades, List<DmsEmployee> branchEmpList,TeamAttendanceResponse topTd,int branchId) {
		//find the current employee's designation
		Optional<DmsDesignation> designation = desOpt.stream().filter(a -> a.getDmsDesignationId() == Integer.parseInt(emp.getDesignationId())).findFirst();
		int delpId = dmsDepartmentRepo.findDepartmentForSales(orgId,branchId).get(0);
		Set<Integer> preDesig=dmsDeptDesignationMappingRepo.findDesignationIdByDeptId(delpId);
		if(preDesig.contains(designation.get().getDmsDesignationId()) && !topTd.equals(td)) {
			Integer currentLevel = null;
			String levelName = null;
			String designationName = null;
			if(designation.isPresent()) {
				 DmsDesignation dmsDesignation = designation.get();
				 currentLevel = dmsDesignation.getLevel();
				 designationName = dmsDesignation.getDesignationName();
				try {
					//find the current employee's levelName 
					levelName = getLevelName2(grades, currentLevel);
				} catch (DynamicFormsServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Integer pme = Integer.valueOf(dmsEmployeeRepo.findById(Integer.valueOf(td.getCode())).get().getBranch());
			Boolean branchCheck=false;
			if(pme==branchId) {
				branchCheck=true;
			}
			//if map already contains items for the current employee's level, then add otherwise create new list and add to map
			if (designationName != null && empLevelMap.containsKey(designationName) && branchCheck) {
				List<TeamAttendanceResponse> currentList =(List<TeamAttendanceResponse>) empLevelMap.get(designationName);// buildDropDown(Integer.parseInt(td.getCode()),branchId, orgId);//
				currentList.add(td);
			} else if(branchCheck) {
				List<TeamAttendanceResponse> currentList = new ArrayList<>();
				currentList.add(td);
				empLevelMap.put(designationName, currentList);
			}
		}
		//now process all the children
		List<TeamAttendanceResponse> empList = buildDropDown22(emp.getEmp_id(), nodeId, orgId);
		for (TeamAttendanceResponse currentTd : empList) {
			DmsEmployee childEmp = getEmployee(branchEmpList, Integer.parseInt(currentTd.getCode()));
			if(childEmp!=null) {
				if(childEmp.getEmp_id() != emp.getEmp_id()) {
					processEmployeeLevel(currentTd, childEmp, nodeId, orgId, empLevelMap, desOpt, grades, branchEmpList,topTd,branchId);
				}	
			}
		}
		
	}
	
	
	public String getLevelName2(List<DmsGrade> list, int level) throws DynamicFormsServiceException {

		String levelName = null;
		Optional<DmsGrade> opt = list.stream().filter(x -> x.getLevel() == level).findFirst();
		if (opt.isPresent()) {
			levelName = opt.get().getLevelName();
			levelName.replaceAll(",", "/");
		} else {
			throw new DynamicFormsServiceException(
					"There is no valid levelname for given level " + level + " in dms_grade",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return levelName;
	}
	
	private List<TargetDropDownV2> buildDropDown2(Integer id, Integer branch, Integer orgId) {
		// String query = "SELECT emp_id,emp_name FROM dms_employee where
		// reporting_to=<ID> and branch=<BRANCH> and org=<ORGID>";
		String query = "\r\n" + "select emp_id,emp_name,reporting_to from dms_employee where org = <ORGID> "
				+ "and reporting_to=<ID> and emp_id in (select emp_id from emp_location_mapping where org_id=<ORGID>"
				+ " and location_node_data_id ='<BRANCH>')"
				+ " and status ='Active' ";
		query = query.replaceAll("<ID>", String.valueOf(id));
		query = query.replaceAll("<BRANCH>", String.valueOf(branch));
		query = query.replaceAll("<ORGID>", String.valueOf(orgId));
		log.debug("buildDropDown2 query " + query);
		List<TargetDropDownV2> list = new ArrayList<>();
		List<Object[]> data = entityManager.createNativeQuery(query).getResultList();

		for (Object[] arr : data) {
			TargetDropDownV2 trRoot = new TargetDropDownV2();
			trRoot.setCode(String.valueOf(arr[0]));
			trRoot.setName(String.valueOf(arr[1]));
			trRoot.setParentId(String.valueOf(arr[2]));
			list.add(trRoot);
		}
		list = list.stream().distinct().collect(Collectors.toList());
		return list;
	}
	//team attendance
	private List<TeamAttendanceResponse> buildDropDown22(Integer id, Integer branch, Integer orgId) {
		// String query = "SELECT emp_id,emp_name FROM dms_employee where
		// reporting_to=<ID> and branch=<BRANCH> and org=<ORGID>";
//		String query = "\r\n" + "select emp_id,emp_name,reporting_to from dms_employee where org = <ORGID> "
//				+ "and reporting_to=<ID> and emp_id in (select emp_id from emp_location_mapping where org_id=<ORGID>"
//				+ " and location_node_data_id ='<BRANCH>')";
		String query = "\r\n" + "select emp.emp_id,emp.emp_name,emp.reporting_to,emp.org , emp.branch from dms_employee emp\r\n"
				+ "		where emp.org = <ORGID> and emp.reporting_to=<ID> \r\n"
				+ "		and emp.emp_id in (select emp_id from emp_location_mapping where org_id=<ORGID> and location_node_data_id =<BRANCH>)";	
		query = query.replaceAll("<ID>", String.valueOf(id));
		query = query.replaceAll("<BRANCH>", String.valueOf(branch));
		query = query.replaceAll("<ORGID>", String.valueOf(orgId));
		log.debug("buildDropDown2 query " + query);
		List<TeamAttendanceResponse> list = new ArrayList<>();
		List<Object[]> data = entityManager.createNativeQuery(query).getResultList();

		for (Object[] arr : data) {
			TeamAttendanceResponse trRoot = new TeamAttendanceResponse();
			trRoot.setCode(String.valueOf(arr[0]));
			trRoot.setName(String.valueOf(arr[1]));
			trRoot.setParentId(String.valueOf(arr[2]));
			trRoot.setOrgId(String.valueOf(arr[3]));
			trRoot.setBranch(String.valueOf(arr[4]));
			list.add(trRoot);
		}
		list = list.stream().distinct().collect(Collectors.toList());
		return list;
	}
	/**
	 * Get the employee object for given empID from the list of all employees of
	 * current orgId
	 * 
	 * @param branchEmpList
	 * @param empId
	 * @return
	 */
	private DmsEmployee getEmployee(List<DmsEmployee> branchEmpList, int empId) {
		DmsEmployee emp = null;
		Optional<DmsEmployee> empOpt = branchEmpList.stream().filter(x -> x.getEmp_id() == empId).findAny();
		System.out.println("empOpt ::" + empOpt.isPresent());
		if (empOpt.isPresent()) {
			emp = empOpt.get();
		}
		return emp;
	}
	
	private Map<String, Object> formatReportingHierarchy2(Map<String, Object> hMap) {
		if (null != hMap) {
			AtomicInteger a = new AtomicInteger(0);
			for (Map.Entry<String, Object> mapentry : hMap.entrySet()) {
				a.incrementAndGet();
				List<TargetDropDownV2> ddList = (List<TargetDropDownV2>) mapentry.getValue();
				ddList = ddList.stream().distinct().collect(Collectors.toList());
				ddList.forEach(x -> {
					x.setDesignation(mapentry.getKey());
					x.setOrder(a.intValue());
				});
			}
		}

		for (Map.Entry<String, Object> mapentry : hMap.entrySet()) {
			List<TargetDropDownV2> list = new ArrayList<>();
			List<TargetDropDownV2> ddList = (List<TargetDropDownV2>) mapentry.getValue();
			ddList = ddList.stream().distinct().collect(Collectors.toList());
			list.addAll(ddList);
			list = list.stream().distinct().collect(Collectors.toList());
			hMap.put(mapentry.getKey(), list);
		}
		return hMap;
	}
	//teamattendance
	private Map<String, Object> formatTeamReportingHierarchy2(Map<String, Object> hMap) {
		if (null != hMap) {
			AtomicInteger a = new AtomicInteger(0);
			for (Map.Entry<String, Object> mapentry : hMap.entrySet()) {
				a.incrementAndGet();
				List<TeamAttendanceResponse> ddList = (List<TeamAttendanceResponse>) mapentry.getValue();
				ddList = ddList.stream().distinct().collect(Collectors.toList());
				ddList.forEach(x -> {
					x.setDesignation(mapentry.getKey());
					x.setOrder(a.intValue());
				});
			}
		}
		for (Map.Entry<String, Object> mapentry : hMap.entrySet()) {
			List<TeamAttendanceResponse> list = new ArrayList<>();
			List<TeamAttendanceResponse> ddList = (List<TeamAttendanceResponse>) mapentry.getValue();
			ddList = ddList.stream().distinct().collect(Collectors.toList());
			list.addAll(ddList);
			list = list.stream().distinct().collect(Collectors.toList());
			hMap.put(mapentry.getKey(), list);
		}
		return hMap;
	}
	
	@Override
	public Map<String, Object> getlevelForDealer(Integer orgId, Integer empId) throws DynamicFormsServiceException {
		Map<String, Object> finalMap = new LinkedHashMap<>();
		Map<String, Object> map = new LinkedHashMap<>();
		try {
			List<LocationNodeDataV2> activeParentNodeList = ObjectMapperUtils.mapAll(locationNodeDataDao.getActiveLevelsForEmpWithOutOrg(empId), LocationNodeDataV2.class);
			List<LocationNodeDef> list = locationNodeDefDao.getLevelByOrgID(orgId);
			List<String> orgLevels = list.stream().map(x->x.getLocationNodeDefType()).collect(Collectors.toList());
			List<String> activeLevels = activeParentNodeList.stream().map(x->x.getType()).distinct().collect(Collectors.toList());
			List<String> disabledLevels = new ArrayList<>(orgLevels);
			disabledLevels.removeAll(activeLevels);
			log.debug("disabledLevels "+disabledLevels);
			log.debug("activeLevels "+activeLevels);
			for(LocationNodeDataV2 d : activeParentNodeList) {
				d.setDisabled("N");
			}
			
			if(null!=disabledLevels && !disabledLevels.isEmpty()) {
				for(String disabledLevel: disabledLevels) {
					List<LocationNodeData> levelDataList = locationNodeDataDao.getNodeDataByLevel(orgId, disabledLevel);
					List<LocationNodeDataV2> levelDataListv2 = ObjectMapperUtils.mapAll(levelDataList, LocationNodeDataV2.class);
					for(LocationNodeDataV2 d : levelDataListv2) {
						d.setDisabled("Y");
					}
					activeParentNodeList.addAll(levelDataListv2);
				}
			}
			List<String> levels = activeParentNodeList.stream().map(x -> x.getType()).collect(Collectors.toList());
			levels = levels.stream().sorted().distinct().collect(Collectors.toList());
			log.debug("USER LEVELS " + levels + " and size:" + levels.size());
			if (null != levels && levels.size() > 0) {
				String leafLevel = levels.get(levels.size() - 1);
				log.debug("leafLevel:::" + leafLevel);
				Integer maxLevel = getMaxLevel();
				log.debug("maxLevel:::" + maxLevel);
			
					for (String level : levels) {
						if (level.equalsIgnoreCase(leafLevel)) {
							List<LocationNodeDataV2> leafNodes = activeParentNodeList.stream().filter(x -> x.getType().equalsIgnoreCase(level)).collect(Collectors.toList());

							for (LocationNodeDataV2 ld : leafNodes) {
								Map<String, Object> empDtaMap = new LinkedHashMap<>();
								int nodeId = ld.getId();
								log.debug("Node Id " + nodeId);
								DmsBranch branch = dmsBranchDao.getBranchByOrgMpId(nodeId);
								int branchId = branch.getBranchId();
								log.debug("branchId::" + branchId);
								Integer roleId = dmsEmployeeRepo.getEmpHrmsRole(empId);
								List<DmsEmployee> branchEmpList = dmsEmployeeRepo.getEmployeesByOrgBranch(orgId,
										branchId,roleId);
								log.debug("branchEmpList size ::" + branchEmpList.size());
						
							}

							map.put(level, leafNodes);
							List<LocationNodeData> levelDataList = locationNodeDataDao.getNodeDataByLevel(orgId, level);
							List<LocationNodeDataV2> levelDataListv2 = ObjectMapperUtils.mapAll(levelDataList, LocationNodeDataV2.class);
							List<LocationNodeDataV2> mapList = (List<LocationNodeDataV2>)map.get(level);
							
							levelDataListv2.forEach(x->{
							
							
								if(!mapList.contains(x)) {
									x.setDisabled("Y");
									mapList.add(x);
								}
							});
								map.put(level, mapList);
								
						} else {

							map.put(level, activeParentNodeList.stream().filter(x -> x.getType().equalsIgnoreCase(level)).collect(Collectors.toList()));
							
							List<LocationNodeData> levelDataList = locationNodeDataDao.getNodeDataByLevel(orgId, level);
							List<LocationNodeDataV2> levelDataListv2 = ObjectMapperUtils.mapAll(levelDataList, LocationNodeDataV2.class);
							List<LocationNodeDataV2> mapList = (List<LocationNodeDataV2>)map.get(level);
							
							levelDataListv2.forEach(x->{
							
							
								if(!mapList.contains(x)) {
									x.setDisabled("Y");
									mapList.add(x);
								}
							});
							map.put(level, mapList);
						//	map.put(getLevelName(level,orgId), mapList);
							
						}
					}
				
			} else {
				throw new DynamicFormsServiceException("No Org Hierarchy Data is present for the given empId",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			AtomicInteger atomicInt = new AtomicInteger(0);
			map.forEach((k,v)->{
				atomicInt.getAndIncrement();
				List<LocationNodeDataV2> l = (List<LocationNodeDataV2>)v;
				l.forEach(z->{
					z.setOrder(atomicInt.intValue());
					
				});
			});
			
			map.forEach((k,v)->{
				List<LocationNodeDataV2> l = (List<LocationNodeDataV2>)v;
				List<LocationNodeDataV2> l3=new ArrayList<>();
				for(LocationNodeDataV2 l1:l) {
					LocationNodeDataV2 obj=new LocationNodeDataV2();
					obj=l1;
					String s=l1.getCananicalName();
					String[] split = s.split("/");
					String str="";
					for(int i=split.length-1;i>=0;i--) {
						if(i==0) {
							str=str+split[i];
						}else {
							str=str+split[i]+"/";
						}
					}
					obj.setCananicalName(str);
					l3.add(obj);
				}
				Map<String,List<LocationNodeDataV2>> innerMap = new LinkedHashMap<>();
				innerMap.put("sublevels",l3);
				map.put(k, innerMap);
				finalMap.put(getLevelName(k, orgId), innerMap);
			});
			
					
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return finalMap;

	}
	
	@Override
	public BulkUploadResponse processBulkExcelForEmployee(Integer empId, Integer orgId,Integer branchId, MultipartFile bulkExcel) throws Exception {
		Resource file = null;
		if (bulkExcel.isEmpty()) {
			BulkUploadResponse res = new BulkUploadResponse();
			List<String> FailedRecords =new ArrayList<>();
			String resonForFailure = "File not found";
			FailedRecords.add(resonForFailure);
			res.setFailedCount(0);
			res.setFailedRecords(FailedRecords);
			res.setSuccessCount(0);
			res.setTotalCount(0);
			return res;
		}
		Path tmpDir = Files.createTempDirectory("temp");
		Path tempFilePath = tmpDir.resolve(bulkExcel.getOriginalFilename());
		Files.write(tempFilePath, bulkExcel.getBytes());
		String fileName = bulkExcel.getOriginalFilename();
		fileName = fileName.substring(0, fileName.indexOf("."));
		return processBulkExcelEmployee(tempFilePath.toString(),empId,orgId,branchId);
	}
	
	public BulkUploadResponse processBulkExcelEmployee(String inputFilePath,Integer empId, Integer orgId,Integer branchId)
			throws Exception {

		Workbook workbook = null;
		Sheet sheet = null;
		List<DmsEmployee> ListDmsEmployee = new ArrayList<>();
		workbook = getWorkBook(new File(inputFilePath));
		sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();
		List<String> FailedRecords =new ArrayList<>();
		int TotalCount =-1;
		int SuccessCount=0;
		int FailedCount=0;
		int emptyCheck=0;
		BulkUploadResponse res = new BulkUploadResponse();
		while (rowIterator.hasNext()) {
			TotalCount++;
			Row row = rowIterator.next();
			try {
			if (row.getRowNum() != 0) {
				emptyCheck++;
				DmsEmployee dmsEmployee = new DmsEmployee();	
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 0))) {
					Integer deptId=dmsDepartmentRepo.findDepartmentForDepartmantName(branchId,getCellValueBasedOnCellType(row, 0)).get(0);
					dmsEmployee.setDeptId(String.valueOf(deptId));
					}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 1))) {
					Integer desigenId=dmsDesignationRepo.findIdByDesignationName(orgId,getCellValueBasedOnCellType(row, 1)).get(0);
					dmsEmployee.setDesignationId(String.valueOf(desigenId));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 2))) {
					String reportingId=dmsEmployeeRepo.findEmpIdByName(getCellValueBasedOnCellType(row, 2));
					dmsEmployee.setReportingTo(reportingId);	
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 3))) {
					String approverId=dmsEmployeeRepo.findEmpIdByName(getCellValueBasedOnCellType(row, 3));
					dmsEmployee.setApprover(approverId);
				} 
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 4))) {
					dmsEmployee.setEmpName(getCellValueBasedOnCellType(row, 4));
					dmsEmployee.setUserName(getCellValueBasedOnCellType(row, 4).strip().toLowerCase());
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 5))) {
					dmsEmployee.setMobile(getCellValueBasedOnCellType(row, 5));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 6))) {
					dmsEmployee.setBasicSal(getCellValueBasedOnCellType(row, 6));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 7))) {
					dmsEmployee.setPrevExperience(getCellValueBasedOnCellType(row, 7));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 8))) {
					dmsEmployee.setEmail(getCellValueBasedOnCellType(row, 8));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 9))) {
					int roleId=dmsRoleRepo.getRoleIdByRoleNameBranchId(getCellValueBasedOnCellType(row, 9),branchId).get(0);
					dmsEmployee.setHrmsRole(String.valueOf(roleId));
				}
				EmployeeAddress employeeAddress =new EmployeeAddress();
				com.automate.df.model.AddressNew presentAddress = new com.automate.df.model.AddressNew(); 
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 10))) {
					presentAddress.setPincode(getCellValueBasedOnCellType(row, 10));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 11))) {
					if(getCellValueBasedOnCellType(row, 11).equals("URBAN")||getCellValueBasedOnCellType(row, 11).equals("urban")) {
					presentAddress.setIsUrban("urban");
					}
					if(getCellValueBasedOnCellType(row, 11).equals("RURAL")||getCellValueBasedOnCellType(row, 11).equals("rural")) {
					presentAddress.setIsRural("rural");
					}
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 12))) {
					presentAddress.setHouseNo(getCellValueBasedOnCellType(row, 12));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 13))) {
					presentAddress.setStreet(getCellValueBasedOnCellType(row, 13));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 14))) {
					presentAddress.setVillage(getCellValueBasedOnCellType(row, 14));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 15))) {
					presentAddress.setDistrict(getCellValueBasedOnCellType(row, 15));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 16))) {
					presentAddress.setState(getCellValueBasedOnCellType(row, 16));
				}
				com.automate.df.model.AddressNew parmanantAddress = new com.automate.df.model.AddressNew(); 
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 17))) {
					parmanantAddress.setPincode(getCellValueBasedOnCellType(row, 17));;
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 18))) {
					if(getCellValueBasedOnCellType(row, 18).equals("URBAN")||getCellValueBasedOnCellType(row, 18).equals("urban")) {
					parmanantAddress.setIsUrban("urban");
					}
					if(getCellValueBasedOnCellType(row, 18).equals("RURAL")||getCellValueBasedOnCellType(row, 18).equals("rural")) {
					parmanantAddress.setIsRural("rural");
					}
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 19))) {
					parmanantAddress.setHouseNo(getCellValueBasedOnCellType(row, 19));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 20))) {
					parmanantAddress.setStreet(getCellValueBasedOnCellType(row, 20));;
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 21))) {
					parmanantAddress.setVillage(getCellValueBasedOnCellType(row, 21));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 22))) {
					parmanantAddress.setDistrict(getCellValueBasedOnCellType(row, 22));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 23))) {
					parmanantAddress.setState(getCellValueBasedOnCellType(row, 23));
				}
				if (StringUtils.isNotBlank(getCellValueBasedOnCellType(row, 24))) {
					String encrypt = PasswordEncryptor.encrypt(getCellValueBasedOnCellType(row, 24));
					dmsEmployee.setPassword1(encrypt);
				}else {
					throw new Exception("Provide password");
				}
				employeeAddress.setPresentAddress(presentAddress);
				employeeAddress.setPermanentAddress(parmanantAddress);
				dmsEmployee.setAddress(employeeAddress);
				ListDmsEmployee.add(dmsEmployee);
				}
		}catch(Exception e) {
			String resonForFailure = e.getMessage();
			System.out.println(resonForFailure);
			FailedRecords.add(resonForFailure);
			continue;
		}
		}
		if(emptyCheck==0) {
			String resonForFailure = "DATA NOT FOUND";
			System.out.println(resonForFailure);
			FailedRecords.add(resonForFailure);
		}
		
		int j=0;
		List<DmsEmployee> ListDmsEmployee1 = new ArrayList<>();
		for (DmsEmployee employee : ListDmsEmployee) {
			try {
				j++;
			DmsEmployee o1=dmsEmployeeRepo.save(employee);
			o1.setHrmsEmpId(String.valueOf(o1.getEmp_id()));
			DmsEmployee o2=dmsEmployeeRepo.save(o1);
			ListDmsEmployee1.add(o2);
			SuccessCount++;

		}catch(DataAccessException e) {
			String resonForFailure = "DUPLICATE ENTRY IN "+j+" ROW FOUND";
			System.out.println(resonForFailure);
			FailedRecords.add(resonForFailure);
			continue;
		}catch(Exception e) {
			String resonForFailure = "ERROR IN SAVEING DATA FOR "+j+" ROW "+e.getMessage();
			System.out.println(resonForFailure);
			FailedRecords.add(resonForFailure);
			continue;
		}	
	}
	FailedCount=TotalCount-SuccessCount;
	res.setFailedCount(FailedCount);
	res.setFailedRecords(FailedRecords);
	res.setSuccessCount(SuccessCount);
	res.setTotalCount(TotalCount);
	return res;
}
	
	private Workbook getWorkBook(File fileName)
	{
		Workbook workbook = null;
		try {
			String myFileName=fileName.getName();
			String extension = myFileName.substring(myFileName.lastIndexOf("."));
			if(extension.equalsIgnoreCase(".xls")){
				workbook = new HSSFWorkbook(new FileInputStream(fileName));
			}
			else if(extension.equalsIgnoreCase(".xlsx")){
				workbook = new XSSFWorkbook(new FileInputStream(fileName));
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return workbook;
	}
	
	private String getCellValueBasedOnCellType(Row rowData,int columnPosition)
	{
		String cellValue=null;
		Cell cell = rowData.getCell(columnPosition);
		if(cell!=null){
			if(cell.getCellType()==Cell.CELL_TYPE_STRING)
			{
				String inputCellValue=cell.getStringCellValue();
				if(inputCellValue.endsWith(".0")){
					inputCellValue=inputCellValue.substring(0, inputCellValue.length()-2);
				}
				cellValue=inputCellValue;
			}
			else if (cell.getCellType()==Cell.CELL_TYPE_NUMERIC)
			{
				if(DateUtil.isCellDateFormatted(cell)) {
					 
					 DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
    
					    Date today = cell.getDateCellValue() ;      
					   
					    cellValue = df.format(today);
				        		
				    }else {
				Integer doubleVal = (int) cell.getNumericCellValue();
				cellValue= Integer.toString(doubleVal);
				    }
			}
			
		}
		return cellValue;
	}
	
	@Override
	public Map<String, Object> getActiveDropdownsV3(List<Integer> levelList, Integer orgId, Integer empId)
			throws DynamicFormsServiceException {
		List<LocationNodeData> list = new ArrayList<>();
		List<Integer> reqNodeIds = new ArrayList<>();
		Map<String, Object> resMap = new LinkedHashMap<>();
		try {
			for (Integer nodeId : levelList) {
				log.debug("nodeId::::"+nodeId);
				String levelName = locationNodeDataDao.getLevelname(nodeId);
				log.debug("Given node level " + levelName + " and nodeId " + nodeId);
				List<String> levels = getOrgLevels(orgId).stream().map(x -> x.getLocationNodeDefType()).collect(Collectors.toList());
				log.debug("levels before " + levels);
				log.debug("levelName::"+levelName);
				levels = levels.subList(levels.indexOf(levelName), levels.size());
				levels = levels.stream().sorted().collect(Collectors.toList());
				log.debug("levels after " + levels);
				Map<String, List<Integer>> levelIdmap = new LinkedHashMap<>();
				for (int i = 0; i < levels.size(); i++) {
					String level = levels.get(i);
					if (i == 0) {
						List<Integer> tmp = new ArrayList<>();
						tmp.add(nodeId);
						levelIdmap.put(level, tmp);
					} else {
						if (!levelIdmap.isEmpty()) {
							String previousLevel = getPreviousLevel(level);
							log.debug("level " + level + ",previousLevel:" + previousLevel);
							if (levelIdmap.containsKey(previousLevel)) {
								log.debug("LevelIdMap contains previous level " + level);
								List<LocationNodeData> nodeData = locationNodeDataDao.getNodeDataByParentId(orgId,
										level, levelIdmap.get(previousLevel));
								List<Integer> idLists = nodeData.stream().map(x -> x.getId())
										.collect(Collectors.toList());
								levelIdmap.put(level, idLists);
								list.addAll(nodeData);
							}
						}

					}
				}
				log.debug("levelIdmap ::" + levelIdmap);

				levelIdmap.forEach((k, v) -> {
					reqNodeIds.addAll(v);
				});
			}

			log.debug("reqNodeIds:::" + reqNodeIds);
			List<LocationNodeDataV2> activeParentNodeList = ObjectMapperUtils
					.mapAll(locationNodeDataDao.getActiveLevelsForEmpWithOutOrg(empId), LocationNodeDataV2.class);
			List<String> levels = activeParentNodeList.stream().map(x -> x.getType()).collect(Collectors.toList());
			levels = levels.stream().sorted().collect(Collectors.toList());
			log.debug("USER LEVELS " + levels + " and size:" + levels.size());
			String leafLevel = null;
			if (null != levels && levels.size() > 0) {
				leafLevel = levels.get(levels.size() - 1);
				log.debug("leafLevel:::" + leafLevel);
			}
			List<LocationNodeData> leafNodeList = locationNodeDataDao.getNodeDataByLevel(orgId, leafLevel);

			List<Integer> leafNodeIdList = leafNodeList.stream().map(x -> x.getId()).collect(Collectors.toList());
			List<Integer> finalleafNodeIdList1 = new ArrayList<>();
			for (Integer i : reqNodeIds) {
				if (leafNodeIdList.contains(i)) {
					finalleafNodeIdList1.add(i);
				}
			}
			List<Integer> finalleafNodeIdList2 = new ArrayList<>();
			for (Integer i : finalleafNodeIdList1) {
				if (levelList.contains(i)) {
					finalleafNodeIdList2.add(i);
				}
			}
			List<Integer> finalleafNodeIdList = new ArrayList<>(new HashSet<Integer>(finalleafNodeIdList2));
			log.debug("finalleafNodeIdList::" + finalleafNodeIdList);

			for (Integer nodeId : finalleafNodeIdList) {
				log.debug("Getting data for nodeId::"+nodeId);
				DmsBranch branch = dmsBranchDao.getBranchByOrgMpId(nodeId);
				int branchId = branch.getBranchId();
				log.debug("branchId::" + branchId);
				List<DmsEmployee> branchEmpList = dmsEmployeeRepo.getEmployeesByOrg(orgId);
				Optional<DmsEmployee> empOpt = branchEmpList.stream().filter(x -> x.getEmp_id() == empId).findAny();
				if (empOpt.isPresent()) {
					DmsEmployee emp = empOpt.get();
					Map<String, Object> reportingHierarchyMap = getReportingHierarchy3(emp, nodeId, orgId,branchId);
					reportingHierarchyMap = formatReportingHierarchy3(reportingHierarchyMap);
					resMap.put(String.valueOf(branchId), reportingHierarchyMap);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return resMap;
	}
	
	public Map<String, Object> getReportingHierarchy3(DmsEmployee emp, int nodeId, Integer orgId,int branchId) {
		long startTime = System.currentTimeMillis();
		log.debug("Inside getReportingHierarchy,branchId: " + branchId + ",orgId:" + orgId);
		Map<String, Object> empDtaMap = new LinkedHashMap<>();
		try {
			// get all the required static data first
			List<DmsDesignation> desOpt = dmsDesignationRepo.findByOrg_id(orgId);
			List<DmsGrade> grades = dmsGradeDao.findAll();
			List<DmsEmployee> branchEmpList = dmsEmployeeRepo.getEmployeesByOrg(orgId);
			// process current employee which recursively calls all child employees
			processEmployee3(empToTD3(emp), emp, nodeId, orgId, empDtaMap, desOpt, grades, branchEmpList,empToTD3(emp),branchId);
			log.debug("empDtaMap in getReportingHierarchy:::" + empDtaMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("Total Time = %s ms", (System.currentTimeMillis() - startTime));

		return empDtaMap;
	}
	
	private void processEmployee3(TargetDropDownV3 td, DmsEmployee emp, int nodeId, Integer orgId, Map<String, Object> empLevelMap, List<DmsDesignation> desOpt, List<DmsGrade> grades, List<DmsEmployee> branchEmpList,TargetDropDownV3 topTd,int branchId) {
		Optional<DmsDesignation> designation = desOpt.stream().filter(a -> a.getDmsDesignationId() == Integer.parseInt(emp.getDesignationId())).findFirst();
		if(!topTd.equals(td)) {
			Integer currentLevel = null;
			String levelName = null;
			String designationName = null;
			if(designation.isPresent()) {
				 DmsDesignation dmsDesignation = designation.get();
				 currentLevel = dmsDesignation.getLevel();
				 designationName = dmsDesignation.getDesignationName();
				try {
					levelName = getLevelName2(grades, currentLevel);
				} catch (DynamicFormsServiceException e) {
					e.printStackTrace();
				}
			}
			Integer pme = Integer.valueOf(dmsEmployeeRepo.findById(Integer.valueOf(td.getUserId())).get().getBranch());
			Boolean branchCheck=false;
			if(pme==branchId) {
				branchCheck=true;
			}
			//if map already contains items for the current employee's level, then add otherwise create new list and add to map
			if (designationName != null && empLevelMap.containsKey(designationName) && branchCheck) {
				List<TargetDropDownV3> currentList =(List<TargetDropDownV3>) empLevelMap.get(designationName);
				currentList.add(td);
			} else if(branchCheck) {
				List<TargetDropDownV3> currentList = new ArrayList<>();
				currentList.add(td);
				empLevelMap.put(designationName, currentList);
			}
		}
		//now process all the children
		List<TargetDropDownV3> empList = buildDropDown3(emp.getEmp_id(), nodeId, orgId);
		for (TargetDropDownV3 currentTd : empList) {
			DmsEmployee childEmp = getEmployee(branchEmpList, Integer.parseInt(currentTd.getUserId()));
			if(childEmp!=null) {
				if(childEmp.getEmp_id() != emp.getEmp_id()) {
					processEmployee3(currentTd, childEmp, nodeId, orgId, empLevelMap, desOpt, grades, branchEmpList,topTd,branchId);
				}	
			}
		}
		
	}
	
	private TargetDropDownV3 empToTD3(DmsEmployee emp) {
		TargetDropDownV3 td = new TargetDropDownV3();
		td.setDesignation(emp.getDesignationId());
		td.setName(emp.getEmpName());
		td.setOrgId(Integer.valueOf(emp.getOrg()));
		td.setParentId(emp.getReportingTo());
		td.setUserId(String.valueOf(emp.getEmp_id()));
		return td;
	}
	
	private List<TargetDropDownV3> buildDropDown3(Integer id, Integer branch, Integer orgId) {
		String query = "\r\n" + "select emp_id,emp_name,reporting_to,org from dms_employee where org = <ORGID> "
				+ "and reporting_to=<ID> and emp_id in (select emp_id from emp_location_mapping where org_id=<ORGID>"
				+ " and location_node_data_id ='<BRANCH>')";
		query = query.replaceAll("<ID>", String.valueOf(id));
		query = query.replaceAll("<BRANCH>", String.valueOf(branch));
		query = query.replaceAll("<ORGID>", String.valueOf(orgId));
		log.debug("buildDropDown2 query " + query);
		List<TargetDropDownV3> list = new ArrayList<>();
		List<Object[]> data = entityManager.createNativeQuery(query).getResultList();

		for (Object[] arr : data) {
			TargetDropDownV3 trRoot = new TargetDropDownV3();
			trRoot.setUserId(String.valueOf(arr[0]));
			trRoot.setName(String.valueOf(arr[1]));
			trRoot.setParentId(String.valueOf(arr[2]));
			trRoot.setOrgId(Integer. valueOf(String.valueOf(arr[3])));
			list.add(trRoot);
		}
		list = list.stream().distinct().collect(Collectors.toList());
		return list;
	}
	
	private Map<String, Object> formatReportingHierarchy3(Map<String, Object> hMap) {
		if (null != hMap) {
			AtomicInteger a = new AtomicInteger(0);
			for (Map.Entry<String, Object> mapentry : hMap.entrySet()) {
				a.incrementAndGet();
				List<TargetDropDownV3> ddList = (List<TargetDropDownV3>) mapentry.getValue();
				ddList = ddList.stream().distinct().collect(Collectors.toList());
				ddList.forEach(x -> {
					x.setDesignation(mapentry.getKey());
					x.setOrder(a.intValue());
				});
			}
		}

		for (Map.Entry<String, Object> mapentry : hMap.entrySet()) {
			List<TargetDropDownV3> list = new ArrayList<>();
			List<TargetDropDownV3> ddList = (List<TargetDropDownV3>) mapentry.getValue();
			ddList = ddList.stream().distinct().collect(Collectors.toList());
			list.addAll(ddList);
			list = list.stream().distinct().collect(Collectors.toList());
			hMap.put(mapentry.getKey(), list);
		}
		return hMap;
	}
	
	@Override
	public Map<String, Object> getTeamActiveDropdownsV3(List<Integer> levelList, Integer orgId, Integer empId,String startDate,String endDate)
			throws DynamicFormsServiceException {
		List<LocationNodeData> list = new ArrayList<>();
		List<Integer> reqNodeIds = new ArrayList<>();
		Map<String, Object> resMap = new LinkedHashMap<>();
		try {
			for (Integer nodeId : levelList) {
				log.debug("nodeId::::"+nodeId);
				String levelName = locationNodeDataDao.getLevelTeamname(nodeId);
				log.debug("Given node level " + levelName + " and nodeId " + nodeId);
				List<String> levels = getOrgLevels(orgId).stream().map(x -> x.getLocationNodeDefType()).collect(Collectors.toList());
				log.debug("levels before " + levels);
				log.debug("levelName::"+levelName);
				levels = levels.subList(levels.indexOf(levelName), levels.size());
				levels = levels.stream().sorted().collect(Collectors.toList());
				log.debug("levels after " + levels);
				Map<String, List<Integer>> levelIdmap = new LinkedHashMap<>();
				for (int i = 0; i < levels.size(); i++) {
					String level = levels.get(i);
					if (i == 0) {
						List<Integer> tmp = new ArrayList<>();
						tmp.add(nodeId);
						levelIdmap.put(level, tmp);
					} else {
						if (!levelIdmap.isEmpty()) {
							String previousLevel = getPreviousLevel(level);
							log.debug("level " + level + ",previousLevel:" + previousLevel);
							if (levelIdmap.containsKey(previousLevel)) {
								log.debug("LevelIdMap contains previous level " + level);
								List<LocationNodeData> nodeData = locationNodeDataDao.getNodeDataByTeamParentId(orgId,
										level, levelIdmap.get(previousLevel));
								List<Integer> idLists = nodeData.stream().map(x -> x.getId())
										.collect(Collectors.toList());
								levelIdmap.put(level, idLists);
								list.addAll(nodeData);
							}
						}

					}
				}
				log.debug("levelIdmap ::" + levelIdmap);

				levelIdmap.forEach((k, v) -> {
					reqNodeIds.addAll(v);
				});
			}

			log.debug("reqNodeIds:::" + reqNodeIds);
			List<LocationNodeDataV2> activeParentNodeList = ObjectMapperUtils
					.mapAll(locationNodeDataDao.getActiveLevelsForTeamEmpWithOutOrg(empId), LocationNodeDataV2.class);
			List<String> levels = activeParentNodeList.stream().map(x -> x.getType()).collect(Collectors.toList());
			levels = levels.stream().sorted().collect(Collectors.toList());
			log.debug("USER LEVELS " + levels + " and size:" + levels.size());
			String leafLevel = null;
			if (null != levels && levels.size() > 0) {
				leafLevel = levels.get(levels.size() - 1);
				log.debug("leafLevel:::" + leafLevel);
			}
			List<LocationNodeData> leafNodeList = locationNodeDataDao.getNodeDataTeamByLevel(orgId, leafLevel);

			List<Integer> leafNodeIdList = leafNodeList.stream().map(x -> x.getId()).collect(Collectors.toList());
			List<Integer> finalleafNodeIdList1 = new ArrayList<>();
			for (Integer i : reqNodeIds) {
				if (leafNodeIdList.contains(i)) {
					finalleafNodeIdList1.add(i);
				}
			}
			List<Integer> finalleafNodeIdList2 = new ArrayList<>();
			for (Integer i : finalleafNodeIdList1) {
				if (levelList.contains(i)) {
					finalleafNodeIdList2.add(i);
				}
			}
			List<Integer> finalleafNodeIdList = new ArrayList<>(new HashSet<Integer>(finalleafNodeIdList2));
			log.debug("finalleafNodeIdList::" + finalleafNodeIdList);

			for (Integer nodeId : finalleafNodeIdList) {
				log.debug("Getting data for nodeId::"+nodeId);
				DmsBranch branch = dmsBranchDao.getBranchTeamByOrgMpId(nodeId);
				int branchId = branch.getBranchId();
				log.debug("branchId::" + branchId);
				List<DmsEmployee> branchEmpList = dmsEmployeeRepo.getEmployeesTeamByOrg(orgId);
				Optional<DmsEmployee> empOpt = branchEmpList.stream().filter(x -> x.getEmp_id() == empId).findAny();
				////System.out.println("empOpt ::" + empOpt.isPresent());
				if (empOpt.isPresent()) {
					DmsEmployee emp = empOpt.get();
					//Map<String, Object> reportingHierarchyMap = getReportingHierarchy(emp, branchId, orgId);
					Map<String, Object> reportingHierarchyMap = getReportingTeamHierarchy3(emp, nodeId, orgId,branchId,startDate,endDate);
					reportingHierarchyMap = formatTeamReportingHierarchy3(reportingHierarchyMap);
					resMap.put(String.valueOf(branchId), reportingHierarchyMap);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return resMap;
	}
	
	public Map<String, Object> getReportingTeamHierarchy3(DmsEmployee emp, int nodeId, Integer orgId,int branchId,String startDate,String endDate) {
		long startTime = System.currentTimeMillis();
		log.debug("Inside getReportingHierarchy,branchId: " + branchId + ",orgId:" + orgId);
		Map<String, Object> empDtaMap = new LinkedHashMap<>();

		try {
			// get all the required static data first
			List<DmsDesignation> desOpt = dmsDesignationRepo.findByTeamOrg_id(orgId);
			List<DmsGrade> grades = dmsGradeDao.findAll();
			List<DmsEmployee> branchEmpList = dmsEmployeeRepo.getEmployeesTeamByOrg(orgId);
			// process current employee which recursively calls all child employees
			processEmployeeLevel3(empTooTD3(emp,orgId,startDate,endDate), emp, nodeId, orgId, empDtaMap, desOpt, grades, branchEmpList,empTooTD3(emp,orgId,startDate,endDate),branchId,startDate,endDate);
			log.debug("empDtaMap in getReportingHierarchy:::" + empDtaMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("Total Time = %s ms", (System.currentTimeMillis() - startTime));

		return empDtaMap;
	}
	
	private void processEmployeeLevel3(TeamAttendanceResponse3 td, DmsEmployee emp, int nodeId, Integer orgId, Map<String, Object> empLevelMap, 
			List<DmsDesignation> desOpt, List<DmsGrade> grades, List<DmsEmployee> branchEmpList,TeamAttendanceResponse3 topTd,int branchId,String startDate,String endDate) throws NumberFormatException, ParseException {
		//find the current employee's designation
		Optional<DmsDesignation> designation = desOpt.stream().filter(a -> a.getDmsDesignationId() == Integer.parseInt(emp.getDesignationId())).findFirst();
		int delpId = dmsDepartmentRepo.findDepartmentForSales(orgId,branchId).get(0);
		Set<Integer> preDesig=dmsDeptDesignationMappingRepo.findDesignationIdByDeptId(delpId);
		if(preDesig.contains(designation.get().getDmsDesignationId()) && !topTd.equals(td)) {
			Integer currentLevel = null;
			String levelName = null;
			String designationName = null;
			if(designation.isPresent()) {
				 DmsDesignation dmsDesignation = designation.get();
				 currentLevel = dmsDesignation.getLevel();
				 designationName = dmsDesignation.getDesignationName();
				try {
					//find the current employee's levelName 
					levelName = getLevelName2(grades, currentLevel);
				} catch (DynamicFormsServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Integer pme = Integer.valueOf(dmsEmployeeRepo.findById(Integer.valueOf(td.getCode())).get().getBranch());
			Boolean branchCheck=false;
			if(pme==branchId) {
				branchCheck=true;
			}
			//if map already contains items for the current employee's level, then add otherwise create new list and add to map
			if (designationName != null && empLevelMap.containsKey(designationName) && branchCheck) {
				List<TeamAttendanceResponse3> currentList =(List<TeamAttendanceResponse3>) empLevelMap.get(designationName);// buildDropDown(Integer.parseInt(td.getCode()),branchId, orgId);//
				currentList.add(td);
			} else if(branchCheck) {
				List<TeamAttendanceResponse3> currentList = new ArrayList<>();
				currentList.add(td);
				empLevelMap.put(designationName, currentList);
			}
		}
		//now process all the children
		List<TeamAttendanceResponse3> empList = buildDropDown4(emp.getEmp_id(), nodeId, orgId,startDate,endDate);
		for (TeamAttendanceResponse3 currentTd : empList) {
			DmsEmployee childEmp = getEmployee(branchEmpList, Integer.parseInt(currentTd.getCode()));
			if(childEmp!=null) {
				if(childEmp.getEmp_id() != emp.getEmp_id()) {
					processEmployeeLevel3(currentTd, childEmp, nodeId, orgId, empLevelMap, desOpt, grades, branchEmpList,topTd,branchId,startDate,endDate);
				}	
			}
		}
		
	}
	
	private List<TeamAttendanceResponse3> buildDropDown4(Integer id, Integer branch, Integer orgId,String startDate,String endDate) throws NumberFormatException, ParseException {
		// String query = "SELECT emp_id,emp_name FROM dms_employee where
		// reporting_to=<ID> and branch=<BRANCH> and org=<ORGID>";
//		String query = "\r\n" + "select emp_id,emp_name,reporting_to from dms_employee where org = <ORGID> "
//				+ "and reporting_to=<ID> and emp_id in (select emp_id from emp_location_mapping where org_id=<ORGID>"
//				+ " and location_node_data_id ='<BRANCH>')";
		String query = "\r\n" + "select emp.emp_id,emp.emp_name,emp.reporting_to,emp.org , emp.branch,emp.status from dms_employee emp\r\n"
				+ "		where emp.org = <ORGID> and emp.reporting_to=<ID> \r\n"
				+ "		and emp.emp_id in (select emp_id from emp_location_mapping where org_id=<ORGID> and location_node_data_id =<BRANCH>)";	
		query = query.replaceAll("<ID>", String.valueOf(id));
		query = query.replaceAll("<BRANCH>", String.valueOf(branch));
		query = query.replaceAll("<ORGID>", String.valueOf(orgId));
		log.debug("buildDropDown2 query " + query);
		List<TeamAttendanceResponse3> list = new ArrayList<>();
		List<Object[]> data = entityManager.createNativeQuery(query).getResultList();

		for (Object[] arr : data) {
			TeamAttendanceResponse3 trRoot = new TeamAttendanceResponse3();
			trRoot.setCode(String.valueOf(arr[0]));
			trRoot.setName(String.valueOf(arr[1]));
			trRoot.setParentId(String.valueOf(arr[2]));
			trRoot.setOrgId(String.valueOf(arr[3]));
			trRoot.setBranch(String.valueOf(arr[4]));
			trRoot.setStatus(String.valueOf(arr[5]));
			AttendanceCount ac = getfilterAttendanceCount(Integer.valueOf(String.valueOf(arr[0])),Integer.valueOf(String.valueOf(arr[3])),startDate,endDate);
			Long current=dmsEmployeeAttendancedao.getEmpNotLoggedIn(Integer.valueOf(String.valueOf(arr[0])));
			if(ac!=null) {
				trRoot.setPresent(ac.getPresent());
				trRoot.setLeave(ac.getLeave());
				trRoot.setWfh(ac.getWfh());
				trRoot.setHoliday(ac.getHolidays());
				if (!(current == 0)) {
					trRoot.setNotLoggedIn(0);
				}
				else{
					trRoot.setNotLoggedIn(1);
					}
			
				
				
				trRoot.setTotal(ac.getTotal());			
			}
			String query2 = "SELECT name\r\n"
					+ "FROM salesDataSetup.location_node_data where id in (SELECT parent_id\r\n"
					+ "FROM salesDataSetup.location_node_data da where da.id in (select org_map_id from dms_branch where branch_id=<BRANCH>))";
			query2 = query2.replaceAll("<BRANCH>", trRoot.getBranch());
			Object data1 = entityManager.createNativeQuery(query2).getResultList();
			
				trRoot.setLocation(String.valueOf(data1));
			
			locationNodeDataDao.findById(orgId);
			trRoot.setBranchName(dmsBranchDao.findById(Integer.valueOf(String.valueOf(arr[4]))).get().getName());
			list.add(trRoot);
		}
		list = list.stream().distinct().collect(Collectors.toList());
		return list;
	}
	@Autowired
	DmsEmployeeAttendancedao dmsEmployeeAttendancedao;
	
	@Autowired
	DmsHolidayListDao dmsHolidayListDao;
	
	public AttendanceCount getfilterAttendanceCount(int empId, int orgId,String startDate,String endDate) throws ParseException {
		List<DmsEmployeeAttendanceEntity> attendence = dmsEmployeeAttendancedao.getEmpWisefilter(empId,orgId,startDate,endDate);
		Timestamp ts=new Timestamp(System.currentTimeMillis());
		Date date=new Date(ts.getTime());
		System.out.println(date);
		String startDate2 = date+" "+"00:00:00";
		String endDate2 = date +" "+"23:59:59";
		Long current=dmsEmployeeAttendancedao.getEmpNotLoggedIn(empId);
		List<DmsEmployeeAttendanceEntity> attendence2 = dmsEmployeeAttendancedao.getEmpWisefilter(empId,orgId,startDate,endDate);
		long holidaylist = dmsHolidayListDao.getholidaylist(orgId,startDate,endDate);
		AttendanceCount attendanceCount = new AttendanceCount();
		long isPresent = attendence.stream().filter(t->t.getIsPresent() == 1).count();
		long isAbsent = attendence.stream().filter(t->t.getIsAbsent() == 1).count();
		long workfromhome = attendence.stream().filter(t->t.getWfh() == 1).count();
		long holidays = holidaylist;
		attendanceCount.setHolidays(holidaylist);
		attendanceCount.setLeave(attendence.stream().filter(t->t.getIsAbsent() == 1).count());
		attendanceCount.setWfh(attendence.stream().filter(t->t.getWfh() == 1).count());
		attendanceCount.setPresent(attendence.stream().filter(t->t.getIsPresent() == 1).count());
		if (!(current == 0)) {
			attendanceCount.setNotLoggedIn(0);
		}
		else{
			attendanceCount.setNotLoggedIn(1);
			}
		if (!(current == 0)) {
		attendanceCount.setTotal(isPresent+isAbsent+workfromhome+holidays+0);
		}
		else {
			attendanceCount.setTotal(isPresent+isAbsent+workfromhome+holidays+1);	
		}
        long totaltimevalue =0 ;
        for (int i = 0; i < attendence.size();i++)
        {
        	if (attendence.get(i).getWorkingHours() != null) {
        	String time = attendence.get(i).getWorkingHours().toString();
        	LocalTime localtime=LocalTime.parse(time);
        	long millsec = localtime.toSecondOfDay();
        	totaltimevalue = totaltimevalue + millsec;
        	}
        }
        if(attendence2.isEmpty()) {
			attendanceCount.setLogedIn(false);
		}else {
			attendanceCount.setLogedIn(true);
		}
        long millis = totaltimevalue;
	    String hms = String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(millis),
	            TimeUnit.SECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(millis)),
	            TimeUnit.SECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(millis)));
	    System.out.println(hms);
	    attendanceCount.setTotalTime(hms);
				return attendanceCount;
	}
	
	
		private TeamAttendanceResponse3 empTooTD3(DmsEmployee emp,int orgId,String startDate,String endDate) throws ParseException {
			TeamAttendanceResponse3 td = new TeamAttendanceResponse3();
			td.setCode(String.valueOf(emp.getEmp_id()));
			td.setName(emp.getEmpName());
			AttendanceCount ac = getfilterAttendanceCount(emp.getEmp_id(),orgId,startDate,endDate);
			if(ac!=null) {
				td.setPresent(ac.getPresent());
				td.setLeave(ac.getLeave());
				td.setWfh(ac.getWfh());
				td.setHoliday(ac.getHolidays());
				td.setTotal(ac.getTotal());		
				td.setLoggedIn(ac.isLogedIn());
			}
			return td;
		}
		
		private Map<String, Object> formatTeamReportingHierarchy3(Map<String, Object> hMap) {
			if (null != hMap) {
				AtomicInteger a = new AtomicInteger(0);
				for (Map.Entry<String, Object> mapentry : hMap.entrySet()) {
					a.incrementAndGet();
					List<TeamAttendanceResponse3> ddList = (List<TeamAttendanceResponse3>) mapentry.getValue();
					
					ddList = ddList.stream().distinct().collect(Collectors.toList());
					ddList.forEach(x -> {
						x.setDesignation(mapentry.getKey());
						x.setOrder(a.intValue());
					});
				}
			}
			for (Map.Entry<String, Object> mapentry : hMap.entrySet()) {
				List<TeamAttendanceResponse3> list = new ArrayList<>();
				List<TeamAttendanceResponse3> ddList = (List<TeamAttendanceResponse3>) mapentry.getValue();
				ddList = ddList.stream().distinct().collect(Collectors.toList());
				list.addAll(ddList);
				list = list.stream().distinct().collect(Collectors.toList());
				hMap.put(mapentry.getKey(), list);
			}
			return hMap;
		}
		
		@Override
		public List<BranchResponce> getBranches(Integer orgId, int id)
				throws DynamicFormsServiceException {
			
			List<BranchResponce> res = new ArrayList<>();
			
			
			try {
				List<LocationNodeData>	list = locationNodeDataDao.getActiveData(orgId, id);
				if(!list.isEmpty()) {
					for(LocationNodeData single:list) {
						
						DmsBranch branch=dmsBranchDao.getBranchByOrgMpId(single.getId());
						BranchResponce one=new BranchResponce();
						if(branch != null) {
							one.setBranchId(branch.getBranchId());
							one.setBranchName(branch.getName());
							res.add(one);
						}
						
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new DynamicFormsServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return res;
		}







	@Override
	public Map<String, Object> getCRMChildUser(List<Integer> levelList, Integer orgId, Integer empId,String dashboard_type)
			throws DynamicFormsServiceException {
		List<LocationNodeData> list = new ArrayList<>();
		List<Integer> reqNodeIds = new ArrayList<>();
		Map<String, Object> resMap = new LinkedHashMap<>();
		try {
			for (Integer nodeId : levelList) {
				log.debug("nodeId::::"+nodeId);
				String levelName = locationNodeDataDao.getLevelname(nodeId);
				log.debug("Given node level " + levelName + " and nodeId " + nodeId);
				List<String> levels = getOrgLevels(orgId).stream().map(x -> x.getLocationNodeDefType()).collect(Collectors.toList());
				log.debug("levels before " + levels);
				log.debug("levelName::"+levelName);
				levels = levels.subList(levels.indexOf(levelName), levels.size());
				levels = levels.stream().sorted().collect(Collectors.toList());
				log.debug("levels after " + levels);
				Map<String, List<Integer>> levelIdmap = new LinkedHashMap<>();
				for (int i = 0; i < levels.size(); i++) {
					String level = levels.get(i);
					if (i == 0) {
						List<Integer> tmp = new ArrayList<>();
						tmp.add(nodeId);
						levelIdmap.put(level, tmp);
					} else {
						if (!levelIdmap.isEmpty()) {
							String previousLevel = getPreviousLevel(level);
							log.debug("level " + level + ",previousLevel:" + previousLevel);
							if (levelIdmap.containsKey(previousLevel)) {
								log.debug("LevelIdMap contains previous level " + level);
								List<LocationNodeData> nodeData = locationNodeDataDao.getNodeDataByParentId(orgId,
										level, levelIdmap.get(previousLevel));
								List<Integer> idLists = nodeData.stream().map(x -> x.getId())
										.collect(Collectors.toList());
								levelIdmap.put(level, idLists);
								list.addAll(nodeData);
							}
						}

					}
				}
				log.debug("levelIdmap ::" + levelIdmap);

				levelIdmap.forEach((k, v) -> {
					reqNodeIds.addAll(v);
				});
			}

			log.debug("reqNodeIds:::" + reqNodeIds);
			List<LocationNodeDataV2> activeParentNodeList = ObjectMapperUtils
					.mapAll(locationNodeDataDao.getActiveLevelsForEmpWithOutOrg(empId), LocationNodeDataV2.class);
			List<String> levels = activeParentNodeList.stream().map(x -> x.getType()).collect(Collectors.toList());
			levels = levels.stream().sorted().collect(Collectors.toList());
			log.debug("USER LEVELS " + levels + " and size:" + levels.size());
			String leafLevel = null;
			if (null != levels && levels.size() > 0) {
				leafLevel = levels.get(levels.size() - 1);
				log.debug("leafLevel:::" + leafLevel);
			}
			List<LocationNodeData> leafNodeList = locationNodeDataDao.getNodeDataByLevel(orgId, leafLevel);

			List<Integer> leafNodeIdList = leafNodeList.stream().map(x -> x.getId()).collect(Collectors.toList());
			List<Integer> finalleafNodeIdList1 = new ArrayList<>();
			for (Integer i : reqNodeIds) {
				if (leafNodeIdList.contains(i)) {
					finalleafNodeIdList1.add(i);
				}
			}
			List<Integer> finalleafNodeIdList2 = new ArrayList<>();
			for (Integer i : finalleafNodeIdList1) {
				if (levelList.contains(i)) {
					finalleafNodeIdList2.add(i);
				}
			}
			List<Integer> finalleafNodeIdList = new ArrayList<>(new HashSet<Integer>(finalleafNodeIdList2));
			log.debug("finalleafNodeIdList::" + finalleafNodeIdList);


			List<Integer> branchIdList = new ArrayList<>();
			for (Integer nodeId : finalleafNodeIdList) {
				log.debug("Getting data for nodeId::" + nodeId);
				DmsBranch branch = dmsBranchDao.getBranchByOrgMpId(nodeId);
				int branchId = branch.getBranchId();
				branchIdList.add(branch.getBranchId());
			}


			DmsEmployee dmsEmployeeObj = dmsEmployeeRepo.getById(empId);
			DMSEmployee dmsEmp = empRepo.findemp(dmsEmployeeObj.getEmpName(),orgId);
			String loginUserRole = dmsEmp.getDmsRole().getRoleName();

			List<String> roleNames = new ArrayList<>();
			roleNames.add("receptionist Manager");
			roleNames.add("reception Manager");
			roleNames.add("CRM");
			roleNames.add("Tele caller Manager");
			roleNames.add("Telecaller Manager");

			boolean isSales = true;
			for (int i = 0; i <roleNames.size() ; i++) {
				if(roleNames.get(i).equalsIgnoreCase(loginUserRole)){
					isSales = false;
				}
			}

			List<String> designationName = new ArrayList<>();
			List<TargetDropDownV2> targetDropDownV2List = new ArrayList<>();
			if(dashboard_type.equalsIgnoreCase("digital")){
				List<DmsEmployee> teleCallerList =  dmsEmployeeRepo.findemps(orgId,branchIdList,"Tele Caller");
				List<DmsEmployee> creList =  dmsEmployeeRepo.findemps(orgId,branchIdList,"CRE");

				if(isSales){
					List<DmsEmployee> crmList =  dmsEmployeeRepo.findemps(orgId,branchIdList,"CRM");
					List<DmsEmployee> telecallerManager =  dmsEmployeeRepo.findemps(orgId,branchIdList,"Tele caller Manager");
					List<DmsEmployee> telecallerManager1 =  dmsEmployeeRepo.findemps(orgId,branchIdList,"Telecaller Manager");
					if(crmList!=null && crmList.size()>0){
						designationName.add("CRM");
						targetDropDownV2List.addAll(setData(crmList,"CRM"));
					}
					if(telecallerManager!=null && telecallerManager.size()>0){
						designationName.add("Tele caller Manager");
						targetDropDownV2List.addAll(setData(telecallerManager,"Tele caller Manager"));
					}
					if(telecallerManager1!=null && telecallerManager1.size()>0){
						designationName.add("Telecaller Manager");
						targetDropDownV2List.addAll(setData(telecallerManager1,"Telecaller Manager"));
					}
				}

				if(teleCallerList!=null && teleCallerList.size()>0){
					designationName.add("Tele Caller");
					targetDropDownV2List.addAll(setData(teleCallerList,"Tele Caller"));
				}
				if(creList!=null && creList.size()>0){
					designationName.add("CRE");
					targetDropDownV2List.addAll(setData(creList,"CRE"));
				}

				resMap.put("Select Designation",designationName);
				resMap.put("Select Employee",targetDropDownV2List);

			}else {

				List<DmsEmployee> receptionList =  dmsEmployeeRepo.findemps(orgId,branchIdList,"Reception");

				if(receptionList!=null && receptionList.size()>0){
					designationName.add("Reception");
					targetDropDownV2List.addAll(setData(receptionList,"Reception"));
				}


				if(isSales){
					List<DmsEmployee> receptionistManager =  dmsEmployeeRepo.findemps(orgId,branchIdList,"receptionist Manager");
					List<DmsEmployee> receptionManager =  dmsEmployeeRepo.findemps(orgId,branchIdList,"reception Manager");
					if(receptionistManager!=null && receptionistManager.size()>0){
						designationName.add("receptionist Manager");
						targetDropDownV2List.addAll(setData(receptionistManager,"receptionist Manager"));
					}
					if(receptionManager!=null && receptionManager.size()>0){
						designationName.add("reception Manager");
						targetDropDownV2List.addAll(setData(receptionManager,"reception Manager"));
					}
				}

				resMap.put("Select Designation",designationName);
				resMap.put("Select Employee",targetDropDownV2List);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return resMap;
	}

	private List<TargetDropDownV2> setData(List<DmsEmployee> list ,String role){
		List<TargetDropDownV2> targetDropDownV2List = new ArrayList<>();
		for (int i = 0; i <list.size() ; i++) {
			TargetDropDownV2 targetDropDownV2 = new TargetDropDownV2();
			targetDropDownV2.setCode(String.valueOf(list.get(i).getEmp_id()));
			targetDropDownV2.setDesignation(role);
			targetDropDownV2.setName(list.get(i).getEmpName());
			targetDropDownV2.setParentId(list.get(i).getReportingTo());
			targetDropDownV2List.add(targetDropDownV2);
		}
		return targetDropDownV2List;
	}

}
