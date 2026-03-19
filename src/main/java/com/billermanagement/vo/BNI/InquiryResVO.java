package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InquiryResVO {
    @JsonProperty("accountInfoInquiryResponse")
    private AccountInfoInquiryResponse accountInfoInquiryResponse;

    @JsonProperty("Fault")
    private Fault fault;

    @JsonProperty("Response")
    private Response response;

}
