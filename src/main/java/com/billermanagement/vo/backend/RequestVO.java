package com.billermanagement.vo.backend;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestVO {
    @JsonProperty("Method")
    private String method;

    @JsonProperty("ProductCode")
    private String productCode;

    @JsonProperty("PartnerId")
    private String partnerId;

    //@JsonProperty("PassPin")
    //private String passPin;

    @JsonProperty("RequestId")
    private String requestId;

    @JsonProperty("Account")
    private String account;

    @JsonProperty("Amount")
    private String amount;

    @JsonProperty("ReffId")
    private String reffId;

    @JsonProperty("AccountCode")
    private String accountCode;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("TransactionId")
    private String transactionId;

    @JsonProperty("Params")
    private List<ParamsVO> params;
}
