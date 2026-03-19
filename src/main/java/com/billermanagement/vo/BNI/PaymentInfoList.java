package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentInfoList {
    @JsonProperty("paymentInfo")
    private PaymentInfo paymentInfo;
}
