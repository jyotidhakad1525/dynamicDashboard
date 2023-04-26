package com.automate.df.entity.oh;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "dms_dept_designation_mapping")
public class DmsDeptDesignationMapping implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int dmsDesignationId;

	@Column(name = "dept_id")
	private String deptId;

	@Column(name = "designation_id")
	private String designationId;
}