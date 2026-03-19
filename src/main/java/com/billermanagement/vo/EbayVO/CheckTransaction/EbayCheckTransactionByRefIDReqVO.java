package com.billermanagement.vo.EbayVO.CheckTransaction;

import com.billermanagement.vo.EbayVO.Authentication;
import com.billermanagement.vo.EbayVO.TransactionDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EbayCheckTransactionByRefIDReqVO {

    @JsonProperty("Authentication")
    private Authentication authentication;

    @JsonProperty("Transaction")
    private TransactionDetails transactionDetails;
}
