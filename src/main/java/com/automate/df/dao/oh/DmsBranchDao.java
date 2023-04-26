package com.automate.df.dao.oh;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.automate.df.entity.oh.DmsBranch;

public interface DmsBranchDao extends JpaRepository<DmsBranch, Integer>{
	@Modifying
	@Transactional 
	@Query(value="update dms_branch set active='N' where org_map_id=:id",nativeQuery = true)
	void removeBranchMapping(	@Param(value = "id") Integer id);
	

	@Query(value="select * FROM dms_branch where org_map_id=:id",nativeQuery = true)
	DmsBranch getBranchByOrgMpId(@Param(value = "id") Integer id);
	
	@Query(value="select * FROM dms_branch where org_map_id=:id",nativeQuery = true)
	DmsBranch getBranchTeamByOrgMpId(@Param(value = "id") Integer id);
	
	
	@Modifying
	@Transactional 
	@Query(value="update dms_branch set name=:name where org_map_id=:id",nativeQuery = true)
	void updateBranchName(	@Param(value = "id") Integer id,
			@Param(value = "name") String name);

	@Query(value="select name FROM dms_branch where org_map_id in (:locationNodeList)",nativeQuery = true)
	List<String> findBrancheNamesByIds(	@Param(value = "locationNodeList") Set<Integer> locationNodeList);

	DmsBranch findByName(String name);

	@Query(value = "select branch_id from dms_branch where dealer_code in (:dealer_code)", nativeQuery = true)
	List<Integer> getDealerCode(@Param(value="dealer_code") List<String> dealer_code);
	
	@Query(value = "select branch_id from dms_branch where org_map_id in (:dealer_code)", nativeQuery = true)
	List<Integer> getDealerCodes(@Param(value="dealer_code") List<Integer> dealer_code);
	
	@Query(value = "select dealer_code from dms_branch where branch_id in (:branch_id)", nativeQuery = true)
	List<String> getDealerCodeName(@Param(value="branch_id") List<Integer> branch_id);

}
