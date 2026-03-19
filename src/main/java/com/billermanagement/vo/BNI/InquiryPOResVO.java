package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InquiryPOResVO {
    @JsonProperty("poInfoInquiryResponse")
    private POInfoInquiryResponse poInfoInquiryResponse;

    @JsonProperty("Fault")
    private Fault fault;

    @JsonProperty("Response")
    private Response response;
}
