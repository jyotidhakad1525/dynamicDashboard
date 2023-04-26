package com.automate.df.dao.salesgap;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.automate.df.entity.salesgap.DmsEmployee;

public interface DmsEmployeeRepo extends JpaRepository<DmsEmployee, Integer> {
	
	@Query(value = "SELECT emp_name FROM dms_employee where emp_id in (:eidList)", nativeQuery = true)
	List<String> findEmpNamesById(@Param(value = "eidList") List<Integer> eidList);
	
	@Query(value = "SELECT * FROM dms_employee where emp_id in (:empId)", nativeQuery = true)
	List<DmsEmployee> findByImmediateId(@Param(value = "empId") List<Integer> empId);

	@Query(value = "SELECT emp_name FROM dms_employee where status = 'Active' and org = :org", nativeQuery = true)
	List<String> findEmpNames(int org);

	@Query(value = "SELECT emp_name FROM dms_employee where status = 'Active' and org = :org and emp_id in (select emp_id from emp_location_mapping where branch_id in (:branch) and active ='Y')", nativeQuery = true)
	List<String> findEmpNames(int org,List<Integer> branch);

	@Query(value = "SELECT emp_name FROM dms_employee where status = 'Active' and org = :org and emp_id in (:emp_id)", nativeQuery = true)
	List<String> findEmpNamesById(int org,List<Integer> emp_id);

	
/*	@Query(value = "SELECT * FROM dms_employee where org=:orgId and branch=:branchId", nativeQuery = true)
	List<DmsEmployee> getEmployeesByOrgBranch(@Param(value = "orgId") Integer orgId,
			@Param(value = "branchId") Integer branchId);
*/	
	@Query(value = "SELECT * FROM dms_employee where org=:orgId and branch=:branchId  and hrms_role =:roleId and status = 'Active'", nativeQuery = true)
	List<DmsEmployee> getEmployeesByOrgBranch(@Param(value = "orgId") Integer orgId,
			@Param(value = "branchId") Integer branchId,@Param(value = "roleId") Integer roleId);
	
	
	@Query(value = "SELECT * FROM dms_employee where org=:orgId", nativeQuery = true)
	List<DmsEmployee> getEmployeesByOrg(@Param(value = "orgId") Integer orgId);

	//teamattendance
	@Query(value = "SELECT * FROM dms_employee where org=:orgId and status = 'Active'", nativeQuery = true)
	List<DmsEmployee> getEmployeesTeamByOrg(@Param(value = "orgId") Integer orgId);




	@Query(value = "SELECT * FROM dms_employee where emp_id = :id", nativeQuery = true)
	Optional<DmsEmployee> findEmpById(@Param(value = "id") Integer id);
	
	@Query(value = "SELECT * FROM dms_employee where status='Active' and org=:orgId "
			+ "and emp_id = :empId and primary_department in (SELECT dms_department_id FROM dms_department where hrms_department_id='Sales')", nativeQuery = true)
	Optional<DmsEmployee> findEmpByDeptwithActive(@Param(value = "orgId") String orgId,@Param(value = "empId") Integer empId);
	

	@Query(value = "SELECT emp_id FROM dms_employee where emp_id in (:empNamelist)", nativeQuery = true)
	List<Integer> findEmpIdsByNames(@Param(value = "empNamelist") List<String> empNamelist);


	@Query(value = "SELECT emp_id FROM dms_employee where emp_name =:empName", nativeQuery = true)
	String findEmpIdByName(@Param(value = "empName") String empName);

	@Query(value = "SELECT * FROM dms_employee where reporting_to =:to", nativeQuery = true)
	Optional<DmsEmployee> findByReportingId(@Param(value = "to") String reportingTo);
	
	@Query(value = "SELECT * from dms_employee where primary_department in (SELECT dms_department_id from dms_department where department_name ='Sales') \r\n"
			+ "and status='Active' and reporting_to in (SELECT emp_id FROM salesDataSetup.dms_employee where emp_id=:empId and hrms_role in\r\n"
			+ " (SELECT role_id from dms_role where role_name in ('MD')))", nativeQuery = true)
	List<DmsEmployee> findAllByOrgIdMDRole(@Param(value = "empId") Integer empId);
	
	@Query(value = "SELECT * FROM dms_employee where org =:orgId and hrms_role =:roleId and status ='Active'", nativeQuery = true)
	List<DmsEmployee> findAllByOrgId(@Param(value = "orgId") Integer orgId,@Param(value = "roleId") Integer roleId);

	@Query(value = "SELECT * FROM dms_employee where org =:orgId and hrms_role =:roleId and status ='Active'and branch=:branchId", nativeQuery = true)
	List<DmsEmployee> findAllByOrgIdBranchId(@Param(value = "orgId") Integer orgId,@Param(value = "branchId") Integer branchId,@Param(value = "roleId") Integer roleId);
	
