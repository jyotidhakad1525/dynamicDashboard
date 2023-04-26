package com.automate.df.model.df.dashboard;

import java.util.List;
import java.util.Map;

import com.automate.df.dao.dashboard.TargetAchivementEvent;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeTargetAchievementEvents {
	private String empName;
	private Integer empId;
	private String orgId;
	private String branchId;
	private String roleName;
	private int childCount;
	private List<TargetAchivementEvent> targetAchievements;
	Map<String, Integer> targetAchievementsMap;
}
