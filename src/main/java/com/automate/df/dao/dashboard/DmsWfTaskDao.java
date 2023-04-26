package com.automate.df.dao.dashboard;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.automate.df.entity.dashboard.DmsWFTask;
import com.automate.df.model.oh.TodaysTaskRes2;

/**
 * 
 * @author sruja
 *
 */
public interface DmsWfTaskDao extends JpaRepository<DmsWFTask, Integer> {

	@Query(value = "SELECT * FROM dms_workflow_task where assignee_id=:assigneeId and \r\n"
			+ " task_created_time >= :startTime and task_created_time <= :endTime", nativeQuery = true)
	List<DmsWFTask> getWfTaskByAssigneeId(@Param(value = "assigneeId") String assigneeId,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task where assignee_id in (:assigneeIdList) and  ((task_created_time >= :startTime and task_created_time <= :endTime) OR (task_updated_time >= :startTime and task_updated_time <= :endTime))and task_status='CLOSED'", nativeQuery = true)
	List<DmsWFTask> getWfTaskByAssigneeIdList(
			@Param(value = "assigneeIdList") List<Integer> assigneeIdList,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task where assignee_id in (:assigneeIdList) and ((task_created_time >= :startTime and task_created_time <= :endTime) OR (task_updated_time >= :startTime and task_updated_time <= :endTime)) and universal_id in (:universalIdList)", nativeQuery = true)
	List<DmsWFTask> getWfTaskByAssigneeIdListByModel(
			@Param(value = "assigneeIdList") List<Integer> assigneeIdList,
			@Param(value = "universalIdList") List<String> universalIdList,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task where assignee_id in (:assigneeIdList) and entity_id in (select id from dms_lead where model=:model) and ((task_created_time >= :startTime and task_created_time <= :endTime) OR (task_updated_time >= :startTime and task_updated_time <= :endTime))", nativeQuery = true)
	List<DmsWFTask> getWfTaskByAssigneeIdListByModelTDHV(
			@Param(value = "assigneeIdList") List<Integer> assigneeIdList,
			@Param(value = "startTime") String startTime, @Param(value = "model") String model,
			@Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task where assignee_id in (:assigneeIdList) and entity_id in (select id from dms_lead where source_of_enquiry=:v) and ((task_created_time >= :startTime and task_created_time <= :endTime) OR (task_updated_time >= :startTime and task_updated_time <= :endTime))", nativeQuery = true)
	List<DmsWFTask> getWfTaskByAssigneeIdListByModelTDHVSource(
			@Param(value = "assigneeIdList") List<Integer> assigneeIdList,
			@Param(value = "startTime") String startTime, @Param(value = "v") int v,
			@Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task where "
			+ " task_created_time >= :startTime and task_created_time <= :endTime and universal_id in (:universalIdList) and task_status IS NOT NULL", nativeQuery = true)
	List<DmsWFTask> getWfTaskByUniversalId(
			@Param(value = "universalIdList") List<String> universalIdList,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

	// or DATE(`task_created_time`) = CURDATE() or DATE(task_expected_end_time) =
	// CURDATE()
	@Query(value = "SELECT * FROM dms_workflow_task where assignee_id = :assigneeId and task_status != 'CLOSED' and task_status != 'CANCELLED' and task_status != 'RESCHEDULED' and ((Date(task_updated_time)>= :startTime   and Date (task_updated_time) <= :endTime ) or (DATE(task_created_time) = CURDATE())) order by task_updated_time desc", nativeQuery = true)
	List<DmsWFTask> getTodaysUpcomingTasks(@Param(value = "assigneeId") Integer assigneeId,
			// @Param(value = "universalIdList") List<String> universalIdList,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task INNER JOIN dms_lead ON (dms_lead.crm_universal_id = dms_workflow_task.universal_id) INNER JOIN dms_branch ON (dms_lead.branch_id = dms_branch.branch_id) where assignee_id =:assigneeId \r\n"
			+ "	and task_status != 'CANCELLED'	and task_status != 'CLOSED' \r\n"
			+ "	and task_status != 'RESCHEDULED' \r\n"
			+ "	and task_created_time>= :startTime  and task_created_time <= :endTime and dms_branch.dealer_code in (:branch_id) order by "
			+ " task_updated_time desc", nativeQuery = true)
	List<DmsWFTask> getTodaysUpcomingTasksWithDateAndDealer(
			@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime,
			@Param(value = "branch_id") List<String> branch_id);

	@Query(value = "SELECT * FROM dms_workflow_task INNER JOIN dms_lead ON (dms_lead.crm_universal_id = dms_workflow_task.universal_id) INNER JOIN dms_branch ON (dms_lead.branch_id = dms_branch.branch_id) where assignee_id =:assigneeId \r\n"
			+ "	and task_status != 'CANCELLED' and task_status != 'CLOSED' \r\n"
			+ "	and task_status != 'RESCHEDULED' \r\n"
			+ "	and task_created_time>= :startTime  and task_created_time <= :endTime and dms_branch.dealer_code in (:branch_id)  order by "
			+ " task_updated_time desc", nativeQuery = true)
	List<DmsWFTask> getTodaysUpcomingTasksWithDealer(
			@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime,
			@Param(value = "branch_id") List<String> branch_id);

	@Query(value = "SELECT * FROM dms_workflow_task where assignee_id =:assigneeId \r\n"
			+ "	and task_status != 'CLOSED' and  task_status != 'CANCELLED' \r\n"
			+ "	and task_created_time>= :startTime  order by "
			+ " task_created_time desc", nativeQuery = true)
	List<DmsWFTask> getTodaysUpcomingTasksV2(@Param(value = "assigneeId") Integer assigneeId,
			// @Param(value = "universalIdList") List<String> universalIdList,
			@Param(value = "startTime") String startTime);

	@Query(value = "SELECT * FROM dms_workflow_task where assignee_id =:assigneeId \r\n"
			+ "	and task_status != 'CLOSED' \r\n"
			+ "	and task_created_time<= :startTime", nativeQuery = true)
	List<DmsWFTask> getTodaysUpcomingTasksV3(@Param(value = "assigneeId") Integer assigneeId,
			// @Param(value = "universalIdList") List<String> universalIdList,
			@Param(value = "startTime") String startTime);

	@Query(value = "SELECT * FROM dms_workflow_task where dms_workflow_task.assignee_id=?1 and  DATE"
			+ "(`task_updated_time`) != CURDATE() and dms_workflow_task.task_status != 'CLOSED' and dms_workflow_task.task_status != 'CANCELLED' and dms_workflow_task.task_status != 'CANCELLED' and dms_workflow_task.task_status != 'RESCHEDULED'  and dms_workflow_task.task_name NOT IN ('Proceed to Pre Booking','Proceed to Booking','Proceed to Invoice','Proceed to Predelivery','Proceed to Delivery') order by "
			+ "task_created_time desc", nativeQuery = true)
	List<DmsWFTask> findAllByPendingStatus(String empId);

	@Query(value = "SELECT * FROM dms_workflow_task where assignee_id =:assigneeId \r\n"
			+ "	and task_status != 'CLOSED' and task_status != 'CANCELLED' \r\n"
			+ "	and task_updated_time>= :startTime  and task_updated_time<=:endTime and task_name NOT IN ('Proceed to Pre Booking','Proceed to Booking','Proceed to Invoice','Proceed to Predelivery','Proceed to Delivery')", nativeQuery = true)

	List<DmsWFTask> findAllByPendingData(@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task where dms_workflow_task.assignee_id=?1 and dms_workflow_task.task_status != 'ASSIGNED'and dms_workflow_task.task_status != 'CLOSED'and dms_workflow_task.task_status != 'IN_PROGRESS' and dms_workflow_task.task_status != 'CANCELLED'and dms_workflow_task.task_status != 'SYSTEM_ERROR' and dms_workflow_task.task_status != 'SENT_FOR_APPROVAL' and dms_workflow_task.task_status != 'APPROVED' order by "
			+ " task_created_time desc", nativeQuery = true)
	List<DmsWFTask> findAllByRescheduledStatusWithNoDate(String empId);

	@Query(value = "SELECT * FROM dms_workflow_task where dms_workflow_task.assignee_id=:assigneeId and task_updated_time>= :startTime  and task_updated_time<=:endTime"
			+ " and dms_workflow_task.task_status != 'ASSIGNED'and dms_workflow_task.task_status != 'CLOSED'and dms_workflow_task.task_status != 'IN_PROGRESS' and dms_workflow_task.task_status != 'CANCELLED'and dms_workflow_task.task_status != 'SYSTEM_ERROR' and dms_workflow_task.task_status != 'SENT_FOR_APPROVAL' and dms_workflow_task.task_status != 'APPROVED' order by task_created_time desc", nativeQuery = true)
	List<DmsWFTask> findAllByRescheduledStatus(@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task where dms_workflow_task.assignee_id=?1 and dms_workflow_task.task_status not in ('ASSIGNED','IN_PROGRESS','SYSTEM_ERROR','SENT_FOR_APPROVAL','APPROVED') and dms_workflow_task.task_status in ('CLOSED','CANCELLED') order by  task_created_time desc", nativeQuery = true)
	List<DmsWFTask> findAllByCompletedStatusWithNoDate(String empId);

	@Query(value = "SELECT * FROM dms_workflow_task where dms_workflow_task.assignee_id=:assigneeId and task_updated_time>= :startTime  and task_updated_time<=:endTime"
			+ " and dms_workflow_task.task_status not in ('ASSIGNED','IN_PROGRESS','SYSTEM_ERROR','SENT_FOR_APPROVAL','APPROVED') and dms_workflow_task.task_status in ('CLOSED','CANCELLED') order by  task_created_time desc", nativeQuery = true)
	List<DmsWFTask> findAllByCompletedStatus(@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task INNER JOIN dms_lead ON (dms_lead.crm_universal_id = dms_workflow_task.universal_id) INNER JOIN dms_branch ON (dms_lead.branch_id = dms_branch.branch_id) where dms_workflow_task.assignee_id=:assigneeId and task_created_time>= :startTime  and task_created_time<=:endTime and dms_workflow_task.task_status not in ('ASSIGNED','IN_PROGRESS','SYSTEM_ERROR','SENT_FOR_APPROVAL','APPROVED') and dms_workflow_task.task_status in ('CLOSED','CANCELLED') and dms_branch.dealer_code in (:branch_id) order by task_created_time desc", nativeQuery = true)
	List<DmsWFTask> findAllByCompletedStatusFilterWithDateAndDealer(
			@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime,
			@Param(value = "branch_id") List<String> branch_id);

	@Query(value = "SELECT * FROM dms_workflow_task INNER JOIN dms_lead ON (dms_lead.crm_universal_id = dms_workflow_task.universal_id) INNER JOIN dms_branch ON (dms_lead.branch_id = dms_branch.branch_id) where dms_workflow_task.assignee_id=:assigneeId and task_created_time>= :startTime  and task_created_time<=:endTime"
			+ " and dms_workflow_task.task_status != 'ASSIGNED' and dms_workflow_task.task_status != 'CLOSED' and dms_workflow_task.task_status != 'IN_PROGRESS' and dms_workflow_task.task_status != 'CANCELLED' and dms_workflow_task.task_status != 'SYSTEM_ERROR' and dms_workflow_task.task_status != 'SENT_FOR_APPROVAL' and dms_workflow_task.task_status != 'APPROVED' and dms_branch.dealer_code in (:branch_id) order by "
			+ " task_created_time desc", nativeQuery = true)
	List<DmsWFTask> findAllByRescheduledStatusFilterWithDateAndDealer(
			@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime,
			@Param(value = "branch_id") List<String> branch_id);

	@Query(value = "SELECT * FROM dms_workflow_task INNER JOIN dms_lead ON (dms_lead.crm_universal_id = dms_workflow_task.universal_id) INNER JOIN dms_branch ON (dms_lead.branch_id = dms_branch.branch_id) where assignee_id =:assigneeId \r\n"
			+ "	and task_status != 'CLOSED' and task_status != 'CANCELLED' and dms_workflow_task.task_status != 'RESCHEDULED' and  DATE(`task_updated_time`) != CURDATE()  \r\n"
			+ "	and task_created_time>= :startTime  and task_created_time<=:endTime and task_name NOT IN ('Proceed to Pre Booking','Proceed to Booking','Proceed to Invoice','Proceed to Predelivery','Proceed to Delivery') and dms_branch.dealer_code in (:branch_id)", nativeQuery = true)
	List<DmsWFTask> findAllByPendingStatusFilterWithDateAndDealer(
			@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime,
			@Param(value = "branch_id") List<String> branch_id);

	@Query(value = "SELECT * FROM dms_workflow_task INNER JOIN dms_lead ON (dms_lead.crm_universal_id = dms_workflow_task.universal_id) INNER JOIN dms_branch ON (dms_lead.branch_id = dms_branch.branch_id) where assignee_id =:assigneeId \r\n"
			+ " and task_status != 'CANCELLED' and task_status != 'CLOSED' and dms_workflow_task.task_status != 'RESCHEDULED' and  DATE(`task_updated_time`) != CURDATE()  \r\n"
			+ "	and task_name NOT IN ('Proceed to Pre Booking','Proceed to Booking','Proceed to Invoice','Proceed to Predelivery','Proceed to Delivery') and dms_branch.dealer_code in (:branch_id)", nativeQuery = true)
	List<DmsWFTask> findAllByPendingStatusFilterWithDealer(
			@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "branch_id") List<String> branch_id);

	@Query(value = "SELECT * FROM dms_workflow_task INNER JOIN dms_lead ON (dms_lead.crm_universal_id = dms_workflow_task.universal_id) INNER JOIN dms_branch ON (dms_lead.branch_id = dms_branch.branch_id) where dms_workflow_task.assignee_id=:assigneeId and dms_workflow_task.task_status not in ('ASSIGNED','IN_PROGRESS','SYSTEM_ERROR','SENT_FOR_APPROVAL','APPROVED') and dms_workflow_task.task_status in ('CLOSED','CANCELLED') and dms_branch.dealer_code in (:branch_id) order by  task_created_time desc", nativeQuery = true)
	List<DmsWFTask> findAllByCompletedStatusFilterWithDealer(
			@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "branch_id") List<String> branch_id);

	@Query(value = "SELECT * FROM dms_workflow_task INNER JOIN dms_lead ON (dms_lead.crm_universal_id = dms_workflow_task.universal_id) INNER JOIN dms_branch ON (dms_lead.branch_id = dms_branch.branch_id) where dms_workflow_task.assignee_id=:assigneeId "
			+ " and dms_workflow_task.task_status != 'ASSIGNED' and dms_workflow_task.task_status != 'CLOSED' and dms_workflow_task.task_status != 'IN_PROGRESS' and dms_workflow_task.task_status != 'CANCELLED' and dms_workflow_task.task_status != 'SYSTEM_ERROR' and dms_workflow_task.task_status != 'SENT_FOR_APPROVAL' and dms_workflow_task.task_status != 'APPROVED' and dms_branch.dealer_code in (:branch_id) order by "
			+ " task_created_time desc", nativeQuery = true)
	List<DmsWFTask> findAllByRescheduledStatusFilterWithDealer(
			@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "branch_id") List<String> branch_id);

	@Query(value = "SELECT * FROM dms_workflow_task  where universal_id=:universalId and task_name=:taskName", nativeQuery = true)
	List<DmsWFTask> getWfTaskByUniversalIdandTask(
			@Param(value = "universalId") String crmUniversalId,
			@Param(value = "taskName") String hOME_VISIT);

	@Query(value = "SELECT * FROM dms_workflow_task where task_name=:taskName and \r\n"
			+ " task_created_time >= :startTime and task_created_time <= :endTime", nativeQuery = true)
	List<DmsWFTask> getWfTaskByTaskName(@Param(value = "taskName") String taskName,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task where dms_workflow_task.assignee_id=:assigneeId and task_created_time>= :startTime  and task_created_time<=:endTime"
			+ " and dms_workflow_task.task_status != 'ASSIGNED'and dms_workflow_task.task_status != 'CLOSED'and dms_workflow_task.task_status != 'IN_PROGRESS' and dms_workflow_task.task_status != 'CANCELLED'and dms_workflow_task.task_status != 'SYSTEM_ERROR' and dms_workflow_task.task_status != 'SENT_FOR_APPROVAL' and dms_workflow_task.task_status != 'APPROVED' order by "
			+ " task_created_time desc", nativeQuery = true)
	List<DmsWFTask> findAllByRescheduledStatus1(@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task where dms_workflow_task.assignee_id=:assigneeId and task_created_time>= :startTime  and task_created_time<=:endTime and dms_workflow_task.task_status not in ('ASSIGNED','IN_PROGRESS','SYSTEM_ERROR','SENT_FOR_APPROVAL','APPROVED') and dms_workflow_task.task_status in ('CLOSED','CANCELLED') order by task_created_time desc", nativeQuery = true)
	List<DmsWFTask> findAllByCompletedStatus1(@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task where assignee_id =:assigneeId \r\n"
			+ "	and task_status != 'CLOSED' and task_status != 'CANCELLED' and dms_workflow_task.task_status != 'RESCHEDULED' and DATE(`task_updated_time`) != CURDATE()  \r\n"
			+ "	and task_created_time>= :startTime  and task_created_time<=:endTime and task_name NOT IN ('Proceed to Pre Booking','Proceed to Booking','Proceed to Invoice','Proceed to Predelivery','Proceed to Delivery')", nativeQuery = true)

	List<DmsWFTask> findAllByPendingData1(@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

	@Query(value = "SELECT * FROM dms_workflow_task where task_id in (:task_id)", nativeQuery = true)
	List<DmsWFTask> findTaskByIds(@Param(value = "task_id") List<Integer> task_id);
	
	@Query(value = " SELECT task_name as taskName, count(*) as taskCount FROM dms_workflow_task INNER JOIN dms_lead ON (dms_lead.crm_universal_id = dms_workflow_task.universal_id) "
			+ "	INNER JOIN dms_branch ON (dms_lead.branch_id = dms_branch.branch_id) "
			+ " where "
			+ " assignee_id in (:assigneeId) "
			+ "	and task_status NOT in (:task_status_not_in) "
			+ "	and (\"(:task_status_in)\" is null or task_status in (:task_status_in)) "
			+ "	and (\"(:task_name_not_in)\" is null or task_name  NOT in (:task_name_not_in)) "
			+ "	and (:current_date_not_inCheck is null or DATE(`task_updated_time`) != CURDATE()) "
			+ "	and (:startTime is null or task_created_time >= :startTime) "
			+ "	and (:endTime is null or task_created_time >= :endTime) "
			+ "	and (\"(:branch_id)\" is null or dms_branch.dealer_code in (:branch_id)) "
			+ "	group by task_name", nativeQuery = true)
	List<TodaysTaskRes2> findCountByStatusAndAssigneeAndCreatedTimeAndDealerCode(
			@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "task_status_not_in") List<String> task_status_not_in,
			@Param(value = "task_status_in") List<String> task_status_in,
			@Param(value = "task_name_not_in") List<String> task_name_not_in,
			@Param(value = "startTime") String startTime, 
			@Param(value = "endTime") String endTime,
			@Param(value = "branch_id") List<String> branch_id,
			@Param(value = "current_date_not_inCheck") String current_date_not_inCheck);
	
	@Query(value = " SELECT * FROM dms_workflow_task INNER JOIN dms_lead ON (dms_lead.crm_universal_id = dms_workflow_task.universal_id) "
			+ "	INNER JOIN dms_branch ON (dms_lead.branch_id = dms_branch.branch_id) "
			+ " where "
			+ " assignee_id in (:assigneeId) "
			+ "	and task_status NOT in (:task_status_not_in) "
			+ "	and (\"(:task_status_in)\" is null or task_status in (:task_status_in)) "
			+ "	and (\"(:task_name_not_in)\" is null or task_name  NOT in (:task_name_not_in)) "
			+ "	and (:current_date_not_inCheck is null or DATE(`task_updated_time`) != CURDATE()) "
			+ "	and (:startTime is null or task_created_time >= :startTime) "
			+ "	and (:endTime is null or task_created_time >= :endTime) "
			+ "	and (\"(:branch_id)\" is null or dms_branch.dealer_code in (:branch_id)) "
			+ "	order by task_updated_time desc", nativeQuery = true)
	List<DmsWFTask> findAllWorkFlowByFilter(
			@Param(value = "assigneeId") Integer assigneeId,
			@Param(value = "task_status_not_in") List<String> task_status_not_in,
			@Param(value = "task_status_in") List<String> task_status_in,
			@Param(value = "task_name_not_in") List<String> task_name_not_in,
			@Param(value = "startTime") String startTime, 
			@Param(value = "endTime") String endTime,
			@Param(value = "branch_id") List<String> branch_id,
			@Param(value = "current_date_not_inCheck") String current_date_not_inCheck);
}
