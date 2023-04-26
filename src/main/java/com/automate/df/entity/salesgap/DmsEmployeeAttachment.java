package com.automate.df.entity.salesgap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name="dms_employee_attachment")
@Entity
@Data
@NoArgsConstructor
public class DmsEmployeeAttachment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private int id;
	
	@Column(name="branch_id")
	private int branchId;
	
	@Column(name="org_id")
	private int orgId;
	 
	@Column(name="owner_id")
	private int ownerId;
	
	@Column(name="document_path")
	private String documentPath;

}
