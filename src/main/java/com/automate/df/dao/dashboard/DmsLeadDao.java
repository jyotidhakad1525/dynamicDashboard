package com.automate.df.dao.dashboard;

import java.util.ArrayList;
import java.util.List;

import io.swagger.models.auth.In;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.automate.df.entity.dashboard.DmsLead;

public interface DmsLeadDao extends JpaRepository<DmsLead, Integer> {

	
	@Query(value = "SELECT * FROM dms_lead where sales_consultant = :empName and createddatetime>=:startDate\r\n"
			+ "and createddatetime<=:endDate and lead_stage=:leadType", nativeQuery = true)
	List<DmsLead> getLeads(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "leadType") String leadType);
	
	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) and createddatetime>=:startDate\r\n"
			+ "and createddatetime<=:endDate and lead_stage=:leadType", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeads(@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "leadType") String leadType);
	
	@Query(value = "SELECT count(*) FROM dms_lead where sales_consultant = :empName and createddatetime>=:startDate\r\n"
			+ "and createddatetime<=:endDate and lead_stage=:leadType", nativeQuery = true)
	Integer getLeadsCount(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "leadType") String leadType);
	
	//-----------
	
	@Query(value = "SELECT count(*) FROM dms_lead A, dms_employee E , dms_role R where A.modifieddatetime>=:startDate "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by = :loginEmpName"
			+ " and A.modifieddatetime<=:endDate and A.lead_stage=:leadType and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getAllLeadsCount(
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "leadType") String leadType,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName,
			@Param(value = "roleName") String roleName);
	
	@Query(value = "SELECT count(*) FROM dms_lead A , dms_branch B, dms_employee E , dms_role R "
			+ " where A.branch_id = B.branch_id and B.dealer_code = :dealerCode AND A.modifieddatetime>=:startDate "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by = :loginEmpName "
			+ " and A.modifieddatetime<=:endDate and A.lead_stage=:leadType and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getAllLeadsCount(
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "leadType") String leadType,
			@Param(value = "orgId") int orgId,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "loginEmpName") String loginEmpName,
			@Param(value = "roleName") String roleName);
	
	
	@Query(value = "SELECT count(*) FROM dms_lead where sales_consultant = :empName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and created_by = :loginEmpName and lead_stage not in ('DROPPED') and organization_id=:orgId  and sales_consultant is not null", nativeQuery = true)
	Integer getAllocatedLeadsCountByEmp(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName);

	@Query(value = "SELECT count(*) FROM dms_lead where sales_consultant = :empName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and created_by = :loginEmpName and lead_stage not in ('DROPPED') and organization_id=:orgId and branch_id in(:dealerCode) and sales_consultant is not null", nativeQuery = true)
	Integer getAllocatedLeadsCountByEmp(@Param(value = "empName") String empName,
										@Param(value = "startDate") String startDate,
										@Param(value = "endDate") String endDate,
										@Param(value = "orgId") int orgId,
										@Param(value = "dealerCode") List<Integer> dealerCode,
										@Param(value = "loginEmpName") String loginEmpName);
	
	@Query(value = "SELECT count(*) FROM dms_lead  A , dms_branch B, dms_employee E , dms_role R  where "
			+ " A.branch_id = B.branch_id and B.dealer_code in (:dealerCode)"
			+ " and A.sales_consultant = :empName and A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by = :loginEmpName "
			+ " and A.modifieddatetime<=:endDate and A.lead_stage not in ('DROPPED') and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getAllocatedLeadsCountByEmp(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "dealerCode") List<String> dealerCode,
			@Param(value = "loginEmpName") String loginEmpName ,
			@Param(value = "roleName") String roleName
			);
	
	@Query(value = "SELECT count(*) FROM dms_lead A, dms_employee E , dms_role R where A.sales_consultant = :empName and A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by = :loginEmpName"
			+ "	 and A.modifieddatetime<=:endDate and A.lead_stage in ('DROPPED') and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getDropeedLeadsCountByEmp(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where sales_consultant = :empName and created_by = :loginEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage in ('DROPPED') and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getDropeedLeadsCountByEm(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName);
	
	@Query(value = "SELECT count(*) FROM dms_lead where sales_consultant = :empName and created_by = :loginEmpName and  lead_stage in ('DROPPED') and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveDropCount(@Param(value = "empName") String empName,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName);
	
	@Query(value = "SELECT count(*) FROM dms_lead where sales_consultant = :empName and created_by = :loginEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage in ('DROPPED') and organization_id=:orgId and branch_id in(:dealerCode) and sales_consultant is not null", nativeQuery = true)
	Integer getDropeedLeadsCountByEm(@Param(value = "empName") String empName,
									 @Param(value = "startDate") String startDate,
									 @Param(value = "endDate") String endDate,
									 @Param(value = "orgId") int orgId,
									 @Param(value = "dealerCode") List<Integer> dealerCode,
									 @Param(value = "loginEmpName") String loginEmpName);
	
	@Query(value = "SELECT count(*) FROM dms_lead  A , dms_branch B, dms_employee E , dms_role R where "
			+ "	 A.branch_id = B.branch_id and B.dealer_code in (:dealerCode) AND  A.sales_consultant = :empName and A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by = :loginEmpName"
			+ "	 and A.modifieddatetime<=:endDate and A.lead_stage in ('DROPPED') and A.organization_id=:orgId  and sales_consultant is not null", nativeQuery = true)
	Integer getDropeedLeadsCountByEmp(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "dealerCode") List<String> dealerCode,
			@Param(value = "loginEmpName") String loginEmpName,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loginEmpName and lead_stage not in ('DROPPED') and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and organization_id=:orgId and sales_consultant is not null and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active')", nativeQuery = true)
	Integer getAllocatedLeadsCount(@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName);

	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loginEmpName and lead_stage not in ('DROPPED')  and organization_id=:orgId and sales_consultant is not null and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active')", nativeQuery = true)
	long getAllocatedLiveLeadsCount(@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName);
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loginEmpName and lead_stage not in ('DROPPED') and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and organization_id=:orgId and branch_id in(:dealerCode) and sales_consultant is not null and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active')", nativeQuery = true)
	Integer getAllocatedLeadsCount(@Param(value = "startDate") String startDate,
								   @Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
								   @Param(value = "dealerCode") List<Integer> dealerCode,
								   @Param(value = "loginEmpName") String loginEmpName);
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loginEmpName and lead_stage not in ('DROPPED')  and organization_id=:orgId and branch_id in(:dealerCode) and sales_consultant is not null and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active')", nativeQuery = true)
	long getAllocatedLiveLeadsCountW(@Param(value = "orgId") int orgId,
								   @Param(value = "dealerCode") List<Integer> dealerCode,
								   @Param(value = "loginEmpName") String loginEmpName);
	
	@Query(value = "SELECT count(*) FROM dms_lead A , dms_branch B , dms_employee E , dms_role R  where A.branch_id = B.branch_id and B.dealer_code in (:dealerCode) AND A.modifieddatetime>=:startDate "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by = :loginEmpName"
			+ " and A.modifieddatetime<=:endDate and A.lead_stage not in ('DROPPED') and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getAllocatedLeadsCount(@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
			@Param(value = "dealerCode") List<String> dealerCode,
			@Param(value = "loginEmpName") String loginEmpName,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT count(*) FROM dms_lead A, dms_employee E , dms_role R where A.modifieddatetime>=:startDate "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by = :loginEmpName"
			+ " and A.modifieddatetime<=:endDate and A.lead_stage in ('DROPPED') and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getDroppedLeadsCount(@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loginEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage in ('DROPPED') and organization_id=:orgId and sales_consultant is not null and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active')", nativeQuery = true)
	Integer 	getDroppedLeadsCountt(@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName);
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loginEmpName  and lead_stage in ('DROPPED') and organization_id=:orgId and sales_consultant is not null and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active')", nativeQuery = true)
	long 	getDroppedLiveLeadsCountt( @Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName);

	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loginEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage in ('DROPPED') and organization_id=:orgId and branch_id in(:dealerCode) and sales_consultant is not null  and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active')", nativeQuery = true)
	Integer getDroppedLeadsCountt(@Param(value = "startDate") String startDate,
								  @Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
								  @Param(value = "dealerCode") List<Integer> dealerCode,
								  @Param(value = "loginEmpName") String loginEmpName);
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loginEmpName and lead_stage in ('DROPPED') and organization_id=:orgId and branch_id in(:dealerCode) and sales_consultant is not null  and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active')", nativeQuery = true)
	long getDroppedLiveLeadsCountt(@Param(value = "orgId") int orgId,
								  @Param(value = "dealerCode") List<Integer> dealerCode,
								  @Param(value = "loginEmpName") String loginEmpName);

	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loginEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage in ('DROPPED') and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getDroppedLeadsCountt1(@Param(value = "startDate") String startDate,
								  @Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
								  @Param(value = "loginEmpName") String loginEmpName);


	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loginEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage in ('DROPPED') and organization_id=:orgId and sales_consultant is not null and branch_id in (:branch_id)", nativeQuery = true)
	Integer getDroppedLeadsCountt1(@Param(value = "startDate") String startDate,
								   @Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
								   @Param(value = "loginEmpName") String loginEmpName,
								   @Param(value = "branch_id") List<Integer> branch_id);

//	@Query(value = "SELECT count(*) FROM dms_lead where lead_stage in ('DROPPED') and id in (:id)", nativeQuery = true)
//	Long getDroppedLeadsCountt1(@Param(value = "id") List<Integer> id);

	@Query(value = "SELECT count(*) FROM dms_lead where  created_by in (:loginEmpName) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage in ('DROPPED') and organization_id=:orgId and sales_consultant is not null and branch_id in (:branch_id) and model =:model", nativeQuery = true)
	Long getDroppedLeadsCountt1(@Param(value = "startDate") String startDate,
									 @Param(value = "endDate") String endDate,
									 @Param(value = "orgId") int orgId,
									 @Param(value = "loginEmpName") List<String> loginEmpName,
									 @Param(value = "branch_id") List<Integer> branch_id,
								     @Param(value = "model") String model);
	
	@Query(value = "SELECT count(*) FROM dms_lead where  created_by in (:loginEmpName) and lead_stage in ('DROPPED') and organization_id=:orgId and sales_consultant is not null and branch_id in (:branch_id) and model =:model", nativeQuery = true)
	Long getDroppedLeadsCountLiveleadFilter(
									 @Param(value = "orgId") int orgId,
									 @Param(value = "loginEmpName") List<String> loginEmpName,
									 @Param(value = "branch_id") List<Integer> branch_id,
								     @Param(value = "model") String model);
	
	@Query(value = "SELECT count(*) FROM dms_lead where  created_by in (:loginEmpName) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage in ('DROPPED') and organization_id=:orgId and sales_consultant is not null and model =:model", nativeQuery = true)
	Long getDroppedLeadsCountt1(@Param(value = "startDate") String startDate,
								@Param(value = "endDate") String endDate,
								@Param(value = "orgId") int orgId,
								@Param(value = "loginEmpName") List<String> loginEmpName,
								@Param(value = "model") String model);
	
	@Query(value = "SELECT count(*) FROM dms_lead where  created_by in (:loginEmpName) and lead_stage in ('DROPPED') and organization_id=:orgId and sales_consultant is not null and model =:model", nativeQuery = true)
	Long getDroppedLeadsCountM(@Param(value = "orgId") int orgId,
								@Param(value = "loginEmpName") List<String> loginEmpName,
								@Param(value = "model") String model);
	

	@Query(value = "SELECT count(*) FROM dms_lead A , dms_branch B , dms_employee E , dms_role R where A.branch_id = B.branch_id and B.dealer_code in (:dealerCode) AND A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by = :loginEmpName"
			+ " and A.modifieddatetime<=:endDate and A.lead_stage in ('DROPPED') and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getDroppedLeadsCount(@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "loginEmpName") String loginEmpName,
			@Param(value = "roleName") String roleName );
	
	// ----------------

	@Query(value = "SELECT id FROM dms_lead where sales_consultant in(:empNamesList) and lead_stage not in ('DROPPED') ", nativeQuery = true)
	List<Integer> getLeadIdsByEmpNamesWithOutDrop(@Param(value = "empNamesList") List<String> empNamesList);
	
	//and  createddatetime>=:startDate and createddatetime<=:endDate
	/*
	 * @Query(value =
	 * "SELECT id FROM dms_lead where sales_consultant in(:empNamesList) and lead_stage not in ('DROPPED') and model in (:model)"
	 * , nativeQuery = true) List<Integer>
	 * getLeadIdsByEmpNamesWithOutDrop1(@Param(value = "empNamesList") List<String>
	 * empNamesList, @Param(value = "model") List<String> model, @Param(value =
	 * "startDate") String startDate,
	 * 
	 * @Param(value = "endDate") String endDate);
	 */
	
	@Query(value = "SELECT id FROM dms_lead where sales_consultant in(:empNamesList) and lead_stage not in ('DROPPED') and model in (:model)", nativeQuery = true)
	List<Integer> getLeadIdsByEmpNamesWithOutDrop1(@Param(value = "empNamesList") List<String> empNamesList, @Param(value = "model") List<String> model);
	
	@Query(value = "SELECT id FROM dms_lead where sales_consultant in(:empNamesList) and lead_stage in ('DROPPED') ", nativeQuery = true)
	List<Integer> getLeadIdsByEmpNamesWithDrop(@Param(value = "empNamesList") List<String> empNamesList);
	
	/*
	 * @Query(value =
	 * "SELECT id FROM dms_lead where sales_consultant in(:empNamesList) and lead_stage in ('DROPPED') and model in (:model) AND createddatetime>=:startDate and createddatetime<=:endDate"
	 * , nativeQuery = true) List<Integer>
	 * getLeadIdsByEmpNamesWithDrop1(@Param(value = "empNamesList") List<String>
	 * empNamesList, @Param(value = "model") List<String> model, @Param(value =
	 * "startDate") String startDate,
	 * 
	 * @Param(value = "endDate") String endDate);
	 */
	
	@Query(value = "SELECT id FROM dms_lead where sales_consultant in(:empNamesList) and lead_stage in ('DROPPED') and model in (:model)", nativeQuery = true)
	List<Integer> getLeadIdsByEmpNamesWithDrop1(@Param(value = "empNamesList") List<String> empNamesList, @Param(value = "model") List<String> model);
	
	
	// Vehicle model query starts here 
	@Query(value = "select distinct model from dms_lead", nativeQuery = true)
	List<String> getModelNames();
	
	
	
	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) and createddatetime>=:startDate\r\n"
			+ "and createddatetime<=:endDate and model=:model and organization_id=:orgId ", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsWithModel(
			@Param(value = "orgId") String orgId,
			
			@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "model") String model);
	
	
	/*
	 * @Query(value =
	 * "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) and createddatetime>=:startDate\r\n"
	 * +
	 * "and createddatetime<=:endDate and model=:model and organization_id=:orgId and lead_stage not in ('DROPPED')"
	 * , nativeQuery = true) List<Integer> getAllEmployeeLeadsWithModel1(
	 * 
	 * @Param(value = "orgId") String orgId,
	 * 
	 * @Param(value = "empNamesList") List<String> empNamesList,
	 * 
	 * @Param(value = "startDate") String startDate,
	 * 
	 * @Param(value = "endDate") String endDate,
	 * 
	 * @Param(value = "model") String model);
	 */
	
	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) and \r\n"
			+ "model=:model and organization_id=:orgId and lead_stage not in ('DROPPED')", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsWithModel1(
			@Param(value = "orgId") String orgId,
			@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "model") String model);
	
	
	/*
	 * @Query(value =
	 * "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) and createddatetime>=:startDate\r\n"
	 * +
	 * "and createddatetime<=:endDate and model=:model and organization_id=:orgId and lead_stage in ('DROPPED')"
	 * , nativeQuery = true) List<Integer> getAllEmployeeLeadsWithModel11(
	 * 
	 * @Param(value = "orgId") String orgId,
	 * 
	 * @Param(value = "empNamesList") List<String> empNamesList,
	 * 
	 * @Param(value = "startDate") String startDate,
	 * 
	 * @Param(value = "endDate") String endDate,
	 * 
	 * @Param(value = "model") String model);
	 */
	
	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) \r\n"
			+ "and model=:model and organization_id=:orgId and lead_stage in ('DROPPED')", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsWithModel11(
			@Param(value = "orgId") String orgId,
			@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "model") String model);
	
	// Vehicle model query ends here
	
	
	// Lead Source and  EventSource query starts here
	
	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) and createddatetime>=:startDate\r\n"
			+ "and createddatetime<=:endDate and source_of_enquiry=:enqId and organization_id=:orgId", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsBasedOnEnquiry(
			@Param(value = "orgId") String orgId,
		
			@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "enqId") Integer enqId);
	
	/*
	 * @Query(value =
	 * "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) and createddatetime>=:startDate\r\n"
	 * +
	 * "and createddatetime<=:endDate and source_of_enquiry=:enqId and model in(:vehicleModelList) and organization_id=:orgId and lead_stage not in ('DROPPED')"
	 * , nativeQuery = true) List<Integer> getAllEmployeeLeadsBasedOnEnquiry1(
	 * 
	 * @Param(value = "orgId") String orgId,
	 * 
	 * @Param(value = "empNamesList") List<String> empNamesList,
	 * 
	 * @Param(value = "startDate") String startDate,
	 * 
	 * @Param(value = "endDate") String endDate,
	 * 
	 * @Param(value = "enqId") Integer enqId,
	 * 
	 * @Param(value = "vehicleModelList") List<String> vehicleModelList);
	 */
	
	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) \r\n"
			+ "and source_of_enquiry=:enqId and model in(:vehicleModelList) and organization_id=:orgId and lead_stage not in ('DROPPED')", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsBasedOnEnquiry1(
			@Param(value = "orgId") String orgId,
		
			@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "enqId") Integer enqId,
			@Param(value = "vehicleModelList") List<String> vehicleModelList);
	
	@Query(value = "SELECT * FROM dms_lead where  created_by in (:empNamesList) \r\n"
			+ "and event_code=:enqId and organization_id=:orgId and lead_stage not in ('DROPPED')", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsBasedOnEnquiryEventPre(
			@Param(value = "orgId") String orgId,
			@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "enqId") String enqId);
	
	
	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) \r\n"
			+ "and event_code=:enqId and organization_id=:orgId and lead_stage not in ('DROPPED')", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsBasedOnEnquiryEvents(
			@Param(value = "orgId") String orgId,
		
			@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "enqId") String enqId);
	
	
	/*
	 * @Query(value =
	 * "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) and createddatetime>=:startDate\r\n"
	 * +
	 * "and createddatetime<=:endDate and source_of_enquiry=:enqId and model in(:vehicleModelList) and organization_id=:orgId and lead_stage in ('DROPPED')"
	 * , nativeQuery = true) List<Integer> getAllEmployeeLeadsBasedOnEnquiry11(
	 * 
	 * @Param(value = "orgId") String orgId,
	 * 
	 * @Param(value = "empNamesList") List<String> empNamesList,
	 * 
	 * @Param(value = "startDate") String startDate,
	 * 
	 * @Param(value = "endDate") String endDate,
	 * 
	 * @Param(value = "enqId") Integer enqId,
	 * 
	 * @Param(value = "vehicleModelList") List<String> vehicleModelList);
	 */
	
	
	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) \r\n"
			+ "and source_of_enquiry=:enqId and model in(:vehicleModelList) and organization_id=:orgId and lead_stage in ('DROPPED')", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsBasedOnEnquiry11(
			@Param(value = "orgId") String orgId,
			@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "enqId") Integer enqId,
			@Param(value = "vehicleModelList") List<String> vehicleModelList);
	
	
	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) \r\n"
			+ "and event_code=:enqId and organization_id=:orgId and lead_stage in ('DROPPED')", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsBasedOnEnquiryEvents11(
			@Param(value = "orgId") String orgId,
			@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "enqId") String enqId);
	
	
	// Lead Source and  EventSource query ends here
	
	
	//Lost Drop query starts
	
	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) and createddatetime>=:startDate\r\n"
			+ "and createddatetime<=:endDate and model in (:model) and lead_stage=:leadType and organization_id=:orgId", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsWithModelandStage(
			@Param(value = "orgId") String orgId,
			
			@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "model") List<String> model,
			@Param(value = "leadType") String leadType);
	
	

	@Query(value="SELECT * FROM dms_lead WHERE first_name=:firstName and last_name=:lastName", nativeQuery = true)
	List<DmsLead> verifyFirstName(@Param(value = "firstName") String firstName,@Param(value = "lastName") String lastName);

	
	@Query(value="SELECT * FROM dms_lead WHERE crm_universal_id=:unversalId", nativeQuery = true)
	List<DmsLead> getLeadByUniversalId(@Param(value = "unversalId") String unversalId);
	
	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) and createddatetime>=:startDate\r\n"
			+ "and createddatetime<=:endDate and lead_stage in (:leadStages) and organization_id=:orgId", nativeQuery = true)
	List<DmsLead> getLeadsBasedonStage(@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") String orgId,
			@Param(value = "leadStages") List<String> leadStages);

	

	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) and createddatetime>=:startDate\r\n"
			+ "and createddatetime<=:endDate and lead_status=:leadType", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsByLeadStatus(@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "leadType") String leadType);

	@Query(value = "SELECT * FROM dms_lead where sales_consultant in(:empNamesList) and createddatetime>=:startDate\r\n"
			+ "and createddatetime<=:endDate", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeasForDate(@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate);

	@Query(value = "SELECT * FROM dms_lead where id in(:idList)", nativeQuery = true)
	List<DmsLead> getLeadsBasedonId(@Param(value = "idList") List<Integer> idList);

	@Query(value = "SELECT id FROM dms_lead where sales_consultant in(:empNamesList) and createddatetime>=:startDate\r\n"
			+ "and createddatetime<=:endDate and organization_id=:orgId", nativeQuery = true)
	List<Integer> getLeadsBasedonEmpNames(@Param(value = "empNamesList") List<String> empNamesList,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") String orgId);
	//Lost Ddrop query ends


	@Query(value="SELECT model FROM dms_lead WHERE crm_universal_id=:unversalId", nativeQuery = true)
	String getModelWithUniversalId(@Param(value = "unversalId") String unversalId);

	@Query(value="SELECT * FROM dms_lead WHERE crm_universal_id=:unversalId", nativeQuery = true)
	DmsLead getDMSLead(@Param(value = "unversalId") String unversalId);
	
	@Query(value="SELECT * FROM dms_lead WHERE organization_id=:orgId and id IN (:leadIdList) AND ((createddatetime>=:startDate and createddatetime<=:endDate) or (modifieddatetime>=:startDate and modifieddatetime<=:endDate)) and lead_stage in ('DROPPED')",nativeQuery = true)
	List<DmsLead> getLeadsByStageandDate(
			@Param(value="orgId") String orgId,
			@Param(value="leadIdList") List<Integer> leadIdList,
			@Param(value="startDate") String startDate,@Param(value="endDate") String endDate
			);
	// ---------------------------------------
	
	@Query(value = "SELECT count(*) FROM dms_lead A, dms_employee E , dms_role R where A.model = :model and A.createddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.allocated = 'Yes' and A.created_by = :loginEmpName "
			+ " and A.createddatetime<=:endDate and A.lead_stage not in ('DROPPED') and A.organization_id=:orgId", nativeQuery = true)
	Integer getAllocatedLeadsCountByModel(@Param(value = "model") String model,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and model=:model and organization_id=:orgId and sales_consultant is not null  and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active')", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsByModel(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") String loggedEmpName );



	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and model=:model and organization_id=:orgId and sales_consultant is not null and branch_id in(:branch_id)  and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active')", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsByModel(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName  and model=:model and organization_id=:orgId and sales_consultant is not null and branch_id in(:branch_id)", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsByModel(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );

	
	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName and model=:model and organization_id=:orgId and sales_consultant is not null  and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active') limit 1", nativeQuery = true)
	List<DmsLead> getLiveLeadsModel(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT * FROM dms_lead where created_by in (:loggedEmpName) and model=:model and organization_id=:orgId and sales_consultant is not null limit 1", nativeQuery = true)
	List<DmsLead> getLiveLeadsModelM(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and createddatetime>=:startDate and createddatetime<=:endDate and model=:model and organization_id=:orgId and sales_consultant is not null  and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active') limit 1", nativeQuery = true)
	List<DmsLead> getLiveLeadsModelWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT * FROM dms_lead where created_by in (:loggedEmpName) and branch_id in(:branch_id) and model=:model and organization_id=:orgId and sales_consultant is not null  limit 1", nativeQuery = true)
	List<DmsLead> getLiveLeadsModelWithFilterM(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and model=:model and organization_id=:orgId and sales_consultant is not null and lead_status in ('INVOICECOMPLETED','PREDELIVERYCOMPLETED')", nativeQuery = true)
	long getLiveLeadsRetailModel(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and model=:model and organization_id=:orgId and sales_consultant is not null  and lead_status in ('INVOICECOMPLETED','PREDELIVERYCOMPLETED')", nativeQuery = true)
	long getLiveLeadsRetailModelM(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id)  and model=:model and organization_id=:orgId and sales_consultant is not null and lead_status in ('INVOICECOMPLETED','PREDELIVERYCOMPLETED')", nativeQuery = true)
	long getLiveLeadsRetailModelWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and branch_id in(:branch_id)  and model=:model and organization_id=:orgId and sales_consultant is not null and lead_status in ('INVOICECOMPLETED','PREDELIVERYCOMPLETED')", nativeQuery = true)
	long getLiveLeadsRetailModelWithFilterM(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and model=:model and organization_id=:orgId and sales_consultant is not null and lead_stage in ('ENQUIRY','PREBOOKING')", nativeQuery = true)
	long getLiveLeadsEnqModel(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and model=:model and organization_id=:orgId  and lead_stage in ('PREENQUIRY')", nativeQuery = true)
	long getLiveLeadsPreEnqModel(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and model=:model and organization_id=:orgId and sales_consultant is not null and lead_stage in ('ENQUIRY','PREBOOKING')", nativeQuery = true)
	long getLiveLeadsEnqModelM(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and model=:model and organization_id=:orgId  and lead_stage in ('PREENQUIRY')", nativeQuery = true)
	long getLiveLeadsPreEnqModelM(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and model=:model and organization_id=:orgId and sales_consultant is not null and lead_stage in ('ENQUIRY','PREBOOKING')", nativeQuery = true)
	long getLiveLeadsEnqModelWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and model=:model and organization_id=:orgId and lead_stage in ('PREENQUIRY')", nativeQuery = true)
	long getLiveLeadsPreEnqModelWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and branch_id in(:branch_id) and model=:model and organization_id=:orgId and sales_consultant is not null  and lead_stage in ('ENQUIRY','PREBOOKING')", nativeQuery = true)
	long getLiveLeadsEnqModelWithFilterM(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and branch_id in(:branch_id) and model=:model and organization_id=:orgId  and lead_stage in ('PREENQUIRY')", nativeQuery = true)
	long getLiveLeadsPreEnqModelWithFilterM(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and model=:model and organization_id=:orgId and sales_consultant is not null  and lead_stage in ('BOOKING','INVOICE') and lead_status in ('PREBOOKINGCOMPLETED','BOOKINGCOMPLETED')", nativeQuery = true)
	long getLiveLeadsBkgModel(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and model=:model and organization_id=:orgId and sales_consultant is not null   and lead_stage in ('BOOKING','INVOICE') and lead_status in ('PREBOOKINGCOMPLETED','BOOKINGCOMPLETED')", nativeQuery = true)
	long getLiveLeadsBkgModelM(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and model=:model and organization_id=:orgId and sales_consultant is not null and lead_stage in ('BOOKING','INVOICE') and lead_status in ('PREBOOKINGCOMPLETED','BOOKINGCOMPLETED')", nativeQuery = true)
	long getLiveLeadsBkgModelWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and branch_id in(:branch_id) and model=:model and organization_id=:orgId and sales_consultant is not null and lead_stage in ('BOOKING','INVOICE') and lead_status in ('PREBOOKINGCOMPLETED','BOOKINGCOMPLETED')", nativeQuery = true)
	long getLiveLeadsBkgModelWithFilterM(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and model=:model and organization_id=:orgId and sales_consultant is not null and lead_stage='DROPPED'", nativeQuery = true)
	long getLiveLeadsLostModel(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and model=:model and organization_id=:orgId and sales_consultant is not null and lead_stage='DROPPED'", nativeQuery = true)
	long getLiveLeadsLostModelWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );

	@Query(value = "SELECT * FROM dms_lead A, dms_branch B, dms_employee E , dms_role R where A.modifieddatetime>=:startDate and A.created_by = :loggedEmpName"
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " A.branch_id = B.branch_id and B.dealer_code = :dealerCode "
			+ " and A.modifieddatetime<=:endDate and A.model=:model and A,organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsByModel(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") String loggedEmpName ,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName and source_of_enquiry=:source and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active')", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	
	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null limit 1", nativeQuery = true)
	List<DmsLead> getLiveLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null limit 1", nativeQuery = true)
	List<DmsLead> getLiveLeadsBySourceWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and lead_stage in ('ENQUIRY','PREBOOKING') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveEnqLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and lead_stage in ('PREENQUIRY') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId ", nativeQuery = true)
	long getLivePreEnqLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and lead_stage in ('ENQUIRY','PREBOOKING') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveEnqLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and lead_stage in ('PREENQUIRY') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId ", nativeQuery = true)
	long getLivePreEnqLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and lead_stage in ('ENQUIRY','PREBOOKING') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null ", nativeQuery = true)
	long getLiveEnqLeadsBySourceWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and lead_stage in ('PREENQUIRY') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId", nativeQuery = true)
	long getLivePreEnqLeadsBySourceWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and branch_id in(:branch_id) and createddatetime>=:startDate and createddatetime<=:endDate and lead_stage in ('ENQUIRY','PREBOOKING') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveEnqLeadsBySourceWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and branch_id in(:branch_id) and lead_stage in ('PREENQUIRY') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId ", nativeQuery = true)
	long getLivePreEnqLeadsBySourceWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and lead_stage in ('BOOKING','INVOICE') and lead_status in ('PREBOOKINGCOMPLETED','BOOKINGCOMPLETED') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveBkgLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and lead_stage in ('BOOKING','INVOICE') and lead_status in ('PREBOOKINGCOMPLETED','BOOKINGCOMPLETED') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveBkgLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and lead_stage in ('BOOKING','INVOICE') and lead_status in ('PREBOOKINGCOMPLETED','BOOKINGCOMPLETED') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveBkgLeadsBySourceWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and branch_id in(:branch_id) and createddatetime>=:startDate and createddatetime<=:endDate and lead_stage in ('BOOKING','INVOICE') and lead_status in ('PREBOOKINGCOMPLETED','BOOKINGCOMPLETED') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveBkgLeadsBySourceWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and lead_status in ('INVOICECOMPLETED','PREDELIVERYCOMPLETED') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveRtlLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and lead_status in ('INVOICECOMPLETED','PREDELIVERYCOMPLETED') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveRtlLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and lead_status in ('INVOICECOMPLETED','PREDELIVERYCOMPLETED') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveRtlLeadsBySourceWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and branch_id in(:branch_id) and createddatetime>=:startDate and createddatetime<=:endDate and lead_status in ('INVOICECOMPLETED','PREDELIVERYCOMPLETED') and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveRtlLeadsBySourceWithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and lead_stage='DROPPED' and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null ", nativeQuery = true)
	long getLiveLostLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and lead_stage='DROPPED' and source_of_enquiry=:source  and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	long getLiveLostLeadsBySourcewithFilter(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );

	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and source_of_enquiry=:source and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active')", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName );

	@Query(value = "SELECT A.* FROM dms_lead A, dms_branch B, dms_employee E , dms_role R where A.modifieddatetime>=:startDate and A.created_by = :loggedEmpName"
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " A.branch_id = B.branch_id and B.dealer_code = :dealerCode "
			+ " and A.modifieddatetime<=:endDate and A.source_of_enquiry=:source and A,organization_id=:orgId and sales_consultant is not null ", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsBySource(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "loggedEmpName") String loggedEmpName ,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT S.name, A.model, A.modifieddatetime, A.first_name as firstName, A.last_name as lastName, A.lead_stage as leadStage, A.sales_consultant as salesConsultant, A.phone " 
			+" FROM dms_lead A, dms_employee E , dms_role R, dms_source_of_enquiries S where A.sales_consultant = :empName and A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.allocated = 'Yes' and A.created_by = :loginEmpName "
			+ " and A.source_of_enquiry = S.id "
			+ " and A.modifieddatetime<=:endDate and A.lead_stage not in ('DROPPED') and A.organization_id=:orgId", nativeQuery = true)
	List<Object[]> getAllocatedLeadsByEmp(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName,
			@Param(value = "roleName") String roleName  );
	
	@Query(value = "SELECT S.name, A.model, A.modifieddatetime, A.first_name , A.last_name , A.lead_stage , A.sales_consultant , A.phone " 
			+ " FROM dms_lead  A , dms_branch B, dms_employee E , dms_role R, dms_source_of_enquiries S  where "
			+ " A.branch_id = B.branch_id and B.dealer_code = :dealerCode"
			+ " and A.sales_consultant = :empName and A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.allocated = 'Yes' and A.created_by = :loginEmpName "
			+ " and A.source_of_enquiry = S.id "
			+ " and A.modifieddatetime<=:endDate and A.lead_stage not in ('DROPPED') and A.organization_id=:orgId", nativeQuery = true)
	List<Object[]> getAllocatedLeadsByEmp(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "loginEmpName") String loginEmpName,
			@Param(value = "roleName") String roleName  
			);
	
	
	@Query(value = "SELECT S.name, A.model, A.modifieddatetime, A.first_name as firstName, A.last_name as lastName, A.lead_stage as leadStage, A.sales_consultant as salesConsultant, A.phone "
			+ " , D.dropped_date "
			+" FROM dms_lead A, dms_employee E , dms_role R, dms_source_of_enquiries S, dms_lead_drop D where A.sales_consultant = :empName and A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and D.lead_id = A.id "
			+ " and A.allocated = 'Yes' and A.created_by = :loginEmpName "
			+ " and A.source_of_enquiry = S.id "
			+ " and A.modifieddatetime<=:endDate and A.lead_stage in ('DROPPED') and A.organization_id=:orgId", nativeQuery = true)
	List<Object[]> getDroppedLeadsByEmp(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName,
			@Param(value = "roleName") String roleName  );
	
	@Query(value = "SELECT S.name, A.model, A.modifieddatetime, A.first_name , A.last_name , A.lead_stage , A.sales_consultant , A.phone " 
			+" , D.dropped_date "
			+ " FROM dms_lead  A , dms_branch B, dms_employee E , dms_role R, dms_source_of_enquiries S , dms_lead_drop D where "
			+ " A.branch_id = B.branch_id and B.dealer_code = :dealerCode "
			+ " and D.lead_id = A.id "
			+ " and A.sales_consultant = :empName and A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.allocated = 'Yes' and A.created_by = :loginEmpName "
			+ " and A.source_of_enquiry = S.id "
			+ " and A.modifieddatetime<=:endDate and A.lead_stage in ('DROPPED') and A.organization_id=:orgId", nativeQuery = true)
	List<Object[]> getDroppedLeadsByEmp(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "loginEmpName") String loginEmpName ,
			@Param(value = "roleName") String roleName 
			);
	
	
/*	@Query(value = "SELECT S.name, A.model, A.createddatetime, A.first_name as firstName, A.last_name as lastName, A.lead_stage as leadStage, A.sales_consultant as salesConsultant, A.phone "
			+ " , D.dropped_date "
			+" FROM dms_lead A, dms_employee E , dms_role R, dms_source_of_enquiries S, dms_lead_drop D where A.createddatetime>=:startDate  "
			+ " and R.role_name = 'Reception'  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and D.lead_id = A.id "
			+ " and A.allocated = 'Yes' and A.created_by = :loginEmpName "
			+ " and A.source_of_enquiry = S.id "
			+ " and D.stage = :stage"
			+ " and A.createddatetime<=:endDate and A.lead_stage in ('DROPPED') and A.organization_id=:orgId", nativeQuery = true)
	List<Object[]> getDroppedLeadsByStage(
			@Param(value = "stage") String stage,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName ); */
	
	@Query(value = "SELECT count(*) FROM dms_lead A, dms_employee E , dms_role R where A.sales_consultant = :empName and A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by in (:loginEmpName) "
			+ " and A.modifieddatetime<=:endDate and A.lead_stage not in ('DROPPED') and A.organization_id=:orgId  and sales_consultant is not null", nativeQuery = true)
	Integer getAllocatedLeadsCountByEmpMan(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") List<String> loginEmpName,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT count(*) FROM dms_lead   where sales_consultant = :empName  and created_by in (:loginEmpName) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage not in ('DROPPED') and organization_id=:orgId  and sales_consultant is not null", nativeQuery = true)
	Integer getAllocatedLeadsCountByEm(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") List<String> loginEmpName);
	
	@Query(value = "SELECT count(*) FROM dms_lead  A , dms_branch B, dms_employee E , dms_role R  where "
			+ " A.branch_id = B.branch_id and B.dealer_code = :dealerCode"
			+ " and A.sales_consultant = :empName and A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by in (:loginEmpName) "
			+ " and A.modifieddatetime<=:endDate and A.lead_stage not in ('DROPPED') and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getAllocatedLeadsCountByEmpMan(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "loginEmpName") List<String> loginEmpName ,
			@Param(value = "roleName") String roleName
			);
	
	@Query(value = "SELECT count(*) FROM dms_lead A, dms_employee E , dms_role R where A.sales_consultant = :empName and A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by in (:loginEmpName)"
			+ "	 and A.modifieddatetime<=:endDate and A.lead_stage in ('DROPPED') and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getDropeedLeadsCountByEmpMan(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") List<String> loginEmpName,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "select count(*)  from dms_lead  where sales_consultant = :empName and organization_id=:orgId and created_by in (:loginEmpName) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage  in ('DROPPED') and sales_consultant is not null", nativeQuery = true)
	Integer getDropeedLeadsCountByEmpMa(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") List<String> loginEmpName);
	
	@Query(value = "SELECT count(*) FROM dms_lead  A , dms_branch B, dms_employee E , dms_role R where "
			+ "	 A.branch_id = B.branch_id and B.dealer_code = :dealerCode AND  A.sales_consultant = :empName and A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by in (:loginEmpName)"
			+ "	 and A.modifieddatetime<=:endDate and A.lead_stage in ('DROPPED') and A.organization_id=:orgId  and sales_consultant is not null", nativeQuery = true)
	Integer getDropeedLeadsCountByEmpMan(@Param(value = "empName") String empName,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "orgId") int orgId,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "loginEmpName") List<String> loginEmpName,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT count(*) FROM dms_lead A, dms_employee E , dms_role R where A.modifieddatetime>=:startDate "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by in (:loginEmpName)"
			+ " and A.modifieddatetime<=:endDate and A.lead_stage not in ('DROPPED') and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getAllocatedLeadsCountMan(@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") List<String> loginEmpName,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT count(*) FROM dms_lead A , dms_branch B , dms_employee E , dms_role R  where A.branch_id = B.branch_id and B.dealer_code = :dealerCode AND A.modifieddatetime>=:startDate "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by in (:loginEmpName)"
			+ " and A.modifieddatetime<=:endDate and A.lead_stage not in ('DROPPED') and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getAllocatedLeadsCountMan(@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "loginEmpName") List<String> loginEmpName,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT count(*) FROM dms_lead A, dms_employee E , dms_role R where A.modifieddatetime>=:startDate "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by in (:loginEmpName)"
			+ " and A.modifieddatetime<=:endDate and A.lead_stage in ('DROPPED') and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getDroppedLeadsCountMan(@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") List<String> loginEmpName,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT count(*) FROM dms_lead  where  created_by in (:loginEmpName)  and lead_stage in ('DROPPED') and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getDroppedLeadsCountMa(@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") List<String> loginEmpName);
	
	@Query(value = "SELECT count(*) FROM dms_lead A , dms_branch B , dms_employee E , dms_role R where A.branch_id = B.branch_id and B.dealer_code = :dealerCode AND A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by in (:loginEmpName)"
			+ " and A.modifieddatetime<=:endDate and A.lead_stage in ('DROPPED') and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getDroppedLeadsCountMan(@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "loginEmpName") List<String> loginEmpName,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "SELECT count(*) FROM dms_lead A, dms_employee E , dms_role R where A.modifieddatetime>=:startDate "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by in (:loginEmpName)"
			+ " and A.modifieddatetime<=:endDate and A.lead_stage=:leadType and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getAllLeadsCountMan(
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "leadType") String leadType,
			@Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") List<String> loginEmpName,
			@Param(value = "roleName") String roleName);
	
	@Query(value = "SELECT count(*) FROM dms_lead A , dms_branch B, dms_employee E , dms_role R "
			+ " where A.branch_id = B.branch_id and B.dealer_code = :dealerCode AND A.modifieddatetime>=:startDate "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by in (:loginEmpName) "
			+ " and A.modifieddatetime<=:endDate and A.lead_stage=:leadType and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getAllLeadsCountMan(
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "leadType") String leadType,
			@Param(value = "orgId") int orgId,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "loginEmpName") List<String> loginEmpName,
			@Param(value = "roleName") String roleName);
	
	@Query(value = "SELECT id FROM dms_lead  where created_by in (:loggedEmpName) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and model=:model and organization_id=:orgId and sales_consultant is not null and lead_stage  not in ('DROPPED')", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsByModelMan(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );


	@Query(value = "SELECT id FROM dms_lead  where created_by in (:loggedEmpName) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and model=:model and organization_id=:orgId and sales_consultant is not null and branch_id in(:branch_id) and lead_stage  not in ('DROPPED')", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsByModelMan(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") List<String> loggedEmpName,
			@Param(value = "branch_id") List<Integer> branch_id);
	
	@Query(value = "SELECT * FROM dms_lead A, dms_branch B, dms_employee E , dms_role R where A.modifieddatetime>=:startDate and A.created_by in (:loggedEmpName)"
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " A.branch_id = B.branch_id and B.dealer_code = :dealerCode "
			+ " and A.modifieddatetime<=:endDate and A.model=:model and A,organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsByModelMan(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") List<String> loggedEmpName ,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "roleName") String roleName );

	@Query(value = "SELECT id FROM dms_lead where created_by in (:loggedEmpName) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and source_of_enquiry=:source and sub_source=:Subsource  and organization_id=:orgId and sales_consultant is not null  and lead_stage  not in ('DROPPED')", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsBySourceMan(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "Subsource") String Subsource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT id FROM dms_lead where created_by in (:loggedEmpName) and source_of_enquiry=:source and sub_source=:Subsource  and organization_id=:orgId and sales_consultant is not null  and lead_stage  not in ('DROPPED') limit 1", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsBySourceManLiveLead(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "Subsource") String Subsource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );

	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and source_of_enquiry=:source and sub_source=:Subsource  and organization_id=:orgId and sales_consultant is not null  and lead_stage in ('DROPPED')", nativeQuery = true)
	Long getAllEmployeeLeadsBySourceManLost(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "Subsource") String Subsource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and source_of_enquiry=:source and sub_source=:Subsource  and organization_id=:orgId and sales_consultant is not null  and lead_stage in ('DROPPED')", nativeQuery = true)
	Long getAllEmployeeLeadsBySourceManLostLiveLead(
			@Param(value = "orgId") String orgId,
			@Param(value = "source") int source,
			@Param(value = "Subsource") String Subsource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );

	@Query(value = "SELECT id FROM dms_lead where created_by in (:loggedEmpName) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and source_of_enquiry=:source and sub_source=:Subsource  and organization_id=:orgId and sales_consultant is not null and branch_id in(:branch_id)  and lead_stage  not in ('DROPPED')", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsBySourceMan(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "Subsource") String Subsource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName,
			@Param(value = "branch_id") List<Integer> branch_id);
	
	@Query(value = "SELECT id FROM dms_lead where created_by in (:loggedEmpName) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and source_of_enquiry=:source and sub_source=:Subsource  and organization_id=:orgId and sales_consultant is not null and branch_id in(:branch_id)  and lead_stage  not in ('DROPPED') limit 1", nativeQuery = true)
	List<Integer> getAllEmployeeLeadsBySourceManLiveLeads(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "Subsource") String Subsource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName,
			@Param(value = "branch_id") List<Integer> branch_id);

	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and source_of_enquiry=:source and sub_source=:Subsource  and organization_id=:orgId and sales_consultant is not null and branch_id in(:branch_id)  and lead_stage in ('DROPPED')", nativeQuery = true)
	Long getAllEmployeeLeadsBySourceManLost(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "Subsource") String Subsource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName,
			@Param(value = "branch_id") List<Integer> branch_id);
	
	@Query(value = "SELECT count(*) FROM dms_lead where created_by in (:loggedEmpName) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and source_of_enquiry=:source and sub_source=:Subsource  and organization_id=:orgId and sales_consultant is not null and branch_id in(:branch_id)  and lead_stage in ('DROPPED')", nativeQuery = true)
	Long getAllEmployeeLeadsBySourceManLostLiveLeads(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "Subsource") String Subsource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName,
			@Param(value = "branch_id") List<Integer> branch_id);
	
	@Query(value = "SELECT A.* FROM dms_lead A, dms_branch B, dms_employee E , dms_role R where A.modifieddatetime>=:startDate and A.created_by = :loggedEmpName"
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " A.branch_id = B.branch_id and B.dealer_code = :dealerCode "
			+ " and A.modifieddatetime<=:endDate and A.source_of_enquiry=:source and A,organization_id=:orgId and sales_consultant is not null ", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsBySourceMan(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "loggedEmpName") List<String> loggedEmpName ,
			@Param(value = "dealerCode") String dealerCode,
			@Param(value = "roleName") String roleName );
	
	@Query(value = "select id from dms_lead  where organization_id=:orgId and created_by in (:emp) and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage not in ('DROPPED')", nativeQuery = true)
	List<Integer> getTotalCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "emp") List<String> emp );
	
	@Query(value = "select id from dms_lead where organization_id=:orgId and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and created_by=:emp and lead_stage  not in ('DROPPED')", nativeQuery = true)
	List<Integer> getTotalCountEmp(
			@Param(value = "orgId") int orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "emp") String emp );

	@Query(value = "select id from dms_lead where organization_id=:orgId and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and created_by=:emp and lead_stage  not in ('DROPPED') and branch_id in(:branch_id)", nativeQuery = true)
	List<Integer> getTotalCountEmp(
			@Param(value = "orgId") int orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "emp") String emp,
			@Param(value = "branch_id") List<Integer> branch_id);

	@Query(value = "select id from dms_lead where organization_id=:orgId and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and created_by=:emp and lead_stage  not in ('DROPPED') and sales_consultant in (select emp_name from dms_employee where org =:orgId and status = 'Active' and emp_id in (:sales_consultant))", nativeQuery = true)
	List<Integer> getTotalCountEmpByEmp(
			@Param(value = "orgId") int orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "emp") String emp,
			@Param(value = "sales_consultant") List<Integer> sales_consultant);
	
	@Query(value = "select id from dms_lead  where organization_id=:orgId and created_by in (:emp) and sales_consultant=:empNme and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage not in ('DROPPED')", nativeQuery = true)
	List<Integer> getEmpLeadCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "empNme") String empNme,
			@Param(value = "emp") List<String> emp);
	
	@Query(value = "select id from dms_lead where organization_id=:orgId and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and sales_consultant=:empNme  and created_by =:emp and lead_stage  not in ('DROPPED')", nativeQuery = true)
	List<Integer> getLeadCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "empNme") String empNme,
			@Param(value = "emp") String emp);

	@Query(value = "select id from dms_lead where organization_id=:orgId and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and sales_consultant=:empNme  and created_by =:emp and lead_stage  not in ('DROPPED') and branch_id in (:branch_id)", nativeQuery = true)
	List<Integer> getLeadCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "empNme") String empNme,
			@Param(value = "emp") String emp,
			@Param(value = "branch_id") List<Integer> branch_id);
	
	@Query(value="select count(id) from dms_lead where created_by=:empName and sales_consultant is null and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage='DROPPED' and organization_id = :organization_id", nativeQuery = true)
	String getTotaContactDrops(@Param(value = "empName") String empName,@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,@Param(value = "organization_id") Integer organization_id);
	
	@Query(value="select count(id) from dms_lead where created_by in (:empName) and sales_consultant is null and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage='DROPPED'", nativeQuery = true)
	String getTotaContactDrops(@Param(value = "empName") List<String> empName,@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate);

	@Query(value="select count(id) from dms_lead where created_by=:empName and sales_consultant is null and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage='DROPPED' and branch_id in (:branch_id)  and organization_id = :organization_id", nativeQuery = true)
	String getTotaContactDrops(@Param(value = "empName") String empName,@Param(value = "startDate") String startDate,
							   @Param(value = "endDate") String endDate,
							   @Param(value = "branch_id") List<Integer> branch_id,
							   @Param(value = "organization_id") Integer organization_id);
	
	@Query(value="select count(*) from dms_lead where created_by=:empName and sales_consultant is null and lead_stage='DROPPED' and branch_id in (:branch_id)  and organization_id = :organization_id", nativeQuery = true)
	long getTotaLiveContactDrops(@Param(value = "empName") String empName,
							   @Param(value = "branch_id") List<Integer> branch_id,
							   @Param(value = "organization_id") Integer organization_id);
	
	@Query(value="select count(*) from dms_lead where created_by=:empName and sales_consultant is null and lead_stage='DROPPED'  and organization_id = :organization_id", nativeQuery = true)
	long getTotaLiveContactDrop(@Param(value = "empName") String empName,
							   @Param(value = "organization_id") Integer organization_id);
	
	@Query(value="select count(*) from dms_lead where created_by=:empName and sales_consultant is null and lead_stage='DROPPED'  and organization_id = :organization_id", nativeQuery = true)
	long getTotaLiveContactDropM(@Param(value = "empName") String empName,
			@Param(value = "organization_id") Integer organization_id);
	
	@Query(value = "select id from dms_lead where organization_id=:orgId and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and sales_consultant=:empNme  and created_by =:emp", nativeQuery = true)
	List<Integer> getLeadDropCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "empNme") String empNme,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and sales_consultant=:empNme  and created_by =:emp", nativeQuery = true)
	int getLiveCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "empNme") String empNme,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and sales_consultant=:empNme  and created_by =:emp and lead_stage in ('ENQUIRY','PREBOOKING')", nativeQuery = true)
	long getEnqLiveCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "empNme") String empNme,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and sales_consultant=:empNme  and created_by =:emp and lead_stage in ('PREENQUIRY')", nativeQuery = true)
	long getPreEnqLiveCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "empNme") String empNme,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and sales_consultant=:empNme  and created_by =:emp and lead_stage in ('BOOKING','INVOICE') and lead_status in ('PREBOOKINGCOMPLETED','BOOKINGCOMPLETED')", nativeQuery = true)
	long getBkgLiveCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "empNme") String empNme,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and created_by =:emp and lead_stage in ('PREENQUIRY')", nativeQuery = true)
	long getTotalPreEnqLiveCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where branch_id in(:branch_id) and organization_id=:orgId and created_by =:emp and lead_stage in ('PREENQUIRY')", nativeQuery = true)
	long getTotalPreEnqLiveCountWithFilter(
			@Param(value = "orgId") int orgId,
			@Param(value = "emp") String emp,
			 @Param(value = "branch_id") List<Integer> branch_id);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and created_by =:emp and lead_stage in ('PREENQUIRY')", nativeQuery = true)
	long getTotalPreEnqLiveCountM(
			@Param(value = "orgId") int orgId,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and created_by =:emp and lead_stage in ('ENQUIRY','PREBOOKING')", nativeQuery = true)
	long getTotalEnqLiveCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where branch_id in(:branch_id) and organization_id=:orgId and created_by =:emp and lead_stage in ('ENQUIRY','PREBOOKING')", nativeQuery = true)
	long getTotalEnqLiveCountWithFilter(
			@Param(value = "orgId") int orgId,
			@Param(value = "emp") String emp,
			 @Param(value = "branch_id") List<Integer> branch_id);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and created_by =:emp and lead_stage in ('ENQUIRY','PREBOOKING')", nativeQuery = true)
	long getTotalEnqLiveCountM(
			@Param(value = "orgId") int orgId,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and created_by =:emp and lead_stage in ('BOOKING','INVOICE') and lead_status in ('PREBOOKINGCOMPLETED','BOOKINGCOMPLETED')", nativeQuery = true)
	long getTotalBkgLiveCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where branch_id in(:branch_id) and organization_id=:orgId and created_by =:emp and lead_stage in ('BOOKING','INVOICE') and lead_status in ('PREBOOKINGCOMPLETED','BOOKINGCOMPLETED')", nativeQuery = true)
	long getTotalBkgLiveCountWithFilter(
			@Param(value = "orgId") int orgId,
			@Param(value = "emp") String emp,
			 @Param(value = "branch_id") List<Integer> branch_id);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and created_by =:emp and lead_stage in ('BOOKING','INVOICE') and lead_status in ('PREBOOKINGCOMPLETED','BOOKINGCOMPLETED')", nativeQuery = true)
	long getTotalBkgLiveCountM(
			@Param(value = "orgId") int orgId,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and created_by =:emp and lead_status in ('INVOICECOMPLETED','PREDELIVERYCOMPLETED')", nativeQuery = true)
	long getTotalRetLiveCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where branch_id in(:branch_id) and organization_id=:orgId and created_by =:emp and lead_status in ('INVOICECOMPLETED','PREDELIVERYCOMPLETED')", nativeQuery = true)
	long getTotalRetLiveCountWithFilter(
			@Param(value = "orgId") int orgId,
			@Param(value = "emp") String emp,
			 @Param(value = "branch_id") List<Integer> branch_id);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and created_by =:emp and lead_status in ('INVOICECOMPLETED','PREDELIVERYCOMPLETED')", nativeQuery = true)
	long getTotalRetLiveCountM(
			@Param(value = "orgId") int orgId,
			@Param(value = "emp") String emp);
	
	@Query(value = "select count(id) from dms_lead where organization_id=:orgId and sales_consultant=:empNme  and created_by =:emp and lead_status in ('INVOICECOMPLETED','PREDELIVERYCOMPLETED')", nativeQuery = true)
	long getReatilLiveCount(
			@Param(value = "orgId") int orgId,
			@Param(value = "empNme") String empNme,
			@Param(value = "emp") String emp);
	
	@Query(value = "SELECT * FROM dms_lead where  created_by = :loginEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage not in ('DROPPED') and organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	List<DmsLead> getAllocatedLeadsCountData(@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
			@Param(value = "loginEmpName") String loginEmpName );
	
	@Query(value = "select id from dms_lead  where organization_id=:orgId and created_by in (:emp) and sales_consultant=:empNme and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate)", nativeQuery = true)
	List<Integer> getEmpLeadCountWithDrop(
			@Param(value = "orgId") int orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "empNme") String empNme,
			@Param(value = "emp") List<String> emp);
			
	@Query(value = "SELECT count(A.id) FROM dms_lead A where A.createddatetime>=:startDate and A.created_by in (:loggedEmpName)"
			+ " and A.createddatetime<=:endDate and A.source_of_enquiry=:source and A.sub_source=:Subsource  and A.organization_id=:orgId and A.sales_consultant is not null and lead_stage in ('DROPPED') ", nativeQuery = true)
	long getAllEmployeeLeadsBySourceManDroppedCount(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "Subsource") String Subsource,
			@Param(value = "loggedEmpName") List<String> loggedEmpName );
	
	@Query(value = "SELECT count(A.id) FROM dms_lead A where A.createddatetime>=:startDate and A.created_by =:loggedEmpName"
			+ " and A.createddatetime<=:endDate and A.source_of_enquiry=:source and A.sub_source=:Subsource  and A.organization_id=:orgId and A.sales_consultant is not null and lead_stage in ('DROPPED') ", nativeQuery = true)
	long getAllEmployeeLeadsBySourcedrop(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "Subsource") String Subsource,
			@Param(value = "loggedEmpName") String loggedEmpName );
	
	@Query(value="SELECT * FROM dms_lead WHERE organization_id=:orgId and id IN (:leadIdList) AND createddatetime>=:startDate and createddatetime<=:endDate",nativeQuery = true)
	List<DmsLead> getLeadsByStageandDate2EventPre(
			@Param(value="orgId") String orgId,
			@Param(value="leadIdList") List<Integer> leadIdList,
			@Param(value="startDate") String startDate,@Param(value="endDate") String endDate);

	@Query(value = "SELECT count(*) FROM dms_lead A , dms_branch B , dms_employee E , dms_role R where A.branch_id = B.branch_id and B.dealer_code in = :dealerCode AND A.modifieddatetime>=:startDate  "
			+ " and R.role_name = :roleName  and R.org_id = A.organization_id and E.org = R.org_id and E.hrms_role = R.role_id and A.created_by = E.emp_name "
			+ " and A.created_by = :loginEmpName"
			+ " and A.modifieddatetime<=:endDate and A.lead_stage in ('DROPPED') and A.organization_id=:orgId and sales_consultant is not null", nativeQuery = true)
	Integer getDroppedLeadsCount1(@Param(value = "startDate") String startDate,
								 @Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
								 @Param(value = "dealerCode") String dealerCode,
								 @Param(value = "loginEmpName") String loginEmpName,
								 @Param(value = "roleName") String roleName );


	@Query(value = "SELECT * FROM dms_lead where  created_by = :loginEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage not in ('DROPPED') and organization_id=:orgId and sales_consultant is not null and branch_id in(:branch_id)", nativeQuery = true)
	List<DmsLead> getAllocatedLeadsCountData(@Param(value = "startDate") String startDate,
											 @Param(value = "endDate") String endDate,
											 @Param(value = "orgId") int orgId,
											 @Param(value = "loginEmpName") String loginEmpName ,
											 @Param(value = "branch_id") List<Integer> branch_id);


	@Query(value="select count(*) from dms_lead where created_by in (:empName) and sales_consultant is null and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage='DROPPED' and branch_id in (:branch_id) and organization_id = :organization_id", nativeQuery = true)
	Integer getTotaContactDrops(@Param(value = "empName") List<String> empName, @Param(value = "startDate") String startDate,
										   @Param(value = "endDate") String endDate,
										   @Param(value = "branch_id") List<Integer> branch_id, @Param(value = "organization_id") Integer organization_id);

	@Query(value="select count(*) from dms_lead where created_by in (:empName) and sales_consultant is null and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage='DROPPED' and organization_id = :organization_id", nativeQuery = true)
	Integer getTotaContactDrops(@Param(value = "empName") List<String> empName,@Param(value = "startDate") String startDate,
										   @Param(value = "endDate") String endDate,
										   @Param(value = "organization_id") Integer organization_id);

	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and model=:model and organization_id=:orgId and sales_consultant is not null  and sales_consultant in (:sales_consultant)", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsByModelSalesConsultant(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "model") String model,
			@Param(value = "loggedEmpName") String loggedEmpName,
			@Param(value = "sales_consultant") List<String> sales_consultant);

	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and model=:model and organization_id=:orgId and sales_consultant is not null and branch_id in(:branch_id)  and sales_consultant in (:sales_consultant)", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsByModelSalesConsultant(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "model") String model,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName,
			@Param(value = "sales_consultant") List<String> sales_consultant );


	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName and source_of_enquiry=:source and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null and sales_consultant in (:sales_consultant)", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsBySourceSalesConsultant(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "loggedEmpName") String loggedEmpName,
			@Param(value = "sales_consultant") List<String> sales_consultant);

	@Query(value = "SELECT * FROM dms_lead where created_by = :loggedEmpName and branch_id in(:branch_id) and source_of_enquiry=:source and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and sub_source=:subSource and organization_id=:orgId and sales_consultant is not null and sales_consultant in (:sales_consultant)", nativeQuery = true)
	List<DmsLead> getAllEmployeeLeadsBySourceSalesConsultant(
			@Param(value = "orgId") String orgId,
			@Param(value = "startDate") String startDate,
			@Param(value = "endDate") String endDate,
			@Param(value = "source") int source,
			@Param(value = "subSource") String subSource,
			@Param(value = "branch_id") List<Integer> branch_id,
			@Param(value = "loggedEmpName") String loggedEmpName ,
			@Param(value = "sales_consultant") List<String> sales_consultant);

	@Query(value = "SELECT * FROM dms_lead where  created_by = :loginEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage not in ('DROPPED') and organization_id=:orgId and branch_id in(:branch_id)", nativeQuery = true)
	List<DmsLead> getAllocatedLeadsCountDataContact(@Param(value = "startDate") String startDate,
											 @Param(value = "endDate") String endDate,
											 @Param(value = "orgId") int orgId,
											 @Param(value = "loginEmpName") String loginEmpName ,
											 @Param(value = "branch_id") List<Integer> branch_id);

	@Query(value = "SELECT * FROM dms_lead where  created_by = :loginEmpName and id in (select lead_id from dms_lead_stage_ref where start_date>=:startDate and start_date<=:endDate) and lead_stage not in ('DROPPED') and organization_id=:orgId", nativeQuery = true)
	List<DmsLead> getAllocatedLeadsCountDataContact(@Param(value = "startDate") String startDate,
											 @Param(value = "endDate") String endDate, @Param(value = "orgId") int orgId,
											 @Param(value = "loginEmpName") String loginEmpName );

}
