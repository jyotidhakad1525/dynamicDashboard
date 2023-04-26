package com.automate.df.model.salesgap;

import java.util.Objects;

import javax.persistence.Column;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamAttendanceResponse3 {
	
	String code;
	String name;
	String parentId;
	Integer order;
	String orgId;
	String branch;
	String designation;
	String docPath;
	String location;
	String branchName;
	long present;
	long notLoggedIn;
	long leave;
	long holiday;
	long wfh;
	long total;
    boolean isLoggedIn;
    String status;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TeamAttendanceResponse3 other = (TeamAttendanceResponse3) obj;
		return Objects.equals(code, other.code) && Objects.equals(name, other.name);
	}
	@Override
	public int hashCode() {
		return Objects.hash(code, name);
	}
	
	

}
