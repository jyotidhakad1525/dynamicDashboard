package com.automate.df.dao.oh;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.automate.df.entity.sales.DmsEmployeeAttendanceEntity;

@Repository
public interface DmsEmployeeAttendancedao extends CrudRepository<DmsEmployeeAttendanceEntity, Integer>
{

	List<DmsEmployeeAttendanceEntity> findAll();
	
	@Query(value = "select * from dms_employee_attendance where emp_id = :empId and org_id = :orgId and month = :month", nativeQuery = true)
    List<DmsEmployeeAttendanceEntity> getAllByempIdAndorgId(int empId,int orgId,String month);
	
	@Query(value = "select * from dms_employee_attendance where (Date(createdtimestamp)>= Date(:startDate) and Date(updatedtimestamp)<= Date(:endDate)) and emp_id= :empId and org_id= :orgId", nativeQuery = true)
	List<DmsEmployeeAttendanceEntity> getAllByDate(int empId, int orgId,String startDate, String endDate);
	
	@Query(value = "SELECT dayname(:date)", nativeQuery = true)
	String getDayName(String date);
	
	@Query(value = "select timediff(Date(:punchOut),Date(:punchIn)) from  dms_employee_attendance where Date(createdtimestamp) = CURDate() and emp_id=:empId", nativeQuery = true)
	Time getWorkingHours(Date punchOut , Date punchIn, int empId);
	
	@Query(value = "Select * from dms_employee_attendance where emp_id=?1 and org_id=?2 and month=?3", nativeQuery = true)
	List<DmsEmployeeAttendanceEntity> getEmpWise(int empId,int orgId,String month);
	
	@Query(value = "Select * from dms_employee_attendance where emp_id=:empId and org_id=:orgId and(Date(createdtimestamp) between Date(:startDate) and Date(:endDate))", nativeQuery = true)
	List<DmsEmployeeAttendanceEntity> getEmpWisefilter(int empId,int orgId,String startDate,String endDate);
	
	@Query(value = "select * from dms_employee_attendance where (Date(createdtimestamp) >= Date(:startDate) and Date(updatedtimestamp) <= Date(:endDate)) and org_id= :orgId", nativeQuery = true)
	List<DmsEmployeeAttendanceEntity> attendenceReport(int orgId,String startDate, String endDate);
	
	@Query(value = "select Date (createdtimestamp) from dms_employee_attendance where  (Date(createdtimestamp) >= Date(:startDate) and Date(updatedtimestamp) <= Date(:endDate)) and org_id= :orgId", nativeQuery = true)
	Date ExcelDates(int orgId,String startDate, String endDate);
	
	@Query(value = "select * from dms_employee_attendance where Date(createdtimestamp) =:date and emp_id=:empId", nativeQuery = true)
	DmsEmployeeAttendanceEntity ExcelPresentWFH(int empId,String date);
	
	@Query(value = "Select emp_id from dms_employee_attendance where org_id=:orgId and (Date(createdtimestamp) >= Date(:startDate) and Date(updatedtimestamp) <= Date(:endDate)) ", nativeQuery = true)
	Set<Integer> empIdList(int orgId,String startDate, String endDate);
	
	@Query(value = "SELECT count(id) FROM dms_employee_attendance where emp_id=:empId and Date(createdtimestamp)=curdate()", nativeQuery = true)
	Long getEmpNotLoggedIn(int empId);

}
