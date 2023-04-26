package com.automate.df.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "all_dropleads_stages")
@NamedQuery(name = "DropStageMenuEntity.findAll", query = "SELECT c FROM DropStageMenuEntity c")
public class DropStageMenuEntity implements Serializable {
	 private static final long serialVersionUID = 4895908114629386216L;
	    @Id
	    @Column(name = "id")
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private int id;
	    
	    @Column(name = "menu")
	    private String menu;
	    
	    @Column(name = "status")
	    private String status;

		public DropStageMenuEntity(int id, String menu, String status) {
			super();
			this.id = id;
			this.menu = menu;
			this.status = status;
		}

		public DropStageMenuEntity() {
			super();
		} 
}
