package com.automate.df.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.automate.df.entity.sales.employee.DMSEmployee;

public interface EmpRepo extends JpaRepository<DMSEmployee, Integer> {

	@Query(value = "SELECT * FROM dms_employee where status='Active' and emp_name=?1 and org =?2 ", nativeQuery = true)
	DMSEmployee findemp(String name, int org);

//	@Query(value = "SELECT * FROM dms_employee where status='Active' and emp_name=?1", nativeQuery = true)
//	DMSEmployee findemp(String name);
	
	@Query(value = "SELECT * FROM dms_employee where status='Active' and emp_name in (:name)", nativeQuery = true)
	List<DMSEmployee> findemps(@Param(value = "name") List<String> name);
	
	@Query(value = "SELECT * FROM dms_employee where status='Active' and emp_id=?1", nativeQuery = true)
	DMSEmployee findemp(int id);

	//Dealer Ranking
	@Query(value = "SELECT * FROM dms_employee where org =:orgId and hrms_role =:roleId and status ='Active'and branch in (Select branch_id from dms_branch where name =:branchName)", nativeQuery = true)
	List<DMSEmployee> findAllByOrgIdBranchId(@Param(value = "orgId") Integer orgId,
			@Param(value = "branchName") String branchName, @Param(value = "roleId") Integer roleId);

	// Branch Ranking
	@Query(value = "SELECT * FROM dms_employee where org=:orgId and branch in (Select branch_id from dms_branch where name = :branchName) and hrms_role =:roleId and status = 'Active'", nativeQuery = true)
	List<DMSEmployee> getEmployeesByOrgBranchRanking(@Param(value = "orgId") Integer orgId,
			@Param(value = "branchName") String branchName, @Param(value = "roleId") Integer roleId);

	@Query(value = "SELECT * FROM dms_employee where emp_name =:emp_name and org =:org and status = 'Active' ", nativeQuery = true)
	List<DMSEmployee> findEmpByNames(@Param(value = "emp_name") String emp_name,
									 @Param(value = "org") Integer org);

}
