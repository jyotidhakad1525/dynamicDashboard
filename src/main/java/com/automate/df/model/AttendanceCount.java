package com.automate.df.model;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AttendanceCount {
	
	long present;
	long leave;
	long holidays;
	long wfh;
	long total;
	long notLoggedIn;
	boolean isLogedIn;
	String totalTime;

}
