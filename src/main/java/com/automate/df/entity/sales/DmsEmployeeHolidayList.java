package com.automate.df.entity.sales;

import java.io.Serializable;
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
@Table(name = "dms_employee_holiday_list")
@NamedQuery(name = "DmsEmployeeHolidayList.findAll", query = "SELECT c FROM DmsEmployeeHolidayList c")
public class DmsEmployeeHolidayList implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "date")
    private Date date;
    
    @Column(name = "day_name")
    private String dayName;
    
    @Column(name = "weekly_enabled")
    private int weeklyEnabled;
    
    @Column(name = "holiday_name")
    private String holidayName;
    
    @Column(name = "status")
    private String status;
    

}
