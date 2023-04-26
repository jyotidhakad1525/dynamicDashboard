package com.automate.df.dao.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TargetAchivementEvent {
	
	String paramName;
	String paramShortName;
	String target;
	String achievment;
	String achivementPerc;
	String shortfall;
	String shortFallPerc;
	List<Object> data;
	String event;
	String eventDate;
	BigDecimal budget;

}
