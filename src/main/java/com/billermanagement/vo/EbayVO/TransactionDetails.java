package com.billermanagement.vo.EbayVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TransactionDetails {

    @JsonProperty("Amount")
    private String amount;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Timestamp")
    private String timestamp;

    @JsonProperty("ReferenceId")
    private String referenceID;
}
