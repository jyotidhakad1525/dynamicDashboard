package com.automate.df.entity;


import com.automate.df.model.DmsEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DMSResponse {

    private DmsEntity dmsEntity;
    private boolean isSuccess;
    private boolean isError;
    private String errorMessage = "";
    private HttpStatus httpStatus;
    private String message;
    private int accountId;
    private int contactId;
    private HttpStatus statusCode;
    private int leadAge;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }


}
