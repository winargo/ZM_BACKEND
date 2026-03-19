package com.billermanagement.vo.InstamoneyVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Result {

    @JsonProperty("kyc_status")
    private String kycStatus;

    @JsonProperty("account_status")
    private String accStatus;

    @JsonProperty("ewallet_account_name")
    private String ewalletAccName;
}
