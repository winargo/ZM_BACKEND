package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InquiryPOReqVO {
    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("refNumber")
    private String refNumber;

    @JsonProperty("trxDate")
    private String trxDate;

    @JsonProperty("signature")
    private String signature;
}
