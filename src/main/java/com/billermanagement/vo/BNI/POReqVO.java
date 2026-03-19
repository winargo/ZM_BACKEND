package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class POReqVO {
    @JsonProperty("refNumber")
    private String refNumber;

    @JsonProperty("serviceType")
    private String serviceType;

    @JsonProperty("trxDate")
    private String trxDate;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("orderingName")
    private String orderingName;

    @JsonProperty("orderingAddress1")
    private String orderingAddress1;

    @JsonProperty("orderingPhoneNumber")
    private String orderingPhoneNumber;

    @JsonProperty("beneficiaryAccount")
    private String beneficiaryAccount;

    @JsonProperty("beneficiaryName")
    private String beneficiaryName;

    @JsonProperty("beneficiaryAddress1")
    private String beneficiaryAddress1;

    @JsonProperty("beneficiaryAddress2")
    private String beneficiaryAddress2;

    @JsonProperty("beneficiaryPhoneNumber")
    private String beneficiaryPhoneNumber;

    @JsonProperty("acctWithInstcode")
    private String acctWithInstcode;

    @JsonProperty("acctWithInstName")
    private String acctWithInstName;

    @JsonProperty("acctWithInstAddress1")
    private String acctWithInstAddress1;

    @JsonProperty("acctWithInstAddress2")
    private String acctWithInstAddress2;

    @JsonProperty("acctWithInstAddress3")
    private String acctWithInstAddress3;

    @JsonProperty("detailPayment1")
    private String detailPayment1;

    @JsonProperty("detailPayment2")
    private String detailPayment2;

    @JsonProperty("detailCharges")
    private String detailCharges;

    @JsonProperty("signature")
    private String signature;
}
