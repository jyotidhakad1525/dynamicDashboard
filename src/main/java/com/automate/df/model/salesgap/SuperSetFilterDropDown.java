package com.automate.df.model.salesgap;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class SuperSetFilterDropDown implements Serializable {
   
	List<Integer> user_ids;
	Integer org_id;
}
