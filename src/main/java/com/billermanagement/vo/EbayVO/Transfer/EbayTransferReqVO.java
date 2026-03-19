package com.billermanagement.vo.EbayVO.Transfer;

import com.billermanagement.vo.EbayVO.Authentication;
import com.billermanagement.vo.EbayVO.Recepient;
import com.billermanagement.vo.EbayVO.Sender;
import com.billermanagement.vo.EbayVO.TransactionDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EbayTransferReqVO {

    @JsonProperty("Authentication")
    private Authentication authentication;

    @JsonProperty("Sender")
    private Sender sender;

    @JsonProperty("Recepient")
    private Recepient recepient;

    @JsonProperty("TransactionDetails")
    private TransactionDetails transactionDetails;
}
