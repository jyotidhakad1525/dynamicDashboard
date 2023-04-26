package com.automate.df.dao.dashboard;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.automate.df.entity.SubSourceId;

public interface SubSourceRepo extends CrudRepository<SubSourceId, Integer> {

	@Query(value = "SELECT id,source_id,sub_source FROM sub_source WHERE org_id =?1", nativeQuery = true)
	 List<SubSourceId> getSources(String orgId);
}
