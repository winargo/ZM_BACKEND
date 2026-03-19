package com.billermanagement.vo.EbayVO.CheckTransaction;

import com.billermanagement.vo.EbayVO.ResponseStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EbayCheckTransactionResVO {

    @JsonProperty("Status")
    private String status;

    @JsonProperty("ReferenceNo")
    private String refNo;

    @JsonProperty("responseStatus")
    private ResponseStatus responseStatus;
}
