package com.automate.df.entity.sales;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "dms_employee_attendance")
@NamedQuery(name = "DmsEmployeeAttendanceEntity.findAll", query = "SELECT c FROM DmsEmployeeAttendanceEntity c")
public class DmsEmployeeAttendanceEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "emp_id")
    private int empId;
    
    @Column(name = "branch_id")
    private int branchId;
    
    @Column(name = "org_id")
    private int orgId;
    
    @Column(name = "is_present")
    private int isPresent;
    
    @Column(name = "is_absent")
    private int isAbsent;
    
    @Column(name = "holiday")
    private int holiday;
    
    @Column(name = "wfh")
    private int wfh;
    
    @Column(name = "createdtimestamp")
    private Date createdtimestamp;
    
    @Column(name = "updatedtimestamp")
    private Date updatedtimestamp;
    
    @Column(name = "punch_in")
    private Time punchIn;
    
    @Column(name = "punch_out")
    private Time punchOut;
    
    @Column(name = "working_hours")
    private Time workingHours;
    
    @Column(name = "comments")
    private String comments;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "is_log_out")
    private int isLogOut;
    
    @Column(name = "status")
    private String Status;
    
    @Column(name = "month")
    private String month;
    
    @Column(name = "day_name")
    private String dayName;

}