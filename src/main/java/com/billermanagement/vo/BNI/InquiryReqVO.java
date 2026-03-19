package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InquiryReqVO {
    @JsonProperty("bankCode")
    private String bankCode;

    @JsonProperty("accountNum")
    private String account;

    @JsonProperty("signature")
    private String signature;
}
