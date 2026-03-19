package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccountInfoInquiryResponse {
    @JsonProperty("accountInfoRes")
    private AccountInfoRes accountInfoRes;
}