	@Query(value = "SELECT emp_name FROM dms_employee where emp_id=:id ", nativeQuery = true)
	String getEmpName(@Param(value = "id") String id);
	
	@Query(value = "SELECT hrms_role FROM dms_employee where emp_id=:id ", nativeQuery = true)
	Integer getEmpHrmsRole(@Param(value = "id") Integer id);
	
	@Query(value = "SELECT * FROM dms_employee where emp_id in (:empId)", nativeQuery = true)
	List<Integer> dmsEmpimmediateByidQuery(@Param(value = "empId") List<Integer> empId);

	@Query(value = "SELECT reporting_to FROM dms_employee where emp_id=:id ", nativeQuery = true)
	Integer getReportingPersonId(@Param(value = "id") Integer id);

	@Query(value = "SELECT * FROM dms_employee where status='Active' and reporting_to in (:empId) and primary_department in (select dms_department_id from dms_department where department_name='Sales')", nativeQuery = true)
	List<DmsEmployee> findReporte(List<Integer> empId);

	@Query(value = "SELECT * FROM dms_employee where status='Active' and reporting_to=?1 and primary_department in (select dms_department_id from dms_department where department_name='Sales')", nativeQuery = true)
	List<DmsEmployee> findReportee(int empId);
	
	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where reporting_to=?1 and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception','Tele Caller','CRE'))", nativeQuery = true)
	List<String> findemps(int empId);
	
	@Query(value = "SELECT emp_id FROM salesDataSetup.dms_employee where reporting_to=?1 and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Tele Caller','CRE'))", nativeQuery = true)
	List<Integer> findempId(int empId);

//	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where branch in (:branchlist) and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception','Tele Caller','CRE'))", nativeQuery = true)
//	List<String> findempsByBranch(@Param(value = "branchlist") List<Integer> branchlist);


	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where emp_id in (select emp_id from emp_location_mapping where branch_id in (:branchlist) and active ='Y') and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception','Tele Caller','CRE'))", nativeQuery = true)
	List<String> findempsByBranch(@Param(value = "branchlist") List<Integer> branchlist);


	@Query(value = "SELECT * FROM dms_employee where status='Active' and emp_name=?1", nativeQuery = true)
	DmsEmployee findemp(String name);
	
	@Query(value = "SELECT * FROM dms_employee where status='Active' and emp_id=?1", nativeQuery = true)
	DmsEmployee findemp(int id);
	
	@Query(value = "select rolenm.role_name from dms_employee emp,dms_role rolenm where emp.emp_id=?1 and emp.org=rolenm.org_id and emp.hrms_role=rolenm.role_id;", nativeQuery = true)
	String getRoleName(int id);
	
	@Query(value = "SELECT branch_id FROM salesDataSetup.emp_location_mapping where emp_id = ?1 and active = 'Y' ", nativeQuery = true)
	List<Integer> bracnhListByUserId(int id);

