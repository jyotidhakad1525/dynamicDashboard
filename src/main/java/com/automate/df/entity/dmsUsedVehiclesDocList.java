package com.automate.df.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "dms_used_vehicles_doc_list")
public class dmsUsedVehiclesDocList {

    private static final long serialVersionUID = -2476163317401375224L;
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer dms_used_vehicles_id;
    private String doc_list;
}
