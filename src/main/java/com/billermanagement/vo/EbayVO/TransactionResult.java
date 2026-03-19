package com.billermanagement.vo.EbayVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TransactionResult {

    @JsonProperty("RequestTransferId")
    private String reqTransferId;
}
