package com.billermanagement.vo.InstamoneyVO.ValidateEwallet;

import com.billermanagement.vo.InstamoneyVO.Result;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IMValidateEwalletResVO {

    @JsonProperty("status")
    private String status;

    @JsonProperty("ewallet_account_number")
    private String accNumber;

    @JsonProperty("ewallet_type")
    private String accType;

    @JsonProperty("is_found")
    private Boolean isFound;

    @JsonProperty("id")
    private String id;

    @JsonProperty("updated")
    private String updated;

    @JsonProperty("result")
    private Result result;
}
