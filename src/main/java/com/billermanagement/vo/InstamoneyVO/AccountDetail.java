package com.billermanagement.vo.InstamoneyVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccountDetail {
    @JsonProperty("account_code")
    private String accountCode;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_holder_name")
    private String accountHolderName;
}
