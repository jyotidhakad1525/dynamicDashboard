package com.automate.df.dao.oh;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.automate.df.entity.sales.DmsEmployeeHolidayList;

@Repository
public interface DmsHolidayListDao extends CrudRepository<DmsEmployeeHolidayList, Integer>
{

	@Query(value = "select * from dms_employee_holiday_list where status='Active'", nativeQuery = true)
	List<DmsEmployeeHolidayList> findAllList(int orgId);
	
	@Query(value = "select * from dms_employee_holiday_list where day_name =:dayName", nativeQuery = true)
    List<DmsEmployeeHolidayList> getHolidayListByDay(String dayName);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE dms_employee_holiday_list SET status = 'InActive' WHERE id = :id", nativeQuery = true)
    int getHolidaySoftDelete(int id);
	
	@Query(value = "select * from dms_employee_holiday_list where status='Active' and weekly_enabled = 1", nativeQuery = true)
	List<DmsEmployeeHolidayList> findAllWeeklyList();
	
	@Query(value = "select count(status) from dms_employee_holiday_list where status='Active' and org_id = :orgId and (date between Date(:startDate) and Date(:endDate))", nativeQuery = true)
	long getholidaylist(int orgId,String startDate,String endDate);
	
	@Query(value = "SELECT date FROM salesDataSetup.dms_employee_holiday_list where date >=:startDate and date <=:endDate and org_id =:orgId", nativeQuery = true)
	List<Date> empReportList(int orgId,String startDate,String endDate);
}
