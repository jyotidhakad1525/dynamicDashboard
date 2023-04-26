package com.automate.df.dao.oh;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.automate.df.entity.oh.DmsDesignation;
import com.automate.df.entity.sales.employee.DmsDepartment;

public interface DmsDepartmentRepo extends JpaRepository<DmsDepartment, Integer> {
	@Query(value = "SELECT dms_department_id FROM dms_department where org_id=:orgId and branch_id=:branchId and department_name='Sales'", nativeQuery = true)
	List<Integer> findDepartmentForSales(@Param(value = "orgId") Integer orgId,@Param(value = "branchId") Integer branchId);
	@Query(value = "SELECT dms_department_id FROM dms_department where branch_id=:branch_id and department_name=:department_name", nativeQuery = true)
	List<Integer> findDepartmentForDepartmantName(@Param(value = "branch_id") Integer branch_id,@Param(value = "department_name") String department_name);
	
}
