package com.automate.df.dao.dashboard;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.automate.df.entity.salesgap.DmsEmployeeAttachment;

public interface DmsEmployeeAttachmentsDao extends CrudRepository<DmsEmployeeAttachment, Integer>  {
	
	  @Query(value = "SELECT document_path FROM salesDataSetup.dms_employee_attachment where owner_id in (:empId) and org_id=:orgId", nativeQuery = true)
			String empDocPath(Integer empId,Integer orgId);

}
