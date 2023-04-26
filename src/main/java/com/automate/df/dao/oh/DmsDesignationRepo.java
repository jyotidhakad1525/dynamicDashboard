package com.automate.df.dao.oh;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.automate.df.entity.oh.DmsDesignation;

public interface DmsDesignationRepo extends JpaRepository<DmsDesignation, Integer> {
	@Query(value = "SELECT * FROM dms_designation where org_id=:orgId", nativeQuery = true)
	List<DmsDesignation> findByOrg_id(@Param(value = "orgId") Integer orgId);
	
	@Query(value = "SELECT * FROM dms_designation where org_id=:orgId", nativeQuery = true)
	List<DmsDesignation> findByTeamOrg_id(@Param(value = "orgId") Integer orgId);
	
	@Query(value = "SELECT dms_designation_id FROM dms_designation where org_id=:orgId and designation_name=:designation_name", nativeQuery = true)
	List<Integer> findIdByDesignationName(@Param(value = "orgId") Integer orgId,@Param(value = "designation_name") String designation_name);
}
