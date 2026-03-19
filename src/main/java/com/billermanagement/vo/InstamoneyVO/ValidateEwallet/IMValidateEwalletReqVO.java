package com.billermanagement.vo.InstamoneyVO.ValidateEwallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IMValidateEwalletReqVO {

    @JsonProperty(value = "ewallet_account_number", required = true)
    private String accNumber;

    @JsonProperty(value = "ewallet_type", required = true)
    private String accType;
}
