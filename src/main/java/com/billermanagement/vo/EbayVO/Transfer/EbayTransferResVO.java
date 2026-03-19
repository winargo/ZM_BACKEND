package com.billermanagement.vo.EbayVO.Transfer;

import com.billermanagement.vo.EbayVO.ResponseStatus;
import com.billermanagement.vo.EbayVO.TransactionResult;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EbayTransferResVO {

    @JsonProperty("TransactionResult")
    private TransactionResult transactionResult;

    @JsonProperty("responseStatus")
    private ResponseStatus responseStatus;
}
