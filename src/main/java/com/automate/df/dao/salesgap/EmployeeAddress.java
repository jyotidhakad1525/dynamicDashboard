package com.automate.df.dao.salesgap;

import com.automate.df.model.AddressNew;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmployeeAddress {

    private AddressNew permanentAddress;

    private AddressNew presentAddress;
}
