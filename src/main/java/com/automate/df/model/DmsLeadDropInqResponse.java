package com.automate.df.model;

import com.automate.df.model.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DmsLeadDropInqResponse extends BaseResponse {

    private Page<DmsLeadDropInqInfo> dmsLeadDropInfos;

}

