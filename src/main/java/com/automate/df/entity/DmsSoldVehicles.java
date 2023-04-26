package com.automate.df.entity;


import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "dms_sold_vehicles")
public class DmsSoldVehicles implements Serializable {

    private static final long serialVersionUID = -2476163317401375224L;
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name")
    private String customer_name;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "mobile_no")
    private String mobile_no;

    @Column(name = "home_no")
    private String home_no;

    @Column(name = "street")
    private String street;

    @Column(name = "aadhar_number")
    private String aadhar_number;

    @Column(name = "aadhar_image")
    private String aadhar_image;

    @Column(name = "village")
    private String village;

    @Column(name = "city")
    private String city;

    @Column(name = "pan_number")
    private String pan_number;

    @Column(name = "pan_image")
    private String pan_image;

    @Column(name = "district")
    private String district;

    @Column(name = "state")
    private String state;

    @Column(name = "sold_price")
    private Double sold_price;

    @Column(name = "sales_consultant_name")
    private String sales_consultant_name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "selling_date")
    private String selling_date;

    @Column(name = "accessories_amount")
    private Double accessories_amount;

    @Column(name = "insurance_premium")
    private Double insurance_premium;

    @Column(name = "finance_company")
    private String finance_company;

    @Column(name = "finance_loan_amount")
    private Double finance_loan_amount;

    @Column(name = "inventory_cost")
    private Double inventory_cost;

    @Column(name = "warranty_charges")
    private Double warranty_charges;

    @Column(name = "rta_charges")
    private Double rta_charges;

    @Column(name = "other_charges")
    private Double other_charges;

    @Column(name = "address_type")
    private String address_type;

    @Column(name = "other_images", columnDefinition = "json")
	@Type(type = "json")
	private List<String> other_images;
}
