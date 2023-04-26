package com.automate.df.dao.oh;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.automate.df.entity.oh.DmsDeptDesignationMapping;

public interface DmsDeptDesignationMappingRepo extends JpaRepository<DmsDeptDesignationMapping, Integer> {
	@Query(value = "SELECT designation_id FROM dms_dept_designation_mapping where dept_id=:deptId", nativeQuery = true)
	Set<Integer> findDesignationIdByDeptId(@Param(value = "deptId") Integer deptId);
}
