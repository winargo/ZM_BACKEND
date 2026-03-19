package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentDetail {
    @JsonProperty("bniReference")
    private String bniReference;

    @JsonProperty("refNumber")
    private String refNumber;

    @JsonProperty("chargesAmount")
    private String chargesAmount;

    @JsonProperty("trxRate")
    private String trxRate;

    @JsonProperty("beneficiaryName")
    private String beneficiaryName;

    @JsonProperty("beneficiaryAddress1")
    private String beneficiaryAddress1;
}
