package com.billermanagement.vo.InstamoneyVO.Transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IMTransferReqVO {

    @JsonProperty("external_id")
    private String externalId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("description")
    private String description;

    @JsonProperty("sender_customer_id")
    private String senderId;

    @JsonProperty("recipient_customer_id")
    private String recipientId;
}
