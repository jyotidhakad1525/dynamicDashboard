package com.automate.df.model.salesgap;

import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamAttendanceResponse {
	
	String code;
	String name;
	String parentId;
	Integer order;
	String orgId;
	String branch;
	String designation;
	String docPath;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TeamAttendanceResponse other = (TeamAttendanceResponse) obj;
		return Objects.equals(code, other.code) && Objects.equals(name, other.name);
	}
	@Override
	public int hashCode() {
		return Objects.hash(code, name);
	}
	
	

}
