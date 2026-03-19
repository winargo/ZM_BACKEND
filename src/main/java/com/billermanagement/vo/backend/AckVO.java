package com.billermanagement.vo.backend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AckVO {
    @JsonProperty("RequestId")
    private String requestId;

    @JsonProperty("TransactionId")
    private String transactionId;

    @JsonProperty("Time")
    private String time;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Desc")
    private String desc;
}
