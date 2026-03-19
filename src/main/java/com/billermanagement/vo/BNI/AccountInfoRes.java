package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccountInfoRes {
    @JsonProperty("bankCode")
    private String bankCode;

    @JsonProperty("accountNumber")
    private String account;

    @JsonProperty("accountStatus")
    private String status;

    @JsonProperty("accountName")
    private String name;
}
