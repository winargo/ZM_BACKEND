package com.billermanagement.vo.backend;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseVO {
    @JsonProperty("Method")
    private String method;

    @JsonProperty("ProductCode")
    private String productCode;

    @JsonProperty("RequestId")
    private String requestId;

    @JsonProperty("TransactionId")
    private String transactionId;

    @JsonProperty("Time")
    private String time;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Amount")
    private Integer amount;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Desc")
    private String desc;

    @JsonProperty("FlowType")
    private String flowType;

    @JsonProperty("Params")
    private List<ParamsVO> params;

    @JsonProperty("Account")
    private String account;

    @JsonProperty("ReffId")
    private String reffId;

    @JsonProperty("Fee")
    private Integer fee;
}