	@Query(value = "SELECT emp_id FROM dms_employee where emp_name =:emp_name and org =:org and status = 'Active' ", nativeQuery = true)
	List<Integer> findEmpIdsByNames(@Param(value = "emp_name") String emp_name,
									@Param(value = "org") Integer org);

	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where emp_id =:emp_id)", nativeQuery = true)
	List<String> findemps(@Param(value = "emp_id") List<Integer> emp_id);


	@Query(value = "SELECT * FROM salesDataSetup.dms_employee where org =:org and hrms_role in (select role_id from dms_role where role_name =:role) and emp_id in (select emp_id from emp_location_mapping where branch_id in (:branch_id) and active ='Y')", nativeQuery = true)
	List<DmsEmployee> findemps(@Param(value = "org") Integer org,
							   @Param(value = "branch_id") List<Integer> branch_id,
						  @Param(value = "role") String role);

	@Query(value = "SELECT * FROM dms_employee where emp_name =:emp_name and org =:org and status = 'Active' ", nativeQuery = true)
	List<DmsEmployee> findEmpByNames(@Param(value = "emp_name") String emp_name,
									@Param(value = "org") Integer org);


	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where emp_id in (select emp_id from emp_location_mapping where branch_id in (:branchlist) and active ='Y') and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception','Tele Caller','CRM'))", nativeQuery = true)
	List<String> findempsByBranch1(@Param(value = "branchlist") List<Integer> branchlist);

	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where reporting_to=?1 and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Tele Caller','CRE','CRM'))", nativeQuery = true)
	List<String> findEmpsSalesManager(int empId);
	
	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where reporting_to=?1 and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Tele Caller','CRE'))", nativeQuery = true)
	List<String> findCrmReportees(int empId);
	
	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where reporting_to=:empId and branch in (:branchlist) and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Tele Caller','CRE'))", nativeQuery = true)
	List<String> findCrmReporteesWithBranches(@Param(value = "empId") int empId,@Param(value = "branchlist") Set<Integer> branchlist);
	
	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where reporting_to=?1 and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Tele Caller','CRE'))", nativeQuery = true)
	List<String> findCrmReportees(List<Integer> empId);
	
	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where reporting_to=?1 and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception'))", nativeQuery = true)
	List<String> findCrmReceptionReportees(int empId);
	
	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where reporting_to=?1 and branch=?2 and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception'))", nativeQuery = true)
	List<String> findCrmReceptionReporteesWithFilter(int empId,List<Integer> branches);
	
	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where reporting_to in (:reporting_to) and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception'))", nativeQuery = true)
	List<String> findCrmReceptionReportees(@Param(value = "reporting_to") List<Integer> empId);

	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where reporting_to =:reporting_to and emp_id in (select emp_id from emp_location_mapping where branch_id in (:branchlist) and active ='Y') and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Tele Caller','CRE','CRM'))", nativeQuery = true)
	List<String> findEmpsSalesManagerByBranch(@Param(value = "reporting_to") Integer reporting_to,@Param(value = "branchlist") List<Integer> branchlist);
	
	@Query(value = "SELECT emp_name FROM dms_employee where branch in (:branchlist) and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Tele Caller','CRE')) and reporting_to in (select emp_id from dms_employee where primary_department in (select dms_department_id from dms_department where hrms_department_id='Sales'))", nativeQuery = true)
	List<String> getEmsByBranches(@Param(value = "branchlist") Set<Integer> branchlist);
	
	@Query(value = "SELECT emp_name FROM dms_employee where branch in (:branchlist) and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception')) and reporting_to in (select emp_id from dms_employee where primary_department in (select dms_department_id from dms_department where hrms_department_id='Sales'))", nativeQuery = true)
	List<String> getReceptionEmsByBranches(@Param(value = "branchlist") Set<Integer> branchlist);
	
	@Query(value = "SELECT emp_name FROM dms_employee where branch in (:branchlist) and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception')) and reporting_to !=:empId", nativeQuery = true)
	List<String> getReceptionEmsByBranchesNotReportees(@Param(value = "branchlist") Set<Integer> branchlist,@Param(value = "empId") int empId);
	
	@Query(value = "SELECT emp_name FROM dms_employee where branch in (:branchlist) and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Tele Caller','CRE')) and reporting_to !=:empId", nativeQuery = true)
	List<String> getCrmEmsByBranchesNotReportees(@Param(value = "branchlist") Set<Integer> branchlist,@Param(value = "empId") int empId);
	
	
	@Query(value = "SELECT emp_name FROM dms_employee where branch in (:branchlist) and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('CRM'))", nativeQuery = true)
	List<String> getCrmEmsByBranches(@Param(value = "branchlist") Set<Integer> branchlist);


	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Tele Caller','CRE'))", nativeQuery = true)
	List<String> getCreAndTellecallerList();

	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where org =:org and emp_id in (select emp_id from emp_location_mapping where branch_id in (:branchlist) and active ='Y') and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Tele Caller','CRE'))", nativeQuery = true)
	List<String> findCreAndTellecallerByBranch( @Param(value = "org") Integer org,@Param(value = "branchlist") List<Integer> branchlist);

	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception'))", nativeQuery = true)
	List<String> getReceptionList();

	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where org =:org and emp_id in (select emp_id from emp_location_mapping where branch_id in (:branchlist) and active ='Y') and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception'))", nativeQuery = true)
	List<String> findReceptionByBranch(@Param(value = "org") Integer org,@Param(value = "branchlist") List<Integer> branchlist);


	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where reporting_to=?1 and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Tele Caller','CRE','CRM','Tele caller Manager','Telecaller Manager'))", nativeQuery = true)
	List<String> getCreAndTellecallerListSales(int empId);

	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where org =:org and emp_id in (select emp_id from emp_location_mapping where branch_id in (:branchlist) and active ='Y') and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Tele Caller','CRE','CRM','Tele caller Manager','Telecaller Manager'))", nativeQuery = true)
	List<String> findCreAndTellecallerByBranchSales(@Param(value = "org") Integer org, @Param(value = "branchlist") List<Integer> branchlist);

	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where reporting_to=?1 and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception','Receptionist Manager','Reception Manager'))", nativeQuery = true)
	List<String> getReceptionListSales(int empId);

	@Query(value = "SELECT emp_name FROM salesDataSetup.dms_employee where org =:org and emp_id in (select emp_id from emp_location_mapping where branch_id in (:branchlist) and active ='Y') and status='Active' and hrms_role in (select role_id from dms_role where role_name in ('Reception','Receptionist Manager','Reception Manager'))", nativeQuery = true)
	List<String> findReceptionByBranchSales(@Param(value = "org") Integer org,@Param(value = "branchlist") List<Integer> branchlist);

	
	@Query(value = "SELECT emp_id FROM dms_employee where reporting_to =:to", nativeQuery = true)
	List<Integer> findReportiEmployee(@Param(value = "to") String reportingTo);
}
