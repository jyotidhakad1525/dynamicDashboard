package com.automate.df.model.df.dashboard;

import java.util.List;

import com.automate.df.dao.dashboard.TargetAchivementEvent;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OverAllTargetAchivementsEvents {
	private List<TargetAchivementEvent> overallTargetAchivementsEvent;
	private List<EmployeeTargetAchievementEvents> employeeTargetAchievements;
}
