package com.billermanagement.vo.InstamoneyVO.ValidateBankName;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IMValidateBankNameReqVO {

    @JsonProperty(value = "bank_account_number", required = true)
    private String bankAccNumber;

    @JsonProperty(value = "bank_code", required = true)
    private String bankCode;

    @JsonProperty(value = "given_name", required = true)
    private String givenName;

    @JsonProperty(value = "surname", required = false)
    private String surname;

    @JsonProperty(value = "upper_threshold", required = false)
    private String upperThreshold;

    @JsonProperty(value = "lower_threshold", required = false)
    private String lowerThreshold;
}
