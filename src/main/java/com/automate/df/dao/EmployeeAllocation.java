package com.automate.df.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.automate.df.entity.sales.allocation.DmsEmployeeAllocation;

public interface EmployeeAllocation
		extends JpaRepository<DmsEmployeeAllocation, Integer>, JpaSpecificationExecutor<DmsEmployeeAllocation> {

	@Query(value = "select * from dms_employee_allocation where lead_id=?1", nativeQuery = true)
	List<DmsEmployeeAllocation> findByLeadId(int id);

	@Query(value = "select * from dms_employee_allocation where employee_id in (:empId)", nativeQuery = true)
	List<DmsEmployeeAllocation> findByEmployeeIdImmediate(List<Integer> empId);

	List<DmsEmployeeAllocation> findByEmployeeId(int empId);

	@Query(value = "select empa.* from dms_employee_allocation empa, dms_lead dmsl where "
			+ "empa.employee_id=:empId and empa.lead_id=dmsl.id and dmsl.lead_status != :leadStage and dmsl.model in (:vehicleModels)"
			+ "and dmsl.sales_consultant in (:salesConsultants)", nativeQuery = true)
	List<DmsEmployeeAllocation> findByEmployeeIdAndSalesConsultantAndLeadStatusAndLeadModel(int empId, String leadStage,
			List<String> vehicleModels, List<String> salesConsultants);
	
	@Query(value = "select empa.* from dms_employee_allocation empa, dms_lead dmsl, dms_source_of_enquiries dse where "
			+ "empa.employee_id=:empId and empa.lead_id=dmsl.id and dse.id=dmsl.source_of_enquiry and dmsl.lead_status != :leadStage and dmsl.source_of_enquiry=:sourceOfEnquiry "
			+ "and dmsl.sales_consultant in (:salesConsultants)", nativeQuery = true)
	List<DmsEmployeeAllocation> findByEmployeeIdAndSalesConsultantAndLeadStatusAndSourceOfEnquiry(int empId, String leadStage,
			Integer sourceOfEnquiry, List<String> salesConsultants);
	
	@Query(value = "select empa.* from dms_employee_allocation empa, dms_lead dmsl where "
			+ "empa.employee_id=:empId and empa.lead_id=dmsl.id and dmsl.lead_status != :leadStage and dmsl.event_code in (:eventCode)"
			+ "and dmsl.sales_consultant in (:salesConsultants)", nativeQuery = true)
	List<DmsEmployeeAllocation> findByEmployeeIdAndSalesConsultantAndLeadStatusAndEventCode(int empId, String leadStage,
			String eventCode, List<String> salesConsultants);

	List<DmsEmployeeAllocation> findByEmployeeIdIn(List<Integer> empId);

}
