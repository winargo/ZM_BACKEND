package com.billermanagement.vo.EbayVO.CheckTransaction;

import com.billermanagement.vo.EbayVO.Authentication;
import com.billermanagement.vo.EbayVO.TransactionResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EbayCheckTransactionReqVO {

    @JsonProperty("Authentication")
    private Authentication authentication;

    @JsonProperty("Transaction")
    private TransactionResult transactionResult;
}
