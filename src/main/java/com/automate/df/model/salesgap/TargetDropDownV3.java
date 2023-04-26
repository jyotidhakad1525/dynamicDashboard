package com.automate.df.model.salesgap;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class TargetDropDownV3 implements Serializable {
   
	String UserId;
	String name;
	String parentId;
	Integer order;
	String designation;
	Integer OrgId;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TargetDropDownV3 other = (TargetDropDownV3) obj;
		return Objects.equals(UserId, other.UserId) && Objects.equals(name, other.name);
	}
	@Override
	public int hashCode() {
		return Objects.hash(UserId, name);
	}
	
	
	
}
