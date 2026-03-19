package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class POInfoInquiryResponse {
    @JsonProperty("paymentInfoList")
    private PaymentInfoList paymentInfoList;
}
