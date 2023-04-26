package com.automate.df.model;


import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Organization {

	String orgname;
	String orgdesc;
	List<Address> address;
}
